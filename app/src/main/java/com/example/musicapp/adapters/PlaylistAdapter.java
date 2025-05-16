package com.example.musicapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.entity.Playlist;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<Playlist> playlists;
    private final Consumer<Playlist> onPlaylistClick;

    public PlaylistAdapter(List<Playlist> playlists, Consumer<Playlist> onPlaylistClick) {
        this.playlists = playlists != null ? new ArrayList<>(playlists) : new ArrayList<>();
        this.onPlaylistClick = onPlaylistClick != null ? onPlaylistClick : playlist -> {};
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_item, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        if (position < 0 || position >= playlists.size()) return;
        Playlist playlist = playlists.get(position);
        if (playlist == null) return;

        holder.bind(playlist);
        holder.itemView.setOnClickListener(v -> onPlaylistClick.accept(playlist));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updatePlaylists(List<Playlist> newPlaylists) {
        this.playlists = newPlaylists != null ? new ArrayList<>(newPlaylists) : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private final ImageView playlistImage;
        private final TextView playlistTitle;
        private final TextView playlistDescription;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistImage = itemView.findViewById(R.id.playlist_image);
            playlistTitle = itemView.findViewById(R.id.playlist_title);
            playlistDescription = itemView.findViewById(R.id.playlist_description);
        }

        void bind(Playlist playlist) {
            if (playlist == null) return;

            playlistTitle.setText(playlist.getTitle() != null ? playlist.getTitle() : "Không rõ tiêu đề");
            playlistDescription.setText(playlist.getDescription() != null ? playlist.getDescription() : "Không có mô tả");

            if (playlist.getImageUrl() != null && !playlist.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(playlist.getImageUrl())
                        .placeholder(R.drawable.music_icon)
                        .error(R.drawable.music_icon)
                        .into(playlistImage);
            } else {
                playlistImage.setImageResource(R.drawable.music_icon);
            }
        }
    }
}
