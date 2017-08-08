package com.applozic.mobicomkit.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.Message;

import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

/**
 * Created by devashish on 24/1/15.
 */
public class BroadcastService {

    private static final String TAG = "BroadcastService";
    private static final String PACKAGE_NAME = "com.package.name";
    private static final String MOBICOMKIT_ALL = "MOBICOMKIT_ALL";

    public static String currentUserId = null;
    public static Integer currentConversationId = null;
    public static boolean mobiTexterBroadcastReceiverActivated;
    public static String currentInfoId = null;
    public static boolean videoCallAcitivityOpend = false;
    public static boolean callRinging = false;
    public static int lastIndexForChats = 0;
    private static boolean contextBasedChatEnabled = false;
    public static String currentUserProfileUserId= null;

    public static void selectMobiComKitAll() {
        currentUserId = MOBICOMKIT_ALL;
    }

    public static boolean isQuick() {
        return currentUserId != null && currentUserId.equals(MOBICOMKIT_ALL);
    }

    public static boolean isChannelInfo() {
        return currentInfoId != null;
    }

    public static boolean isIndividual() {
        return currentUserId != null && !isQuick();
    }

    public static synchronized boolean isContextBasedChatEnabled() {
        return contextBasedChatEnabled;
    }

    public static synchronized boolean setContextBasedChat(boolean contextBasedChat) {
        return contextBasedChatEnabled = contextBasedChat;
    }

    public static void sendFirstTimeSyncCompletedBroadcast(Context context) {
        Utils.printLog(context,TAG, "Sending " + INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString() + " broadcast");
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intent);
    }

    public static void sendLoadMoreBroadcast(Context context, boolean loadMore) {
        Utils.printLog(context,TAG, "Sending " + INTENT_ACTIONS.LOAD_MORE.toString() + " broadcast");
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTIONS.LOAD_MORE.toString());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("loadMore", loadMore);
        sendBroadcast(context, intent);
    }

    public static void sendDeliveryReportForContactBroadcast(Context context, String action, String contactId) {
        Utils.printLog(context,TAG, "Sending message delivery report of contact broadcast for " + action + ", " + contactId);
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(action);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(MobiComKitConstants.CONTACT_ID, contactId);
        sendBroadcast(context, intentUpdate);
    }

    public static void sendMessageUpdateBroadcast(Context context, String action, Message message) {
        Utils.printLog(context,TAG, "Sending message update broadcast for " + action + ", " + message.getKeyString());
        Intent intentUpdate = new Intent();
        intentUpdate.setAction(action);
        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
        intentUpdate.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        sendBroadcast(context, intentUpdate);
    }

    public static void sendMessageDeleteBroadcast(Context context, String action, String keyString, String contactNumbers) {
        Utils.printLog(context,TAG, "Sending message delete broadcast for " + action);
        Intent intentDelete = new Intent();
        intentDelete.setAction(action);
        intentDelete.putExtra("keyString", keyString);
        intentDelete.putExtra("contactNumbers", contactNumbers);
        intentDelete.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intentDelete);
    }

    public static void sendConversationDeleteBroadcast(Context context, String action, String contactNumber, Integer channelKey, String response) {
        Utils.printLog(context,TAG, "Sending conversation delete broadcast for " + action);
        Intent intentDelete = new Intent();
        intentDelete.setAction(action);
        intentDelete.putExtra("channelKey", channelKey);
        intentDelete.putExtra("contactNumber", contactNumber);
        intentDelete.putExtra("response", response);
        intentDelete.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intentDelete);
    }

    public static void sendNotificationBroadcast(Context context, Message message) {
        Utils.printLog(context,TAG, "Sending notification broadcast....");
        Intent notificationIntent = new Intent();
        notificationIntent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        notificationIntent.setAction(Utils.getMetaDataValue(context.getApplicationContext(), PACKAGE_NAME) + ".send.notification");
        context.sendBroadcast(notificationIntent);
    }

    public static void sendUpdateLastSeenAtTimeBroadcast(Context context, String action, String contactId) {
        Utils.printLog(context,TAG, "Sending lastSeenAt broadcast....");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("contactId", contactId);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intent);
    }

    public static void sendUpdateTypingBroadcast(Context context, String action, String applicationId, String userId, String isTyping) {
        Utils.printLog(context,TAG, "Sending typing Broadcast.......");
        Intent intentTyping = new Intent();
        intentTyping.setAction(action);
        intentTyping.putExtra("applicationId", applicationId);
        intentTyping.putExtra("userId", userId);
        intentTyping.putExtra("isTyping", isTyping);
        intentTyping.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intentTyping);
    }


    public static void sendUpdate(Context context, String action) {
        Utils.printLog(context,TAG, action);
        Intent intent = new Intent();
        intent.setAction(action);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intent);
    }


    public static void sendConversationReadBroadcast(Context context, String action, String currentId, boolean isGroup) {
        Utils.printLog(context,TAG, "Sending  Broadcast for conversation read ......");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("currentId", currentId);
        intent.putExtra("isGroup", isGroup);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(context, intent);
    }

    public static void sendUpdateUserDetailBroadcast(Context context, String action, String contactId){
        Utils.printLog(context,TAG, "Sending profileImage update....");
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra("contactId", contactId);
        sendBroadcast(context, intent);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString());
        intentFilter.addAction(INTENT_ACTIONS.LOAD_MORE.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString());
        intentFilter.addAction(INTENT_ACTIONS.SYNC_MESSAGE.toString());
        intentFilter.addAction(INTENT_ACTIONS.DELETE_MESSAGE.toString());
        intentFilter.addAction(INTENT_ACTIONS.DELETE_CONVERSATION.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_DELIVERY.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString());
        intentFilter.addAction(INTENT_ACTIONS.INSTRUCTION.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString());
        intentFilter.addAction(INTENT_ACTIONS.MQTT_DISCONNECTED.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_CHANNEL_NAME.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString());
        intentFilter.addAction(INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString());
        intentFilter.addAction(INTENT_ACTIONS.CHANNEL_SYNC.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString());
        intentFilter.addAction(INTENT_ACTIONS.CONVERSATION_READ.toString());
        intentFilter.addAction(INTENT_ACTIONS.UPDATE_USER_DETAIL.toString());
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        return intentFilter;
    }

    public static void sendBroadcast(Context context, Intent intent) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public enum INTENT_ACTIONS {
        LOAD_MORE, FIRST_TIME_SYNC_COMPLETE, MESSAGE_SYNC_ACK_FROM_SERVER,
        SYNC_MESSAGE, DELETE_MESSAGE, DELETE_CONVERSATION, MESSAGE_DELIVERY, MESSAGE_DELIVERY_FOR_CONTACT, INSTRUCTION,
        UPLOAD_ATTACHMENT_FAILED, MESSAGE_ATTACHMENT_DOWNLOAD_DONE, MESSAGE_ATTACHMENT_DOWNLOAD_FAILD,
        UPDATE_LAST_SEEN_AT_TIME, UPDATE_TYPING_STATUS, MESSAGE_READ_AND_DELIVERED, MESSAGE_READ_AND_DELIVERED_FOR_CONTECT, CHANNEL_SYNC,
        CONTACT_VERIFIED, NOTIFY_USER, MQTT_DISCONNECTED, UPDATE_CHANNEL_NAME, UPDATE_TITLE_SUBTITLE, CONVERSATION_READ,UPDATE_USER_DETAIL
    }
}
