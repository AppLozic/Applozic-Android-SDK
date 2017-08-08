package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.selfdestruct.DisappearingMessageTask;
import com.applozic.mobicomkit.api.notification.VideoCallNotificationHelper;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.sync.SyncMessageFeed;

import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.personalization.PersonalizedMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

/**
 * Created by devashish on 24/3/15.
 */
public class MobiComMessageService {

    public static final long DELAY = 60000L;
    private static final String TAG = "MobiComMessageService";
    public static Map<String, Uri> map = new HashMap<String, Uri>();
    public static Map<String, Message> mtMessages = new LinkedHashMap<String, Message>();
    protected Context context;
    protected MobiComConversationService conversationService;
    protected MessageDatabaseService messageDatabaseService;
    protected MessageClientService messageClientService;
    protected Class messageIntentServiceClass;
    protected BaseContactService baseContactService;
    protected UserService userService;
    protected FileClientService fileClientService;

    public MobiComMessageService(Context context, Class messageIntentServiceClass) {
        this.context = context;
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.messageClientService = new MessageClientService(context);
        this.conversationService = new MobiComConversationService(context);
        this.messageIntentServiceClass = messageIntentServiceClass;
        //Todo: this can be changed to DeviceContactService for device contacts usage.
        this.baseContactService = new AppContactService(context);
        fileClientService = new FileClientService(context);
        ;
        this.userService = UserService.getInstance(context);
    }

    public Message processMessage(final Message messageToProcess, String tofield) {
        try {
            if (!TextUtils.isEmpty(ApplozicClient.getInstance(context).getMessageMetaDataServiceName())) {
                Class serviceName = Class.forName(ApplozicClient.getInstance(context).getMessageMetaDataServiceName());
                Intent intentService = new Intent(context, serviceName);
                if (Message.MetaDataType.HIDDEN.getValue().equals(messageToProcess.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                    intentService.putExtra(MobiComKitConstants.MESSAGE, messageToProcess);
                    intentService.putExtra(MobiComKitConstants.HIDDEN, true);
                    context.startService(intentService);
                    return null;
                } else if (Message.MetaDataType.PUSHNOTIFICATION.getValue().equals(messageToProcess.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))) {
                    BroadcastService.sendNotificationBroadcast(context, messageToProcess);
                    intentService.putExtra(MobiComKitConstants.MESSAGE, messageToProcess);
                    intentService.putExtra(MobiComKitConstants.PUSH_NOTIFICATION, true);
                    context.startService(intentService);
                    return null;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Message message = prepareMessage(messageToProcess, tofield);
        //download contacts in advance.
        if (message.getGroupId() != null) {
            Channel channel = ChannelService.getInstance(context).getChannelInfo(message.getGroupId());
            if (channel == null) {
                return null;
            }
        }
        if (message.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
            fileClientService.loadContactsvCard(message);
        }
        try {
            List<String> messageKeys = new ArrayList<>();
            if (message.getMetadata() != null && message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()) != null && !messageDatabaseService.isMessagePresent(message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()))) {
                messageKeys.add(message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
            }
            if (messageKeys != null && messageKeys.size() > 0) {
                Message[] replyMessageList = conversationService.getMessageListByKeyList(messageKeys);
                if (replyMessageList != null) {
                    Message replyMessage = replyMessageList[0];
                    if (replyMessage != null) {
                        if (replyMessage.hasAttachment() && !(replyMessage.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                            conversationService.setFilePathifExist(replyMessage);
                        }
                        if (replyMessage.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                            fileClientService.loadContactsvCard(replyMessage);
                        }
                        replyMessage.setReplyMessage(Message.ReplyMessage.HIDE_MESSAGE.getValue());
                        messageDatabaseService.createMessage(replyMessage);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (message.getType().equals(Message.MessageType.MT_INBOX.getValue())) {
            addMTMessage(message);
        } else if (message.getType().equals(Message.MessageType.MT_OUTBOX.getValue())) {
            BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);
            messageDatabaseService.createMessage(message);
            if (!message.getCurrentId().equals(BroadcastService.currentUserId)) {
                MobiComUserPreference.getInstance(context).setNewMessageFlag(true);
            }
            if (message.isVideoNotificationMessage()) {
                Utils.printLog(context,TAG, "Got notifications for Video call...");
                VideoCallNotificationHelper helper = new VideoCallNotificationHelper(context);
                helper.handleVideoCallNotificationMessages(message);

            }
        }
        Utils.printLog(context,TAG, "processing message: " + message);
        return message;
    }

    public Message prepareMessage(Message messageToProcess, String tofield) {
        Message message = new Message(messageToProcess);
        message.setMessageId(messageToProcess.getMessageId());
        message.setKeyString(messageToProcess.getKeyString());
        message.setPairedMessageKeyString(messageToProcess.getPairedMessageKeyString());

        if (message.getMessage() != null && PersonalizedMessage.isPersonalized(message.getMessage())) {
            Contact contact = null;
            if (message.getGroupId() == null) {
                contact = baseContactService.getContactById(tofield);
            }
            if (contact != null) {
                message.setMessage(PersonalizedMessage.prepareMessageFromTemplate(message.getMessage(), contact));
            }
        }
        return message;
    }

    public Contact addMTMessage(Message message) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        Contact receiverContact = null;
        message.processContactIds(context);

        String currentId = message.getCurrentId();
        if (message.getGroupId() == null) {
            receiverContact = baseContactService.getContactById(message.getContactIds());
        }

        if (message.getMessage() != null && PersonalizedMessage.isPersonalized(message.getMessage())) {
            message.setMessage(PersonalizedMessage.prepareMessageFromTemplate(message.getMessage(), receiverContact));
        }

        messageDatabaseService.createMessage(message);

        //Check if we are........container is already opened...don't send broadcast
        boolean isContainerOpened;
        if (message.getConversationId() != null && BroadcastService.isContextBasedChatEnabled()) {
            if (BroadcastService.currentConversationId == null) {
                BroadcastService.currentConversationId = message.getConversationId();
            }
            isContainerOpened = (currentId.equals(BroadcastService.currentUserId) && message.getConversationId().equals(BroadcastService.currentConversationId));
        } else {
            isContainerOpened = currentId.equals(BroadcastService.currentUserId);
        }
        if (message.isVideoNotificationMessage()) {
            Utils.printLog(context,TAG, "Got notifications for Video call...");
            BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);

            VideoCallNotificationHelper helper = new VideoCallNotificationHelper(context);
            helper.handleVideoCallNotificationMessages(message);

        } else if (message.isVideoCallMessage()) {
            BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);
            VideoCallNotificationHelper.buildVideoCallNotification(context, message);
        } else if (!isContainerOpened) {
            if (message.isConsideredForCount()) {
                if (message.getTo() != null && message.getGroupId() == null) {
                    messageDatabaseService.updateContactUnreadCount(message.getTo());
                    BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);
                    sendNotification(message);
                }
                if (message.getGroupId() != null && !Message.GroupMessageMetaData.FALSE.getValue().equals(message.getMetaDataValueForKey(Message.GroupMessageMetaData.KEY.getValue()))) {
                    if (!Message.ContentType.CHANNEL_CUSTOM_MESSAGE.getValue().equals(message.getContentType())) {
                        messageDatabaseService.updateChannelUnreadCount(message.getGroupId());
                    }
                    BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);
                    Channel currentChannel = ChannelService.getInstance(context).getChannelInfo(message.getGroupId());
                    if (currentChannel != null && !currentChannel.isNotificationMuted()) {
                        sendNotification(message);
                    }
                }
                MobiComUserPreference.getInstance(context).setNewMessageFlag(true);
            } else {
                BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);
            }
        } else {
            BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString(), message);
        }

