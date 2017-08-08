package com.applozic.mobicomkit.uiwidgets.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.uiwidgets.R;

/**
 * Created by sunil on 17/5/16.
 */
public class ApplozicChannelLeaveMember extends AsyncTask<Void, Void, Boolean> {

    Context context;
    String clientGroupId;
    Integer channelKey;
    String userId;
    ChannelLeaveMemberListener channelLeaveMemberListener;
    ChannelService channelService;
    Exception exception;
    String leaveResponse;
    ProgressDialog progressDialog;
    boolean enableProgressDialog;

    public ApplozicChannelLeaveMember(Context context, Integer channelKey, String userId, ChannelLeaveMemberListener channelLeaveMemberListener) {
        this.channelKey = channelKey;
        this.userId = userId;
        this.channelLeaveMemberListener = channelLeaveMemberListener;
        this.context = context;
        this.channelService = ChannelService.getInstance(context);
    }

    public boolean isEnableProgressDialog() {
        return enableProgressDialog;
    }

    public void setEnableProgressDialog(boolean enableProgressDialog) {
        this.enableProgressDialog = enableProgressDialog;
    }

    public String getClientGroupId() {
        return clientGroupId;
    }

    public void setClientGroupId(String clientGroupId) {
        this.clientGroupId = clientGroupId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (enableProgressDialog) {
            progressDialog = ProgressDialog.show(context, "",
                    context.getString(R.string.channel_member_exit), true);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (!TextUtils.isEmpty(userId) && userId.trim().length() != 0) {
                if (channelKey != null && channelKey != 0) {
                    leaveResponse = channelService.leaveMemberFromChannelProcess(channelKey, userId.trim());
                } else if (!TextUtils.isEmpty(clientGroupId)) {
                    leaveResponse = channelService.leaveMemberFromChannelProcess(clientGroupId, userId.trim());
                }
                if (!TextUtils.isEmpty(leaveResponse)) {
                    return MobiComKitConstants.SUCCESS.equals(leaveResponse);
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
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (resultBoolean && channelLeaveMemberListener != null) {
            channelLeaveMemberListener.onSuccess(leaveResponse, context);
        } else if (!resultBoolean && exception != null && channelLeaveMemberListener != null) {
            channelLeaveMemberListener.onFailure(leaveResponse, exception, context);
        }
    }

    public interface ChannelLeaveMemberListener {
        void onSuccess(String response, Context context);

        void onFailure(String response, Exception e, Context context);
    }
}
