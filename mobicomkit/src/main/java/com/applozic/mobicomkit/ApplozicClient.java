package com.applozic.mobicomkit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by devashish on 8/21/2015.
 */
public class ApplozicClient {

    public static final String AL_MESSAGE_META_DATA_KEY = "AL_MESSAGE_META_DATA_KEY";
    private static final String HANDLE_DISPLAY_NAME = "CLIENT_HANDLE_DISPLAY_NAME";
    private static final String HANDLE_DIAL = "CLIENT_HANDLE_DIAL";
    private static final String CHAT_LIST_HIDE_ON_NOTIFICATION = "CHAT_LIST_HIDE_ON_NOTIFICATION";
    private static final String CONTEXT_BASED_CHAT = "CONTEXT_BASED_CHAT";
    private static final String NOTIFICATION_SMALL_ICON = "NOTIFICATION_SMALL_ICON";
    private static final String APP_NAME = "APP_NAME";
    private static final String APPLICATION_KEY = "APPLICATION_KEY";
    private static final String NOTIFICATION_DISABLE = "NOTIFICATION_DISABLE";
    private static final String CONTACT_DEFAULT_IMAGE = "CONTACT_DEFAULT_IMAGE";
    private static final String GROUP_DEFAULT_IMAGE = "GROUP_DEFAULT_IMAGE";
    private static final String MESSAGE_META_DATA_SERVICE = "MESSAGE_META_DATA_SERVICE";
    private static final String ENABLE_IP_CALL = "ENABLE_IP_CALL";
    private static final String SHOW_MY_CONTACT_ONLY = "SHOW_MY_CONTACT_ONLY";
    private static final String START_GROUP_OF_TWO = "START_GROUP_OF_TWO";
    private static final String AL_SHOW_APP_ICON = "AL_SHOW_APP_ICON";
    public static ApplozicClient applozicClient;
    public SharedPreferences sharedPreferences;
    private Context context;

    private ApplozicClient(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(MobiComKitClientService.getApplicationKey(context), context.MODE_PRIVATE);
    }

    public static ApplozicClient getInstance(Context context) {
        if (applozicClient == null) {
            applozicClient = new ApplozicClient(context.getApplicationContext());
        }

        return applozicClient;
    }

    public boolean isHandleDisplayName() {
        return sharedPreferences.getBoolean(HANDLE_DISPLAY_NAME, true);
    }

    public ApplozicClient setHandleDisplayName(boolean enable) {
        sharedPreferences.edit().putBoolean(HANDLE_DISPLAY_NAME, enable).commit();
        return this;
    }

    public boolean isHandleDial() {
        return sharedPreferences.getBoolean(HANDLE_DIAL, false);
    }

    public ApplozicClient setHandleDial(boolean enable) {
        sharedPreferences.edit().putBoolean(HANDLE_DIAL, enable).commit();
        return this;
    }

    public ApplozicClient hideChatListOnNotification() {
        sharedPreferences.edit().putBoolean(CHAT_LIST_HIDE_ON_NOTIFICATION, true).commit();
        return this;
    }

    public boolean isChatListOnNotificationIsHidden() {
        return sharedPreferences.getBoolean(CHAT_LIST_HIDE_ON_NOTIFICATION, false);
    }

    public boolean isContextBasedChat() {
        return sharedPreferences.getBoolean(CONTEXT_BASED_CHAT, false);
    }

    public ApplozicClient setContextBasedChat(boolean enable) {
        sharedPreferences.edit().putBoolean(CONTEXT_BASED_CHAT, enable).commit();
        return this;
    }

    public ApplozicClient hideNotificationSmallIcon() {
        sharedPreferences.edit().putBoolean(NOTIFICATION_SMALL_ICON, true).commit();
        return this;
    }

    public boolean isNotificationSmallIconHidden() {
        return sharedPreferences.getBoolean(NOTIFICATION_SMALL_ICON, false);
    }

