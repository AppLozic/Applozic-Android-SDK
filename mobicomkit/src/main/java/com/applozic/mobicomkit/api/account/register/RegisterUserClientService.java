package com.applozic.mobicomkit.api.account.register;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.ConversationIntentService;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.exception.InvalidApplicationException;
import com.applozic.mobicomkit.exception.UnAuthoriseException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;

import java.net.ConnectException;
import java.util.TimeZone;

/**
 * Created by devashish on 2/2/15.
 */
public class RegisterUserClientService extends MobiComKitClientService {

    public static final String CREATE_ACCOUNT_URL = "/rest/ws/register/client?";
    public static final String UPDATE_ACCOUNT_URL = "/rest/ws/register/update?";
    public static final String CHECK_PRICING_PACKAGE = "/rest/ws/application/pricing/package";
    public static final Short MOBICOMKIT_VERSION_CODE = 109;
    private static final String TAG = "RegisterUserClient";
    private static final String INVALID_APP_ID = "INVALID_APPLICATIONID";
    private HttpRequestUtils httpRequestUtils;

    public RegisterUserClientService(Context context) {
        this.context = context.getApplicationContext();
        this.httpRequestUtils = new HttpRequestUtils(context);
    }

    public String getCreateAccountUrl() {
        return getBaseUrl() + CREATE_ACCOUNT_URL;
    }

    public String getPricingPackageUrl() {
        return getBaseUrl() + CHECK_PRICING_PACKAGE;
    }

    public String getUpdateAccountUrl() {
        return getBaseUrl() + UPDATE_ACCOUNT_URL;
    }


//    final RegistrationResponse registrationResponse = createAccount(user);
//    Intent intent = new Intent(context, ApplozicMqttIntentService.class);
//    intent.putExtra(ApplozicMqttIntentService.CONNECTED_PUBLISH,true);
//    context.startService(intent);
//    return registrationResponse;


    public RegistrationResponse createAccount(User user) throws Exception {

        user.setDeviceType(Short.valueOf("1"));
        user.setPrefContactAPI(Short.valueOf("2"));
        user.setTimezone(TimeZone.getDefault().getID());
        user.setEnableEncryption(user.isEnableEncryption());


        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        Gson gson = new Gson();
        user.setAppVersionCode(MOBICOMKIT_VERSION_CODE);
        user.setApplicationId(getApplicationKey(context));
        user.setRegistrationId(mobiComUserPreference.getDeviceRegistrationId());

        if (getAppModuleName(context) != null) {
            user.setAppModuleName(getAppModuleName(context));
        }

        Utils.printLog(context,TAG, "Net status" + Utils.isInternetAvailable(context.getApplicationContext()));

        if (!Utils.isInternetAvailable(context.getApplicationContext())) {
            throw new ConnectException("No Internet Connection");
        }

//        Log.i(TAG, "App Id is: " + getApplicationKey(context));
        Utils.printLog(context,TAG, "Registration json " + gson.toJson(user));
        String response = httpRequestUtils.postJsonToServer(getCreateAccountUrl(), gson.toJson(user));

        Utils.printLog(context,TAG, "Registration response is: " + response);

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw new Exception("503 Service Unavailable");
//            return null;
        }
        if (response.contains(INVALID_APP_ID)) {
            throw new InvalidApplicationException("Invalid Application Id");
        }
        final RegistrationResponse registrationResponse = gson.fromJson(response, RegistrationResponse.class);

