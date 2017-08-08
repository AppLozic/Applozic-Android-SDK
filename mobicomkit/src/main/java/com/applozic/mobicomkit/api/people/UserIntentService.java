package com.applozic.mobicomkit.api.people;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.SyncCallService;

/**
 * Created by devashish on 15/12/13.
 */
public class UserIntentService extends IntentService {

    public static final String USER_ID = "userId";
    public static final String USER_LAST_SEEN_AT_STATUS = "USER_LAST_SEEN_AT_STATUS";
    private static final String TAG = "UserIntentService";

    public UserIntentService() {
        super("UserIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String userId = intent.getStringExtra(USER_ID);
        if (!TextUtils.isEmpty(userId)) {
            SyncCallService.getInstance(UserIntentService.this).processUserStatus(userId);
        } else if (intent.getBooleanExtra(USER_LAST_SEEN_AT_STATUS, false)) {
            new MobiComConversationService(UserIntentService.this).processLastSeenAtStatus();
        }
    }

}
