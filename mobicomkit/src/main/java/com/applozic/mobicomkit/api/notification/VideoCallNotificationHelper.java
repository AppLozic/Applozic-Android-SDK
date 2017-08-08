package com.applozic.mobicomkit.api.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by devashish on 08/08/16.
 */
public class VideoCallNotificationHelper {

    public static final String CALL_STARTED = "CALL_STARTED";
    public static final String CALL_END = "CALL_END";
    public static final String MSG_TYPE = "MSG_TYPE";
    public static final String CALL_ID = "CALL_ID";
    public static final String CALL_DIALED = "CALL_DIALED";
    public static final String CALL_REJECTED = "CALL_REJECTED";
    public static final String CALL_ANSWERED = "CALL_ANSWERED";
    public static final String CALL_MISSED = "CALL_MISSED";
    public static final String CALL_CANCELED = "CALL_CANCELED";
    public static final String CALL_AUDIO_ONLY = "CALL_AUDIO_ONLY";
    public static final int MAX_NOTIFICATION_RING_DURATION = 1 * 60 * 1000;
    public static final String NOTIFICATION_ACTIVITY_NAME = "com.applozic.audiovideo.activity.CallActivity";
    public static final String CALL_DURATION = "CALL_DURATION";
    private static final String TAG = "CallNotiHandler";
    String videoCallId;
    Context context;
    boolean isAudioOnly;

    private MobiComConversationService conversationService;
    private AppContactService baseContactService;

    public VideoCallNotificationHelper(Context context) {
        this.context = context;
        this.isAudioOnly = false;
        init();
    }

    public VideoCallNotificationHelper(Context context, boolean isAudioOnly) {
        this.context = context;
        this.isAudioOnly = isAudioOnly;
        init();
    }

    public static String getStatus(Map<String, String> metaDataMap) {

        String type = metaDataMap.get(MSG_TYPE);

        String audioORVideoCallPrefix = Boolean.valueOf(metaDataMap.get(CALL_AUDIO_ONLY)) ? "Audio call" : "Video call";
        if (type.equals(CALL_STARTED)) {
            return audioORVideoCallPrefix + " started";
        } else if (type.equals(CALL_END)) {
            return audioORVideoCallPrefix;
        } else if (type.equals(CALL_REJECTED)) {
            return "Call busy";
        } else {
            return "Missed " + audioORVideoCallPrefix;
        }
    }

    public static boolean isMissedCall(Message message) {
        String msgType = message.getMetaDataValueForKey(VideoCallNotificationHelper.MSG_TYPE);
        return (VideoCallNotificationHelper.CALL_MISSED.equals(msgType)
                || VideoCallNotificationHelper.CALL_REJECTED.equals(msgType)
                || VideoCallNotificationHelper.CALL_CANCELED.equals(msgType));
    }

    public static boolean isAudioCall(Message message) {
        return Boolean.parseBoolean(message.getMetaDataValueForKey(CALL_AUDIO_ONLY));
    }

    public static void buildVideoCallNotification(Context context, Message message) {
        Map<String, String> metaDataMap = message.getMetadata();
        Contact contact = new AppContactService(context).getContactById(message.getContactIds());
        String audioORVideoCallPrefix = Boolean.valueOf(metaDataMap.get(CALL_AUDIO_ONLY)) ? "audio call " : "video call ";
        if (metaDataMap.get(VideoCallNotificationHelper.MSG_TYPE).equals(VideoCallNotificationHelper.CALL_MISSED)) {
            Message message1 = new Message(message);
            message1.setMessage("You missed " + audioORVideoCallPrefix + " from " + contact.getDisplayName());
            BroadcastService.sendNotificationBroadcast(context, message1);
        }
    }

    public void init() {
        this.conversationService = new MobiComConversationService(context);
        this.baseContactService = new AppContactService(context);
    }

    public Map<String, String> getDialCallMetaData() {

        Map<String, String> metaData = new HashMap<>();
        metaData.put(MSG_TYPE, CALL_DIALED);
        metaData.put(CALL_ID, videoCallId);
        metaData.put(CALL_AUDIO_ONLY, Boolean.toString(isAudioOnly));
        return metaData;

    }

    public Map<String, String> getAnswerCallMetaData() {

        Map<String, String> metaData = new HashMap<>();

        metaData.put(CALL_ID, videoCallId);
        metaData.put(MSG_TYPE, CALL_ANSWERED);
        metaData.put(CALL_AUDIO_ONLY, Boolean.toString(isAudioOnly));
        return metaData;

    }

    public Map<String, String> getVideoCallStartedMap() {

        Map<String, String> metaData = new HashMap<>();
        metaData.put(MSG_TYPE, CALL_STARTED);
        metaData.put(CALL_ID, videoCallId);
        metaData.put(CALL_AUDIO_ONLY, Boolean.toString(isAudioOnly));
        return metaData;

    }

    public Map<String, String> getVideoCallEndMap(String callDuration) {

        Map<String, String> metaData = new HashMap<>();
        metaData.put(MSG_TYPE, CALL_END);
        metaData.put(CALL_ID, videoCallId);
        metaData.put(CALL_DURATION, callDuration);
        metaData.put(CALL_AUDIO_ONLY, Boolean.toString(isAudioOnly));
        return metaData;

    }

