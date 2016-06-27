package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.os.AsyncTask;


public class UserLogoutTask extends AsyncTask<Void,Void,Boolean> {

    public interface TaskListener{
        void onSuccess(Context context);
        void onFailure(Exception exception);
    }

    private final TaskListener taskListener;
    private final Context context;
    private Exception mException;



    public UserLogoutTask(TaskListener listener, Context context) {

        this.taskListener = listener;
        this.context = context;

    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            new UserClientService(context).logout();
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
            this.taskListener.onSuccess(context);

        } else if (mException != null && this.taskListener != null) {
            this.taskListener.onFailure(mException);
        }
    }
}
