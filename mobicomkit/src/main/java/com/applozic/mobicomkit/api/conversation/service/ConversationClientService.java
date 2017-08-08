package com.applozic.mobicomkit.api.conversation.service;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.HttpRequestUtils;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.conversation.database.ConversationDatabaseService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.feed.ConversationFeed;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Conversation;

import java.net.URLEncoder;
import java.util.Set;

/**
 * Created by sunil on 18/2/16.
 */
public class ConversationClientService extends MobiComKitClientService {

    private static final String CREATE_CONVERSATION_URL = "/rest/ws/conversation/id";
    private static final String CONVERSATION_URL = "/rest/ws/conversation/topicId";
    private static final String CONVERSATION_CLOSE_TOPIC_ID_URL = "/rest/ws/conversation/closeall";
    private static final String CONVERSATION_CLOSE_URL = "/rest/ws/conversation/close";
    private static final String TAG = "ConversationClient";
    final private static String TOPIC_ID = "topicId";
    final private static String WITH_USER_ID = "withUserId";
    private static ConversationClientService conversationClientService;
    private Context context;
    private ConversationDatabaseService conversationDatabaseService;
    private HttpRequestUtils httpRequestUtils;

    private ConversationClientService(Context context) {
        super(context);
        this.context = context;
        this.httpRequestUtils = new HttpRequestUtils(context);

    }

    public synchronized static ConversationClientService getInstance(Context context) {
        if (conversationClientService == null) {
            conversationClientService = new ConversationClientService(context.getApplicationContext());
        }
        return conversationClientService;
    }

    public String getCreateConversationUrl() {
        return getBaseUrl() + CREATE_CONVERSATION_URL;
    }

    public String getConversationUrl() {
        return getBaseUrl() + CONVERSATION_URL;
    }

    public String getConversationCloseUrl() {
        return getBaseUrl() + CONVERSATION_CLOSE_URL;
    }

    public String getConversationCloseByTopicIdUrl() {
        return getBaseUrl() + CONVERSATION_CLOSE_TOPIC_ID_URL;
    }

    public ChannelFeed createConversation(Conversation conversation) {
        ChannelFeed channelFeed = null;
        try {
            String jsonFromObject = GsonUtils.getJsonFromObject(conversation, conversation.getClass());
            String createChannelResponse = httpRequestUtils.postData(getCreateConversationUrl(), "application/json", "application/json", jsonFromObject);
            Utils.printLog(context,TAG, "Create Conversation reponse:" + createChannelResponse);
            ChannelFeedApiResponse channelFeedApiResponse = (ChannelFeedApiResponse) GsonUtils.getObjectFromJson(createChannelResponse, ChannelFeedApiResponse.class);

            if (channelFeedApiResponse != null && channelFeedApiResponse.isSuccess()) {
                channelFeed = channelFeedApiResponse.getResponse();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return channelFeed;
    }

    public Conversation getConversation(Integer conversationId) {
        String response = "";
        try {
            if (conversationId != null) {
                response = httpRequestUtils.getResponse(getConversationUrl() + "?id=" + String.valueOf(conversationId), "application/json", "application/json");
                ConversationFeed apiResponse = (ConversationFeed) GsonUtils.getObjectFromJson(response, ConversationFeed.class);
                Utils.printLog(context,TAG, "Conversation response  is :" + response);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    return (Conversation) apiResponse.getResponse();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String closeConversation(Integer conversationId) {
        String response;
        try {
            if (conversationId != null) {
                response = httpRequestUtils.getResponse(getConversationCloseUrl() + "?id=" + String.valueOf(conversationId), "application/json", "application/json");
                if (TextUtils.isEmpty(response)) {
                    return null;
                }
                ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
                Utils.printLog(context,TAG, "Conversation close  API Response :" + response);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    return apiResponse.getResponse().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String closeConversationByTopicId(Set<String> topicIds, String userId) {
        String response = "";
        try {
            StringBuffer stringBuffer = new StringBuffer();
            if (topicIds != null && topicIds.size() > 0) {
                for (String topicId : topicIds) {
                    stringBuffer.append(TOPIC_ID).append("=").append(URLEncoder.encode(topicId, "UTF-8")).append("&");
                }
                stringBuffer.append(WITH_USER_ID).append("=").append(URLEncoder.encode(userId, "UTF-8"));
                response = httpRequestUtils.getResponse(getConversationCloseByTopicIdUrl() + "?" + stringBuffer.toString(), "application/json", null);
                if (TextUtils.isEmpty(response)) {
                    return null;
                }
                ApiResponse apiResponse = (ApiResponse) GsonUtils.getObjectFromJson(response, ApiResponse.class);
                Utils.printLog(context,TAG, "Conversation close by topic id :" + response);
                if (apiResponse != null && apiResponse.isSuccess()) {
                    return apiResponse.getResponse().toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
