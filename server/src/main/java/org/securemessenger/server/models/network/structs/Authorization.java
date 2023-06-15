package org.securemessenger.server.models.network.structs;

public class Authorization {
    private String key;

    public Authorization(String key) {
        this.key = key;
    }

    public Authorization() {}

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
