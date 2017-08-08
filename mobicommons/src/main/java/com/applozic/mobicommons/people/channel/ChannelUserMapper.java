package com.applozic.mobicommons.people.channel;

import com.applozic.mobicommons.json.JsonMarker;

import java.io.Serializable;

/**
 * Created by sunil on 28/12/15.
 */
public class ChannelUserMapper extends JsonMarker {

    private Integer key;
    private String userKey;
    private short status;
    private int unreadCount;

    public ChannelUserMapper() {
    }

    public ChannelUserMapper(Integer key, String userKey, int unreadCount) {
        this.key = key;
        this.userKey = userKey;
        this.unreadCount = unreadCount;
    }

    public ChannelUserMapper(Integer key, String userKey) {
        this.key = key;
        this.userKey = userKey;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ChannelUserMapper{" +
                "key=" + key +
                ", userKey='" + userKey + '\'' +
                ", status=" + status +
                '}';
    }
}
