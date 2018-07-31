package com.applozic.mobicomkit.api.account.user;

/**
 * Created by Aman on 7/12/2015.
 */

import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.account.register.RegisterUserClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.listners.AlLoginHandler;

import java.lang.ref.WeakReference;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

    private TaskListener taskListener;
    private final WeakReference<Context> context;
    private User user;
    private Exception mException;
    private RegistrationResponse registrationResponse;
    private UserClientService userClientService;
    private RegisterUserClientService registerUserClientService;
    private UserService userService;
    private AlLoginHandler loginHandler;

    public UserLoginTask(User user, TaskListener listener, Context context) {
        this.taskListener = listener;
        this.context = new WeakReference<Context>(context);
        this.user = user;
        this.userClientService = new UserClientService(context);
        this.registerUserClientService = new RegisterUserClientService(context);
        this.userService = UserService.getInstance(context);
    }

    public UserLoginTask(User user, AlLoginHandler listener, Context context) {
        this.loginHandler = listener;
        this.context = new WeakReference<Context>(context);
        this.user = user;
        this.userClientService = new UserClientService(context);
        this.registerUserClientService = new RegisterUserClientService(context);
        this.userService = UserService.getInstance(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            userClientService.clearDataAndPreference();
            registrationResponse = registerUserClientService.createAccount(user);
            userService.processPackageDetail();
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
            this.taskListener.onSuccess(registrationResponse, context.get());
        } else if (mException != null && this.taskListener != null) {
            this.taskListener.onFailure(registrationResponse, mException);
        } else if (result && this.loginHandler != null) {
            this.loginHandler.onSuccess(registrationResponse, context.get());
        } else if (mException != null && this.loginHandler != null) {
            this.loginHandler.onFailure(registrationResponse, mException);
        }
    }

    public interface TaskListener {
        void onSuccess(RegistrationResponse registrationResponse, Context context);

        void onFailure(RegistrationResponse registrationResponse, Exception exception);

    }


}