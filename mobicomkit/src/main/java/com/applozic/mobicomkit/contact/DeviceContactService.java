package com.applozic.mobicomkit.contact;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.ALContactProcessor;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.applozic.mobicommons.people.contact.ContactUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by adarsh on 7/7/15.
 */
public class DeviceContactService implements BaseContactService {

    private Context context;

    public DeviceContactService(Context context) {
        this.context = context;
    }

    @Override
    public void add(Contact contact) {

    }

    @Override
    public void addAll(List<Contact> contactList) {

    }

    @Override
    public void deleteContact(Contact contact) {

    }

    @Override
    public void deleteContactById(String contactId) {

    }

    @Override
    public List<Contact> getAll() {
        return null;
    }

    @Override
    public Contact getContactById(String contactId) {
        Contact contact = ContactUtils.getContact(context, contactId);
        if (contact != null) {
            contact.processContactNumbers(context);
        }
        return contact;
    }

    @Override
    public void updateContact(Contact contact) {

    }

    @Override
    public void upsert(Contact contact) {

    }

    @Override
    public List<Contact> getAllContactListExcludingLoggedInUser() {
        return null;
    }

    @Override
    public Bitmap downloadContactImage(Context context, Contact contact) {
        return null;
    }

    @Override
    public Bitmap downloadGroupImage(Context context, Channel channel) {
        return null;
    }

    @Override
    public Contact getContactReceiver(List<String> items, List<String> userIds) {
        if (items != null && !items.isEmpty()) {
            return ContactUtils.getContact(context, items.get(0));
        }

        return null;
    }

    @Override
    public boolean isContactExists(String contactId) {
        //Todo: write implementation for device contacts
        return false;
    }

    @Override
    public void updateConnectedStatus(String contactId, Date date, boolean connected) {

    }

    @Override
    public void updateUserBlocked(String userId, boolean userBlocked) {

    }

    @Override
    public void updateUserBlockedBy(String userId, boolean userBlockedBy) {

    }

    @Override
    public boolean isContactPresent(String userId) {
        return false;
    }

    @Override
    public int getChatConversationCount() {
        return 0;
    }

    @Override
    public int getGroupConversationCount() {
        return 0;
    }

    @Override
    public void updateLocalImageUri(Contact contact) {

    }

    @Override
    public List<Contact> getContacts(Contact.ContactType contactType) {
        return null;
    }

    public Contact getContactFromContactCursor(Cursor people) {
        String contactNO = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        String displayName = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        Log.i("TAG","displayName" +displayName);
        String lookupKey = "lkupkey-" + people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY));


        if (!TextUtils.isEmpty(contactNO) && contactNO.trim().length() > 8) {
            contactNO = contactNO.trim().replace(" ", "").replace("-", "");
            contactNO = contactNO.replaceFirst("^0+(?!$)", "");
        } else {
            return null;
        }

        String formattedPhoneNumber = contactNO;

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getSimCountryIso().toUpperCase();
        try {
            if (context.getApplicationContext() instanceof ALContactProcessor) {
                formattedPhoneNumber = ((ALContactProcessor) context.getApplicationContext()).processContact(contactNO, countryCode);
                Log.i("TAG","displayName" +displayName);

            }
        } catch (ClassCastException e) {

        }

        ContactDatabase contactDatabase = new ContactDatabase(context);
        Contact contact = contactDatabase.getContactByPhoneNo(formattedPhoneNumber);
        Contact contactByLookupKey = contactDatabase.getContactById(lookupKey);

        if (contactDatabase.isContactPresent(formattedPhoneNumber, Contact.ContactType.DEVICE_AND_APPLOZIC)) {
            if (!displayName.equals(contact.getPhoneDisplayName())) {
                contactDatabase.updatePhoneContactDisplayName(formattedPhoneNumber, displayName, Contact.ContactType.DEVICE_AND_APPLOZIC.getValue());
            }
            return null;
        }

        if (contact != null) {
            //Log.d(TAG, "Contact is present with the same phone number: " + formattedPhoneNumber);
            lookupKey = contact.getUserId();
        } else if (contactByLookupKey != null) {
            //Log.d(TAG, "Contact is present with the same lookupkey: " + lookupKey);
            lookupKey = lookupKey + "-" + formattedPhoneNumber;
        }

        Contact newContact = new Contact();
        newContact.setContactNumber(formattedPhoneNumber);
        newContact.setUserId(lookupKey);
        newContact.setDeviceContactType(Contact.ContactType.DEVICE.getValue());
        newContact.setFullName(displayName);
        newContact.setPhoneDisplayName(displayName);
        newContact.processContactNumbers(context);
        return newContact;
    }

    public List<String> getModifiedContacts() {
        List<String> contactIdList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts.CONTACT_ID}, ContactsContract.RawContacts.DIRTY + "=1", null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (!TextUtils.isEmpty(cursor.getString(0))) {
                    contactIdList.add(cursor.getString(0));
                }
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        Utils.printLog(context, "DeviceContactService", "Total modified contacts to sync : " + contactIdList.size());
        return contactIdList;
    }

    public void processModifiedContacts() {
        try {
            Set<String> contactNumberList = new HashSet<String>();
            AppContactService appContactService = new AppContactService(context);
            for (String contactId : getModifiedContacts()) {
                Cursor cursorPhone = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                        new String[]{contactId},
                        null);

                if (cursorPhone != null && cursorPhone.getCount() > 0) {
                    while (cursorPhone.moveToNext()) {
                        Contact contact = getContactFromContactCursor(cursorPhone);
                        if (contact == null) {
                            continue;
                        }
                        appContactService.upsert(contact);
                        contactNumberList.add(contact.getFormattedContactNumber());
                    }
                }
                if (cursorPhone != null) {
                    cursorPhone.close();
                }
            }

            if (contactNumberList != null && contactNumberList.size() > 0) {
                UserService.getInstance(context).processUserDetailsByContactNos(contactNumberList);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
