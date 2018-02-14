package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by sunil on 17/5/16.
 */
public class ApplozicUserUpdateTask extends AsyncTask<Void, Void, Boolean> {

    Context context;
    Contact contact;
    UserUpdateListener userUpdateListener;
    String updateUserResponse;
    UserService userService;
    Exception exception;

    public ApplozicUserUpdateTask(Context context, Contact contact, UserUpdateListener userUpdateListener) {
        this.context = context;
        this.contact = contact;
        this.userUpdateListener = userUpdateListener;
        this.userService = UserService.getInstance(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (contact != null) {
                updateUserResponse = userService.updateUserDetails(contact);
            }
            if (!TextUtils.isEmpty(updateUserResponse)) {
                return MobiComKitConstants.SUCCESS.equals(updateUserResponse);
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
        if (resultBoolean && userUpdateListener != null) {
            userUpdateListener.onSuccess(updateUserResponse, context);
        } else if (!resultBoolean && userUpdateListener != null) {
            userUpdateListener.onFailure(updateUserResponse, exception, context);
        }
    }

    public interface UserUpdateListener {
        void onSuccess(String response, Context context);

        void onFailure(String response, Exception e, Context context);
    }
}
