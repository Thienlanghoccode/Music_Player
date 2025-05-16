package com.example.musicapp.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.entity.Song;
import com.squareup.picasso.Picasso;
import java.util.function.Consumer;

/**
 * ViewHolder cho mục bài hát trong RecyclerView.
 * Hỗ trợ hiển thị bài hát ở cả layout ngang (horizontally) và dọc (longitudinal).
 */
public class SongViewHolder extends RecyclerView.ViewHolder {
    private final TextView number;
    private final ImageView songImage;
    private final TextView songTitle;
    private final TextView songArtist;

    public SongViewHolder(@NonNull View itemView, boolean isLongitudinal) {
        super(itemView);
        number = isLongitudinal ? itemView.findViewById(R.id.song_number) : null;
        songImage = itemView.findViewById(R.id.song_image);
        songTitle = itemView.findViewById(R.id.song_title);
        songArtist = itemView.findViewById(R.id.song_artist);
    }

    public void bind(Song song, int position, boolean isLongitudinal, Consumer<Song> onSongClick) {
        if (song == null) return;

        // Hiển thị số thứ tự nếu là danh sách dọc
        if (isLongitudinal && number != null) {
            number.setVisibility(View.VISIBLE);
            number.setText(String.valueOf(position + 1));
        } else if (number != null) {
            number.setVisibility(View.GONE);
        }

        // Cập nhật tiêu đề và nghệ sĩ
        songTitle.setText(song.getTitle() != null ? song.getTitle() : "Không rõ tiêu đề");
        songArtist.setText(song.getArtist() != null ? song.getArtist() : "Không rõ nghệ sĩ");

        // Tải hình ảnh bài hát
        if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(song.getImageUrl())
                    .placeholder(R.drawable.music_icon)
                    .error(R.drawable.music_icon)
                    .into(songImage);
        } else {
            songImage.setImageResource(R.drawable.music_icon);
        }

        itemView.setOnClickListener(v -> onSongClick.accept(song));
    }
}
