package com.applozic.mobicomkit.api.account.user;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.database.MobiComDatabaseHelper;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.SyncBlockUserApiResponse;
import com.applozic.mobicomkit.feed.UserDetailListFeed;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by devashish on 24/12/14.
 */
public class UserClientService extends MobiComKitClientService {

    public static final String SHARED_PREFERENCE_VERSION_UPDATE_KEY = "mck.version.update";
    public static final String PHONE_NUMBER_UPDATE_URL = "/rest/ws/registration/phone/number/update";
    public static final String NOTIFY_CONTACTS_ABOUT_JOINING_MT = "/rest/ws/registration/notify/contacts";
    public static final String VERIFICATION_CONTACT_NUMBER_URL = "/rest/ws/verification/number";
    public static final String VERIFICATION_CODE_CONTACT_NUMBER_URL = "/rest/ws/verification/code";
    public static final String APP_VERSION_UPDATE_URL = "/rest/ws/register/version/update";
    public static final String SETTING_UPDATE_URL = "/rest/ws/setting/single/update";
    public static final String TIMEZONE_UPDATAE_URL = "/rest/ws/setting/updateTZ";
    public static final String USER_INFO_URL = "/rest/ws/user/info?";
    public static final Short MOBICOMKIT_VERSION_CODE = 109;
    public static final String USER_DISPLAY_NAME_UPDATE = "/rest/ws/user/name?";
    public static final String BLOCK_USER_URL = "/rest/ws/user/block";
    public static final String BLOCK_USER_SYNC_URL = "/rest/ws/user/blocked/sync";
    public static final String UNBLOCK_USER_SYNC_URL = "/rest/ws/user/unblock";
    public static final String USER_DETAILS_URL = "/rest/ws/user/detail?";
    public static final String ONLINE_USER_LIST_URL = "/rest/ws/user/ol/list";
    public static final String REGISTERED_USER_LIST_URL = "/rest/ws/user/filter";
    public static final String USER_PROFILE_UPDATE_URL = "/rest/ws/user/update";
    public static final String USER_READ_URL = "/rest/ws/user/read";
    public static final String USER_DETAILS_LIST_POST_URL = "/rest/ws/user/detail";
    public static final String UPDATE_USER_PASSWORD = "/rest/ws/user/update/password";
    public static final String USER_LOGOUT = "/rest/ws/device/logout";
    public static final String APPLICATION_INFO_UPDATE_URL = "/apps/customer/application/info/update";
    public static final int BATCH_SIZE = 60;
    private static final String TAG = "UserClientService";
    private HttpRequestUtils httpRequestUtils;

