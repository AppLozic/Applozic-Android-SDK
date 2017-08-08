package com.applozic.mobicomkit.api.conversation;

/**
 * Created by devashish on 28/03/16.
 */

import com.applozic.mobicommons.json.JsonMarker;


public class MessageInfo extends JsonMarker {


    String userId;
    Long deliveredAtTime;
    Long readAtTime;
    Short status;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getDeliveredAtTime() {
        return deliveredAtTime;
    }

    public void setDeliveredAtTime(Long deliveredAtTime) {
        this.deliveredAtTime = deliveredAtTime;
    }

    public Long getReadAtTime() {
        return readAtTime;
    }

    public void setReadAtTime(Long readAtTime) {
        this.readAtTime = readAtTime;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public boolean isRead() {
        return readAtTime != null;
    }


    @Override
    public String toString() {
        return "MessageInfo{" +
                "userId='" + userId + '\'' +
                ", deliveredAtTime=" + deliveredAtTime +
                ", readAtTime=" + readAtTime +
                ", status=" + status +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageInfo that = (MessageInfo) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (deliveredAtTime != null ? !deliveredAtTime.equals(that.deliveredAtTime) : that.deliveredAtTime != null)
            return false;
        if (readAtTime != null ? !readAtTime.equals(that.readAtTime) : that.readAtTime != null)
            return false;
        return !(status != null ? !status.equals(that.status) : that.status != null);

    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (deliveredAtTime != null ? deliveredAtTime.hashCode() : 0);
        result = 31 * result + (readAtTime != null ? readAtTime.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
