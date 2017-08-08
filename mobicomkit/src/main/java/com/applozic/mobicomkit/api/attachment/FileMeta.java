package com.applozic.mobicomkit.api.attachment;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by adarsh on 4/10/14.
 */
public class FileMeta extends JsonMarker {

    private String key;
    private String userKey;
    private String blobKey;
    private String name;
    private String url;
    private int size;
    private String contentType;
    private String thumbnailUrl;
    private Long createdAtTime;

    public String getKeyString() {
        return key;
    }

    public void setKeyString(String keyString) {
        this.key = keyString;
    }

    public String getSuUserKeyString() {
        return userKey;
    }

    public void setSuUserKeyString(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    public String getBlobKeyString() {
        return blobKey;
    }

    public void setBlobKeyString(String blobKeyString) {
        this.blobKey = blobKeyString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSizeInReadableFormat() {
        String value = "0 KB";
        if (size / 1024 >= 1024) {
            value = String.valueOf(Math.round(size / (1024 * 1024))) + " MB";
        } else {
            value = String.valueOf(Math.round(size / 1024)) + " KB";
        }
        return value;
    }

    @Override
    public String toString() {
        return "FileMeta{" +
                "key='" + key + '\'' +
                ", userKey='" + userKey + '\'' +
                ", blobKey='" + blobKey + '\'' +
                ", url=" + url +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", contentType='" + contentType + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", createdAtTime=" + createdAtTime +
                '}';
    }


}
