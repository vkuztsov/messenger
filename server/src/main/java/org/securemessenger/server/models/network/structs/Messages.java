package org.securemessenger.server.models.network.structs;

public class Messages {
    private ChatMessage[] messages;

    public Messages(ChatMessage[] messages) {
        this.messages = messages;
    }

    public Messages() {}

    public ChatMessage[] getMessages() {
        return messages;
    }

    public void setMessages(ChatMessage[] messages) {
        this.messages = messages;
    }
}
