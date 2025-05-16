package com.example.musicapp.entity;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Objects;

public class Song implements Parcelable {
    private String id;
    private String title;
    private String artist;
    private String previewUrl;
    private String imageUrl;

    public Song(String id, String title, String artist, String previewUrl, String imageUrl) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.previewUrl = previewUrl;
        this.imageUrl = imageUrl;
    }

    public Song(String id, String artist, String previewUrl, String imageUrl) {
        this.id = id;
        this.artist = artist;
        this.previewUrl = previewUrl;
        this.imageUrl = imageUrl;
    }

    protected Song(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        previewUrl = in.readString();
        imageUrl = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(previewUrl);
        dest.writeString(imageUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}