package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicommons.people.channel.Channel;

/**
 * Created by ninu on 02/08/17.
 */

public class AlGroupInformationAsyncTask extends AsyncTask<Object, Object, Channel> {
    Context context;
    ChannelService channelService;
    GroupMemberListener groupMemberListener;
    private Exception exception;
    private String clientGroupId;
    private Integer channelKey;

    public AlGroupInformationAsyncTask(Context context, String clientGroupId, GroupMemberListener groupMemberListener) {
        this.context = context;
        this.channelService = ChannelService.getInstance(context);
        this.clientGroupId = clientGroupId;
        this.groupMemberListener = groupMemberListener;
    }

    public AlGroupInformationAsyncTask(Context context, Integer channelKey, GroupMemberListener groupMemberListener) {
        this.context = context;
        this.channelService = ChannelService.getInstance(context);
        this.channelKey = channelKey;
        this.groupMemberListener = groupMemberListener;
    }

    @Override
    protected Channel doInBackground(Object... params) {
        try {
            if (!TextUtils.isEmpty(clientGroupId)) {
                return channelService.getChannelInfo(clientGroupId);
            } else if (channelKey != null) {
                return channelService.getChannelInfo(channelKey);
            }
        } catch (Exception e) {
            exception = e;
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Channel channel) {
        super.onPostExecute(channel);
        if (channel != null && groupMemberListener != null) {
            groupMemberListener.onSuccess(channel, context);
        } else if (channel == null && groupMemberListener != null) {
            groupMemberListener.onFailure(channel, exception, context);
        }
    }

    public interface GroupMemberListener {
        void onSuccess(Channel channel, Context context);

        void onFailure(Channel channel, Exception e, Context context);
    }
}