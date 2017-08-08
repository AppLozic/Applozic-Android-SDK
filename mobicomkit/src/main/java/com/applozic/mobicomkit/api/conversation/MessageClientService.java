package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.schedule.ScheduledMessageUtil;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.MessageResponse;
import com.applozic.mobicomkit.sync.SmsSyncRequest;
import com.applozic.mobicomkit.sync.SyncMessageFeed;
import com.applozic.mobicomkit.sync.SyncUserDetailsResponse;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

;

/**
 * Created by devashish on 26/12/14.
 */
public class MessageClientService extends MobiComKitClientService {

    public static final int SMS_SYNC_BATCH_SIZE = 5;
    public static final String DEVICE_KEY = "deviceKey";
    public static final String LAST_SYNC_KEY = "lastSyncTime";
    public static final String REGISTRATION_ID = "registrationId";
    public static final String FILE_META = "fileMeta";
    public static final String MTEXT_DELIVERY_URL = "/rest/ws/message/delivered";
    public static final String SERVER_SYNC_URL = "/rest/ws/message/sync";
    // public static final String SEND_MESSAGE_URL = "/rest/ws/mobicomkit/v1/message/add";
    public static final String SEND_MESSAGE_URL = "/rest/ws/message/send";
    public static final String SYNC_SMS_URL = "/rest/ws/sms/add/batch";
    public static final String MESSAGE_LIST_URL = "/rest/ws/message/list";
    public static final String MESSAGE_DELETE_URL = "/rest/ws/message/delete";
    public static final String UPDATE_DELIVERY_FLAG_URL = "/rest/ws/sms/update/delivered";
    // public static final String MESSAGE_THREAD_DELETE_URL = "/rest/ws/mobicomkit/v1/message/delete/conversation.task";
    public static final String UPDATE_READ_STATUS_URL = "/rest/ws/message/read/conversation";
    public static final String MESSAGE_THREAD_DELETE_URL = "/rest/ws/message/delete/conversation";
    public static final String USER_DETAILS_URL = "/rest/ws/user/detail";
    public static final String USER_DETAILS_LIST_URL = "/rest/ws/user/status";
    public static final String PRODUCT_CONVERSATION_ID_URL = "/rest/ws/conversation/id";
    public static final String PRODUCT_TOPIC_ID_URL = "/rest/ws/conversation/topicId";
    public static final String ARGUMRNT_SAPERATOR = "&";
    public static final String UPDATE_READ_STATUS_FOR_SINGLE_MESSAGE_URL = "/rest/ws/message/read";
    public static final String MESSAGE_INFO_URL = "/rest/ws/message/info";
    public static final String MESSAGE_BY_MESSAGE_KEYS_URL = "/rest/ws/message/detail";

    private static final String TAG = "MessageClientService";
    private Context context;
    private MessageDatabaseService messageDatabaseService;
    private HttpRequestUtils httpRequestUtils;
    private BaseContactService baseContactService;

    public MessageClientService(Context context) {
        super(context);
        this.context = context.getApplicationContext();
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
        this.baseContactService = new AppContactService(context);
    }

    public String getMtextDeliveryUrl() {
        return getBaseUrl() + MTEXT_DELIVERY_URL;
    }

    public String getServerSyncUrl() {
        return getBaseUrl() + SERVER_SYNC_URL;
    }

    public String getSendMessageUrl() {
        return getBaseUrl() + SEND_MESSAGE_URL;
    }

    public String getSyncSmsUrl() {
        return getBaseUrl() + SYNC_SMS_URL;
    }

    public String getMessageListUrl() {
        return getBaseUrl() + MESSAGE_LIST_URL;
    }

    public String getMessageDeleteUrl() {
        return getBaseUrl() + MESSAGE_DELETE_URL;
    }

    public String getUpdateDeliveryFlagUrl() {
        return getBaseUrl() + UPDATE_DELIVERY_FLAG_URL;
    }

