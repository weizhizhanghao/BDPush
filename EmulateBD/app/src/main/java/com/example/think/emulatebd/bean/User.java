package com.example.think.emulatebd.bean;

import java.io.Serializable;

/**
 * Created by HuangMei on 2016/12/15.
 */

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String UserId;//
    private String channelId;
    private String nick;//
    private int headIcon;//
    private int group;

    public User() {
    }

    public User(String userId, String channelId, String nick, int headIcon, int group) {
        UserId = userId;
        this.channelId = channelId;
        this.nick = nick;
        this.headIcon = headIcon;
        this.group = group;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(int headIcon) {
        this.headIcon = headIcon;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return "User [UserId=" + UserId + ", channelId=" + channelId
                + ", nick=" + nick + ", headIcon=" + headIcon + ", group="
                + group + "]";
    }
}
