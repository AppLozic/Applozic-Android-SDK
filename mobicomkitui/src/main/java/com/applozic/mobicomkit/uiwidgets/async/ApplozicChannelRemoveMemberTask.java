package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.uiwidgets.R;

/**
 * Created by sunil on 17/5/16.
 */
public class ApplozicChannelRemoveMemberTask extends AsyncTask<Void, Void, Boolean> {

    Context context;
    Integer channelKey;
    String userId;
    ChannelRemoveMemberListener channelRemoveMemberListener;
    ChannelService channelService;
    Exception exception;
    String removeResponse;

    public ApplozicChannelRemoveMemberTask(Context context, Integer channelKey, String userId, ChannelRemoveMemberListener channelRemoveMemberListener) {
        this.channelKey = channelKey;
        this.userId = userId;
        this.channelRemoveMemberListener = channelRemoveMemberListener;
        this.context = context;
        this.channelService = ChannelService.getInstance(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (!TextUtils.isEmpty(userId) && userId.trim().length() != 0 && channelKey != null) {
                removeResponse = channelService.removeMemberFromChannelProcess(channelKey, userId.trim());
                if (!TextUtils.isEmpty(removeResponse)) {
                    return MobiComKitConstants.SUCCESS.equals(removeResponse);
                }
            } else {
                throw new Exception(context.getString(R.string.applozic_userId_error_info_in_logs));
            }
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean resultBoolean) {
        super.onPostExecute(resultBoolean);

        if (resultBoolean && channelRemoveMemberListener != null) {
            channelRemoveMemberListener.onSuccess(removeResponse, context);
        } else if (!resultBoolean && exception != null && channelRemoveMemberListener != null) {
            channelRemoveMemberListener.onFailure(removeResponse, exception, context);
        }
    }

    public interface ChannelRemoveMemberListener {
        void onSuccess(String response, Context context);

        void onFailure(String response, Exception e, Context context);
    }
}
