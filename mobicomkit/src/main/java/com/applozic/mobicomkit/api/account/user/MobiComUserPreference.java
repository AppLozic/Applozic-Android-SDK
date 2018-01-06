package com.applozic.mobicomkit.api.account.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;

import java.util.Set;


public class MobiComUserPreference {

    private static final String USER_ID = "userId";
    public static MobiComUserPreference userpref;
    //Constants for preferneces ..
    private static String device_registration_id = "device_registration_id";
    private static String device_key_string = "device_key_string";
    private static String last_outbox_sync_time = "last_outbox_sync_time";
    private static String delivery_report_pref_key = "delivery_report_pref_key";
    private static String last_inbox_sync_time = "last_inbox_sync_time";
    private static String last_message_stat_sync_time = "last_message_stat_sync_time";
    private static String sent_sms_sync_pref_key = "sent_sms_sync_pref_key";
    private static String email = "email";
    private static String email_verified = "email_verified";
    private static String user_key_string = "user_key_string";
    private static String stop_service = "stop_service";
    private static String patch_available = "patch_available";
    private static String webhook_enable_key = "webhook_enable_key";
    private static String group_sms_freq_key = "group_sms_freq_key";
    private static String update_push_registration = "update_push_registration";
    private static String verify_contact_number = "verify_contact_number";
    private static String received_sms_sync_pref_key = "received_sms_sync_pref_key";
    private static String phone_number_key = "phone_number_key";
    private static String call_history_display_within_messages_pref_key = "call_history_display_within_messages_pref_key";
    private static String mobitexter_contact_sync_key = "mobitexter_contact_sync_key";
    private static String last_sms_sync_time = "last_sms_sync_time";
    private static String new_message_flag = "new_message_flag";
    private static String base_url = "base_url";
    private static String display_name = "display_name";
    private static String logged_in = "logged_in";
    private static String lastSeenAtSyncTime = "lastSeenAtSyncTime";
    private static String channelSyncTime = "channelSyncTime";
    private static String device_time_offset_from_UTC = "device_time_offset_from_UTC";
    private static String image_compression_enabled = "image_compression_enabled";
    private static String userBlockSyncTime = "user_block_Sync_Time";
    private static String max_compressed_image_size = "max_compressed_image_size";
    private static String image_link = "image_link";
    private static String registered_users_last_fetch_time = "registered_users_last_fetch_time";
    private static String password = "password";
    private static String authenticationType = "authenticationType";
    private static String mqtt_broker_url = "mqtt_broker_url";
    private static String contact_list_server_call = "contact_list_server_call";
    private static String pricing_package = "pricing_package";
    private static String delete_channel = "delete_channel";
    private static String encryption_Key = "encryption_Key";
    private static String enable_encryption = "enable_encryption";
    private static String enable_auto_download_on_wifi = "enable_auto_download_on_wifi";
    private static String enable_auto_download_on_cellular = "enable_auto_download_on_cellular";
    private static String video_call_token = "video_call_token";
    private static String user_type_id = "user_type_id";
    private static String application_info_call_done = "application_info_call_done";
    private static String notification_sound_fileName = "notificationSoundFileName";
    private static String notification_sound_enabled = "notification_sound_enabled";
    private static String CONTACTS_GROUP_ID = "CONTACTS_GROUP_ID";
    private static String CONTACT_GROUP_ID_LISTS = "contactGroupIdLists";
    private static String IS_CONTACT_GROUP_NAME_LIST = "isContactGroupNameList";
    private static String last_sync_time_for_metadata_update = "lastSyncTimeForMetadataUpdate";
    private static String START_TIME_FOR_MESSAGE_LIST_SCROLL = "startTimeForMessageListScroll";
    private static String USER_ROLE_TYPE = "userRoleType";

    public SharedPreferences sharedPreferences;
    private Context context;
    private String countryCode;


