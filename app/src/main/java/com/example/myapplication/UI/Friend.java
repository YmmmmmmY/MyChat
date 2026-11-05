package com.example.myapplication.UI;

public class Friend {
    private int avatarId;
    private String name;
    private String lastMsg;
    private String time;

    public Friend(int avatarId, String name, String lastMsg, String time) {
        this.avatarId = avatarId;
        this.name = name;
        this.lastMsg = lastMsg;
        this.time = time;
    }
    public int getAvatarId() { return avatarId; }
    public String getName() { return name; }
    public String getLastMsg() { return lastMsg; }
    public String getTime() { return time; }
}
