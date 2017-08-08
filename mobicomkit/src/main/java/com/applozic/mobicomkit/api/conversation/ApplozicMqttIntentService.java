package com.applozic.mobicomkit.api.conversation;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.ApplozicMqttService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by sunil on 30/12/15.
 */
public class ApplozicMqttIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public static final String TAG = "ApplozicMqttIntentService";
    public static final String SUBSCRIBE = "subscribe";
    public static final String SUBSCRIBE_TO_TYPING = "subscribeToTyping";
    public static final String UN_SUBSCRIBE_TO_TYPING = "unSubscribeToTyping";
    public static final String DEVICE_KEY_STRING = "deviceKeyString";
    public static final String USER_KEY_STRING = "userKeyString";
    public static final String CONNECTED_PUBLISH = "connectedPublish";
    public static final String CONTACT = "contact";
    public static final String CHANNEL = "channel";
    public static final String TYPING = "typing";
    public static final String STOP_TYPING = "STOP_TYPING";

    public ApplozicMqttIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        boolean subscribe = intent.getBooleanExtra(SUBSCRIBE, false);
        if (subscribe) {
            ApplozicMqttService.getInstance(getApplicationContext()).subscribe();
        }
        Contact contact = (Contact) intent.getSerializableExtra(CONTACT);
        Channel channel = (Channel) intent.getSerializableExtra(CHANNEL);

        boolean subscribeToTyping = intent.getBooleanExtra(SUBSCRIBE_TO_TYPING, false);
        if (subscribeToTyping) {
            ApplozicMqttService.getInstance(getApplicationContext()).subscribeToTypingTopic(channel);
            return;
        }
        boolean unSubscribeToTyping = intent.getBooleanExtra(UN_SUBSCRIBE_TO_TYPING, false);
        if (unSubscribeToTyping) {
            ApplozicMqttService.getInstance(getApplicationContext()).unSubscribeToTypingTopic(channel);
            return;
        }
        String userKeyString = intent.getStringExtra(USER_KEY_STRING);
        String deviceKeyString = intent.getStringExtra(DEVICE_KEY_STRING);
        if (!TextUtils.isEmpty(userKeyString) && !TextUtils.isEmpty(deviceKeyString)) {
            ApplozicMqttService.getInstance(getApplicationContext()).disconnectPublish(userKeyString, deviceKeyString, "0");
        }

        boolean connectedStatus = intent.getBooleanExtra(CONNECTED_PUBLISH, false);
        if (connectedStatus) {
            ApplozicMqttService.getInstance(getApplicationContext()).connectPublish(MobiComUserPreference.getInstance(getApplicationContext()).getSuUserKeyString(), MobiComUserPreference.getInstance(getApplicationContext()).getDeviceKeyString(), "1");
        }

        if (contact != null) {
            boolean stop = intent.getBooleanExtra(STOP_TYPING, false);
            if (stop) {
                ApplozicMqttService.getInstance(getApplicationContext()).typingStopped(contact, null);
            }
        }

        if (contact != null && (contact.isBlocked() || contact.isBlockedBy())) {
            return;
        }

        if (contact != null || channel != null) {
            boolean typing = intent.getBooleanExtra(TYPING, false);
            if (typing) {
                ApplozicMqttService.getInstance(getApplicationContext()).typingStarted(contact, channel);
            } else {
                ApplozicMqttService.getInstance(getApplicationContext()).typingStopped(contact, channel);
            }
        }

    }
}
