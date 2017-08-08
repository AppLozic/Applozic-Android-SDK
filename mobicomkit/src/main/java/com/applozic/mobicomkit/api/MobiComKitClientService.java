package com.applozic.mobicomkit.api;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;

import com.applozic.mobicommons.commons.core.utils.Utils;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by devashish on 27/12/14.
 */
public class MobiComKitClientService {

    public static final String BASE_URL_METADATA = "com.applozic.server.url";
    public static final String MQTT_BASE_URL_METADATA = "com.applozic.mqtt.server.url";
    public static final String FILE_URL = "/rest/ws/aws/file/";
    public static String APPLICATION_KEY_HEADER = "Application-Key";
    public static String APP_MOUDLE_NAME_KEY_HEADER = "App-Module-Name";
    public static String APPLICATION_KEY_HEADER_VALUE_METADATA = "com.applozic.application.key";
    public static String APP_MODULE_NAME_META_DATA_KEY = "com.applozic.module.key";
    protected Context context;
    protected String DEFAULT_URL = "https://apps.applozic.com";
    protected String FILE_BASE_URL = "https://applozic.appspot.com";
    protected String DEFAULT_MQTT_URL = "tcp://apps.applozic.com:1883";

    public MobiComKitClientService() {

    }

    public MobiComKitClientService(Context context) {
        this.context = context.getApplicationContext();
    }

    public static String getApplicationKey(Context context) {
        String applicationKey = Applozic.getInstance(context).getApplicationKey();
        if (!TextUtils.isEmpty(applicationKey)) {
            return applicationKey;
        }
        return Utils.getMetaDataValue(context.getApplicationContext(), APPLICATION_KEY_HEADER_VALUE_METADATA);
    }

    public static String getAppModuleName(Context context) {

        return Utils.getMetaDataValue(context.getApplicationContext(), APP_MODULE_NAME_META_DATA_KEY);

    }

    protected String getBaseUrl() {
        String SELECTED_BASE_URL = MobiComUserPreference.getInstance(context).getUrl();

        if (!TextUtils.isEmpty(SELECTED_BASE_URL)) {
            return SELECTED_BASE_URL;
        }
        String BASE_URL = Utils.getMetaDataValue(context.getApplicationContext(), BASE_URL_METADATA);
        if (!TextUtils.isEmpty(BASE_URL)) {
            return BASE_URL;
        }

        return DEFAULT_URL;
    }

    protected String getMqttBaseUrl() {
        String MQTT_BROKER_URL = MobiComUserPreference.getInstance(context).getMqttBrokerUrl();
        if (!TextUtils.isEmpty(MQTT_BROKER_URL)) {
            return MQTT_BROKER_URL;
        }
        String MQTT_BASE_URL = Utils.getMetaDataValue(context.getApplicationContext(), MQTT_BASE_URL_METADATA);
        if (!TextUtils.isEmpty(MQTT_BASE_URL)) {
            return MQTT_BASE_URL;
        }
        return DEFAULT_MQTT_URL;
    }

    public PasswordAuthentication getCredentials() {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(context);
        if (!userPreferences.isRegistered()) {
            return null;
        }
        return new PasswordAuthentication(userPreferences.getUserId(), userPreferences.getDeviceKeyString().toCharArray());
    }

    public HttpURLConnection openHttpConnection(String urlString) throws IOException {
        HttpURLConnection httpConn;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");

        try {
            httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            /*String userCredentials = getCredentials().getUserName() + ":" + String.valueOf(getCredentials().getPassword());
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
            httpConn.setRequestProperty("Authorization", basicAuth);
            httpConn.setRequestProperty(APPLICATION_KEY_HEADER, getApplicationKey(context));

            if( getAppModuleName(context)!=null ){
                httpConn.setRequestProperty(APP_MOUDLE_NAME_KEY_HEADER, getApplicationKey(context));
            }

            httpConn.setRequestProperty(HttpRequestUtils.USERID_HEADER, HttpRequestUtils.USERID_HEADER_VALUE);*/
            httpConn.connect();
            //Shifting this Code to individual class..this is needed so that caller can decide ..what should be done with the error
//            response = httpConn.getResponseCode();
//            if (response == HttpURLConnection.HTTP_OK) {
//                in = httpConn.getInputStream();
//
//            }

        } catch (Exception ex) {
            throw new IOException("Error connecting");
        }
        return httpConn;
    }

    public String getFileUrl() {
        return FILE_BASE_URL + FILE_URL;
    }

}
