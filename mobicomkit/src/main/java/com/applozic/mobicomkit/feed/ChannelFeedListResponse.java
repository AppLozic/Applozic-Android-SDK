package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by reytum on 22/6/17.
 */

public class ChannelFeedListResponse extends JsonMarker {

    public static final String SUCCESS = "success";
    private String status;
    private String generatedAt;
    private List<ChannelFeed> response;
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

    public List<ChannelFeed> getResponse() {
        return response;
    }

    public void setResponse(List<ChannelFeed> response) {
        this.response = response;
    }

    public List<ErrorResponseFeed> getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(List<ErrorResponseFeed> errorResponse) {
        this.errorResponse = errorResponse;
    }

    @Override
    public String toString() {
        return "ChannelFeedListResponse{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response=" + response +
                ", errorResponse=" + errorResponse +
                '}';
    }
}
