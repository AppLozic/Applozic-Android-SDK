package com.applozic.mobicommons.people.channel;


import com.applozic.mobicommons.json.JsonMarker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sunil on 5/1/16.
 */
public class Conversation extends JsonMarker {

    public static final String USER_ID_KEY = "userId";
    public static final String FALL_BACK_TEMPLATE_KEY = "fallBackTemplate";

    private Integer id;
    private String topicId;
    private String topicDetail;
    private String userId;
    private List<String> supportIds;
    private boolean created;
    private boolean closed;
    private String senderUserName;
    private String applicationKey;
    private Integer groupId;
    private ArrayList fallBackTemplatesList;
    private String topicLocalImageUri;

    public Conversation() {

    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getSupportIds() {
        return supportIds;
    }

    public void setSupportIds(List<String> supportIds) {
        this.supportIds = supportIds;
    }

    public boolean isCreated() {
        return created;
    }

    public void setCreated(boolean created) {
        this.created = created;
    }

    public String getSenderUserName() {
        return senderUserName;
    }

    public void setSenderUserName(String senderUserName) {
        this.senderUserName = senderUserName;
    }

    public String getApplicationKey() {
        return applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String getTopicDetail() {
        return topicDetail;
    }

    public void setTopicDetail(String topicDetail) {
        this.topicDetail = topicDetail;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setSenderSmsFormat(String userId, String format) {
        setSmsFormat(userId, format);
    }

    public void setReceiverSmsFormat(String userId, String format) {
        setSmsFormat(userId, format);
    }

    public String getTopicLocalImageUri() {
        return topicLocalImageUri;
    }

    public void setTopicLocalImageUri(String topicLocalImageUri) {
        this.topicLocalImageUri = topicLocalImageUri;
    }

    public void setSmsFormat(String userId, String smsFormat) {

        if (this.fallBackTemplatesList == null) {
            this.fallBackTemplatesList = new ArrayList();
        }
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(USER_ID_KEY, userId);
        map.put(FALL_BACK_TEMPLATE_KEY, smsFormat);
        fallBackTemplatesList.add(map);
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", topicId='" + topicId + '\'' +
                ", topicDetail='" + topicDetail + '\'' +
                ", userId='" + userId + '\'' +
                ", supportIds=" + supportIds +
                ", created=" + created +
                ", closed=" + closed +
                ", senderUserName='" + senderUserName + '\'' +
                ", applicationKey='" + applicationKey + '\'' +
                ", groupId=" + groupId +
                '}';
    }
}
