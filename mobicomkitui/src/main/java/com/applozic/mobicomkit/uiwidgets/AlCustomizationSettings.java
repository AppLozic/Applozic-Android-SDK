package com.applozic.mobicomkit.uiwidgets;

import com.applozic.mobicomkit.uiwidgets.conversation.MobicomMessageTemplate;
import com.applozic.mobicommons.json.JsonMarker;

import java.util.Map;

/**
 * Created by sunil on 10/10/16.
 */
public class AlCustomizationSettings extends JsonMarker {


    public String customMessageBackgroundColor = "#FF03A9F4";
    private String sentMessageBackgroundColor = "#FF03A9F4";
    private String receivedMessageBackgroundColor = "#FFFFFFFF";
    private String sendButtonBackgroundColor = "#FF03A9F4";
    private String attachmentIconsBackgroundColor = "#FF03A9F4";
    private String chatBackgroundColorOrDrawable;
    private String editTextBackgroundColorOrDrawable;
    private String editTextLayoutBackgroundColorOrDrawable;
    private String channelCustomMessageBgColor = "#cccccc";

    private String sentContactMessageTextColor = "#FFFFFFFF";
    private String receivedContactMessageTextColor = "#000000";
    private String sentMessageTextColor = "#FFFFFFFF";
    private String receivedMessageTextColor = "#000000";
    private String messageEditTextTextColor = "#000000";
    private String sentMessageLinkTextColor = "#FFFFFFFF";
    private String receivedMessageLinkTextColor = "#5fba7d";
    private String messageEditTextHintTextColor = "#bdbdbd";
    private String typingTextColor;
    private String noConversationLabelTextColor = "#000000";
    private String conversationDateTextColor = "#333333";
    private String conversationDayTextColor = "#333333";
    private String messageTimeTextColor = "#838b83";
    private String channelCustomMessageTextColor = "#666666";

    private String sentMessageBorderColor = "#FF03A9F4";
    private String receivedMessageBorderColor = "#FFFFFFFF";
    private String channelCustomMessageBorderColor = "#cccccc";
    private String collapsingToolbarLayoutColor = "#FF03A9F4";
    private String groupParticipantsTextColor = "#FF03A9F4";
    private String groupDeleteButtonBackgroundColor = "#FF03A9F4";
    private String groupExitButtonBackgroundColor = "#FF03A9F4";
    private String adminTextColor = "#FF03A9F4";
    private String adminBackgroundColor = "#FFFFFFFF";
    private String attachCameraIconName = "applozic_ic_action_camera_new";
    private String adminBorderColor = "#FF03A9F4";
    private String userNotAbleToChatTextColor = "#000000";
    private String chatBackgroundImageName;

    private String audioPermissionNotFoundMsg;
    private String noConversationLabel = "You have no conversations";
    private String noSearchFoundForChatMessages = "No conversation found";
    private String restrictedWordMessage = "Restricted words are not allowed";
    private boolean locationShareViaMap = true;
    private boolean startNewFloatingButton;
    private boolean startNewButton = true;
    private boolean onlineStatusMasterList;
    private boolean priceWidget;
    private boolean startNewGroup = true;
    private boolean imageCompression;
    private boolean inviteFriendsInContactActivity;
    private boolean registeredUserContactListCall;
    private boolean createAnyContact;
    private boolean showActionDialWithOutCalling;
    private boolean profileLogoutButton;
    private boolean userProfileFragment = true;
    private boolean messageSearchOption;
    private boolean conversationContactImageVisibility = true;
    private boolean hideGroupAddMembersButton;
    private boolean hideGroupNameUpdateButton;
    private boolean hideGroupExitButton;
    private boolean hideGroupRemoveMemberOption;
    private boolean profileOption;
    private boolean broadcastOption;
    private boolean hideAttachmentButton;
    private boolean groupUsersOnlineStatus;
    private boolean refreshOption = true;
    private boolean deleteOption = true;
    private boolean blockOption = true;
    private boolean muteOption = true;
    private MobicomMessageTemplate messageTemplate;
    private String logoutPackageName;
    private String groupDeletePackageName;
    private boolean logoutOption = false;
    private int defaultGroupType = 2;
    private boolean muteUserChatOption = false;

