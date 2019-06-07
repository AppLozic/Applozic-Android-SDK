package com.applozic.mobicomkit.api.notification;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.listners.AlConstantsHandler;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.contact.Contact;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: devashish
 * Date: 17/3/13
 * Time: 7:36 PM
 */
public class NotificationService  {
    public static final int NOTIFICATION_ID = 1000;
    private static final String TAG = "NotificationService";
    private static final String NOTIFICATION_SMALL_ICON_METADATA = "com.applozic.mobicomkit.notification.smallIcon";
    private static String GROUP_KEY = "applozic_key";
    MessageDatabaseService messageDatabaseService;
    List<Message> unReadMessageList = new ArrayList<>();
    long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
    private static Context context;
    private int iconResourceId;
    private int wearable_action_title;
    private int wearable_action_label;
    private int wearable_send_icon;
    private AppContactService appContactService;
    private ApplozicClient applozicClient;
    private String activityToOpen;
    private int notificationDisableThreshold = 0;
    private NotificationChannels notificationChannels;
    private String[] constArray = {MobiComKitConstants.LOCATION, MobiComKitConstants.AUDIO, MobiComKitConstants.VIDEO, MobiComKitConstants.ATTACHMENT};
    private String notificationFilePath;
    static Vibrator vibrator;
    AlertDialog.Builder builder;

    public NotificationService(int iconResourceID, Context context, int wearable_action_label, int wearable_action_title, int wearable_send_icon) {
        this.context = context;
        this.iconResourceId = iconResourceID;
        this.wearable_action_label = wearable_action_label;
        this.wearable_action_title = wearable_action_title;
        this.wearable_send_icon = wearable_send_icon;
        this.applozicClient = ApplozicClient.getInstance(context);
        this.appContactService = new AppContactService(context);
        this.activityToOpen = Utils.getMetaDataValue(context, "activity.open.on.notification");
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.notificationDisableThreshold = applozicClient.getNotificationMuteThreshold();
        this.notificationFilePath = Applozic.getInstance(context).getCustomNotificationSound();

        notificationChannels = new NotificationChannels(context, notificationFilePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannels.prepareNotificationChannels();
        }
    }

