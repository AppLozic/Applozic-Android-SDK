package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.uiwidgets.R;

import java.util.Set;

/**
 * Created by sunil on 31/5/16.
 */
public class ApplozicAddMemberToMultipleChannels extends AsyncTask<Void, Void, Boolean> {


    Context context;
    Set<String> clientGroupIds;
    Set<Integer> channelKeys;
    String userId;
    AddMemberToMultipleChannels addMemberToMultipleChannels;
    ChannelService channelService;
    Exception exception;
    String addResponse;

    public ApplozicAddMemberToMultipleChannels(Context context, Set<Integer> channelKeys, String userId, AddMemberToMultipleChannels addMemberToMultipleChannels) {
        this.addMemberToMultipleChannels = addMemberToMultipleChannels;
        this.userId = userId;
        this.channelKeys = channelKeys;
        this.context = context;
        this.channelService = ChannelService.getInstance(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (!TextUtils.isEmpty(userId) && userId.trim().length() != 0) {
                if (channelKeys != null && channelKeys.size() > 0) {
                    addResponse = channelService.addMemberToMultipleChannelsProcessByChannelKeys(channelKeys, userId.trim());
                } else if (clientGroupIds != null && clientGroupIds.size() > 0) {
                    addResponse = channelService.addMemberToMultipleChannelsProcess(clientGroupIds, userId.trim());
                }
                if (!TextUtils.isEmpty(addResponse)) {
                    return MobiComKitConstants.SUCCESS.equals(addResponse);
                }
            } else {
                throw new Exception(context.getString(R.string.applozic_add_user_to_multiple_channel_error_info_in_logs));

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

        if (resultBoolean && addMemberToMultipleChannels != null) {
            addMemberToMultipleChannels.onSuccess(addResponse, context);
        } else if (!resultBoolean  && addMemberToMultipleChannels != null) {
            addMemberToMultipleChannels.onFailure(addResponse, exception, context);
        }
    }

    public interface AddMemberToMultipleChannels {
        void onSuccess(String response, Context context);

        void onFailure(String response, Exception e, Context context);
    }
}
