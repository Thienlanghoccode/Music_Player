package com.example.musicapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import androidx.core.app.NotificationCompat;
import com.example.musicapp.MainActivity;
import com.example.musicapp.R;
import com.example.musicapp.entity.Song;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class NotificationHelper {
    private static final String CHANNEL_ID = "MusicApp";
    protected static final int MUSIC_NOTIFICATION_ID = 1;
    private static final int ERROR_NOTIFICATION_ID = 2;
    private final Context context;
    private final NotificationManager notificationManager;


    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }


    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Music App", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Tạo thông báo điều khiển phát nhạc.
     * @param song Bài hát đang phát (có thể null).
     * @param isPlaying Trạng thái phát nhạc.
     * @return Đối tượng Notification hoặc null nếu không có bài hát.
     */
    public Notification createMusicNotification(Song song, boolean isPlaying) {
        if (song == null) {
            notificationManager.cancel(MUSIC_NOTIFICATION_ID);
            return null;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle(song.getTitle() != null ? song.getTitle() : "Không rõ tiêu đề")
                .setContentText(song.getArtist() != null ? song.getArtist() : "Không rõ nghệ sĩ")
                .setContentIntent(pendingIntent)
                .setOngoing(isPlaying);

        // Nút Play/Pause
        Intent playPauseIntent = new Intent(context, MusicService.class)
                .setAction(isPlaying ? "PAUSE" : "RESUME");
        PendingIntent playPausePendingIntent = PendingIntent.getService(
                context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(
                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                isPlaying ? "Tạm dừng" : "Tiếp tục",
                playPausePendingIntent);

        // Nút Next
        Intent nextIntent = new Intent(context, MusicService.class).setAction("NEXT");
        PendingIntent nextPendingIntent = PendingIntent.getService(
                context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_next, "Tiếp theo", nextPendingIntent);

        // Nút Stop
        Intent stopIntent = new Intent(context, MusicService.class).setAction("STOP");
        PendingIntent stopPendingIntent = PendingIntent.getService(
                context, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.ic_stop, "Dừng", stopPendingIntent);

        // Tải hình ảnh bài hát bất đồng bộ
        if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(song.getImageUrl())
                    .resize(128, 128)
                    .centerCrop()
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            builder.setLargeIcon(bitmap);
                            notificationManager.notify(MUSIC_NOTIFICATION_ID, builder.build());
                        }

                        @Override
                        public void onBitmapFailed(Exception e, android.graphics.drawable.Drawable errorDrawable) {
                            builder.setLargeIcon((Bitmap) null);
                            notificationManager.notify(MUSIC_NOTIFICATION_ID, builder.build());
                        }

                        @Override
                        public void onPrepareLoad(android.graphics.drawable.Drawable placeHolderDrawable) {
                            builder.setLargeIcon((Bitmap) null);
                            notificationManager.notify(MUSIC_NOTIFICATION_ID, builder.build());
                        }
                    });
        } else {
            builder.setLargeIcon((Bitmap) null);
            notificationManager.notify(MUSIC_NOTIFICATION_ID, builder.build());
        }

        return builder.build();
    }

    /**
     * Hiển thị thông báo lỗi (ví dụ: lỗi tìm kiếm).
     * @param errorMessage Thông điệp lỗi.
     */
    public void showErrorNotification(String errorMessage) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.music_icon)
                .setContentTitle("Lỗi")
                .setContentText(errorMessage != null ? errorMessage : "Đã xảy ra lỗi")
                .setAutoCancel(true);

        notificationManager.notify(ERROR_NOTIFICATION_ID, builder.build());
    }


    public void cancelMusicNotification() {
        notificationManager.cancel(MUSIC_NOTIFICATION_ID);
    }
}