    private int totalRegisteredUserToFetch = 100;
    private int maxAttachmentAllowed = 5;
    private int maxAttachmentSizeAllowed = 30;
    private int totalOnlineUsers = 0;
    private String themeColorPrimary;
    private String themeColorPrimaryDark;
    private String editTextHintText = "Write a Message..";
    private boolean replyOption = true;
    private String replyMessageLayoutSentMessageBackground = "#C0C0C0";
    private String replyMessageLayoutReceivedMessageBackground = "#F5F5F5";
    private boolean groupInfoScreenVisible = true;
    private boolean forwardOption = true;
    private boolean recordButton = true;
    private boolean showAllDeviceContacts = true;
    private boolean poweredByApplozic = false;
    private boolean enableImageCompression = false;
    private boolean disableGlobalStoragePermission = true;
    private boolean enableMessageFastScroll = false;
    private boolean isContactSearchFromServer = false;
    private String messageSearchTextColor = "#5fba7d";
    private int notificationDisableThreshold = 0;
    private boolean hideGroupDeleteButton = false;
    private boolean hideGroupSubtitle = false;

    private boolean launchChatFromProfilePicOrName = false;

    private boolean showImageOnToolbar = false;
    private String restrictedWordRegex;
    private boolean enableMessageReport = true;
    private boolean deleteMessageOption = true;

    private Map<String, Boolean> attachmentOptions;

    private Map<String, Boolean> filterGallery;

    public boolean isBroadcastOption() {
        return broadcastOption;
    }

    public boolean isStartNewFloatingButton() {
        return startNewFloatingButton;
    }

    public boolean isStartNewButton() {
        return startNewButton;
    }

    public String getNoConversationLabel() {
        return noConversationLabel;
    }

    public String getCustomMessageBackgroundColor() {
        return customMessageBackgroundColor;
    }


    public String getSentMessageBackgroundColor() {
        return sentMessageBackgroundColor;
    }

    public String getReceivedMessageBackgroundColor() {
        return receivedMessageBackgroundColor;
    }

    public boolean isOnlineStatusMasterList() {
        return onlineStatusMasterList;
    }

    public boolean isPriceWidget() {
        return priceWidget;
    }

    public String getSendButtonBackgroundColor() {
        return sendButtonBackgroundColor;
    }

    public boolean isStartNewGroup() {
        return startNewGroup;
    }

    public boolean isImageCompression() {
        return imageCompression;
    }


    public boolean isInviteFriendsInContactActivity() {
        return inviteFriendsInContactActivity;
    }

    public String getAttachmentIconsBackgroundColor() {
        return attachmentIconsBackgroundColor;
    }

    public boolean isLocationShareViaMap() {
        return locationShareViaMap;
    }

    public boolean isConversationContactImageVisibility() {
        return conversationContactImageVisibility;
    }

    public String getSentContactMessageTextColor() {
        return sentContactMessageTextColor;
    }

    public String getReceivedContactMessageTextColor() {
        return receivedContactMessageTextColor;
    }

    public String getSentMessageTextColor() {
        return sentMessageTextColor;
    }

    public String getReceivedMessageTextColor() {
        return receivedMessageTextColor;
    }

    public String getSentMessageBorderColor() {
        return sentMessageBorderColor;
    }

    public String getReceivedMessageBorderColor() {
        return receivedMessageBorderColor;
    }

    public String getChatBackgroundColorOrDrawable() {
        return chatBackgroundColorOrDrawable;
    }

    public String getMessageEditTextTextColor() {
        return messageEditTextTextColor;
    }

    public String getAudioPermissionNotFoundMsg() {
        return audioPermissionNotFoundMsg;
    }

    public boolean isRegisteredUserContactListCall() {
        return registeredUserContactListCall;
    }

    public boolean isCreateAnyContact() {
        return createAnyContact;
    }

    public boolean isShowActionDialWithOutCalling() {
        return showActionDialWithOutCalling;
    }

    public String getSentMessageLinkTextColor() {
        return sentMessageLinkTextColor;
    }

    public String getReceivedMessageLinkTextColor() {
        return receivedMessageLinkTextColor;
    }

    public String getMessageEditTextHintTextColor() {
        return messageEditTextHintTextColor;
    }

    public boolean isHideGroupAddMembersButton() {
        return hideGroupAddMembersButton;
    }

