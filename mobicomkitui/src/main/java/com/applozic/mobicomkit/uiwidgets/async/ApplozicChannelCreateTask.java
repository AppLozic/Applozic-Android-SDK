package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicommons.people.channel.Channel;

import java.util.List;

/**
 * Created by sunil on 17/5/16.
 */
public class ApplozicChannelCreateTask extends AsyncTask<Void, Void, Boolean> {
    Context context;
    String groupName;
    List<String> groupMemberList;
    ChannelService channelService;
    Channel channel;
    ChannelInfo channelInfo;
    Exception exception;
    ChannelCreateListener channelCreateListener;
    String groupImageLink;
    String clientGroupId;
    int type = Channel.GroupType.PUBLIC.getValue().intValue();


    public ApplozicChannelCreateTask(Context context, ChannelCreateListener channelCreateListener, String groupName, List<String> groupMemberList, String groupImageLink) {
        this.context = context;
        this.groupName = groupName;
        this.groupMemberList = groupMemberList;
        this.groupImageLink = groupImageLink;
        this.channelCreateListener = channelCreateListener;
        this.channelService = ChannelService.getInstance(context);
    }

    public String getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (!TextUtils.isEmpty(groupName) && groupName.trim().length() != 0 && groupMemberList != null && groupMemberList.size() > 0) {
                channelInfo = new ChannelInfo(groupName.trim(), groupMemberList, groupImageLink);
                if (!TextUtils.isEmpty(clientGroupId)) {
                    channelInfo.setClientGroupId(clientGroupId);
                }
                channelInfo.setType(type);
                channel = channelService.createChannel(channelInfo);
                return channel != null;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean resultBoolean) {
        super.onPostExecute(resultBoolean);

        if (resultBoolean && channel != null && channelCreateListener != null) {
            channelCreateListener.onSuccess(channel, context);
        } else if (!resultBoolean && channelCreateListener != null) {
            channelCreateListener.onFailure(exception, context);
        }

    }

    public interface ChannelCreateListener {
        void onSuccess(Channel channel, Context context);

        void onFailure(Exception e, Context context);
    }
}