    public UserClientService(Context context) {
        super(context);
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getPhoneNumberUpdateUrl() {
        return getBaseUrl() + PHONE_NUMBER_UPDATE_URL;
    }

    public String getUserProfileUpdateUrl() {
        return getBaseUrl() + USER_PROFILE_UPDATE_URL;
    }

    public String getNotifyContactsAboutJoiningMt() {
        return getBaseUrl() + NOTIFY_CONTACTS_ABOUT_JOINING_MT;
    }

    public String getVerificationContactNumberUrl() {
        return getBaseUrl() + VERIFICATION_CONTACT_NUMBER_URL;
    }

    public String getVerificationCodeContactNumberUrl() {
        return getBaseUrl() + VERIFICATION_CODE_CONTACT_NUMBER_URL;
    }

    public String getAppVersionUpdateUrl() {
        return getBaseUrl() + APP_VERSION_UPDATE_URL;
    }

    public String getUpdateUserDisplayNameUrl() {
        return getBaseUrl() + USER_DISPLAY_NAME_UPDATE;
    }

    public String getSettingUpdateUrl() {
        return getBaseUrl() + SETTING_UPDATE_URL;
    }

    public String getTimezoneUpdataeUrl() {
        return getBaseUrl() + TIMEZONE_UPDATAE_URL;
    }

    public String getUserInfoUrl() {
        return getBaseUrl() + USER_INFO_URL;
    }

    public String getBlockUserUrl() {
        return getBaseUrl() + BLOCK_USER_URL;
    }

    public String getBlockUserSyncUrl() {
        return getBaseUrl() + BLOCK_USER_SYNC_URL;
    }

    public String getUnBlockUserSyncUrl() {
        return getBaseUrl() + UNBLOCK_USER_SYNC_URL;
    }

    public String getUserDetailsListUrl() {
        return getBaseUrl() + USER_DETAILS_URL;
    }

    public String getOnlineUserListUrl() {
        return getBaseUrl() + ONLINE_USER_LIST_URL;
    }

    public String getRegisteredUserListUrl() {
        return getBaseUrl() + REGISTERED_USER_LIST_URL;
    }

    public String getUserDetailsListPostUrl() {
        return getBaseUrl() + USER_DETAILS_LIST_POST_URL;
    }

    public String getUserReadUrl() {
        return getBaseUrl() + USER_READ_URL;
    }

    public String getUpdateUserPasswordUrl() {
        return getBaseUrl() + UPDATE_USER_PASSWORD;
    }

    public String getUserLogout() {
        return getBaseUrl() + USER_LOGOUT;
    }

    public String getApplicationInfoUrl() {
        return getBaseUrl() + APPLICATION_INFO_UPDATE_URL;
    }

    public ApiResponse logout() {
        return logout(false);
    }

    public void clearDataAndPreference() {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        final String deviceKeyString = mobiComUserPreference.getDeviceKeyString();
        final String userKeyString = mobiComUserPreference.getSuUserKeyString();
        String url = mobiComUserPreference.getUrl();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        mobiComUserPreference.clearAll();
        MessageDatabaseService.recentlyAddedMessage.clear();
        MobiComDatabaseHelper.getInstance(context).delDatabase();
        mobiComUserPreference.setUrl(url);
        Intent intent = new Intent(context, ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.USER_KEY_STRING, userKeyString);
        intent.putExtra(ApplozicMqttIntentService.DEVICE_KEY_STRING, deviceKeyString);
        context.startService(intent);
    }

    public ApiResponse logout(boolean fromLogin) {
        Utils.printLog(context, TAG, "Al Logout call !!");
        ApiResponse apiResponse = userLogoutResponse();
        if (apiResponse != null && apiResponse.isSuccess()) {
            MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
            final String deviceKeyString = mobiComUserPreference.getDeviceKeyString();
            final String userKeyString = mobiComUserPreference.getSuUserKeyString();
            String url = mobiComUserPreference.getUrl();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
            mobiComUserPreference.clearAll();
            MessageDatabaseService.recentlyAddedMessage.clear();
            MobiComDatabaseHelper.getInstance(context).delDatabase();
            mobiComUserPreference.setUrl(url);
            if (!fromLogin) {
                Intent intent = new Intent(context, ApplozicMqttIntentService.class);
                intent.putExtra(ApplozicMqttIntentService.USER_KEY_STRING, userKeyString);
                intent.putExtra(ApplozicMqttIntentService.DEVICE_KEY_STRING, deviceKeyString);
                context.startService(intent);
            }
        }
        return apiResponse;
    }

    public ApiResponse userLogoutResponse() {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.postData(getUserLogout(), "application/json", "application/json", null);
            if (!TextUtils.isEmpty(response)) {
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public boolean sendVerificationCodeToServer(String verificationCode) {
        try {
            String response = httpRequestUtils.getResponse(getVerificationCodeContactNumberUrl() + "?verificationCode=" + verificationCode, "application/json", "application/json");
            JSONObject json = new JSONObject(response);
            return json.has("code") && json.get("code").equals("200");
        } catch (Exception e) {
            Utils.printLog(context, "Verification Code", "Got Exception while submitting verification code to server: " + e);
        }
        return false;
    }

    public void updateCodeVersion(final String deviceKeyString) {
        String url = getAppVersionUpdateUrl() + "?appVersionCode=" + MOBICOMKIT_VERSION_CODE + "&deviceKey=" + deviceKeyString;
        String response = httpRequestUtils.getResponse(url, "text/plain", "text/plain");
        Utils.printLog(context, TAG, "Version update response: " + response);

    }

    public String updatePhoneNumber(String contactNumber) throws UnsupportedEncodingException {
        return httpRequestUtils.getResponse(getPhoneNumberUpdateUrl() + "?phoneNumber=" + URLEncoder.encode(contactNumber, "UTF-8"), "text/plain", "text/plain");
    }

    public void notifyFriendsAboutJoiningThePlatform() {
        String response = httpRequestUtils.getResponse(getNotifyContactsAboutJoiningMt(), "text/plain", "text/plain");
        Utils.printLog(context, TAG, "Response for notify contact about joining MT: " + response);
    }

    public String sendPhoneNumberForVerification(String contactNumber, String countryCode, boolean viaSms) {
        try {
            String viaSmsParam = "";
            if (viaSms) {
                viaSmsParam = "&viaSms=true";
            }
            return httpRequestUtils.getResponse(getVerificationContactNumberUrl() + "?countryCode=" + countryCode + "&contactNumber=" + URLEncoder.encode(contactNumber, "UTF-8") + viaSmsParam, "application/json", "application/json");
        } catch (Exception e) {
            Utils.printLog(context, "Verification Code", "Got Exception while submitting contact number for verification to server: " + e);
        }
        return null;
    }

    public void updateSetting(final String key, final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                   /* List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                    nameValuePairs.add(new BasicNameValuePair("key", key));
                    nameValuePairs.add(new BasicNameValuePair("value", value));
                    String response = httpRequestUtils.postData(getCredentials(), getSettingUpdateUrl(), "text/plain", "text/plain", null);
                    Log.i(TAG, "Response from setting update : " + response);*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public Map<String, String> getUserInfo(Set<String> userIds) throws JSONException, UnsupportedEncodingException {

        if (userIds == null && userIds.isEmpty()) {
            return new HashMap<>();
        }

        String userIdParam = "";
        for (String userId : userIds) {
            userIdParam += "&userIds" + "=" + URLEncoder.encode(userId, "UTF-8");
        }

        String response = httpRequestUtils.getResponse(getUserInfoUrl() + userIdParam, "application/json", "application/json");
        Utils.printLog(context, TAG, "Response: " + response);

        JSONObject jsonObject = new JSONObject(response);

        Map<String, String> info = new HashMap<String, String>();

        Iterator iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = jsonObject.getString(key);
            info.put(key, value);
        }
        return info;
    }

    public void updateUserDisplayName(final String userId, final String displayName) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String parameters = "";
                try {
                    if (!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(displayName)) {
                        parameters = "userId=" + URLEncoder.encode(userId, "UTF-8") + "&displayName=" + URLEncoder.encode(displayName, "UTF-8");
                        String response = httpRequestUtils.getResponse(getUpdateUserDisplayNameUrl() + parameters, "application/json", "application/json");

                        ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
                        if (apiResponse != null) {
                            Utils.printLog(context, TAG, " Update display name Response :" + apiResponse.getStatus());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
    }

    public ApiResponse userBlock(String userId, boolean block) {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            if (!TextUtils.isEmpty(userId)) {
                response = httpRequestUtils.getResponse((block ? getBlockUserUrl() : getUnBlockUserSyncUrl()) + "?userId=" + URLEncoder.encode(userId, "UTF-8"), "application/json", "application/json");
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public ApiResponse userUnBlock(String userId) {
        String response = "";
        ApiResponse apiResponse = null;
        try {
            if (!TextUtils.isEmpty(userId)) {
                response = httpRequestUtils.getResponse(getUnBlockUserSyncUrl() + "?userId=" + URLEncoder.encode(userId, "UTF-8"), "application/json", "application/json");
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public SyncBlockUserApiResponse getSyncUserBlockList(String lastSyncTime) {
        try {
            String url = getBlockUserSyncUrl() + "?lastSyncTime=" + lastSyncTime;
            String response = httpRequestUtils.getResponse(url, "application/json", "application/json");

            if (response == null || TextUtils.isEmpty(response) || response.equals("UnAuthorized Access")) {
                return null;
            }
            return (SyncBlockUserApiResponse) GsonUtils.getObjectFromJson(response, SyncBlockUserApiResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUserDetails(Set<String> userIds) {
        try {
            if (userIds != null && userIds.size() > 0) {
                String response = "";
                String userIdParam = "";
                for (String userId : userIds) {
                    userIdParam += "&userIds" + "=" + URLEncoder.encode(userId, "UTF-8");
                }
                response = httpRequestUtils.getResponse(getUserDetailsListUrl() + userIdParam, "application/json", "application/json");
                Utils.printLog(context, TAG, "User details response is :" + response);
                if (TextUtils.isEmpty(response) || response.contains("<html>")) {
                    return null;
                }
                return response;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String postUserDetailsByUserIds(Set<String> userIds) {
        try {
            if (userIds != null && userIds.size() > 0) {
                List<String> userDetailsList = new ArrayList<>();
                String response = "";
                int count = 0;
                for (String userId : userIds) {
                    count++;
                    userDetailsList.add(userId);
                    if (count % BATCH_SIZE == 0) {
                        UserDetailListFeed userDetailListFeed = new UserDetailListFeed();
                        userDetailListFeed.setContactSync(true);
                        userDetailListFeed.setUserIdList(userDetailsList);
                        String jsonFromObject = GsonUtils.getJsonFromObject(userDetailListFeed, userDetailListFeed.getClass());
                        Utils.printLog(context, TAG, "Sending json:" + jsonFromObject);
                        response = httpRequestUtils.postData(getUserDetailsListPostUrl() + "?contactSync=true", "application/json", "application/json", jsonFromObject);
                        userDetailsList = new ArrayList<String>();
                        if (!TextUtils.isEmpty(response)) {
                            UserService.getInstance(context).processUserDetailsResponse(response);
                        }
                    }
                }
                if (!userDetailsList.isEmpty() && userDetailsList.size() > 0) {
                    UserDetailListFeed userDetailListFeed = new UserDetailListFeed();
                    userDetailListFeed.setContactSync(true);
                    userDetailListFeed.setUserIdList(userDetailsList);
                    String jsonFromObject = GsonUtils.getJsonFromObject(userDetailListFeed, userDetailListFeed.getClass());
                    response = httpRequestUtils.postData(getUserDetailsListPostUrl() + "?contactSync=true", "application/json", "application/json", jsonFromObject);

                    Utils.printLog(context, TAG, "User details response is :" + response);
                    if (TextUtils.isEmpty(response) || response.contains("<html>")) {
                        return null;
                    }

                    if (!TextUtils.isEmpty(response)) {
                        UserService.getInstance(context).processUserDetailsResponse(response);
                    }
                }
                return response;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, String> getOnlineUserList(int numberOfUser) {
        Map<String, String> info = new HashMap<String, String>();
        try {
            String response = httpRequestUtils.getResponse(getOnlineUserListUrl() + "?startIndex=0&pageSize=" + numberOfUser, "application/json", "application/json");
            if (response != null && !MobiComKitConstants.ERROR.equals(response)) {
                JSONObject jsonObject = new JSONObject(response);
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = jsonObject.getString(key);
                    info.put(key, value);
                }
                return info;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public String getRegisteredUsers(Long startTime, int pageSize) {
        String response = null;
        try {
            String url = "?pageSize=" + pageSize;
            if (startTime > 0) {
                url = url + "&startTime=" + startTime;
            }
            response = httpRequestUtils.getResponse(getRegisteredUserListUrl() + url, "application/json", "application/json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public ApiResponse updateDisplayNameORImageLink(String displayName, String profileImageLink, String status, String contactNumber) {
        JSONObject jsonFromObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(displayName)) {
                jsonFromObject.put("displayName", displayName);
            }
            if (!TextUtils.isEmpty(profileImageLink)) {
                jsonFromObject.put("imageLink", profileImageLink);
            }
            if (!TextUtils.isEmpty(status)) {
                jsonFromObject.put("statusMessage", status);
            }
            if (!TextUtils.isEmpty(contactNumber)) {
                jsonFromObject.put("phoneNumber", contactNumber);
            }
            String response = httpRequestUtils.postData(getUserProfileUpdateUrl(), "application/json", "application/json", jsonFromObject.toString());
            Utils.printLog(context, TAG, response);
            return ((ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ApiResponse getUserReadServerCall() {
        String response = null;
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.getResponse(getUserReadUrl(), null, null);
            if (response != null) {
                apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            }
            Utils.printLog(context, TAG, "User read response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return apiResponse;
    }

    public String updateUserPassword(String oldPassword, String newPassword) {
        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
            return null;
        }
        String response = "";
        ApiResponse apiResponse = null;
        try {
            response = httpRequestUtils.getResponse(getUpdateUserPasswordUrl() + "?oldPassword=" + oldPassword + "&newPassword=" + newPassword, "application/json", "application/json");
            if (TextUtils.isEmpty(response)) {
                return null;
            }
            apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            if (apiResponse != null && apiResponse.isSuccess()) {
                return apiResponse.getStatus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String packageDetail(CustomerPackageDetail customerPackageDetail) {
        String response;
        String jsonFromObject = GsonUtils.getJsonFromObject(customerPackageDetail, CustomerPackageDetail.class);
        try {
            response = httpRequestUtils.postData(getApplicationInfoUrl(), "application/json", "application/json", jsonFromObject);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