    public Map<String, String> getVideoCanceledMap() {

        Map<String, String> metaData = new HashMap<>();
        metaData.put(MSG_TYPE, CALL_CANCELED);
        metaData.put(CALL_ID, videoCallId);
        metaData.put(CALL_AUDIO_ONLY, Boolean.toString(isAudioOnly));
        return metaData;

    }

    public Map<String, String> getRejectedCallMap() {

        Map<String, String> metaData = new HashMap<>();
        metaData.put(CALL_ID, videoCallId);
        metaData.put(MSG_TYPE, CALL_REJECTED);
        metaData.put(CALL_AUDIO_ONLY, Boolean.toString(isAudioOnly));
        return metaData;
    }

    public Map<String, String> getMissedCallMap() {
        Map<String, String> metaData = new HashMap<>();
        metaData.put(CALL_ID, videoCallId);
        metaData.put(MSG_TYPE, CALL_MISSED);
        metaData.put(CALL_AUDIO_ONLY, Boolean.toString(isAudioOnly));
        return metaData;
    }

    public String sendVideoCallRequest(Contact contact, boolean audioOnly) {
        Message notificationMessage = getNotificationMessage(contact);
        this.videoCallId = MobiComUserPreference.getInstance(context).getDeviceKeyString()
                + ":" + notificationMessage.getCreatedAtTime();
        notificationMessage.setMessage(videoCallId);
        notificationMessage.setMetadata(getDialCallMetaData());
        if (audioOnly) {
            notificationMessage.getMetadata().put(CALL_AUDIO_ONLY, "true");
        }
        conversationService.sendMessage(notificationMessage, MessageIntentService.class);
        return videoCallId;

    }

    /**
     * @param contact
     * @return
     */

    public String sendVideoCallRequest(Contact contact) {
        return sendVideoCallRequest(contact, false);
    }

    /**
     * @param contact
     * @return
     */
    public String sendAudioCallRequest(Contact contact) {
        return sendVideoCallRequest(contact, true);
    }

    public void sendVideoCallAnswer(Contact contact, String videoCallId) {

        Log.i(TAG, "sendVideoCallAnswer()");

        this.videoCallId = videoCallId;
        Message notificationMessage = getNotificationMessage(contact);
        notificationMessage.setMessage(videoCallId);
        notificationMessage.setMetadata(getAnswerCallMetaData());
        conversationService.sendMessage(notificationMessage, MessageIntentService.class);
        Log.i(TAG, "sendVideoCallAnswer()  END");

    }

//    public void sendVideoCallCanceled(Contact contact, String videoCallId) {
//
//        Message statusMessage = getVideoCallStatusMessage(contact);
//        statusMessage.setMetadata(getVideoCanceledMap());
//        statusMessage.setMessage(videoCallId);
//        conversationService.sendMessage(statusMessage, MessageIntentService.class);
//
//    }

//    public void sendVideoCallCanceledNotification(Contact contact, String videoCallId) {
//
//        Message statusMessage = getNotificationMessage(contact);
//        statusMessage.setMetadata(getVideoCanceledMap());
//        statusMessage.setMessage(videoCallId);
//        conversationService.sendMessage(statusMessage, MessageIntentService.class);
//
//    }

    public void sendVideoCallReject(Contact contact, String videoCallId) {
        this.videoCallId = videoCallId;
        Message notificationMessage = getNotificationMessage(contact);
        notificationMessage.setMetadata(getRejectedCallMap());
        notificationMessage.setMessage(videoCallId);
        conversationService.sendMessage(notificationMessage, MessageIntentService.class);
    }

    public void sendCallMissed(Contact contact, String videoCallId) {
        this.videoCallId = videoCallId;
        Message notificationMessage = getNotificationMessage(contact);
        notificationMessage.setMetadata(getMissedCallMap());
        notificationMessage.setMessage(videoCallId);
        conversationService.sendMessage(notificationMessage, MessageIntentService.class);
    }


//    public void updateVideoMessageStatus(String callId,String type){
//        messageDatabaseService.updateVideoCallMetaData(callId,type);
//    }

    public void sendVideoCallStarted(Contact contact, String videoCallId) {
        Message statusMessage = getVideoCallStatusMessage(contact);
        statusMessage.setMetadata(getVideoCallStartedMap());
        statusMessage.setMessage(videoCallId);
        conversationService.sendMessage(statusMessage, MessageIntentService.class);
    }

    public void sendVideoCallEnd(Contact contact, String videoCallId, String duration) {

        Message statusMessage = getVideoCallStatusMessage(contact);
        statusMessage.setMetadata(getVideoCallEndMap(duration));
        statusMessage.setMessage("Call End");
        conversationService.sendMessage(statusMessage, MessageIntentService.class);

    }

    @NonNull
    private Message getNotificationMessage(Contact contact) {
        Message notificationMessage = new Message();
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);

        notificationMessage.setContactIds(contact.getContactIds());
        notificationMessage.setTo(contact.getContactIds());
        notificationMessage.setCreatedAtTime(System.currentTimeMillis());

