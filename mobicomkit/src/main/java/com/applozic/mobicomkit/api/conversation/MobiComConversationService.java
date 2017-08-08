package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserDetail;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.sync.SyncUserDetailsResponse;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.AnnotationExclusionStrategy;
import com.applozic.mobicommons.json.ArrayAdapterFactory;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MobiComConversationService {

    public static final String SERVER_SYNC = "SERVER_SYNC_[CONVERSATION]_[CONTACT]_[CHANNEL]";
    private static final String TAG = "Conversation";
    protected Context context = null;
    protected MessageClientService messageClientService;
    protected MessageDatabaseService messageDatabaseService;
    private SharedPreferences sharedPreferences;
    private BaseContactService baseContactService;
    private ConversationService conversationService;
    private ChannelService channelService;

    public MobiComConversationService(Context context) {
        this.context = context;
        this.messageClientService = new MessageClientService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.baseContactService = new AppContactService(context);
        this.conversationService = ConversationService.getInstance(context);
        this.channelService = ChannelService.getInstance(context);
        this.sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), context.MODE_PRIVATE);
    }

    public void sendMessage(Message message) {
        sendMessage(message, MessageIntentService.class);
    }

    public void sendMessage(Message message, Class messageIntentClass) {
        Intent intent = new Intent(context, messageIntentClass);
        intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        context.startService(intent);
    }

    public List<Message> getLatestMessagesGroupByPeople() {
        return getLatestMessagesGroupByPeople(null, null);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(Long createdAt, String searchString) {
        boolean emptyTable = messageDatabaseService.isMessageTableEmpty();

        if (emptyTable || createdAt != null && createdAt != 0) {
            getMessages(null, createdAt, null, null, null, false);
        }

        return messageDatabaseService.getMessages(createdAt, searchString);
    }

    public List<Message> getMessages(String userId, Long startTime, Long endTime) {
        return getMessages(startTime, endTime, new Contact(userId), null, null, false);
    }

    public synchronized List<Message> getMessages(Long startTime, Long endTime, Contact contact, Channel channel, Integer conversationId) {
        return getMessages(startTime, endTime, contact, channel, conversationId, false);
    }

    public synchronized List<Message> getMessages(Long startTime, Long endTime, Contact contact, Channel channel, Integer conversationId, boolean isSkipRead) {
        List<Message> messageList = new ArrayList<Message>();
        List<Message> cachedMessageList = messageDatabaseService.getMessages(startTime, endTime, contact, channel, conversationId);
        boolean isServerCallNotRequired = false;

        if (channel != null) {
            Channel newChannel = ChannelService.getInstance(context).getChannelByChannelKey(channel.getKey());
            isServerCallNotRequired = (newChannel != null && !Channel.GroupType.OPEN.getValue().equals(newChannel.getType()) || channelService.processIsUserPresentInChannel(channel.getKey()));
        } else if (contact != null) {
            isServerCallNotRequired = true;
        }

        if (isServerCallNotRequired && (!cachedMessageList.isEmpty() &&
                (cachedMessageList.size() > 1 || wasServerCallDoneBefore(contact, channel, conversationId))
                || (contact == null && channel == null && cachedMessageList.isEmpty() && wasServerCallDoneBefore(contact, channel, conversationId)))) {
            Utils.printLog(context,TAG, "cachedMessageList size is : " + cachedMessageList.size());
            return cachedMessageList;
        }

        String data;
        try {
            data = messageClientService.getMessages(contact, channel, startTime, endTime, conversationId, isSkipRead);
            Utils.printLog(context,TAG, "Received response from server for Messages: " + data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return cachedMessageList;
        }

        if (data == null || TextUtils.isEmpty(data) || data.equals("UnAuthorized Access") || !data.contains("{")) {
            //Note: currently not supporting syncing old channel messages from server
            if (channel != null && channel.getKey() != null) {
                return cachedMessageList;
            }
            return cachedMessageList;
        }

        updateServerCallDoneStatus(contact, channel, conversationId);

        try {
            Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ArrayAdapterFactory())
                    .setExclusionStrategies(new AnnotationExclusionStrategy()).create();
            JsonParser parser = new JsonParser();
            JSONObject jsonObject = new JSONObject(data);
            String channelFeedResponse = "";
            String conversationPxyResponse = "";
            String element = parser.parse(data).getAsJsonObject().get("message").toString();
            String userDetailsElement = parser.parse(data).getAsJsonObject().get("userDetails").toString();

            if (!TextUtils.isEmpty(userDetailsElement)) {
                UserDetail[] userDetails = (UserDetail[]) GsonUtils.getObjectFromJson(userDetailsElement, UserDetail[].class);
                processUserDetails(userDetails);
            }

            if (jsonObject.has("groupFeeds")) {
                channelFeedResponse = parser.parse(data).getAsJsonObject().get("groupFeeds").toString();
                ChannelFeed[] channelFeeds = (ChannelFeed[]) GsonUtils.getObjectFromJson(channelFeedResponse, ChannelFeed[].class);
                ChannelService.getInstance(context).processChannelFeedList(channelFeeds, false);
                if (channel != null && !isServerCallNotRequired) {
                    BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString());
                }
            }
            if (jsonObject.has("conversationPxys")) {
                conversationPxyResponse = parser.parse(data).getAsJsonObject().get("conversationPxys").toString();
                Conversation[] conversationPxy = (Conversation[]) GsonUtils.getObjectFromJson(conversationPxyResponse, Conversation[].class);
                ConversationService.getInstance(context).processConversationArray(conversationPxy, channel, contact);
            }
            Message[] messages = gson.fromJson(element, Message[].class);
            MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
            /*String connectedUsersResponse = parser.parse(data).getAsJsonObject().get("connectedUsers").toString();
            String[] connectedUserIds = (String[]) GsonUtils.getObjectFromJson(connectedUsersResponse, String[].class);*/

            if (messages != null && messages.length > 0 && cachedMessageList.size() > 0 && cachedMessageList.get(0).isLocalMessage()) {
                if (cachedMessageList.get(0).equals(messages[0])) {
                    Utils.printLog(context,TAG, "Both messages are same.");
                    deleteMessage(cachedMessageList.get(0));
                }
            }


            for (Message message : messages) {
                if (!message.isCall() || userPreferences.isDisplayCallRecordEnable()) {
                    //TODO: remove this check..right now in some cases it is coming as null.
                    // we have to figure out if it is a parsing problem or response from server.
                    if (message.getTo() == null) {
                        continue;
                    }

                    if (message.hasAttachment() && !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        setFilePathifExist(message);
                    }
                    if (message.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                        FileClientService fileClientService = new FileClientService(context);
                        fileClientService.loadContactsvCard(message);
                    }
                    if (Message.MetaDataType.HIDDEN.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                        continue;
                    }
                    if (messageDatabaseService.isMessagePresent(message.getKeyString(), Message.ReplyMessage.HIDE_MESSAGE.getValue())) {
                        messageDatabaseService.updateMessageReplyType(message.getKeyString(), Message.ReplyMessage.NON_HIDDEN.getValue());
                    } else {
                        messageDatabaseService.createMessage(message);
                    }
                    if (contact == null && channel == null) {
                        if (message.isHidden()) {
                            if (message.getGroupId() != null) {
                                Channel newChannel = ChannelService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                                if (newChannel != null) {
                                    getMessages(null, null, null, newChannel, null, true);
                                }
                            } else {
                                getMessages(null, null, new Contact(message.getContactIds()), null, null, true);
                            }
                        }
                    }
                }
            }
            if (contact == null && channel == null) {
                Intent intent = new Intent(MobiComKitConstants.APPLOZIC_UNREAD_COUNT);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

     /*   messageList.removeAll(cachedMessageList);
        messageList.addAll(cachedMessageList);

        Collections.sort(messageList, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                return lhs.getCreatedAtTime().compareTo(rhs.getCreatedAtTime());
            }
        });*/

        List<Message> finalMessageList = messageDatabaseService.getMessages(startTime, endTime, contact, channel, conversationId);
        List<String> messageKeys = new ArrayList<>();
        for (Message msg : finalMessageList) {
            if (msg.getTo() == null) {
                continue;
            }
            if (Message.MetaDataType.HIDDEN.getValue().equals(msg.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(msg.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                continue;
            }
            if (msg.getMetadata() != null && msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()) != null && !messageDatabaseService.isMessagePresent(msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()))) {
                messageKeys.add(msg.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
            }
        }
        if (messageKeys != null && messageKeys.size() > 0) {
            Message[] replyMessageList = getMessageListByKeyList(messageKeys);
            if (replyMessageList != null) {
                for (Message replyMessage : replyMessageList) {
                    if (replyMessage.getTo() == null) {
                        continue;
                    }
                    if (Message.MetaDataType.HIDDEN.getValue().equals(replyMessage.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue())) || Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(replyMessage.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                        continue;
                    }
                    if (replyMessage.hasAttachment() && !(replyMessage.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        setFilePathifExist(replyMessage);
                    }
                    if (replyMessage.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                        FileClientService fileClientService = new FileClientService(context);
                        fileClientService.loadContactsvCard(replyMessage);
                    }
                    replyMessage.setReplyMessage(Message.ReplyMessage.HIDE_MESSAGE.getValue());
                    messageDatabaseService.createMessage(replyMessage);
                }
            }
        }

        return finalMessageList;
    }

    private void processUserDetails(SyncUserDetailsResponse userDetailsResponse) {
        for (UserDetail userDetail : userDetailsResponse.getResponse()) {
            Contact newContact = baseContactService.getContactById(userDetail.getUserId());
            Contact contact = new Contact();
            contact.setUserId(userDetail.getUserId());
            contact.setContactNumber(userDetail.getPhoneNumber());
            contact.setStatus(userDetail.getStatusMessage());
            //contact.setApplicationId(); Todo: set the application id
            contact.setConnected(userDetail.isConnected());
            contact.setFullName(userDetail.getDisplayName());
            contact.setLastSeenAt(userDetail.getLastSeenAtTime());
            if (userDetail.getUnreadCount() != null) {
                contact.setUnreadCount(userDetail.getUnreadCount());
            }
            if (!TextUtils.isEmpty(userDetail.getImageLink())) {
                contact.setImageURL(userDetail.getImageLink());
            }
            contact.setUserTypeId(userDetail.getUserTypeId());
            contact.setDeletedAtTime(userDetail.getDeletedAtTime());
            if (newContact != null) {
                if (newContact.isConnected() != contact.isConnected()) {
                    BroadcastService.sendUpdateLastSeenAtTimeBroadcast(context, BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString(), contact.getContactIds());
                }
            }
            baseContactService.upsert(contact);
        }
        MobiComUserPreference.getInstance(context).setLastSeenAtSyncTime(userDetailsResponse.getGeneratedAt());
    }


    public Message[] getMessageListByKeyList(List<String> messageKeyList) {
        String response = messageClientService.getMessageByMessageKeys(messageKeyList);
        if (!TextUtils.isEmpty(response)) {
            JsonParser parser = new JsonParser();
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                String status = null;
                if (jsonObject.has("status")) {
                    status = jsonObject.getString("status");
                }
                if (!TextUtils.isEmpty(status) && "success".equals(status)) {
                    String responseString = jsonObject.getString("response");
                    String messageResponse = parser.parse(responseString).getAsJsonObject().get("message").toString();
                    if (!TextUtils.isEmpty(messageResponse)) {
                        return (Message[]) GsonUtils.getObjectFromJson(messageResponse, Message[].class);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean wasServerCallDoneBefore(Contact contact, Channel channel, Integer conversationId) {
        if (contact == null && channel == null) {
            return false;
        }
        return sharedPreferences.getBoolean(getServerSyncCallKey(contact, channel, conversationId), false);
    }

    private void updateServerCallDoneStatus(Contact contact, Channel channel, Integer conversationId) {
        if (contact == null && channel == null) {
            return;
        }
        Utils.printLog(context,TAG, "updating server call to true");
        sharedPreferences.edit().putBoolean(getServerSyncCallKey(contact, channel, conversationId), true).commit();
    }

    public String getServerSyncCallKey(Contact contact, Channel channel, Integer conversationId) {
        return SERVER_SYNC.replace("[CONVERSATION]", conversationId != null ? String.valueOf(conversationId) : "")
                .replace("[CONTACT]", contact != null ? contact.getContactIds() : "")
                .replace("[CHANNEL]", channel != null ? String.valueOf(channel.getKey()) : "");
    }

    public void setFilePathifExist(Message message) {
        FileMeta fileMeta = message.getFileMetas();
        File file = FileClientService.getFilePath(FileUtils.getName(fileMeta.getName()) + message.getCreatedAtTime() + "." + FileUtils.getFileFormat(fileMeta.getName()), context.getApplicationContext(), fileMeta.getContentType());
        if (file.exists()) {
            ArrayList<String> arrayList = new ArrayList<String>();
            arrayList.add(file.getAbsolutePath());
            message.setFilePaths(arrayList);
        }
    }

    public boolean deleteMessage(Message message, Contact contact) {
        if (!message.isSentToServer()) {
            deleteMessageFromDevice(message, contact != null ? contact.getContactIds() : null);
            return true;
        }
        String response = messageClientService.deleteMessage(message, contact);
        if ("success".equals(response)) {
            deleteMessageFromDevice(message, contact != null ? contact.getContactIds() : null);
        } else {
            messageDatabaseService.updateDeleteSyncStatus(message, "1");
        }
        return true;
    }

    public boolean deleteMessage(Message message) {
        return deleteMessage(message, null);
    }

    public String deleteMessageFromDevice(Message message, String contactNumber) {
        if (message == null) {
            return null;
        }
        return messageDatabaseService.deleteMessage(message, contactNumber);
    }

    public void deleteConversationFromDevice(String contactNumber) {
        messageDatabaseService.deleteConversation(contactNumber);
    }

    public void deleteChannelConversationFromDevice(Integer channelKey) {
        messageDatabaseService.deleteChannelConversation(channelKey);
    }

    public void deleteAndBroadCast(final Contact contact, boolean deleteFromServer) {
        deleteConversationFromDevice(contact.getContactIds());
        if (deleteFromServer) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    messageClientService.deleteConversationThreadFromServer(contact);
                }
            });
            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            thread.start();
        }
        BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(), contact.getContactIds(), 0, "success");
    }

    public String deleteSync(final Contact contact, final Channel channel, Integer conversationId) {
        String response = "";
        if (contact != null || channel != null) {
            response = messageClientService.syncDeleteConversationThreadFromServer(contact, channel);
        }

        if (!TextUtils.isEmpty(response) && "success".equals(response)) {
            if (contact != null) {
                messageDatabaseService.deleteConversation(contact.getContactIds());
                if (conversationId != null && conversationId != 0) {
                    conversationService.deleteConversation(contact.getContactIds());
                }
            } else {
                messageDatabaseService.deleteChannelConversation(channel.getKey());
            }

        }
        BroadcastService.sendConversationDeleteBroadcast(context, BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString(),
                contact != null ? contact.getContactIds() : null, channel != null ? channel.getKey() : null, response);
        return response;
    }

    public String deleteMessageFromDevice(String keyString, String contactNumber) {
        return deleteMessageFromDevice(messageDatabaseService.getMessage(keyString), contactNumber);
    }

    public synchronized void processLastSeenAtStatus() {
        try {
            SyncUserDetailsResponse userDetailsResponse = messageClientService.getUserDetailsList(MobiComUserPreference.getInstance(context).getLastSeenAtSyncTime());
            if (userDetailsResponse != null && userDetailsResponse.getResponse() != null && "success".equals(userDetailsResponse.getStatus())) {
                processUserDetails(userDetailsResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processUserDetails(UserDetail[] userDetails) {
        if (userDetails != null && userDetails.length > 0) {
            for (UserDetail userDetail : userDetails) {
                Contact contact = new Contact();
                contact.setUserId(userDetail.getUserId());
                contact.setContactNumber(userDetail.getPhoneNumber());
                contact.setConnected(userDetail.isConnected());
                contact.setFullName(userDetail.getDisplayName());
                contact.setLastSeenAt(userDetail.getLastSeenAtTime());
                contact.setStatus(userDetail.getStatusMessage());
                contact.setUnreadCount(userDetail.getUnreadCount());
                contact.setUserTypeId(userDetail.getUserTypeId());
                contact.setImageURL(userDetail.getImageLink());
                contact.setDeletedAtTime(userDetail.getDeletedAtTime());
                baseContactService.upsert(contact);
            }
        }
    }

    public String getConversationIdString(Integer conversationId) {
        return BroadcastService.isContextBasedChatEnabled() && conversationId != null && conversationId != 0 ? "_" + conversationId : "";
    }

    public void read(Contact contact, Channel channel) {
        try {
            int unreadCount = 0;
            if (contact != null) {
                Contact newContact = baseContactService.getContactById(contact.getContactIds());
                unreadCount = newContact.getUnreadCount();
                messageDatabaseService.updateReadStatusForContact(contact.getContactIds());
            } else if (channel != null) {
                Channel newChannel = channelService.getChannelByChannelKey(channel.getKey());
                unreadCount = newChannel.getUnreadCount();
                messageDatabaseService.updateReadStatusForChannel(String.valueOf(newChannel.getKey()));
            }

            Intent intent = new Intent(context, ConversationReadService.class);
            intent.putExtra(ConversationReadService.CONTACT, contact);
            intent.putExtra(ConversationReadService.CHANNEL, channel);
            intent.putExtra(ConversationReadService.UNREAD_COUNT, unreadCount);
            context.startService(intent);
        } catch (Exception e) {
        }
    }


//    public void addFileMetaDetails(String responseString, Message message) {
//        JsonParser jsonParser = new JsonParser();
//        List<FileMeta> metaFileList = new ArrayList<FileMeta>();
//        JsonObject jsonObject = jsonParser.parse(responseString).getAsJsonObject();
//        if (jsonObject.has("fileMetas")) {
//            Gson gson = new Gson();
//            metaFileList.add(gson.fromJson(jsonObject.get("fileMetas"), FileMeta.class));
//        }
//        message.setFileMetas(metaFileList);
//    }

}