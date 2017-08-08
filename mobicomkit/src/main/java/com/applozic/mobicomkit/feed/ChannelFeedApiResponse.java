package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by sunil on 12/1/16.
 */
public class ChannelFeedApiResponse extends JsonMarker {

    public static final String SUCCESS = "success";
    private String status;
    private String generatedAt;
    private ChannelFeed response;
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

    public ChannelFeed getResponse() {
        return response;
    }

    public void setResponse(ChannelFeed response) {
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
        return "ChannelFeedApiResponse{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response=" + response +
                ", errorResponse=" + errorResponse +
                '}';
    }
}
