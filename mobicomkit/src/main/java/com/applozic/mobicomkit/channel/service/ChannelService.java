package com.applozic.mobicomkit.channel.service;

import android.content.Context;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.notification.MuteNotificationRequest;
import com.applozic.mobicomkit.api.people.ChannelInfo;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.database.ChannelDatabaseService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.ChannelFeed;
import com.applozic.mobicomkit.feed.ChannelFeedApiResponse;
import com.applozic.mobicomkit.feed.ChannelFeedListResponse;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.sync.SyncChannelFeed;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUserMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sunil on 1/1/16.
 */
public class ChannelService {

    public static boolean isUpdateTitle = false;
    private static ChannelService channelService;
    public Context context;
    private ChannelDatabaseService channelDatabaseService;
    private ChannelClientService channelClientService;
    private BaseContactService baseContactService;
    private UserService userService;

    private ChannelService(Context context) {
        this.context = context;
        channelClientService = ChannelClientService.getInstance(context);
        channelDatabaseService = ChannelDatabaseService.getInstance(context);
        userService = UserService.getInstance(context);
        baseContactService = new AppContactService(context);
    }

    public synchronized static ChannelService getInstance(Context context) {
        if (channelService == null) {
            channelService = new ChannelService(context.getApplicationContext());
        }
        return channelService;
    }

    public Channel getChannelInfoFromLocalDb(Integer key) {
        return channelDatabaseService.getChannelByChannelKey(key);
    }

    public Channel getChannelInfo(Integer key) {
        if (key == null) {
            return null;
        }
        Channel channel = channelDatabaseService.getChannelByChannelKey(key);
        if (channel == null) {
            ChannelFeed channelFeed = channelClientService.getChannelInfo(key);
            if (channelFeed != null) {
                channelFeed.setUnreadCount(0);
                ChannelFeed[] channelFeeds = new ChannelFeed[1];
                channelFeeds[0] = channelFeed;
                processChannelFeedList(channelFeeds, false);
                //BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.UPDATE_CHANNEL_NAME.toString());
                channel = getChannel(channelFeed);
                return channel;
            }
        }
        return channel;
    }

    public Channel getChannelInfo(String clientGroupId) {
        if (TextUtils.isEmpty(clientGroupId)) {
            return null;
        }
        Channel channel = channelDatabaseService.getChannelByClientGroupId(clientGroupId);
        if (channel == null) {
            ChannelFeed channelFeed = channelClientService.getChannelInfo(clientGroupId);
            if (channelFeed != null) {
                channelFeed.setUnreadCount(0);
                ChannelFeed[] channelFeeds = new ChannelFeed[1];
                channelFeeds[0] = channelFeed;
                processChannelFeedList(channelFeeds, false);
                channel = getChannel(channelFeed);
                return channel;
            }
        }
        return channel;
    }

    public void createMultipleChannels(List<ChannelInfo> channelInfo) {
        List<ChannelFeed> channelFeeds = channelClientService.createMultipleChannels(channelInfo);
        if (channelFeeds != null) {
            processChannelList(channelFeeds);
        }
    }

    public void processChannelFeedList(ChannelFeed[] channelFeeds, boolean isUserDetails) {
        if (channelFeeds != null && channelFeeds.length > 0) {
            for (ChannelFeed channelFeed : channelFeeds) {
                processChannelFeed(channelFeed, isUserDetails);
            }
        }
    }

