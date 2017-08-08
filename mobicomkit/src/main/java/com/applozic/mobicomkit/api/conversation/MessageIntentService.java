package com.applozic.mobicomkit.api.conversation;

import android.app.IntentService;
import android.content.Intent;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.schedule.ScheduleMessageService;
import com.applozic.mobicommons.json.GsonUtils;

/**
 * Created by devashish on 15/12/13.
 */
public class MessageIntentService extends IntentService {

    private static final String TAG = "MessageIntentService";
    private MessageClientService messageClientService;

    public MessageIntentService() {
        super("MessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        messageClientService = new MessageClientService(MessageIntentService.this);
        final Message message = (Message) GsonUtils.getObjectFromJson(intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT), Message.class);
        Thread thread = new Thread(new MessageSender(message));
        thread.start();
    }

    private class MessageSender implements Runnable {
        private Message message;

        public MessageSender(Message message) {
            this.message = message;
        }

        @Override
        public void run() {
            try {
                messageClientService.sendMessageToServer(message, ScheduleMessageService.class);
                messageClientService.syncPendingMessages(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