    public String getMessageThreadDeleteUrl() {
        return getBaseUrl() + MESSAGE_THREAD_DELETE_URL;
    }

    public String getUpdateReadStatusUrl() {
        return getBaseUrl() + UPDATE_READ_STATUS_URL;
    }

    public String getUserDetailUrl() {
        return getBaseUrl() + USER_DETAILS_URL;
    }

    public String getUserDetailsListUrl() {
        return getBaseUrl() + USER_DETAILS_LIST_URL;
    }

    public String getProductConversationUrl() {
        return getBaseUrl() + PRODUCT_CONVERSATION_ID_URL;
    }

    public String getProductTopicIdUrl() {
        return getBaseUrl() + PRODUCT_TOPIC_ID_URL;
    }

    public String getMessageInfoUrl() {
        return getBaseUrl() + MESSAGE_INFO_URL;
    }

    public String getSingleMessageReadUrl() {
        return getBaseUrl() + UPDATE_READ_STATUS_FOR_SINGLE_MESSAGE_URL;
    }

    public String getMessageByMessageKeysUrl(){
        return getBaseUrl() + MESSAGE_BY_MESSAGE_KEYS_URL;
    }

    public void updateDeliveryStatus(String messageKeyString, String userId, String receiverNumber) {
        try {
            //Note: messageKeyString comes as null for the welcome message as it is inserted directly.
            if (TextUtils.isEmpty(messageKeyString) || TextUtils.isEmpty(userId)) {
                return;
            }
            httpRequestUtils.getResponse(getMtextDeliveryUrl() + "?key=" + messageKeyString
                    + "&userId=" + userId, "text/plain", "text/plain");
        } catch (Exception ex) {
            Utils.printLog(context,TAG, "Exception while updating delivery report for MT message");
        }
    }

    public synchronized void syncPendingMessages(boolean broadcast) {
        List<Message> pendingMessages = messageDatabaseService.getPendingMessages();
        Utils.printLog(context,TAG, "Found " + pendingMessages.size() + " pending messages to sync.");
        for (Message message : pendingMessages) {
            Utils.printLog(context,TAG, "Syncing pending message: " + message);
            sendPendingMessageToServer(message, broadcast);
        }
    }

    public synchronized void syncDeleteMessages(boolean deleteMessage) {
        List<Message> pendingDeleteMessages = messageDatabaseService.getPendingDeleteMessages();
        Utils.printLog(context,TAG, "Found " + pendingDeleteMessages.size() + " pending messages for Delete.");
        for (Message message : pendingDeleteMessages) {
            deletePendingMessages(message, deleteMessage);
        }
    }