    public void processChannelFeed(ChannelFeed channelFeed, boolean isUserDetails) {
        if (channelFeed != null) {
            Set<String> memberUserIds = null;
            if (channelFeed.getMembersName() != null) {
                memberUserIds = channelFeed.getMembersName();
            } else {
                memberUserIds = channelFeed.getContactGroupMembersId();
            }

            Channel channel = getChannel(channelFeed);
            if (channelDatabaseService.isChannelPresent(channel.getKey())) {
                channelDatabaseService.updateChannel(channel);
            } else {
                channelDatabaseService.addChannel(channel);
            }
            if (channelFeed.getConversationPxy() != null) {
                channelFeed.getConversationPxy().setGroupId(channelFeed.getId());
                ConversationService.getInstance(context).addConversation(channelFeed.getConversationPxy());
            }
            if (memberUserIds != null && memberUserIds.size() > 0) {
                for (String userId : memberUserIds) {
                    ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelFeed.getId(), userId);
                    if (channelDatabaseService.isChannelUserPresent(channelFeed.getId(), userId)) {
                        channelDatabaseService.updateChannelUserMapper(channelUserMapper);
                    } else {
                        channelDatabaseService.addChannelUserMapper(channelUserMapper);
                    }
                }
            }

            if (isUserDetails) {
                userService.processUserDetail(channelFeed.getUsers());
            }
        }
    }

    public synchronized Channel getChannelByChannelKey(Integer channelKey) {
        if (channelKey == null) {
            return null;
        }
        return channelDatabaseService.getChannelByChannelKey(channelKey);
    }


    public List<ChannelUserMapper> getListOfUsersFromChannelUserMapper(Integer channelKey) {
        return channelDatabaseService.getChannelUserList(channelKey);
    }

    public Channel getChannel(Integer channelKey) {
        Channel channel = channelDatabaseService.getChannelByChannelKey(channelKey);
        if (channel == null) {
            channel = new Channel(channelKey);
        }
        return channel;
    }

    public void updateChannel(Channel channel) {
        if (channelDatabaseService.getChannelByChannelKey(channel.getKey()) == null) {
            channelDatabaseService.addChannel(channel);
        } else {
            channelDatabaseService.updateChannel(channel);
        }
    }

    public List<Channel> getChannelList() {
        return channelDatabaseService.getAllChannels();
    }

    public synchronized void syncChannels() {
        final MobiComUserPreference userpref = MobiComUserPreference.getInstance(context);
        SyncChannelFeed syncChannelFeed = channelClientService.getChannelFeed(userpref.getChannelSyncTime());
        if (syncChannelFeed == null) {
            return;
        }
        if (syncChannelFeed.isSuccess()) {
            processChannelList(syncChannelFeed.getResponse());
            BroadcastService.sendUpdate(context, BroadcastService.INTENT_ACTIONS.CHANNEL_SYNC.toString());
        }
        userpref.setChannelSyncTime(syncChannelFeed.getGeneratedAt());

    }

    public synchronized Channel createChannel(final ChannelInfo channelInfo) {
        Channel channel = null;
        ChannelFeed channelFeed = channelClientService.createChannel(channelInfo);
        if (channelFeed != null) {
            ChannelFeed[] channelFeeds = new ChannelFeed[1];
            channelFeeds[0] = channelFeed;
            processChannelFeedList(channelFeeds, true);
            channel = getChannel(channelFeed);
        }
        return channel;
    }

    public Channel getChannel(ChannelFeed channelFeed) {
        Channel channel = new Channel(channelFeed.getId(), channelFeed.getName(), channelFeed.getAdminName(), channelFeed.getType(), channelFeed.getUnreadCount(), channelFeed.getImageUrl());
        channel.setClientGroupId(channelFeed.getClientGroupId());
        channel.setNotificationAfterTime(channelFeed.getNotificationAfterTime());
        channel.setDeletedAtTime(channelFeed.getDeletedAtTime());
        channel.setMetadata(channelFeed.getMetadata());
        return channel;
    }

    public String removeMemberFromChannelProcess(Integer channelKey, String userId) {
        if (channelKey == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.removeMemberFromChannel(channelKey, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.removeMemberFromChannel(channelKey, userId);
        }
        return apiResponse.getStatus();
    }


    public String removeMemberFromChannelProcess(String clientGroupId, String userId) {
        if (clientGroupId == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.removeMemberFromChannel(clientGroupId, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.removeMemberFromChannel(clientGroupId, userId);
        }
        return apiResponse.getStatus();

    }

    public String addMemberToChannelProcess(Integer channelKey, String userId) {
        if (channelKey == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(channelKey, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelKey, userId);
            channelDatabaseService.addChannelUserMapper(channelUserMapper);
        }
        return apiResponse.getStatus();
    }

    public String addMemberToChannelProcess(String clientGroupId, String userId) {
        if (TextUtils.isEmpty(clientGroupId) && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(clientGroupId, userId);
        if (apiResponse == null) {
            return null;
        }
        return apiResponse.getStatus();
    }

    public ApiResponse addMemberToChannelProcessWithResponse(String clientGroupId, String userId) {
        if (TextUtils.isEmpty(clientGroupId) && TextUtils.isEmpty(userId)) {
            return null;
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(clientGroupId, userId);
        if (apiResponse == null) {
            return null;
        }
        return apiResponse;
    }

    public ApiResponse addMemberToChannelProcessWithResponse(Integer channelKey, String userId) {
        if (channelKey == null && TextUtils.isEmpty(userId)) {
            return null;
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(channelKey, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelKey, userId);
            channelDatabaseService.addChannelUserMapper(channelUserMapper);
        }
        return apiResponse;
    }

    public String addMemberToMultipleChannelsProcess(Set<String> clientGroupIds, String userId) {
        if (clientGroupIds == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.addMemberToMultipleChannelsByClientGroupIds(clientGroupIds, userId);
        if (apiResponse == null) {
            return null;
        }
        return apiResponse.getStatus();
    }

    public String addMemberToMultipleChannelsProcessByChannelKeys(Set<Integer> channelKeys, String userId) {
        if (channelKeys == null && TextUtils.isEmpty(userId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.addMemberToMultipleChannelsByChannelKey(channelKeys, userId);
        if (apiResponse == null) {
            return null;
        }
        return apiResponse.getStatus();
    }

    public String leaveMemberFromChannelProcess(String clientGroupId, String userId) {
        if (TextUtils.isEmpty(clientGroupId)) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.leaveMemberFromChannel(clientGroupId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.leaveMemberFromChannel(clientGroupId, userId);
        }
        return apiResponse.getStatus();
    }

    public String leaveMemberFromChannelProcess(Integer channelKey, String userId) {
        if (channelKey == null) {
            return "";
        }
        ApiResponse apiResponse = channelClientService.leaveMemberFromChannel(channelKey);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.leaveMemberFromChannel(channelKey, userId);
        }
        return apiResponse.getStatus();
    }

    public String updateChannel(GroupInfoUpdate groupInfoUpdate) {
        if (groupInfoUpdate == null) {
            return null;
        }
        ApiResponse apiResponse = channelClientService.updateChannel(groupInfoUpdate);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.updateChannel(groupInfoUpdate);
        }
        return apiResponse.getStatus();
    }


    public synchronized void processChannelList(List<ChannelFeed> channelFeedList) {
        if (channelFeedList != null && channelFeedList.size() > 0) {
            for (ChannelFeed channelFeed : channelFeedList) {
                Set<String> memberUserIds = channelFeed.getMembersName();
                Set<String> userIds = new HashSet<>();
                Channel channel = getChannel(channelFeed);
                if (channelDatabaseService.isChannelPresent(channel.getKey())) {
                    channelDatabaseService.updateChannel(channel);
                    channelDatabaseService.deleteChannelUserMappers(channel.getKey());
                } else {
                    channelDatabaseService.addChannel(channel);
                }
                if (memberUserIds != null && memberUserIds.size() > 0) {
                    for (String userId : memberUserIds) {
                        ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelFeed.getId(), userId);
                        channelDatabaseService.addChannelUserMapper(channelUserMapper);
                        if (!baseContactService.isContactExists(userId)) {
                            userIds.add(userId);
                        }
                    }
                    if (userIds != null && userIds.size() > 0) {
                        userService.processUserDetailsByUserIds(userIds);
                    }
                }
            }
        }
    }

    public synchronized boolean processIsUserPresentInChannel(Integer channelKey) {
        return channelDatabaseService.isChannelUserPresent(channelKey, MobiComUserPreference.getInstance(context).getUserId());
    }

    public synchronized boolean isUserAlreadyPresentInChannel(Integer channelKey, String userId) {
        return channelDatabaseService.isChannelUserPresent(channelKey, userId);
    }

    public synchronized boolean processIsUserPresentInChannel(String clientGroupId) {
        Channel channel = channelDatabaseService.getChannelByClientGroupId(clientGroupId);
        return channelDatabaseService.isChannelUserPresent(channel.getKey(), MobiComUserPreference.getInstance(context).getUserId());
    }

    public synchronized boolean isUserAlreadyPresentInChannel(String clientGroupId, String userId) {
        Channel channel = channelDatabaseService.getChannelByClientGroupId(clientGroupId);
        return channelDatabaseService.isChannelUserPresent(channel.getKey(), userId);
    }

    public synchronized String processChannelDeleteConversation(Channel channel, Context context) {
        String response = new MobiComConversationService(context).deleteSync(null, channel, null);
        if (!TextUtils.isEmpty(response) && "success".equals(response)) {
            channelDatabaseService.deleteChannelUserMappers(channel.getKey());
            channelDatabaseService.deleteChannel(channel.getKey());
        }
        return response;

    }

    public void updateChannelLocalImageURI(Integer channelKey, String localImageURI) {
        channelDatabaseService.updateChannelLocalImageURI(channelKey, localImageURI);
    }

    public ApiResponse muteNotifications(MuteNotificationRequest muteNotificationRequest) {

        ApiResponse apiResponse = channelClientService.muteNotification(muteNotificationRequest);

        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            channelDatabaseService.updateNotificationAfterTime(muteNotificationRequest.getId(), muteNotificationRequest.getNotificationAfterTime());
        }
        return apiResponse;
    }

    public Channel getChannelByClientGroupId(String clientGroupId) {
        if (TextUtils.isEmpty(clientGroupId)) {
            return null;
        }
        return channelDatabaseService.getChannelByClientGroupId(clientGroupId);
    }

    public ChannelFeedApiResponse createChannelWithResponse(ChannelInfo channelInfo) {
        ChannelFeedApiResponse channelFeedApiResponse = channelClientService.createChannelWithResponse(channelInfo);
        if (channelFeedApiResponse == null) {
            return null;
        }
        if (channelFeedApiResponse.isSuccess()) {
            ChannelFeed channelFeed = channelFeedApiResponse.getResponse();
            if (channelFeed != null) {
                ChannelFeed[] channelFeeds = new ChannelFeed[1];
                channelFeeds[0] = channelFeed;
                processChannelFeedList(channelFeeds, true);
            }
        }
        return channelFeedApiResponse;
    }

    public ApiResponse addMemberToChannelWithResponseProcess(Integer channelKey, String userId) {
        if (channelKey == null && TextUtils.isEmpty(userId)) {
            return null;
        }
        ApiResponse apiResponse = channelClientService.addMemberToChannel(channelKey, userId);
        if (apiResponse == null) {
            return null;
        }
        if (apiResponse.isSuccess()) {
            ChannelUserMapper channelUserMapper = new ChannelUserMapper(channelKey, userId);
            channelDatabaseService.addChannelUserMapper(channelUserMapper);
        }
        return apiResponse;
    }

    public String getGroupOfTwoReceiverUserId(Integer channelKey) {
        return channelDatabaseService.getGroupOfTwoReceiverId(channelKey);
    }

    public Channel createGroupOfTwo(ChannelInfo channelInfo) {
        if (channelInfo == null) {
            return null;
        }
        if (!TextUtils.isEmpty(channelInfo.getClientGroupId())) {
            Channel channel = channelDatabaseService.getChannelByClientGroupId(channelInfo.getClientGroupId());
            if (channel != null) {
                return channel;
            } else {
                ChannelFeedApiResponse channelFeedApiResponse = channelClientService.createChannelWithResponse(channelInfo);
                if (channelFeedApiResponse == null) {
                    return null;
                }
                if (channelFeedApiResponse.isSuccess()) {
                    ChannelFeed channelFeed = channelFeedApiResponse.getResponse();
                    if (channelFeed != null) {
                        ChannelFeed[] channelFeeds = new ChannelFeed[1];
                        channelFeeds[0] = channelFeed;
                        processChannelFeedList(channelFeeds, true);
                        return getChannel(channelFeed);
                    }
                } else {
                    ChannelFeed channelFeed = channelClientService.getChannelInfo(channelInfo.getClientGroupId());
                    if (channelFeed != null) {
                        ChannelFeed[] channelFeeds = new ChannelFeed[1];
                        channelFeeds[0] = channelFeed;
                        processChannelFeedList(channelFeeds, false);
                        return getChannel(channelFeed);
                    }
                }
            }
        }
        return null;
    }

    public List<ChannelFeed> getGroupInfoFromGroupIds(List<String> groupIds) {
        return getGroupInfoFromGroupIds(groupIds, null);
    }

    public List<ChannelFeed> getGroupInfoFromClientGroupIds(List<String> clientGroupIds) {
        return getGroupInfoFromGroupIds(null, clientGroupIds);
    }

    public List<ChannelFeed> getGroupInfoFromGroupIds(List<String> groupIds, List<String> clientGroupIds) {

        ChannelFeedListResponse channelFeedList = channelClientService.getGroupInfoFromGroupIds(groupIds, clientGroupIds);

        if (channelFeedList == null) {
            return null;
        }

        if (channelFeedList != null && ChannelFeedListResponse.SUCCESS.equals(channelFeedList.getStatus())) {
            processChannelFeedList(channelFeedList.getResponse().toArray(new ChannelFeed[channelFeedList.getResponse().size()]), false);
        }

        return channelFeedList.getResponse();
    }

    public boolean addMemberToContactGroup(String contactGroupId, String groupType, List<String> contactGroupMemberList) {

        ApiResponse apiResponse = null;
        if(!TextUtils.isEmpty(contactGroupId) && contactGroupMemberList!=null) {
            if (!TextUtils.isEmpty(groupType)) {
                apiResponse = channelClientService.addMemberToContactGroupOfType(contactGroupId, groupType, contactGroupMemberList);

            } else {
                apiResponse = channelClientService.addMemberToContactGroup(contactGroupId, contactGroupMemberList);
            }
        }

        if (apiResponse == null) {
            return false;
        }
        return apiResponse.isSuccess();
    }

    public ChannelFeed getMembersFromContactGroup(String contactGroupId, String groupType) {
        ChannelFeed channelFeed = null;
        if(!TextUtils.isEmpty(contactGroupId)) {
            if (!TextUtils.isEmpty(groupType)) {
                channelFeed = channelClientService.getMembersFromContactGroupOfType(contactGroupId, groupType);
            } else {
                channelFeed = channelClientService.getMembersFromContactGroup(contactGroupId);
            }
        }
        if (channelFeed != null) {
            ChannelFeed[] channelFeeds = new ChannelFeed[1];
            channelFeeds[0] = channelFeed;
            processChannelFeedList(channelFeeds, false);
            return channelFeed;
        }
        return null;
    }
}
