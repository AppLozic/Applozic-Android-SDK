package com.applozic.mobicomkit.sample;

import android.content.Context;
import android.database.Cursor;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicommons.people.ALContactProcessor;
import com.crashlytics.android.Crashlytics;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import io.fabric.sdk.android.Fabric;

/**
 * Created by sunil on 21/3/16.
 */
public class ApplozicSampleApplication extends MultiDexApplication implements ALContactProcessor {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TAG", "processContact ApplozicSampleApplication ");

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        Fabric.with(this, new Crashlytics());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public String processContact(String contactNumber, String countryCode) {
        if (TextUtils.isEmpty(contactNumber)) {
            return "";
        }
        Phonenumber.PhoneNumber phoneNumber;
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        if (TextUtils.isEmpty(contactNumber)) {
            return "";
        }
        try {
            phoneNumber = phoneUtil.parse(contactNumber, countryCode);
            if (phoneNumber.hasCountryCode()) {
                countryCode = String.valueOf(phoneNumber.getCountryCode());
            }
            contactNumber = String.valueOf(phoneNumber.getNationalNumber());
        } catch (Exception ex) {
            try {
                contactNumber = String.valueOf(Long.parseLong(contactNumber));
            } catch (Exception e) {
                return contactNumber;
            }
        }
        return "+" + countryCode + contactNumber;
    }
}

