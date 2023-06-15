package com.securemessenger.client.models.network;

public class RMessage {
    private RType type;
    private Object content;

    public RMessage(RType type, Object content) {
        this.type = type;
        this.content = content;
    }

    public RMessage() {}

    public RType getType() {
        return type;
    }

    public void setType(RType type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
