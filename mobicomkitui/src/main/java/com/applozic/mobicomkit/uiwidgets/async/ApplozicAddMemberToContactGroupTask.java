package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.channel.service.ChannelService;

import java.util.List;

/**
 * Created by Rahul-PC on 06-06-2017.
 */

public class ApplozicAddMemberToContactGroupTask extends AsyncTask<Object, Object, Boolean> {
    String contactGroupId;
    Context context;
    List<String> groupMemberList;
    ChannelService channelService;
    GroupMemberListener groupMemberListener;
    private Exception exception;
    private String groupType;


    public ApplozicAddMemberToContactGroupTask(Context context, String contactGroupId, String groupType, List<String> groupMemberList, GroupMemberListener groupMemberListener) {
        this.context = context;
        this.contactGroupId = contactGroupId;
        this.groupMemberList = groupMemberList;
        channelService = ChannelService.getInstance(context);
        this.groupMemberListener = groupMemberListener;
        this.groupType = groupType;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            if (contactGroupId != null && groupMemberList != null) {
                boolean response = channelService.addMemberToContactGroup(contactGroupId, groupType, groupMemberList);
                if (response)
                    return true;
            }
        } catch (Exception e) {
            exception = e;
            return false;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (result && groupMemberListener != null) {
            groupMemberListener.onSuccess(result, context);
        } else if (!result && exception != null && groupMemberListener != null) {
            groupMemberListener.onFailure(result, exception, context);
        }
    }

    public interface GroupMemberListener {
        void onSuccess(boolean response, Context context);

        void onFailure(boolean response, Exception e, Context context);
    }
}