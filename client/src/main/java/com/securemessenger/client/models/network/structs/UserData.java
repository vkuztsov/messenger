package com.securemessenger.client.models.network.structs;

public class UserData {
    private String username, photoUrl, publicKey;

    public UserData(String username, String photoUrl, String publicKey) {
        this.username = username;
        this.photoUrl = photoUrl;
        this.publicKey = publicKey;
    }

    public UserData() {}

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
