package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.feed.RegisteredUsersApiResponse;

/**
 * Created by sunil on 3/10/16.
 */
public class RegisteredUsersAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private final TaskListener taskListener;
    Context context;
    int numberOfUsersToFetch;
    UserService userService;
    long lastTimeFetched;
    String[] userIdArray;
    RegisteredUsersApiResponse registeredUsersApiResponse;
    boolean callForRegistered;
    private Exception mException;
    private Message message;
    private String messageContent;

    public RegisteredUsersAsyncTask(Context context, TaskListener listener, int numberOfUsersToFetch, Message message, String messageContent) {
        this.message = message;
        this.context = context;
        this.taskListener = listener;
        this.messageContent = messageContent;
        this.numberOfUsersToFetch = numberOfUsersToFetch;
        this.userService = UserService.getInstance(context);
    }


    public RegisteredUsersAsyncTask(Context context, TaskListener listener, int numberOfUsersToFetch, long lastTimeFetched, Message message, String messageContent, boolean callForRegistered) {
        this.callForRegistered = callForRegistered;
        this.message = message;
        this.taskListener = listener;
        this.context = context;
        this.messageContent = messageContent;
        this.numberOfUsersToFetch = numberOfUsersToFetch;
        this.lastTimeFetched = lastTimeFetched;
        this.userService = UserService.getInstance(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (callForRegistered) {
                registeredUsersApiResponse = userService.getRegisteredUsersList(lastTimeFetched, numberOfUsersToFetch);
            } else {
                userIdArray = userService.getOnlineUsers(numberOfUsersToFetch);
            }
            return registeredUsersApiResponse != null || userIdArray != null;
        } catch (Exception e) {
            e.printStackTrace();
            mException = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        if (result && this.taskListener != null) {
            this.taskListener.onSuccess(registeredUsersApiResponse, userIdArray);
        } else if (!result && this.taskListener != null) {
            this.taskListener.onFailure(registeredUsersApiResponse, userIdArray, mException);
        }
        this.taskListener.onCompletion();
    }

    public interface TaskListener {

        void onSuccess(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray);

        void onFailure(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray, Exception exception);

        void onCompletion();
    }


}