        if (registrationResponse.isPasswordInvalid()) {
            throw new UnAuthoriseException("Invalid uername/password");

        }
        Utils.printLog(context,"Registration response ", "is " + registrationResponse);
        if (registrationResponse.getNotificationResponse() != null) {
            Utils.printLog(context,"Registration response ", "" + registrationResponse.getNotificationResponse());
        }
        mobiComUserPreference.setEncryptionKey(registrationResponse.getEncryptionKey());
        mobiComUserPreference.enableEncryption(user.isEnableEncryption());
        mobiComUserPreference.setCountryCode(user.getCountryCode());
        mobiComUserPreference.setUserId(user.getUserId());
        mobiComUserPreference.setContactNumber(user.getContactNumber());
        mobiComUserPreference.setEmailVerified(user.isEmailVerified());
        mobiComUserPreference.setDisplayName(user.getDisplayName());
        mobiComUserPreference.setMqttBrokerUrl(registrationResponse.getBrokerUrl());
        mobiComUserPreference.setDeviceKeyString(registrationResponse.getDeviceKey());
        mobiComUserPreference.setEmailIdValue(user.getEmail());
        mobiComUserPreference.setImageLink(user.getImageLink());
        mobiComUserPreference.setSuUserKeyString(registrationResponse.getUserKey());
        mobiComUserPreference.setLastSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
        mobiComUserPreference.setLastSeenAtSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
        mobiComUserPreference.setChannelSyncTime(String.valueOf(registrationResponse.getCurrentTimeStamp()));
        mobiComUserPreference.setUserBlockSyncTime("10000");
        mobiComUserPreference.setPassword(user.getPassword());
        mobiComUserPreference.setPricingPackage(registrationResponse.getPricingPackage());
        mobiComUserPreference.setAuthenticationType(String.valueOf(user.getAuthenticationTypeId()));
        if (user.getUserTypeId() != null) {
            mobiComUserPreference.setUserTypeId(String.valueOf(user.getUserTypeId()));
        }
        if(!TextUtils.isEmpty(user.getNotificationSoundFilePath())){
            mobiComUserPreference.setNotificationSoundFilePath(user.getNotificationSoundFilePath());
        }
        Contact contact = new Contact();
        contact.setUserId(user.getUserId());
        contact.setFullName(registrationResponse.getDisplayName());
        contact.setImageURL(registrationResponse.getImageLink());
        contact.setContactNumber(registrationResponse.getContactNumber());
        if (user.getUserTypeId() != null) {
            contact.setUserTypeId(user.getUserTypeId());
        }
        contact.setStatus(registrationResponse.getStatusMessage());
        contact.processContactNumbers(context);
        new AppContactService(context).upsert(contact);

        Intent conversationIntentService = new Intent(context, ConversationIntentService.class);
        conversationIntentService.putExtra(ConversationIntentService.SYNC, false);
        context.startService(conversationIntentService);

