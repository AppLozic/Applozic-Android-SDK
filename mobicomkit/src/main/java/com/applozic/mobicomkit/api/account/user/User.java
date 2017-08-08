package com.applozic.mobicomkit.api.account.user;

import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by devashish on 22/12/14.
 */
public class User extends JsonMarker {

    private String userId;
    private String email;
    private String password;
    private String registrationId;
    private String applicationId;
    private String contactNumber;
    private String countryCode;
    private Short prefContactAPI = Short.valueOf("2");
    private boolean emailVerified = true;
    private String timezone;
    private Short appVersionCode;
    private String roleName = "USER";
    private Short deviceType;
    private String imageLink;
    private boolean enableEncryption;
    private Short authenticationTypeId = AuthenticationType.CLIENT.getValue();
    private String displayName;
    private String appModuleName;
    private Short userTypeId;
    private List<String> features;
    private String notificationSoundFilePath;

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emailId) {
        this.email = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Short getPrefContactAPI() {
        return prefContactAPI;
    }

    public void setPrefContactAPI(Short prefContactAPI) {
        this.prefContactAPI = prefContactAPI;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Short getAppVersionCode() {
        return appVersionCode;
    }

    public void setAppVersionCode(Short appVersionCode) {
        this.appVersionCode = appVersionCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Short getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Short deviceType) {
        this.deviceType = deviceType;
    }

    public Short getAuthenticationTypeId() {
        return authenticationTypeId;
    }

    public void setAuthenticationTypeId(Short authenticationTypeId) {
        this.authenticationTypeId = authenticationTypeId;
    }

    public String getAppModuleName() {
        return appModuleName;
    }

    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    public Short getUserTypeId() {
        return userTypeId;
    }

    public void setUserTypeId(Short userTypeId) {
        this.userTypeId = userTypeId;
    }

    public String getNotificationSoundFilePath() {
        return notificationSoundFilePath;
    }

    public void setNotificationSoundFilePath(String notificationSoundFilePath) {
        this.notificationSoundFilePath = notificationSoundFilePath;
    }

    public enum AuthenticationType {

        CLIENT(Short.valueOf("0")), APPLOZIC(Short.valueOf("1")), FACEBOOK(Short.valueOf("2"));
        private Short value;

        AuthenticationType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }

    public enum Features {

        IP_AUDIO_CALL("100"), IP_VIDEO_CALL("101");
        private String value;

        Features(String c) {
            value = c;
        }

        public String getValue() {
            return value;
        }
    }
}
