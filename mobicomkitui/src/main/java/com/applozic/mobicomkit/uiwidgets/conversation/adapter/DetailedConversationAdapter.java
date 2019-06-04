package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.AttachmentManager;
import com.applozic.mobicomkit.api.attachment.AttachmentView;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.notification.VideoCallNotificationHelper;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.contact.MobiComVCFParser;
import com.applozic.mobicomkit.contact.VCFContactData;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.attachmentview.ApplozicDocumentView;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.FullScreenImageActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.OnClickReplyInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlRichMessage;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALProfileClickListener;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALStoragePermission;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALStoragePermissionListener;
import com.applozic.mobicomkit.uiwidgets.uilistener.ContextMenuClickListener;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.LocationUtils;
import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageCache;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.emoticon.EmojiconHandler;
import com.applozic.mobicommons.emoticon.EmoticonUtils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;
import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

/**
 * Created by adarsh on 4/7/15.
 */
public class DetailedConversationAdapter extends RecyclerView.Adapter implements Filterable {

    private static final String TAG = "DetailedConversation";

    private static final int FILE_THRESOLD_SIZE = 400;

    public ImageLoader contactImageLoader, loadImage;
    public String searchString;
    private AlCustomizationSettings alCustomizationSettings;
    private Context context;
    private Context activityContext;
    private Contact contact;
    private Channel channel;
    private boolean individual;
    private Drawable sentIcon;
    private Drawable deliveredIcon;
    private Drawable pendingIcon;
    private Drawable scheduledIcon;
    private ImageLoader imageThumbnailLoader;
    private EmojiconHandler emojiconHandler;
    private FileClientService fileClientService;
    private MessageDatabaseService messageDatabaseService;
    private BaseContactService contactService;
    private Class<?> messageIntentClass;
    private List<Message> messageList;
    private List<Message> originalList;
    private MobiComConversationService conversationService;
    private ImageCache imageCache;
    private View view;
    private ContextMenuClickListener contextMenuClickListener;
    private ALStoragePermissionListener storagePermissionListener;
    private ALRichMessageListener listener;
    private String geoApiKey;

    public void setAlCustomizationSettings(AlCustomizationSettings alCustomizationSettings) {
        this.alCustomizationSettings = alCustomizationSettings;
    }

    public void setContextMenuClickListener(ContextMenuClickListener contextMenuClickListener) {
        this.contextMenuClickListener = contextMenuClickListener;
    }

    public void setRichMessageCallbackListener(ALRichMessageListener listener) {
        this.listener = listener;
    }

    public void setStoragePermissionListener(ALStoragePermissionListener storagePermissionListener) {
        this.storagePermissionListener = storagePermissionListener;
    }

