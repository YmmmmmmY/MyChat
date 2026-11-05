package com.example.myapplication.UI;

public class Song {
    private String songName;
    private int coverId;
    private int audioId;

    public Song(String songName, int coverId, int audioId) {
        this.songName = songName;
        this.coverId = coverId;
        this.audioId = audioId;
    }

    public String getSongName() {
        return songName;
    }

    public int getCoverId() {
        return coverId;
    }

    public int getAudioId() {
        return audioId;
    }
}