    public void deletePendingMessages(Message message, boolean deleteMessage) {

        String contactNumberParameter = "";
        String response = "";
        if (message != null && !TextUtils.isEmpty(message.getContactIds())) {
            try {
                contactNumberParameter = "&userId=" + message.getContactIds();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (message.isSentToServer()) {
            response = httpRequestUtils.getResponse(getMessageDeleteUrl() + "?key=" + message.getKeyString() + contactNumberParameter, "text/plain", "text/plain");
        }
        Utils.printLog(context,TAG, "Delete response from server for pending message: " + response);
        if ("success".equals(response)) {
            messageDatabaseService.deleteMessage(message, message.getContactIds());
        }

    }

    public boolean syncMessagesWithServer(List<Message> messageList) {
        Utils.printLog(context,TAG, "Total messages to sync: " + messageList.size());
        List<Message> messages = new ArrayList<Message>(messageList);
        do {
            try {
                SmsSyncRequest smsSyncRequest = new SmsSyncRequest();
                if (messages.size() > SMS_SYNC_BATCH_SIZE) {
                    List<Message> subList = new ArrayList(messages.subList(0, SMS_SYNC_BATCH_SIZE));
                    smsSyncRequest.setSmsList(subList);
                    messages.removeAll(subList);
                } else {
                    smsSyncRequest.setSmsList(new ArrayList<Message>(messages));
                    messages.clear();
                }

                String response = syncMessages(smsSyncRequest);
                Utils.printLog(context,TAG, "response from sync sms url::" + response);
                String[] keyStrings = null;
                if (!TextUtils.isEmpty(response) && !response.equals("error")) {
                    keyStrings = response.trim().split(",");
                }
                if (keyStrings != null) {
                    int i = 0;
                    for (Message message : smsSyncRequest.getSmsList()) {
                        if (!TextUtils.isEmpty(keyStrings[i])) {
                            message.setKeyString(keyStrings[i]);
                            messageDatabaseService.createMessage(message);
                        }
                        i++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Utils.printLog(context,TAG, "exception" + e);
                return false;
            }
        } while (messages.size() > 0);
        return true;
    }

    public void sendPendingMessageToServer(Message message, boolean broadcast) {

        try {
            if (message.isContactMessage()) {
                try {
                    this.processMessage(message);
                } catch (Exception e) {
                    Utils.printLog(context,TAG, "Exception while sending contact message.");
                }
                return;
            }

            if (message.hasAttachment()) {
                return;
            }

            MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
            message.setDeviceKeyString(mobiComUserPreference.getDeviceKeyString());
            message.setSuUserKeyString(mobiComUserPreference.getSuUserKeyString());

            String response = sendMessage(message);

            if (TextUtils.isEmpty(response) || response.contains("<html>") || response.equals("error")) {
                Utils.printLog(context,TAG, "Error while sending pending messages.");
                return;
            }

            MessageResponse messageResponse = (MessageResponse) GsonUtils.getObjectFromJson(response, MessageResponse.class);
            String keyString = messageResponse.getMessageKey();
            String createdAt = messageResponse.getCreatedAtTime();
            message.setSentMessageTimeAtServer(Long.parseLong(createdAt));
            message.setKeyString(keyString);

        /*recentMessageSentToServer.add(message);*/

            if (broadcast) {
                BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString(), message);
            }

            messageDatabaseService.updateMessageSyncStatus(message, keyString);
        } catch (Exception e) {
            Utils.printLog(context,TAG, "Error while sending pending messages.");
        }

    }

    public void sendMessageToServer(Message message) throws Exception {
        sendMessageToServer(message, null);
    }

    public void sendMessageToServer(Message message, Class intentClass) throws Exception {
        processMessage(message);
        if (message.getScheduledAt() != null && message.getScheduledAt() != 0 && intentClass != null) {
            new ScheduledMessageUtil(context, intentClass).createScheduleMessage(message, context);
        }
    }

    public void processMessage(Message message) throws Exception {

        boolean isBroadcast = (message.getMessageId() == null);

        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        message.setSent(Boolean.TRUE);
        message.setSendToDevice(Boolean.FALSE);
        message.setSuUserKeyString(userPreferences.getSuUserKeyString());
        message.processContactIds(context);
        Contact contact = null;
        Channel channel = null;
        boolean isBroadcastOneByOneGroupType = false;
        boolean skipMessage = false;
        if (message.getGroupId() == null) {
            contact = baseContactService.getContactById(message.getContactIds());
        } else {
            channel = ChannelService.getInstance(context).getChannel(message.getGroupId());
            isBroadcastOneByOneGroupType = Channel.GroupType.BROADCAST_ONE_BY_ONE.getValue().equals(channel.getType());
        }
        long messageId = -1;

        List<String> fileKeys = new ArrayList<String>();
        String keyString = null;
        if (!isBroadcastOneByOneGroupType) {
            keyString = UUID.randomUUID().toString();
            message.setKeyString(keyString);
            message.setSentToServer(false);
        } else {
            message.setSentToServer(true);
        }

        if (Message.MetaDataType.HIDDEN.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
            skipMessage = true;
        }

        if (!skipMessage) {
            messageId = messageDatabaseService.createMessage(message);
        }

        if (isBroadcast && !skipMessage) {
            BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);
        }
        if (!isBroadcastOneByOneGroupType && message.isUploadRequired()) {
            for (String filePath : message.getFilePaths()) {
                try {
                    String fileMetaResponse = new FileClientService(context).uploadBlobImage(filePath);
                    if (fileMetaResponse == null) {
                        if (skipMessage) {
                            return;
                        }
                        if (!message.isContactMessage()) {
                            messageDatabaseService.updateCanceledFlag(messageId, 1);
                        }
                        BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString(), message);
                        return;
                    }
                    JsonParser jsonParser = new JsonParser();
                    JsonObject jsonObject = jsonParser.parse(fileMetaResponse).getAsJsonObject();
                    if (jsonObject.has(FILE_META)) {
                        Gson gson = new Gson();
                        message.setFileMetas(gson.fromJson(jsonObject.get(FILE_META), FileMeta.class));
                    }
                } catch (Exception ex) {
                    Utils.printLog(context,TAG, "Error uploading file to server: " + filePath);
                  /*  recentMessageSentToServer.remove(message);*/
                    if (!message.isContactMessage() && !skipMessage) {
                        messageDatabaseService.updateCanceledFlag(messageId, 1);
                    }
                    if (!skipMessage) {
                        BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString(), message);
                    }
                    return;
                }
            }
            if (messageId != -1 && !skipMessage) {
                messageDatabaseService.updateMessageFileMetas(messageId, message);
            }
        }

        Message newMessage = new Message();
        newMessage.setTo(message.getTo());
        newMessage.setKeyString(message.getKeyString());
        newMessage.setMessage(message.getMessage());
        newMessage.setFileMetas(message.getFileMetas());
        newMessage.setCreatedAtTime(message.getCreatedAtTime());
        newMessage.setRead(Boolean.TRUE);
        newMessage.setDeviceKeyString(message.getDeviceKeyString());
        newMessage.setSuUserKeyString(message.getSuUserKeyString());
        newMessage.setSent(message.isSent());
        newMessage.setType(message.getType());
        newMessage.setTimeToLive(message.getTimeToLive());
        newMessage.setSource(message.getSource());
        newMessage.setScheduledAt(message.getScheduledAt());
        newMessage.setStoreOnDevice(message.isStoreOnDevice());
        newMessage.setDelivered(message.getDelivered());
        newMessage.setStatus(message.getStatus());
        newMessage.setMetadata(message.getMetadata());

        newMessage.setSendToDevice(message.isSendToDevice());
        newMessage.setContentType(message.getContentType());
        newMessage.setConversationId(message.getConversationId());
        if (message.getGroupId() != null) {
            newMessage.setGroupId(message.getGroupId());
        }
        if (!TextUtils.isEmpty(message.getClientGroupId())) {
            newMessage.setClientGroupId(message.getClientGroupId());
        }

        if (contact != null && !TextUtils.isEmpty(contact.getApplicationId())) {
            newMessage.setApplicationId(contact.getApplicationId());
        } else {
            newMessage.setApplicationId(getApplicationKey(context));
        }

        //Todo: set filePaths

        try {
            if (!isBroadcastOneByOneGroupType) {
                String response = sendMessage(newMessage);
                if (message.hasAttachment() && TextUtils.isEmpty(response) && !message.isContactMessage() && !skipMessage) {
                    messageDatabaseService.updateCanceledFlag(messageId, 1);
                    BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString(), message);
                }
                MessageResponse messageResponse = (MessageResponse) GsonUtils.getObjectFromJson(response, MessageResponse.class);
                keyString = messageResponse.getMessageKey();
                if (!TextUtils.isEmpty(keyString)) {
                    message.setSentMessageTimeAtServer(Long.parseLong(messageResponse.getCreatedAtTime()));
                    message.setConversationId(messageResponse.getConversationId());
                    message.setSentToServer(true);
                    message.setKeyString(keyString);
                }
                if (!skipMessage) {
                    messageDatabaseService.updateMessage(messageId, message.getSentMessageTimeAtServer(), keyString, message.isSentToServer());
                }
            } else {
                message.setSentMessageTimeAtServer(message.getCreatedAtTime());
                messageDatabaseService.updateMessage(messageId, message.getSentMessageTimeAtServer(), keyString, message.isSentToServer());
            }

            if (!TextUtils.isEmpty(keyString)) {
                //Todo: Handle server message add failure due to internet disconnect.
            } else {
                //Todo: If message type is mtext, tell user that internet is not working, else send update with db id.
            }
            if (!skipMessage) {
                BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString(), message);
            }

        } catch (Exception e) {
        }

      /*  if (recentMessageSentToServer.size() > 20) {
            recentMessageSentToServer.subList(0, 10).clear();
        }*/
    }

    public String syncMessages(SmsSyncRequest smsSyncRequest) throws Exception {
        String data = GsonUtils.getJsonFromObject(smsSyncRequest, SmsSyncRequest.class);
        return httpRequestUtils.postData(getSyncSmsUrl(), "application/json", null, data);
    }

    public String sendMessage(Message message) {
        try {
            String jsonFromObject = GsonUtils.getJsonFromObject(message, message.getClass());
            Utils.printLog(context,TAG, "Sending message to server: " + jsonFromObject);
            return httpRequestUtils.postData(getSendMessageUrl(), "application/json;charset=utf-8", null, jsonFromObject);
        } catch (Exception e) {
            return null;
        }
    }

    public SyncMessageFeed getMessageFeed(String lastSyncTime) {
        String url = getServerSyncUrl() + "?" +
                LAST_SYNC_KEY
                + "=" + lastSyncTime;
        try {
            String response = httpRequestUtils.getResponse(url, "application/json", "application/json");
            Utils.printLog(context,TAG, "Sync call response: " + response);
            return (SyncMessageFeed) GsonUtils.getObjectFromJson(response, SyncMessageFeed.class);
        } catch (Exception e) {
            // showAlert("Unable to Process request .Please Contact Support");
            return null;
        }
    }

    public void deleteConversationThreadFromServer(Contact contact) {
        if (TextUtils.isEmpty(contact.getContactIds())) {
            return;
        }
        try {
            String url = getMessageThreadDeleteUrl() + "?userId=" + contact.getContactIds();
            String response = httpRequestUtils.getResponse(url, "text/plain", "text/plain");
            Utils.printLog(context,TAG, "Delete messages response from server: " + response + contact.getContactIds());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String syncDeleteConversationThreadFromServer(Contact contact, Channel channel) {
        String response = null;
        String parameterString = "";
        try {
            if (contact != null && !TextUtils.isEmpty(contact.getContactIds())) {
                parameterString = "?userId=" + contact.getContactIds();
            } else if (channel != null) {
                parameterString = "?groupId=" + channel.getKey();
            }
            String url = getMessageThreadDeleteUrl() + parameterString;
            response = httpRequestUtils.getResponse(url, "text/plain", "text/plain");
            Utils.printLog(context,TAG, "Delete messages response from server: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String deleteMessage(Message message, Contact contact) {
        String contactNumberParameter = "";
        String response = "";
        if (contact != null && !TextUtils.isEmpty(contact.getContactIds())) {
            try {
                contactNumberParameter = "&userId=" + contact.getContactIds();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (message.isSentToServer()) {
            response = httpRequestUtils.getResponse(getMessageDeleteUrl() + "?key=" + message.getKeyString() + contactNumberParameter, "text/plain", "text/plain");
            Utils.printLog(context,TAG, "delete response is " + response);
        }
        return response;
    }


    public String getMessageByMessageKeys(List<String> messageKeys){
        if(messageKeys != null && messageKeys.size()>0){
            String messageKeyUrlBuild="";
            for (String messageKey : messageKeys) {
                messageKeyUrlBuild += "keys" + "=" + messageKey+"&";
            }
            String  response = httpRequestUtils.getResponse(getMessageByMessageKeysUrl() +"?"+ messageKeyUrlBuild, "application/json", "application/json");
            Utils.printLog(context,TAG, "Message keys response is :" + response);
            if (TextUtils.isEmpty(response) || response.contains("<html>")) {
                return null;
            }
            return response;
        }
        return null;
    }


    public void updateReadStatus(Contact contact, Channel channel) {
        String contactNumberParameter = "";
        String response = "";
        if (contact != null && !TextUtils.isEmpty(contact.getContactIds())) {
            contactNumberParameter = "?userId=" + contact.getContactIds();
        } else if (channel != null) {
            contactNumberParameter = "?groupId=" + channel.getKey();
        }
        response = httpRequestUtils.getResponse(getUpdateReadStatusUrl() + contactNumberParameter, "text/plain", "text/plain");
        Utils.printLog(context,TAG, "Read status response is " + response);
    }

    public void updateReadStatusForSingleMessage(String pairedmessagekey) {
        String singleReadMessageParm = "";
        String response = "";
        if (!TextUtils.isEmpty(pairedmessagekey)) {
            try {
                singleReadMessageParm = "?key=" + pairedmessagekey;
                response = httpRequestUtils.getResponse(getSingleMessageReadUrl() + singleReadMessageParm, "text/plain", "text/plain");
                Utils.printLog(context,TAG, "Read status response for single message is " + response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public String getMessages(Contact contact, Channel channel, Long startTime, Long endTime, Integer conversationId) throws UnsupportedEncodingException {
        return getMessages(contact, channel, startTime, endTime, conversationId, false);
    }

    public String getMessages(Contact contact, Channel channel, Long startTime, Long endTime, Integer conversationId, boolean isSkipRead) throws UnsupportedEncodingException {
        String contactNumber = (contact != null ? contact.getFormattedContactNumber() : "");
        String params = "";
        if (contact != null || channel != null) {
            params = isSkipRead ? "skipRead=" + isSkipRead + "&startIndex=0&pageSize=50" + "&" : "startIndex=0&pageSize=50&";
        }
        if (contact == null && channel == null) {
            params = "startIndex=0&mainPageSize=60" + "&";
        }
        if (contact != null && !TextUtils.isEmpty(contact.getUserId())) {
            params += "userId=" + contact.getUserId() + "&";
        }
        params += (startTime != null && startTime.intValue() != 0) ? "startTime=" + startTime + "&" : "";
        params += (endTime != null && endTime.intValue() != 0) ? "endTime=" + endTime + "&" : "";
        params += (channel != null && channel.getKey() != null) ? "groupId=" + channel.getKey() + "&" : "";

        if (BroadcastService.isContextBasedChatEnabled()) {
            if (conversationId != null && conversationId != 0) {
                params += "conversationId=" + conversationId + "&";
            }
            if (endTime != null && endTime.intValue() == 0 || endTime == null) {
                params += "conversationReq=true";
            }
        }
        params = params + "&" + "deletedGroupIncluded=true";

        return httpRequestUtils.getResponse(getMessageListUrl() + "?" + params
                , "application/json", "application/json");
    }

    public String deleteMessage(Message message) {
        return deleteMessage(message.getKeyString());
    }

    public String deleteMessage(String keyString) {
        return httpRequestUtils.getResponse(getMessageDeleteUrl() + "?key=" + keyString, "text/plain", "text/plain");
    }

    public SyncUserDetailsResponse getUserDetailsList(String lastSeenAt) {
        try {
            String url = getUserDetailsListUrl() + "?lastSeenAt=" + lastSeenAt;
            String response = httpRequestUtils.getResponse(url, "application/json", "application/json");

            if (response == null || TextUtils.isEmpty(response) || response.equals("UnAuthorized Access")) {
                return null;
            }
            Utils.printLog(context,TAG, "Sync UserDetails response is:" + response);
            SyncUserDetailsResponse userDetails = (SyncUserDetailsResponse) GsonUtils.getObjectFromJson(response, SyncUserDetailsResponse.class);
            return userDetails;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String[] getConnectedUsers() {
        try {
            String response = getMessages(null, null, null, null, null);
            if (response == null || TextUtils.isEmpty(response) || response.equals("UnAuthorized Access") || !response.contains("{")) {
                return null;
            }
            JsonParser parser = new JsonParser();
            String element = parser.parse(response).getAsJsonObject().get("connectedUsers").toString();
            return (String[]) GsonUtils.getObjectFromJson(element, String[].class);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void processUserStatus(Contact contact) {
        if (contact != null && contact.getContactIds() != null) {
            processUserStatus(contact.getUserId(),false);
        }
    }

    public void processUserStatus(String userId){
        processUserStatus(userId,false);
    }

    public void processUserStatus(String userId,boolean isProfileImageUpdated) {
        try {
            String contactNumberParameter = "";
            String response = "";
            try {
                contactNumberParameter = "?userIds=" + URLEncoder.encode(userId);
            } catch (Exception e) {
                contactNumberParameter = "?userIds=" + userId;
                e.printStackTrace();
            }

            response = httpRequestUtils.getResponse(getUserDetailUrl() + contactNumberParameter, "application/json", "application/json");
            Utils.printLog(context,TAG, "User details response is " + response);
            if (TextUtils.isEmpty(response) || response.contains("<html>")) {
                return;
            }

            UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(response, UserDetail[].class);

            if (userDetails != null) {
                for (UserDetail userDetail : userDetails) {
                    Contact contact = new Contact();
                    contact.setUserId(userDetail.getUserId());
                    contact.setFullName(userDetail.getDisplayName());
                    contact.setConnected(userDetail.isConnected());
                    contact.setContactNumber(userDetail.getPhoneNumber());
                    contact.setLastSeenAt(userDetail.getLastSeenAtTime());
                    contact.setImageURL(userDetail.getImageLink());
                    contact.setStatus(userDetail.getStatusMessage());
                    contact.setUserTypeId(userDetail.getUserTypeId());
                    contact.setDeletedAtTime(userDetail.getDeletedAtTime());
                    contact.setUnreadCount(0);
                    baseContactService.upsert(contact);
                }
                if(isProfileImageUpdated){
                    BroadcastService.sendUpdateUserDetailBroadcast(context,BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString(), userId);
                }else{
                    BroadcastService.sendUpdateLastSeenAtTimeBroadcast(context,BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString(), userId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String getTopicId(Integer conversationId) {
        try {
            String topicId = null;
            String url = getProductTopicIdUrl() + "?conversationId=" + conversationId;
            String response = httpRequestUtils.getResponse(url, "application/json", "application/json");
            if (response == null || TextUtils.isEmpty(response) || response.equals("UnAuthorized Access")) {
                return null;
            }
            ApiResponse productConversationIdResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            if ("success".equals(productConversationIdResponse.getStatus())) {
                topicId = productConversationIdResponse.getResponse().toString();
                return topicId;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }


    public MessageInfoResponse getMessageInfoList(String messageKey) {

        String url = getMessageInfoUrl() + "?key=" + messageKey;
        String response = httpRequestUtils.getResponse(url, "application/json", "application/json");

        if (response == null || TextUtils.isEmpty(response) || response.equals("UnAuthorized Access")) {
            return null;
        }
        MessageInfoResponse messageInfoResponse =
                (MessageInfoResponse) GsonUtils.getObjectFromJson(response, MessageInfoResponse.class);
        return messageInfoResponse;
    }

}
