package com.example.musicapp.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.example.musicapp.entity.Song;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MusicService extends Service {

    private static final String TAG = "MusicService";
    private static final String ACTION_SONG_STATE_CHANGED = "com.example.musicapp.SONG_STATE_CHANGED";
    private static final String EXTRA_SONG = "song";
    private static final String EXTRA_IS_PLAYING = "is_playing";
    private static final String EXTRA_ERROR = "error";
    private static final String ACTION_PLAY = "com.example.musicapp.PLAY";
    private static final String ACTION_PAUSE = "com.example.musicapp.PAUSE";
    private static final String ACTION_RESUME = "com.example.musicapp.RESUME";
    private static final String ACTION_STOP = "com.example.musicapp.STOP";
    private static final String ACTION_NEXT = "com.example.musicapp.NEXT";
    private static final int MUSIC_NOTIFICATION_ID = 1;

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private List<Song> playlist = new ArrayList<>();
    private int currentSongIndex = -1;
    private Optional<Song> currentSong = Optional.empty();
    private boolean isPlaying = false;
    private NotificationHelper notificationHelper;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new NotificationHelper(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY:
                    Song song = intent.getParcelableExtra("SONG");
                    if (song != null) {
                        playSong(song.getPreviewUrl(), song);
                    }
                    break;
                case ACTION_PAUSE:
                    pauseSong();
                    break;
                case ACTION_RESUME:
                    resumeSong();
                    break;
                case ACTION_STOP:
                    stopSong();
                    break;
                case ACTION_NEXT:
                    playNext();
                    break;
            }
        }
        return START_STICKY;
    }

    public void setPlaylist(List<Song> songs, int index) {
        if (songs == null || index < 0 || index >= songs.size()) {
            Log.e(TAG, "Playlist không hợp lệ hoặc index không hợp lệ");
            return;
        }
        playlist = new ArrayList<>(songs);
        currentSongIndex = index;
        currentSong = Optional.ofNullable(playlist.get(currentSongIndex));
    }

    @SuppressLint("ForegroundServiceType")
    public void playSong(String url, Song song) {
        if (url == null || url.isEmpty()) {
            Log.e(TAG, "URL bài hát không hợp lệ");
            sendErrorBroadcast("Không thể phát: URL không hợp lệ");
            playNext();
            return;
        }

        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
            currentSong = Optional.ofNullable(song);
            sendStateBroadcast();
            Notification notification = notificationHelper.createMusicNotification(song, true);
            if (notification != null) {
                startForeground(MUSIC_NOTIFICATION_ID, notification);
            }
            mediaPlayer.setOnCompletionListener(mp -> playNext()); // Tự động phát bài tiếp theo
            Log.d(TAG, "Phát bài hát: " + (song != null ? song.getTitle() : "null"));
        } catch (IOException | IllegalStateException | SecurityException e) {
            Log.e(TAG, "Lỗi khi phát bài hát: " + e.getMessage(), e);
            isPlaying = false;
            sendErrorBroadcast("Không thể phát bài hát: " + e.getMessage());
            playNext();
        }
    }

    public void pauseSong() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            sendStateBroadcast();
            Log.d(TAG, "Tạm dừng bài hát");
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer.start();
            isPlaying = true;
            sendStateBroadcast();
            Log.d(TAG, "Tiếp tục bài hát");
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        currentSong = Optional.empty();
        currentSongIndex = -1;
        sendStateBroadcast();
        notificationHelper.cancelMusicNotification();
        stopForeground(true);
        Log.d(TAG, "Dừng bài hát");
    }

    public void playNext() {
        if (playlist.isEmpty() || currentSongIndex + 1 >= playlist.size()) {
            Log.d(TAG, "Không có bài hát tiếp theo");
            stopSong();
            return;
        }
        currentSongIndex++;
        currentSong = Optional.of(playlist.get(currentSongIndex));
        currentSong.ifPresent(song -> playSong(song.getPreviewUrl(), song));
    }

    public void playPrevious() {
        if (playlist.isEmpty() || currentSongIndex - 1 < 0) {
            Log.d(TAG, "Không có bài hát trước đó");
            return;
        }
        currentSongIndex--;
        currentSong = Optional.of(playlist.get(currentSongIndex));
        currentSong.ifPresent(song -> playSong(song.getPreviewUrl(), song));
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public Song getCurrentSong() {
        return currentSong.orElse(null);
    }

    public long getCurrentPosition() {
        return mediaPlayer != null ? mediaPlayer.getCurrentPosition() : 0;
    }

    public long getDuration() {
        return mediaPlayer != null ? mediaPlayer.getDuration() : 0;
    }

    @SuppressLint("ForegroundServiceType")
    private void sendStateBroadcast() {
        Intent broadcastIntent = new Intent(ACTION_SONG_STATE_CHANGED);
        currentSong.ifPresent(song -> broadcastIntent.putExtra(EXTRA_SONG, song));
        broadcastIntent.putExtra(EXTRA_IS_PLAYING, isPlaying);
        sendBroadcast(broadcastIntent);

        if (currentSong.isPresent()) {
            Notification notification = notificationHelper.createMusicNotification(currentSong.get(), isPlaying);
            if (notification != null) {
                startForeground(MUSIC_NOTIFICATION_ID, notification);
            }
        } else {
            notificationHelper.cancelMusicNotification();
            stopForeground(true);
        }
    }

    private void sendErrorBroadcast(String error) {
        Intent broadcastIntent = new Intent(ACTION_SONG_STATE_CHANGED);
        broadcastIntent.putExtra(EXTRA_ERROR, error);
        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSong();
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSong();
    }
}