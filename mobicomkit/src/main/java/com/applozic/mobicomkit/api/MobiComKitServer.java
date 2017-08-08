package com.applozic.mobicomkit.api;

import android.content.Context;

/**
 * Created by devashish on 22/12/14.
 */
public class MobiComKitServer {

    public static final boolean PROD = true;
    public static final String PROD_DISPLAY_URL = "https://apps.applozic.com";
    public static final String SERVER_DEVICE_CONTACT_SYNC_URL = "/rest/ws/contact/v1/device/add";
    public static final String SERVER_CONTACT_SYNC_URL = "/rest/ws/contact/v1/add";
    public static final String FREE_MESSAGE_FAILED_URL = "/rest/ws/sms/mtext/failed?";
    public static final String CONTACT_SYNC_COMPLETE_URL = "/rest/ws/contact/syncCompleted?suUserKeyString";
    //Todo: Fix this url.
    public static final String APP_SERVER_URL = "xxx";
    //public static final String ERROR_BASE_URL = "http://onlinesmsutility.appspot.com";
    public static final String ERROR_BASE_URL = "https://osu-alpha.appspot.com";
    public static final String SUBMIT_ERROR_URL = ERROR_BASE_URL + "/rest/ws/error/submit";
    public static String SUPPORT_PHONE_NUMBER_METADATA = "com.applozic.support.phone.number";
    private Context context;

    public MobiComKitServer(Context context) {
        this.context = context;
    }
//    public static String APPLICATION_KEY_HEADER_VALUE = "c";


}
