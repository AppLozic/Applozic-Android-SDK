package com.applozic.mobicomkit.api.conversation;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.applozic.mobicomkit.ConversationRunnables;
import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by applozic on 12/2/15.
 */
public class SyncCallService {

    private static final String TAG = "SyncCall";

    public static boolean refreshView = false;
    private static SyncCallService syncCallService;
    private Context context;
    private MobiComMessageService mobiComMessageService;
    private MobiComConversationService mobiComConversationService;
    private BaseContactService contactService;
    private ChannelService channelService;
    private MessageClientService messageClientService;
    private MessageDatabaseService messageDatabaseService;

    private SyncCallService(Context context) {
        this.context = context;
        this.mobiComMessageService = new MobiComMessageService(context, MessageIntentService.class);
        this.mobiComConversationService = new MobiComConversationService(context);
        this.contactService = new AppContactService(context);
        this.channelService = ChannelService.getInstance(context);
        this.messageClientService = new MessageClientService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
    }

    public static SyncCallService getInstance(Context context) {
        if (syncCallService == null) {
            syncCallService = new SyncCallService(context.getApplicationContext());
        }
        return syncCallService;
    }

    public synchronized void updateDeliveryStatus(String key) {
        mobiComMessageService.updateDeliveryStatus(key, false);
        refreshView = true;
    }

    public synchronized void updateReadStatus(String key) {
        mobiComMessageService.updateDeliveryStatus(key, true);
        refreshView = true;
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(String searchString) {
        return mobiComConversationService.getLatestMessagesGroupByPeople(null, searchString);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople() {
        return mobiComConversationService.getLatestMessagesGroupByPeople(null, null);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(Long createdAt, String searchString) {
        return mobiComConversationService.getLatestMessagesGroupByPeople(createdAt, searchString);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(String searchString,Integer parentGroupKey) {
        return mobiComConversationService.getLatestMessagesGroupByPeople(null,searchString,parentGroupKey);
    }

    public synchronized List<Message> getLatestMessagesGroupByPeople(Long createdAt,String searchString,Integer parentGroupKey) {
        return mobiComConversationService.getLatestMessagesGroupByPeople(createdAt,searchString,parentGroupKey);
    }

    public synchronized void syncMessages(String key) {
        syncMessages(key, null);
    }

    public synchronized void syncMessages(String key, Message message) {
        if (!TextUtils.isEmpty(key) && mobiComMessageService.isMessagePresent(key)) {
            Utils.printLog(context, TAG, "Message is already present, MQTT reached before GCM.");
        } else {
            if (Utils.isDeviceInIdleState(context)) {
                new ConversationRunnables(context, message, false, true, false);
            } else {
                Intent intent = new Intent(context, ConversationIntentService.class);
                intent.putExtra(ConversationIntentService.SYNC, true);
                if (message != null) {
                    intent.putExtra(ConversationIntentService.AL_MESSAGE, message);
                }
                ConversationIntentService.enqueueWork(context, intent);
            }
        }
    }

    public synchronized void syncMessageMetadataUpdate(String key, boolean isFromFcm) {
        if (!TextUtils.isEmpty(key) && mobiComMessageService.isMessagePresent(key)) {
            if (Utils.isDeviceInIdleState(context)) {
                new ConversationRunnables(context, null, false, false, true);
            } else {
                Utils.printLog(context, TAG, "Syncing updated message metadata from " + (isFromFcm ? "FCM" : "MQTT") + " for message key : " + key);
                Intent intent = new Intent(context, ConversationIntentService.class);
                intent.putExtra(ConversationIntentService.MESSAGE_METADATA_UPDATE, true);
                ConversationIntentService.enqueueWork(context, intent);
            }
        }
    }

    public synchronized void syncMutedUserList(boolean isFromFcm, String userId) {

        if (userId == null) {
            Utils.printLog(context, TAG, "Syncing muted user list from " + (isFromFcm ? "FCM" : "MQTT"));
            Intent intent = new Intent(context, ConversationIntentService.class);
            intent.putExtra(ConversationIntentService.MUTED_USER_LIST_SYNC, true);
            ConversationIntentService.enqueueWork(context, intent);
        } else {
            Utils.printLog(context, TAG, "Unmuting userId : " + userId + " from " + (isFromFcm ? "FCM" : "MQTT"));
            new ContactDatabase(context).updateNotificationAfterTime(userId, Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime().getTime());
            BroadcastService.sendMuteUserBroadcast(context, BroadcastService.INTENT_ACTIONS.MUTE_USER_CHAT.toString(), false, userId);
        }
    }

    public synchronized void updateDeliveryStatusForContact(String contactId, boolean markRead) {
        mobiComMessageService.updateDeliveryStatusForContact(contactId, markRead);
    }

    public synchronized void updateConversationReadStatus(String currentId, boolean isGroup) {
        if (TextUtils.isEmpty(currentId)) {
            return;
        }
        if (isGroup) {
            messageDatabaseService.updateChannelUnreadCountToZero(Integer.valueOf(currentId));
        } else {
            messageDatabaseService.updateContactUnreadCountToZero(currentId);
        }
        BroadcastService.sendConversationReadBroadcast(context, BroadcastService.INTENT_ACTIONS.CONVERSATION_READ.toString(), currentId, isGroup);
    }

    public synchronized void updateConnectedStatus(String contactId, Date date, boolean connected) {
        contactService.updateConnectedStatus(contactId, date, connected);
    }

    public synchronized void deleteConversationThread(String userId) {
        mobiComConversationService.deleteConversationFromDevice(userId);
        refreshView = true;
    }

    public synchronized void deleteChannelConversationThread(String channelKey) {
        mobiComConversationService.deleteChannelConversationFromDevice(Integer.valueOf(channelKey));
        refreshView = true;
    }

    public synchronized void deleteMessage(String messageKey) {
        mobiComConversationService.deleteMessageFromDevice(messageKey, null);
        refreshView = true;
    }

    public synchronized void updateUserBlocked(String userId, boolean userBlocked) {
        contactService.updateUserBlocked(userId, userBlocked);
    }

    public synchronized void updateUserBlockedBy(String userId, boolean userBlockedBy) {
        contactService.updateUserBlockedBy(userId, userBlockedBy);
    }

    public void syncBlockUsers() {
        UserService.getInstance(context).processSyncUserBlock();
    }

    public void checkAccountStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new RegisterUserClientService(context).syncAccountStatus();
            }
        }).start();
    }

    public void processUserStatus(String userId) {
        messageClientService.processUserStatus(userId);
    }

    public void syncUserDetail(String userId) {
        messageClientService.processUserStatus(userId, true);
    }

    public void processContactSync(final String userId) {
        Utils.printLog(context, TAG, "process contact sync for userId: " + userId);
        if (!TextUtils.isEmpty(userId) && contactService.isContactPresent(userId)) {
            Contact contact = contactService.getContactById(userId);


            if (contact.isApplozicType()) {
                Utils.printLog(context, TAG, "Contact is already present, MQTT reached before GCM.");
                return;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserService.getInstance(context).processContactSync();
            }
        }).start();
    }

}