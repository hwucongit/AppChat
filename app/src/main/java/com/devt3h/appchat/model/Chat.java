package com.devt3h.appchat.model;

public class Chat {
    private String sender_id;
    private String receiver_id;
    private String message;
    private String type;
    private boolean seen;

    public Chat() {
    }

    public Chat(String sender_id, String receiver_id, String message, String type, boolean seen) {
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.message = message;
        this.type = type;
        this.seen = seen;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
