package com.applozic.mobicomkit.api.account.user;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by sunil on 24/11/15.
 */
public class UserDetail extends JsonMarker {

    private String userId;
    private boolean connected;
    private String displayName;
    private Long lastSeenAtTime;
    private String imageLink;
    private Integer unreadCount;
    private String phoneNumber;
    private String statusMessage;
    private Short userTypeId;
    private Long deletedAtTime;


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Long getLastSeenAtTime() {
        return lastSeenAtTime;
    }

    public void setLastSeenAtTime(Long lastSeenAtTime) {
        this.lastSeenAtTime = lastSeenAtTime;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Short getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(Short userTypeId) {
        this.userTypeId = userTypeId;
    }

    public Long getDeletedAtTime() {
        return deletedAtTime;
    }

    public void setDeletedAtTime(Long deletedAtTime) {
        this.deletedAtTime = deletedAtTime;
    }


    @Override
    public String toString() {
        return "UserDetail{" +
                "userId='" + userId + '\'' +
                ", connected=" + connected +
                ", displayName='" + displayName + '\'' +
                ", lastSeenAtTime=" + lastSeenAtTime +
                ", imageLink='" + imageLink + '\'' +
                ", unreadCount=" + unreadCount +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", userTypeId=" + userTypeId +
                ", deletedAtTime=" + deletedAtTime +
                '}';
    }
}
