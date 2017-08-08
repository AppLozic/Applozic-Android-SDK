package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicommons.people.channel.Conversation;

import java.util.List;

/**
 * Created by ninu on 27/04/17.
 */

public class ConversationFeed extends JsonMarker {

    private static final String SUCCESS = "success";
    private String status;
    private String generatedAt;
    private Conversation response;
    private List<ErrorResponseFeed> errorResponse;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Conversation response) {
        this.response = response;
    }

    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }

    public List<ErrorResponseFeed> getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(List<ErrorResponseFeed> errorResponse) {
        this.errorResponse = errorResponse;
    }

    @Override
    public String toString() {
        return "ConversationFeed{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response=" + response +
                ", errorResponse=" + errorResponse +
                '}';
    }
}
