package org.securemessenger.server.models.network.structs;

public class Username {
    private String username;

    public Username(String username) {
        this.username = username;
    }

    public Username() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
