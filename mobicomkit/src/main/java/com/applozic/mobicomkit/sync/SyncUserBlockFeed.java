package com.applozic.mobicomkit.sync;


import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by sunil on 3/3/16.
 */
public class SyncUserBlockFeed extends JsonMarker {

    private String blockedTo;
    private String blockedBy;
    private Boolean userBlocked;
    private String applicationKey;
    private Long updatedAtTime;
    private Long createdAtTime;

    public SyncUserBlockFeed() {
    }


    public String getBlockedTo() {
        return blockedTo;
    }

    public void setBlockedTo(String blockedTo) {
        this.blockedTo = blockedTo;
    }

    public String getBlockedBy() {
        return blockedBy;
    }

    public void setBlockedBy(String blockedBy) {
        this.blockedBy = blockedBy;
    }

    public Boolean getUserBlocked() {
        return userBlocked;
    }

    public void setUserBlocked(Boolean userBlocked) {
        this.userBlocked = userBlocked;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public Long getUpdatedAtTime() {
        return updatedAtTime;
    }

    public void setUpdatedAtTime(Long updatedAtTime) {
        this.updatedAtTime = updatedAtTime;
    }

    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    @Override
    public String toString() {
        return "SyncUserBlockFeed{" +
                ", blockedTo='" + blockedTo + '\'' +
                ", blockedBy='" + blockedBy + '\'' +
                ", userBlocked=" + userBlocked +
                ", applicationKey='" + applicationKey + '\'' +
                ", updatedAtTime=" + updatedAtTime +
                ", createdAtTime=" + createdAtTime +
                '}';
    }
}
