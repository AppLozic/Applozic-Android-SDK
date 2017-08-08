package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.feed.ApiResponse;

public class UserLogoutTask extends AsyncTask<Void, Void, Boolean> {

    private final TaskListener taskListener;
    private final Context context;
    UserClientService userClientService;
    private Exception mException;

    public UserLogoutTask(TaskListener listener, Context context) {
        this.taskListener = listener;
        this.context = context;
        userClientService = new UserClientService(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        ApiResponse apiResponse = null;
        try {
            apiResponse = userClientService.logout();
            return apiResponse != null && apiResponse.isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        // And if it is we call the callback function on it.
        if (result && this.taskListener != null) {
            this.taskListener.onSuccess(context);

        } else if (mException != null && this.taskListener != null) {
            this.taskListener.onFailure(mException);
        }
    }

    public interface TaskListener {
        void onSuccess(Context context);

        void onFailure(Exception exception);
    }
}
