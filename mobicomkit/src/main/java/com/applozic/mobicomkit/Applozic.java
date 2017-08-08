package com.applozic.mobicomkit;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sunil on 29/8/16.
 */
public class Applozic {

    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    private static final String DEVICE_REGISTRATION_ID = "DEVICE_REGISTRATION_ID";
    private static final String MY_PREFERENCE = "applozic_preference_key";
    public static Applozic applozic;
    public SharedPreferences sharedPreferences;
    private Context context;

    private Applozic(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(MY_PREFERENCE, context.MODE_PRIVATE);
    }

    public static Applozic init(Context context, String applicationKey) {
        applozic = getInstance(context);
        applozic.setApplicationKey(applicationKey);
        return applozic;
    }

    public static Applozic getInstance(Context context) {
        if (applozic == null) {
            applozic = new Applozic(context.getApplicationContext());
        }
        return applozic;
    }

    public String getApplicationKey() {
        return sharedPreferences.getString(APPLICATION_KEY, null);
    }

    public Applozic setApplicationKey(String applicationKey) {
        sharedPreferences.edit().putString(APPLICATION_KEY, applicationKey).commit();
        return this;
    }

    public String getDeviceRegistrationId() {
        return sharedPreferences.getString(DEVICE_REGISTRATION_ID, null);
    }

    public Applozic setDeviceRegistrationId(String registrationId) {
        sharedPreferences.edit().putString(DEVICE_REGISTRATION_ID, registrationId).commit();
        return this;
    }

}
