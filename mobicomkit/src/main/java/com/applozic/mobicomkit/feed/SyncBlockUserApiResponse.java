package com.applozic.mobicomkit.feed;

import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicomkit.sync.SyncUserBlockListFeed;

/**
 * Created by sunil on 17/3/16.
 */
public class SyncBlockUserApiResponse extends JsonMarker {

    public static final String SUCCESS = "success";
    private String status;
    private String generatedAt;
    private SyncUserBlockListFeed response;

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

    public SyncUserBlockListFeed getResponse() {
        return response;
    }

    public void setResponse(SyncUserBlockListFeed response) {
        this.response = response;
    }


    public boolean isSuccess() {
        return SUCCESS.equals(status);
    }


    @Override
    public String toString() {
        return "ApiResponse{" +
                "status='" + status + '\'' +
                ", generatedAt='" + generatedAt + '\'' +
                ", response='" + response + '\'' +
                '}';
    }

}