        Utils.printLog(context,TAG, "Updating delivery status: " + message.getPairedMessageKeyString() + ", " + userPreferences.getUserId() + ", " + userPreferences.getContactNumber());
        messageClientService.updateDeliveryStatus(message.getPairedMessageKeyString(), userPreferences.getUserId(), userPreferences.getContactNumber());
        return receiverContact;
    }

    public void sendNotification(Message message) {
        BroadcastService.sendNotificationBroadcast(context, message);
        Intent intent = new Intent(MobiComKitConstants.APPLOZIC_UNREAD_COUNT);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public synchronized void syncMessages() {
        final MobiComUserPreference userpref = MobiComUserPreference.getInstance(context);
        Utils.printLog(context,TAG, "Starting syncMessages for lastSyncTime: " + userpref.getLastSyncTime());
        SyncMessageFeed syncMessageFeed = messageClientService.getMessageFeed(userpref.getLastSyncTime());
        if (syncMessageFeed == null) {
            return;
        }
        if (syncMessageFeed != null && syncMessageFeed.getMessages() != null) {
            Utils.printLog(context,TAG, "Got sync response " + syncMessageFeed.getMessages().size() + " messages.");
            processUserDetailFromMessages(syncMessageFeed.getMessages());
        }
        // if regIdInvalid in syncrequest, tht means device reg with c2dm is no
        // more valid, do it again and make the sync request again
        if (syncMessageFeed != null && syncMessageFeed.isRegIdInvalid()
                && Utils.hasFroyo()) {
            Utils.printLog(context,TAG, "Going to call GCM device registration");
            //Todo: Replace it with mobicomkit gcm registration
            // C2DMessaging.register(context);
        }
        if (syncMessageFeed != null && syncMessageFeed.getMessages() != null) {
            List<Message> messageList = syncMessageFeed.getMessages();

            for (final Message message : messageList) {
                if (Message.ContentType.CHANNEL_CUSTOM_MESSAGE.getValue().equals(message.getContentType())) {
                    ChannelService.getInstance(context).syncChannels();
                    //Todo: fix this, what if there are mulitple messages.
                    ChannelService.isUpdateTitle = true;
                }
                processMessage(message, message.getTo());
                MobiComUserPreference.getInstance(context).setLastInboxSyncTime(message.getCreatedAtTime());
            }

            updateDeliveredStatus(syncMessageFeed.getDeliveredMessageKeys());
            userpref.setLastSyncTime(String.valueOf(syncMessageFeed.getLastSyncTime()));
        }
    }

    public MessageInfoResponse getMessageInfoResponse(String messageKey) {
        MessageInfoResponse messageInfoResponse = messageClientService.getMessageInfoList(messageKey);
        return messageInfoResponse;

    }

    private void updateDeliveredStatus(List<String> deliveredMessageKeys) {
        if (deliveredMessageKeys == null) {
            return;
        }
        for (String messageKey : deliveredMessageKeys) {
            messageDatabaseService.updateMessageDeliveryReportForContact(messageKey, false);
            Message message = messageDatabaseService.getMessage(messageKey);
            if (message != null) {
                BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString(), message);
            }
        }
    }

    public boolean isMessagePresent(String key) {
        return messageDatabaseService.isMessagePresent(key);
    }

    public synchronized void syncMessagesWithServer(String syncMessage) {
        Toast.makeText(context, syncMessage, Toast.LENGTH_LONG).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncMessages();
            }
        }).start();
    }

    public void processContactFromMessages(List<Message> messages) {
        try {

            if (!ApplozicClient.getInstance(context).isHandleDisplayName()) {
                return;
            }
            Set<String> userIds = new HashSet<String>();

            for (Message msg : messages) {
                if (!baseContactService.isContactExists(msg.getContactIds())) {
                    userIds.add(msg.getContactIds());
                }
            }

            if (userIds.isEmpty()) {
                return;
            }

            try {
                Map<String, String> userIdsHashMap = new UserClientService(context).getUserInfo(userIds);

                for (Map.Entry<String, String> keyValue : userIdsHashMap.entrySet()) {
                    Contact contact = new Contact();
                    contact.setUserId(keyValue.getKey());
                    contact.setFullName(keyValue.getValue());
                    contact.setUnreadCount(0);
                    baseContactService.upsert(contact);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {

        }
    }

    public void processUserDetailFromMessages(List<Message> messages) {
        try {
            if (!ApplozicClient.getInstance(context).isHandleDisplayName()) {
                return;
            }
            Set<String> userIds = new HashSet<String>();
            for (Message msg : messages) {
                if (!baseContactService.isContactPresent(msg.getContactIds())) {
                    userIds.add(msg.getContactIds());
                }
            }

            if (userIds.isEmpty()) {
                return;
            }
            userService.processUserDetails(userIds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void putMtextToDatabase(String payloadForMtextReceived) {
        JSONObject json = null;
        try {
            json = new JSONObject(payloadForMtextReceived);

            String smsKeyString = json.getString("keyString");
            String receiverNumber = json.getString("contactNumber");
            String body = json.getString("message");
            Integer timeToLive = json.isNull("timeToLive") ? null : Integer.parseInt(json.getString("timeToLive"));
            Message mTextMessageReceived = new Message();
            mTextMessageReceived.setTo(json.getString("senderContactNumber"));
            mTextMessageReceived.setCreatedAtTime(System.currentTimeMillis());
            mTextMessageReceived.setMessage(body);
            mTextMessageReceived.setSendToDevice(Boolean.FALSE);
            mTextMessageReceived.setSent(Boolean.TRUE);
            mTextMessageReceived.setDeviceKeyString(MobiComUserPreference.getInstance(context).getDeviceKeyString());
            mTextMessageReceived.setType(Message.MessageType.MT_INBOX.getValue());
            mTextMessageReceived.setSource(Message.Source.MT_MOBILE_APP.getValue());
            mTextMessageReceived.setTimeToLive(timeToLive);

         /*   if (json.has("fileMetaKeyStrings")) {
                JSONArray fileMetaKeyStringsJSONArray = json.getJSONArray("fileMetaKeyStrings");
                List<String> fileMetaKeyStrings = new ArrayList<String>();
                for (int i = 0; i < fileMetaKeyStringsJSONArray.length(); i++) {
                    JSONObject fileMeta = fileMetaKeyStringsJSONArray.getJSONObject(i);
                    fileMetaKeyStrings.add(fileMeta.toString());
                }
                //mTextMessageReceived.setFileMetaKeyStrings(fileMetaKeyStrings);
            }*/

            mTextMessageReceived.processContactIds(context);

            mTextMessageReceived.setTo(mTextMessageReceived.getTo());
            Contact receiverContact = baseContactService.getContactById(receiverNumber);

            if (mTextMessageReceived.getMessage() != null && PersonalizedMessage.isPersonalized(mTextMessageReceived.getMessage())) {
                mTextMessageReceived.setMessage(PersonalizedMessage.prepareMessageFromTemplate(mTextMessageReceived.getMessage(), receiverContact));
            }

            try {
                messageClientService.sendMessageToServer(mTextMessageReceived);
            } catch (Exception ex) {
                Utils.printLog(context,TAG, "Received message error " + ex.getMessage());
            }
            messageClientService.updateDeliveryStatus(smsKeyString, null, receiverNumber);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    public void addWelcomeMessage(String content) {
        Message message = new Message();
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        message.setContactIds(new Support(context).getSupportNumber());
        message.setTo(new Support(context).getSupportNumber());
        message.setMessage(content);
        message.setStoreOnDevice(Boolean.TRUE);
        message.setSendToDevice(Boolean.FALSE);
        message.setType(Message.MessageType.MT_INBOX.getValue());
        message.setDeviceKeyString(userPreferences.getDeviceKeyString());
        message.setSource(Message.Source.MT_MOBILE_APP.getValue());
        conversationService.sendMessage(message, messageIntentServiceClass);
    }

    public void sendCustomMessage(Message message) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        message.setStoreOnDevice(Boolean.TRUE);
        message.setSendToDevice(Boolean.FALSE);
        message.setType(Message.MessageType.MT_OUTBOX.getValue());
        message.setContentType(Message.ContentType.CUSTOM.getValue());
        message.setDeviceKeyString(userPreferences.getDeviceKeyString());
        message.setSource(Message.Source.MT_MOBILE_APP.getValue());
        conversationService.sendMessage(message, messageIntentServiceClass);
    }

    public synchronized void updateDeliveryStatusForContact(String contactId, boolean markRead) {
        int rows = messageDatabaseService.updateMessageDeliveryReportForContact(contactId, markRead);
        Utils.printLog(context,TAG, "Updated delivery report of " + rows + " messages for contactId: " + contactId);

        if (rows > 0) {
            String action = markRead ? BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString() :
                    BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString();
            BroadcastService.sendDeliveryReportForContactBroadcast(context, action, contactId);
        }
    }

    public synchronized void updateDeliveryStatus(String key, boolean markRead) {
        //Todo: Check if this is possible? In case the delivery report reaches before the sms is reached, then wait for the sms.
        Utils.printLog(context,TAG, "Got the delivery report for key: " + key);
        String keyParts[] = key.split((","));
        Message message = messageDatabaseService.getMessage(keyParts[0]);
        if (message != null && (message.getStatus() != Message.Status.DELIVERED_AND_READ.getValue())) {
            message.setDelivered(Boolean.TRUE);

            if (markRead) {
                message.setStatus(Message.Status.DELIVERED_AND_READ.getValue());
            } else {
                message.setStatus(Message.Status.DELIVERED.getValue());
            }
            //Todo: Server need to send the contactNumber of the receiver in case of group messaging and update
            //delivery report only for that number
            messageDatabaseService.updateMessageDeliveryReportForContact(keyParts[0], null, markRead);
            String action = markRead ? BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString() :
                    BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString();
            BroadcastService.sendMessageUpdateBroadcast(context, action, message);
            if (message.getTimeToLive() != null && message.getTimeToLive() != 0) {
                Timer timer = new Timer();
                timer.schedule(new DisappearingMessageTask(context, new MobiComConversationService(context), message), message.getTimeToLive() * 60 * 1000);
            }
        } else if (message == null) {
            Utils.printLog(context,TAG, "Message is not present in table, keyString: " + keyParts[0]);
        }
        map.remove(key);
        mtMessages.remove(key);
    }

    public void createEmptyMessages(List<Contact> contactList) {
        for (Contact contact : contactList) {
            createEmptyMessage(contact);
        }

        BroadcastService.sendLoadMoreBroadcast(context, true);
    }

    public void createEmptyMessage(Contact contact) {
        Message sms = new Message();
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        sms.setContactIds(contact.getFormattedContactNumber());
        sms.setTo(contact.getContactNumber());
        sms.setCreatedAtTime(0L);
        sms.setStoreOnDevice(Boolean.TRUE);
        sms.setSendToDevice(Boolean.FALSE);
        sms.setType(Message.MessageType.MT_OUTBOX.getValue());
        sms.setDeviceKeyString(userPreferences.getDeviceKeyString());
        sms.setSource(Message.Source.MT_MOBILE_APP.getValue());
        messageDatabaseService.createMessage(sms);
    }
}
