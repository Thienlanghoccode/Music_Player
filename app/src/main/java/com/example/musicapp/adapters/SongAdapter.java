package com.example.musicapp.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicapp.R;
import com.example.musicapp.entity.Song;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class SongAdapter extends RecyclerView.Adapter<SongViewHolder> {

    private List<Song> songs;
    private final Consumer<Song> onSongClick;
    private final boolean isLongitudinal;
    public SongAdapter(List<Song> songs, Consumer<Song> onSongClick, boolean isLongitudinal) {
        this.songs = songs != null ? new ArrayList<>(songs) : new ArrayList<>();
        this.onSongClick = onSongClick != null ? onSongClick : song -> {};
        this.isLongitudinal = isLongitudinal;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isLongitudinal ? R.layout.song_item_longitudinal : R.layout.song_item_horizontally;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new SongViewHolder(view, isLongitudinal);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        if (position < 0 || position >= songs.size()) return;
        Song song = songs.get(position);
        if (song == null) return;

        holder.bind(song, position, isLongitudinal, onSongClick);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs != null ? new ArrayList<>(newSongs) : new ArrayList<>();
        notifyDataSetChanged();
    }
}
