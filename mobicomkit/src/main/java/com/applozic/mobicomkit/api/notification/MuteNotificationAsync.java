package com.applozic.mobicomkit.api.notification;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.ApiResponse;

/**
 * Created by Adarsh on 12/30/16.
 */

public class MuteNotificationAsync extends AsyncTask<Void, Void, Boolean> {

    private final MuteNotificationAsync.TaskListener taskListener;
    private final Context context;
    private ApiResponse apiResponse;
    private Exception mException;
    private MuteNotificationRequest muteNotificationRequest;

    public MuteNotificationAsync(Context context, MuteNotificationAsync.TaskListener listener, MuteNotificationRequest request) {
        this.context = context;
        this.taskListener = listener;
        this.muteNotificationRequest = request;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            apiResponse = ChannelService.getInstance(context).muteNotifications(muteNotificationRequest);
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (result && this.taskListener != null) {
            this.taskListener.onSuccess(apiResponse);
        } else if (mException != null && this.taskListener != null) {
            this.taskListener.onFailure(apiResponse, mException);
        }
        this.taskListener.onCompletion();
    }

    public interface TaskListener {

        void onSuccess(ApiResponse apiResponse);

        void onFailure(ApiResponse apiResponse, Exception exception);

        void onCompletion();
    }


}