    public boolean isHideGroupNameUpdateButton() {
        return hideGroupNameUpdateButton;
    }

    public boolean isHideGroupExitButton() {
        return hideGroupExitButton;
    }

    public boolean isHideGroupRemoveMemberOption() {
        return hideGroupRemoveMemberOption;
    }


    public String getEditTextBackgroundColorOrDrawable() {
        return editTextBackgroundColorOrDrawable;
    }

    public String getEditTextLayoutBackgroundColorOrDrawable() {
        return editTextLayoutBackgroundColorOrDrawable;
    }

    public String getTypingTextColor() {
        return typingTextColor;
    }

    public boolean isProfileOption() {
        return profileOption;
    }

    public String getNoConversationLabelTextColor() {
        return noConversationLabelTextColor;
    }

    public String getConversationDateTextColor() {
        return conversationDateTextColor;
    }

    public String getConversationDayTextColor() {
        return conversationDayTextColor;
    }

    public String getMessageTimeTextColor() {
        return messageTimeTextColor;
    }

    public String getChannelCustomMessageBgColor() {
        return channelCustomMessageBgColor;
    }

    public String getChannelCustomMessageBorderColor() {
        return channelCustomMessageBorderColor;
    }

    public String getChannelCustomMessageTextColor() {
        return channelCustomMessageTextColor;
    }

    public String getNoSearchFoundForChatMessages() {
        return noSearchFoundForChatMessages;
    }

    public boolean isProfileLogoutButton() {
        return profileLogoutButton;
    }

    public boolean isUserProfileFragment() {
        return userProfileFragment;
    }

    public boolean isMessageSearchOption() {
        return messageSearchOption;
    }


    public int getTotalRegisteredUserToFetch() {
        return totalRegisteredUserToFetch;
    }


    public int getMaxAttachmentAllowed() {
        return maxAttachmentAllowed;
    }

    public int getMaxAttachmentSizeAllowed() {
        return maxAttachmentSizeAllowed;
    }

    public int getTotalOnlineUsers() {
        return totalOnlineUsers;
    }

    public String getCollapsingToolbarLayoutColor() {
        return collapsingToolbarLayoutColor;
    }

    public String getGroupParticipantsTextColor() {
        return groupParticipantsTextColor;
    }

    public String getGroupExitButtonBackgroundColor() {
        return groupExitButtonBackgroundColor;
    }

    public String getGroupDeleteButtonBackgroundColor() {
        return groupDeleteButtonBackgroundColor;
    }

    public String getAdminTextColor() {
        return adminTextColor;
    }

    public String getAdminBackgroundColor() {
        return adminBackgroundColor;
    }

    public String getAttachCameraIconName() {
        return attachCameraIconName;
    }

    public String getAdminBorderColor() {
        return adminBorderColor;
    }

    public String getUserNotAbleToChatTextColor() {
        return userNotAbleToChatTextColor;
    }

    public String getChatBackgroundImageName() {
        return chatBackgroundImageName;
    }

    public Map<String, Boolean> getAttachmentOptions() {
        return attachmentOptions;
    }

    public void setAttachmentOptions(Map<String, Boolean> attachmentOptions) {
        this.attachmentOptions = attachmentOptions;
    }


    public boolean isHideAttachmentButton() {
        return hideAttachmentButton;
    }

    public void setHideAttachmentButton(boolean hideAttachmentButton) {
        this.hideAttachmentButton = hideAttachmentButton;
    }

    public String getRestrictedWordMessage() {
        return restrictedWordMessage;
    }

    public void setRestrictedWordMessage(String restrictedWordMessage) {
        this.restrictedWordMessage = restrictedWordMessage;
    }

    public boolean isLaunchChatFromProfilePicOrName() {
        return launchChatFromProfilePicOrName;
    }

    public boolean isGroupUsersOnlineStatus() {
        return groupUsersOnlineStatus;
    }


    public boolean isRefreshOption() {
        return refreshOption;
    }

    public void setRefreshOption(boolean refreshOption) {
        this.refreshOption = refreshOption;
    }

    public boolean isDeleteOption() {
        return deleteOption;
    }

    public void setDeleteOption(boolean deleteOption) {
        this.deleteOption = deleteOption;
    }

    public boolean isBlockOption() {
        return blockOption;
    }

