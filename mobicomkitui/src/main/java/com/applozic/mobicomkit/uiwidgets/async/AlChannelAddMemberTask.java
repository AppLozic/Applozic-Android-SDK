package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ErrorResponseFeed;
import com.applozic.mobicomkit.uiwidgets.R;

import java.util.List;

/**
 * Created by ninu on 13/07/17.
 */

public class AlChannelAddMemberTask extends AsyncTask<Void, Void, Boolean> {
    Context context;
    Integer channelKey;
    String userId;
    ChannelAddMemberListener channelAddMemberListener;
    ChannelService channelService;
    Exception exception;
    String addResponse;
    String clientGroupId;
    ApiResponse apiResponse;

    public AlChannelAddMemberTask(Context context, Integer channelKey, String userId, ChannelAddMemberListener channelAddMemberListener) {
        this.channelKey = channelKey;
        this.userId = userId;
        this.context = context;
        this.channelAddMemberListener = channelAddMemberListener;
        this.channelService = ChannelService.getInstance(context);
    }

    public AlChannelAddMemberTask(Context context, String clientGroupId, String userId, ChannelAddMemberListener channelAddMemberListener) {
        this.clientGroupId = clientGroupId;
        this.userId = userId;
        this.channelAddMemberListener = channelAddMemberListener;
        this.context = context;
        this.channelService = ChannelService.getInstance(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (!TextUtils.isEmpty(userId) && userId.trim().length() != 0 && (channelKey != null && channelKey != 0 || !TextUtils.isEmpty(clientGroupId))) {
                if (channelKey != null) {
                    apiResponse = channelService.addMemberToChannelProcessWithResponse(channelKey, userId.trim());
                } else if (!TextUtils.isEmpty(clientGroupId)) {
                    apiResponse = channelService.addMemberToChannelProcessWithResponse(clientGroupId, userId.trim());
                }
                if (apiResponse != null) {
                    return apiResponse.isSuccess();
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
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result && channelAddMemberListener != null) {
            channelAddMemberListener.onSuccess(apiResponse.getStatus(), context);
        } else if (!result && channelAddMemberListener != null) {
            if (apiResponse != null) {
                channelAddMemberListener.onFailure(apiResponse.getStatus(), exception, context, apiResponse.getErrorResponse());
            } else {
                channelAddMemberListener.onFailure(addResponse, exception, context, null);
            }
        }
    }

    public interface ChannelAddMemberListener {
        void onSuccess(String response, Context context);

        void onFailure(String response, Exception e, Context context, List<ErrorResponseFeed> errorResponseFeeds);
    }

}


