package com.applozic.mobicommons.people.channel;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * Created by sunil on 3/9/16.
 */
public class ChannelMetadata extends JsonMarker {

    public final static String CREATE_GROUP_MESSAGE = "CREATE_GROUP_MESSAGE";
    public final static String REMOVE_MEMBER_MESSAGE = "REMOVE_MEMBER_MESSAGE";
    public final static String ADD_MEMBER_MESSAGE = "ADD_MEMBER_MESSAGE";
    public final static String JOIN_MEMBER_MESSAGE = "JOIN_MEMBER_MESSAGE";
    public final static String GROUP_NAME_CHANGE_MESSAGE = "GROUP_NAME_CHANGE_MESSAGE";
    public final static String GROUP_ICON_CHANGE_MESSAGE = "GROUP_ICON_CHANGE_MESSAGE";
    public final static String GROUP_LEFT_MESSAGE = "GROUP_LEFT_MESSAGE";
    public final static String DELETED_GROUP_MESSAGE = "DELETED_GROUP_MESSAGE";
    public final static String HIDE_METADATA_NOTIFICATION = "HIDE";

    public static final String ADMIN_NAME = ":adminName";
    public static final String GROUP_NAME = ":groupName";
    public static final String USER_NAME = ":userName";

    private String createGroupMessage;
    private String removeMemberMessage;
    private String addMemberMessage;
    private String JoinMemberMessage;
    private String groupNameChangeMessage;
    private String groupIconChangeMessage;
    private String groupLeftMessage;
    private String deletedGroupMessage;
    private boolean hideMetaDataNotification;


    public String getCreateGroupMessage() {
        return createGroupMessage;
    }

    public void setCreateGroupMessage(String createGroupMessage) {
        this.createGroupMessage = createGroupMessage;
    }

    public String getRemoveMemberMessage() {
        return removeMemberMessage;
    }

    public void setRemoveMemberMessage(String removeMemberMessage) {
        this.removeMemberMessage = removeMemberMessage;
    }

    public String getAddMemberMessage() {
        return addMemberMessage;
    }

    public void setAddMemberMessage(String addMemberMessage) {
        this.addMemberMessage = addMemberMessage;
    }

    public String getJoinMemberMessage() {
        return JoinMemberMessage;
    }

    public void setJoinMemberMessage(String joinMemberMessage) {
        JoinMemberMessage = joinMemberMessage;
    }

    public String getGroupNameChangeMessage() {
        return groupNameChangeMessage;
    }

    public void setGroupNameChangeMessage(String groupNameChangeMessage) {
        this.groupNameChangeMessage = groupNameChangeMessage;
    }

    public String getGroupIconChangeMessage() {
        return groupIconChangeMessage;
    }

    public void setGroupIconChangeMessage(String groupIconChangeMessage) {
        this.groupIconChangeMessage = groupIconChangeMessage;
    }

    public String getGroupLeftMessage() {
        return groupLeftMessage;
    }

    public void setGroupLeftMessage(String groupLeftMessage) {
        this.groupLeftMessage = groupLeftMessage;
    }

    public String getDeletedGroupMessage() {
        return deletedGroupMessage;
    }

    public void setDeletedGroupMessage(String deletedGroupMessage) {
        this.deletedGroupMessage = deletedGroupMessage;
    }

    public boolean getHideMetaDataNotification() {
        return hideMetaDataNotification;
    }

    public void setHideMetaDataNotification(boolean hideMetaDataNotification) {
        this.hideMetaDataNotification = hideMetaDataNotification;
    }
}
