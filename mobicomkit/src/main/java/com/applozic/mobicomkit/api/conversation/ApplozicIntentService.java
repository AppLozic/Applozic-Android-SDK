package com.applozic.mobicomkit.api.conversation;

import android.app.IntentService;
import android.content.Intent;

import com.applozic.mobicomkit.api.account.user.UserService;

/**
 * Created by sunil on 26/12/15.
 */
public class ApplozicIntentService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public static final String CONTACT = "contact";
    public static final String CHANNEL = "channel";
    public static final String AL_SYNC_ON_CONNECTIVITY = "AL_SYNC_ON_CONNECTIVITY";
    private static final String TAG = "ApplozicIntentService";
    MobiComConversationService conversationService;
    private MessageClientService messageClientService;

    public ApplozicIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.messageClientService = new MessageClientService(this);
        this.conversationService = new MobiComConversationService(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        boolean connectivityChange = intent.getBooleanExtra(AL_SYNC_ON_CONNECTIVITY, false);
        if (connectivityChange) {
            SyncCallService.getInstance(ApplozicIntentService.this).syncMessages(null);
            messageClientService.syncPendingMessages(true);
            messageClientService.syncDeleteMessages(true);
            conversationService.processLastSeenAtStatus();
            UserService.getInstance(ApplozicIntentService.this).processSyncUserBlock();
        }
    }
}

