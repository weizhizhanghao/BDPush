package com.example.think.emulatebd.bean;

import com.example.think.emulatebd.app.PushApplication;
import com.example.think.emulatebd.common.util.SharePreferenceUtil;
import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by HuangMei on 2016/12/15.
 */

public class Message implements Serializable{
    private static final long serialVersionUID = 1L;
    @Expose
    private String user_id;
    @Expose
    private String channel_id;
    @Expose
    private String nick;
    @Expose
    private int head_id;
    @Expose
    private long time_samp;
    @Expose
    private String message;
    @Expose
    private String tag;

    public Message(long time_samp, String message, String tag) {
        super();
        SharePreferenceUtil util = PushApplication.getInstance().getSpUtil();
        this.user_id = util.getUserId();
        this.channel_id = util.getChannelId();
        this.nick = util.getNick();
        this.head_id = util.getHeadIcon();
        this.time_samp = time_samp;
        this.message = message;
        this.tag = tag;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getHead_id() {
        return head_id;
    }

    public void setHead_id(int head_id) {
        this.head_id = head_id;
    }

    public long getTime_samp() {
        return time_samp;
    }

    public void setTime_samp(long time_samp) {
        this.time_samp = time_samp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
