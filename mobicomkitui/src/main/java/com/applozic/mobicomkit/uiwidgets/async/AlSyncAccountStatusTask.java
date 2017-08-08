package com.applozic.mobicomkit.uiwidgets.async;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;


/**
 * Created by sunil on 19/12/2016.
 */

public class AlSyncAccountStatusTask extends AsyncTask<Void, Void, Boolean> {
    Context context;
    RegisterUserClientService registerUserClientService;
    TaskListener taskListener;
    String loggedInUserId;

    public AlSyncAccountStatusTask(Context context, TaskListener taskListener) {
        this.context = context;
        this.taskListener = taskListener;
        this.registerUserClientService = new RegisterUserClientService(context);
        this.loggedInUserId = MobiComUserPreference.getInstance(context).getUserId();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        User user = new User();
        user.setUserId(loggedInUserId);
        try {
            registerUserClientService.updateRegisteredAccount(user);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if (taskListener != null) {
            taskListener.onCompletion(context);
        }
    }

    public interface TaskListener {
        void onCompletion(Context context);
    }
}
