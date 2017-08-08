package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.notification.VideoCallNotificationHelper;
import com.applozic.mobicommons.json.JsonMarker;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Message extends JsonMarker {

    private Long createdAtTime = new Date().getTime();
    private String to;
    private String message;
    private String key;
    private String deviceKey;
    private String userKey;
    private String emailIds;
    private boolean shared;
    private boolean sent;
    private Boolean delivered;
    private Short type = MessageType.MT_OUTBOX.getValue();
    private boolean storeOnDevice;
    private String contactIds = "";
    private Integer groupId;
    private boolean sendToDevice;
    private Long scheduledAt;
    private Short source = Source.MT_MOBILE_APP.getValue();
    private Integer timeToLive;
    private boolean sentToServer = true;
    private String fileMetaKey;
    private List<String> filePaths;
    private String pairedMessageKey;
    private long sentMessageTimeAtServer;
    private boolean canceled = false;
    private String clientGroupId;
    @SerializedName("fileMeta")
    private FileMeta fileMeta;
    @SerializedName("id")
    private Long messageId;
    private Boolean read = false;
    private boolean attDownloadInProgress;
    private String applicationId;
    private Integer conversationId;
    private String topicId;
    private boolean connected = false;
    private short contentType = ContentType.DEFAULT.getValue();
    private Map<String, String> metadata = new HashMap<>();
    private short status = Status.READ.getValue();
    private boolean hidden;
    private int replyMessage;

    public Message() {

    }

    public Message(String to, String body) {
        this.to = to;
        this.message = body;
    }

    //copy constructor
    public Message(Message message) {
        //this.setKeyString(message.getKeyString());
        this.setMessage(message.getMessage());
        this.setContactIds(message.getContactIds());
        this.setCreatedAtTime(message.getCreatedAtTime());
        this.setDeviceKeyString(message.getDeviceKeyString());
        this.setSendToDevice(message.isSendToDevice());
        this.setTo(message.getTo());
        this.setType(message.getType());
        this.setSent(message.isSent());
        this.setDelivered(message.getDelivered());
        this.setStoreOnDevice(message.isStoreOnDevice());
        this.setScheduledAt(message.getScheduledAt());
        this.setSentToServer(message.isSentToServer());
        this.setSource(message.getSource());
        this.setTimeToLive(message.getTimeToLive());
        this.setFileMetas(message.getFileMetas());
        this.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
        this.setFilePaths(message.getFilePaths());
        this.setGroupId(message.getGroupId());
        this.setRead(message.isRead());
        this.setApplicationId(message.getApplicationId());
        this.setContentType(message.getContentType());
        this.setStatus(message.getStatus());
        this.setConversationId(message.getConversationId());
        this.setTopicId(message.getTopicId());
        this.setMetadata(message.getMetadata());
    }

    public long getSentMessageTimeAtServer() {
        return sentMessageTimeAtServer;
    }

    public void setSentMessageTimeAtServer(long sentMessageTimeAtServer) {
        this.sentMessageTimeAtServer = sentMessageTimeAtServer;
    }

    public boolean isAttDownloadInProgress() {
        return attDownloadInProgress;
    }

    public void setAttDownloadInProgress(boolean attDownloadInProgress) {
        this.attDownloadInProgress = attDownloadInProgress;
    }

    public Boolean isRead() {
        return read || isTypeOutbox() || getScheduledAt() != null;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public boolean isSelfDestruct() {
        return getTimeToLive() != null;
    }

    public boolean isUploadRequired() {
        return hasAttachment() && (fileMeta == null);
    }

    public boolean hasAttachment() {
        return ((filePaths != null && !filePaths.isEmpty()) || (fileMeta != null));
    }

    public boolean isAttachmentUploadInProgress() {
        return filePaths != null && !filePaths.isEmpty() && FileUtils.isFileExist(filePaths.get(0))&& !sentToServer;
    }

    public boolean isAttachmentDownloaded() {
        return filePaths != null && !filePaths.isEmpty() && FileUtils.isFileExist(filePaths.get(0));
    }

    public boolean isCall() {
        return MessageType.CALL_INCOMING.getValue().equals(type) || MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isOutgoingCall() {
        return MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isIncomingCall() {
        return MessageType.CALL_INCOMING.getValue().equals(type);
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public boolean isDummyEmptyMessage() {
        return getCreatedAtTime() != null && getCreatedAtTime() == 0 && TextUtils.isEmpty(getMessage());
    }

    public boolean isLocalMessage() {
        return TextUtils.isEmpty(getKeyString()) && isSentToServer();
    }

    public String getKeyString() {
        return key;
    }

    public void setKeyString(String keyString) {
        this.key = keyString;
    }

    public Long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(Long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Boolean getDelivered() {
        return delivered != null ? delivered : false;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isStoreOnDevice() {
        return storeOnDevice;
    }

    public void setStoreOnDevice(boolean storeOnDevice) {
        this.storeOnDevice = storeOnDevice;
    }

    public String getDeviceKeyString() {
        return deviceKey;
    }

    public void setDeviceKeyString(String deviceKeyString) {
        this.deviceKey = deviceKeyString;
    }

    public String getSuUserKeyString() {
        return userKey;
    }

    public void setSuUserKeyString(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    public Short getType() {
        return type;
    }

    public void setType(Short type) {
        this.type = type;
    }

    public void processContactIds(Context context) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (TextUtils.isEmpty(getContactIds())) {
            setContactIds(getTo());

        }
    }

    public String getContactIds() {
        return getTo();
    }

    public void setContactIds(String contactIds) {
        this.contactIds = contactIds;
    }

    public boolean isSendToDevice() {
        return sendToDevice;
    }

    public void setSendToDevice(boolean sendToDevice) {
        this.sendToDevice = sendToDevice;
    }

    public Long getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Long scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public boolean isSentToMany() {
        return !TextUtils.isEmpty(getTo()) && getTo().split(",").length > 1;
    }

    public boolean isSentToServer() {
        return sentToServer;
    }

    public void setSentToServer(boolean sentToServer) {
        this.sentToServer = sentToServer;
    }

    public boolean isTypeOutbox() {
        return MessageType.OUTBOX.getValue().equals(type) || MessageType.MT_OUTBOX.getValue().equals(type) ||
                MessageType.OUTBOX_SENT_FROM_DEVICE.getValue().equals(type) || MessageType.CALL_OUTGOING.getValue().equals(type);
    }

    public boolean isSentViaApp() {
        return MessageType.MT_OUTBOX.getValue().equals(this.type);
    }

    public boolean isSentViaCarrier() {
        return MessageType.OUTBOX.getValue().equals(type);
    }

    public Short getSource() {
        return source;
    }

    public void setSource(Short source) {
        this.source = source;
    }

    public Integer getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Integer timeToLive) {
        this.timeToLive = timeToLive;
    }

    public String getFileMetaKeyStrings() {
        return fileMetaKey;
    }

    public void setFileMetaKeyStrings(String fileMetaKeyStrings) {
        this.fileMetaKey = fileMetaKeyStrings;
    }

    public List<String> getFilePaths() {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public String getPairedMessageKeyString() {
        return pairedMessageKey;
    }

    public void setPairedMessageKeyString(String pairedMessageKeyString) {
        this.pairedMessageKey = pairedMessageKeyString;
    }

    public FileMeta getFileMetas() {
        return fileMeta;
    }

    public void setFileMetas(FileMeta fileMetas) {
        this.fileMeta = fileMetas;
    }

    public String getEmailIds() {
        return emailIds;
    }

    public void setEmailIds(String emailIds) {
        this.emailIds = emailIds;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public short getContentType() {
        return contentType;
    }

    public void setContentType(short contentType) {
        this.contentType = contentType;
    }

    public Integer getConversationId() {
        return conversationId;
    }

    public void setConversationId(Integer conversationId) {
        this.conversationId = conversationId;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public String getCurrentId() {
        return getGroupId() != null ? String.valueOf(getGroupId()) : getContactIds();
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;

        if (message.isTempDateType() && isTempDateType()) {
            return DateUtils.getDate(message.getCreatedAtTime()).equals(DateUtils.getDate(getCreatedAtTime()));
        }

        if (getMessageId() != null && message.getMessageId() != null && getMessageId().equals(message.getMessageId())) {
            return true;
        }

        if (getKeyString() != null && message.getKeyString() != null) {
            return getKeyString().equals(message.getKeyString());
        }

       /* if ((isSentToMany() && !getContactIds().contains(message.getContactIds())) || (message.isSentToMany() && !message.getContactIds().contains(getContactIds()))) {
            return false;
        } else if (!isSentToMany() && !message.isSentToMany() && !PhoneNumberUtils.compare(to, message.to)) {
            return false;
        }

        if (!getMessage().equals(message.getMessage()) && !getMessage().contains(message.getMessage()) && !message.getMessage().contains(getMessage())) {
            return false;
        }

        if (getType() != null && message.getType() != null && ((isTypeOutbox() && !message.isTypeOutbox()) || (!isTypeOutbox() && message.isTypeOutbox()))) {
            return false;
        }

        if (getTimeToLive() != null && !getTimeToLive().equals(message.getTimeToLive())) {
            return false;
        } else if (getTimeToLive() != null && !getCreatedAtTime().equals(message.getCreatedAtTime())) {
            return false;
        }

        if ((getFilePaths() != null && !getFilePaths().equals(message.getFilePaths()))
                || (message.getFilePaths() != null && !message.getFilePaths().equals(getFilePaths()))) {
            return false;
        } else if (getFileMetaKeyStrings() != null && message.getFileMetaKeyStrings() != null && !getFileMetaKeyStrings().equals(message.getFileMetaKeyStrings())) {
            return false;
        }
*/
        /*long createdTimeDifference = 0;
        if (!getDelivered().equals(getDelivered())) {
            createdTimeDifference = 240 * 1000;
        }
*//*
        if (createdAtTime != null && message.getCreatedAtTime() != null &&
                Math.abs(createdAtTime - message.getCreatedAtTime()) > createdTimeDifference) {
            return false;
        }
*/
        return false;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (messageId != null ? messageId.hashCode() : 0);
        if (isTempDateType()) {
            result = 31 * result + DateUtils.getDate(getCreatedAtTime()).hashCode();
        }
        return result;
    }

  /*@Override
    public int hashCode() {
       *//* int result = getContactIds() != null ? getContactIds().hashCode() : 0;
        result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
        result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
        result = 31 * result + (getTimeToLive() != null ? getTimeToLive().hashCode() : 0);*//*
        return result;
    }*/

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public short getStatus() {
        return status;
    }

    public void setStatus(short status) {
        this.status = status;
    }

    public boolean isTempDateType() {
        return type.equals(MessageType.DATE_TEMP.value);
    }

    public void setTempDateType(short tempDateType) {
        this.type = tempDateType;
    }

    public boolean isCustom() {
        return contentType == ContentType.CUSTOM.value;
    }

    public boolean isChannelCustomMessage() {
        return contentType == ContentType.CHANNEL_CUSTOM_MESSAGE.getValue();
    }

    public boolean isDeliveredAndRead() {
        return Message.Status.DELIVERED_AND_READ.getValue().shortValue() == getStatus();
    }

    public boolean isReadStatus() {
        return Status.READ.getValue() == getStatus();
    }

    public boolean isReadStatusForUpdate() {
        return Status.READ.getValue() == getStatus() || isTypeOutbox();
    }

    public boolean isContactMessage() {
        return ContentType.CONTACT_MSG.getValue().equals(getContentType());
    }

    public boolean isLocationMessage() {
        return ContentType.LOCATION.getValue().equals(getContentType());
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public boolean isGroupMessage() {
        return (this.groupId != null);
    }

    public String getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public String getMetaDataValueForKey(String key) {
        return getMetadata() != null ? getMetadata().get(key) : null;
    }

    public boolean isUpdateMessage() {
        return !Message.ContentType.HIDDEN.getValue().equals(contentType)
                && !Message.MetaDataType.ARCHIVE.getValue().equals(getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))
                && !isVideoNotificationMessage();

    }

    public boolean isVideoNotificationMessage() {
        return ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue().equals(getContentType());
    }

    public boolean isVideoCallMessage() {
        return ContentType.VIDEO_CALL_STATUS_MSG.getValue().equals(getContentType());
    }

    public boolean isVideoOrAudioCallMessage() {
        String msgType = getMetaDataValueForKey(VideoCallNotificationHelper.MSG_TYPE);
        return (VideoCallNotificationHelper.CALL_STARTED.equals(msgType)
                || VideoCallNotificationHelper.CALL_REJECTED.equals(msgType)
                || VideoCallNotificationHelper.CALL_CANCELED.equals(msgType)
                || VideoCallNotificationHelper.CALL_ANSWERED.equals(msgType)
                || VideoCallNotificationHelper.CALL_END.equals(msgType)
                || VideoCallNotificationHelper.CALL_DIALED.equals(msgType)
                || VideoCallNotificationHelper.CALL_ANSWERED.equals(msgType)
                || VideoCallNotificationHelper.CALL_MISSED.equals(msgType));
    }

    public boolean isConsideredForCount() {
        return (!Message.ContentType.HIDDEN.getValue().equals(getContentType()) &&
                !ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue().equals(getContentType()) && !isReadStatus() && !isHidden());
    }

    public boolean isHidden() {
        return GroupMessageMetaData.TRUE.getValue().equals(getMetaDataValueForKey(GroupMessageMetaData.HIDE_KEY.getValue())) || Message.ContentType.HIDDEN.getValue().equals(getContentType());
    }

    public void setHidden(boolean hidden) {
        hidden = hidden;
    }

    public int isReplyMessage() {
        return replyMessage;
    }

    public void setReplyMessage(int replyMessage) {
        this.replyMessage = replyMessage;
    }

    @Override
    public String toString() {
        return "Message{" +
                "createdAtTime=" + createdAtTime +
                ", to='" + to + '\'' +
                ", message='" + message + '\'' +
                ", key='" + key + '\'' +
                ", deviceKey='" + deviceKey + '\'' +
                ", userKey='" + userKey + '\'' +
                ", emailIds='" + emailIds + '\'' +
                ", shared=" + shared +
                ", sent=" + sent +
                ", delivered=" + delivered +
                ", type=" + type +
                ", storeOnDevice=" + storeOnDevice +
                ", contactIds='" + contactIds + '\'' +
                ", groupId=" + groupId +
                ", sendToDevice=" + sendToDevice +
                ", scheduledAt=" + scheduledAt +
                ", source=" + source +
                ", timeToLive=" + timeToLive +
                ", sentToServer=" + sentToServer +
                ", fileMetaKey='" + fileMetaKey + '\'' +
                ", filePaths=" + filePaths +
                ", pairedMessageKey='" + pairedMessageKey + '\'' +
                ", sentMessageTimeAtServer=" + sentMessageTimeAtServer +
                ", canceled=" + canceled +
                ", clientGroupId='" + clientGroupId + '\'' +
                ", fileMeta=" + fileMeta +
                ", messageId=" + messageId +
                ", read=" + read +
                ", attDownloadInProgress=" + attDownloadInProgress +
                ", applicationId='" + applicationId + '\'' +
                ", conversationId=" + conversationId +
                ", topicId='" + topicId + '\'' +
                ", connected=" + connected +
                ", contentType=" + contentType +
                ", metadata=" + metadata +
                ", status=" + status +
                '}';
    }

    public enum Source {

        DEVICE_NATIVE_APP(Short.valueOf("0")), WEB(Short.valueOf("1")), MT_MOBILE_APP(Short.valueOf("2")), API(Short.valueOf("3"));
        private Short value;

        Source(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum MessageType {

        INBOX(Short.valueOf("0")), OUTBOX(Short.valueOf("1")), DRAFT(Short.valueOf("2")),
        OUTBOX_SENT_FROM_DEVICE(Short.valueOf("3")), MT_INBOX(Short.valueOf("4")),
        MT_OUTBOX(Short.valueOf("5")), CALL_INCOMING(Short.valueOf("6")), CALL_OUTGOING(Short.valueOf("7")),
        DATE_TEMP(Short.valueOf("100"));
        private Short value;

        MessageType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum ContentType {

        DEFAULT(Short.valueOf("0")), ATTACHMENT(Short.valueOf("1")), LOCATION(Short.valueOf("2")),
        TEXT_HTML(Short.valueOf("3")), PRICE(Short.valueOf("4")), TEXT_URL(Short.valueOf("5")), CONTACT_MSG(Short.valueOf("7")), AUDIO_MSG(Short.valueOf("8")), VIDEO_MSG(Short.valueOf("9")), CHANNEL_CUSTOM_MESSAGE(Short.valueOf("10")), CUSTOM(Short.valueOf("101")), HIDDEN(Short.valueOf("11")), VIDEO_CALL_NOTIFICATION_MSG(Short.valueOf("102")),
        VIDEO_CALL_STATUS_MSG(Short.valueOf("103"));
        private Short value;

        ContentType(Short value) {
            this.value = value;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum Status {

        UNREAD(Short.valueOf("0")), READ(Short.valueOf("1")), PENDING(Short.valueOf("2")),
        SENT(Short.valueOf("3")), DELIVERED(Short.valueOf("4")), DELIVERED_AND_READ(Short.valueOf("5"));
        private Short value;

        Status(Short value) {
            this.value = value;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum MetaDataType {
        KEY("category"),
        HIDDEN("HIDDEN"),
        PUSHNOTIFICATION("PUSHNOTIFICATION"),
        ARCHIVE("ARCHIVE"), AL_REPLY("AL_REPLY");
        private String value;

        MetaDataType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum GroupMessageMetaData {
        KEY("show"),
        HIDE_KEY("hide"),
        FALSE("false"),
        TRUE("true");
        private String value;

        GroupMessageMetaData(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ReplyMessage{
        NON_HIDDEN(0),
        REPLY_MESSAGE(1),
        HIDE_MESSAGE(2);
        private Integer value;

        ReplyMessage(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }

}