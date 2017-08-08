package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by sunil on 16/12/2016.
 */

public class ErrorResponseFeed extends JsonMarker {
    private String errorCode;
    private String description;
    private String displayMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

}