    private MobiComUserPreference(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), context.MODE_PRIVATE);
        initialize(context);
    }

    public static MobiComUserPreference getInstance(Context context) {
        if (userpref == null) {
            userpref = new MobiComUserPreference(context.getApplicationContext());
        }
        return userpref;
    }

    /*
    public void setDeviceRegistrationId(String deviceRegistrationId) {
        sharedPreferences.edit().putString(OsuConstants.DEVICE_REGISTRATION_ID, deviceRegistrationId).commit();
    }

    public String getDeviceRegistrationId() {
        return sharedPreferences.getString(OsuConstants.DEVICE_REGISTRATION_ID, null);
    }*/

    public boolean isRegistered() {
        return !TextUtils.isEmpty(getDeviceKeyString());
    }

    public String getDeviceRegistrationId() {
        return sharedPreferences.getString(device_registration_id, null);
    }

    public void setDeviceRegistrationId(String deviceRegistrationId) {
        sharedPreferences.edit().putString(device_registration_id, deviceRegistrationId).commit();
    }

    public String getDeviceKeyString() {
        return sharedPreferences.getString(device_key_string, null);
    }

    public void setDeviceKeyString(String deviceKeyString) {
        sharedPreferences.edit().putString(device_key_string, deviceKeyString).commit();
    }

    public long getLastOutboxSyncTime() {
        return sharedPreferences.getLong(last_outbox_sync_time, 0L);
    }

    public void setLastOutboxSyncTime(long lastOutboxSyncTime) {
        sharedPreferences.edit().putLong(last_outbox_sync_time, lastOutboxSyncTime).commit();
    }

    public boolean isReportEnable() {
        return sharedPreferences.getBoolean(delivery_report_pref_key, false);
    }

    public void setReportEnable(boolean reportEnable) {
        sharedPreferences.edit().putBoolean(delivery_report_pref_key, reportEnable).commit();
    }

    public String getLastSyncTime() {
        return sharedPreferences.getString(last_sms_sync_time, "0");
    }

    public void setLastSyncTime(String lastSyncTime) {
        sharedPreferences.edit().putString(last_sms_sync_time, lastSyncTime).commit();
    }

    public long getLastInboxSyncTime() {
        return sharedPreferences.getLong(last_inbox_sync_time, 0L);
    }

    public void setLastInboxSyncTime(long lastInboxSyncTime) {
        sharedPreferences.edit().putLong(last_inbox_sync_time, lastInboxSyncTime).commit();
    }

    public Long getLastMessageStatSyncTime() {
        return sharedPreferences.getLong(last_message_stat_sync_time, 0);
    }

    public void setLastMessageStatSyncTime(long lastMessageStatSyncTime) {
        sharedPreferences.edit().putLong(last_message_stat_sync_time, lastMessageStatSyncTime).commit();
    }

    public boolean isSentSmsSyncFlag() {
        return sharedPreferences.getBoolean(sent_sms_sync_pref_key, true);
    }

    public void setSentSmsSyncFlag(boolean sentSmsSyncFlag) {
        sharedPreferences.edit().putBoolean(sent_sms_sync_pref_key, sentSmsSyncFlag).commit();
    }

    public String getEmailIdValue() {
        return sharedPreferences.getString(email, null);
    }

    public void setEmailIdValue(String emailIdValue) {
        sharedPreferences.edit().putString(email, emailIdValue).commit();
    }

    public String getUserId() {
        String userId = sharedPreferences.getString(USER_ID, null);
        if (TextUtils.isEmpty(userId)) {
            return getEmailIdValue();
        }
        return userId;
    }

    public void setUserId(String userId) {
        sharedPreferences.edit().putString(USER_ID, userId).commit();
    }

    public boolean isEmailVerified() {
        return sharedPreferences.getBoolean(email_verified, true);
    }

    public void setEmailVerified(boolean emailVerified) {
        sharedPreferences.edit().putBoolean(email_verified, emailVerified).commit();
    }

    public String getSuUserKeyString() {
        return sharedPreferences.getString(user_key_string, null);
    }

    public void setSuUserKeyString(String suUserKeyString) {
        sharedPreferences.edit().putString(user_key_string, suUserKeyString).commit();
    }

    public boolean isStopServiceFlag() {
        return sharedPreferences.getBoolean(stop_service, false);
    }

    public void setStopServiceFlag(Boolean stopServiceFlag) {
        sharedPreferences.edit().putBoolean(stop_service, stopServiceFlag).commit();
    }

    public boolean isPatchAvailable() {
        return sharedPreferences.getBoolean(patch_available, false);
    }

    public void setPatchAvailable(Boolean patchAvailable) {
        sharedPreferences.edit().putBoolean(patch_available, patchAvailable).commit();
    }

    public boolean isWebHookEnable() {
        return sharedPreferences.getBoolean(webhook_enable_key, false);
    }

    public void setWebHookEnable(boolean enable) {
        sharedPreferences.edit().putBoolean(webhook_enable_key, enable).commit();
    }

    public int getGroupSmsDelayInSec() {
        return sharedPreferences.getInt(group_sms_freq_key, 0);
    }

    public void setDelayGroupSmsDelayTime(int delay) {
        sharedPreferences.edit().
                putInt(group_sms_freq_key, delay).commit();
    }


