//package com.example.musicapp.service;
//
//import android.app.Service;
//import android.content.Intent;
//import android.media.MediaPlayer;
//import android.os.Binder;
//import android.os.IBinder;
//
//import androidx.annotation.Nullable;
//
//import com.example.musicapp.entity.Song;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MusicPlayerService extends Service {
//
//    private static MusicPlayerService instance;
//
//    private final IBinder binder = new MusicBinder();
//    private MediaPlayer mediaPlayer;
//    private ArrayList<Song> playlist;
//    private int currentIndex = 0;
//    private MusicService musicService;
//
//    public static MusicPlayerService getInstance() {
//        if (instance == null) {
//            throw new IllegalStateException("MusicPlayerService not initialized");
//        }
//        return instance;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        instance = this;
//        mediaPlayer = new MediaPlayer();
//        playlist = new ArrayList<>();
//        musicService = new MusicService();
//
//        // Set up completion listener to auto-play next song
//        mediaPlayer.setOnCompletionListener(mp -> playNext());
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return binder;
//    }
//
//    public class MusicBinder extends Binder {
//        public MusicPlayerService getService() {
//            return MusicPlayerService.this;
//        }
//    }
//
//    public void setPlaylist(List<Song> songs, int startIndex) {
//        playlist.clear();
//        playlist.addAll(songs);
//        currentIndex = startIndex;
//    }
//
//    public void playSong(Song song) {
//        try {
//            mediaPlayer.reset();
//            mediaPlayer.setDataSource(musicService.getCurrentSongUrl());
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void playAtIndex(int index) {
//        if (index >= 0 && index < playlist.size()) {
//            currentIndex = index;
//            playSong(playlist.get(currentIndex));
//        }
//    }
//
//    public void play() {
//        if (!mediaPlayer.isPlaying()) {
//            mediaPlayer.start();
//        }
//    }
//
//    public void pause() {
//        if (mediaPlayer.isPlaying()) {
//            mediaPlayer.pause();
//        }
//    }
//
//    public void playNext() {
//        if (playlist.isEmpty()) return;
//
//        currentIndex = (currentIndex + 1) % playlist.size();
//        playSong(playlist.get(currentIndex));
//    }
//
//    public void playPrevious() {
//        if (playlist.isEmpty()) return;
//
//        currentIndex = (currentIndex - 1 + playlist.size()) % playlist.size();
//        playSong(playlist.get(currentIndex));
//    }
//
//    public boolean isPlaying() {
//        return mediaPlayer.isPlaying();
//    }
//
//    public int getCurrentPosition() {
//        return mediaPlayer.getCurrentPosition();
//    }
//
//    public int getDuration() {
//        return mediaPlayer.getDuration();
//    }
//
//    public void seekTo(int position) {
//        mediaPlayer.seekTo(position);
//    }
//
//    public Song getCurrentSong() {
//        if (playlist.isEmpty()) return null;
//        return playlist.get(currentIndex);
//    }
//
//    @Override
//    public void onDestroy() {
//        if (mediaPlayer != null) {
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//        instance = null;
//        super.onDestroy();
//    }
//}