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
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by adarsh on 4/7/15.
 */
public class DetailedConversationAdapter extends ArrayAdapter<Message> {

    private static final String TAG = "DetailedConversation";

    private static final int FILE_THRESOLD_SIZE = 400;

    public ImageLoader contactImageLoader, loadImage;
    public String searchString;
    AlCustomizationSettings alCustomizationSettings;
    private Context context;
    private Contact contact;
    private Channel channel;
    private boolean individual;
    private Drawable sentIcon;
    private Drawable deliveredIcon;
    private Drawable pendingIcon;
    private Drawable scheduledIcon;
    private ImageLoader imageThumbnailLoader;
    private TextView downloadSizeTextView;
    private EmojiconHandler emojiconHandler;
    private FileClientService fileClientService;
    private MessageDatabaseService messageDatabaseService;
    private BaseContactService contactService;
    private Contact senderContact;
    private long deviceTimeOffset = 0;
    private Class<?> messageIntentClass;
    private List<Message> messageList;
    private List<Message> originalList;
    private MobiComConversationService conversationService;
    private ImageCache imageCache;
    private AlphabetIndexer mAlphabetIndexer; // Stores the AlphabetIndexer instance
    private TextAppearanceSpan highlightTextSpan;


    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Channel channel, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, textViewResourceId, messageList, null, channel, messageIntentClass, emojiconHandler);
    }

    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, Contact contact, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        this(context, textViewResourceId, messageList, contact, null, messageIntentClass, emojiconHandler);
    }

    public DetailedConversationAdapter(final Context context, int textViewResourceId, List<Message> messageList, final Contact contact, Channel channel, Class messageIntentClass, EmojiconHandler emojiconHandler) {
        super(context, textViewResourceId, messageList);
        this.messageIntentClass = messageIntentClass;
        this.context = context;
        this.contact = contact;
        this.channel = channel;
        this.emojiconHandler = emojiconHandler;
        this.individual = (contact != null || channel != null);
        this.fileClientService = new FileClientService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.conversationService = new MobiComConversationService(context);
        this.contactService = new AppContactService(context);
        this.imageCache = ImageCache.getInstance(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        this.senderContact = contactService.getContactById(MobiComUserPreference.getInstance(context).getUserId());
        this.messageList = messageList;
        contactImageLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage(getContext(), (Contact) data);
            }
        };
        contactImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_180_holo_light);
        contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);

        loadImage = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadMessageImage(getContext(), (String) data);
            }
        };
        loadImage.setImageFadeIn(false);
        loadImage.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        imageThumbnailLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadThumbnailImage(getContext(), (Message) data, getImageLayoutParam(false).width, getImageLayoutParam(false).height);
            }
        };
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);

        sentIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_sent);
        deliveredIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered);
        pendingIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_pending);
        scheduledIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_schedule);
        final String alphabet = context.getString(R.string.alphabet);
        mAlphabetIndexer = new AlphabetIndexer(null, 1, alphabet);
        highlightTextSpan = new TextAppearanceSpan(context, R.style.searchTextHiglight);
    }

    public void setAlCustomizationSettings(AlCustomizationSettings alCustomizationSettings) {
        this.alCustomizationSettings = alCustomizationSettings;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View customView;
        deviceTimeOffset = MobiComUserPreference.getInstance(context).getDeviceTimeOffset();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Message message = getItem(position);
        int type = getItemViewType(position);
        if (type == 2) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            SimpleDateFormat simpleDateFormatDay = new SimpleDateFormat("EEEE");
            customView = inflater.inflate(R.layout.mobicom_date_layout, parent, false);
            TextView dateView = (TextView) customView.findViewById(R.id.chat_screen_date);
            TextView dayTextView = (TextView) customView.findViewById(R.id.chat_screen_day);
            Date date = new Date(message.getCreatedAtTime());
            dateView.setTextColor(Color.parseColor(alCustomizationSettings.getConversationDateTextColor().trim()));
            dayTextView.setTextColor(Color.parseColor(alCustomizationSettings.getConversationDayTextColor().trim()));

            if (DateUtils.isSameDay(message.getCreatedAtTime())) {
                dayTextView.setVisibility(View.VISIBLE);
                dayTextView.setText(R.string.today);
            } else {
                dayTextView.setVisibility(View.VISIBLE);
                dateView.setVisibility(View.VISIBLE);
                dayTextView.setText(simpleDateFormatDay.format(date));
                dateView.setText(simpleDateFormat.format(date));
            }
            return customView;
        } else if (type == 3) {
            customView = inflater.inflate(R.layout.applozic_custom_message_layout, parent, false);
            TextView customContentTextView = (TextView) customView.findViewById(R.id.applozic_custom_message_layout_content);
            customContentTextView.setText(message.getMessage());
            customContentTextView.setVisibility(View.VISIBLE);
            return customView;
        } else if (type == 4) {
            customView = inflater.inflate(R.layout.applozic_channel_custom_message_layout, parent, false);
            TextView channelMessageTextView = (TextView) customView.findViewById(R.id.channel_message);
            GradientDrawable bgGradientDrawable = (GradientDrawable) channelMessageTextView.getBackground();
            bgGradientDrawable.setColor(Color.parseColor(alCustomizationSettings.getChannelCustomMessageBgColor()));
            bgGradientDrawable.setStroke(3, Color.parseColor(alCustomizationSettings.getChannelCustomMessageBorderColor()));
            channelMessageTextView.setTextColor(Color.parseColor(alCustomizationSettings.getChannelCustomMessageTextColor()));
            channelMessageTextView.setText(message.getMessage());
            return customView;
        } else if (type == 5) {
            customView = inflater.inflate(R.layout.applozic_call_layout, parent, false);
            populateVideoCall(customView, message);
            return customView;

        } else if (type == 0) {
            customView = inflater.inflate(R.layout.mobicom_received_message_list_view, parent, false);
        } else {
            customView = inflater.inflate(R.layout.mobicom_sent_message_list_view, parent, false);
        }
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
                    //contact.setFormattedContactNumber(ContactNumberUtils.getPhoneNumber(items.get(0), MobiComUserPreference.getInstance(context).getCountryCode()));
                } else {
                    receiverContact = contactService.getContactReceiver(items, userIds);
                }
            } else {
                if (!TextUtils.isEmpty(message.getContactIds())) {
                    contactDisplayName = contactService.getContactById(message.getContactIds());
                }
            }

            Configuration config = context.getResources().getConfiguration();
            View messageTextLayout = customView.findViewById(R.id.messageTextLayout);
            //TextView smReceivers = (TextView) customView.findViewById(R.id.smReceivers);
            //TextView status = (TextView) customView.findViewById(R.id.status);
            TextView createdAtTime = (TextView) customView.findViewById(R.id.createdAtTime);
            TextView messageTextView = (TextView) customView.findViewById(R.id.message);
            TextView onlineTextView = (TextView) customView.findViewById(R.id.onlineTextView);
            CircleImageView contactImage = (CircleImageView) customView.findViewById(R.id.contactImage);
            //ImageView contactImage = (ImageView) customView.findViewById(R.id.contactImage);
            TextView alphabeticTextView = (TextView) customView.findViewById(R.id.alphabeticImage);
            ImageView sentOrReceived = (ImageView) customView.findViewById(R.id.sentOrReceivedIcon);
            ImageView mapImageView = (ImageView) customView.findViewById(R.id.static_mapview);
            LinearLayout nameTextLayout = (LinearLayout) customView.findViewById(R.id.nameTextLayout);
            RelativeLayout chatLocation = (RelativeLayout) customView.findViewById(R.id.chat_location);
            TextView deliveryStatus = (TextView) customView.findViewById(R.id.status);
            TextView selfDestruct = (TextView) customView.findViewById(R.id.selfDestruct);
            final ImageView preview = (ImageView) customView.findViewById(R.id.preview);
            final AttachmentView attachmentView = (AttachmentView) customView.findViewById(R.id.main_attachment_view);
            TextView attachedFile = (TextView) customView.findViewById(R.id.attached_file);
            final ImageView attachmentIcon = (ImageView) customView.findViewById(R.id.attachmentIcon);
            downloadSizeTextView = (TextView) customView.findViewById(R.id.attachment_size_text);
            final LinearLayout attachmentDownloadLayout = (LinearLayout) customView.findViewById(R.id.attachment_download_layout);
            final LinearLayout attachmentRetry = (LinearLayout) customView.findViewById(R.id.attachment_retry_layout);
            final RelativeLayout attachmentDownloadProgressLayout = (RelativeLayout) customView.findViewById(R.id.attachment_download_progress_layout);
            final RelativeLayout mainAttachmentLayout = (RelativeLayout) customView.findViewById(R.id.attachment_preview_layout);
            LinearLayout mainContactShareLayout = (LinearLayout) customView.findViewById(R.id.contact_share_layout);
            final ImageView videoIcon = (ImageView) customView.findViewById(R.id.video_icon);

            final ProgressBar mediaDownloadProgressBar = (ProgressBar) customView.findViewById(R.id.media_download_progress_bar);
            final ProgressBar mediaUploadProgressBar = (ProgressBar) customView.findViewById(R.id.media_upload_progress_bar);
            TextView nameTextView = (TextView) customView.findViewById(R.id.name_textView);
            RelativeLayout replyRelativeLayout = (RelativeLayout) customView.findViewById(R.id.reply_message_layout);
            RelativeLayout imageViewRLayout = (RelativeLayout) customView.findViewById(R.id.imageViewRLayout);
            TextView replyMessageTextView = (TextView) customView.findViewById(R.id.messageTextView);
            ImageView imageViewPhoto = (ImageView) customView.findViewById(R.id.imageViewForPhoto);
            TextView replyNameTextView = (TextView) customView.findViewById(R.id.replyNameTextView);
            ImageView imageViewForAttachmentType = (ImageView) customView.findViewById(R.id.imageViewForAttachmentType);
            createdAtTime.setTextColor(Color.parseColor(alCustomizationSettings.getMessageTimeTextColor()));

            if (message.getMetadata() != null && !message.getMetadata().isEmpty() && message.getMetadata().containsKey(Message.MetaDataType.AL_REPLY.getValue())) {
                final Message msg = messageDatabaseService.getMessage(message.getMetaDataValueForKey(Message.MetaDataType.AL_REPLY.getValue()));
                if (msg != null) {
                    String displayName;

                    replyRelativeLayout.setBackgroundColor(message.isTypeOutbox() ?
                            Color.parseColor(alCustomizationSettings.getReplyMessageLayoutSentMessageBackground()) : Color.parseColor(alCustomizationSettings.getReplyMessageLayoutReceivedMessageBackground()));

                    replyNameTextView.setTextColor(message.isTypeOutbox() ?
                            Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));

                    replyMessageTextView.setTextColor(message.isTypeOutbox() ?
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

                    replyNameTextView.setText(displayName);
                    if (msg.hasAttachment()) {
                        FileMeta fileMeta = msg.getFileMetas();
                        imageViewForAttachmentType.setVisibility(View.VISIBLE);
                        if (fileMeta.getContentType().contains("image")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_image_camera_alt);
                            if (TextUtils.isEmpty(msg.getMessage())) {
                                replyMessageTextView.setText(context.getString(R.string.photo_string));
                            } else {
                                replyMessageTextView.setText(msg.getMessage());
                            }
                            imageViewPhoto.setVisibility(View.VISIBLE);
                            imageViewRLayout.setVisibility(View.VISIBLE);
                            imageThumbnailLoader.loadImage(msg, imageViewPhoto);
                        } else if (fileMeta.getContentType().contains("video")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_video);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                                    imageViewForAttachmentType.setScaleX(-1);
                                }
                            }
                            if (TextUtils.isEmpty(msg.getMessage())) {
                                replyMessageTextView.setText(context.getString(R.string.video_string));
                            } else {
                                replyMessageTextView.setText(msg.getMessage());
                            }
                            imageViewPhoto.setVisibility(View.VISIBLE);
                            imageViewRLayout.setVisibility(View.VISIBLE);
                            if (msg.getFilePaths() != null && msg.getFilePaths().size() > 0) {
                                if (imageCache.getBitmapFromMemCache(msg.getKeyString()) != null) {
                                    imageViewPhoto.setImageBitmap(imageCache.getBitmapFromMemCache(msg.getKeyString()));
                                } else {
                                    imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(msg.getFilePaths().get(0)));
                                    imageViewPhoto.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(msg.getFilePaths().get(0)));
                                }
                            }
                        } else if (fileMeta.getContentType().contains("audio")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_music_note);
                            if (TextUtils.isEmpty(msg.getMessage())) {
                                replyMessageTextView.setText(context.getString(R.string.audio_string));
                            } else {
                                replyMessageTextView.setText(msg.getMessage());
                            }
                            imageViewPhoto.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else if (msg.isContactMessage()) {
                            MobiComVCFParser parser = new MobiComVCFParser();
                            try {
                                VCFContactData data = parser.parseCVFContactData(msg.getFilePaths().get(0));
                                if (data != null) {
                                    imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                                    replyMessageTextView.setText(context.getString(R.string.contact_string));
                                    replyMessageTextView.append(" " + data.getName());
                                }
                            } catch (Exception e) {
                                imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                                replyMessageTextView.setText(context.getString(R.string.contact_string));
                            }
                            imageViewPhoto.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_attachment);
                            if (TextUtils.isEmpty(msg.getMessage())) {
                                replyMessageTextView.setText(context.getString(R.string.attachment_string));
                            } else {
                                replyMessageTextView.setText(msg.getMessage());
                            }
                            imageViewPhoto.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        }
                        imageViewForAttachmentType.setColorFilter(Color.parseColor(message.isTypeOutbox() ? alCustomizationSettings.getSentMessageTextColor() : alCustomizationSettings.getReceivedMessageTextColor()));
                    } else if (msg.getContentType() == Message.ContentType.LOCATION.getValue()) {
                        imageViewForAttachmentType.setVisibility(View.VISIBLE);
                        imageViewPhoto.setVisibility(View.VISIBLE);
                        imageViewRLayout.setVisibility(View.VISIBLE);
                        replyMessageTextView.setText(context.getString(R.string.al_location_string));
                        imageViewForAttachmentType.setColorFilter(Color.parseColor(message.isTypeOutbox() ? alCustomizationSettings.getSentMessageTextColor() : alCustomizationSettings.getReceivedMessageTextColor()));
                        imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_location_on_white_24dp);
                        loadImage.setLoadingImage(R.drawable.applozic_map_offline_thumbnail);
                        loadImage.loadImage(LocationUtils.loadStaticMap(msg.getMessage()), imageViewPhoto);
                    } else {
                        imageViewForAttachmentType.setVisibility(View.GONE);
                        imageViewRLayout.setVisibility(View.GONE);
                        imageViewPhoto.setVisibility(View.GONE);
                        replyMessageTextView.setText(msg.getMessage());
                    }
                    replyRelativeLayout.setVisibility(View.VISIBLE);
                    replyRelativeLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((OnClickReplyInterface) context).onClickOnMessageReply(msg);
                        }
                    });
                }
            }


            if (TextUtils.isEmpty(message.getMessage())) {
                messageTextView.setVisibility(View.GONE);
            }
            if (attachedFile != null) {
                attachedFile.setText("");
                attachedFile.setVisibility(View.GONE);
            }

            if (attachmentIcon != null) {
                attachmentIcon.setVisibility(View.GONE);
            }

            if (channel != null && nameTextView != null && contactDisplayName != null) {
                nameTextView.setVisibility(Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) ? View.GONE : View.VISIBLE);
                if (alCustomizationSettings.isLaunchChatFromProfilePicOrName()) {
                    nameTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(context, ConversationActivity.class);
                            intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                            if (message.getConversationId() != null) {
                                intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                            }
                            context.startActivity(intent);
                        }
                    });
                }
                String userId = contactDisplayName.getDisplayName();
                char firstLetter = contactDisplayName.getDisplayName().charAt(0);
                if (userId.length() > 0) {
                    nameTextView.setText(String.valueOf(userId));
                }
                Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                nameTextView.setTextColor(context.getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
            }

            attachmentDownloadLayout.setVisibility(View.GONE);
            preview.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
            attachmentView.setVisibility(View.GONE);
            if (message.isTypeOutbox() && !message.isCanceled()) {
                mediaUploadProgressBar.setVisibility(message.isAttachmentUploadInProgress() ? View.VISIBLE : View.GONE);
            } else {
                mediaUploadProgressBar.setVisibility(View.GONE);
            }
            if (attachedFile != null) {
                attachedFile.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);
            }
            //Todo: show progress for download image of type inbox

            if (individual && message.getTimeToLive() != null) {
                selfDestruct
                        .setText("Self destruct in " + message.getTimeToLive() + " mins");
                selfDestruct.setVisibility(View.VISIBLE);
            } else {
                selfDestruct.setText("");
                selfDestruct.setVisibility(View.GONE);
            }

            if (sentOrReceived != null) {
                if ((!message.isCall()) || message.isDummyEmptyMessage()) {
                    sentOrReceived.setVisibility(View.GONE);
                } else if (message.isCall()) {
                    sentOrReceived.setImageResource(R.drawable.applozic_ic_action_call_holo_light);
                } else if (getItemViewType(position) == 0) {
                    sentOrReceived.setImageResource(R.drawable.mobicom_social_forward);
                } else {
                    sentOrReceived.setImageResource(R.drawable.mobicom_social_reply);
                }

                if (message.isCall()) {
                    messageTextView.setTextColor(context.getResources().getColor(message.isIncomingCall() ? R.color.incoming_call : R.color.outgoing_call));
                }
            }

            if (nameTextLayout != null && contact != null) {
                nameTextLayout.setVisibility(View.GONE);
            }

            if (message.isCall() || message.isDummyEmptyMessage()) {
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else if (!message.isSentToServer() && message.isTypeOutbox()) {
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, message.getScheduledAt() != null ? scheduledIcon : pendingIcon, null);
            } else if (message.getKeyString() != null && message.isTypeOutbox() && message.isSentToServer()) {
                Drawable statusIcon;
                if (message.isDeliveredAndRead()) {
                    statusIcon = getContext().getResources().getDrawable(R.drawable.applozic_ic_action_message_read);
                } else {
                    statusIcon = (message.getDelivered() || (contact != null && new Support(context).isSupportNumber(contact.getFormattedContactNumber())) ?
                            deliveredIcon : (message.getScheduledAt() != null ? scheduledIcon : sentIcon));
                }
                createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, statusIcon, null);
            }

            if (message.isCall()) {
                deliveryStatus.setText("");
            }/* else if (message.getType().equals(Message.MessageType.MT_OUTBOX.getValue()) || message.getType().equals(Message.MessageType.MT_INBOX.getValue())) {
                deliveryStatus.setText("via MT");
            } else {
                deliveryStatus.setText("via Carrier");
            }*/

            if (contactDisplayName != null && contactImage != null && alCustomizationSettings.isLaunchChatFromProfilePicOrName()) {
                contactImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ConversationActivity.class);
                        intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                        if (message.getConversationId() != null) {
                            intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                        }
                        context.startActivity(intent);
                    }
                });

                alphabeticTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ConversationActivity.class);
                        intent.putExtra(ConversationUIService.USER_ID, message.getContactIds());
                        if (message.getConversationId() != null) {
                            intent.putExtra(ConversationUIService.CONVERSATION_ID, message.getConversationId());
                        }
                        context.startActivity(intent);
                    }
                });

            }
            if (message.isTypeOutbox()) {
                loadContactImage(senderContact, contactDisplayName, message, contactImage, alphabeticTextView, onlineTextView);
            } else {
                loadContactImage(receiverContact, contactDisplayName, message, contactImage, alphabeticTextView, onlineTextView);
            }

            ApplozicDocumentView audioView =  new ApplozicDocumentView(this.context);
            audioView.inflateViewWithMessage(customView,message);
            audioView.hideView(true);
            if (message.hasAttachment() && attachedFile != null & !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                mainAttachmentLayout.setLayoutParams(getImageLayoutParam(false));
                if (message.getFileMetas() != null && (message.getFileMetas().getContentType().contains("image") || message.getFileMetas().getContentType().contains("video"))) {
                    attachedFile.setVisibility(View.GONE);
                }
                if (message.isAttachmentDownloaded()) {
                    preview.setVisibility(View.GONE);
                    String[] filePaths = new String[message.getFilePaths().size()];
                    int i = 0;
                    for (final String filePath : message.getFilePaths()) {
                        filePaths[i++] = filePath;
                        final String mimeType = FileUtils.getMimeType(filePath);
                        if (mimeType != null && mimeType.startsWith("image")) {
                            attachmentView.setMessage(message);
                            attachmentView.setProressBar(mediaDownloadProgressBar);
                            attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                            attachmentView.setVisibility(View.VISIBLE);
                            attachedFile.setVisibility(View.GONE);
                        } else if (mimeType != null && mimeType.startsWith("video")) {
                            preview.setVisibility(View.VISIBLE);
                            videoIcon.setVisibility(View.VISIBLE);
                            attachmentDownloadLayout.setVisibility(View.GONE);
                            attachedFile.setVisibility(View.GONE);
                            if (imageCache.getBitmapFromMemCache(message.getKeyString()) != null) {
                                preview.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
                            } else {
                                imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(filePath));
                                preview.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(filePath));
                            }
                        } else {
                            showAttachmentIconAndText(attachedFile, message, mimeType);
                        }
                    }
                } else if (message.isAttachmentUploadInProgress()) {
                    //showPreview(smListItem, preview, attachmentDownloadLayout);
                    attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                    attachmentView.setProressBar(mediaDownloadProgressBar);
                    attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                    attachmentView.setMessage(message);
                    attachmentView.setVisibility(View.VISIBLE);
                } else if (AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                    //ondraw is called and thread is assigned to the attachment view...
                    attachmentView.setMessage(message);
                    attachmentView.setVisibility(View.VISIBLE);
                    attachmentView.setProressBar(mediaDownloadProgressBar);
                    attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                    showPreview(message, preview, attachmentDownloadLayout);
                    FileMeta fileMeta = message.getFileMetas();
                    final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                    if (!fileMeta.getContentType().contains("image") && !fileMeta.getContentType().contains("video")) {
                        showAttachmentIconAndText(attachedFile, message, mimeType);
                    }
                    downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                    attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                    attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                } else {
                    String fileKeys = message.getFileMetaKeyStrings();
                    int i = 0;
                    showPreview(message, preview, attachmentDownloadLayout);
                    //TODO: while doing multiple image support in single sms ...we might improve this
                    // for (String fileKey : message.getFileMetaKeyStrings()) {
                    if (message.getFileMetas() != null) {
                        FileMeta fileMeta = message.getFileMetas();
                        attachmentDownloadLayout.setVisibility(View.VISIBLE);
                        attachmentDownloadProgressLayout.setVisibility(View.GONE);
                        downloadSizeTextView.setText(fileMeta.getSizeInReadableFormat());
                        final String mimeType = FileUtils.getMimeType(fileMeta.getName());
                        if (!fileMeta.getContentType().contains("image") && !fileMeta.getContentType().contains("video")) {
                            showAttachmentIconAndText(attachedFile, message, mimeType);
                        }

                    }

                    //  }

                }
                if (isNormalAttachment(message)) {
                    attachedFile.setVisibility(View.GONE);
                    mainAttachmentLayout.setVisibility(View.GONE);
                    mainContactShareLayout.setVisibility(View.GONE);
                    chatLocation.setVisibility(View.GONE);
                    audioView.hideView(false);
                    createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
                    return customView;
                }
            }
            if (message.isCanceled()) {
                attachmentRetry.setVisibility(View.VISIBLE);
            }
            attachmentRetry.setOnClickListener(new View.OnClickListener() {
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
                        Toast.makeText(context, context.getString(R.string.applozic_resending_attachment), Toast.LENGTH_SHORT).show();
                        mediaUploadProgressBar.setVisibility(View.VISIBLE);
                        attachmentRetry.setVisibility(View.GONE);
                        //updating Cancel Flag to smListItem....
                        message.setCanceled(false);
                        messageDatabaseService.updateCanceledFlag(message.getMessageId(), 0);
                        conversationService.sendMessage(message, messageIntentClass);
                    } else {
                        Toast.makeText(context, context.getString(R.string.internet_connection_not_available), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            attachmentDownloadProgressLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    attachmentView.setVisibility(View.GONE);
                    attachmentView.cancelDownload();
                    attachmentDownloadProgressLayout.setVisibility(View.GONE);
                    message.setAttDownloadInProgress(false);
                }
            });

            //final ProgressBar mediaDownloadProgressBar = mediaDownloadProgressBar;
            preview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO: 1. get the image Size and decide if we can download directly
                    //2. if already downloaded to ds card show it directly ....
                    //3. if deleted from sd crad only ...ask user to download it again or skip ...
                    if (message.getContentType() == Message.ContentType.TEXT_URL.getValue()) {
                        return;
                    }
                    if (message.isAttachmentDownloaded()) {
                        showFullView(message);
                        return;
                    } if ((message.isTypeOutbox() && message.isSentToServer()) || (!message.isTypeOutbox())) {
                        attachmentDownloadLayout.setVisibility(View.GONE);
                        attachmentView.setProressBar(mediaDownloadProgressBar);
                        attachmentView.setDownloadProgressLayout(attachmentDownloadProgressLayout);
                        attachmentView.setMessage(message);
                        attachmentView.setVisibility(View.VISIBLE);
                        attachmentDownloadProgressLayout.setVisibility(View.VISIBLE);
                    }

                }
            });
            preview.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            attachmentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFullView(message);
                }
            });
            attachmentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            if (attachedFile != null) {
                attachedFile.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return false;
                    }
                });
            }
            if (message.getScheduledAt() != null) {
                createdAtTime.setText(DateUtils.getFormattedDate(message.getScheduledAt()));
            } else if (createdAtTime != null && message.isDummyEmptyMessage()) {
                createdAtTime.setText("");
            } else if (createdAtTime != null) {
                createdAtTime.setText(DateUtils.getFormattedDate(message.getCreatedAtTime()));
            }
            String mimeType = "";
            if (messageTextView != null) {
                messageTextView.setTextColor(message.isTypeOutbox() ?
                        Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor()));
                messageTextView.setLinkTextColor(message.isTypeOutbox() ?
                        Color.parseColor(alCustomizationSettings.getSentMessageLinkTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageLinkTextColor()));

                if (message.getContentType() == Message.ContentType.TEXT_URL.getValue()) {
                    try {
                        attachedFile.setVisibility(View.GONE);
                        preview.setVisibility(View.VISIBLE);
                        messageTextView.setText(message.getMessage());
                        loadImage.setImageFadeIn(false);
                        loadImage.loadImage(message.getFileMetas().getBlobKeyString(), preview);
                        attachmentDownloadLayout.setVisibility(View.GONE);
                    } catch (Exception e) {
                    }
                } else if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                    //                  attachedFile.setVisibility(View.GONE);
                    //                preview.setVisibility(View.GONE);

                    chatLocation.setLayoutParams(getImageLayoutParam(false));
                    chatLocation.setVisibility(View.VISIBLE);

                    loadImage.setImageFadeIn(false);
                    //Default image while loading image.
                    loadImage.setLoadingImage(R.drawable.applozic_map_offline_thumbnail);
                    loadImage.loadImage(LocationUtils.loadStaticMap(message.getMessage()), mapImageView);
                    messageTextView.setVisibility(View.GONE);

                    mapImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?q=" + LocationUtils.getLocationFromMessage(message.getMessage()));
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            getContext().startActivity(intent);

                        }
                    });


                } else if (message.getContentType() == Message.ContentType.PRICE.getValue()) {
                    messageTextView.setText(ConversationUIService.FINAL_PRICE_TEXT + message.getMessage());
                } else if ((message.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) && !message.isAttachmentDownloaded()) {
                    preview.setVisibility(View.VISIBLE);
                    preview.setImageResource(R.drawable.applozic_video_default_thumbnail);
                } else if (message.getContentType() == Message.ContentType.TEXT_HTML.getValue()) {
                    messageTextView.setText(Html.fromHtml(message.getMessage()));
                } else {
                    messageTextView.setText(EmoticonUtils.getSmiledText(context, message.getMessage(), emojiconHandler));
                    if (mimeType != null && attachmentIcon != null) {
                        if (mimeType.startsWith("image")) {
                            attachmentIcon.setImageResource(R.drawable.applozic_ic_action_camera);
                        } else if (mimeType.startsWith("video")) {
                            attachmentIcon.setImageResource(R.drawable.applozic_ic_action_video);
                        }
                    }
                }

                if (messageTextLayout != null) {
                    GradientDrawable bgShape = (GradientDrawable) messageTextLayout.getBackground();
                    bgShape.setColor(message.isTypeOutbox() ?
                            Color.parseColor(alCustomizationSettings.getSentMessageBackgroundColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageBackgroundColor()));
                    bgShape.setStroke(3, message.isTypeOutbox() ?
                            Color.parseColor(alCustomizationSettings.getSentMessageBorderColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageBackgroundColor()));
                }
               /* if (messageTextLayout != null) {
                    //messageTextLayout.setBackgroundResource(messageTypeColorMap.get(message.getType()));
                    *//*messageTextLayout.setBackgroundColor(message.isTypeOutbox() ?
                            applozicSetting.getSentMessageBackgroundColor() : applozicSetting.getReceivedMessageBackgroundColor());*//*

                    if (message.hasAttachment()) {
                        if (TextUtils.isEmpty(message.getMessage())) {
                            messageTextView.setBackgroundColor(context.getResources().getColor(R.color.conversation_list_background));
                        } else {
                            if (message.isTypeOutbox()) {
                                messageTextView.setBackgroundColor(context.getResources().getColor(R.color.sent_message_bg_color));
                            } else {
                                messageTextView.setBackgroundColor(context.getResources().getColor(R.color.received_message_bg_color));
                            }
                        }
                        //messageTextLayout.setLayoutParams(getImageLayoutParam(message.isTypeOutbox()));
                        //messageTextLayout.setBackgroundResource(R.drawable.send_sms_background);
                        customView.findViewById(R.id.messageTextInsideLayout).setBackgroundResource(R.color.attachment_background_color);
                    }
                }*/
            }
            //Handling contact share
            if (message.isContactMessage()) {
                attachedFile.setVisibility(View.GONE);
                mainAttachmentLayout.setVisibility(View.GONE);
                setupContactShareView(message, mainContactShareLayout);
                return customView;
            } else {
                mainContactShareLayout.setVisibility(View.GONE);

            }

            int startIndex = indexOfSearchQuery(message.getMessage());
            if (startIndex != -1) {
                final SpannableString highlightedName = new SpannableString(message.getMessage());

                // Sets the span to start at the starting point of the match and end at "length"
                // characters beyond the starting point
                highlightedName.setSpan(highlightTextSpan, startIndex,
                        startIndex + searchString.toString().length(), 0);

                messageTextView.setText(highlightedName);
            }
        }
        return customView;
    }

    private void setupContactShareView(final Message message, LinearLayout mainContactShareLayout) {
        mainContactShareLayout.setVisibility(View.VISIBLE);
        mainContactShareLayout.setLayoutParams(getImageLayoutParam(false));
        MobiComVCFParser parser = new MobiComVCFParser();
        try {

            VCFContactData data = parser.parseCVFContactData(message.getFilePaths().get(0));
            ImageView shareContactImage = (ImageView) mainContactShareLayout.findViewById(R.id.contact_share_image);
            TextView shareContactName = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_tv_name);
            TextView shareContactNo = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_tv_no);
            TextView shareEmailContact = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_emailId);

            Button addContactButton = (Button) mainContactShareLayout.findViewById(R.id.contact_share_add_btn);
            shareContactName.setText(data.getName());

            int resId = message.isTypeOutbox() ? Color.parseColor(alCustomizationSettings.getSentMessageTextColor()) : Color.parseColor(alCustomizationSettings.getReceivedMessageTextColor());
            shareContactName.setTextColor(resId);
            shareContactNo.setTextColor(resId);
            shareEmailContact.setTextColor(resId);
            addContactButton.setTextColor(resId);

            if (data.getProfilePic() != null) {
                if (imageCache.getBitmapFromMemCache(message.getKeyString()) == null) {
                    imageCache.addBitmapToCache(message.getKeyString(), data.getProfilePic());
                }

                shareContactImage.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
            }
            if (!TextUtils.isEmpty(data.getTelephoneNumber())) {
                shareContactNo.setText(data.getTelephoneNumber());
            } else {
                shareContactNo.setVisibility(View.GONE);
            }
            if (data.getEmail() != null) {
                shareEmailContact.setText(data.getEmail());
            } else {
                shareEmailContact.setVisibility(View.GONE);
            }


            addContactButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                    }
                }
            });

        } catch (Exception e) {
            Utils.printLog(context,"DetailedConvAdapter", "Exception in parsing");
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
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    Utils.printLog(context,TAG, "No application found to open this file");
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
                    Intent intent = new Intent(context, FullScreenImageActivity.class);
                    intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(smListItem, Message.class));
                    ((MobiComKitActivityInterface) context).startActivityForResult(intent, MobiComKitActivityInterface.REQUEST_CODE_FULL_SCREEN_ACTION);
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
                            context.startActivity(intentVideo);
                        } else {
                            Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Utils.printLog(context,TAG, "No application found to open this file");
        }

    }

    @Override
    public int getViewTypeCount() {
        return 6;
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


    private void populateVideoCall(View customView, Message msg) {


        Map<String, String> metaData = msg.getMetadata();

        TextView statusTextView = (TextView) customView.findViewById(R.id.applozic_call_status);
        TextView timeTextView = (TextView) customView.findViewById(R.id.applozic_call_timing);
        TextView durationTextView = (TextView) customView.findViewById(R.id.applozic_call_duration);

        ImageView imageView = (ImageView) customView.findViewById(R.id.applozic_call_image_type);

        timeTextView.setText(DateUtils.getFormattedDate(msg.getCreatedAtTime()));
        statusTextView.setText(VideoCallNotificationHelper.getStatus(metaData));

        if (VideoCallNotificationHelper.isMissedCall(msg)) {
            imageView.setImageResource(R.drawable.ic_communication_call_missed);
        }

        if (!VideoCallNotificationHelper.isAudioCall(msg)) {
            imageView.setImageResource(R.drawable.ic_videocam_white_24px);
        }

        if (metaData.get(VideoCallNotificationHelper.MSG_TYPE).equals(VideoCallNotificationHelper.CALL_END)) {

            String duration = metaData.get(VideoCallNotificationHelper.CALL_DURATION);

            if (!TextUtils.isEmpty(duration)) {

                durationTextView.setVisibility(View.VISIBLE);
                duration = Utils.getTimeDurationInFormat(Long.parseLong(duration));
                durationTextView.setText(duration);
            }
        } else {
            durationTextView.setVisibility(View.GONE);
        }

    }

    public void  refreshContactData(){
        if(contact != null){
            contact = contactService.getContactById(contact.getContactIds());
        }
    }

    private boolean isNormalAttachment(Message message) {

        if (message.getFileMetas() != null) {
            return !(message.getFileMetas().getContentType().contains("image") || message.getFileMetas().getContentType().contains("video")|| message.isContactMessage());
        }else if( message.getFilePaths() != null){
            String filePath = message.getFilePaths().get(0);
            final String mimeType = FileUtils.getMimeType(filePath);
            if(mimeType!=null) {
                return !(mimeType.contains("image") || mimeType.contains("video") || message.isContactMessage());
            }
        }
        return false;
    }
}