//    public boolean getNewPatchAvailable() {
//        return newPatchAvailable;
//    }
//
//    public boolean getUpdateRegFlag() {
//        return updateRegFlag;
//    }

    public boolean isUpdateRegFlag() {
        return sharedPreferences.getBoolean(update_push_registration, false);
    }

    public void setUpdateRegFlag(boolean updateRegFlag) {
        sharedPreferences.edit().putBoolean(update_push_registration, updateRegFlag).commit();
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public boolean isVerifyContactNumber() {
        return sharedPreferences.getBoolean(verify_contact_number, false);
    }

    public void setVerifyContactNumber(boolean verifyContactNumber) {
        sharedPreferences.edit().putBoolean(verify_contact_number, verifyContactNumber).commit();
    }

    public boolean getReceivedSmsSyncFlag() {
        return sharedPreferences.getBoolean(received_sms_sync_pref_key, true);
    }

    public void setReceivedSmsSyncFlag(boolean receivedSmsSyncFlag) {
        sharedPreferences.edit().putBoolean(received_sms_sync_pref_key, receivedSmsSyncFlag).commit();
    }

    public String getContactNumber() {
        return sharedPreferences.getString(phone_number_key, null);
    }

    public void setContactNumber(String contactNumber) {
        // contactNumber = ContactNumberUtils.getPhoneNumber(contactNumber, getCountryCode());
        sharedPreferences.edit().putString(phone_number_key, contactNumber).commit();
    }

    public boolean isDisplayCallRecordEnable() {
        return sharedPreferences.getBoolean(call_history_display_within_messages_pref_key, false);
    }

    public void setDisplayCallRecordEnable(boolean enable) {
        sharedPreferences.edit().putBoolean(call_history_display_within_messages_pref_key, enable).commit();
    }

    public boolean getNewMessageFlag() {
        return sharedPreferences.getBoolean(new_message_flag, false);
    }

    public void setNewMessageFlag(boolean enable) {
        sharedPreferences.edit().putBoolean(new_message_flag, enable).commit();
    }

    public long getDeviceTimeOffset() {
        return sharedPreferences.getLong(device_time_offset_from_UTC, 0L);
    }

    public boolean setDeviceTimeOffset(long diiference) {
        return sharedPreferences.edit().putLong(device_time_offset_from_UTC, diiference).commit();
    }

    //Local initialization of few fields)
    public void initialize(Context context) {
      /*  TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = telephonyManager.getSimCountryIso().toUpperCase();
        String contactNumber = telephonyManager.getLine1Number();
        setCountryCode(countryCode);
        if (!TextUtils.isEmpty(contactNumber)) {
            setContactNumber(contactNumber);
        }
        if (getLastMessageStatSyncTime() == null || getLastMessageStatSyncTime() == 0) {
            setLastMessageStatSyncTime(new Date().getTime());
        }*/
    }

    public boolean isMobiTexterContactSyncCompleted() {
        return sharedPreferences.getBoolean(mobitexter_contact_sync_key, false);
    }

    public void setMobiTexterContactSyncCompleted(boolean status) {
        sharedPreferences.edit().
                putBoolean(mobitexter_contact_sync_key, status).commit();
    }

    public String getUrl() {
        return sharedPreferences.getString(base_url, null);
    }

    public void setUrl(String url) {
        sharedPreferences.edit().putString(base_url, url).commit();
    }

    public String getMqttBrokerUrl() {
        return sharedPreferences.getString(mqtt_broker_url, null);
    }

    public void setMqttBrokerUrl(String url) {
        sharedPreferences.edit().putString(mqtt_broker_url, url).commit();
    }

    public int getPricingPackage() {
        return sharedPreferences.getInt(pricing_package, RegistrationResponse.PricingType.STARTER.getValue());
    }

    public void setPricingPackage(int pricingPackage) {
        sharedPreferences.edit().putInt(pricing_package, pricingPackage).commit();
    }

    public String getDisplayName() {
        return sharedPreferences.getString(display_name, null);
    }

    public void setDisplayName(String displayName) {
        sharedPreferences.edit().putString(display_name, displayName).commit();
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(getUserId());
    }

    public String getLastSeenAtSyncTime() {
        return sharedPreferences.getString(lastSeenAtSyncTime, "0");
    }

    public void setLastSeenAtSyncTime(String lastSeenAtTime) {
        sharedPreferences.edit().putString(lastSeenAtSyncTime, lastSeenAtTime).commit();
    }

    public String getChannelSyncTime() {
        return sharedPreferences.getString(channelSyncTime, "0");
    }

    public void setChannelSyncTime(String syncChannelTime) {
        sharedPreferences.edit().putString(channelSyncTime, syncChannelTime).commit();
    }

    public int getCompressedImageSizeInMB() {
        return sharedPreferences.getInt(max_compressed_image_size, 10);

    }

    public void setLastSyncTimeForMetadataUpdate(String lastSyncTime) {
        sharedPreferences.edit().putString(last_sync_time_for_metadata_update, lastSyncTime).commit();
    }

    public String getLastSyncTimeForMetadataUpdate() {
        return sharedPreferences.getString(last_sync_time_for_metadata_update, null);
    }

    public void setCompressedImageSizeInMB(int maxSize) {

        sharedPreferences.edit().putInt(max_compressed_image_size, maxSize).commit();
    }

    public String getUserBlockSyncTime() {
        return sharedPreferences.getString(userBlockSyncTime, "0");
    }

    public void setUserBlockSyncTime(String lastUserBlockSyncTime) {
        sharedPreferences.edit().putString(userBlockSyncTime, lastUserBlockSyncTime).commit();
    }

    public long getRegisteredUsersLastFetchTime() {
        return sharedPreferences.getLong(registered_users_last_fetch_time, 0l);
    }

    public void setRegisteredUsersLastFetchTime(long lastFetchTime) {
        sharedPreferences.edit().putLong(registered_users_last_fetch_time, lastFetchTime).commit();
    }

    public String getImageLink() {
        return sharedPreferences.getString(image_link, null);
    }

    public void setImageLink(String imageUrl) {
        sharedPreferences.edit().putString(image_link, imageUrl).commit();
    }

    public String getPassword() {
        return sharedPreferences.getString(password, null);
    }

    public void setPassword(String val) {
        sharedPreferences.edit().putString(password, val).commit();
    }

    public String getAuthenticationType() {
        return sharedPreferences.getString(authenticationType, "0");
    }

    public void setAuthenticationType(String val) {
        sharedPreferences.edit().putString(authenticationType, val).commit();
    }

    public void setDeleteChannel(boolean channelDelete) {
        sharedPreferences.edit().putBoolean(delete_channel, channelDelete).commit();
    }

    public boolean isChannelDeleted() {
        return sharedPreferences.getBoolean(delete_channel, false);
    }

    @Override
    public String toString() {
        return "MobiComUserPreference{" +
                "context=" + context +
                ", countryCode='" + getCountryCode() + '\'' +
                ", deviceKeyString=" + getDeviceKeyString() +
                ", contactNumber=" + getContactNumber() +
                '}';
    }

    public boolean clearAll() {

        return sharedPreferences.edit().clear().commit();

        //Intent intent = new Intent(this, LoginActivity.class);
        //startActivity(intent);


    }

    public boolean isImageCompressionEnabled() {
        return sharedPreferences.getBoolean(image_compression_enabled, true);
    }

    public void setImageCompressionEnabled(boolean imageCompressionEnabled) {
        sharedPreferences.edit().putBoolean(image_compression_enabled, imageCompressionEnabled).commit();
    }

    public boolean getWasContactListServerCallAlreadyDone() {
        return sharedPreferences.getBoolean(contact_list_server_call, false);
    }

    public void setWasContactListServerCallAlreadyDone(Boolean serverCallAlreadyDone) {
        sharedPreferences.edit().putBoolean(contact_list_server_call, serverCallAlreadyDone).commit();
    }

    public String getEncryptionKey() {
        return sharedPreferences.getString(encryption_Key, null);
    }

    public void setEncryptionKey(String encryptionKey) {
        sharedPreferences.edit().putString(encryption_Key, encryptionKey).commit();
    }

    public boolean isEncryptionEnabled() {
        return sharedPreferences.getBoolean(enable_encryption, false);
    }

    public void enableEncryption(boolean enableEncryption) {
        sharedPreferences.edit().putBoolean(enable_encryption, enableEncryption).commit();
    }

    public boolean getAutoDownloadOnWifi() {
        return sharedPreferences.getBoolean(enable_auto_download_on_wifi, false);
    }

    public void setAutoDownloadOnWifi(boolean enable) {
        sharedPreferences.edit().putBoolean(enable_auto_download_on_wifi, enable).commit();
    }

    public boolean getAutoDownloadOnCellular() {
        return sharedPreferences.getBoolean(enable_auto_download_on_cellular, false);
    }

    public void setAutoDownloadOnCellular(boolean enable) {
        sharedPreferences.edit().putBoolean(enable_auto_download_on_cellular, enable).commit();
    }

    public String getVideoCallToken() {
        return sharedPreferences.getString(video_call_token, null);
    }

    public void setVideoCallToken(String token) {
        sharedPreferences.edit().putString(video_call_token, token).commit();

    }

    public String getUserTypeId() {
        return sharedPreferences.getString(user_type_id, null);
    }

    public void setUserTypeId(String userTypeId) {
        sharedPreferences.edit().putString(user_type_id, userTypeId).commit();
    }


    public boolean getApplicationInfoCall() {
        return sharedPreferences.getBoolean(application_info_call_done, false);
    }

    public void setApplicationInfoCallDone(boolean customerResponse) {
        sharedPreferences.edit().putBoolean(application_info_call_done, customerResponse).commit();
    }

    public String getNotificationSoundFilePath() {
        return sharedPreferences.getString(notification_sound_fileName, null);
    }

    public void setNotificationSoundFilePath(String soundUri) {
        sharedPreferences.edit().putString(notification_sound_fileName, soundUri).commit();
    }

    public boolean getNotificationSoundEnabled() {
        return sharedPreferences.getBoolean(notification_sound_enabled, true);
    }

    public void setNotificationSoundEnabled(boolean notification_sound_enabled) {
        sharedPreferences.edit().putBoolean(this.notification_sound_enabled, notification_sound_enabled).commit();
    }

    public String getContactsGroupId() {
        return sharedPreferences.getString(CONTACTS_GROUP_ID, null);
    }

    public void setContactsGroupId(String contactsGroupId) {
        sharedPreferences.edit().putString(CONTACTS_GROUP_ID, contactsGroupId).commit();
    }

    public void setContactGroupIdList(Set<String> contactGroupList) {
        sharedPreferences.edit().putStringSet(CONTACT_GROUP_ID_LISTS, contactGroupList).commit();
    }

    public Set<String> getContactGroupIdList() {
        return sharedPreferences.getStringSet(CONTACT_GROUP_ID_LISTS, null);
    }

    public boolean isContactGroupNameList() {
        return sharedPreferences.getBoolean(IS_CONTACT_GROUP_NAME_LIST, false);
    }

    public void setIsContactGroupNameList(boolean isContactGroupNameList) {
        sharedPreferences.edit().putBoolean(IS_CONTACT_GROUP_NAME_LIST, isContactGroupNameList).commit();
    }

    public void setStartTimeForPagination(Long startTime) {
        sharedPreferences.edit().putLong(START_TIME_FOR_MESSAGE_LIST_SCROLL, startTime).commit();
    }

    public Long getStartTimeForPagination() {
        return sharedPreferences.getLong(START_TIME_FOR_MESSAGE_LIST_SCROLL, 0);
    }

    public void setUserRoleType(Short roleType) {
        sharedPreferences.edit().putInt(USER_ROLE_TYPE, roleType).commit();
    }

    public Short getUserRoleType() {
        return Short.valueOf((short) sharedPreferences.getInt(USER_ROLE_TYPE, 0));
    }
}
