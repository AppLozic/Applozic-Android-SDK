package com.applozic.mobicomkit.uiwidgets.uikit;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class WishTripBridge {

    public static final String ACTION_VIEW_PROFILE = "action_view_profile";
    public static final String EXTRA_APPLOZIC_USER_ID = "extra_applozic_user_id";

    public static void openProfileActivity(Context context, long userId) {
        Intent intent = new Intent();
        intent.setAction(ACTION_VIEW_PROFILE);
        intent.putExtra(EXTRA_APPLOZIC_USER_ID, userId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
