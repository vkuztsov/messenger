package com.securemessenger.client.models.network.structs;

public class Registration {
    private String username, password, publicKey;

    public Registration(String username, String password, String publicKey) {
        this.username = username;
        this.password = password;
        this.publicKey = publicKey;
    }

    public Registration () {}

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPublicKey() {
        return publicKey;
    }
}