    public void setBlockOption(boolean blockOption) {
        this.blockOption = blockOption;
    }

    public boolean isMuteOption() {
        return muteOption;
    }

    public void setMuteOption(boolean muteOption) {
        this.muteOption = muteOption;
    }

    public boolean isLogoutOption() {
        return logoutOption;
    }

    public void setLogout(boolean logoutOption) {
        this.logoutOption = logoutOption;
    }

    public String getLogoutPackage() {
        return logoutPackageName;
    }

    public void setLogoutPackageName(String logoutPackageName) {
        this.logoutPackageName = logoutPackageName;
    }

    public String getGroupDeletePackageName() {
        return groupDeletePackageName;
    }

    public String getThemeColorPrimary() {
        return themeColorPrimary;
    }

    public String getThemeColorPrimaryDark() {
        return themeColorPrimaryDark;
    }

    public String getEditTextHintText() {
        return editTextHintText;
    }

    public boolean isReplyOption() {
        return replyOption;
    }

    public void setReplyOption(boolean replyOption) {
        this.replyOption = replyOption;
    }

    public String getReplyMessageLayoutSentMessageBackground() {
        return replyMessageLayoutSentMessageBackground;
    }

    public String getReplyMessageLayoutReceivedMessageBackground() {
        return replyMessageLayoutReceivedMessageBackground;
    }

    public void setUserChatMuteOption(boolean muteUserChatOption) {
        this.muteUserChatOption = muteUserChatOption;
    }

    public boolean isMuteUserChatOption() {
        return muteUserChatOption;
    }

    public boolean isGroupInfoScreenVisible() {
        return groupInfoScreenVisible;
    }

    public boolean isForwardOption() {
        return forwardOption;
    }

    public void setForwardOption(boolean forwardOption) {
        this.forwardOption = forwardOption;
    }

    public boolean isRecordButton() {
        return recordButton;
    }

    public void setRecordButton(boolean recordButton) {
        this.recordButton = recordButton;
    }

    public int getDefaultGroupType() {
        return defaultGroupType;
    }

    public void setDefaultGroupType(int defaultGroupType) {
        this.defaultGroupType = defaultGroupType;
    }

