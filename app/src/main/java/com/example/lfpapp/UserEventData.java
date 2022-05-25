package com.example.lfpapp;

public class UserEventData {
    String beatmapID;
    String beatmapSetID;
    String date;
    String title;
    String username;
    int mode;

    public UserEventData(String beatmapID, String beatmapSetID, String date, String title, String username, int mode) {
        this.beatmapID = beatmapID;
        this.beatmapSetID = beatmapSetID;
        this.date = date;
        this.title = title;
        this.username = username;
        this.mode = mode;
    }

    public String getBeatmapID() {
        return beatmapID;
    }

    public void setBeatmapID(String beatmapID) {
        this.beatmapID = beatmapID;
    }

    public String getBeatmapSetID() {
        return beatmapSetID;
    }

    public void setBeatmapSetID(String beatmapSetID) {
        this.beatmapSetID = beatmapSetID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
