package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by sunil on 19/9/16.
 */
public class InstantMessageResponse extends JsonMarker {

    private String id;
    private String type;
    private String message;
    private boolean notifyUser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isNotifyUser() {
        return notifyUser;
    }

    public void setNotifyUser(boolean notifyUser) {
        this.notifyUser = notifyUser;
    }

    @Override
    public String toString() {
        return "MqttMessageResponse{" +
                "id='" + id + '\'' +
                "type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", notifyUser=" + notifyUser +
                '}';
    }

}
