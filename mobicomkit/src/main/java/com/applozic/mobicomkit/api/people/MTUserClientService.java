package com.applozic.mobicomkit.api.people;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * Created by devashish on 23/12/14.
 */
public class MTUserClientService extends MobiComKitClientService {

    public static final String CHECK_FOR_MT_USER = "/rest/ws/contact/v2/ismtexter";

    public MTUserClientService(Context context) {
        super(context);
    }

    public String getCheckForMtUser() {
        return getBaseUrl() + CHECK_FOR_MT_USER;
    }

    public ContactContent getContactContent(String contactNumber) {
        String response = null;
        try {
            response = new HttpRequestUtils(context).getResponse(getCheckForMtUser() + "?requestSource=1&contactNumber=" + URLEncoder.encode(contactNumber, "UTF-8"), "text/plain", "application/json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(response)) {
            Gson gson = new Gson();
            return gson.fromJson(response, ContactContent.class);
        }
        return null;
    }
}
