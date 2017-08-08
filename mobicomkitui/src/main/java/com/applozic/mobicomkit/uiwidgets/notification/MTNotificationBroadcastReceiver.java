package com.applozic.mobicomkit.uiwidgets.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

/**
 * Created by adarsh on 3/5/15.
 */
public class MTNotificationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MTBroadcastReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        Utils.printLog(context,TAG, "Received broadcast, action: " + action + ", message: " + messageJson);
        if (!TextUtils.isEmpty(messageJson)) {
            final Message message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
            Intent notificationIntentService = new Intent(context, NotificationIntentService.class);
            notificationIntentService.setAction(NotificationIntentService.ACTION_AL_NOTIFICATION);
            notificationIntentService.putExtra(MobiComKitConstants.AL_MESSAGE, message);
            context.startService(notificationIntentService);
        }
    }
}
