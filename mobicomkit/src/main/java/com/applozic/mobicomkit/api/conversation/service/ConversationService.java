package com.applozic.mobicomkit.api.conversation.service;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.conversation.database.ConversationDatabaseService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.List;

/**
 * Created by sunil on 18/2/16.
 */
public class ConversationService {

    private static ConversationService conversationService;
    private Context context;
    private ConversationDatabaseService conversationDatabaseService;
    private ConversationClientService conversationClientService;

    private ConversationService(Context context) {
        this.context = context;
        conversationDatabaseService = ConversationDatabaseService.getInstance(context);
        conversationClientService = ConversationClientService.getInstance(context);

    }

    public synchronized static ConversationService getInstance(Context context) {
        if (conversationService == null) {
            conversationService = new ConversationService(context.getApplicationContext());
        }
        return conversationService;
    }

    public synchronized void processConversationArray(Conversation[] conversations, Channel channel, Contact contact) {

        if (conversations != null && conversations.length > 0) {
            for (Conversation conversation : conversations) {
                if (channel != null) {
                    conversation.setGroupId(channel.getKey());
                } else if (contact != null) {
                    conversation.setUserId(contact.getUserId());
                    conversation.setGroupId(0);
                }
                if (conversationDatabaseService.isConversationPresent(conversation.getId())) {
                    conversationDatabaseService.updateConversation(conversation);
                } else {
                    conversationDatabaseService.addConversation(conversation);
                }
            }
        }
    }

    public synchronized Conversation getConversationByConversationId(Integer conversationId) {
        return conversationDatabaseService.getConversationByConversationId(conversationId);
    }


    public synchronized List<Conversation> getConversationList(Channel channel, Contact contact) {
        return conversationDatabaseService.getConversationList(channel, contact);
    }


    public synchronized void addConversation(Conversation conversation) {
        if (conversation != null) {
            if (conversationDatabaseService.isConversationPresent(conversation.getId())) {
                conversationDatabaseService.updateConversation(conversation);
            } else {
                conversationDatabaseService.addConversation(conversation);
            }
        }
    }

    public synchronized Integer createConversation(Conversation conversation) {
        ChannelFeed channelFeed = conversationClientService.createConversation(conversation);
        if (channelFeed != null) {
            if (conversation.getSupportIds() != null) {
                ChannelFeed[] channelFeeds = new ChannelFeed[1];
                channelFeeds[0] = channelFeed;
                ChannelService.getInstance(context).processChannelFeedList(channelFeeds, false);
                ;
            }
            if (channelFeed.getConversationPxy() != null) {
                addConversation(channelFeed.getConversationPxy());
                return channelFeed.getConversationPxy().getId();
            }
        }
        return null;
    }

    public synchronized void getConversation(Integer conversationId) {
        if (!conversationDatabaseService.isConversationPresent(conversationId)) {
            Conversation conversation = conversationClientService.getConversation(conversationId);
            if (conversation != null) {
                conversationDatabaseService.addConversation(conversation);
            }
        }
        return;
    }

    public synchronized void deleteConversation(String userId) {
        conversationDatabaseService.deleteConversation(userId);
    }

    public synchronized Integer isConversationExist(String userId, String topicId) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(topicId)) {
            return null;
        }
        return conversationDatabaseService.isConversationExit(userId, topicId);
    }

    public void updateTopicLocalImageUri(String imageUri, Integer conversationId) {
        conversationDatabaseService.updateTopicLocalImageUri(imageUri, conversationId);
    }
}
