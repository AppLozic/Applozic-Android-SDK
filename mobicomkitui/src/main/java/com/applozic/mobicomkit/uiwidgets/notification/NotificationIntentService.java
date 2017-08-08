package com.applozic.mobicomkit.uiwidgets.notification;

import android.app.IntentService;
import android.content.Intent;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.notification.NotificationService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;


public class NotificationIntentService extends IntentService {
    public static final String ACTION_AL_NOTIFICATION = "com.applozic.mobicomkit.api.notification.action.NOTIFICATION";

    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_AL_NOTIFICATION.equals(action)) {
                Message message = (Message) intent.getSerializableExtra(MobiComKitConstants.AL_MESSAGE);
                int notificationId = Utils.getLauncherIcon(getApplicationContext());
                final NotificationService notificationService =
                        new NotificationService(notificationId == 0 ? R.drawable.mobicom_ic_launcher : notificationId, NotificationIntentService.this, R.string.wearable_action_label, R.string.wearable_action_title, R.drawable.mobicom_ic_action_send);

                if (MobiComUserPreference.getInstance(NotificationIntentService.this).isLoggedIn()) {
                    Channel channel = ChannelService.getInstance(NotificationIntentService.this).getChannelInfo(message.getGroupId());
                    Contact contact = null;
                    if (message.getConversationId() != null) {
                        ConversationService.getInstance(NotificationIntentService.this).getConversation(message.getConversationId());
                    }
                    if (message.getGroupId() == null) {
                        contact = new AppContactService(NotificationIntentService.this).getContactById(message.getContactIds());
                    }
                    notificationService.notifyUser(contact, channel, message);
                }
            }

        }
    }

}