    public boolean isNotAllowed() {
        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);
        boolean isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
        return !isDebuggable && (pref.getPricingPackage() == RegistrationResponse.PricingType.CLOSED.getValue()
                || pref.getPricingPackage() == RegistrationResponse.PricingType.BETA.getValue());
    }

    public boolean isAccountClosed() {
        return MobiComUserPreference.getInstance(context).getPricingPackage() == RegistrationResponse.PricingType.CLOSED.getValue();
    }

    public String getAppName() {
        return sharedPreferences.getString(APP_NAME, "Applozic");
    }

    public ApplozicClient setAppName(String notficationAppName) {
        sharedPreferences.edit().putString(APP_NAME, notficationAppName).commit();
        return this;
    }

    public String getApplicationKey() {
        return sharedPreferences.getString(APPLICATION_KEY, null);
    }

    public ApplozicClient setApplicationKey(String applicationKey) {
        sharedPreferences.edit().putString(APPLICATION_KEY, applicationKey).commit();
        return this;
    }

    public boolean isNotificationDisabled() {
        return sharedPreferences.getBoolean(NOTIFICATION_DISABLE, false);
    }

    public ApplozicClient enableNotification() {
        sharedPreferences.edit().putBoolean(NOTIFICATION_DISABLE, false).commit();
        return this;
    }

    public ApplozicClient disableNotification() {
        sharedPreferences.edit().putBoolean(NOTIFICATION_DISABLE, true).commit();
        return this;
    }

    public String getDefaultContactImage() {
        return sharedPreferences.getString(CONTACT_DEFAULT_IMAGE, "applozic_ic_contact_picture_holo_light");
    }

    public ApplozicClient setDefaultContactImage(String imageName) {
        sharedPreferences.edit().putString(CONTACT_DEFAULT_IMAGE, imageName).commit();
        return this;
    }

    public String getDefaultChannelImage() {
        return sharedPreferences.getString(GROUP_DEFAULT_IMAGE, "applozic_group_icon");
    }

    public ApplozicClient setDefaultChannelImage(String groupImageName) {
        sharedPreferences.edit().putString(GROUP_DEFAULT_IMAGE, groupImageName).commit();
        return this;
    }

    public String getMessageMetaDataServiceName() {
        return sharedPreferences.getString(MESSAGE_META_DATA_SERVICE, null);
    }

    public ApplozicClient setMessageMetaDataServiceName(String messageMetaDataServiceName) {
        sharedPreferences.edit().putString(MESSAGE_META_DATA_SERVICE, messageMetaDataServiceName).commit();
        return this;
    }

    public boolean isShowMyContacts() {
        return sharedPreferences.getBoolean(SHOW_MY_CONTACT_ONLY, false);
    }

    public ApplozicClient enableShowMyContacts() {
        sharedPreferences.edit().putBoolean(SHOW_MY_CONTACT_ONLY, true).commit();
        return this;
    }

    public ApplozicClient disableShowMyContacts() {
        sharedPreferences.edit().putBoolean(SHOW_MY_CONTACT_ONLY, false).commit();
        return this;
    }

    public boolean isIPCallEnabled() {
        return sharedPreferences.getBoolean(ENABLE_IP_CALL, false);
    }

    public void setIPCallEnabled(boolean iPCallEnabled) {
        sharedPreferences.edit().putBoolean(ENABLE_IP_CALL, iPCallEnabled).commit();
    }

    public String getMessageMetaData() {
        return sharedPreferences.getString(AL_MESSAGE_META_DATA_KEY, null);
    }

    public ApplozicClient setMessageMetaData(Map<String, String> messageMetaDataMap) {
        if (messageMetaDataMap != null) {
            sharedPreferences.edit().putString(AL_MESSAGE_META_DATA_KEY, new JSONObject(messageMetaDataMap).toString()).commit();
        }
        return this;
    }

    public ApplozicClient startGroupOfTwo() {
        sharedPreferences.edit().putBoolean(START_GROUP_OF_TWO, true).commit();
        return this;
    }

    public boolean isStartGroupOfTwo() {
        return sharedPreferences.getBoolean(START_GROUP_OF_TWO, false);
    }

    public ApplozicClient disableStartGroupOfTwo() {
        sharedPreferences.edit().putBoolean(START_GROUP_OF_TWO, false).commit();
        return this;
    }

    public ApplozicClient showAppIconInNotification(boolean showOrHide) {
        sharedPreferences.edit().putBoolean(AL_SHOW_APP_ICON, showOrHide).commit();
        return this;
    }

    public boolean isShowAppIconInNotification() {
        return sharedPreferences.getBoolean(AL_SHOW_APP_ICON, false);
    }

}
