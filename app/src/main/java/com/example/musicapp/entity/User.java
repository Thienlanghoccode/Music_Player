package com.example.musicapp.entity;

import android.graphics.Bitmap;

public class User {
    private String username,email;
    public Bitmap avatar;
    public User(String email, String username, Bitmap avatar) {
        this.email = email;
        this.username = username;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }
}
