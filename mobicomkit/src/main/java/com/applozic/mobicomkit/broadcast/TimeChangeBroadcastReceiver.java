package com.applozic.mobicomkit.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicommons.commons.core.utils.DateUtils;

/**
 * Created by adarsh on 28/7/15.
 */
public class TimeChangeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("TimeChange :: ", "This has been called on date change");
                long diff = DateUtils.getTimeDiffFromUtc();
                MobiComUserPreference.getInstance(context).setDeviceTimeOffset(diff);
            }
        }).start();

    }
}
