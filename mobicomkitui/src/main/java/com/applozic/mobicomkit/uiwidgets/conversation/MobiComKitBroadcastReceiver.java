package com.applozic.mobicomkit.uiwidgets.conversation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by devashish on 4/2/15.
 */
public class MobiComKitBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "MTBroadcastReceiver";

    private ConversationUIService conversationUIService;
    private BaseContactService baseContactService;

    public MobiComKitBroadcastReceiver(FragmentActivity fragmentActivity,ConversationUIService conversationUIService) {
        this.conversationUIService = conversationUIService;
        this.baseContactService = new AppContactService(fragmentActivity);
    }

    public MobiComKitBroadcastReceiver(FragmentActivity fragmentActivity) {
        this.conversationUIService = new ConversationUIService(fragmentActivity);
        this.baseContactService = new AppContactService(fragmentActivity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Message message = null;
        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        if (!TextUtils.isEmpty(messageJson)) {
            message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
        }
        Utils.printLog(context, TAG, "Received broadcast, action: " + action + ", message: " + message);

        if (message != null && !message.isSentToMany()) {
            conversationUIService.addMessage(message);
        } else if (message != null && message.isSentToMany() && BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            for (String toField : message.getTo().split(",")) {
                Message singleMessage = new Message(message);
                singleMessage.setKeyString(message.getKeyString());
                singleMessage.setTo(toField);
                singleMessage.processContactIds(context);
                conversationUIService.addMessage(message);
            }
        }

        String keyString = intent.getStringExtra("keyString");
        String userId = message != null ? message.getContactIds() : "";

        if (BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString().equals(action)) {
            InstructionUtil.showInstruction(context, intent.getIntExtra("resId", -1), intent.getBooleanExtra("actionable", false), R.color.instruction_color);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_CHANNEL_NAME.toString().equals(action)) {
            conversationUIService.updateChannelName();
        } else if (BroadcastService.INTENT_ACTIONS.FIRST_TIME_SYNC_COMPLETE.toString().equals(action)) {
            conversationUIService.downloadConversations(true);
        } else if (BroadcastService.INTENT_ACTIONS.LOAD_MORE.toString().equals(action)) {
            conversationUIService.setLoadMore(intent.getBooleanExtra("loadMore", true));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_SYNC_ACK_FROM_SERVER.toString().equals(action)) {
            conversationUIService.updateMessageKeyString(message);
        } else if (BroadcastService.INTENT_ACTIONS.SYNC_MESSAGE.toString().equals(intent.getAction())) {
            conversationUIService.syncMessages(message, keyString);
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_MESSAGE.toString().equals(intent.getAction())) {
            userId = intent.getStringExtra("contactNumbers");
            conversationUIService.deleteMessage(keyString, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY.toString().equals(action) ||
                BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED.toString().equals(action)) {
            conversationUIService.updateDeliveryStatus(message, userId);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_DELIVERY_FOR_CONTACT.toString().equals(action)) {
            conversationUIService.updateDeliveryStatusForContact(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_READ_AND_DELIVERED_FOR_CONTECT.toString().equals(action)) {
            conversationUIService.updateReadStatusForContact(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.DELETE_CONVERSATION.toString().equals(action)) {
            String contactNumber = intent.getStringExtra("contactNumber");
            Integer channelKey = intent.getIntExtra("channelKey", 0);
            String response = intent.getStringExtra("response");
            Contact contact = null;
            if (contactNumber != null) {
                contact = baseContactService.getContactById(contactNumber);
            }
            conversationUIService.deleteConversation(contact, channelKey, response);
        } else if (BroadcastService.INTENT_ACTIONS.UPLOAD_ATTACHMENT_FAILED.toString().equals(action) && message != null) {
            conversationUIService.updateUploadFailedStatus(message);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString().equals(action) && message != null) {
            conversationUIService.updateDownloadStatus(message);
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString().equals(action) && message != null) {
            conversationUIService.updateDownloadFailed(message);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TYPING_STATUS.toString().equals(action)) {
            String currentUserId = intent.getStringExtra("userId");
            String isTyping = intent.getStringExtra("isTyping");
            conversationUIService.updateTypingStatus(currentUserId, isTyping);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_LAST_SEEN_AT_TIME.toString().equals(action)) {
            conversationUIService.updateLastSeenStatus(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MQTT_DISCONNECTED.toString().equals(action)) {
            conversationUIService.reconnectMQTT();
        } else if (BroadcastService.INTENT_ACTIONS.CHANNEL_SYNC.toString().equals(action)) {
            boolean isMetaDataUpdate = intent.getBooleanExtra("isMetadataUpdate", false);
            conversationUIService.updateChannelSync(isMetaDataUpdate);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_TITLE_SUBTITLE.toString().equals(action)) {
            conversationUIService.updateTitleAndSubtitle();
        } else if (BroadcastService.INTENT_ACTIONS.CONVERSATION_READ.toString().equals(action)) {
            String currentId = intent.getStringExtra("currentId");
            boolean isGroup = intent.getBooleanExtra("isGroup", false);
            conversationUIService.updateConversationRead(currentId, isGroup);
        } else if (BroadcastService.INTENT_ACTIONS.UPDATE_USER_DETAIL.toString().equals(action)) {
            conversationUIService.updateUserInfo(intent.getStringExtra("contactId"));
        } else if (BroadcastService.INTENT_ACTIONS.MESSAGE_METADATA_UPDATE.toString().equals(action)) {
            conversationUIService.updateMessageMetadata(keyString);
        } else if (BroadcastService.INTENT_ACTIONS.MUTE_USER_CHAT.toString().equals(action)) {
            conversationUIService.muteUserChat(intent.getBooleanExtra("mute", false), intent.getStringExtra("userId"));
        }
    }
}
