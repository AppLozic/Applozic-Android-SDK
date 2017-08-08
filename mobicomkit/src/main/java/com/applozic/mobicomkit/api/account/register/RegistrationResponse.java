package com.applozic.mobicomkit.api.account.register;

import android.text.TextUtils;

import com.applozic.mobicommons.json.JsonMarker;

/**
 * @author devashish
 */
public class RegistrationResponse extends JsonMarker {

    private String message;
    private String deviceKey;
    private String userKey;
    private String userId;
    private String contactNumber;
    private Long lastSyncTime;
    private Long currentTimeStamp;
    private String displayName;
    private String notificationResponse;
    private String brokerUrl;
    private String imageLink;
    private String statusMessage;
    private String encryptionKey;
    private boolean enableEncryption;
    private Short pricingPackage = PricingType.STARTER.getValue();

    public String getMessage() {
        return message;
    }

    ;

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKeyString) {
        this.deviceKey = deviceKeyString;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String suUserKeyString) {
        this.userKey = suUserKeyString;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public Long getLastSyncTime() {
        return lastSyncTime == null ? 0L : lastSyncTime;
    }

    public void setLastSyncTime(Long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public Long getCurrentTimeStamp() {
        return currentTimeStamp == null ? 0L : currentTimeStamp;
    }

    public void setCurrentTimeStamp(Long currentTimeStamp) {
        this.currentTimeStamp = currentTimeStamp;
    }

    public String getNotificationResponse() {
        return notificationResponse;
    }

    public void setNotificationResponse(String notificationResponse) {
        this.notificationResponse = notificationResponse;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public boolean isPasswordInvalid() {
        return (!TextUtils.isEmpty(message) && ("PASSWORD_INVALID".equals(message) || "PASSWORD_REQUIRED".equals(message)));
    }

    public Short getPricingPackage() {
        return pricingPackage;
    }

    public void setPricingPackage(Short pricingPackage) {
        this.pricingPackage = pricingPackage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isEnableEncryption() {
        return enableEncryption;
    }

    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }

    @Override
    public String toString() {
        return "RegistrationResponse{" +
                "message='" + message + '\'' +
                ", deviceKey='" + deviceKey + '\'' +
                ", userKey='" + userKey + '\'' +
                ", userId='" + userId + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", lastSyncTime=" + lastSyncTime +
                ", currentTimeStamp=" + currentTimeStamp +
                ", displayName='" + displayName + '\'' +
                ", notificationResponse='" + notificationResponse + '\'' +
                ", brokerUrl='" + brokerUrl + '\'' +
                ", imageLink='" + imageLink + '\'' +
                ", statusMessage='" + statusMessage + '\'' +
                ", encryptionKey='" + encryptionKey + '\'' +
                ", enableEncryption=" + enableEncryption +
                ", pricingPackage=" + pricingPackage +
                '}';
    }

    public static enum PricingType {

        CLOSED(Short.valueOf("-1")), BETA(Short.valueOf("0")), STARTER(Short.valueOf("1")), LAUNCH(Short.valueOf("2")), GROWTH(Short.valueOf("3")), ENTERPRISE(
                Short.valueOf("4"));
        private final Short value;

        private PricingType(Short c) {
            value = c;
        }

        public Short getValue() {
            return value;
        }
    }
}