    public DetailedConversationAdapter(final Context context, List<Message> messageList, Channel channel, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, messageList, null, channel, messageIntentClass, emojiconHandler);
    }

    public DetailedConversationAdapter(final Context context, List<Message> messageList, Contact contact, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, messageList, contact, null, messageIntentClass, emojiconHandler);
    }

    public DetailedConversationAdapter(final Context context, List<Message> messageList, final Contact contact, Channel channel, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this.messageIntentClass = messageIntentClass;
        this.context = ApplozicService.getContext(context);
        this.activityContext = context;
        this.contact = contact;
        this.channel = channel;
        this.emojiconHandler = emojiconHandler;
        this.individual = (contact != null || channel != null);
        this.fileClientService = new FileClientService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.conversationService = new MobiComConversationService(context);
        this.contactService = new AppContactService(context);
        this.messageList = messageList;
        geoApiKey = Utils.getMetaDataValue(ApplozicService.getContext(context), ConversationActivity.GOOGLE_API_KEY_META_DATA);
        contactImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage(context, (Contact) data);
            }
        };
        contactImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_180_holo_light);
        contactImageLoader.setImageFadeIn(false);

        loadImage = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadMessageImage(context, (String) data);
            }
        };
        loadImage.setImageFadeIn(false);
        imageThumbnailLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadThumbnailImage(context, (Message) data, getImageLayoutParam(false).width, getImageLayoutParam(false).height);
            }
        };

        if (context != null) {
            this.imageCache = ImageCache.getInstance(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
            contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
            loadImage.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
            imageThumbnailLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        }

        imageThumbnailLoader.setImageFadeIn(false);
        sentIcon = context.getResources().getDrawable(R.drawable.applozic_ic_action_message_sent);
        deliveredIcon = context.getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered);
        pendingIcon = context.getResources().getDrawable(R.drawable.applozic_ic_action_message_pending);
        scheduledIcon = context.getResources().getDrawable(R.drawable.applozic_ic_action_message_schedule);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (layoutInflater == null) {
            return null;
        }

        if (viewType == 2) {
            View v2 = layoutInflater.inflate(R.layout.mobicom_date_layout, parent, false);
            return new MyViewHolder2(v2);
        } else if (viewType == 3) {
            View v3 = layoutInflater.inflate(R.layout.applozic_custom_message_layout, parent, false);
            return new MyViewHolder3(v3);
        } else if (viewType == 4) {
            View v4 = layoutInflater.inflate(R.layout.applozic_channel_custom_message_layout, parent, false);
            return new MyViewHolder4(v4);
        } else if (viewType == 5) {
            View v5 = layoutInflater.inflate(R.layout.applozic_call_layout, parent, false);
            return new MyViewHolder5(v5);
        } else if (viewType == 0) {
            View v0 = layoutInflater.inflate(R.layout.mobicom_received_message_list_view, parent, false);
            return new MyViewHolder(v0);
        }

        view = layoutInflater.inflate(R.layout.mobicom_sent_message_list_view, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        final Message message = getItem(position);

        try {
            if (type == 2) {
                MyViewHolder2 myViewHolder2 = (MyViewHolder2) holder;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("EEEE");
                Date date = new Date(message.getCreatedAtTime());

                myViewHolder2.dateView.setTextColor(Color.parseColor(alCustomizationSettings.getConversationDateTextColor().trim()));
                myViewHolder2.dayTextView.setTextColor(Color.parseColor(alCustomizationSettings.getConversationDayTextColor().trim()));

                if (DateUtils.isSameDay(message.getCreatedAtTime())) {
                    myViewHolder2.dayTextView.setVisibility(View.VISIBLE);
                    myViewHolder2.dateView.setVisibility(GONE);
                    myViewHolder2.dayTextView.setText(R.string.today);
                } else {
                    myViewHolder2.dayTextView.setVisibility(View.VISIBLE);
                    myViewHolder2.dateView.setVisibility(View.VISIBLE);
                    myViewHolder2.dayTextView.setText(simpleDateFormatDay.format(date));
                    myViewHolder2.dateView.setText(simpleDateFormat.format(date));
                }
                return;
            } else if (type == 3) {
                MyViewHolder3 myViewHolder3 = (MyViewHolder3) holder;
                myViewHolder3.customContentTextView.setText(message.getMessage());
                myViewHolder3.customContentTextView.setVisibility(View.VISIBLE);
                return;
            } else if (type == 4) {
                MyViewHolder4 myViewHolder4 = (MyViewHolder4) holder;
                GradientDrawable bgGradientDrawable = (GradientDrawable) myViewHolder4.channelMessageTextView.getBackground();
                bgGradientDrawable.setColor(Color.parseColor(alCustomizationSettings.getChannelCustomMessageBgColor()));
                bgGradientDrawable.setStroke(3, Color.parseColor(alCustomizationSettings.getChannelCustomMessageBorderColor()));
                myViewHolder4.channelMessageTextView.setTextColor(Color.parseColor(alCustomizationSettings.getChannelCustomMessageTextColor()));
                myViewHolder4.channelMessageTextView.setText(message.getMessage());
                return;
            } else if (type == 5) {
                MyViewHolder5 myViewHolder5 = (MyViewHolder5) holder;

                if (message != null) {
                    myViewHolder5.timeTextView.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
                    if (message.getMetadata() != null) {
                        myViewHolder5.statusTextView.setText(VideoCallNotificationHelper.getStatus(message.getMetadata()));
                    }

                    if (VideoCallNotificationHelper.isMissedCall(message)) {
                        myViewHolder5.imageView.setImageResource(R.drawable.ic_communication_call_missed);
                    }

                    if (VideoCallNotificationHelper.isAudioCall(message)) {
                        myViewHolder5.imageView.setImageResource(R.drawable.applozic_ic_action_call);
                    } else {
                        myViewHolder5.imageView.setImageResource(R.drawable.ic_videocam_white_24px);
                    }
                    if (message.getMetadata() != null) {
                        if (message.getMetadata().get(VideoCallNotificationHelper.MSG_TYPE).equals(VideoCallNotificationHelper.CALL_END)) {
                            String duration = message.getMetadata().get(VideoCallNotificationHelper.CALL_DURATION);

                            if (!TextUtils.isEmpty(duration)) {
                                myViewHolder5.durationTextView.setVisibility(View.VISIBLE);
                                duration = Utils.getTimeDurationInFormat(Long.parseLong(duration));
                                myViewHolder5.durationTextView.setText(duration);
                            }
                        } else {
                            myViewHolder5.durationTextView.setVisibility(View.GONE);
                        }
                    }
                }
            } else {
                final MyViewHolder myHolder = (MyViewHolder) holder;
                if (message != null) {
                    Contact receiverContact = null;
                    Contact contactDisplayName = null;
                    if (message.getGroupId() == null) {
                        List<String> items = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
                        List<String> userIds = null;
                        if (!TextUtils.isEmpty(message.getContactIds())) {
                            userIds = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
                        }
                        if (individual) {
                            receiverContact = contact;
                            contact.setContactNumber(items.get(0));
                            if (userIds != null) {
                                contact.setUserId(userIds.get(0));
                            }
                        } else {
                            receiverContact = contactService.getContactReceiver(items, userIds);
                        }
                    } else {
                        if (!TextUtils.isEmpty(message.getContactIds())) {
                            contactDisplayName = contactService.getContactById(message.getContactIds());
                        }
                    }
                    Configuration config = context.getResources().getConfiguration();
                    if (message.getMetadata() != null && !message.getMetadata().isEmpty() && message.getMetadata().containsKey(Message.MetaDataType.AL_REPLY.getValue())) {
                        String keyString = message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue());

                        Message messageToBeReplied = new Message();
                        messageToBeReplied.setKeyString(keyString);
                        int indexOfObject = messageList.indexOf(messageToBeReplied);
                        if (indexOfObject != -1) {
                            messageToBeReplied = messageList.get(indexOfObject);
                        } else {
                            messageToBeReplied = messageDatabaseService.getMessage(message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
                        }
                        final Message msg = messageToBeReplied;
                        if (msg != null) {
                            String displayName;

                            myHolder.replyRelativeLayout.setBackgroundColor(message.isTypeOutbox() ?
                                    Color.parseColor(alCustomizationSettings.getReplyMessageLayoutSentMessageBackground()) : Color.parseColor(alCustomizationSettings.getReplyMessageLayoutReceivedMessageBackground()));

                            myHolder.replyNameTextView.setTextColor(message.isTypeOutbox() ?
                                    Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));

                            myHolder.replyMessageTextView.setTextColor(message.isTypeOutbox() ?
                                    Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));

                            if (msg.getGroupId() != null) {
                                if (MobiComUserPreference.getInstance(context).getUserId().equals(msg.getContactIds()) || TextUtils.isEmpty(msg.getContactIds())) {
                                    displayName = context.getString(R.string.you_string);
                                } else {
                                    displayName = contactService.getContactById(msg.getContactIds()).getDisplayName();
                                }
                            } else {
                                if (msg.isTypeOutbox()) {
                                    displayName = context.getString(R.string.you_string);
                                } else {
                                    displayName = contactService.getContactById(msg.getContactIds()).getDisplayName();
                                }
                            }

                            myHolder.replyNameTextView.setText(displayName);
                            if (msg.hasAttachment()) {
                                FileMeta fileMeta = msg.getFileMetas();
                                myHolder.imageViewForAttachmentType.setVisibility(View.VISIBLE);
                                if (fileMeta.getContentType().contains("image")) {
                                    myHolder.imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_image_camera_alt);
                                    if (TextUtils.isEmpty(msg.getMessage())) {
                                        myHolder.replyMessageTextView.setText(context.getString(R.string.photo_string));
                                    } else {
                                        myHolder.replyMessageTextView.setText(msg.getMessage());
                                    }
                                    myHolder.imageViewPhoto.setVisibility(View.VISIBLE);
                                    myHolder.imageViewRLayout.setVisibility(View.VISIBLE);
                                    imageThumbnailLoader.loadImage(msg, myHolder.imageViewPhoto);
                                } else if (fileMeta.getContentType().contains("video")) {
                                    myHolder.imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_video);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                                            myHolder.imageViewForAttachmentType.setScaleX(-1);
                                        }
                                    }
                                    if (TextUtils.isEmpty(msg.getMessage())) {
                                        myHolder.replyMessageTextView.setText(context.getString(R.string.video_string));
                                    } else {
                                        myHolder.replyMessageTextView.setText(msg.getMessage());
                                    }
                                    myHolder.imageViewPhoto.setVisibility(View.VISIBLE);
                                    myHolder.imageViewRLayout.setVisibility(View.VISIBLE);
                                    if (msg.getFilePaths() != null && msg.getFilePaths().size() > 0) {
                                        if (imageCache != null && imageCache.getBitmapFromMemCache(msg.getKeyString()) != null) {
                                            myHolder.imageViewPhoto.setImageBitmap(imageCache.getBitmapFromMemCache(msg.getKeyString()));
                                        } else {
                                            if (imageCache != null) {
                                                imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(msg.getFilePaths().get(0)));
                                            }
                                            myHolder.imageViewPhoto.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(msg.getFilePaths().get(0)));
                                        }
                                    }
                                } else if (fileMeta.getContentType().contains("audio")) {
                                    myHolder.imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_music_note);
                                    if (TextUtils.isEmpty(msg.getMessage())) {
                                        myHolder.replyMessageTextView.setText(context.getString(R.string.audio_string));
                                    } else {
                                        myHolder.replyMessageTextView.setText(msg.getMessage());
                                    }
                                    myHolder.imageViewPhoto.setVisibility(View.GONE);
                                    myHolder.imageViewRLayout.setVisibility(View.GONE);
                                } else if (msg.isContactMessage()) {
                                    MobiComVCFParser parser = new MobiComVCFParser();
                                    try {
                                        VCFContactData data = parser.parseCVFContactData(msg.getFilePaths().get(0));
                                        if (data != null) {
                                            myHolder.imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                                            myHolder.replyMessageTextView.setText(context.getString(R.string.contact_string));
                                            myHolder.replyMessageTextView.append(" " + data.getName());
                                        }
                                    } catch (Exception e) {
                                        myHolder.imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                                        myHolder.replyMessageTextView.setText(context.getString(R.string.contact_string));
                                    }
                                    myHolder.imageViewPhoto.setVisibility(View.GONE);
                                    myHolder.imageViewRLayout.setVisibility(View.GONE);
                                } else {
                                    myHolder.imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_attachment);
                                    if (TextUtils.isEmpty(msg.getMessage())) {
                                        myHolder.replyMessageTextView.setText(context.getString(R.string.attachment_string));
                                    } else {
                                        myHolder.replyMessageTextView.setText(msg.getMessage());
                                    }
                                    myHolder.imageViewPhoto.setVisibility(View.GONE);
                                    myHolder.imageViewRLayout.setVisibility(View.GONE);
                                }
                                myHolder.imageViewForAttachmentType.setColorFilter(Color.parseColor(message.isTypeOutbox() ? alCustomizationSettings.getSentMessageTextColor() : alCustomizationSettings.getReceivedMessageTextColor()));
                            } else if (msg.getContentType() == Message.ContentType.LOCATION.getValue()) {
                                myHolder.imageViewForAttachmentType.setVisibility(View.VISIBLE);
                                myHolder.imageViewPhoto.setVisibility(View.VISIBLE);
                                myHolder.imageViewRLayout.setVisibility(View.VISIBLE);
                                myHolder.replyMessageTextView.setText(context.getString(R.string.al_location_string));
                                myHolder.imageViewForAttachmentType.setColorFilter(Color.parseColor(message.isTypeOutbox() ? alCustomizationSettings.getSentMessageTextColor() : alCustomizationSettings.getReceivedMessageTextColor()));
                                myHolder.imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_location_on_white_24dp);
                                loadImage.setLoadingImage(R.drawable.applozic_map_offline_thumbnail);
                                loadImage.loadImage(LocationUtils.loadStaticMap(msg.getMessage(), geoApiKey), myHolder.imageViewPhoto);
                            } else {
                                myHolder.imageViewForAttachmentType.setVisibility(View.GONE);
                                myHolder.imageViewRLayout.setVisibility(View.GONE);
                                myHolder.imageViewPhoto.setVisibility(View.GONE);
                                myHolder.replyMessageTextView.setText(msg.getMessage());
                            }
                            myHolder.replyRelativeLayout.setVisibility(View.VISIBLE);
                            myHolder.replyRelativeLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ((OnClickReplyInterface) activityContext).onClickOnMessageReply(msg);
                                }
                            });
                        }
                    } else {
                        myHolder.replyRelativeLayout.setVisibility(GONE);
                    }

                    if (TextUtils.isEmpty(message.getMessage())) {
                        myHolder.messageTextView.setVisibility(View.GONE);
                    }

                    myHolder.messageTextView.setVisibility(View.GONE);
                    myHolder.mapImageView.setVisibility(GONE);

                    if (channel != null) {
                        if (!message.hasAttachment() && TextUtils.isEmpty(message.getMessage()) && message.getMetadata() == null) {
                            myHolder.messageTextView.setText("");
                        }
                    }

                    if (myHolder.chatLocation != null) {
                        myHolder.chatLocation.setVisibility(View.GONE);
                    }

                    if (myHolder.attachedFile != null) {
                        //myHolder.attachedFile.setText("");
                        myHolder.attachedFile.setVisibility(View.GONE);
                    }

                    if (myHolder.attachmentIcon != null) {
                        myHolder.attachmentIcon.setVisibility(View.GONE);
                    }

                    if (channel != null && myHolder.nameTextView != null && contactDisplayName != null) {
                        myHolder.nameTextView.setVisibility(Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) ? View.GONE : View.VISIBLE);
                        if (alCustomizationSettings.isLaunchChatFromProfilePicOrName()) {
                            myHolder.nameTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(activityContext, ConversationActivity.class);
                                    intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                                    if (message.getConversationId() != null) {
                                        intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                                    }
                                    activityContext.startActivity(intent);
                                }
                            });
                        }

                        String userId = contactDisplayName.getDisplayName();
                        char firstLetter = contactDisplayName.getDisplayName().charAt(0);
                        if (userId.length() > 0) {
                            myHolder.nameTextView.setText(String.valueOf(userId));
                        }

                        Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                        myHolder.nameTextView.setTextColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
                    }
                    myHolder.createdAtTime.setTextColor(Color.parseColor(alCustomizationSettings.getMessageTimeTextColor()));

                    myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                    //myHolder.preview.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
                    myHolder.attachmentView.setVisibility(View.GONE);

                    if (message.isTypeOutbox() && !message.isCanceled()) {
                        myHolder.mediaUploadProgressBar.setVisibility(View.GONE);
                        myHolder.mediaUploadProgressBar.setVisibility(message.isAttachmentUploadInProgress() ? View.VISIBLE : View.GONE);
                    } else {
                        myHolder.mediaUploadProgressBar.setVisibility(View.GONE);
                    }

                    if (myHolder.attachedFile != null) {
                        myHolder.attachedFile.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
                    }

                    if (individual && message.getTimeToLive() != null) {
                        myHolder.selfDestruct
                                .setText("Self destruct in " + message.getTimeToLive() + " mins");
                        myHolder.selfDestruct.setVisibility(View.VISIBLE);
                    } else {
                        myHolder.selfDestruct.setText("");
                        myHolder.selfDestruct.setVisibility(View.GONE);
                    }

                    if (myHolder.sentOrReceived != null) {
                        if ((!message.isCall()) || message.isDummyEmptyMessage()) {
                            myHolder.sentOrReceived.setVisibility(View.GONE);
                        } else if (message.isCall()) {
                            myHolder.sentOrReceived.setImageResource(R.drawable.applozic_ic_action_call_holo_light);
                        } else if (getItemViewType(position) == 0) {
                            myHolder.sentOrReceived.setImageResource(R.drawable.mobicom_social_forward);
                        } else {
                            myHolder.sentOrReceived.setImageResource(R.drawable.mobicom_social_reply);
                        }

                        if (message.isCall()) {
                            myHolder.messageTextView.setTextColor(context.getResources().getColor(message.isIncomingCall() ? R.color.incoming_call : R.color.outgoing_call));
                        }
                    }

                    if (myHolder.nameTextLayout != null && contact != null) {
                        myHolder.nameTextLayout.setVisibility(View.GONE);
                    }

                    if (message.isCall() || message.isDummyEmptyMessage()) {
                        myHolder.createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    } else if (!message.isSentToServer() && message.isTypeOutbox() && (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()))) {
                        myHolder.createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, message.getScheduledAt() != null ? scheduledIcon : pendingIcon, null);
                    } else if (message.getKeyString() != null && message.isTypeOutbox() && message.isSentToServer() && (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()))) {
                        Drawable statusIcon;
                        if (message.isDeliveredAndRead()) {
                            statusIcon = context.getResources().getDrawable(R.drawable.applozic_ic_action_message_read);
                        } else {
                            statusIcon = (message.getDelivered() || (contact != null && new Support(context).isSupportNumber(contact.getFormattedContactNumber())) ?
                                    deliveredIcon : (message.getScheduledAt() != null ? scheduledIcon : sentIcon));
                        }
                        myHolder.createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, statusIcon, null);
                    }

                    if (message.isCall()) {
                        myHolder.deliveryStatus.setText("");
                    }

                    if (contactDisplayName != null && myHolder.contactImage != null && alCustomizationSettings.isLaunchChatFromProfilePicOrName()) {
                        myHolder.contactImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(activityContext, ConversationActivity.class);
                                intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                                if (message.getConversationId() != null) {
                                    intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                                }
                                activityContext.startActivity(intent);
                            }
                        });

                        myHolder.alphabeticTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(activityContext, ConversationActivity.class);
                                intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                                if (message.getConversationId() != null) {
                                    intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                                }
                                activityContext.startActivity(intent);
                            }
                        });
                    }

                    if (!message.isTypeOutbox()) {
                        loadContactImage(receiverContact, contactDisplayName, message, myHolder.contactImage, myHolder.alphabeticTextView, myHolder.onlineTextView);
                    }

                    ApplozicDocumentView audioView = new ApplozicDocumentView(activityContext, storagePermissionListener);
                    audioView.inflateViewWithMessage(myHolder.view, message);
                    audioView.hideView(true);

                    if (message.hasAttachment() && myHolder.attachedFile != null & !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                        myHolder.mainAttachmentLayout.setLayoutParams(getImageLayoutParam(false));
                        myHolder.mainAttachmentLayout.setVisibility(View.VISIBLE);
                        if (message.getFileMetas() != null && (message.getFileMetas().getContentType().contains("image") || message.getFileMetas().getContentType().contains("video"))) {
                            myHolder.attachedFile.setVisibility(View.GONE);
                        }
                        if (message.isAttachmentDownloaded()) {
                            myHolder.mapImageView.setVisibility(View.GONE);
                            //myHolder.preview.setVisibility(View.GONE);
                            String[] filePaths = new String[message.getFilePaths().size()];
                            int i = 0;
                            for (final String filePath : message.getFilePaths()) {
                                filePaths[i++] = filePath;
                                final String mimeType = FileUtils.getMimeType(filePath);
                                if (mimeType != null && mimeType.startsWith("image")) {
                                    //myHolder.attachmentView.setImageBitmap(null);
                                    myHolder.attachmentView.setVisibility(View.GONE);
                                    myHolder.videoIcon.setVisibility(View.GONE);
                                    myHolder.preview.setVisibility(View.VISIBLE);
                                    myHolder.preview.setImageBitmap(null);
                                    myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                                    myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                                    Glide.with(context).load(new File(filePath)).into(myHolder.preview);
                                    /*Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                                    myHolder.preview.setImageBitmap(bitmap);*/
                                    //Picasso.with(context).load(new File(filePath)).into(myHolder.preview);
                                    myHolder.attachmentView.setMessage(message);
                                    myHolder.mediaDownloadProgressBar.setVisibility(View.GONE);
                                    //myHolder.mediaUploadProgressBar.setVisibility(GONE);
                                    myHolder.attachedFile.setVisibility(View.GONE);
                                    myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                                    myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                                    /**/
                                    /*myHolder.attachmentView.setVisibility(View.VISIBLE);
                                    myHolder.videoIcon.setVisibility(View.GONE);
                                    myHolder.preview.setVisibility(View.GONE);*/
                                } else if (mimeType != null && mimeType.startsWith("video")) {
                                    myHolder.preview.setVisibility(View.VISIBLE);
                                    myHolder.videoIcon.setVisibility(View.VISIBLE);
                                    myHolder.mediaDownloadProgressBar.setVisibility(View.GONE);
                                    myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                                    myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                                    myHolder.attachedFile.setVisibility(View.GONE);
                                    if (imageCache.getBitmapFromMemCache(message.getKeyString()) != null) {
                                        myHolder.preview.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
                                    } else {
                                        imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(filePath));
                                        myHolder.preview.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(filePath));
                                    }
                                } else {
                                    myHolder.preview.setVisibility(View.GONE);
                                    myHolder.mediaDownloadProgressBar.setVisibility(View.GONE);
                                    myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                                    myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                                    showAttachmentIconAndText(myHolder.attachedFile, message, mimeType);
                                }
                            }
                        } else if (message.isAttachmentUploadInProgress()) {
                            //showPreview(smListItem, preview, attachmentDownloadLayout);
                            myHolder.preview.setImageDrawable(null);
                            myHolder.preview.setImageBitmap(null);
                            myHolder.attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                            myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                            myHolder.videoIcon.setVisibility(GONE);
                            myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                            myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                            myHolder.attachmentView.setMessage(message);
                            myHolder.attachmentView.setVisibility(View.VISIBLE);
                            myHolder.attachedFile.setVisibility(GONE);
                            myHolder.attachmentIcon.setVisibility(GONE);
                        } else if (AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                            //ondraw is called and thread is assigned to the attachment view...
                            myHolder.preview.setImageDrawable(null);
                            myHolder.preview.setImageBitmap(null);
                            myHolder.attachmentView.setMessage(message);
                            myHolder.attachmentView.setVisibility(View.VISIBLE);
                            myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                            myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                            myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                            myHolder.preview.setVisibility(View.VISIBLE);
                            showPreview(message, myHolder.preview, myHolder.attachmentDownloadLayout);
                            FileMeta fileMeta = message.getFileMetas();
                            final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                            if (!fileMeta.getContentType().contains("image") && !fileMeta.getContentType().contains("video")) {
                                showAttachmentIconAndText(myHolder.attachedFile, message, mimeType);
                            }
                            myHolder.downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                            myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                            myHolder.attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                        } else {
                            String fileKeys = message.getFileMetaKeyStrings();
                            int i = 0;
                            myHolder.preview.setVisibility(View.GONE);
                            //showPreview(null, myHolder.preview, myHolder.attachmentDownloadLayout);
                            showPreview(message, myHolder.preview, myHolder.attachmentDownloadLayout);
                            myHolder.preview.setVisibility(View.VISIBLE);
                            myHolder.videoIcon.setVisibility(View.GONE);
                            //TODO: while doing multiple image support in single sms ...we might improve this
                            // for (String fileKey : message.getFileMetaKeyStrings()) {
                            if (message.getFileMetas() != null) {
                                FileMeta fileMeta = message.getFileMetas();
                                myHolder.attachmentDownloadLayout.setVisibility(View.VISIBLE);
                                myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                                myHolder.downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                                final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                                if (!fileMeta.getContentType().contains("image") && !fileMeta.getContentType().contains("video")) {
                                    showAttachmentIconAndText(myHolder.attachedFile, message, mimeType);
                                }
                            }
                        }
                        if (isNormalAttachment(message)) {
                            myHolder.videoIcon.setVisibility(View.GONE);
                            myHolder.attachedFile.setVisibility(View.GONE);
                            myHolder.mainAttachmentLayout.setVisibility(View.GONE);
                            myHolder.mainContactShareLayout.setVisibility(View.GONE);
                            myHolder.chatLocation.setVisibility(View.GONE);
                            myHolder.preview.setVisibility(View.GONE);
                            audioView.hideView(false);
                            myHolder.createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
                        }
                    }
                    if (message.isCanceled()) {
                        myHolder.attachmentRetry.setVisibility(View.VISIBLE);
                    } else {
                        myHolder.attachmentRetry.setVisibility(View.GONE);
                    }
                    myHolder.attachmentRetry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Utils.isInternetAvailable(context)) {
                                File file = null;
                                if (message != null && message.getFilePaths() != null) {
                                    file = new File(message.getFilePaths().get(0));
                                }
                                if (file != null && !file.exists()) {
                                    Toast.makeText(context, context.getString(R.string.file_does_not_exist), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Toast.makeText(context, context.getString(R.string.applozic_resending_attachment), Toast.LENGTH_LONG).show();
                                myHolder.mediaUploadProgressBar.setVisibility(View.VISIBLE);
                                myHolder.attachmentRetry.setVisibility(View.GONE);
                                //updating Cancel Flag to smListItem....
                                message.setCanceled(false);
                                messageDatabaseService.updateCanceledFlag(message.getMessageId(), 0);
                                conversationService.sendMessage(message, messageIntentClass);
                            } else {
                                Toast.makeText(context, context.getString(R.string.internet_connection_not_available), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    myHolder.attachmentDownloadProgressLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myHolder.attachmentView.setVisibility(View.GONE);
                            myHolder.attachmentView.cancelDownload();
                            myHolder.attachmentDownloadProgressLayout.setVisibility(View.GONE);
                            message.setAttDownloadInProgress(false);
                        }
                    });
                    //final ProgressBar mediaDownloadProgressBar = mediaDownloadProgressBar;
                    myHolder.preview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //TODO: 1. get the image Size and decide if we can download directly
                            //2. if already downloaded to ds card show it directly ....
                            //3. if deleted from sd crad only ...ask user to download it again or skip ...
                            if (message.getContentType() == Message.ContentType.TEXT_URL.getValue()) {
                                return;
                            }
                            if (message.isAttachmentDownloaded()) {
                                if (storagePermissionListener.isPermissionGranted()) {
                                    showFullView(message);
                                } else {
                                    storagePermissionListener.checkPermission(new ALStoragePermission() {
                                        @Override
                                        public void onAction(boolean didGrant) {
                                            if (didGrant) {
                                                showFullView(message);
                                            }
                                        }
                                    });
                                }
                            } else {
                                if (storagePermissionListener.isPermissionGranted()) {
                                    myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                                    myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                                    myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                                    myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                                    myHolder.attachmentView.setMessage(message);
                                    myHolder.attachmentView.setVisibility(View.VISIBLE);
                                    myHolder.attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                                } else {
                                    storagePermissionListener.checkPermission(new ALStoragePermission() {
                                        @Override
                                        public void onAction(boolean didGrant) {
                                            if (didGrant) {
                                                myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                                                myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                                                myHolder.attachmentView.setProressBar(myHolder.mediaDownloadProgressBar);
                                                myHolder.attachmentView.setDownloadProgressLayout(myHolder.attachmentDownloadProgressLayout);
                                                myHolder.attachmentView.setMessage(message);
                                                myHolder.attachmentView.setVisibility(View.VISIBLE);
                                                myHolder.attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });

                    myHolder.preview.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return false;
                        }
                    });

                    myHolder.attachmentView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (storagePermissionListener.isPermissionGranted()) {
                                showFullView(message);
                            } else {
                                storagePermissionListener.checkPermission(new ALStoragePermission() {
                                    @Override
                                    public void onAction(boolean didGrant) {
                                        if (didGrant) {
                                            showFullView(message);
                                        }
                                    }
                                });
                            }
                        }
                    });

                    myHolder.attachmentView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            return false;
                        }
                    });

                    if (myHolder.attachedFile != null) {
                        myHolder.attachedFile.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                return false;
                            }
                        });
                    }

                    if (message.getScheduledAt() != null) {
                        myHolder.createdAtTime.setText(DateUtils.getFormattedDate(message.getScheduledAt()));
                    } else if (myHolder.createdAtTime != null && message.isDummyEmptyMessage()) {
                        myHolder.createdAtTime.setText("");
                    } else if (myHolder.createdAtTime != null) {
                        myHolder.createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
                    }

                    String mimeType = "";
                    if (TextUtils.isEmpty(message.getMessage())) {
                        myHolder.messageTextView.setVisibility(message.hasAttachment() ? View.GONE : View.GONE);
                    } else {
                        myHolder.messageTextView.setVisibility(message.getContentType() == Message.ContentType.LOCATION.getValue() ? View.GONE : View.VISIBLE);
                    }

                    if (myHolder.messageTextView != null) {
                        myHolder.messageTextView.setTextColor(message.isTypeOutbox() ?
                                Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));
                        myHolder.messageTextView.setLinkTextColor(message.isTypeOutbox() ?
                                Color.parseColor(alCustomizationSettings.getSentMessageLinkTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageLinkTextColor()));

                        if (message.getContentType() == Message.ContentType.TEXT_URL.getValue()) {
                            try {
                                myHolder.mapImageView.setVisibility(View.GONE);
                                myHolder.attachedFile.setVisibility(View.GONE);
                                myHolder.preview.setVisibility(View.VISIBLE);
                                setMessageText(myHolder.messageTextView, message);
                                loadImage.setImageFadeIn(false);
                                loadImage.loadImage(message.getFileMetas().getBlobKeyString(), myHolder.preview);
                                myHolder.attachmentDownloadLayout.setVisibility(View.GONE);
                            } catch (Exception e) {
                            }
                        } else if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                            //                  attachedFile.setVisibility(View.GONE);
                            //                preview.setVisibility(View.GONE);
                            myHolder.chatLocation.setLayoutParams(getImageLayoutParam(false));
                            myHolder.chatLocation.setVisibility(View.VISIBLE);
                            loadImage.setImageFadeIn(false);
                            //Default image while loading image.
                            myHolder.mapImageView.setVisibility(View.VISIBLE);
                            loadImage.setLoadingImage(R.drawable.applozic_map_offline_thumbnail);
                            loadImage.loadImage(LocationUtils.loadStaticMap(message.getMessage(), geoApiKey), myHolder.mapImageView);
                            myHolder.messageTextView.setVisibility(View.GONE);
                            myHolder.preview.setVisibility(View.GONE);

                            myHolder.mapImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=" + LocationUtils.getLocationFromMessage(message.getMessage()));
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                    activityContext.startActivity(intent);
                                }
                            });
                        } else if (message.getContentType() == Message.ContentType.PRICE.getValue()) {
                            myHolder.mapImageView.setVisibility(View.GONE);
                            myHolder.messageTextView.setText(ConversationUIService.FINAL_PRICE_TEXT + message.getMessage());
                        } else if ((message.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) && !message.isAttachmentDownloaded()) {
                            myHolder.preview.setVisibility(View.VISIBLE);
                            myHolder.mapImageView.setVisibility(View.GONE);
                            myHolder.preview.setImageResource(R.drawable.applozic_video_default_thumbnail);
                        } else if (message.getContentType() == Message.ContentType.TEXT_HTML.getValue()) {
                            myHolder.mapImageView.setVisibility(View.GONE);
                            setMessageText(myHolder.messageTextView, message);
                        } else {
                            myHolder.mapImageView.setVisibility(View.GONE);
                            myHolder.chatLocation.setVisibility(View.GONE);
                            myHolder.messageTextView.setText(EmoticonUtils.getSmiledText(activityContext, message.getMessage(), emojiconHandler));
                        }

                        if (myHolder.messageTextLayout != null) {
                            GradientDrawable bgShape = (GradientDrawable) myHolder.messageTextLayout.getBackground();
                            bgShape.setColor(message.isTypeOutbox() ?
                                    Color.parseColor(alCustomizationSettings.getSentMessageBackgroundColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageBackgroundColor()));
                            bgShape.setStroke(3, message.isTypeOutbox() ?
                                    Color.parseColor(alCustomizationSettings.getSentMessageBorderColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageBackgroundColor()));
                        }
                    }

                    if (!message.hasAttachment()) {
                        myHolder.preview.setVisibility(View.GONE);
                        myHolder.attachedFile.setVisibility(View.GONE);
                        myHolder.mainAttachmentLayout.setVisibility(View.GONE);
                        myHolder.mediaDownloadProgressBar.setVisibility(View.VISIBLE);
                        myHolder.attachmentView.setVisibility(View.GONE);
                        myHolder.videoIcon.setVisibility(View.GONE);
                        myHolder.attachedFile.setVisibility(View.GONE);
                        myHolder.mainContactShareLayout.setVisibility(View.GONE);
                    }

                    if (message.getMetadata() != null && "300".equals(message.getMetadata().get("contentType"))) {
                        myHolder.richMessageLayout.setVisibility(View.VISIBLE);
                        new AlRichMessage(activityContext, myHolder.richMessageContainer, myHolder.richMessageLayout, message, listener);
                    } else {
                        myHolder.richMessageLayout.setVisibility(View.GONE);
                    }

                    //Handling contact share
                    if (message.isContactMessage()) {
                        myHolder.attachedFile.setVisibility(View.GONE);
                        myHolder.mainAttachmentLayout.setVisibility(View.GONE);
                        setupContactShareView(message, myHolder);
                    } else {
                        myHolder.mainContactShareLayout.setVisibility(View.GONE);
                    }

                    int startIndex = indexOfSearchQuery(message.getMessage());
                    if (startIndex != -1) {
                        final SpannableString highlightedName = new SpannableString(message.getMessage());

                        // Sets the span to start at the starting point of the match and end at "length"
                        // characters beyond the starting point
                        highlightedName.setSpan(new ForegroundColorSpan(Color.parseColor(alCustomizationSettings.getMessageSearchTextColor())), startIndex,
                                startIndex + searchString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                        myHolder.messageTextView.setText(highlightedName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupContactShareView(final Message message, MyViewHolder myViewHolder) {
        myViewHolder.mainContactShareLayout.setVisibility(View.VISIBLE);
        myViewHolder.mainContactShareLayout.setLayoutParams(getImageLayoutParam(false));
        MobiComVCFParser parser = new MobiComVCFParser();
        try {

            VCFContactData data = parser.parseCVFContactData(message.getFilePaths().get(0));
            myViewHolder.shareContactName.setText(data.getName());

            int resId = message.isTypeOutbox() ? Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor());
            myViewHolder.shareContactName.setTextColor(resId);
            myViewHolder.shareContactNo.setTextColor(resId);
            myViewHolder.shareEmailContact.setTextColor(resId);
            myViewHolder.addContactButton.setTextColor(resId);

            if (data.getProfilePic() != null) {
                if (imageCache.getBitmapFromMemCache(message.getKeyString()) == null) {
                    imageCache.addBitmapToCache(message.getKeyString(), data.getProfilePic());
                }
                myViewHolder.shareContactImage.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
            }
            if (!TextUtils.isEmpty(data.getTelephoneNumber())) {
                myViewHolder.shareContactNo.setText(data.getTelephoneNumber());
            } else {
                myViewHolder.shareContactNo.setVisibility(View.GONE);
            }
            if (data.getEmail() != null) {
                myViewHolder.shareEmailContact.setText(data.getEmail());
            } else {
                myViewHolder.shareEmailContact.setVisibility(View.GONE);
            }


            myViewHolder.addContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (storagePermissionListener.isPermissionGranted()) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri outputUri = null;
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (Utils.hasNougat()) {
                            outputUri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                        } else {
                            outputUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                        }
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            intent.setDataAndType(outputUri, "text/x-vcard");
                            activityContext.startActivity(intent);
                        } else {
                            Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        storagePermissionListener.checkPermission(new ALStoragePermission() {
                            @Override
                            public void onAction(boolean didGrant) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_VIEW);
                                Uri outputUri = null;
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                if (Utils.hasNougat()) {
                                    outputUri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                                } else {
                                    outputUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                                }
                                if (intent.resolveActivity(context.getPackageManager()) != null) {
                                    intent.setDataAndType(outputUri, "text/x-vcard");
                                    activityContext.startActivity(intent);
                                } else {
                                    Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });

        } catch (Exception e) {
            Utils.printLog(context, "DetailedConvAdapter", "Exception in parsing");
        }

    }

    public void setMessageText(TextView messageTextView, Message message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            messageTextView.setText(Html.fromHtml(message.getMessage(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            messageTextView.setText(Html.fromHtml(message.getMessage()));
        }
    }

    private void loadContactImage(Contact contact, Contact contactDisplayName, Message messageObj, ImageView contactImage, TextView alphabeticTextView, TextView onlineTextView) {

        if (alphabeticTextView != null) {
            String contactNumber = "";
            char firstLetter = 0;
            if (contact != null) {
                contactNumber = contact.getDisplayName().toUpperCase();
                firstLetter = contact.getDisplayName().toUpperCase().charAt(0);
            } else if (channel != null && contactDisplayName != null) {
                firstLetter = contactDisplayName.getDisplayName().toUpperCase().charAt(0);
                contactNumber = contactDisplayName.getDisplayName().toUpperCase();
            }

            if (firstLetter != '+') {
                alphabeticTextView.setText(String.valueOf(firstLetter));
            } else if (!TextUtils.isEmpty(contactNumber) && contactNumber.length() >= 2) {
                alphabeticTextView.setText(String.valueOf(contactNumber.charAt(1)));
            }

            Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
            /*alphabeticTextView.setTextColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetTextColorMap.get(colorKey)));
            alphabeticTextView.setBackgroundResource(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey));*/
            GradientDrawable bgShape = (GradientDrawable) alphabeticTextView.getBackground();
            bgShape.setColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
        }

        if (contact != null && contact.isDrawableResources() && contactImage != null) {
            int drawableResourceId = context.getResources().getIdentifier(contact.getrDrawableName(), "drawable", context.getPackageName());
            contactImage.setImageResource(drawableResourceId);
            contactImage.setVisibility(View.VISIBLE);
            alphabeticTextView.setVisibility(View.GONE);
        } else if (contact != null && contactImage != null) {
            if (TextUtils.isEmpty(contact.getImageURL())) {
                contactImage.setVisibility(View.GONE);
                alphabeticTextView.setVisibility(View.VISIBLE);
            } else {
                contactImageLoader.loadImage(contact, contactImage, alphabeticTextView);
            }
        }

        if (contactDisplayName != null && contactDisplayName.isDrawableResources() && contactImage != null) {
            int drawableResourceId = context.getResources().getIdentifier(contactDisplayName.getrDrawableName(), "drawable", context.getPackageName());
            contactImage.setImageResource(drawableResourceId);
            contactImage.setVisibility(View.VISIBLE);
            alphabeticTextView.setVisibility(View.GONE);
        } else if (contactDisplayName != null && contactImage != null) {
            if (alCustomizationSettings.isGroupUsersOnlineStatus() && onlineTextView != null) {
                if (contactDisplayName.isConnected()) {
                    onlineTextView.setVisibility(View.VISIBLE);
                } else {
                    onlineTextView.setVisibility(View.GONE);
                }
            }
            if (TextUtils.isEmpty(contactDisplayName.getImageURL())) {
                contactImage.setVisibility(View.GONE);
                alphabeticTextView.setVisibility(View.VISIBLE);
            } else {
                contactImageLoader.loadImage(contactDisplayName, contactImage, alphabeticTextView);
            }
        }

    }

    private void showAttachmentIconAndText(TextView attachedFile, final Message message, final String mimeType) {

        String fileName = "";
        if (message.getFileMetas() == null && message.getFilePaths() != null) {
            fileName = message.getFilePaths().get(0).substring(message.getFilePaths().get(0).lastIndexOf("/") + 1);
        } else if (message.getFileMetas() != null) {
            fileName = message.getFileMetas().getName();
        }
        attachedFile.setTextColor(message.isTypeOutbox() ?
                Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));
        attachedFile.setText(fileName);
        attachedFile.setVisibility(View.VISIBLE);
        attachedFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (message.isAttachmentDownloaded()) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        Uri outputUri;
                        if (Utils.hasNougat()) {
                            outputUri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                        } else {
                            outputUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                        }
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            intent.setDataAndType(outputUri, mimeType);
                            activityContext.startActivity(intent);
                        } else {
                            Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Utils.printLog(context, TAG, "No application found to open this file");
                }
            }

        });
    }

    private void showPreview(Message message, ImageView preview, LinearLayout attachmentDownloadLayout) {
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.setLoadingImage(R.id.media_upload_progress_bar);
        imageThumbnailLoader.loadImage(message, preview);
        attachmentDownloadLayout.setVisibility(View.GONE);
    }

    private void showFullView(Message smListItem) {
        try {
            final String mimeType = FileUtils.getMimeType(smListItem.getFilePaths().get(0));
            if (mimeType != null) {
                if (mimeType.startsWith("image")) {
                    Intent intent = new Intent(activityContext, FullScreenImageActivity.class);
                    intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(smListItem, Message.class));
                    ((MobiComKitActivityInterface) activityContext).startActivityForResult(intent, MobiComKitActivityInterface.REQUEST_CODE_FULL_SCREEN_ACTION);
                }
                if (mimeType.startsWith("video")) {
                    if (smListItem.isAttachmentDownloaded()) {
                        Intent intentVideo = new Intent();
                        intentVideo.setAction(Intent.ACTION_VIEW);
                        Uri outputUri;
                        if (Utils.hasNougat()) {
                            outputUri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(smListItem.getFilePaths().get(0)));
                        } else {
                            outputUri = Uri.fromFile(new File(smListItem.getFilePaths().get(0)));
                        }
                        intentVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        if (intentVideo.resolveActivity(context.getPackageManager()) != null) {
                            intentVideo.setDataAndType(outputUri, "video/*");
                            activityContext.startActivity(intentVideo);
                        } else {
                            Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.printLog(context, TAG, "No application found to open this file");
        }

    }

    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message == null) {
            return 0;
        }
        if (message.isTempDateType()) {
            return 2;
        }
        if (message.isCustom()) {
            return 3;
        }
        if (message.isChannelCustomMessage()) {
            return 4;
        }
        if (message.isVideoCallMessage()) {
            return 5;
        }
        return message.isTypeOutbox() ? 1 : 0;
    }

    private Message getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public ViewGroup.LayoutParams getImageLayoutParam(boolean outBoxType) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
        ViewGroup.MarginLayoutParams params;
        if (outBoxType) {
            params = new RelativeLayout.LayoutParams(metrics.widthPixels + (int) wt_px * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins((int) wt_px, 0, (int) wt_px, 0);
        } else {
            params = new LinearLayout.LayoutParams(metrics.widthPixels - (int) wt_px * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 0);

        }
        return params;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                final FilterResults oReturn = new FilterResults();
                final List<Message> results = new ArrayList<Message>();
                if (originalList == null)
                    originalList = messageList;
                if (constraint != null) {
                    searchString = constraint.toString();
                    if (originalList != null && originalList.size() > 0) {
                        for (final Message message : originalList) {
                            if (message.getMessage().toLowerCase()
                                    .contains(constraint.toString())) {
                                results.add(message);


                            }
                        }
                    }
                    oReturn.values = results;
                } else {
                    oReturn.values = originalList;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                messageList = (ArrayList<Message>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private int indexOfSearchQuery(String message) {
        if (!TextUtils.isEmpty(searchString)) {
            return message.toLowerCase(Locale.getDefault()).indexOf(
                    searchString.toString().toLowerCase(Locale.getDefault()));
        }
        return -1;
    }

    public void refreshContactData() {
        if (contact != null) {
            contact = contactService.getContactById(contact.getContactIds());
        }
    }

    private boolean isNormalAttachment(Message message) {
        if (message.getFileMetas() != null) {
            return !(message.getFileMetas().getContentType().contains("image") || message.getFileMetas().getContentType().contains("video") || message.isContactMessage());
        } else if (message.getFilePaths() != null) {
            String filePath = message.getFilePaths().get(0);
            final String mimeType = FileUtils.getMimeType(filePath);
            if (mimeType != null) {
                return !(mimeType.contains("image") || mimeType.contains("video") || message.isContactMessage());
            }
        }
        return false;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        ImageView mapImageView;
        RelativeLayout chatLocation;
        TextView downloadSizeTextView;
        AttachmentView attachmentView;
        LinearLayout attachmentDownloadLayout;
        ImageView preview;
        LinearLayout attachmentRetry;
        RelativeLayout attachmentDownloadProgressLayout;
        RelativeLayout mainAttachmentLayout;
        LinearLayout mainContactShareLayout;
        ImageView videoIcon;
        ProgressBar mediaDownloadProgressBar;
        ProgressBar mediaUploadProgressBar;
        ImageView attachmentIcon, shareContactImage;
        TextView alphabeticTextView;
        CircleImageView contactImage;
        View messageTextLayout;
        TextView nameTextView;
        TextView attachedFile;
        ImageView sentOrReceived;
        TextView messageTextView;
        TextView createdAtTime;
        TextView onlineTextView;
        TextView selfDestruct;
        TextView deliveryStatus, shareContactName, shareContactNo, shareEmailContact;
        LinearLayout nameTextLayout;
        View view;
        RelativeLayout replyRelativeLayout;
        RelativeLayout imageViewRLayout;
        TextView replyMessageTextView;
        ImageView imageViewPhoto;
        TextView replyNameTextView;
        ImageView imageViewForAttachmentType;
        Button addContactButton;
        int position;
        LinearLayout richMessageLayout;
        RecyclerView richMessageContainer;

        public MyViewHolder(final View customView) {
            super(customView);

            position = getLayoutPosition();                //   getAdapterPosition();
            this.view = customView;
            mapImageView = (ImageView) customView.findViewById(R.id.static_mapview);
            chatLocation = (RelativeLayout) customView.findViewById(R.id.chat_location);
            preview = (ImageView) customView.findViewById(R.id.preview);
            attachmentView = (AttachmentView) customView.findViewById(R.id.main_attachment_view);
            attachmentIcon = (ImageView) customView.findViewById(R.id.attachmentIcon);
            downloadSizeTextView = (TextView) customView.findViewById(R.id.attachment_size_text);
            attachmentDownloadLayout = (LinearLayout) customView.findViewById(R.id.attachment_download_layout);
            attachmentRetry = (LinearLayout) customView.findViewById(R.id.attachment_retry_layout);
            attachmentDownloadProgressLayout = (RelativeLayout) customView.findViewById(R.id.attachment_download_progress_layout);
            mainAttachmentLayout = (RelativeLayout) customView.findViewById(R.id.attachment_preview_layout);
            mainContactShareLayout = (LinearLayout) customView.findViewById(R.id.contact_share_layout);
            videoIcon = (ImageView) customView.findViewById(R.id.video_icon);
            mediaDownloadProgressBar = (ProgressBar) customView.findViewById(R.id.media_download_progress_bar);
            mediaUploadProgressBar = (ProgressBar) customView.findViewById(R.id.media_upload_progress_bar);
            messageTextLayout = customView.findViewById(R.id.messageTextLayout);
            createdAtTime = (TextView) customView.findViewById(R.id.createdAtTime);
            messageTextView = (TextView) customView.findViewById(R.id.message);
            contactImage = (CircleImageView) customView.findViewById(R.id.contactImage);
            alphabeticTextView = (TextView) customView.findViewById(R.id.alphabeticImage);
            deliveryStatus = (TextView) customView.findViewById(R.id.status);
            selfDestruct = (TextView) customView.findViewById(R.id.selfDestruct);
            nameTextView = (TextView) customView.findViewById(R.id.name_textView);
            attachedFile = (TextView) customView.findViewById(R.id.attached_file);
            onlineTextView = (TextView) customView.findViewById(R.id.onlineTextView);
            nameTextLayout = (LinearLayout) customView.findViewById(R.id.nameTextLayout);
            replyRelativeLayout = (RelativeLayout) customView.findViewById(R.id.reply_message_layout);
            imageViewRLayout = (RelativeLayout) customView.findViewById(R.id.imageViewRLayout);
            replyMessageTextView = (TextView) customView.findViewById(R.id.messageTextView);
            imageViewPhoto = (ImageView) customView.findViewById(R.id.imageViewForPhoto);
            replyNameTextView = (TextView) customView.findViewById(R.id.replyNameTextView);
            imageViewForAttachmentType = (ImageView) customView.findViewById(R.id.imageViewForAttachmentType);

            shareContactImage = (ImageView) mainContactShareLayout.findViewById(R.id.contact_share_image);
            shareContactName = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_tv_name);
            shareContactNo = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_tv_no);
            shareEmailContact = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_emailId);
            addContactButton = (Button) mainContactShareLayout.findViewById(R.id.contact_share_add_btn);

            richMessageLayout = (LinearLayout) customView.findViewById(R.id.alRichMessageView);
            richMessageContainer = (RecyclerView) customView.findViewById(R.id.alRichMessageContainer);

            customView.setOnCreateContextMenuListener(this);

            mapImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            preview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });

            if (contactImage != null) {
                contactImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendCallback(messageList, getLayoutPosition());
                    }
                });
            }
            if (alphabeticTextView != null) {
                alphabeticTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendCallback(messageList, getLayoutPosition());
                    }
                });
            }

            attachmentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(R.string.messageOptions);
            int positionInSmsList = this.getLayoutPosition();

            if (positionInSmsList < 0 || messageList.isEmpty()) {
                return;
            }

            Message message = messageList.get(positionInSmsList);

            if (message.isTempDateType() || message.isCustom() || message.isChannelCustomMessage()) {
                return;
            }

            String[] menuItems = context.getResources().getStringArray(R.array.menu);

            for (int i = 0; i < menuItems.length; i++) {

                if (!(message.isGroupMessage() && message.isTypeOutbox() && message.isSentToServer()) && menuItems[i].equals(context.getResources().getString(R.string.info))) {
                    continue;
                }

                if ((message.hasAttachment() || message.getContentType() == Message.ContentType.LOCATION.getValue() || message.isVideoOrAudioCallMessage()) &&
                        menuItems[i].equals(context.getResources().getString(R.string.copy))) {
                    continue;
                }

                if (menuItems[i].equals(context.getResources().getString(R.string.forward)) && !alCustomizationSettings.isForwardOption()) {
                    continue;
                }

                if (((channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || message.isCall() || (message.hasAttachment() && !message.isAttachmentDownloaded()) || message.isVideoOrAudioCallMessage()) && (menuItems[i].equals(context.getResources().getString(R.string.forward)) ||
                        menuItems[i].equals(context.getResources().getString(R.string.resend)))) {
                    continue;
                }
                if (menuItems[i].equals(context.getResources().getString(R.string.resend)) && (!message.isSentViaApp() || message.isSentToServer() || message.isVideoOrAudioCallMessage())) {
                    continue;
                }

                if (menuItems[i].equals(context.getResources().getString(R.string.reply)) && (!alCustomizationSettings.isReplyOption() || message.isAttachmentUploadInProgress() || TextUtils.isEmpty(message.getKeyString()) || !message.isSentToServer() || (message.hasAttachment() && !message.isAttachmentDownloaded()) || (channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()) && !ChannelService.getInstance(context).processIsUserPresentInChannel(channel.getKey())) || message.isVideoOrAudioCallMessage() || contact != null && contact.isDeleted())) {
                    continue;
                }

                if (menuItems[i].equals(context.getResources().getString(R.string.delete)) && (TextUtils.isEmpty(message.getKeyString()) || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())))) {
                    continue;
                }
                if (menuItems[i].equals(context.getResources().getString(R.string.info)) && (TextUtils.isEmpty(message.getKeyString()) || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || message.isVideoOrAudioCallMessage() || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())))) {
                    continue;
                }
                if (menuItems[i].equals(context.getResources().getString(R.string.share)) && (message.isAttachmentUploadInProgress() || message.getFilePaths() == null || !(new File(message.getFilePaths().get(0)).exists()))) {
                    continue;
                }
                if(menuItems[i].equals("identify")){
                    continue;
                }

                MenuItem item = menu.add(Menu.NONE, i, i, menuItems[i]);
                item.setOnMenuItemClickListener(onEditMenu);
            }
        }

        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int position = getLayoutPosition();
                if (position < 0 || messageList.isEmpty()) {
                    return true;
                }
                return contextMenuClickListener == null || contextMenuClickListener.onItemClick(position, item);
            }
        };
    }

    class MyViewHolder2 extends RecyclerView.ViewHolder {
        TextView dateView;
        TextView dayTextView;

        public MyViewHolder2(View itemView) {
            super(itemView);
            dateView = (TextView) itemView.findViewById(R.id.chat_screen_date);
            dayTextView = (TextView) itemView.findViewById(R.id.chat_screen_day);
        }
    }

    class MyViewHolder3 extends RecyclerView.ViewHolder {
        TextView customContentTextView;

        public MyViewHolder3(View itemView) {
            super(itemView);
            customContentTextView = (TextView) itemView.findViewById(R.id.applozic_custom_message_layout_content);
        }
    }

    class MyViewHolder4 extends RecyclerView.ViewHolder {
        TextView channelMessageTextView;

        public MyViewHolder4(View itemView) {
            super(itemView);
            channelMessageTextView = (TextView) itemView.findViewById(R.id.channel_message);
        }
    }

    class MyViewHolder5 extends RecyclerView.ViewHolder {
        TextView statusTextView;
        TextView timeTextView;
        TextView durationTextView;
        ImageView imageView;

        public MyViewHolder5(View itemView) {
            super(itemView);
            statusTextView = (TextView) itemView.findViewById(R.id.applozic_call_status);
            timeTextView = (TextView) itemView.findViewById(R.id.applozic_call_timing);
            durationTextView = (TextView) itemView.findViewById(R.id.applozic_call_duration);
            imageView = (ImageView) itemView.findViewById(R.id.applozic_call_image_type);
        }
    }

    public void sendCallback(List<Message> messageList, int pos) {
        Message message = messageList.get(pos);
        if (message != null) {
            if (context instanceof ALProfileClickListener) {
                ((ALProfileClickListener) context).onClick(activityContext, message.getTo(), channel, false);
            }
        }
    }
}

