package com.applozic.mobicomkit;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Process;
import android.text.TextUtils;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ashish on 27/03/18.
 */

public class ConversationRunnables {

    Context context;
    private static final int PRE_FETCH_MESSAGES_FOR = 6;
    private boolean isMutedList;
    private boolean isSync;
    private boolean isMessageMetadataSync;
    private static String TAG = "ConversationSyncThread";
    private MobiComMessageService mobiComMessageService;
    private Message message;

    public ConversationRunnables(Context context, Message message, boolean isMutedList, boolean isSync, boolean isMessageMetadataSync) {
        this.context = context;
        this.isSync = isSync;
        this.isMutedList = isMutedList;
        this.isMessageMetadataSync = isMessageMetadataSync;
        this.message = message;
        mobiComMessageService = new MobiComMessageService(context, MessageIntentService.class);

        startSync();
    }

    public void startSync() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                if (isMutedList) {
                    Utils.printLog(context, TAG, "Muted user list sync started from thread..");
                    try {
                        UserService.getInstance(context).getMutedUserList();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                if (isMessageMetadataSync) {
                    Utils.printLog(context, TAG, "Syncing messages service started for metadata update from thread");
                    mobiComMessageService.syncMessageForMetadataUpdate();
                    return;
                }

                Utils.printLog(context, TAG, "Syncing messages service started from thread: " + isSync);

                if (message != null) {
                    mobiComMessageService.processInstantMessage(message);
                } else {
                    if (isSync) {
                        mobiComMessageService.syncMessages();
                    } else {
                        syncConversation();
                    }
                }
            }
        });
    }

    private void syncConversation() {
        try {
            MobiComConversationService mobiComConversationService = new MobiComConversationService(context);
            List<Message> messages = mobiComConversationService.getLatestMessagesGroupByPeople();
            UserService.getInstance(context).processSyncUserBlock();

            if (Applozic.getInstance(context).isDeviceContactSync()) {
                Set<String> contactNoSet = new HashSet<String>();
                List<Contact> contacts = new AppContactService(context).getContacts(Contact.ContactType.DEVICE);
                for (Contact contact : contacts) {
                    if (!TextUtils.isEmpty(contact.getFormattedContactNumber())) {
                        contactNoSet.add(contact.getFormattedContactNumber());
                    }
                }

                if (!contactNoSet.isEmpty()) {
                    UserService userService = UserService.getInstance(context.getApplicationContext());
                    userService.processUserDetailsByContactNos(contactNoSet);
                }
                MobiComUserPreference.getInstance(context).setDeviceContactSyncTime(new Date().getTime());
            }

            for (Message message : messages.subList(0, Math.min(PRE_FETCH_MESSAGES_FOR, messages.size()))) {
                Contact contact = null;
                Channel channel = null;

                if (message.getGroupId() != null) {
                    channel = new Channel(message.getGroupId());
                } else {
                    contact = new Contact(message.getContactIds());
                }

                mobiComConversationService.getMessages(1L, null, contact, channel, null, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
