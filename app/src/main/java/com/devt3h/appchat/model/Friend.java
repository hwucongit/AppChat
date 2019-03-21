package com.devt3h.appchat.model;

public class Friend {
    private String receiver_id;
    private String sender_id;
    private String status;
    private String is_see;

    public Friend() {
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIs_see() {
        return is_see;
    }

    public void setIs_see(String is_see) {
        this.is_see = is_see;
    }
}