        Intent intent = new Intent(context, ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.CONNECTED_PUBLISH, true);
        context.startService(intent);
        return registrationResponse;
    }


    public RegistrationResponse createAccount(String email, String userId, String phoneNumber, String displayName, String imageLink, String pushNotificationId) throws Exception {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);
        String url = mobiComUserPreference.getUrl();
        mobiComUserPreference.clearAll();
        mobiComUserPreference.setUrl(url);

        return updateAccount(email, userId, phoneNumber, displayName, imageLink, pushNotificationId);
    }

    private RegistrationResponse updateAccount(String email, String userId, String phoneNumber, String displayName, String imageLink, String pushNotificationId) throws Exception {
        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        user.setImageLink(imageLink);
        user.setRegistrationId(pushNotificationId);
        user.setDisplayName(displayName);

        //user.setCountryCode(mobiComUserPreference.getCountryCode());
       /*if (!TextUtils.isEmpty(phoneNumber)) {
            try {

                    user.setCountryCode(PhoneNumberUtil.getInstance().getRegionCodeForNumber(PhoneNumberUtil.getInstance().parse(phoneNumber, "")));
                    mobiComUserPreference.setCountryCode(user.getCountryCode());

            } catch (NumberParseException e) {
                e.printStackTrace();
            }
        }*/
        user.setContactNumber(phoneNumber);

        final RegistrationResponse registrationResponse = createAccount(user);
        Intent intent = new Intent(context, ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.CONNECTED_PUBLISH, true);
        context.startService(intent);
        return registrationResponse;
    }

    public RegistrationResponse updatePushNotificationId(final String pushNotificationId) throws Exception {
        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);
        //Note: In case if gcm registration is done before login then only updating in pref

        RegistrationResponse registrationResponse = null;
        User user = getUserDetail();

        if (!TextUtils.isEmpty(pushNotificationId)) {
            pref.setDeviceRegistrationId(pushNotificationId);
        }
        user.setRegistrationId(pushNotificationId);
        if (pref.isRegistered()) {
            registrationResponse = updateRegisteredAccount(user);
        }
        return registrationResponse;
    }


    public RegistrationResponse updateRegisteredAccount(User user) throws Exception {
        RegistrationResponse registrationResponse = null;

        user.setDeviceType(Short.valueOf("1"));
        user.setPrefContactAPI(Short.valueOf("2"));
        user.setTimezone(TimeZone.getDefault().getID());

        MobiComUserPreference mobiComUserPreference = MobiComUserPreference.getInstance(context);

        Gson gson = new Gson();
        user.setEnableEncryption(mobiComUserPreference.isEncryptionEnabled());
        user.setAppVersionCode(MOBICOMKIT_VERSION_CODE);
        user.setApplicationId(getApplicationKey(context));
        user.setAuthenticationTypeId(Short.valueOf(mobiComUserPreference.getAuthenticationType()));
        if (!TextUtils.isEmpty(mobiComUserPreference.getUserTypeId())) {
            user.setUserTypeId(Short.valueOf(mobiComUserPreference.getUserTypeId()));
        }
        if (getAppModuleName(context) != null) {
            user.setAppModuleName(getAppModuleName(context));
        }
        if (!TextUtils.isEmpty(mobiComUserPreference.getDeviceRegistrationId())) {
            user.setRegistrationId(mobiComUserPreference.getDeviceRegistrationId());
        }
        Utils.printLog(context,TAG, "Registration update json " + gson.toJson(user));
        String response = httpRequestUtils.postJsonToServer(getUpdateAccountUrl(), gson.toJson(user));

        if (TextUtils.isEmpty(response) || response.contains("<html")) {
            throw null;
        }
        if (response.contains(INVALID_APP_ID)) {
            throw new InvalidApplicationException("Invalid Application Id");
        }

        registrationResponse = gson.fromJson(response, RegistrationResponse.class);

        if (registrationResponse.isPasswordInvalid()) {
            throw new UnAuthoriseException("Invalid uername/password");
        }

        Utils.printLog(context,TAG, "Registration update response: " + registrationResponse);
        mobiComUserPreference.setPricingPackage(registrationResponse.getPricingPackage());
        if (registrationResponse.getNotificationResponse() != null) {
            Utils.printLog(context,TAG, "Notification response: " + registrationResponse.getNotificationResponse());
        }

        return registrationResponse;

    }

    private User getUserDetail() {

        MobiComUserPreference pref = MobiComUserPreference.getInstance(context);

        User user = new User();
        user.setEmail(pref.getEmailIdValue());
        user.setUserId(pref.getUserId());
        user.setContactNumber(pref.getContactNumber());
        user.setDisplayName(pref.getDisplayName());
        user.setImageLink(pref.getImageLink());
        return user;
    }

    public void syncAccountStatus() {
        try {
            String response = httpRequestUtils.getResponse(getPricingPackageUrl(), "application/json", "application/json");
            Utils.printLog(context,TAG, "Pricing package response: " + response);
            ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
            if (apiResponse.getResponse() != null) {
                int pricingPackage = Integer.parseInt(apiResponse.getResponse().toString());
                MobiComUserPreference.getInstance(context).setPricingPackage(pricingPackage);
            }
        } catch (Exception e) {
            Utils.printLog(context,TAG, "Account status sync call failed");
        }
    }

}
