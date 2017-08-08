package com.applozic.mobicomkit.api.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.contact.Contact;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: devashish
 * Date: 17/3/13
 * Time: 7:36 PM
 */
public class NotificationService {

    private static final int NOTIFICATION_ID = 1000;
    private static final String NOTIFICATION_SMALL_ICON_METADATA = "com.applozic.mobicomkit.notification.smallIcon";
    private static final String TAG = "NotificationService";
    private Context context;
    private int iconResourceId;
    private int wearable_action_title;
    private int wearable_action_label;
    private int wearable_send_icon;
    private AppContactService appContactService;
    private ApplozicClient applozicClient;
    private String activityToOpen;

    public NotificationService(int iconResourceID, Context context, int wearable_action_label, int wearable_action_title, int wearable_send_icon) {
        this.context = context;
        this.iconResourceId = iconResourceID;
        this.wearable_action_label = wearable_action_label;
        this.wearable_action_title = wearable_action_title;
        this.wearable_send_icon = wearable_send_icon;
        this.applozicClient = ApplozicClient.getInstance(context);
        this.appContactService = new AppContactService(context);
        activityToOpen = Utils.getMetaDataValue(context, "activity.open.on.notification");
    }

    public void notifyUser(Contact contact, Channel channel, Message message) {
        if (ApplozicClient.getInstance(context).isNotificationDisabled()) {
            Utils.printLog(context,TAG, "Notification is disabled");
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
            notificationText = MobiComKitConstants.LOCATION;
        } else if (message.getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
            notificationText = MobiComKitConstants.AUDIO;
        } else if (message.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
            notificationText = MobiComKitConstants.VIDEO;
        } else if (message.hasAttachment() && TextUtils.isEmpty(message.getMessage())) {
            notificationText = MobiComKitConstants.ATTACHMENT;
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

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(smallIconResourceId)
                        .setLargeIcon(ApplozicClient.getInstance(context).isShowAppIconInNotification() ? BitmapFactory.decodeResource(context.getResources(), iconResourceId) : notificationIconBitmap != null ? notificationIconBitmap : BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier(channel != null && !Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) ? applozicClient.getDefaultChannelImage() : applozicClient.getDefaultContactImage(), "drawable", context.getPackageName())))
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(title)
                        .setContentText(channel != null && !Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) ? displayNameContact.getDisplayName() + ": " + notificationText : notificationText)
                        .setSound(TextUtils.isEmpty(MobiComUserPreference.getInstance(context).getNotificationSoundFilePath())?RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION): Uri.parse(MobiComUserPreference.getInstance(context).getNotificationSoundFilePath()));
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        if (message.hasAttachment()) {
            try {
                FileMeta fileMeta = message.getFileMetas();
                HttpURLConnection httpConn = null;
                if (fileMeta.getThumbnailUrl() != null) {
                    httpConn = new MobiComKitClientService(context).openHttpConnection(fileMeta.getThumbnailUrl());
                    int response = httpConn.getResponseCode();
                    if (response == HttpURLConnection.HTTP_OK) {
                        Bitmap bitmap = BitmapFactory.decodeStream(httpConn.getInputStream());
                        String imageName = FileUtils.getName(fileMeta.getName()) + message.getCreatedAtTime() + "." + FileUtils.getFileFormat(fileMeta.getName());
                        File file = FileClientService.getFilePath(imageName, context, "image", true);
                        ImageUtils.saveImageToInternalStorage(file, bitmap);
                        mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));
                    }
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

}