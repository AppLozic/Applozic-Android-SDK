package com.applozic.mobicomkit.api.conversation.stat;

import android.content.Context;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;

import com.applozic.mobicommons.json.GsonUtils;

/**
 * Created by devashish on 26/12/14.
 */
public class MessageStatClientService extends MobiComKitClientService {

    public static final String MESSAGE_STAT_URL = "/rest/ws/sms/stat/update";
    private static final String TAG = "MessageStatClientService";


    public MessageStatClientService(Context context) {
        super(context);
    }

    public String getMessageStatUrl() {
        return getBaseUrl() + MESSAGE_STAT_URL;
    }


    public String sendMessageStat(MessageStat messageStat) {
        try {
            return new HttpRequestUtils(context).postData(getMessageStatUrl(), "application/json", null, GsonUtils.getJsonFromObject(messageStat, MessageStat.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
