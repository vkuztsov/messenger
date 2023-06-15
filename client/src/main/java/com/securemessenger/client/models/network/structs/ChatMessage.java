package com.securemessenger.client.models.network.structs;

public class ChatMessage {
    private String username, content;

    public ChatMessage(String username, String content) {
        this.username = username;
        this.content = content;
    }

    public ChatMessage() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
