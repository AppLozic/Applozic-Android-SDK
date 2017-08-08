package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;

/**
 * Created by devashish on 7/22/2015.
 */
public class PushNotificationTask extends AsyncTask<Void, Void, Boolean> {

    private final String pushNotificationId;
    private final TaskListener taskListener;
    private final Context context;
    private Exception mException;
    private RegistrationResponse registrationResponse;

    public PushNotificationTask(String pushNotificationId, TaskListener listener, Context context) {
        this.pushNotificationId = pushNotificationId;
        this.taskListener = listener;
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            registrationResponse = new RegisterUserClientService(context).updatePushNotificationId(pushNotificationId);
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        // And if it is we call the callback function on it.
        if (result && this.taskListener != null) {
            this.taskListener.onSuccess(registrationResponse);

        } else if (mException != null && this.taskListener != null) {
            this.taskListener.onFailure(registrationResponse, mException);
        }
    }

    public interface TaskListener {

        void onSuccess(RegistrationResponse registrationResponse);

        void onFailure(RegistrationResponse registrationResponse, Exception exception);

    }
}
