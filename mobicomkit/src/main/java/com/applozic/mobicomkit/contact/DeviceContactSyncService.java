package com.applozic.mobicomkit.contact;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicommons.people.ALContactProcessor;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by devashish on 02/06/16.
 */
public class DeviceContactSyncService extends JobIntentService {

    private static final String TAG = "DvcContactSync";
    public static final String PROCESS_USER_DETAILS = "PROCESS_USER_DETAILS";
    public static final String PROCESS_MODIFIED_DEVICE_CONTACTS = "PROCESS_MODIFIED_DEVICE_CONTACTS";

    static final int JOB_ID = 2000;


    static public void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DeviceContactSyncService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent == null) {
            return;
        }
        Thread thread = new Thread(new DeviceContactSync(intent.getBooleanExtra(PROCESS_USER_DETAILS, false), intent.getBooleanExtra(PROCESS_MODIFIED_DEVICE_CONTACTS, false)));
        thread.start();
    }



    private class DeviceContactSync implements Runnable {

        private boolean processUserDetails;
        private boolean processModifiedContacts;

        public DeviceContactSync(boolean processUserDetails, boolean processModifiedContacts) {
            this.processUserDetails = processUserDetails;
            this.processModifiedContacts = processModifiedContacts;
        }

        @Override
        public void run() {
            try {
                if (processModifiedContacts) {
                    processModifiedContacts();
                } else {
                    process(processUserDetails);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void process(boolean processUserDetails) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY};
        Cursor people = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
        AppContactService appContactService = new AppContactService(this);
        Log.i(TAG, "FoundPeople " + people);
        Log.i(TAG, "Found " + people.getCount() + " device contacts");

        DeviceContactService deviceContactService = new DeviceContactService(getApplicationContext());
        Set<String> userIdList = new HashSet<>();

        while (people.moveToNext()) {
            Contact contact = deviceContactService.getContactFromContactCursor(people);
            if (contact == null) {
                continue;
            }
            appContactService.upsert(contact);
            userIdList.add(contact.getFormattedContactNumber());
        }

        if (processUserDetails && !userIdList.isEmpty()) {
            UserService userService = UserService.getInstance(DeviceContactSyncService.this);
            userService.processUserDetailsByContactNos(userIdList);
        }

        if (processUserDetails) {
            MobiComUserPreference.getInstance(DeviceContactSyncService.this).setDeviceContactSyncTime(new Date().getTime());
        }
        if (people != null) {
            people.close();
        }
    }

    private void processModifiedContacts() {
        new DeviceContactService(DeviceContactSyncService.this).processModifiedContacts();
    }

}