        notificationMessage.setStoreOnDevice(Boolean.TRUE);
        notificationMessage.setSendToDevice(Boolean.TRUE);
        notificationMessage.setContentType(Message.ContentType.VIDEO_CALL_NOTIFICATION_MSG.getValue());
        notificationMessage.setDeviceKeyString(userPreferences.getDeviceKeyString());
        notificationMessage.setMessage(videoCallId);
        return notificationMessage;
    }

    @NonNull
    private Message getVideoCallStatusMessage(Contact contact) {

        Message notificationMessage = new Message();
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);

        notificationMessage.setContactIds(contact.getContactIds());
        notificationMessage.setTo(contact.getContactIds());
        notificationMessage.setCreatedAtTime(System.currentTimeMillis());

        notificationMessage.setStoreOnDevice(Boolean.TRUE);
        notificationMessage.setSendToDevice(Boolean.TRUE);
        notificationMessage.setContentType(Message.ContentType.VIDEO_CALL_STATUS_MSG.getValue());
        notificationMessage.setDeviceKeyString(userPreferences.getDeviceKeyString());
        return notificationMessage;
    }

    public void handleVideoCallNotificationMessages(final Message message) {

        Map<String, String> valueMap = message.getMetadata();
        String type = valueMap.get(MSG_TYPE);
        videoCallId = valueMap.get(CALL_ID);

        if (TextUtils.isEmpty(type)) {
            return;
        }

        if (type.equals(CALL_DIALED)) {

            handleIncomingVideoNotification(message);

        } else if (type.equals(CALL_ANSWERED)) {

            Intent intent = new Intent(MobiComKitConstants.APPLOZIC_VIDEO_CALL_ANSWER);
            intent.putExtra(CALL_ID, videoCallId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        } else if (type.equals(CALL_REJECTED)) {

            Intent intent = new Intent(MobiComKitConstants.APPLOZIC_VIDEO_CALL_REJECTED);
            intent.putExtra(CALL_ID, videoCallId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

            if (!message.isTypeOutbox() && BroadcastService.videoCallAcitivityOpend) {

                Contact contact = baseContactService.getContactById(message.getContactIds());
                Message statusMessage = getVideoCallStatusMessage(contact);
                statusMessage.setMessage("Call Busy");
                statusMessage.setMetadata(getRejectedCallMap());
                conversationService.sendMessage(statusMessage, MessageIntentService.class);

            }

        } else if (type.equals(CALL_MISSED)) {

            Intent intent = new Intent(CALL_MISSED);
            intent.putExtra(CALL_ID, videoCallId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        } else if (type.equals(CALL_CANCELED)) {

            Intent intent = new Intent(CALL_CANCELED);
            intent.putExtra(CALL_ID, videoCallId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        } else if (type.equals(CALL_END)) {
            Intent intent = new Intent(CALL_END);
            intent.putExtra(CALL_ID, videoCallId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

    }

    private void handleIncomingVideoNotification(Message msg) {

        String isAudioCallOnly = msg.getMetadata().get(CALL_AUDIO_ONLY);
        boolean staleNotification = System.currentTimeMillis() - msg.getCreatedAtTime() > MAX_NOTIFICATION_RING_DURATION;
        //OR SELF Connecting

        if (staleNotification || msg.isTypeOutbox()) {

            //Contact contact = baseContactService.getContactById(msg.getContactIds());
            //sendCallMissed(contact, msg.getMessage());
            Log.i(TAG, "notification not valid ignoring..");
            return;

        }

        if (BroadcastService.callRinging) {

            Contact contactDetail = baseContactService.getContactById(msg.getTo());
            VideoCallNotificationHelper helper = new VideoCallNotificationHelper(context, isAudioOnly);
            helper.sendVideoCallReject(contactDetail, videoCallId);
            return;
        }

        if (BroadcastService.videoCallAcitivityOpend) {

            Intent intent = new Intent(MobiComKitConstants.APPLOZIC_VIDEO_DIALED);
            intent.putExtra("CONTACT_ID", msg.getTo());
            intent.putExtra(CALL_ID, videoCallId);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            return;
        }

        Class activityToOpen = null;
        try {
            activityToOpen = Class.forName(NOTIFICATION_ACTIVITY_NAME);

        } catch (Exception e) {

        }
        Intent intent1 = new Intent(context, activityToOpen);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent1.putExtra("CONTACT_ID", msg.getTo());
        intent1.putExtra(CALL_ID, videoCallId);

        if (!TextUtils.isEmpty(isAudioCallOnly) && "true".equals(isAudioCallOnly)) {
            intent1.putExtra(CALL_AUDIO_ONLY, true);
        }
        context.startActivity(intent1);
        return;
    }

    public void sendVideoCallMissedMessage(Contact contactToCall, String callId) {
        Message notificationMessage = getVideoCallStatusMessage(contactToCall);
        notificationMessage.setMetadata(getMissedCallMap());
        notificationMessage.setMessage("Call Missed");
        conversationService.sendMessage(notificationMessage, MessageIntentService.class);

    }


}