package com.applozic.mobicomkit.uiwidgets.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.uiwidgets.R;

/**
 * Created by Rahul-PC on 07-06-2017.
 */

public class ApplozicGetMemberFromContactGroupTask extends AsyncTask<Void, Void, String[]> {
    private final String contactGroupId;
    Context context;
    GroupMemberListener groupMemberListener;
    ProgressDialog dialog;
    private ChannelService channelService;
    private String groupType;

    public ApplozicGetMemberFromContactGroupTask(Context context, String contactGroupId, String groupType, GroupMemberListener groupMemberListener) {
        this.context = context;
        this.contactGroupId = contactGroupId;
        channelService = ChannelService.getInstance(context);
        this.groupType = groupType;
        this.groupMemberListener = groupMemberListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getResources().getString(R.string.processing_please_wait));
        dialog.show();

    }

    @Override
    protected String[] doInBackground(Void[] params) {
        try {
            if (contactGroupId != null) {
                ChannelFeed channelFeed = channelService.getMembersFromContactGroup(contactGroupId, groupType);
                if (channelFeed != null && channelFeed.getContactGroupMembersId() != null) {

                    String userIdArray[] = channelFeed.getContactGroupMembersId().toArray(new String[channelFeed.getContactGroupMembersId().size()]);
                    for (int i = 0; i < userIdArray.length; i++) {
                        if (MobiComUserPreference.getInstance(context).getUserId().equals(userIdArray[i])) {
                            userIdArray[i] = "";
                        }
                    }
                    return userIdArray;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] userIdArray) {
        super.onPostExecute(userIdArray);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if (userIdArray != null) {
            groupMemberListener.onSuccess(userIdArray, context);
        } else {
            groupMemberListener.onFailure(context.getString(R.string.error), context);
        }
    }


    public interface GroupMemberListener {
        void onSuccess(String[] userIdArray, Context context);

        void onFailure(String userIdArray, Context context);
    }
}