    public static void warn(){
        vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 400, 100, 400};
        vibrator.vibrate(pattern, 2);
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(5000);
                    vibrator.cancel();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
     }

    public void notifyUser(Contact contact, Channel channel, Message message, int index) {
        if (ApplozicClient.getInstance(context).isNotificationDisabled()) {
            Utils.printLog(context, TAG, "Notification is disabled !!");
            return;
        }
        Bitmap notificationIconBitmap = null;
        unReadMessageList = messageDatabaseService.getUnreadMessages();
        int count = appContactService.getChatConversationCount() + appContactService.getGroupConversationCount();
        int totalCount = messageDatabaseService.getTotalUnreadCount();

        Class activity = null;
        try {
            activity = Class.forName(activityToOpen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (message.getGroupId() != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact newContact = appContactService.getContactById(userId);
                    notificationIconBitmap = appContactService.downloadContactImage(context, newContact);
                }
            } else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                String userId = message.getTo();
                if (!TextUtils.isEmpty(userId)) {
                    Contact newContact = appContactService.getContactById(userId);
                    notificationIconBitmap = appContactService.downloadContactImage(context, newContact);
                }
            } else {
                notificationIconBitmap = appContactService.downloadGroupImage(context, channel);
            }
        } else {
            notificationIconBitmap = appContactService.downloadContactImage(context, contact);
        }

        Integer smallIconResourceId = Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) != null ? Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) : iconResourceId;
        Intent intent;
        if (count < 2) {
            intent = new Intent(context, activity);
            intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
        } else {
            intent = new Intent(context, activity);
            intent.putExtra(MobiComKitConstants.QUICK_LIST, true);
        }
        if (applozicClient.isChatListOnNotificationIsHidden()) {
            intent.putExtra("takeOrder", true);
        }
        if (applozicClient.isContextBasedChat()) {
            intent.putExtra("contextBasedChat", true);
        }
        intent.putExtra("sms_body", "text");
        intent.setType("vnd.android-dir/mms-sms");

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) (System.currentTimeMillis() & 0xfffffff),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, notificationChannels.getDefaultChannelId(muteNotifications(index)))
                        .setSmallIcon(smallIconResourceId)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setPriority(muteNotifications(index) ? NotificationCompat.PRIORITY_LOW : NotificationCompat.PRIORITY_HIGH)
                        .setWhen(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mBuilder.setGroup(GROUP_KEY);
            mBuilder.setGroupSummary(true);
        } else {
            if (totalCount != 0) {
                mBuilder.setNumber(totalCount);
            }
        }

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        if (ApplozicClient.getInstance(context).getVibrationOnNotification() && !muteNotifications(index)) {
            mBuilder.setVibrate(pattern);
        }
        if (!muteNotifications(index)) {
            mBuilder.setSound(TextUtils.isEmpty(notificationFilePath) ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(notificationFilePath));
        }

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        // Sets a title for the Inbox in expanded layout

        inboxStyle.setBigContentTitle(getNotificationTitle(count, contact, channel, message));

        // Moves events into the expanded layout
        try {
            if (unReadMessageList != null) {
                for (Message messageString : unReadMessageList) {
                    if (messageString.getGroupId() != null) {
                        Channel unreadChannel = ChannelService.getInstance(context).getChannelByChannelKey(messageString.getGroupId());
                        if (unreadChannel != null && unreadChannel.getUnreadCount() == 0) {
                            continue;
                        }
                    } else {
                        Contact unreadCount = appContactService.getContactById(messageString.getContactIds());
                        if (unreadCount != null && unreadCount.getUnreadCount() == 0) {
                            continue;
                        }
                    }
                    inboxStyle.addLine(getSpannedText(getMessageBody(messageString, count, channel, contact)));
                }
            }
            // Moves the expanded layout object into the notification object.

        } catch (Exception e) {
            e.printStackTrace();
        }
        String summaryText = "";
        if (count < 1) {
            summaryText = "";
            mBuilder.setLargeIcon(notificationIconBitmap != null ? notificationIconBitmap : BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(message.getGroupId() != null ? applozicClient.getDefaultChannelImage() : applozicClient.getDefaultContactImage(), "drawable", context.getPackageName())));
            mBuilder.setContentText(getSpannedText(getMessageBody(message, count, channel, contact)));
        } else if (count >= 1 && count < 2) {
            summaryText = totalCount < 2 ? totalCount + " new message " : totalCount + " new messages ";
            mBuilder.setLargeIcon(notificationIconBitmap != null ? notificationIconBitmap : BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(message.getGroupId() != null ? applozicClient.getDefaultChannelImage() : applozicClient.getDefaultContactImage(), "drawable", context.getPackageName())));
            mBuilder.setContentText(summaryText);
        } else {
            summaryText = totalCount + " messages from " + count + " chats";
            mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), iconResourceId));
            mBuilder.setContentText(summaryText);
        }
        inboxStyle.setSummaryText(summaryText);
        mBuilder.setContentTitle(getNotificationTitle(count, contact, channel, message));
        mBuilder.setStyle(inboxStyle);

        // Issue the notification here.

        if (message.hasAttachment()) {
            try {
                InputStream in;
                FileMeta fileMeta = message.getFileMetas();
                HttpURLConnection httpConn = null;
                if (fileMeta.getThumbnailBlobKey() != null) {
                    Bitmap bitmap = new FileClientService(context).loadThumbnailImage(context, message, 200, 200);
                    mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        WearableNotificationWithVoice notificationWithVoice =
                new WearableNotificationWithVoice(mBuilder, wearable_action_title,
                        wearable_action_label, wearable_send_icon, NOTIFICATION_ID);
        notificationWithVoice.setCurrentContext(context);
        notificationWithVoice.setPendingIntent(pendingIntent);

        try {
            if (unReadMessageList != null && unReadMessageList.size() > 0) {
                notificationWithVoice.sendNotification();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CharSequence getNotificationTitle(int conversationCount, Contact contact, Channel channel, Message message) {
        if (conversationCount < 2) {
            String notificationTitle = null;
            if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        Contact receiverContact = appContactService.getContactById(userId);
                        if (receiverContact != null) {
                            notificationTitle = receiverContact.getDisplayName();
                        }
                    }
                } else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                    String userId = message.getTo();
                    if (!TextUtils.isEmpty(userId)) {
                        Contact receiverContact = appContactService.getContactById(userId);
                        if (receiverContact != null) {
                            notificationTitle = receiverContact.getDisplayName();
                        }
                    }
                } else {
                    notificationTitle = channel.getName().trim();
                }
            } else if (contact != null) {
                notificationTitle = contact.getDisplayName().trim();
            }
            return Utils.getStyleString(notificationTitle);
        } else {
            return Utils.getStyleString(ApplozicClient.getInstance(context).getAppName());
        }
    }


    public CharSequence getMessageBody(Message message, int count, Channel channel, Contact contact) {
        String notificationText;
        if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
            notificationText = getText(0);
        } else if (message.getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
            notificationText = getText(1);
        } else if (message.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
            notificationText = getText(2);
        } else if (message.hasAttachment() && TextUtils.isEmpty(message.getMessage())) {
            notificationText = getText(3);
        } else {
            notificationText = message.getMessage();
        }
        CharSequence messageBody;
        Contact messageContactDisplayName = contact != null ? contact : appContactService.getContactById(message.getTo());
        if (message.getGroupId() != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) || Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                messageBody = Utils.getStyleStringForMessage(notificationText);
            } else {
                messageBody = Utils.getStyledStringForChannel(messageContactDisplayName.getDisplayName(), channel.getName(), notificationText);
            }
        } else {
            if (count < 2) {
                messageBody = Utils.getStyleStringForMessage(notificationText);
            } else {
                messageBody = Utils.getStyledStringForContact(messageContactDisplayName.getDisplayName(), notificationText);
            }
        }
        return messageBody;
    }


    public void notifyUserForNormalMessage(Contact contact, Channel channel, Message message, int index) {
            if (ApplozicClient.getInstance(context).isNotificationDisabled()) {
                Utils.printLog(context, TAG, "Notification is disabled");
                return;
            }
            String title = null;
            String notificationText;
            Bitmap notificationIconBitmap = null;
            Contact displayNameContact = null;
            if (message.getGroupId() != null) {
                if (channel == null) {
                    return;
                }
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        Contact newContact = appContactService.getContactById(userId);
                        notificationIconBitmap = appContactService.downloadContactImage(context, newContact);
                        title = newContact.getDisplayName();
                    }
                } else if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                    String userId = message.getTo();
                    if (!TextUtils.isEmpty(userId)) {
                        Contact newContact = appContactService.getContactById(userId);
                        notificationIconBitmap = appContactService.downloadGroupImage(context, channel);
                        title = newContact.getDisplayName();
                    }
                } else {
                    displayNameContact = appContactService.getContactById(message.getTo());
                    title = ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(context).getUserId());
                    notificationIconBitmap = appContactService.downloadGroupImage(context, channel);
                }
            } else {
                title = contact.getDisplayName();
                notificationIconBitmap = appContactService.downloadContactImage(context, contact);
            }

            if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                notificationText = getText(0);
            } else if (message.getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
                notificationText = getText(1);
            } else if (message.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
                notificationText = getText(2);
            } else if (message.hasAttachment() && TextUtils.isEmpty(message.getMessage())) {
                notificationText = getText(3);
            } else {
                notificationText = message.getMessage();
            }

            Class activity = null;
            try {
                activity = Class.forName(activityToOpen);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Integer smallIconResourceId = Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) != null ? Utils.getMetaDataValueForResources(context, NOTIFICATION_SMALL_ICON_METADATA) : iconResourceId;
            Intent intent = new Intent(context, activity);
            intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
            if (applozicClient.isChatListOnNotificationIsHidden()) {
                intent.putExtra("takeOrder", true);
            }
            if (applozicClient.isContextBasedChat()) {
                intent.putExtra("contextBasedChat", true);
            }
            intent.putExtra("sms_body", "text");
            intent.setType("vnd.android-dir/mms-sms");

            PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) (System.currentTimeMillis() & 0xfffffff),
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, notificationChannels.getDefaultChannelId(muteNotifications(index)));

            mBuilder.setSmallIcon(smallIconResourceId)
                    .setLargeIcon(ApplozicClient.getInstance(context).isShowAppIconInNotification() ? BitmapFactory.decodeResource(context.getResources(), iconResourceId) : notificationIconBitmap != null ? notificationIconBitmap : BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(channel != null && !(Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) || Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) ? applozicClient.getDefaultChannelImage() : applozicClient.getDefaultContactImage(), "drawable", context.getPackageName())))
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(muteNotifications(index) ? NotificationCompat.PRIORITY_LOW : NotificationCompat.PRIORITY_MAX)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title)
                    .setContentText(channel != null && !(Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) || Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) ? (displayNameContact != null ? (displayNameContact.getDisplayName() + ": " + getSpannedText(notificationText)) : "" + getSpannedText(notificationText)) : getSpannedText(notificationText));
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true);
            if (ApplozicClient.getInstance(context).isUnreadCountBadgeEnabled()) {
                int totalCount = messageDatabaseService.getTotalUnreadCount();
                if (totalCount != 0) {
                    mBuilder.setNumber(totalCount);
                }
            }
            if (!muteNotifications(index)) {
                mBuilder.setSound(TextUtils.isEmpty(notificationFilePath) ? RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(notificationFilePath));
            }
            if (message.hasAttachment()) {
                try {
                    FileMeta fileMeta = message.getFileMetas();
                    HttpURLConnection httpConn = null;
                    if (fileMeta.getThumbnailBlobKey() != null) {
                        Bitmap bitmap = new FileClientService(context).loadThumbnailImage(context, message, 200, 200);
                        mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            WearableNotificationWithVoice notificationWithVoice =
                    new WearableNotificationWithVoice(mBuilder, wearable_action_title,
                            wearable_action_label, wearable_send_icon, message.getGroupId() != null ? String.valueOf(message.getGroupId()).hashCode() : message.getContactIds().hashCode());
            notificationWithVoice.setCurrentContext(context);
            notificationWithVoice.setPendingIntent(pendingIntent);

            try {
                notificationWithVoice.sendNotification();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSpannedText(CharSequence message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(message.toString(), Html.FROM_HTML_MODE_COMPACT).toString();
        } else {
            return Html.fromHtml(message.toString()).toString();
        }
    }

    public String getText(int index) {
        if (context.getApplicationContext() instanceof AlConstantsHandler) {
            return getTextFromIndex(((AlConstantsHandler) context.getApplicationContext()).getNotificationTexts(), index);
        }

        return constArray[index];
    }

    public String getTextFromIndex(String[] texts, int index) {
        if (texts != null && texts.length == 4) {
            return texts[index];
        }
        return null;
    }

    public boolean muteNotifications(int index) {
        return !(notificationDisableThreshold == 0 || (notificationDisableThreshold > 0 && index < notificationDisableThreshold));
    }

}