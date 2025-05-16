package com.example.musicapp.entity;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Playlist implements Parcelable {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private List<Song> songs;

    public Playlist(String id, String title, String description, String imageUrl, List<Song> songs) {
        this.id = id != null ? id : "";
        this.title = title != null ? title : "Danh sách phát không tên";
        this.description = description != null ? description : "";
        this.imageUrl = imageUrl != null ? imageUrl : "";
        this.songs = songs != null ? new ArrayList<>(songs) : new ArrayList<>();
    }

    protected Playlist(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        songs = new ArrayList<>();
        in.readList(songs, Song.class.getClassLoader());
    }

    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeList(songs);
    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id != null ? id : "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title : "Danh sách phát không tên";
    }

    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }


    public List<Song> getSongs() {
        return Collections.unmodifiableList(songs);
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs != null ? new ArrayList<>(songs) : new ArrayList<>();
    }

    public boolean addSong(Song song) {
        if (song == null || songs.contains(song)) {
            return false;
        }
        return songs.add(song);
    }

    public boolean removeSong(Song song) {
        if (song == null) {
            return false;
        }
        return songs.remove(song);
    }

    public int getSongCount() {
        return songs.size();
    }


    @NonNull
    @Override
    public String toString() {
        return "Playlist{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", songCount=" + songs.size() +
                '}';
    }
}