    public void setMessageTemplate(MobicomMessageTemplate messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public MobicomMessageTemplate getMessageTemplate() {
        return messageTemplate;
    }

    public boolean isShowAllDeviceContacts() {
        return showAllDeviceContacts;
    }

    public void setShowAllDeviceContacts(boolean showAllDeviceContacts) {
        this.showAllDeviceContacts = showAllDeviceContacts;
    }

    public boolean isPoweredByApplozic() {
        return poweredByApplozic;
    }

    public boolean isImageCompressionEnabled() {
        return enableImageCompression;
    }

    public boolean isGlobalStoagePermissionDisabled() {
        return disableGlobalStoragePermission;
    }

    public boolean isMessageFastScrollEnabled() {
        return enableMessageFastScroll;
    }

    public boolean isContactSearchFromServer() {
        return isContactSearchFromServer;
    }

    public String getMessageSearchTextColor() {
        return messageSearchTextColor;
    }

    public int getNotificationDisableThreshold() {
        return notificationDisableThreshold;
    }

    public boolean isHideGroupDeleteButton() {
        return hideGroupDeleteButton;
    }

    public boolean isShowImageOnToolbar() {
        return showImageOnToolbar;
    }

    public Map<String, Boolean> getFilterGallery() {
        return filterGallery;
    }

    public void setFilterGallery(Map<String, Boolean> filterGallery) {
        this.filterGallery = filterGallery;
    }

    public String getRestrictedWordRegex() {
        return restrictedWordRegex;
    }

    public boolean isGroupSubtitleHidden() {
        return hideGroupSubtitle;
    }

    public boolean isMessageReportEnabled() {
        return enableMessageReport;
    }

    public void enableMessageReport(boolean enableMessageReport) {
        this.enableMessageReport = enableMessageReport;
    }

    public boolean isDeleteMessageOption() {
        return deleteMessageOption;
    }

    public void setDeleteMessageOption(boolean deleteMessageOption) {
        this.deleteMessageOption = deleteMessageOption;
    }

    @Override
    public String toString() {
        return "AlCustomizationSettings{" +
                "customMessageBackgroundColor='" + customMessageBackgroundColor + '\'' +
                ", sentMessageBackgroundColor='" + sentMessageBackgroundColor + '\'' +
                ", receivedMessageBackgroundColor='" + receivedMessageBackgroundColor + '\'' +
                ", sendButtonBackgroundColor='" + sendButtonBackgroundColor + '\'' +
                ", attachmentIconsBackgroundColor='" + attachmentIconsBackgroundColor + '\'' +
                ", chatBackgroundColorOrDrawable='" + chatBackgroundColorOrDrawable + '\'' +
                ", editTextBackgroundColorOrDrawable='" + editTextBackgroundColorOrDrawable + '\'' +
                ", editTextLayoutBackgroundColorOrDrawable='" + editTextLayoutBackgroundColorOrDrawable + '\'' +
                ", channelCustomMessageBgColor='" + channelCustomMessageBgColor + '\'' +
                ", sentContactMessageTextColor='" + sentContactMessageTextColor + '\'' +
                ", receivedContactMessageTextColor='" + receivedContactMessageTextColor + '\'' +
                ", sentMessageTextColor='" + sentMessageTextColor + '\'' +
                ", receivedMessageTextColor='" + receivedMessageTextColor + '\'' +
                ", messageEditTextTextColor='" + messageEditTextTextColor + '\'' +
                ", sentMessageLinkTextColor='" + sentMessageLinkTextColor + '\'' +
                ", receivedMessageLinkTextColor='" + receivedMessageLinkTextColor + '\'' +
                ", messageEditTextHintTextColor='" + messageEditTextHintTextColor + '\'' +
                ", typingTextColor='" + typingTextColor + '\'' +
                ", noConversationLabelTextColor='" + noConversationLabelTextColor + '\'' +
                ", conversationDateTextColor='" + conversationDateTextColor + '\'' +
                ", conversationDayTextColor='" + conversationDayTextColor + '\'' +
                ", messageTimeTextColor='" + messageTimeTextColor + '\'' +
                ", channelCustomMessageTextColor='" + channelCustomMessageTextColor + '\'' +
                ", sentMessageBorderColor='" + sentMessageBorderColor + '\'' +
                ", receivedMessageBorderColor='" + receivedMessageBorderColor + '\'' +
                ", channelCustomMessageBorderColor='" + channelCustomMessageBorderColor + '\'' +
                ", audioPermissionNotFoundMsg='" + audioPermissionNotFoundMsg + '\'' +
                ", noConversationLabel='" + noConversationLabel + '\'' +
                ", noSearchFoundForChatMessages='" + noSearchFoundForChatMessages + '\'' +
                ", locationShareViaMap=" + locationShareViaMap +
                ", startNewFloatingButton=" + startNewFloatingButton +
                ", startNewButton=" + startNewButton +
                ", onlineStatusMasterList=" + onlineStatusMasterList +
                ", priceWidget=" + priceWidget +
                ", startNewGroup=" + startNewGroup +
                ", imageCompression=" + imageCompression +
                ", inviteFriendsInContactActivity=" + inviteFriendsInContactActivity +
                ", registeredUserContactListCall=" + registeredUserContactListCall +
                ", createAnyContact=" + createAnyContact +
                ", showActionDialWithOutCalling=" + showActionDialWithOutCalling +
                ", profileLogoutButton=" + profileLogoutButton +
                ", userProfileFragment=" + userProfileFragment +
                ", messageSearchOption=" + messageSearchOption +
                ", conversationContactImageVisibility=" + conversationContactImageVisibility +
                ", hideGroupAddMembersButton=" + hideGroupAddMembersButton +
                ", hideGroupNameUpdateButton=" + hideGroupNameUpdateButton +
                ", hideGroupExitButton=" + hideGroupExitButton +
                ", hideGroupRemoveMemberOption=" + hideGroupRemoveMemberOption +
                ", profileOption=" + profileOption +
                ", totalRegisteredUserToFetch=" + totalRegisteredUserToFetch +
                ", maxAttachmentAllowed=" + maxAttachmentAllowed +
                ", maxAttachmentSizeAllowed=" + maxAttachmentSizeAllowed +
                ", totalOnlineUsers=" + totalOnlineUsers +
                '}';
    }
}
