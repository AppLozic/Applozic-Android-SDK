package com.applozic.mobicomkit.api.notification;

import android.text.TextUtils;

import com.applozic.mobicommons.json.JsonMarker;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Adarsh on 12/30/16.
 */

public class MuteNotificationRequest extends JsonMarker {

    String userId;
    @SerializedName("id")
    Integer groupId;            //Group unique identifier
    String clientGroupId;        //	Client Group unique identifier
    Long notificationAfterTime; //Time Interval for which notification has be be disabled

    public MuteNotificationRequest(String clientGroupId, Long notificationAfterTime) {
        this.clientGroupId = clientGroupId;
        this.notificationAfterTime = notificationAfterTime;
    }

    public MuteNotificationRequest(Integer groupId, Long notificationAfterTime) {
        this.groupId = groupId;
        this.notificationAfterTime = notificationAfterTime;
    }

    public MuteNotificationRequest(Long notificationAfterTime, String userId) {
        this.notificationAfterTime = notificationAfterTime;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getId() {
        return groupId;
    }

    public void setId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public Long getNotificationAfterTime() {
        return notificationAfterTime;
    }

    public void setNotificationAfterTime(Long notificationAfterTime) {
        this.notificationAfterTime = notificationAfterTime;
    }

    public boolean isRequestValid() {

        return !((notificationAfterTime == null || notificationAfterTime <= 0) ||
                (TextUtils.isEmpty(userId) && TextUtils.isEmpty(clientGroupId) && groupId == null));
    }
}
