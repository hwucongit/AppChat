package com.devt3h.appchat.model;

public class User {
    private String name;
    private String email;
    private String avatarURL;
    private String id;
    private String birthday;
    private String country;
    private String status;
    private String career;
    private boolean online;
    private long lastUpdateStatus;

    public User() {
    }


    public User(String name, String email, String avatarURL, String id, String birthday, boolean online) {
        this.name = name;
        this.email = email;
        this.avatarURL = avatarURL;
        this.id = id;
        this.birthday = birthday;
        this.online = online;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public boolean isOnline() {
        return online;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public long getLastUpdateStatus() {
        return lastUpdateStatus;
    }

    public void setLastUpdateStatus(long lastUpdateStatus) {
        this.lastUpdateStatus = lastUpdateStatus;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
