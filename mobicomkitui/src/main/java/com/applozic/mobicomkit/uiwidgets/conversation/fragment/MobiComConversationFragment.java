package com.applozic.mobicomkit.uiwidgets.conversation.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import com.googlecode.tesseract.android.TessBaseAPI;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserBlockTask;
import com.applozic.mobicomkit.api.attachment.AttachmentView;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageBuilder;
import com.applozic.mobicomkit.api.conversation.MessageClientService;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.selfdestruct.DisappearingMessageTask;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.notification.MuteNotificationAsync;
import com.applozic.mobicomkit.api.notification.MuteNotificationRequest;
import com.applozic.mobicomkit.api.notification.NotificationService;
import com.applozic.mobicomkit.api.notification.MuteUserNotificationAsync;
import com.applozic.mobicomkit.api.people.UserIntentService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.MobiComVCFParser;
import com.applozic.mobicomkit.contact.VCFContactData;
import com.applozic.mobicomkit.exception.ApplozicException;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.feed.TopicDetail;
import com.applozic.mobicomkit.listners.AlContactListener;
import com.applozic.mobicomkit.listners.MediaUploadProgressHandler;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.async.AlMessageMetadataUpdateTask;
import com.applozic.mobicomkit.uiwidgets.attachmentview.AlBitmapUtils;
import com.applozic.mobicomkit.uiwidgets.attachmentview.ApplozicAudioManager;
import com.applozic.mobicomkit.uiwidgets.attachmentview.ApplozicAudioRecordManager;
import com.applozic.mobicomkit.uiwidgets.conversation.AlLinearLayoutManager;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.DeleteConversationAsyncTask;
import com.applozic.mobicomkit.uiwidgets.conversation.MessageCommunicator;
import com.applozic.mobicomkit.uiwidgets.conversation.MobicomMessageTemplate;
import com.applozic.mobicomkit.uiwidgets.conversation.UIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ChannelInfoActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.RecyclerViewPositionHelper;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.ApplozicContextSpinnerAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.DetailedConversationAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.MobicomMessageTemplateAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.ALBookingDetailsModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.ALGuestCountModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.ALRichMessageListener;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.ALRichMessageModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.AlHotelBookingModel;
import com.applozic.mobicomkit.uiwidgets.conversation.richmessaging.payment.PaymentActivity;
import com.applozic.mobicomkit.uiwidgets.people.fragment.UserProfileFragment;
import com.applozic.mobicomkit.uiwidgets.schedule.ConversationScheduler;
import com.applozic.mobicomkit.uiwidgets.schedule.ScheduledTimeHolder;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALProfileClickListener;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALStoragePermission;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALStoragePermissionListener;
import com.applozic.mobicomkit.uiwidgets.uilistener.ContextMenuClickListener;
import com.applozic.mobicomkit.uiwidgets.uilistener.CustomToolbarListener;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.LocationUtils;
import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageCache;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.emoticon.EmojiconHandler;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUserMapper;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;

import static android.view.View.VISIBLE;
import static java.util.Collections.disjoint;

/**
 * reg
 * Created by devashish on 10/2/15.
 */
abstract public class MobiComConversationFragment extends Fragment implements View.OnClickListener, GestureDetector.OnGestureListener, ContextMenuClickListener, ALRichMessageListener {

    //Todo: Increase the file size limit
    private String path = Environment.getExternalStorageDirectory().toString() + "/Applozic/image";
    public String filename;
    public String fileURL;
    public static final int MAX_ALLOWED_FILE_SIZE = 10 * 1024 * 1024;
    private static final String TAG = "MobiComConversation";
    private static int count;
    public FrameLayout emoticonsFrameLayout, contextFrameLayout;
    public GridView multimediaPopupGrid;
    protected List<Conversation> conversations;
    protected String title = "Conversations";
    protected DownloadConversation downloadConversation;
    protected MobiComConversationService conversationService;
    protected TextView infoBroadcast;
    protected Class messageIntentClass;
    protected TextView emptyTextView;
    protected boolean loadMore = true;
    protected Contact contact;
    protected Channel channel;
    protected Integer currentConversationId;
    protected EditText messageEditText;
    protected ImageButton sendButton, recordButton;
    protected ImageButton attachButton;
    protected Spinner sendType;
    protected LinearLayout individualMessageSendLayout, mainEditTextLinearLayout;
    protected LinearLayout extendedSendingOptionLayout;
    protected RelativeLayout attachmentLayout;
    protected ProgressBar mediaUploadProgressBar;
    protected View spinnerLayout;
    protected SwipeRefreshLayout swipeLayout;
    protected Button scheduleOption;
    protected ScheduledTimeHolder scheduledTimeHolder = new ScheduledTimeHolder();
    protected Spinner selfDestructMessageSpinner;
    protected ImageView mediaContainer;
    protected TextView attachedFile, userNotAbleToChatTextView;
    protected String filePath;
    protected boolean firstTimeMTexterFriend;
    protected MessageCommunicator messageCommunicator;
    //protected ConversationListView listView = null;
    protected List<Message> messageList = new ArrayList<Message>();
    protected Drawable sentIcon;
    protected Drawable deliveredIcon;
    protected ImageButton emoticonsBtn;
    protected Support support;
    protected MultimediaOptionFragment multimediaOptionFragment = new MultimediaOptionFragment();
    protected boolean hideExtendedSendingOptionLayout;
    protected SyncCallService syncCallService;
    protected ApplozicContextSpinnerAdapter applozicContextSpinnerAdapter;
    private List<Conversation> conversationList;
    protected Message messageToForward;
    protected String searchString;
    protected AlCustomizationSettings alCustomizationSettings;
    String audio_duration;
    LinearLayout userNotAbleToChatLayout;
    int resourceId;
    List<ChannelUserMapper> channelUserMapperList;
    AdapterView.OnItemSelectedListener adapterView;
    MessageDatabaseService messageDatabaseService;
    AppContactService appContactService;
    ConversationUIService conversationUIService;
    long millisecond;
    MuteNotificationRequest muteNotificationRequest;
    List<String> restrictedWords;
    RelativeLayout replayRelativeLayout;
    ImageButton attachReplyCancelLayout;
    TextView nameTextView, messageTextView;
    ImageView galleryImageView;
    FileClientService fileClientService;
    ImageLoader imageThumbnailLoader, messageImageLoader;
    ImageView imageViewForAttachmentType;
    RelativeLayout imageViewRLayout;
    Map<String, String> messageMetaData = new HashMap<>();
    LinearLayout slideTextLinearlayout;
    TextView recordTimeTextView;
    FrameLayout audioRecordFrameLayout;
    ApplozicAudioRecordManager applozicAudioRecordManager;
    String timeStamp, audioFileName;
    String outputFile;
    CountDownTimer t;
    GestureDetectorCompat mDetector;
    boolean longPress;
    boolean isToastVisible = false;
    int seconds = 0, minutes = 0;
    ImageView slideImageView;
    private EmojiconHandler emojiIconHandler;
    private Bitmap previewThumbnail;
    protected TextView isTyping, bottomlayoutTextView;
    private String defaultText;
    private boolean typingStarted;
    private Integer channelKey;
    private Toolbar toolbar;
    private Menu menu;
    private Spinner contextSpinner;
    private boolean onSelected;
    private ImageCache imageCache;
    private float startedDraggingX = -1;
    private float distCanMove = dp(80);
    private EditText errorEditTextView;
    private RecyclerView messageTemplateView;
    private ImageView audioRecordIconImageView;
    WeakReference<ImageButton> recordButtonWeakReference;
    RecyclerView recyclerView;
    RecyclerViewPositionHelper recyclerViewPositionHelper;
    protected LinearLayoutManager linearLayoutManager;
    int positionInSmsList;
    DetailedConversationAdapter recyclerDetailConversationAdapter;
    MobicomMessageTemplate messageTemplate;
    MobicomMessageTemplateAdapter templateAdapter;
    boolean isAlreadyLoading;
    FloatingActionButton messageDropDownActionButton;
    TextView messageUnreadCountTextView;
    int messageUnreadCount = 0;
    TextView applozicLabel;
    private String geoApiKey;
    private String loggedInUserId;

    public static int dp(float value) {
        return (int) Math.ceil(1 * value);
    }

    public void setEmojiIconHandler(EmojiconHandler emojiIconHandler) {
        this.emojiIconHandler = emojiIconHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoApiKey = Utils.getMetaDataValue(ApplozicService.getContext(getContext()), ConversationActivity.GOOGLE_API_KEY_META_DATA);
        String jsonString = FileUtils.loadSettingsJsonFile(ApplozicService.getContext(getContext()));
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        restrictedWords = FileUtils.loadRestrictedWordsFile(getContext());
        conversationUIService = new ConversationUIService(getActivity());
        syncCallService = SyncCallService.getInstance(getActivity());
        appContactService = new AppContactService(getActivity());
        messageDatabaseService = new MessageDatabaseService(getActivity());
        fileClientService = new FileClientService(getActivity());
        setHasOptionsMenu(true);
        imageThumbnailLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadThumbnailImage(getContext(), (Message) data, getImageLayoutParam(false).width, getImageLayoutParam(false).height);
            }
        };

        imageCache = ImageCache.getInstance((getActivity()).getSupportFragmentManager(), 0.1f);
        imageThumbnailLoader.setImageFadeIn(false);
        imageThumbnailLoader.addImageCache((getActivity()).getSupportFragmentManager(), 0.1f);
        messageImageLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadMessageImage(getContext(), (String) data);
            }
        };
        messageImageLoader.setImageFadeIn(false);
        messageImageLoader.addImageCache((getActivity()).getSupportFragmentManager(), 0.1f);
        applozicAudioRecordManager = new ApplozicAudioRecordManager(getActivity());
        mDetector = new GestureDetectorCompat(getContext(), this);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View list = inflater.inflate(R.layout.mobicom_message_list, container, false);
        recyclerView = (RecyclerView) list.findViewById(R.id.messageList);
        linearLayoutManager = new AlLinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerViewPositionHelper = new RecyclerViewPositionHelper(recyclerView, linearLayoutManager);
        ((ConversationActivity) getActivity()).setChildFragmentLayoutBGToTransparent();
        messageList = new ArrayList<Message>();
        multimediaPopupGrid = (GridView) list.findViewById(R.id.mobicom_multimedia_options1);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        loggedInUserId = MobiComUserPreference.getInstance(getContext()).getUserId();

        toolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        toolbar.setClickable(true);
        mainEditTextLinearLayout = (LinearLayout) list.findViewById(R.id.main_edit_text_linear_layout);
        individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        slideImageView = (ImageView) list.findViewById(R.id.slide_image_view);
        sendButton = (ImageButton) individualMessageSendLayout.findViewById(R.id.conversation_send);
        recordButton = (ImageButton) individualMessageSendLayout.findViewById(R.id.record_button);
        mainEditTextLinearLayout = (LinearLayout) list.findViewById(R.id.main_edit_text_linear_layout);
        audioRecordFrameLayout = (FrameLayout) list.findViewById(R.id.audio_record_frame_layout);
        messageTemplateView = (RecyclerView) list.findViewById(R.id.mobicomMessageTemplateView);
        applozicLabel = list.findViewById(R.id.applozicLabel);
        Configuration config = getResources().getConfiguration();
        recordButtonWeakReference = new WeakReference<ImageButton>(recordButton);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                sendButton.setScaleX(-1);
                mainEditTextLinearLayout.setBackgroundResource(R.drawable.applozic_chat_left_icon);
                audioRecordFrameLayout.setBackgroundResource(R.drawable.applozic_chat_left_icon);
                slideImageView.setImageResource(R.drawable.slide_arrow_right);

            }
        }

        if (MobiComUserPreference.getInstance(getContext()).getPricingPackage() == 1) {
            applozicLabel.setVisibility(VISIBLE);
        }

        if (alCustomizationSettings.isPoweredByApplozic()) {
            list.findViewById(R.id.txtPoweredByApplozic).setVisibility(VISIBLE);
        }

        extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);

        attachmentLayout = (RelativeLayout) list.findViewById(R.id.attachment_layout);
        isTyping = (TextView) list.findViewById(R.id.isTyping);

        contextFrameLayout = (FrameLayout) list.findViewById(R.id.contextFrameLayout);

        contextSpinner = (Spinner) list.findViewById(R.id.spinner_show);
        slideTextLinearlayout = (LinearLayout) list.findViewById(R.id.slide_LinearLayout);
        errorEditTextView = (EditText) list.findViewById(R.id.error_edit_text_view);
        audioRecordIconImageView = (ImageView) list.findViewById(R.id.audio_record_icon_image_view);
        recordTimeTextView = (TextView) list.findViewById(R.id.recording_time_text_view);
        mDetector = new GestureDetectorCompat(getContext(), this);
        adapterView = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                if (conversations != null && conversations.size() > 0) {
                    Conversation conversation = conversations.get(pos);
                    BroadcastService.currentConversationId = conversation.getId();
                    if (onSelected) {
                        currentConversationId = conversation.getId();
                        if (messageList != null) {
                            messageList.clear();
                        }
                        downloadConversation = new DownloadConversation(recyclerView, true, 1, 0, 0, contact, channel, conversation.getId());
                        downloadConversation.execute();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };

        mediaUploadProgressBar = (ProgressBar) attachmentLayout.findViewById(R.id.media_upload_progress_bar);
        emoticonsFrameLayout = (FrameLayout) list.findViewById(R.id.emojicons_frame_layout);
        emoticonsBtn = (ImageButton) list.findViewById(R.id.emoticons_btn);
        if (emojiIconHandler == null && emoticonsBtn != null) {
            emoticonsBtn.setVisibility(View.GONE);
        }
        replayRelativeLayout = (RelativeLayout) list.findViewById(R.id.reply_message_layout);
        messageTextView = (TextView) list.findViewById(R.id.messageTextView);
        galleryImageView = (ImageView) list.findViewById(R.id.imageViewForPhoto);
        nameTextView = (TextView) list.findViewById(R.id.replyNameTextView);
        attachReplyCancelLayout = (ImageButton) list.findViewById(R.id.imageCancel);
        messageDropDownActionButton = (FloatingActionButton) list.findViewById(R.id.message_drop_down);
        messageUnreadCountTextView = (TextView) list.findViewById(R.id.message_unread_count_textView);
        imageViewRLayout = (RelativeLayout) list.findViewById(R.id.imageViewRLayout);
        imageViewForAttachmentType = (ImageView) list.findViewById(R.id.imageViewForAttachmentType);
        spinnerLayout = inflater.inflate(R.layout.mobicom_message_list_header_footer, null);
        infoBroadcast = (TextView) spinnerLayout.findViewById(R.id.info_broadcast);
        spinnerLayout.setVisibility(View.GONE);
        emptyTextView = (TextView) list.findViewById(R.id.noConversations);
        emptyTextView.setTextColor(Color.parseColor(alCustomizationSettings.getNoConversationLabelTextColor().trim()));
        emoticonsBtn.setOnClickListener(this);
        //listView.addHeaderView(spinnerLayout);
        sentIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_sent);
        deliveredIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered);

        //listView.setLongClickable(true);

        recordButton.setVisibility(alCustomizationSettings.isRecordButton() && (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType())) ? View.VISIBLE : View.GONE);
        sendButton.setVisibility(alCustomizationSettings.isRecordButton() && (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType())) ? View.GONE : View.VISIBLE);

        GradientDrawable bgShape = (GradientDrawable) sendButton.getBackground();
        bgShape.setColor(Color.parseColor(alCustomizationSettings.getSendButtonBackgroundColor().trim()));

        GradientDrawable bgShapeRecordButton = (GradientDrawable) recordButton.getBackground();
        bgShapeRecordButton.setColor(Color.parseColor(alCustomizationSettings.getSendButtonBackgroundColor().trim()));

        attachButton = (ImageButton) individualMessageSendLayout.findViewById(R.id.attach_button);

        sendType = (Spinner) extendedSendingOptionLayout.findViewById(R.id.sendTypeSpinner);
        messageEditText = (EditText) individualMessageSendLayout.findViewById(R.id.conversation_message);

        messageEditText.setTextColor(Color.parseColor(alCustomizationSettings.getMessageEditTextTextColor()));

        messageEditText.setHintTextColor(Color.parseColor(alCustomizationSettings.getMessageEditTextHintTextColor()));

        userNotAbleToChatLayout = (LinearLayout) list.findViewById(R.id.user_not_able_to_chat_layout);
        userNotAbleToChatTextView = (TextView) userNotAbleToChatLayout.findViewById(R.id.user_not_able_to_chat_textView);
        userNotAbleToChatTextView.setTextColor(Color.parseColor(alCustomizationSettings.getUserNotAbleToChatTextColor()));

        if (channel != null && channel.isDeleted()) {
            userNotAbleToChatTextView.setText(R.string.group_has_been_deleted_text);
        }

        bottomlayoutTextView = (TextView) list.findViewById(R.id.user_not_able_to_chat_textView);
        if (!TextUtils.isEmpty(defaultText)) {
            messageEditText.setText(defaultText);
            defaultText = "";
        }
        scheduleOption = (Button) extendedSendingOptionLayout.findViewById(R.id.scheduleOption);
        mediaContainer = (ImageView) attachmentLayout.findViewById(R.id.media_container);
        attachedFile = (TextView) attachmentLayout.findViewById(R.id.attached_file);
        ImageView closeAttachmentLayout = (ImageView) attachmentLayout.findViewById(R.id.close_attachment_layout);

        swipeLayout = (SwipeRefreshLayout) list.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        //listView.setMessageEditText(messageEditText);

        ArrayAdapter<CharSequence> sendTypeAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.send_type_options, R.layout.mobiframework_custom_spinner);

        sendTypeAdapter.setDropDownViewResource(R.layout.mobiframework_custom_spinner);
        sendType.setAdapter(sendTypeAdapter);

        t = new CountDownTimer(Long.MAX_VALUE, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                count++;
                seconds = count;
                if (seconds == 60) {
                    minutes++;
                    count = 0;
                    seconds = 0;
                }
                if (minutes == 60) {
                    minutes = 0;
                    count = 0;
                }
                if (count % 2 == 0) {
                    audioRecordIconImageView.setVisibility(VISIBLE);
                    audioRecordIconImageView.setImageResource(R.drawable.applozic_audio_record);
                } else {
                    audioRecordIconImageView.setVisibility(View.INVISIBLE);
                }

                recordTimeTextView.setText(String.format("%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                count = 0;
            }
        };

        recordButton.setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mDetector.onTouchEvent(motionEvent);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && longPress) {
                    isToastVisible = true;
                    errorEditTextView.setVisibility(View.GONE);
                    errorEditTextView.requestFocus();
                    errorEditTextView.setError(null);
                    startedDraggingX = -1;
                    audioRecordFrameLayout.setVisibility(View.GONE);
                    mainEditTextLinearLayout.setVisibility(View.VISIBLE);
                    applozicAudioRecordManager.sendAudio();
                    t.cancel();
                    longPress = false;
                    messageEditText.requestFocus();
                    seconds = 0;
                    minutes = 0;
                    count = 0;
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    float x = motionEvent.getX();
                    if (x < -distCanMove) {
                        count = 0;
                        t.cancel();
                        audioRecordIconImageView.setImageResource(R.drawable.applozic_audio_delete);
                        recordTimeTextView.setVisibility(View.GONE);
                        applozicAudioRecordManager.cancelAudio();
                        messageEditText.requestFocus();
                    }
                    x = x + ApplozicAudioRecordAnimation.getX(recordButton);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideTextLinearlayout
                            .getLayoutParams();
                    if (startedDraggingX != -1) {
                        float dist = (x - startedDraggingX);
                        params.leftMargin = dp(30) + (int) dist;
                        slideTextLinearlayout.setLayoutParams(params);
                        float alpha = 1.0f + dist / distCanMove;
                        if (alpha > 1) {
                            alpha = 1;
                        } else if (alpha < 0) {
                            alpha = 0;
                        }
                        ApplozicAudioRecordAnimation.setAlpha(slideTextLinearlayout, alpha);
                    }
                    if (x <= ApplozicAudioRecordAnimation.getX(slideTextLinearlayout) + slideTextLinearlayout.getWidth()
                            + dp(30)) {
                        if (startedDraggingX == -1) {
                            startedDraggingX = x;
                            distCanMove = (audioRecordFrameLayout.getMeasuredWidth()
                                    - slideTextLinearlayout.getMeasuredWidth() - dp(48)) / 2.0f;
                            if (distCanMove <= 0) {
                                distCanMove = dp(80);
                            } else if (distCanMove > dp(80)) {
                                distCanMove = dp(80);
                            }
                        }
                    }
                    if (params.leftMargin > dp(30)) {
                        params.leftMargin = dp(30);
                        slideTextLinearlayout.setLayoutParams(params);
                        ApplozicAudioRecordAnimation.setAlpha(slideTextLinearlayout, 1);
                        startedDraggingX = -1;
                    }
                }
                view.onTouchEvent(motionEvent);
                return true;
            }
        });

        scheduleOption.setOnClickListener(new View.OnClickListener() {

                                              @Override
                                              public void onClick(View v) {
                                                  ConversationScheduler conversationScheduler = new ConversationScheduler();
                                                  conversationScheduler.setScheduleOption(scheduleOption);
                                                  conversationScheduler.setScheduledTimeHolder(scheduledTimeHolder);
                                                  conversationScheduler.setCancelable(false);
                                                  conversationScheduler.show(getActivity().getSupportFragmentManager(), "conversationScheduler");
                                              }
                                          }
        );

        messageEditText.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // EmojiconHandler.addEmojis(getActivity(), messageEditText.getText(), Utils.dpToPx(30));
                //TODO: write code to emoticons .....

            }

            public void afterTextChanged(Editable s) {
                try {
                    if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() > 0 && !typingStarted) {
                        typingStarted = true;
                        handleSendAndRecordButtonView(true);
                        if (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()) || contact != null) {
                            Applozic.publishTypingStatus(getContext(), channel, contact, typingStarted);
                        }
                    } else if (s.toString().trim().length() == 0 && typingStarted) {
                        typingStarted = false;
                        handleSendAndRecordButtonView(false);
                        if (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()) || contact != null) {
                            Applozic.publishTypingStatus(getContext(), channel, contact, typingStarted);
                        }
                    }

                } catch (Exception e) {

                }
                //sendButton.setVisibility((s == null || s.toString().trim().length() == 0) && TextUtils.isEmpty(filePath) ? View.GONE : View.VISIBLE);
                //attachButton.setVisibility(s == null || s.toString().trim().length() == 0 ? View.VISIBLE : View.GONE);
            }
        });

        messageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alCustomizationSettings.isMessageFastScrollEnabled()) {
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linearLayoutManager.setStackFromEnd(true);
                            linearLayoutManager.setReverseLayout(true);
                        }
                    });
                }
                emoticonsFrameLayout.setVisibility(View.GONE);
            }
        });

        if (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
            attachButton.setVisibility(View.GONE);
            messageEditText.setPadding(20, 0, 0, 0);
        }

        attachReplyCancelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageMetaData = null;
                replayRelativeLayout.setVisibility(View.GONE);
            }
        });

        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (typingStarted) {
                        if (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()) || contact != null) {
                            Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
                            intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
                            intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
                            intent.putExtra(ApplozicMqttIntentService.TYPING, typingStarted);
                            ApplozicMqttIntentService.enqueueWork(getActivity(), intent);
                        }
                    }
                    emoticonsFrameLayout.setVisibility(View.GONE);

                    multimediaPopupGrid.setVisibility(View.GONE);
                }
            }

        });

        recordButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                if (!isToastVisible && !typingStarted) {
                                                    vibrate();
                                                    errorEditTextView.requestFocus();
                                                    errorEditTextView.setError(ApplozicService.getContext(getContext()).getString(R.string.hold_to_record_release_to_send));
                                                    isToastVisible = true;
                                                    new CountDownTimer(3000, 1000) {

                                                        @Override
                                                        public void onTick(long millisUntilFinished) {
                                                        }

                                                        @Override
                                                        public void onFinish() {
                                                            errorEditTextView.setError(null);
                                                            messageEditText.requestFocus();
                                                            isToastVisible = false;

                                                        }
                                                    }.start();
                                                } else {
                                                    errorEditTextView.setError(null);
                                                    isToastVisible = false;
                                                }
                                                emoticonsFrameLayout.setVisibility(View.GONE);
                                                sendMessage();
                                                handleSendAndRecordButtonView(false);
                                                errorEditTextView.setVisibility(View.VISIBLE);
                                            }
                                        }
        );

        sendButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              if (alCustomizationSettings.isMessageFastScrollEnabled()) {
                                                  if (getActivity() == null) {
                                                      return;
                                                  }
                                                  getActivity().runOnUiThread(new Runnable() {
                                                      @Override
                                                      public void run() {
                                                          recyclerView.smoothScrollToPosition(messageList.size());
                                                          recyclerView.getLayoutManager().scrollToPosition(messageList.size());
                                                      }
                                                  });
                                              }
                                              emoticonsFrameLayout.setVisibility(View.GONE);
                                              sendMessage();
                                              if (contact != null && !contact.isBlocked() || channel != null) {
                                                  handleSendAndRecordButtonView(false);
                                              }
                                          }
                                      }
        );


        closeAttachmentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filePath = null;
                if (previewThumbnail != null) {
                    previewThumbnail.recycle();
                }
                attachmentLayout.setVisibility(View.GONE);

                if (messageEditText != null && TextUtils.isEmpty(messageEditText.getText().toString().trim()) && recordButton != null && sendButton != null) {
                    handleSendAndRecordButtonView(false);
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (recyclerDetailConversationAdapter != null) {
                    recyclerDetailConversationAdapter.contactImageLoader.setPauseWork(newState == RecyclerView.SCROLL_STATE_DRAGGING);
                }
            }

            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                //super.onScrolled(recyclerView, dx, dy);

                if (alCustomizationSettings.isMessageFastScrollEnabled()) {
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int lastVisible = linearLayoutManager.findLastVisibleItemPosition();

                    if (totalItemCount - lastVisible != 1) {
                        messageDropDownActionButton.setVisibility(VISIBLE);
                    } else {
                        messageUnreadCountTextView.setVisibility(View.INVISIBLE);
                        messageDropDownActionButton.setVisibility(View.INVISIBLE);
                        messageUnreadCount = 0;
                    }
                }

                if (loadMore) {
                    int topRowVerticalPosition =
                            (recyclerView == null || recyclerView.getChildCount() == 0) ?
                                    0 : recyclerView.getChildAt(0).getTop();
                    swipeLayout.setEnabled(topRowVerticalPosition >= 0);
                }

            }
        });

        messageDropDownActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.smoothScrollToPosition(messageList.size());
                        recyclerView.getLayoutManager().scrollToPosition(messageList.size());
                    }
                });
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getContext() != null && getContext().getApplicationContext() instanceof ALProfileClickListener) {
                    ((ALProfileClickListener) getContext().getApplicationContext()).onClick(getActivity(), (contact != null ? contact.getUserId() : null), channel, true);
                }

                if (channel != null) {

                    if (Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())
                            && User.RoleType.USER_ROLE.getValue().equals(MobiComUserPreference.getInstance(getContext()).getUserRoleType())) {
                        return;
                    }
                    if (channel.isDeleted()) {
                        return;
                    }
                    if (alCustomizationSettings.isGroupInfoScreenVisible() && !Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) && !Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                        Intent channelInfo = new Intent(getActivity(), ChannelInfoActivity.class);
                        channelInfo.putExtra(ChannelInfoActivity.CHANNEL_KEY, channel.getKey());
                        startActivity(channelInfo);
                    } else if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) && alCustomizationSettings.isUserProfileFragment()) {
                        UserProfileFragment userProfileFragment = (UserProfileFragment) UIService.getFragmentByTag(getActivity(), ConversationUIService.USER_PROFILE_FRAMENT);
                        if (userProfileFragment == null) {
                            String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                            if (!TextUtils.isEmpty(userId)) {
                                Contact newcContact = appContactService.getContactById(userId);
                                userProfileFragment = new UserProfileFragment();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(ConversationUIService.CONTACT, newcContact);
                                userProfileFragment.setArguments(bundle);
                                ConversationActivity.addFragment(getActivity(), userProfileFragment, ConversationUIService.USER_PROFILE_FRAMENT);
                            }
                        }
                    }
                } else {
                    if (alCustomizationSettings.isUserProfileFragment()) {
                        UserProfileFragment userProfileFragment = (UserProfileFragment) UIService.getFragmentByTag(getActivity(), ConversationUIService.USER_PROFILE_FRAMENT);
                        if (userProfileFragment == null) {
                            userProfileFragment = new UserProfileFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(ConversationUIService.CONTACT, contact);
                            userProfileFragment.setArguments(bundle);
                            ConversationActivity.addFragment(getActivity(), userProfileFragment, ConversationUIService.USER_PROFILE_FRAMENT);
                        }
                    }

                }
            }
        });
        recyclerView.setLongClickable(true);
        //Adding fragment for emoticons...
//        //Fragment emojiFragment = new EmojiconsFragment(this, this);
//        Fragment emojiFragment = new EmojiconsFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.add(R.id.emojicons_frame_layout, emojiFragment).commit();
        messageTemplate = alCustomizationSettings.getMessageTemplate();

        if (messageTemplate != null && messageTemplate.isEnabled()) {
            templateAdapter = new MobicomMessageTemplateAdapter(getContext(), messageTemplate);
            MobicomMessageTemplateAdapter.MessageTemplateDataListener listener = new MobicomMessageTemplateAdapter.MessageTemplateDataListener() {
                @Override
                public void onItemSelected(String message) {

                    final Message lastMessage = !messageList.isEmpty() ? messageList.get(messageList.size() - 1) : null;

                    if ((messageTemplate.getTextMessageList() != null && !messageTemplate.getTextMessageList().getMessageList().isEmpty() && messageTemplate.getTextMessageList().isSendMessageOnClick() && "text".equals(getMessageType(lastMessage)))
                            || (messageTemplate.getImageMessageList() != null && !messageTemplate.getImageMessageList().getMessageList().isEmpty() && messageTemplate.getImageMessageList().isSendMessageOnClick() && "image".equals(getMessageType(lastMessage)))
                            || (messageTemplate.getVideoMessageList() != null && !messageTemplate.getVideoMessageList().getMessageList().isEmpty() && messageTemplate.getVideoMessageList().isSendMessageOnClick() && "video".equals(getMessageType(lastMessage)))
                            || (messageTemplate.getLocationMessageList() != null && !messageTemplate.getLocationMessageList().getMessageList().isEmpty() && messageTemplate.getLocationMessageList().isSendMessageOnClick() && "location".equals(getMessageType(lastMessage)))
                            || (messageTemplate.getContactMessageList() != null && !messageTemplate.getContactMessageList().getMessageList().isEmpty() && messageTemplate.getContactMessageList().isSendMessageOnClick() && "contact".equals(getMessageType(lastMessage)))
                            || (messageTemplate.getAudioMessageList() != null && !messageTemplate.getAudioMessageList().getMessageList().isEmpty() && messageTemplate.getAudioMessageList().isSendMessageOnClick() && "audio".equals(getMessageType(lastMessage)))
                            || messageTemplate.getSendMessageOnClick()) {
                        sendMessage(message);
                    }

                    if (messageTemplate.getHideOnSend()) {
                        AlMessageMetadataUpdateTask.MessageMetadataListener listener1 = new AlMessageMetadataUpdateTask.MessageMetadataListener() {
                            @Override
                            public void onSuccess(Context context, String message) {
                                templateAdapter.removeTemplates();
                            }

                            @Override
                            public void onFailure(Context context, String error) {
                            }
                        };

                        if (lastMessage != null) {
                            Map<String, String> metadata = lastMessage.getMetadata();
                            metadata.put("isDoneWithClicking", "true");
                            lastMessage.setMetadata(metadata);
                            new AlMessageMetadataUpdateTask(getContext(), lastMessage.getKeyString(), lastMessage.getMetadata(), listener1).execute();
                        }
                    }

                    final Intent intent = new Intent();
                    intent.setAction("com.applozic.mobicomkit.TemplateMessage");
                    intent.putExtra("templateMessage", message);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    getActivity().sendBroadcast(intent);
                }
            };

            templateAdapter.setOnItemSelected(listener);
            LinearLayoutManager horizontalLayoutManagaer
                    = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            messageTemplateView.setLayoutManager(horizontalLayoutManagaer);
            messageTemplateView.setAdapter(templateAdapter);
        }

        createTemplateMessages();

        return list;
    }

    private void setToolbarTitle(String title) {
        if (getActivity() == null) {
            return;
        }
        ((CustomToolbarListener) getActivity()).setToolbarTitle(title);
    }

    private void setToolbarSubtitle(String subtitle) {
        if (getActivity() == null) {
            return;
        }
        if ((alCustomizationSettings.isGroupSubtitleHidden() || ApplozicSetting.getInstance(getContext()).isGroupSubtitleHidden()) && channel != null && !subtitle.contains(ApplozicService.getContext(getContext()).getString(R.string.is_typing))) {
            ((CustomToolbarListener) getActivity()).setToolbarSubtitle("");
            return;
        }
        ((CustomToolbarListener) getActivity()).setToolbarSubtitle(subtitle);
    }

    private void setToolbarImage(Contact contact, Channel channel) {
        if (getActivity() == null) {
            return;
        }
        ((CustomToolbarListener) getActivity()).setToolbarImage(contact, channel);
    }

    public void handleSendAndRecordButtonView(boolean isSendButtonVisible) {
        sendButton.setVisibility(alCustomizationSettings.isRecordButton() && (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType())) ? isSendButtonVisible ? View.VISIBLE : View.GONE : View.VISIBLE);
        recordButton.setVisibility(alCustomizationSettings.isRecordButton() && (contact != null || channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType())) ? isSendButtonVisible ? View.GONE : View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return true;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }


    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        if (contact != null && contact.isBlocked()) {
            userBlockDialog(false, contact, false);
            return;
        }
        if (getActivity() instanceof ALStoragePermissionListener) {
            if (((ALStoragePermissionListener) getActivity()).isPermissionGranted()) {
                startRecording();
            } else {
                ((ALStoragePermissionListener) getActivity()).checkPermission(new ALStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                    }
                });
            }
        }
    }

    private void startRecording() {
        isToastVisible = true;
        errorEditTextView.requestFocus();
        errorEditTextView.setError(null);
        recordTimeTextView.setVisibility(View.VISIBLE);
        audioRecordIconImageView.setImageResource(R.drawable.applozic_audio_record);

        ApplozicAudioManager.getInstance(getContext()).audiostop();


        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) slideTextLinearlayout
                .getLayoutParams();
        params.leftMargin = dp(30);
        slideTextLinearlayout.setLayoutParams(params);
        ApplozicAudioRecordAnimation.setAlpha(slideTextLinearlayout, 1);
        startedDraggingX = -1;
        ViewConfiguration.getLongPressTimeout();
        timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        audioFileName = "AUD_" + timeStamp + "_" + ".m4a";
        outputFile = FileClientService.getFilePath(audioFileName, ApplozicService.getContext(getContext()), "audio/m4a").getAbsolutePath();
        applozicAudioRecordManager.setTimeStamp(timeStamp);
        applozicAudioRecordManager.setAudioFileName(audioFileName);
        applozicAudioRecordManager.setOutputFile(outputFile);
        vibrate();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO},
                    10);
        } else {
            applozicAudioRecordManager.recordAudio();
            t.cancel();
            t.start();
            count = 0;
        }
        recordButton.getParent()
                .requestDisallowInterceptTouchEvent(true);
        audioRecordFrameLayout.setVisibility(View.VISIBLE);
        mainEditTextLinearLayout.setVisibility(View.GONE);
        longPress = true;
    }

    private void vibrate() {
        try {
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(200);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sendMessage() {
        if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    if (withUserContact.isBlocked()) {
                        userBlockDialog(false, withUserContact, true);
                    } else {
                        processSendMessage();
                    }
                }
            } else if (Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                if (Utils.isInternetAvailable(getActivity())) {
                    processSendMessage();
                } else {
                    Toast.makeText(ApplozicService.getContext(getContext()), ApplozicService.getContext(getContext()).getString(R.string.internet_connection_not_available), Toast.LENGTH_SHORT).show();
                }
            } else {
                processSendMessage();
            }
        } else if (contact != null) {
            if (contact.isBlocked()) {
                userBlockDialog(false, contact, false);
            } else {
                processSendMessage();
            }
        }
    }

    public void sendOpenGroupMessage(String messageText) {

        attachReplyCancelLayout.setVisibility(View.GONE);
        replayRelativeLayout.setVisibility(View.GONE);

        Map<String, String> messageMetaData = new HashMap<>();
        if (this.messageMetaData != null && !this.messageMetaData.isEmpty()) {
            messageMetaData.putAll(this.messageMetaData);
        }


        new MessageBuilder(getActivity()).setMessage(messageText).setMetadata(messageMetaData).setGroupId(channel.getKey()).send(new MediaUploadProgressHandler() {

            @Override
            public void onUploadStarted(ApplozicException e, String oldMessageKey) {

            }

            @Override
            public void onProgressUpdate(int percentage, ApplozicException e, String oldMessageKey) {

            }

            @Override
            public void onCancelled(ApplozicException e, String oldMessageKey) {

            }

            @Override
            public void onCompleted(ApplozicException e, String oldMessageKey) {

            }

            @Override
            public void onSent(Message message, String oldMessageKey) {
                Message messageToBeReplied = new Message();
                messageToBeReplied.setKeyString(oldMessageKey);
                int indexOfObject = messageList.indexOf(messageToBeReplied);
                if (indexOfObject != -1) {
                    messageList.set(indexOfObject, message);
                    recyclerDetailConversationAdapter.notifyDataSetChanged();
                }
            }
        });

        this.messageMetaData = null;
    }


    protected void processSendMessage() {
        if (!TextUtils.isEmpty(messageEditText.getText().toString().trim()) || !TextUtils.isEmpty(filePath)) {
            String inputMessage = messageEditText.getText().toString();
            String[] inputMsg = inputMessage.toLowerCase().split(" ");
            List<String> userInputList = Arrays.asList(inputMsg);

            boolean disjointResult = (restrictedWords == null) ? true : disjoint(restrictedWords, userInputList);

            if (disjointResult) {

                if (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                    sendOpenGroupMessage(messageEditText.getText().toString().trim());
                } else {
                    sendMessage(messageEditText.getText().toString().trim());
                }
                messageEditText.setText("");
                scheduleOption.setText(R.string.ScheduleText);
                if (scheduledTimeHolder.getTimestamp() != null) {
                    showScheduleMessageToast();
                }
                scheduledTimeHolder.resetScheduledTimeHolder();

            } else {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                        setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                alertDialog.setTitle(alCustomizationSettings.getRestrictedWordMessage());
                alertDialog.setCancelable(true);
                alertDialog.create().show();

            }
        }

    }

    public void showScheduleMessageToast() {
        if (this.getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), R.string.info_message_scheduled, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteMessageFromDeviceList(String messageKeyString) {
        try {
            int position;
            boolean updateQuickConversation = false;
            int index;
            for (Message message : messageList) {
                boolean value = message.getKeyString() != null ? message.getKeyString().equals(messageKeyString) : false;
                if (value) {
                    index = messageList.indexOf(message);
                    if (index != -1) {
                        int aboveIndex = index - 1;
                        int belowIndex = index + 1;
                        Message aboveMessage = messageList.get(aboveIndex);
                        if (belowIndex != messageList.size()) {
                            Message belowMessage = messageList.get(belowIndex);
                            if (aboveMessage.isTempDateType() && belowMessage.isTempDateType()) {
                                messageList.remove(aboveMessage);
                            }
                        } else if (belowIndex == messageList.size() && aboveMessage.isTempDateType()) {
                            messageList.remove(aboveMessage);
                        }
                    }
                }
                if (message.getKeyString() != null && message.getKeyString().equals(messageKeyString)) {
                    position = messageList.indexOf(message);

                    if (position == messageList.size() - 1) {
                        updateQuickConversation = true;
                    }
                    if (message.getScheduledAt() != null && message.getScheduledAt() != 0) {
                        messageDatabaseService.deleteScheduledMessage(messageKeyString);
                    }
                    messageList.remove(position);
                    recyclerDetailConversationAdapter.notifyDataSetChanged();
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(VISIBLE);
                        ((MobiComKitActivityInterface) getActivity()).removeConversation(message, channel != null ? String.valueOf(channel.getKey()) : contact.getFormattedContactNumber());
                    }
                    break;
                }
            }
            int messageListSize = messageList.size();
            if (messageListSize > 0 && updateQuickConversation) {
                ((MobiComKitActivityInterface) getActivity()).updateLatestMessage(messageList.get(messageListSize - 1), channel != null ? String.valueOf(channel.getKey()) : contact.getFormattedContactNumber());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getCurrentUserId() {
        if (contact == null) {
            return "";
        }
        return contact.getUserId() != null ? contact.getUserId() : contact.getFormattedContactNumber();
    }

    public Contact getContact() {
        return contact;
    }

    protected void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getFormattedContactNumber() {
        return contact != null ? contact.getFormattedContactNumber() : null;
    }

    public boolean hasMultiplePhoneNumbers() {
        return contact != null && contact.hasMultiplePhoneNumbers();
    }

    public MultimediaOptionFragment getMultimediaOptionFragment() {
        return multimediaOptionFragment;
    }

    public Spinner getSendType() {
        return sendType;
    }

    public Spinner getSelfDestructMessageSpinner() {
        return selfDestructMessageSpinner;
    }

    public Button getScheduleOption() {
        return scheduleOption;
    }

    public void setFirstTimeMTexterFriend(boolean firstTimeMTexterFriend) {
        this.firstTimeMTexterFriend = firstTimeMTexterFriend;
    }

//    public EmojiconEditText getMessageEditText() {
//        return messageEditText;
//    }

    public void clearList() {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerDetailConversationAdapter != null) {
                    messageList.clear();
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                    recyclerDetailConversationAdapter.notifyDataSetChanged();
                }
                if (applozicContextSpinnerAdapter != null) {
                    contextFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    public void updateMessage(final Message message) {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Note: Removing and adding the same message again as the new sms object will contain the keyString.
                messageList.remove(message);
                messageList.add(message);
                recyclerDetailConversationAdapter.notifyDataSetChanged();
            }
        });
    }

    public void updateMessageMetadata(String keyString) {
        int i = -1;
        if (!messageList.isEmpty()) {
            for (Message message : messageList) {
                if (keyString.equals(message.getKeyString())) {
                    i = messageList.indexOf(message);
                }
            }
        }
        if (i != -1) {
            messageList.get(i).setMetadata(messageDatabaseService.getMessage(keyString).getMetadata());
            if (recyclerDetailConversationAdapter != null) {
                recyclerDetailConversationAdapter.notifyDataSetChanged();
            }
            if (messageList.get(messageList.size() - 1).getMetadata().containsKey("isDoneWithClicking")) {
                if (templateAdapter != null) {
                    templateAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void addMessage(final Message message) {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Todo: Handle disappearing messages.
                boolean added = updateMessageList(message, false);
                if (added) {
                    //Todo: update unread count
                    linearLayoutManager.setStackFromEnd(true);
                    if (recyclerDetailConversationAdapter == null) {
                        return;
                    }
                    recyclerDetailConversationAdapter.notifyDataSetChanged();

                    if (alCustomizationSettings.isMessageFastScrollEnabled()) {
                        if (messageDropDownActionButton.getVisibility() == View.INVISIBLE) {
                            linearLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                        }
                    } else {
                        linearLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                    }
                    emptyTextView.setVisibility(View.GONE);
                    currentConversationId = message.getConversationId();
                    channelKey = message.getGroupId();
                    if (Message.MessageType.MT_INBOX.getValue().equals(message.getType()) && (contact != null || (channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType())))) {
                        try {
                            if (alCustomizationSettings.isMessageFastScrollEnabled()) {
                                messageUnreadCount += 1;
                                messageUnreadCountTextView.setVisibility(VISIBLE);
                                messageUnreadCountTextView.setText(String.valueOf(messageUnreadCount));
                            }
                            messageDatabaseService.updateReadStatusForKeyString(message.getKeyString());
                            Intent intent = new Intent(getActivity(), UserIntentService.class);
                            intent.putExtra(UserIntentService.SINGLE_MESSAGE_READ, true);
                            intent.putExtra(UserIntentService.CONTACT, contact);
                            intent.putExtra(UserIntentService.CHANNEL, channel);
                            UserIntentService.enqueueWork(getActivity(), intent);
                        } catch (Exception e) {
                            Utils.printLog(getContext(), TAG, "Got exception while read");
                        }
                    }
                }
                createTemplateMessages();
                selfDestructMessage(message);
            }
        });
    }

    protected abstract void processMobiTexterUserCheck();

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.menu = menu;

        if (contact != null && contact.isDeleted()) {
            menu.findItem(R.id.dial).setVisible(false);
            menu.findItem(R.id.refresh).setVisible(false);
            menu.removeItem(R.id.conversations);
            menu.findItem(R.id.userBlock).setVisible(false);
            menu.findItem(R.id.userUnBlock).setVisible(false);
            menu.findItem(R.id.dial).setVisible(false);
            return;
        }

        String contactNumber = contact != null ? contact.getContactNumber() : null;
        ApplozicClient setting = ApplozicClient.getInstance(getActivity());

        if ((setting.isHandleDial() && !TextUtils.isEmpty(contactNumber) && contactNumber.length() > 2)
                || (setting.isIPCallEnabled())) {
            if (setting.isIPCallEnabled()) {
                menu.findItem(R.id.dial).setVisible(true);
                menu.findItem(R.id.video_call).setVisible(true);
            }
            if (setting.isHandleDial()) {
                menu.findItem(R.id.dial).setVisible(true);
            }
        } else {
            menu.findItem(R.id.video_call).setVisible(false);
            menu.findItem(R.id.dial).setVisible(false);
        }
        if (channel != null) {
            menu.findItem(R.id.dial).setVisible(false);
            menu.findItem(R.id.video_call).setVisible(false);

            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId) && alCustomizationSettings.isBlockOption()) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    if (withUserContact.isBlocked()) {
                        menu.findItem(R.id.userUnBlock).setVisible(true);
                    } else {
                        menu.findItem(R.id.userBlock).setVisible(true);
                    }
                }
            } else {
                menu.findItem(R.id.userBlock).setVisible(false);
                menu.findItem(R.id.userUnBlock).setVisible(false);
                if (alCustomizationSettings.isMuteOption() && !Channel.GroupType.BROADCAST.getValue().equals(channel.getType())) {
                    menu.findItem(R.id.unmuteGroup).setVisible(!Channel.GroupType.OPEN.getValue().equals(channel.getType()) && !channel.isDeleted() && channel.isNotificationMuted());
                    menu.findItem(R.id.muteGroup).setVisible(!Channel.GroupType.OPEN.getValue().equals(channel.getType()) && !channel.isDeleted() && !channel.isNotificationMuted());
                }
            }
        } else if (alCustomizationSettings.isMuteUserChatOption() && contact != null) {
            menu.findItem(R.id.userBlock).setVisible(false);
            menu.findItem(R.id.userUnBlock).setVisible(false);
            menu.findItem(R.id.unmuteGroup).setVisible(!contact.isDeleted() && contact.isNotificationMuted());
            menu.findItem(R.id.muteGroup).setVisible(!contact.isDeleted() && !contact.isNotificationMuted());

        } else if (contact != null && alCustomizationSettings.isBlockOption()) {
            if (contact.isBlocked()) {
                menu.findItem(R.id.userUnBlock).setVisible(true);
            } else {
                menu.findItem(R.id.userBlock).setVisible(true);
            }
        }

        menu.removeItem(R.id.menu_search);
        menu.removeItem(R.id.start_new);

        if (channel != null && channel.isDeleted()) {
            menu.findItem(R.id.refresh).setVisible(false);
            menu.findItem(R.id.deleteConversation).setVisible(false);
        } else {
            menu.findItem(R.id.refresh).setVisible(alCustomizationSettings.isRefreshOption());
            if (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                menu.findItem(R.id.deleteConversation).setVisible(false);
            } else {
                menu.findItem(R.id.deleteConversation).setVisible(alCustomizationSettings.isDeleteOption() || ApplozicSetting.getInstance(getContext()).isDeleteConversationOption());
            }
        }

        menu.removeItem(R.id.conversations);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.userBlock) {
            if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        userBlockDialog(true, appContactService.getContactById(userId), true);
                    }
                }
            } else if (contact != null) {
                userBlockDialog(true, contact, false);
            }
        }
        if (id == R.id.userUnBlock) {
            if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        userBlockDialog(false, appContactService.getContactById(userId), true);
                    }
                }
            } else if (contact != null) {
                userBlockDialog(false, contact, false);
            }
        }
        if (id == R.id.dial) {
            if (contact != null) {
                if (contact.isBlocked()) {
                    userBlockDialog(false, contact, false);
                } else {
                    ((ConversationActivity) getActivity()).processCall(contact, currentConversationId);
                }
            }
        }
        if (id == R.id.deleteConversation) {
            deleteConversationThread();
            return true;
        }

        if (id == R.id.video_call) {
            if (contact != null) {
                if (contact.isBlocked()) {
                    userBlockDialog(false, contact, false);
                } else {
                    ((ConversationActivity) getActivity()).processVideoCall(contact, currentConversationId);
                }
            }
        }
        if (id == R.id.muteGroup) {
            if (channel != null) {
                muteGroupChat();
            } else if (contact != null) {
                muteUserChat();
            }
        }
        if (id == R.id.unmuteGroup) {
            if (channel != null) {
                umuteGroupChat();
            } else if (contact != null) {
                unMuteUserChat();
            }
        }
        return false;
    }

    @Override
    public boolean onItemClick(int position, MenuItem item) {
        if (messageList.size() <= position || position == -1) {
            return true;
        }
        Message message = messageList.get(position);
        if (message.isTempDateType() || message.isCustom()) {
            return true;
        }

        switch (item.getItemId()) {
            case 0:
                if (getActivity() == null) {
                    break;
                }
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ApplozicService.getContext(getContext()).getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(message.getMessage());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(ApplozicService.getContext(getContext()).getString(R.string.copied_message), message.getMessage());
                    clipboard.setPrimaryClip(clip);
                }
                break;
            case 1:
                conversationUIService.startContactActivityForResult(message, null);
                break;
            case 2:
                Message messageToResend = new Message(message);
                //messageToResend.setCreatedAtTime(new Date().getTime());
                messageToResend.setCreatedAtTime(System.currentTimeMillis() + MobiComUserPreference.getInstance(getActivity()).getDeviceTimeOffset());
                conversationService.sendMessage(messageToResend, messageIntentClass);
                break;
            case 3:
                String messageKeyString = message.getKeyString();
                new DeleteConversationAsyncTask(conversationService, message, contact).execute();
                deleteMessageFromDeviceList(messageKeyString);
                break;
            case 4:
                String messageJson = GsonUtils.getJsonFromObject(message, Message.class);
                conversationUIService.startMessageInfoFragment(messageJson);
                break;
            case 5:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                if (message.getFilePaths() != null) {
                    Uri shareUri = null;
                    if (Utils.hasNougat()) {
                        shareUri = FileProvider.getUriForFile(ApplozicService.getContext(getContext()), Utils.getMetaDataValue(getActivity(), MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                    } else {
                        shareUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                    }
                    shareIntent.setDataAndType(shareUri, "text/x-vcard");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                    if (!TextUtils.isEmpty(message.getMessage())) {
                        shareIntent.putExtra(Intent.EXTRA_TEXT, message.getMessage());
                    }
                    shareIntent.setType(FileUtils.getMimeType(new File(message.getFilePaths().get(0))));
                } else {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, message.getMessage());
                    shareIntent.setType("text/plain");
                }
                startActivity(Intent.createChooser(shareIntent, ApplozicService.getContext(getContext()).getString(R.string.send_message_to)));
                break;

            case 6:
                try {
                    Configuration config = ApplozicService.getContext(getContext()).getResources().getConfiguration();
                    messageMetaData = new HashMap<>();
                    String displayName;
                    if (message.getGroupId() != null) {
                        if (loggedInUserId.equals(message.getContactIds()) || TextUtils.isEmpty(message.getContactIds())) {
                            displayName = ApplozicService.getContext(getContext()).getString(R.string.you_string);
                        } else {
                            displayName = appContactService.getContactById(message.getContactIds()).getDisplayName();
                        }
                    } else {
                        if (message.isTypeOutbox()) {
                            displayName = ApplozicService.getContext(getContext()).getString(R.string.you_string);
                        } else {
                            displayName = appContactService.getContactById(message.getContactIds()).getDisplayName();
                        }
                    }
                    nameTextView.setText(displayName);
                    if (message.hasAttachment()) {
                        FileMeta fileMeta = message.getFileMetas();
                        imageViewForAttachmentType.setVisibility(VISIBLE);
                        if (fileMeta.getContentType().contains("image")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_image_camera_alt);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.photo_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(VISIBLE);
                            imageViewRLayout.setVisibility(VISIBLE);
                            imageThumbnailLoader.loadImage(message, galleryImageView);
                        } else if (fileMeta.getContentType().contains("video")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_video);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                                    imageViewForAttachmentType.setScaleX(-1);
                                }
                            }
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.video_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            if (message.getFilePaths() != null && message.getFilePaths().size() > 0) {
                                if (imageCache.getBitmapFromMemCache(message.getKeyString()) != null) {
                                    galleryImageView.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
                                } else {
                                    imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(message.getFilePaths().get(0)));
                                    galleryImageView.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(message.getFilePaths().get(0)));
                                }
                            }
                            galleryImageView.setVisibility(VISIBLE);
                            imageViewRLayout.setVisibility(VISIBLE);
                        } else if (fileMeta.getContentType().contains("audio")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_music_note);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.audio_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else if (message.isContactMessage()) {
                            MobiComVCFParser parser = new MobiComVCFParser();
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                            try {
                                VCFContactData data = parser.parseCVFContactData(message.getFilePaths().get(0));
                                if (data != null) {
                                    messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.contact_string));
                                    messageTextView.append(" " + data.getName());
                                }
                            } catch (Exception e) {
                                imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.contact_string));
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_attachment);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.attachment_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        }
                        imageViewForAttachmentType.setColorFilter(ContextCompat.getColor(ApplozicService.getContext(getContext()), R.color.apploizc_lite_gray_color));
                    } else if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                        imageViewForAttachmentType.setVisibility(VISIBLE);
                        galleryImageView.setVisibility(VISIBLE);
                        imageViewRLayout.setVisibility(VISIBLE);
                        messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.al_location_string));
                        imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_location_on_white_24dp);
                        imageViewForAttachmentType.setColorFilter(ContextCompat.getColor(ApplozicService.getContext(getContext()), R.color.apploizc_lite_gray_color));
                        messageImageLoader.setLoadingImage(R.drawable.applozic_map_offline_thumbnail);
                        messageImageLoader.loadImage(LocationUtils.loadStaticMap(message.getMessage(), geoApiKey), galleryImageView);
                    } else {
                        imageViewForAttachmentType.setVisibility(View.GONE);
                        imageViewRLayout.setVisibility(View.GONE);
                        galleryImageView.setVisibility(View.GONE);
                        messageTextView.setText(message.getMessage());
                    }
                    messageMetaData.put(Message.MetaDataType.AL_REPLY.getValue(), message.getKeyString());
                    if (messageMetaData != null && !messageMetaData.isEmpty()) {
                        String replyMessageKey = messageMetaData.get(Message.MetaDataType.AL_REPLY.getValue());
                        if (!TextUtils.isEmpty(replyMessageKey) && (contact != null || (channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType())))) {
                            messageDatabaseService.updateMessageReplyType(replyMessageKey, Message.ReplyMessage.REPLY_MESSAGE.getValue());
                        }
                    }
                    attachReplyCancelLayout.setVisibility(VISIBLE);
                    replayRelativeLayout.setVisibility(VISIBLE);
                } catch (Exception e) {

                }
                break;

            case 7:
                if(message.hasAttachment()){
                FileMeta fileMeta = message.getFileMetas();
                if (fileMeta.getContentType().contains("image")) {
                    filename= fileMeta.getName();
                    fileMeta.getCreatedAtTime();
                    fileURL=fileMeta.getThumbnailUrl();
                TessBaseAPI tessBaseAPI = new TessBaseAPI();
                tessBaseAPI.setDebug(true);
                tessBaseAPI.init("/sdcard/", "eng");
                String path = "/sdcard/Applozic/image/"+filename;
                tessBaseAPI.setImage(GetBitmap(path));
                final String result = tessBaseAPI.getUTF8Text();
                tessBaseAPI.end();
                AlertDialog.Builder builder = new AlertDialog.Builder(MobiComConversationFragment.this.getContext());
                builder.setTitle("Identify Result")
                        .setMessage(result)
                        .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ApplozicService.getContext(getContext()).getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(result);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                            }
                        })
                        .show();

        }}
        else {
                    Toast.makeText(MobiComConversationFragment.this.getContext(),"Only Photo can identify",Toast.LENGTH_LONG).show();
                }}

        return true;
    }


    public static Bitmap GetBitmap(String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth+20;
        int height = opts.outHeight+30;
        float scaleWidth = 0.f, scaleHeight = 0.f;
        opts.inJustDecodeBounds = false;
        float scale = Math.max(scaleWidth, scaleHeight);
        opts.inSampleSize = (int) scale;
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(
                BitmapFactory.decodeFile(path, opts));
        return Bitmap.createScaledBitmap(weak.get(), width, height, true);
    }

    public void loadConversation(final Contact contact, final Channel channel, final Integer conversationId, final String searchString) {
        try {
            if (downloadConversation != null) {
                downloadConversation.cancel(true);
            }
            setContact(contact);
            setChannel(channel);

            BroadcastService.currentUserId = contact != null ? contact.getContactIds() : String.valueOf(channel.getKey());
            typingStarted = false;
            onSelected = false;
            messageMetaData = null;

            if (userNotAbleToChatLayout != null) {
                if (contact != null && contact.isDeleted()) {
                    userNotAbleToChatLayout.setVisibility(VISIBLE);
                    individualMessageSendLayout.setVisibility(View.GONE);
                } else {
                    userNotAbleToChatLayout.setVisibility(View.GONE);
                    individualMessageSendLayout.setVisibility(VISIBLE);
                }
            }

            if (contact != null && this.channel != null) {
                if (getActivity() != null) {
                    setToolbarSubtitle("");
                    setToolbarImage(contact, channel);
                }
                if (menu != null) {
                    menu.findItem(R.id.unmuteGroup).setVisible(false);
                    menu.findItem(R.id.muteGroup).setVisible(false);
                }
            }
            if (replayRelativeLayout != null) {
                replayRelativeLayout.setVisibility(View.GONE);
            }
        /*
        filePath = null;*/
            if (TextUtils.isEmpty(filePath) && attachmentLayout != null) {
                attachmentLayout.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(defaultText) && messageEditText != null) {
                messageEditText.setText(defaultText);
                defaultText = "";
            }

            // infoBroadcast.setVisibility(channel != null ? View.VISIBLE : View.GONE);
            extendedSendingOptionLayout.setVisibility(VISIBLE);

            unregisterForContextMenu(recyclerView);
            if (getActivity() != null) {
                if (ApplozicClient.getInstance(getActivity()).isNotificationStacking()) {
                    NotificationManagerCompat nMgr = NotificationManagerCompat.from(getActivity());
                    nMgr.cancel(NotificationService.NOTIFICATION_ID);
                } else {
                    if (contact != null) {
                        if (!TextUtils.isEmpty(contact.getContactIds())) {
                            NotificationManager notificationManager =
                                    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                            if (notificationManager != null) {
                                notificationManager.cancel(contact.getContactIds().hashCode());
                            }
                        }
                    }

                    if (channel != null) {
                        NotificationManager notificationManager =
                                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        if (notificationManager != null) {
                            notificationManager.cancel(String.valueOf(channel.getKey()).hashCode());
                        }
                    }
                }
            }

            clearList();
            updateTitle();
            swipeLayout.setEnabled(true);
            loadMore = true;
            if (selfDestructMessageSpinner != null) {
                selfDestructMessageSpinner.setSelection(0);
            }

            if (contact != null) {
                recyclerDetailConversationAdapter = new DetailedConversationAdapter(getActivity(), messageList, contact, messageIntentClass, emojiIconHandler);
                recyclerDetailConversationAdapter.setAlCustomizationSettings(alCustomizationSettings);
                recyclerDetailConversationAdapter.setRichMessageCallbackListener(this);
                recyclerDetailConversationAdapter.setContextMenuClickListener(this);
                if (getActivity() instanceof ALStoragePermissionListener) {
                    recyclerDetailConversationAdapter.setStoragePermissionListener((ALStoragePermissionListener) getActivity());
                } else {
                    recyclerDetailConversationAdapter.setStoragePermissionListener(new ALStoragePermissionListener() {
                        @Override
                        public boolean isPermissionGranted() {
                            return false;
                        }

                        @Override
                        public void checkPermission(ALStoragePermission storagePermission) {

                        }
                    });
                }
            } else if (channel != null) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!ChannelService.getInstance(getContext()).isUserAlreadyPresentInChannel(channel.getKey(), loggedInUserId)
                                && messageTemplate != null && messageTemplate.isEnabled() && templateAdapter != null) {
                            if (getActivity() == null) {
                                return;
                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    templateAdapter.removeTemplates();
                                }
                            });
                        }
                    }
                });
                thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();

                recyclerDetailConversationAdapter = new DetailedConversationAdapter(getActivity(), messageList, channel, messageIntentClass, emojiIconHandler);
                recyclerDetailConversationAdapter.setAlCustomizationSettings(alCustomizationSettings);
                recyclerDetailConversationAdapter.setContextMenuClickListener(this);
                recyclerDetailConversationAdapter.setRichMessageCallbackListener(this);
                if (getActivity() instanceof ALStoragePermissionListener) {
                    recyclerDetailConversationAdapter.setStoragePermissionListener((ALStoragePermissionListener) getActivity());
                } else {
                    recyclerDetailConversationAdapter.setStoragePermissionListener(new ALStoragePermissionListener() {
                        @Override
                        public boolean isPermissionGranted() {
                            return false;
                        }

                        @Override
                        public void checkPermission(ALStoragePermission storagePermission) {

                        }
                    });
                }
            }
            linearLayoutManager.setSmoothScrollbarEnabled(true);
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    linearLayoutManager.setStackFromEnd(true);
                    //recyclerView.smoothScrollToPosition(messageList.size());
                }
            });
            recyclerView.setAdapter(recyclerDetailConversationAdapter);
            registerForContextMenu(recyclerView);

            processMobiTexterUserCheck();

            downloadConversation = new DownloadConversation(recyclerView, true, 1, 0, 0, contact, channel, conversationId);
            downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            if (hideExtendedSendingOptionLayout) {
                extendedSendingOptionLayout.setVisibility(View.GONE);
            }
            emoticonsFrameLayout.setVisibility(View.GONE);

            if (contact != null) {
                Intent intent = new Intent(getActivity(), UserIntentService.class);
                intent.putExtra(UserIntentService.USER_ID, contact.getUserId());
                UserIntentService.enqueueWork(getActivity(), intent);
            }

            if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        Intent intent = new Intent(getActivity(), UserIntentService.class);
                        intent.putExtra(UserIntentService.USER_ID, userId);
                        UserIntentService.enqueueWork(getActivity(), intent);
                    }
                } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            updateChannelSubTitle(channel);
                        }
                    });
                    thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    thread.start();
                }
            }

            if (alCustomizationSettings.isMessageFastScrollEnabled()) {
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.smoothScrollToPosition(messageList.size());
                        recyclerView.getLayoutManager().scrollToPosition(messageList.size());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateLastSeenStatus() {
        if (this.getActivity() == null) {
            return;
        }
        if (contact != null) {
            appContactService.getContactByIdAsync(contact.getContactIds(), new AlContactListener() {
                @Override
                public void onGetContact(Contact contact) {
                    if (contact != null) {
                        processUpdateLastSeenStatus(contact);
                    }
                }
            });
        } else if (channel != null && Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
            String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
            if (!TextUtils.isEmpty(userId)) {
                Contact withUserContact = appContactService.getContactById(userId);
                processUpdateLastSeenStatus(withUserContact);
            }
        }
    }


    protected void processUpdateLastSeenStatus(final Contact withUserContact) {
        if (withUserContact == null) {
            return;
        }

        if (this.getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userNotAbleToChatLayout != null && individualMessageSendLayout != null) {
                    userNotAbleToChatLayout.setVisibility(withUserContact.isDeleted() ? VISIBLE : View.GONE);
                    individualMessageSendLayout.setVisibility(withUserContact.isDeleted() ? View.GONE : VISIBLE);
                    bottomlayoutTextView.setText(R.string.user_has_been_deleted_text);
                }

                if (menu != null) {
                    menu.findItem(R.id.userBlock).setVisible(alCustomizationSettings.isBlockOption() ? !withUserContact.isDeleted() && !withUserContact.isBlocked() : alCustomizationSettings.isBlockOption());
                    menu.findItem(R.id.userUnBlock).setVisible(alCustomizationSettings.isBlockOption() ? !withUserContact.isDeleted() && withUserContact.isBlocked() : alCustomizationSettings.isBlockOption());
                    menu.findItem(R.id.refresh).setVisible(alCustomizationSettings.isRefreshOption() ? !withUserContact.isDeleted() : alCustomizationSettings.isRefreshOption());
                }

                if (withUserContact.isBlocked() || withUserContact.isBlockedBy() || withUserContact.isDeleted()) {
                    if (getActivity() != null) {
                        setToolbarSubtitle("");
                    }
                    return;
                }
                if (withUserContact != null) {
                    if (withUserContact.isConnected()) {
                        typingStarted = false;
                        if (getActivity() != null) {
                            setToolbarSubtitle(ApplozicService.getContext(getContext()).getString(R.string.user_online));
                            setToolbarImage(withUserContact, null);
                        }
                    } else if (withUserContact.getLastSeenAt() != 0) {
                        if (getActivity() != null) {
                            setToolbarSubtitle(ApplozicService.getContext(getContext()).getString(R.string.subtitle_last_seen_at_time) + " " + DateUtils.getDateAndTimeForLastSeen(ApplozicService.getContext(getContext()), withUserContact.getLastSeenAt(), R.string.JUST_NOW, R.plurals.MINUTES_AGO, R.plurals.HOURS_AGO, R.string.YESTERDAY));
                            setToolbarImage(withUserContact, null);
                        }
                    } else {
                        if (getActivity() != null) {
                            setToolbarSubtitle("");
                            setToolbarImage(withUserContact, null);
                        }
                    }
                }
            }

        });
    }

    public void updateChannelSubTitle(final Channel channel) {

        channelUserMapperList = ChannelService.getInstance(getActivity()).getListOfUsersFromChannelUserMapper(channel.getKey());
        if (channelUserMapperList != null && channelUserMapperList.size() > 0) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    final Contact withUserContact = appContactService.getContactById(userId);
                    if (withUserContact != null) {
                        if (getActivity() == null) {
                            return;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (withUserContact.isBlocked()) {
                                    if (getActivity() != null) {
                                        setToolbarSubtitle("");
                                    }
                                } else {
                                    if (withUserContact.isConnected() && getActivity() != null) {
                                        setToolbarSubtitle(ApplozicService.getContext(getContext()).getString(R.string.user_online));
                                        setToolbarImage(null, channel);
                                    } else if (withUserContact.getLastSeenAt() != 0 && getActivity() != null) {
                                        setToolbarSubtitle(ApplozicService.getContext(getContext()).getString(R.string.subtitle_last_seen_at_time) + " " + DateUtils.getDateAndTimeForLastSeen(getContext(), withUserContact.getLastSeenAt(), R.string.JUST_NOW, R.plurals.MINUTES_AGO, R.plurals.HOURS_AGO, R.string.YESTERDAY));
                                        setToolbarImage(null, channel);
                                    } else if (getActivity() != null) {
                                        setToolbarSubtitle("");
                                        setToolbarImage(null, channel);
                                    }
                                }
                            }
                        });
                    }
                }

            } else {
                final StringBuffer stringBuffer = new StringBuffer();
                Contact contactDisplayName;
                String youString = "";
                int i = 0;
                for (ChannelUserMapper channelUserMapper : channelUserMapperList) {
                    i++;
                    if (i > 20)
                        break;
                    contactDisplayName = appContactService.getContactById(channelUserMapper.getUserKey());
                    if (!TextUtils.isEmpty(channelUserMapper.getUserKey())) {
                        if (loggedInUserId.equals(channelUserMapper.getUserKey())) {
                            youString = ApplozicService.getContext(getContext()).getString(R.string.you_string);
                        } else {
                            stringBuffer.append(contactDisplayName.getDisplayName()).append(",");
                        }
                    }
                }

                final String finalYouString = youString;
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!TextUtils.isEmpty(stringBuffer)) {
                            if (channelUserMapperList.size() <= 20) {
                                if (!TextUtils.isEmpty(finalYouString)) {
                                    stringBuffer.append(finalYouString).append(",");
                                }
                                int lastIndex = stringBuffer.lastIndexOf(",");
                                String userIds = stringBuffer.replace(lastIndex, lastIndex + 1, "").toString();
                                if (getActivity() != null) {
                                    setToolbarSubtitle(userIds);
                                    setToolbarImage(null, channel);
                                }
                            } else {
                                if (getActivity() != null) {
                                    setToolbarSubtitle(stringBuffer.toString());
                                    setToolbarImage(null, channel);
                                }
                            }
                        } else {
                            if (getActivity() != null) {
                                setToolbarSubtitle(finalYouString);
                                setToolbarImage(null, channel);
                            }
                        }
                    }
                });
            }
        }
    }

    public boolean isBroadcastedToChannel(Integer channelKey) {
        return getChannel() != null && getChannel().getKey().equals(channelKey);
    }

    public boolean getCurrentChannelKey(Integer channelKey) {
        return channel != null && channel.getKey().equals(channelKey);
    }

    public Channel getChannel() {
        return channel;
    }

    protected void setChannel(Channel channel) {
        this.channel = channel;

    }

    public boolean isMsgForConversation(Message message) {

        if (BroadcastService.isContextBasedChatEnabled() && message.getConversationId() != null) {
            return isMessageForCurrentConversation(message) && compareConversationId(message);
        }
        return isMessageForCurrentConversation(message);
    }

    public boolean isMessageForCurrentConversation(Message message) {
        return (message.getGroupId() != null && channel != null && message.getGroupId().equals(channel.getKey())) ||
                (!TextUtils.isEmpty(message.getContactIds()) && contact != null && message.getContactIds().equals(contact.getContactIds())) && message.getGroupId() == null;
    }

    public boolean compareConversationId(Message message) {
        return message.getConversationId() != null && currentConversationId != null && message.getConversationId().equals(currentConversationId);
    }

//    public void onEmojiconBackspace() {
//        EmojiconsFragment.backspace(messageEditText);
//    }

    public void updateUploadFailedStatus(final Message message) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i = messageList.indexOf(message);
                if (i != -1) {
                    messageList.get(i).setCanceled(true);
                    recyclerDetailConversationAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    public void downloadFailed(final Message message) {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = messageList.indexOf(message);
                if (index != -1) {
                    View view = recyclerView.getChildAt(index -
                            linearLayoutManager.findFirstVisibleItemPosition());

                    if (view != null) {
                        final LinearLayout attachmentDownloadLayout = (LinearLayout) view.findViewById(R.id.attachment_download_layout);
                        attachmentDownloadLayout.setVisibility(VISIBLE);
                    }

                }
            }

        });
    }

    abstract public void attachLocation(Location mCurrentLocation);

    public void updateDeliveryStatusForAllMessages(final boolean markRead) {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Drawable statusIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered);
                    if (markRead) {
                        statusIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_read);
                    }
                    for (int index = 0; index < messageList.size(); index++) {
                        Message message = messageList.get(index);
                        if ((message.getStatus() == Message.Status.DELIVERED_AND_READ.getValue()) || message.isTempDateType() || message.isCustom() || !message.isTypeOutbox() || message.isChannelCustomMessage()) {
                            continue;
                        }
                        if (messageList.get(index) != null) {
                            messageList.get(index).setDelivered(true);
                        }
                        message.setDelivered(true);
                        if (markRead) {
                            if (messageList.get(index) != null) {
                                messageList.get(index).setStatus(Message.Status.DELIVERED_AND_READ.getValue());
                            }
                            message.setStatus(Message.Status.DELIVERED_AND_READ.getValue());
                        }
                        View view = recyclerView.getChildAt(index -
                                linearLayoutManager.findFirstVisibleItemPosition());
                        if (view != null && !message.isCustom() && !message.isChannelCustomMessage()) {
                            TextView createdAtTime = (TextView) view.findViewById(R.id.createdAtTime);
                            TextView status = (TextView) view.findViewById(R.id.status);
                            //status.setText("Delivered");
                            createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, statusIcon, null);
                        }
                    }
                } catch (Exception ex) {
                    Utils.printLog(getContext(), TAG, "Exception while updating delivery status in UI.");
                }
            }
        });
    }

    public void updateDeliveryStatus(final Message message) {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int index = messageList.indexOf(message);
                    if (index != -1) {
                        if (messageList.get(index).getStatus() == Message.Status.DELIVERED_AND_READ.getValue()
                                || messageList.get(index).isTempDateType()
                                || messageList.get(index).isCustom()
                                || messageList.get(index).isChannelCustomMessage()) {
                            return;
                        }
                        messageList.get(index).setDelivered(true);
                        messageList.get(index).setStatus(message.getStatus());
                        View view = recyclerView.getChildAt(index -
                                linearLayoutManager.findFirstVisibleItemPosition());
                        if (view != null && !messageList.get(index).isCustom()) {
                            TextView createdAtTime = (TextView) view.findViewById(R.id.createdAtTime);
                            /*TextView status = (TextView) view.findViewById(R.id.status);
                            status.setText("Delivered");*/
                            Drawable statusIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered);
                            if (message.getStatus() == Message.Status.DELIVERED_AND_READ.getValue()) {
                                statusIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_read);
                                messageList.get(index).setStatus(Message.Status.DELIVERED_AND_READ.getValue());
                            }
                            createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, statusIcon, null);
                        }
                    } else if (!message.isVideoNotificationMessage() && !message.isHidden()) {
                        messageList.add(message);
                        linearLayoutManager.scrollToPositionWithOffset(messageList.size() - 1, 0);
                        emptyTextView.setVisibility(View.GONE);
                        recyclerDetailConversationAdapter.notifyDataSetChanged();
                    }
                } catch (Exception ex) {
                    Utils.printLog(getContext(), TAG, "Exception while updating delivery status in UI.");
                }
            }
        });
    }

    public void loadFile(Uri uri) {
        loadFile(uri, null);
    }

    public void loadFile(Uri uri, File file) {
        if (uri == null || file == null) {
            Toast.makeText(getActivity(), R.string.file_not_selected, Toast.LENGTH_LONG).show();
            return;
        }
        handleSendAndRecordButtonView(true);
        errorEditTextView.setVisibility(View.GONE);
        long fileSize = file.length() / 1024;
        long maxFileSize = alCustomizationSettings.getMaxAttachmentSizeAllowed() * 1024 * 1024;
        if (fileSize > maxFileSize) {
            Toast.makeText(getActivity(), R.string.info_attachment_max_allowed_file_size, Toast.LENGTH_LONG).show();
            return;
        }

        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        if (mimeType != null && (mimeType.startsWith("image"))) {
            AttachmentAsyncTask attachmentAsyncTask = new AttachmentAsyncTask(uri, file, getActivity());
            attachmentAsyncTask.setImageViewLayoutWeakReference(mediaContainer);
            attachmentAsyncTask.setRelativeLayoutWeakReference(attachmentLayout);
            attachmentAsyncTask.setTextViewWeakReference(attachedFile);
            attachmentAsyncTask.setAlCustomizationSettingsLayoutWeakReference(alCustomizationSettings);
            attachmentAsyncTask.execute();
        } else {
            filePath = Uri.parse(file.getAbsolutePath()).toString();
        }

    }

    public synchronized boolean updateMessageList(Message message, boolean update) {
        boolean toAdd = !messageList.contains(message);
        loadMore = true;
        if (update) {
            messageList.remove(message);
            messageList.add(message);
        } else if (toAdd) {
            Message firstDateMessage = new Message();
            firstDateMessage.setTempDateType(Short.valueOf("100"));
            firstDateMessage.setCreatedAtTime(message.getCreatedAtTime());
            if (!messageList.contains(firstDateMessage)) {
                messageList.add(firstDateMessage);
            }

            messageList.add(message);
        }
        return toAdd;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        support = new Support(activity);
        try {
            messageCommunicator = (MessageCommunicator) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement interfaceDataCommunicator");
        }
    }

    protected AlertDialog showInviteDialog(int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(getString(messageId).replace("[name]", getNameForInviteDialog()))
                .setTitle(titleId);
        builder.setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent share = new Intent(Intent.ACTION_SEND);
               /* String textToShare = getActivity().getResources().getString(R.string.invite_message);
                share.setAction(Intent.ACTION_SEND)
                        .setType("text/plain").putExtra(Intent.EXTRA_TEXT, textToShare);*/
                startActivity(Intent.createChooser(share, "Share Via"));
                sendType.setSelection(0);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                sendType.setSelection(0);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }


    public String getNameForInviteDialog() {
        if (contact != null) {
            return contact.getDisplayName();
        } else if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    return withUserContact.getDisplayName();
                }
            } else {
                return ChannelUtils.getChannelTitleName(channel, loggedInUserId);
            }
        }
        return "";
    }

    public void onClickOnMessageReply(Message message) {
        if (message != null) {
            if (recyclerView != null) {
                int height = recyclerView.getHeight();
                int itemHeight = recyclerView.getChildAt(0).getHeight();
                int index = messageList.indexOf(message);
                if (index != -1) {
                    recyclerView.requestFocusFromTouch();
                    linearLayoutManager.setStackFromEnd(true);
                    //recyclerView.smoothScrollBy(0, height / 2 - itemHeight / 2);
                    //recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, new RecyclerView.State(), index);
                    linearLayoutManager.scrollToPositionWithOffset(index, height / 2 - itemHeight / 2);
                    //((ListView) listView).setSelectionFromTop(index + 1, height / 2 - itemHeight / 2);
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (recyclerView != null) {
                                try {
                                    if (recyclerView.isFocused()) {
                                        recyclerView.clearFocus();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }, 800);
                }
            }

        }
    }

    public void forwardMessage(Message messageToForward, Contact contact, Channel channel) {
        this.contact = contact;
        this.channel = channel;
        if (messageToForward.isAttachmentDownloaded()) {
            filePath = messageToForward.getFilePaths().get(0);
        }
        this.messageToForward = messageToForward;
        loadConversation(contact, channel, currentConversationId, null);

    }

    private void sendForwardMessage(Message messageToForward) {
        //reset Messages Fields...
        if (getActivity() == null) {
            return;
        }
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());

        if (channel != null) {
            if (!ChannelService.getInstance(getContext()).processIsUserPresentInChannel(channel.getKey())) {
                return;
            }
            messageToForward.setGroupId(channel.getKey());
            messageToForward.setClientGroupId(null);
            messageToForward.setContactIds(null);
            messageToForward.setTo(null);
        } else {
            if (contact.isBlocked()) {
                return;
            }
            messageToForward.setGroupId(null);
            messageToForward.setClientGroupId(null);
            messageToForward.setTo(contact.getContactIds());
            messageToForward.setContactIds(contact.getContactIds());
        }

        messageToForward.setKeyString(null);
        messageToForward.setMessageId(null);
        messageToForward.setDelivered(false);
        messageToForward.setRead(Boolean.TRUE);
        messageToForward.setStoreOnDevice(Boolean.TRUE);
        messageToForward.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
        if (currentConversationId != null && currentConversationId != 0) {
            messageToForward.setConversationId(currentConversationId);
        }
        Map<String, String> metaDataMapForward = messageToForward.getMetadata();
        if (metaDataMapForward != null && !metaDataMapForward.isEmpty() && metaDataMapForward.get(Message.MetaDataType.AL_REPLY.getValue()) != null) {
            messageToForward.setMetadata(null);
        }
        messageToForward.setSendToDevice(Boolean.FALSE);
        messageToForward.setType(sendType.getSelectedItemId() == 1 ? Message.MessageType.MT_OUTBOX.getValue() : Message.MessageType.OUTBOX.getValue());
        messageToForward.setTimeToLive(getTimeToLive());
        messageToForward.setSentToServer(false);
        messageToForward.setStatus(Message.Status.READ.getValue());

        if (!TextUtils.isEmpty(filePath)) {
            List<String> filePaths = new ArrayList<String>();
            filePaths.add(filePath);
            messageToForward.setFilePaths(filePaths);
        }
        conversationService.sendMessage(messageToForward, messageIntentClass);
        if (selfDestructMessageSpinner != null) {
            selfDestructMessageSpinner.setSelection(0);
        }
        attachmentLayout.setVisibility(View.GONE);
        filePath = null;
    }


    public void sendMessage(String message, Map<String, String> messageMetaData, FileMeta fileMetas, String fileMetaKeyStrings, short messageContentType) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());
        Message messageToSend = new Message();

        if (channel != null) {
            messageToSend.setGroupId(channel.getKey());
            if (!TextUtils.isEmpty(channel.getClientGroupId())) {
                messageToSend.setClientGroupId(channel.getClientGroupId());
            }
            /*   List<String> contactIds = new ArrayList<String>();
            List<String> toList = new ArrayList<String>();
            for (Contact contact : channel.getContacts()) {
                if (!TextUtils.isEmpty(contact.getContactNumber())) {
                    toList.add(contact.getContactNumber());
                    contactIds.add(contact.getFormattedContactNumber());
                }
            }
            messageToSend.setTo(TextUtils.join(",", toList));
            messageToSend.setContactIds(TextUtils.join(",", contactIds));*/
        } else {
            messageToSend.setTo(contact.getContactIds());
            messageToSend.setContactIds(contact.getContactIds());
        }
        messageToSend.setRead(Boolean.TRUE);
        messageToSend.setStoreOnDevice(Boolean.TRUE);
        if (messageToSend.getCreatedAtTime() == null) {
            messageToSend.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
        }
        if (currentConversationId != null && currentConversationId != 0) {
            messageToSend.setConversationId(currentConversationId);
        }
        messageToSend.setSendToDevice(Boolean.FALSE);
        messageToSend.setType(sendType.getSelectedItemId() == 1 ? Message.MessageType.MT_OUTBOX.getValue() : Message.MessageType.OUTBOX.getValue());
        messageToSend.setTimeToLive(getTimeToLive());
        messageToSend.setMessage(message);
        messageToSend.setDeviceKeyString(userPreferences.getDeviceKeyString());
        messageToSend.setScheduledAt(scheduledTimeHolder.getTimestamp());
        messageToSend.setSource(Message.Source.MT_MOBILE_APP.getValue());
        if (!TextUtils.isEmpty(filePath)) {
            List<String> filePaths = new ArrayList<String>();
            filePaths.add(filePath);
            messageToSend.setFilePaths(filePaths);
            if (messageContentType == Message.ContentType.AUDIO_MSG.getValue() || messageContentType == Message.ContentType.CONTACT_MSG.getValue() || messageContentType == Message.ContentType.VIDEO_MSG.getValue()) {
                messageToSend.setContentType(messageContentType);
            } else {
                messageToSend.setContentType(Message.ContentType.ATTACHMENT.getValue());
            }
        } else {
            messageToSend.setContentType(messageContentType);
        }
        if (messageMetaData == null) {
            messageMetaData = new HashMap<>();
        }
        messageToSend.setFileMetaKeyStrings(fileMetaKeyStrings);
        messageToSend.setFileMetas(fileMetas);
        if (!TextUtils.isEmpty(ApplozicClient.getInstance(getActivity()).getMessageMetaData())) {
            Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> messageMetaDataMap = null;
            try {
                messageMetaDataMap = new Gson().fromJson(ApplozicClient.getInstance(getActivity()).getMessageMetaData(), mapType);
                if (messageMetaDataMap != null && !messageMetaDataMap.isEmpty()) {
                    messageMetaData.putAll(messageMetaDataMap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (this.messageMetaData != null && !this.messageMetaData.isEmpty()) {
            messageMetaData.putAll(this.messageMetaData);
        }

        messageToSend.setMetadata(messageMetaData);


        conversationService.sendMessage(messageToSend, messageIntentClass);
        if (replayRelativeLayout != null) {
            replayRelativeLayout.setVisibility(View.GONE);
        }
        if (selfDestructMessageSpinner != null) {
            selfDestructMessageSpinner.setSelection(0);
        }
        attachmentLayout.setVisibility(View.GONE);
        if (channel != null && channel.getType() != null && Channel.GroupType.BROADCAST_ONE_BY_ONE.getValue().equals(channel.getType())) {
            sendBroadcastMessage(message, filePath);
        }
        this.messageMetaData = null;
        filePath = null;
    }

    public void sendProductMessage(final String messageToSend, final FileMeta fileMeta, final Contact contact, final short messageContentType) {
        final Message message = new Message();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String topicId;
                MobiComConversationService conversationService = new MobiComConversationService(getActivity());
                MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());
                topicId = new MessageClientService(getActivity()).getTopicId(currentConversationId);
                if (getChannel() != null) {
                    message.setGroupId(channelKey);
                } else {
                    message.setContactIds(contact.getUserId());
                    message.setTo(contact.getUserId());
                }
                message.setMessage(messageToSend);
                message.setRead(Boolean.TRUE);
                message.setStoreOnDevice(Boolean.TRUE);
                message.setSendToDevice(Boolean.FALSE);
                message.setContentType(messageContentType);
                message.setType(Message.MessageType.MT_OUTBOX.getValue());
                message.setDeviceKeyString(userPreferences.getDeviceKeyString());
                message.setSource(Message.Source.MT_MOBILE_APP.getValue());
                message.setTopicId(messageToSend);
                message.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
                message.setTopicId(topicId);
                message.setConversationId(currentConversationId);
                message.setFileMetas(fileMeta);
                conversationService.sendMessage(message, MessageIntentService.class);
            }
        }).start();

    }

    public void sendBroadcastMessage(String message, String path) {
        MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(getActivity());
        if (channelUserMapperList != null && channelUserMapperList.size() > 0) {
            for (ChannelUserMapper channelUserMapper : channelUserMapperList) {
                if (!loggedInUserId.equals(channelUserMapper.getUserKey())) {
                    Message messageToSend = new Message();
                    messageToSend.setTo(channelUserMapper.getUserKey());
                    messageToSend.setContactIds(channelUserMapper.getUserKey());
                    messageToSend.setRead(Boolean.TRUE);
                    messageToSend.setStoreOnDevice(Boolean.TRUE);
                    if (messageToSend.getCreatedAtTime() == null) {
                        messageToSend.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
                    }
                    if (currentConversationId != null && currentConversationId != 0) {
                        messageToSend.setConversationId(currentConversationId);
                    }
                    messageToSend.setSendToDevice(Boolean.FALSE);
                    messageToSend.setType(sendType.getSelectedItemId() == 1 ? Message.MessageType.MT_OUTBOX.getValue() : Message.MessageType.OUTBOX.getValue());
                    messageToSend.setTimeToLive(getTimeToLive());
                    messageToSend.setMessage(message);
                    messageToSend.setDeviceKeyString(userPreferences.getDeviceKeyString());
                    messageToSend.setScheduledAt(scheduledTimeHolder.getTimestamp());
                    messageToSend.setSource(Message.Source.MT_MOBILE_APP.getValue());
                    if (!TextUtils.isEmpty(path)) {
                        List<String> filePaths = new ArrayList<String>();
                        filePaths.add(path);
                        messageToSend.setFilePaths(filePaths);
                    }
                    conversationService.sendMessage(messageToSend, MessageIntentService.class);

                    if (selfDestructMessageSpinner != null) {
                        selfDestructMessageSpinner.setSelection(0);
                    }
                    attachmentLayout.setVisibility(View.GONE);
                }
            }
        }
    }

    private Integer getTimeToLive() {
        if (selfDestructMessageSpinner == null || selfDestructMessageSpinner.getSelectedItemPosition() <= 1) {
            return null;
        }
        return Integer.parseInt(selfDestructMessageSpinner.getSelectedItem().toString().replace("mins", "").replace("min", "").trim());
    }

    public void sendMessage(String message) {
        sendMessage(message, null, null, null, Message.ContentType.DEFAULT.getValue());
    }

    public void sendMessage(short messageContentType, String filePath) {
        this.filePath = filePath;
        sendMessage("", messageContentType);
    }

    public void sendMessage(String message, short messageContentType, String filePath) {
        this.filePath = filePath;
        sendMessage(message, null, null, null, messageContentType);
    }

    public void sendMessage(String message, short messageContentType) {
        sendMessage(message, null, null, null, messageContentType);
    }

    public void sendMessage(String message, Map<String, String> messageMetaData, short messageContentType) {
        sendMessage(message, messageMetaData, null, null, messageContentType);
    }

    public void updateMessageKeyString(final Message message) {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = messageList.indexOf(message);
                if (index != -1) {
                    Message messageListItem = messageList.get(index);
                    messageListItem.setKeyString(message.getKeyString());
                    messageListItem.setSentToServer(true);
                    messageListItem.setCreatedAtTime(message.getSentMessageTimeAtServer());
                    messageListItem.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                    messageListItem.setFileMetas(message.getFileMetas());
                    if (messageList.get(index) != null) {
                        messageList.get(index).setKeyString(message.getKeyString());
                        messageList.get(index).setSentToServer(true);
                        messageList.get(index).setCreatedAtTime(message.getSentMessageTimeAtServer());
                        messageList.get(index).setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                        messageList.get(index).setFileMetas(message.getFileMetas());
                    }
                    View view = recyclerView.getChildAt(index - linearLayoutManager.findFirstVisibleItemPosition());
                    if (view != null) {
                        ProgressBar mediaUploadProgressBarIndividualMessage = (ProgressBar) view.findViewById(R.id.media_upload_progress_bar);
                        RelativeLayout downloadInProgressLayout = (RelativeLayout) view.findViewById(R.id.applozic_doc_download_progress_rl);
                        if (mediaUploadProgressBarIndividualMessage != null) {
                            mediaUploadProgressBarIndividualMessage.setVisibility(View.GONE);
                        }
                        if (downloadInProgressLayout != null) {
                            downloadInProgressLayout.setVisibility(View.GONE);
                        }
                        if (message.getFileMetas() != null && !"image".contains(message.getFileMetas().getContentType()) && !"video".contains(message.getFileMetas().getContentType())) {
                            RelativeLayout applozicDocRelativeLayout = (RelativeLayout) view.findViewById(R.id.applozic_doc_downloaded);
                            ImageView imageViewDoc = (ImageView) applozicDocRelativeLayout.findViewById(R.id.doc_icon);
                            if (message.getFileMetas() != null) {
                                if (message.getFileMetas().getContentType().contains("audio")) {
                                    imageViewDoc.setImageResource(R.drawable.ic_play_circle_outline);
                                } else {
                                    imageViewDoc.setImageResource(R.drawable.ic_documentreceive);
                                }
                                applozicDocRelativeLayout.setVisibility(VISIBLE);
                            } else if (message.getFilePaths() != null) {
                                String filePath = message.getFilePaths().get(0);
                                final String mimeType = FileUtils.getMimeType(filePath);
                                if (mimeType.contains("audio")) {
                                    imageViewDoc.setImageResource(R.drawable.ic_play_circle_outline);
                                } else {
                                    imageViewDoc.setImageResource(R.drawable.ic_documentreceive);
                                }
                                applozicDocRelativeLayout.setVisibility(VISIBLE);
                            }
                        }
                        TextView createdAtTime = (TextView) view.findViewById(R.id.createdAtTime);
                        if (createdAtTime != null && messageListItem.getKeyString() != null && messageListItem.isTypeOutbox() && !messageListItem.isCall() && !messageListItem.getDelivered() && !messageListItem.isCustom() && !messageListItem.isChannelCustomMessage() && messageListItem.getScheduledAt() == null
                                && (!(channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || contact != null)) {
                            createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, support.isSupportNumber(getCurrentUserId()) ? deliveredIcon : sentIcon, null);
                        }
                    }
                }
            }
        });
    }

    public void updateDownloadStatus(final Message message) {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int index = messageList.indexOf(message);
                    if (index != -1) {
                        Message smListItem = messageList.get(index);
                        smListItem.setKeyString(message.getKeyString());
                        smListItem.setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                        if (messageList.get(index) != null) {
                            messageList.get(index).setKeyString(message.getKeyString());
                            messageList.get(index).setFileMetaKeyStrings(message.getFileMetaKeyStrings());
                        }
                        View view = recyclerView.getChildAt(index - linearLayoutManager.findFirstVisibleItemPosition());
                        if (view != null) {
                            final RelativeLayout attachmentDownloadProgressLayout = (RelativeLayout) view.findViewById(R.id.attachment_download_progress_layout);
                            final AttachmentView attachmentView = (AttachmentView) view.findViewById(R.id.main_attachment_view);
                            final ImageView preview = (ImageView) view.findViewById(R.id.preview);
                            TextView audioDurationTextView = (TextView) view.findViewById(R.id.audio_duration_textView);
                            final ImageView videoIcon = (ImageView) view.findViewById(R.id.video_icon);
                            String audioDuration;
                            if (message.getFileMetas() != null && message.getFileMetas().getContentType().contains("image")) {
                                attachmentView.setVisibility(VISIBLE);
                                preview.setVisibility(View.GONE);
                                attachmentView.setMessage(smListItem);
                                attachmentDownloadProgressLayout.setVisibility(View.GONE);
                            } else if (message.getFileMetas() != null && message.getFileMetas().getContentType().contains("video")) {
                                FileClientService fileClientService = new FileClientService(getContext());
                                attachedFile.setVisibility(View.GONE);
                                preview.setVisibility(VISIBLE);
                                videoIcon.setVisibility(VISIBLE);
                                preview.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(message.getFilePaths().get(0)));
                            } else if (message.getFileMetas() != null) {
                                //Hide Attachment View...
                                RelativeLayout applozicDocRelativeLayout = (RelativeLayout) view.findViewById(R.id.applozic_doc_downloaded);
                                ImageView imageViewDoc = (ImageView) applozicDocRelativeLayout.findViewById(R.id.doc_icon);
                                if (message.getFileMetas() != null && message.getFilePaths() == null) {
                                    if (message.getFileMetas().getContentType().contains("audio")) {
                                        imageViewDoc.setImageResource(R.drawable.ic_play_circle_outline);
                                    } else {
                                        imageViewDoc.setImageResource(R.drawable.ic_documentreceive);
                                    }
                                    applozicDocRelativeLayout.setVisibility(VISIBLE);
                                } else if (message.getFilePaths() != null) {
                                    String filePath = message.getFilePaths().get(0);
                                    final String mimeType = FileUtils.getMimeType(filePath);
                                    if (mimeType.contains("audio")) {
                                        if (message.isAttachmentDownloaded()) {
                                            audioDuration = ApplozicAudioManager.getInstance(getContext()).refreshAudioDuration(filePath);
                                            audioDurationTextView.setVisibility(View.VISIBLE);
                                            audioDurationTextView.setText(audioDuration);
                                        } else {
                                            audioDurationTextView.setVisibility(View.VISIBLE);
                                            audioDurationTextView.setText("00:00");
                                        }
                                        imageViewDoc.setImageResource(R.drawable.ic_play_circle_outline);
                                    } else {
                                        imageViewDoc.setImageResource(R.drawable.ic_documentreceive);
                                    }
                                    applozicDocRelativeLayout.setVisibility(VISIBLE);
                                }
                                view.findViewById(R.id.applozic_doc_download_progress_rl).setVisibility(View.GONE);
                            }
                        }

                    }
                } catch (Exception ex) {
                    Utils.printLog(getContext(), TAG, "Exception while updating download status: " + ex.getMessage());
                }
            }
        });
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    public void setConversationId(Integer conversationId) {
        this.currentConversationId = conversationId;
    }

    public void updateUserTypingStatus(final String typingUserId, final String isTypingStatus) {
        if (contact != null) {
            if (contact.isBlocked() || contact.isBlockedBy()) {
                return;
            }
        }
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isTypingStatus.equals("1")) {
                    if (channel != null) {
                        if (!loggedInUserId.equals(typingUserId)) {
                            Contact displayNameContact = appContactService.getContactById(typingUserId);
                            if (displayNameContact.isBlocked() || displayNameContact.isBlockedBy()) {
                                return;
                            }
                            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                                if (getActivity() != null) {
                                    setToolbarSubtitle(ApplozicService.getContext(getContext()).getString(R.string.is_typing));
                                    setToolbarImage(null, channel);
                                }
                            } else {
                                if (getActivity() != null) {
                                    setToolbarSubtitle(displayNameContact.getDisplayName() + " " + ApplozicService.getContext(getContext()).getString(R.string.is_typing));
                                    setToolbarImage(null, channel);
                                }
                            }
                        }
                    } else {
                        if (getActivity() != null) {
                            setToolbarSubtitle(ApplozicService.getContext(getContext()).getString(R.string.is_typing));
                            setToolbarImage(null, channel);
                        }
                    }
                } else {
                    if (channel != null) {
                        if (!loggedInUserId.equals(typingUserId)) {
                            Contact displayNameContact = appContactService.getContactById(typingUserId);
                            if (displayNameContact.isBlocked() || displayNameContact.isBlockedBy()) {
                                return;
                            }
                            updateChannelSubTitle(channel);
                        }
                    } else {
                        updateLastSeenStatus();
                    }

                }
            }
        });
    }

//    public void onEmojiconClicked(Emojicon emojicon) {
//        //TODO: Move OntextChangeListiner to EmojiEditableTExt
//        int currentPos = messageEditText.getSelectionStart();
//        messageEditText.setTextKeepState(messageEditText.getText().
//                insert(currentPos, emojicon.getEmoji()));
//    }


    @Override
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        return super.getLayoutInflater(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
    }

    //TODO: Please add onclick events here...  anonymous class are
// TODO :hard to read and suggested if we have very few event view
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.emoticons_btn) {
            if (emoticonsFrameLayout.getVisibility() == VISIBLE) {
                emoticonsFrameLayout.setVisibility(View.GONE);
                Utils.toggleSoftKeyBoard(getActivity(), false);
            } else {
                Utils.toggleSoftKeyBoard(getActivity(), true);
                emoticonsFrameLayout.setVisibility(VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (longPress) {
            count = 0;
            t.cancel();
            longPress = false;
            applozicAudioRecordManager.cancelAudio();
            audioRecordFrameLayout.setVisibility(View.GONE);
            mainEditTextLinearLayout.setVisibility(VISIBLE);
        }
        BroadcastService.currentUserId = null;
        BroadcastService.currentConversationId = null;
        if (typingStarted) {
            if (contact != null || (channel != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType()))) {
                Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
                intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
                intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
                intent.putExtra(ApplozicMqttIntentService.TYPING, false);
                ApplozicMqttIntentService.enqueueWork(getActivity(), intent);
            }
            typingStarted = false;
        }
        Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
        intent.putExtra(ApplozicMqttIntentService.UN_SUBSCRIBE_TO_TYPING, true);
        ApplozicMqttIntentService.enqueueWork(getActivity(), intent);
        if (recyclerDetailConversationAdapter != null) {
            recyclerDetailConversationAdapter.contactImageLoader.setPauseWork(false);
        }
    }

    public void updateTitle() {
        StringBuffer stringBufferTitle = new StringBuffer();
        if (contact != null) {
            stringBufferTitle.append(contact.getDisplayName());
        } else if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    stringBufferTitle.append(withUserContact.getDisplayName());
                }
            } else {
                stringBufferTitle.append(ChannelUtils.getChannelTitleName(channel, loggedInUserId));
            }
        }
        if (stringBufferTitle != null && getActivity() != null) {
            setToolbarTitle(stringBufferTitle.toString());
        }

    }

    public void loadConversation(Channel channel, Integer conversationId) {
        loadConversation(null, channel, conversationId, null);
    }

    public void loadConversation(Contact contact, Integer conversationId) {
        loadConversation(contact, null, conversationId, null);
    }

    //With search
    public void loadConversation(Contact contact, Integer conversationId, String searchString) {
        loadConversation(contact, null, conversationId, searchString);
    }

    public void loadConversation(Channel channel, Integer conversationId, String searchString) {
        loadConversation(null, channel, conversationId, searchString);
    }

    public void deleteConversationThread() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                setPositiveButton(R.string.delete_conversation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteConversationAsyncTask(new MobiComConversationService(getActivity()), contact, channel, currentConversationId, getActivity()).execute();
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setTitle(ApplozicService.getContext(getContext()).getString(R.string.dialog_delete_conversation_title).replace("[name]", getNameForInviteDialog()));
        alertDialog.setMessage(ApplozicService.getContext(getContext()).getString(R.string.dialog_delete_conversation_confir).replace("[name]", getNameForInviteDialog()));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = positionInSmsList;
        if (messageList.size() <= position) {
            return true;
        }
        Message message = messageList.get(position);
        if (message.isTempDateType() || message.isCustom()) {
            return true;
        }

        switch (item.getItemId()) {
            case 0:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) ApplozicService.getContext(getContext()).getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(message.getMessage());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ApplozicService.getContext(getContext()).getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(ApplozicService.getContext(getContext()).getString(R.string.copied_message), message.getMessage());
                    clipboard.setPrimaryClip(clip);
                }
                break;
            case 1:
                conversationUIService.startContactActivityForResult(message, null);
                break;
            case 2:
                Message messageToResend = new Message(message);
                //messageToResend.setCreatedAtTime(new Date().getTime());
                messageToResend.setCreatedAtTime(System.currentTimeMillis() + MobiComUserPreference.getInstance(getActivity()).getDeviceTimeOffset());
                conversationService.sendMessage(messageToResend, messageIntentClass);
                break;
            case 3:
                String messageKeyString = message.getKeyString();
                new DeleteConversationAsyncTask(conversationService, message, contact).execute();
                deleteMessageFromDeviceList(messageKeyString);
                break;
            case 4:
                String messageJson = GsonUtils.getJsonFromObject(message, Message.class);
                conversationUIService.startMessageInfoFragment(messageJson);
                break;
            case 5:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                if (message.getFilePaths() != null) {
                    Uri shareUri = null;
                    if (Utils.hasNougat()) {
                        shareUri = FileProvider.getUriForFile(ApplozicService.getContext(getContext()), Utils.getMetaDataValue(ApplozicService.getContext(getContext()), MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                    } else {
                        shareUri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                    }
                    shareIntent.setDataAndType(shareUri, "text/x-vcard");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                    if (!TextUtils.isEmpty(message.getMessage())) {
                        shareIntent.putExtra(Intent.EXTRA_TEXT, message.getMessage());
                    }
                    shareIntent.setType(FileUtils.getMimeType(new File(message.getFilePaths().get(0))));
                } else {
                    shareIntent.putExtra(Intent.EXTRA_TEXT, message.getMessage());
                    shareIntent.setType("text/plain");
                }
                startActivity(Intent.createChooser(shareIntent, ApplozicService.getContext(getContext()).getString(R.string.send_message_to)));
                break;

            case 6:
                try {
                    Configuration config = null;
                    if (getActivity() != null) {
                        config = getActivity().getResources().getConfiguration();
                    }
                    messageMetaData = new HashMap<>();
                    String displayName;
                    if (message.getGroupId() != null) {
                        if (loggedInUserId.equals(message.getContactIds()) || TextUtils.isEmpty(message.getContactIds())) {
                            displayName = ApplozicService.getContext(getContext()).getString(R.string.you_string);
                        } else {
                            displayName = appContactService.getContactById(message.getContactIds()).getDisplayName();
                        }
                    } else {
                        if (message.isTypeOutbox()) {
                            displayName = ApplozicService.getContext(getContext()).getString(R.string.you_string);
                        } else {
                            displayName = appContactService.getContactById(message.getContactIds()).getDisplayName();
                        }
                    }
                    nameTextView.setText(displayName);
                    if (message.hasAttachment()) {
                        FileMeta fileMeta = message.getFileMetas();
                        imageViewForAttachmentType.setVisibility(VISIBLE);
                        if (fileMeta.getContentType().contains("image")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_image_camera_alt);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.photo_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(VISIBLE);
                            imageViewRLayout.setVisibility(VISIBLE);
                            imageThumbnailLoader.loadImage(message, galleryImageView);
                        } else if (fileMeta.getContentType().contains("video")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_video);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                if (config != null && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                                    imageViewForAttachmentType.setScaleX(-1);
                                }
                            }
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.video_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            if (message.getFilePaths() != null && message.getFilePaths().size() > 0) {
                                if (imageCache.getBitmapFromMemCache(message.getKeyString()) != null) {
                                    galleryImageView.setImageBitmap(imageCache.getBitmapFromMemCache(message.getKeyString()));
                                } else {
                                    imageCache.addBitmapToCache(message.getKeyString(), fileClientService.createAndSaveVideoThumbnail(message.getFilePaths().get(0)));
                                    galleryImageView.setImageBitmap(fileClientService.createAndSaveVideoThumbnail(message.getFilePaths().get(0)));
                                }
                            }
                            galleryImageView.setVisibility(VISIBLE);
                            imageViewRLayout.setVisibility(VISIBLE);
                        } else if (fileMeta.getContentType().contains("audio")) {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_music_note);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.audio_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else if (message.isContactMessage()) {
                            MobiComVCFParser parser = new MobiComVCFParser();
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                            try {
                                VCFContactData data = parser.parseCVFContactData(message.getFilePaths().get(0));
                                if (data != null) {
                                    messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.contact_string));
                                    messageTextView.append(" " + data.getName());
                                }
                            } catch (Exception e) {
                                imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.contact_string));
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_attachment);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.attachment_string));
                            } else {
                                messageTextView.setText(message.getMessage());
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        }
                        imageViewForAttachmentType.setColorFilter(ContextCompat.getColor(getActivity(), R.color.apploizc_lite_gray_color));
                    } else if (message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                        imageViewForAttachmentType.setVisibility(VISIBLE);
                        galleryImageView.setVisibility(VISIBLE);
                        imageViewRLayout.setVisibility(VISIBLE);
                        messageTextView.setText(ApplozicService.getContext(getContext()).getString(R.string.al_location_string));
                        imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_location_on_white_24dp);
                        imageViewForAttachmentType.setColorFilter(ContextCompat.getColor(getActivity(), R.color.apploizc_lite_gray_color));
                        messageImageLoader.setLoadingImage(R.drawable.applozic_map_offline_thumbnail);
                        messageImageLoader.loadImage(LocationUtils.loadStaticMap(message.getMessage(), geoApiKey), galleryImageView);
                    } else {
                        imageViewForAttachmentType.setVisibility(View.GONE);
                        imageViewRLayout.setVisibility(View.GONE);
                        galleryImageView.setVisibility(View.GONE);
                        messageTextView.setText(message.getMessage());
                    }
                    messageMetaData.put(Message.MetaDataType.AL_REPLY.getValue(), message.getKeyString());
                    if (messageMetaData != null && !messageMetaData.isEmpty()) {
                        String replyMessageKey = messageMetaData.get(Message.MetaDataType.AL_REPLY.getValue());
                        if (!TextUtils.isEmpty(replyMessageKey)) {
                            messageDatabaseService.updateMessageReplyType(replyMessageKey, Message.ReplyMessage.REPLY_MESSAGE.getValue());
                        }
                    }
                    attachReplyCancelLayout.setVisibility(VISIBLE);
                    replayRelativeLayout.setVisibility(VISIBLE);
                } catch (Exception e) {

                }
                break;

        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MobiComUserPreference.getInstance(getActivity()).isChannelDeleted()) {
            MobiComUserPreference.getInstance(getActivity()).setDeleteChannel(false);
            if (getActivity().getSupportFragmentManager() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return;
        }
        ((ConversationActivity) getActivity()).setChildFragmentLayoutBGToTransparent();
        if (contact != null || channel != null) {
            BroadcastService.currentUserId = contact != null ? contact.getContactIds() : String.valueOf(channel.getKey());
            BroadcastService.currentConversationId = currentConversationId;
            if (BroadcastService.currentUserId != null) {
                NotificationManagerCompat nMgr = NotificationManagerCompat.from(getActivity());
                if (ApplozicClient.getInstance(getActivity()).isNotificationStacking()) {
                    nMgr.cancel(NotificationService.NOTIFICATION_ID);
                } else {
                    if (contact != null) {
                        if (!TextUtils.isEmpty(contact.getContactIds())) {
                            nMgr.cancel(contact.getContactIds().hashCode());
                        }
                    }
                    if (channel != null) {
                        nMgr.cancel(String.valueOf(channel.getKey()).hashCode());
                    }
                }
            }

            Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
            intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
            intent.putExtra(ApplozicMqttIntentService.SUBSCRIBE_TO_TYPING, true);
            ApplozicMqttIntentService.enqueueWork(getActivity(), intent);

            if (downloadConversation != null) {
                downloadConversation.cancel(true);
            }

            new AsyncTask<Void, Object, Object>() {
                @Override
                protected Object doInBackground(Void... voids) {
                    if (channel != null) {
                        boolean present = ChannelService.getInstance(getActivity()).processIsUserPresentInChannel(channel.getKey());
                        Channel newChannel = ChannelService.getInstance(getActivity()).getChannelByChannelKey(channel.getKey());
                        if (!present) {
                            if (newChannel != null && newChannel.getType() != null && Channel.GroupType.OPEN.getValue().equals(newChannel.getType())) {
                                MobiComUserPreference.getInstance(getActivity()).setNewMessageFlag(true);
                            }
                        }
                        updateChannelTitle(newChannel);

                        if (newChannel.getType() != null && !Channel.GroupType.OPEN.getValue().equals(newChannel.getType())) {
                            hideSendMessageLayout(newChannel.isDeleted() || !present);
                        } else {
                            hideSendMessageLayout(newChannel.isDeleted());
                        }

                        if (ChannelService.isUpdateTitle) {
                            updateChannelSubTitle(newChannel);
                            ChannelService.isUpdateTitle = false;
                        }
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    if (appContactService != null && contact != null) {
                        updateLastSeenStatus();
                    }
                }
            }.execute();

            if (messageList.isEmpty()) {
                loadConversation(contact, channel, currentConversationId, null);
            } else if (MobiComUserPreference.getInstance(getContext()).getNewMessageFlag()) {
                loadnewMessageOnResume(contact, channel, currentConversationId);
            }
            MobiComUserPreference.getInstance(getContext()).setNewMessageFlag(false);

            if (SyncCallService.refreshView) {
                SyncCallService.refreshView = false;
            }

        }
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                downloadConversation = new DownloadConversation(recyclerView, false, 1, 1, 1, contact, channel, currentConversationId);
                downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

    }

    private void hideSendMessageLayout(final boolean hide) {
        if (getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (hide) {
                    individualMessageSendLayout.setVisibility(View.GONE);
                    userNotAbleToChatLayout.setVisibility(VISIBLE);
                } else {
                    userNotAbleToChatLayout.setVisibility(View.GONE);

                }
            }
        });

    }

    public void updateChannelTitleAndSubTitle() {
        if (channel != null) {
            Channel channelInfo = ChannelService.getInstance(getActivity()).getChannelInfo(channel.getKey());

            if (channelInfo.isDeleted()) {
                channel.setDeletedAtTime(channelInfo.getDeletedAtTime());
                individualMessageSendLayout.setVisibility(View.GONE);
                userNotAbleToChatLayout.setVisibility(VISIBLE);
                userNotAbleToChatTextView.setText(R.string.group_has_been_deleted_text);
                if (channel != null && !ChannelService.getInstance(getContext()).isUserAlreadyPresentInChannel(channel.getKey(), loggedInUserId)
                        && messageTemplate != null && messageTemplate.isEnabled() && templateAdapter != null) {
                    templateAdapter.removeTemplates();
                }
            } else {
                if ((!ChannelService.getInstance(getActivity()).processIsUserPresentInChannel(channel.getKey())
                        && userNotAbleToChatLayout != null
                        && !Channel.GroupType.OPEN.getValue().equals(channel.getType()))) {

                    individualMessageSendLayout.setVisibility(View.GONE);
                    userNotAbleToChatLayout.setVisibility(VISIBLE);
                    if (messageTemplate != null && messageTemplate.isEnabled() && templateAdapter != null) {
                        templateAdapter.removeTemplates();
                    }
                } else if (ChannelService.getInstance(getActivity()).processIsUserPresentInChannel(channel.getKey())
                        && userNotAbleToChatLayout != null
                        && !Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                    individualMessageSendLayout.setVisibility(View.VISIBLE);
                    userNotAbleToChatLayout.setVisibility(View.GONE);
                }
            }

            updateChannelTitle(channelInfo);
            updateChannelSubTitle(channelInfo);
        }
    }

    public void updateContextBasedGroup() {
        if (channel != null) {
            Channel channelInfo = ChannelService.getInstance(getActivity()).getChannelInfo(channel.getKey());

            if ((Channel.GroupType.GROUPOFTWO.getValue().equals(channelInfo.getType())) && channel.isContextBasedChat()) {
                Conversation conversation = new Conversation();
                TopicDetail topic = new TopicDetail();
                topic.setTitle(channelInfo.getMetadata().get(Channel.GroupMetaDataType.TITLE.getValue()));
                topic.setSubtitle(channelInfo.getMetadata().get(Channel.GroupMetaDataType.PRICE.getValue()));
                topic.setLink(channelInfo.getMetadata().get(Channel.GroupMetaDataType.LINK.getValue()));
                conversation.setTopicDetail(topic.getJson());
                conversationList.get(0).setTopicDetail(topic.getJson());
                applozicContextSpinnerAdapter.notifyDataSetChanged();
            }
        }
    }

    public void updateChannelTitle(Channel newChannel) {
        if (!Channel.GroupType.GROUPOFTWO.getValue().equals(newChannel.getType())) {
            if (newChannel != null) {
                title = ChannelUtils.getChannelTitleName(newChannel, loggedInUserId);
                channel = newChannel;
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setToolbarTitle(title);
                    }
                });
            }
        }
    }

    public void updateTitleForOpenGroup() {
        try {
            if (channel != null) {
                Channel newChannel = ChannelService.getInstance(getActivity()).getChannelByChannelKey(channel.getKey());
                if (getActivity() != null) {
                    setToolbarTitle(newChannel.getName());
                }
            }
            updateChannelSubTitle(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void selfDestructMessage(Message message) {
        if (Message.MessageType.MT_INBOX.getValue().equals(message.getType()) &&
                message.getTimeToLive() != null && message.getTimeToLive() != 0) {
            new Timer().schedule(new DisappearingMessageTask(getActivity(), conversationService, message), message.getTimeToLive() * 60 * 1000);
        }
    }

    public void loadnewMessageOnResume(Contact contact, Channel channel, Integer conversationId) {
        downloadConversation = new DownloadConversation(recyclerView, true, 1, 0, 0, contact, channel, conversationId);
        downloadConversation.execute();
    }

    public int scrollToFirstSearchIndex() {

        int position = 0;
        if (searchString != null) {
            for (position = messageList.size() - 1; position >= 0; position--) {
                Message message = messageList.get(position);
                if (!TextUtils.isEmpty(message.getMessage()) && message.getMessage().toLowerCase(Locale.getDefault()).indexOf(
                        searchString.toString().toLowerCase(Locale.getDefault())) != -1) {
                    return position;
                }
            }
        } else {
            position = messageList.size();
        }
        return position;
    }

    public void blockUserProcess(final String userId, final boolean block, final boolean isFromChannel) {

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "",
                ApplozicService.getContext(getContext()).getString(R.string.please_wait_info), true);

        UserBlockTask.TaskListener listener = new UserBlockTask.TaskListener() {

            @Override
            public void onSuccess(ApiResponse apiResponse) {
                if (block && typingStarted) {
                    if (getActivity() != null) {
                        setToolbarSubtitle("");
                    }
                    Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
                    intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
                    intent.putExtra(ApplozicMqttIntentService.STOP_TYPING, true);
                    ApplozicMqttIntentService.enqueueWork(getActivity(), intent);

                }
                menu.findItem(R.id.userBlock).setVisible(!block);
                menu.findItem(R.id.userUnBlock).setVisible(block);
            }

            @Override
            public void onFailure(ApiResponse apiResponse, Exception exception) {
                String error = getString(Utils.isInternetAvailable(getActivity()) ? R.string.applozic_server_error : R.string.you_need_network_access_for_block_or_unblock);
                Toast toast = Toast.makeText(getActivity(), error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onCompletion() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (!isFromChannel) {
                    contact = appContactService.getContactById(userId);
                }
            }

        };

        new UserBlockTask(getActivity(), listener, userId, block).execute((Void) null);
    }

    public void userBlockDialog(final boolean block, final Contact withUserContact, final boolean isFromChannel) {
        if (withUserContact == null) {
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        blockUserProcess(withUserContact.getUserId(), block, isFromChannel);
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        String name = withUserContact.getDisplayName();
        alertDialog.setMessage(getString(block ? R.string.user_block_info : R.string.user_un_block_info).replace("[name]", name));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void muteGroupChat() {

        final CharSequence[] items = {ApplozicService.getContext(getContext()).getString(R.string.eight_Hours), ApplozicService.getContext(getContext()).getString(R.string.one_week), ApplozicService.getContext(getContext()).getString(R.string.one_year)};
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        millisecond = date.getTime();

        final MuteNotificationAsync.TaskListener taskListener = new MuteNotificationAsync.TaskListener() {
            @Override
            public void onSuccess(ApiResponse apiResponse) {
                if (menu != null) {
                    menu.findItem(R.id.muteGroup).setVisible(false);
                    menu.findItem(R.id.unmuteGroup).setVisible(true);
                }
            }

            @Override
            public void onFailure(ApiResponse apiResponse, Exception exception) {

            }

            @Override
            public void onCompletion() {

            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(ApplozicService.getContext(getContext()).getString(R.string.mute_group_for))
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, final int selectedItem) {
                        if (selectedItem == 0) {
                            millisecond = millisecond + 28800000;
                        } else if (selectedItem == 1) {
                            millisecond = millisecond + 604800000;

                        } else if (selectedItem == 2) {
                            millisecond = millisecond + 31558000000L;
                        }

                        muteNotificationRequest = new MuteNotificationRequest(channel.getKey(), millisecond);
                        MuteNotificationAsync muteNotificationAsync = new MuteNotificationAsync(getContext(), taskListener, muteNotificationRequest);
                        muteNotificationAsync.execute((Void) null);
                        dialog.dismiss();

                    }
                });
        AlertDialog alertdialog = builder.create();
        alertdialog.show();
    }

    public void umuteGroupChat() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        millisecond = date.getTime();

        final MuteNotificationAsync.TaskListener taskListener = new MuteNotificationAsync.TaskListener() {
            @Override
            public void onSuccess(ApiResponse apiResponse) {
                if (menu != null) {
                    menu.findItem(R.id.unmuteGroup).setVisible(false);
                    menu.findItem(R.id.muteGroup).setVisible(true);
                }
            }

            @Override
            public void onFailure(ApiResponse apiResponse, Exception exception) {

            }

            @Override
            public void onCompletion() {

            }
        };
        muteNotificationRequest = new MuteNotificationRequest(channel.getKey(), millisecond);
        MuteNotificationAsync muteNotificationAsync = new MuteNotificationAsync(getContext(), taskListener, muteNotificationRequest);
        muteNotificationAsync.execute((Void) null);
    }

    public void muteUserChat() {
        final CharSequence[] items = {ApplozicService.getContext(getContext()).getString(R.string.eight_Hours), ApplozicService.getContext(getContext()).getString(R.string.one_week), ApplozicService.getContext(getContext()).getString(R.string.one_year)};
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        millisecond = date.getTime();

        final MuteUserNotificationAsync.TaskListener listener = new MuteUserNotificationAsync.TaskListener() {

            @Override
            public void onSuccess(String status, Context context) {
                if (menu != null) {
                    menu.findItem(R.id.muteGroup).setVisible(false);
                    menu.findItem(R.id.unmuteGroup).setVisible(true);
                }
            }

            @Override
            public void onFailure(String error, Context context) {

            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(ApplozicService.getContext(getContext()).getString(R.string.mute_user_for))
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, final int selectedItem) {
                        if (selectedItem == 0) {
                            millisecond = millisecond + 28800000;
                        } else if (selectedItem == 1) {
                            millisecond = millisecond + 604800000;
                        } else if (selectedItem == 2) {
                            millisecond = millisecond + 31558000000L;
                        }

                        new MuteUserNotificationAsync(listener, millisecond, contact.getUserId(), getContext()).execute();
                        dialog.dismiss();

                    }
                });
        AlertDialog alertdialog = builder.create();
        alertdialog.show();
    }

    public void unMuteUserChat() {
        Date date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
        millisecond = date.getTime();

        final MuteUserNotificationAsync.TaskListener taskListener = new MuteUserNotificationAsync.TaskListener() {

            @Override
            public void onSuccess(String status, Context context) {
                if (menu != null) {
                    menu.findItem(R.id.unmuteGroup).setVisible(false);
                    menu.findItem(R.id.muteGroup).setVisible(true);
                }
            }

            @Override
            public void onFailure(String error, Context context) {

            }
        };
        new MuteUserNotificationAsync(taskListener, millisecond, contact.getUserId(), getContext()).execute();
    }

    public void muteUser(boolean mute) {
        if (menu != null) {
            menu.findItem(R.id.unmuteGroup).setVisible(mute);
            menu.findItem(R.id.muteGroup).setVisible(!mute);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        count = 0;
        t.cancel();
        ((ConversationActivity) getActivity()).setChildFragmentLayoutBG();
        ApplozicAudioManager.getInstance(getContext()).audiostop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null) {
            ((CustomToolbarListener) getActivity()).hideSubtitleAndProfilePic();
        }
    }

    public ViewGroup.LayoutParams getImageLayoutParam(boolean outBoxType) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getActivity().getResources().getDisplayMetrics());
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

    public void createTemplateMessages() {
        if (templateAdapter == null) {
            return;
        }

        if (!messageList.isEmpty()) {
            Message lastMessage = messageList.get(messageList.size() - 1);

            if (lastMessage.getMetadata().containsKey("isDoneWithClicking")) {
                return;
            }

            if (lastMessage.getMetadata() != null && lastMessage.getMetadata().containsKey(MobiComKitConstants.TEMPLATE_MESSAGE_LIST)) {
                Map<String, String> messageArray = (Map<String, String>) GsonUtils.getObjectFromJson(lastMessage.getMetadata().get(MobiComKitConstants.TEMPLATE_MESSAGE_LIST), Map.class);
                templateAdapter.setMessageList(messageArray);
                templateAdapter.notifyDataSetChanged();
                //createMessageTemplate(Arrays.asList(messageArray));
            } else {
                String type = getMessageType(lastMessage);
                if ("audio".equals(type)) {
                    if (messageTemplate.getAudioMessageList() != null) {
                        if ((lastMessage.isTypeOutbox() && messageTemplate.getAudioMessageList().isShowOnSenderSide()) ||
                                messageTemplate.getAudioMessageList().isShowOnReceiverSide()) {
                            templateAdapter.setMessageList(messageTemplate.getAudioMessageList().getMessageList());
                            templateAdapter.notifyDataSetChanged();
                        }
                    }
                } else if ("video".equals(type)) {
                    if (messageTemplate.getVideoMessageList() != null) {
                        if ((lastMessage.isTypeOutbox() && messageTemplate.getVideoMessageList().isShowOnSenderSide()) ||
                                messageTemplate.getVideoMessageList().isShowOnReceiverSide()) {
                            templateAdapter.setMessageList(messageTemplate.getVideoMessageList().getMessageList());
                            templateAdapter.notifyDataSetChanged();
                        }
                    }
                } else if ("image".equals(type)) {
                    if (messageTemplate.getImageMessageList() != null) {
                        if ((lastMessage.isTypeOutbox() && messageTemplate.getImageMessageList().isShowOnSenderSide()) ||
                                messageTemplate.getImageMessageList().isShowOnReceiverSide()) {
                            templateAdapter.setMessageList(messageTemplate.getImageMessageList().getMessageList());
                            templateAdapter.notifyDataSetChanged();
                        }
                    }
                } else if (lastMessage.getContentType() == Message.ContentType.LOCATION.getValue()) {
                    if (messageTemplate.getLocationMessageList() != null) {
                        if ((lastMessage.isTypeOutbox() && messageTemplate.getLocationMessageList().isShowOnSenderSide()) ||
                                messageTemplate.getLocationMessageList().isShowOnReceiverSide()) {
                            templateAdapter.setMessageList(messageTemplate.getLocationMessageList().getMessageList());
                            templateAdapter.notifyDataSetChanged();
                        }
                    }
                } else if (lastMessage.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
                    if (messageTemplate.getContactMessageList() != null) {
                        if ((lastMessage.isTypeOutbox() && messageTemplate.getContactMessageList().isShowOnSenderSide()) ||
                                messageTemplate.getContactMessageList().isShowOnReceiverSide()) {
                            templateAdapter.setMessageList(messageTemplate.getContactMessageList().getMessageList());
                            templateAdapter.notifyDataSetChanged();
                        }
                    }
                } else if ("text".equals(type)) {
                    if (messageTemplate.getTextMessageList() != null) {
                        if ((lastMessage.isTypeOutbox() && messageTemplate.getTextMessageList().isShowOnSenderSide()) ||
                                messageTemplate.getTextMessageList().isShowOnReceiverSide()) {
                            templateAdapter.setMessageList(messageTemplate.getTextMessageList().getMessageList());
                            templateAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    public String getMessageType(Message lastMessage) {
        String type = null;

        if (lastMessage == null) {
            return null;
        }

        if (lastMessage.getContentType() == Message.ContentType.LOCATION.getValue()) {
            type = "location";
        } else if (lastMessage.getContentType() == Message.ContentType.AUDIO_MSG.getValue()) {
            type = "audio";
        } else if (lastMessage.getContentType() == Message.ContentType.VIDEO_MSG.getValue()) {
            type = "video";
        } else if (lastMessage.getContentType() == Message.ContentType.ATTACHMENT.getValue()) {
            if (lastMessage.getFilePaths() != null) {
                String filePath = lastMessage.getFilePaths().get(lastMessage.getFilePaths().size() - 1);
                String mimeType = FileUtils.getMimeType(filePath);

                if (mimeType != null) {
                    if (mimeType.startsWith("image")) {
                        type = "image";
                    } else if (mimeType.startsWith("audio")) {
                        type = "audio";
                    } else if (mimeType.startsWith("video")) {
                        type = "video";
                    }
                }
            } else if (lastMessage.getFileMetas() != null) {
                if (lastMessage.getFileMetas().getContentType().contains("image")) {
                    type = "image";
                } else if (lastMessage.getFileMetas().getContentType().contains("audio")) {
                    type = "audio";
                } else if (lastMessage.getFileMetas().getContentType().contains("video")) {
                    type = "video";
                }
            }
        } else if (lastMessage.getContentType() == Message.ContentType.CONTACT_MSG.getValue()) {
            type = "contact";
        } else {
            type = "text";
        }
        return type;
    }

    public class DownloadConversation extends AsyncTask<Void, Integer, Long> {

        //private AbsListView view;
        private RecyclerView recyclerView;
        //private QuickConversationAdapter recyclerAdapter;
        private int firstVisibleItem;
        private int amountVisible;
        private int totalItems;
        private boolean initial;
        private Contact contact;
        private Channel channel;
        private Integer conversationId;
        private List<Message> nextMessageList = new ArrayList<Message>();

        private WeakReference<TextView> emptyTextViewWeakReference;
        private WeakReference<ImageButton> sendButtonWeakReference;
        private WeakReference<EditText> messageEditTextWeakReference;
        private WeakReference<SwipeRefreshLayout> swipeLayoutWeakReference;
        private WeakReference<LinearLayoutManager> layoutManagerWeakReference;
        private WeakReference<Spinner> contextSpinnerWeakReference;
        private WeakReference<FrameLayout> contextFrameLayoutWeakReference;

        private void setWeakReferences() {
            emptyTextViewWeakReference = new WeakReference<TextView>(emptyTextView);
            sendButtonWeakReference = new WeakReference<ImageButton>(sendButton);
            messageEditTextWeakReference = new WeakReference<EditText>(messageEditText);
            swipeLayoutWeakReference = new WeakReference<SwipeRefreshLayout>(swipeLayout);
            layoutManagerWeakReference = new WeakReference<LinearLayoutManager>(linearLayoutManager);
            contextSpinnerWeakReference = new WeakReference<Spinner>(contextSpinner);
            contextFrameLayoutWeakReference = new WeakReference<FrameLayout>(contextFrameLayout);
        }

        private TextView emptyTextView() {
            if (emptyTextViewWeakReference != null) {
                return emptyTextViewWeakReference.get();
            }
            return null;
        }

        private ImageButton sendButton() {
            if (sendButtonWeakReference != null) {
                return sendButtonWeakReference.get();
            }
            return null;
        }

        private EditText messageEditText() {
            if (messageEditTextWeakReference != null) {
                return messageEditTextWeakReference.get();
            }
            return null;
        }

        private SwipeRefreshLayout swipeLayout() {
            if (swipeLayoutWeakReference != null) {
                return swipeLayoutWeakReference.get();
            }
            return null;
        }

        private LinearLayoutManager linearLayoutManager() {
            if (layoutManagerWeakReference != null) {
                return layoutManagerWeakReference.get();
            }
            return null;
        }

        private Spinner contextSpinner() {
            if (contextSpinnerWeakReference != null) {
                return contextSpinnerWeakReference.get();
            }
            return null;
        }

        private FrameLayout contextFrameLayout() {
            if (contextFrameLayoutWeakReference != null) {
                return contextFrameLayoutWeakReference.get();
            }
            return null;
        }

        public DownloadConversation(RecyclerView recyclerView, boolean initial, int firstVisibleItem, int amountVisible, int totalItems, Contact contact, Channel channel, Integer conversationId) {
            this.recyclerView = recyclerView;
            this.initial = initial;
            this.firstVisibleItem = firstVisibleItem;
            this.amountVisible = amountVisible;
            this.totalItems = totalItems;
            this.contact = contact;
            this.channel = channel;
            this.conversationId = conversationId;
            setWeakReferences();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (emptyTextView() != null) {
                emptyTextView().setVisibility(View.GONE);
            }
            isAlreadyLoading = true;
            if (swipeLayout() != null) {
                swipeLayout().post(new Runnable() {
                    @Override
                    public void run() {
                        if (swipeLayout() != null) {
                            swipeLayout().setRefreshing(true);
                        }
                    }
                });
            }
            if (initial) {
                if (recordButtonWeakReference != null) {
                    ImageButton recordButton = recordButtonWeakReference.get();
                    if (recordButton != null) {
                        recordButton.setEnabled(false);
                    }
                }
                if (sendButton() != null) {
                    sendButton().setEnabled(false);
                }
                if (messageEditText() != null) {
                    messageEditText().setEnabled(false);
                }
            }

            if (!initial && messageList.isEmpty()) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity()).
                        setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        loadMore = false;
                    }
                });
                //Todo: Move this to mobitexter app
                alertDialog.setTitle(R.string.sync_older_messages);
                alertDialog.setCancelable(true);
                alertDialog.create().show();
            }
        }

        @Override
        protected Long doInBackground(Void... voids) {
            try {
                if (initial) {
                    Long lastConversationloadTime = 1L;
                    if (!messageList.isEmpty()) {
                        for (int i = messageList.size() - 1; i >= 0; i--) {
                            if (messageList.get(i).isTempDateType()) {
                                continue;
                            }
                            lastConversationloadTime = messageList.get(i).getCreatedAtTime();
                            break;
                        }
                    }


                    nextMessageList = conversationService.getMessages(lastConversationloadTime + 1L, null, contact, channel, conversationId);
                } else if (firstVisibleItem == 1 && loadMore && !messageList.isEmpty()) {
                    loadMore = false;
                    Long endTime = null;
                    for (Message message : messageList) {
                        if (message.isTempDateType()) {
                            continue;
                        }
                        endTime = messageList.get(0).getCreatedAtTime();
                        break;
                    }
                    nextMessageList = conversationService.getMessages(null, endTime, contact, channel, conversationId);
                }
                if (BroadcastService.isContextBasedChatEnabled()) {
                    conversations = ConversationService.getInstance(getActivity()).getConversationList(channel, contact);
                }

                List<Message> createAtMessage = new ArrayList<Message>();
                if (nextMessageList != null && !nextMessageList.isEmpty()) {
                    Message firstDateMessage = new Message();
                    firstDateMessage.setTempDateType(Short.valueOf("100"));
                    firstDateMessage.setCreatedAtTime(nextMessageList.get(0).getCreatedAtTime());

                    if (initial && !messageList.contains(firstDateMessage)) {
                        createAtMessage.add(firstDateMessage);
                    } else if (!initial) {
                        createAtMessage.add(firstDateMessage);
                        messageList.remove(firstDateMessage);
                    }
                    if (!createAtMessage.contains(nextMessageList.get(0))) {
                        createAtMessage.add(nextMessageList.get(0));
                    }

                    for (int i = 1; i <= nextMessageList.size() - 1; i++) {
                        long dayDifference = DateUtils.daysBetween(new Date(nextMessageList.get(i - 1).getCreatedAtTime()), new Date(nextMessageList.get(i).getCreatedAtTime()));

                        if (dayDifference >= 1) {
                            Message message = new Message();
                            message.setTempDateType(Short.valueOf("100"));
                            message.setCreatedAtTime(nextMessageList.get(i).getCreatedAtTime());
                            if (initial && !messageList.contains(message)) {
                                createAtMessage.add(message);
                            } else if (!initial) {
                                createAtMessage.add(message);
                                messageList.remove(message);
                            }
                        }
                        if (!createAtMessage.contains(nextMessageList.get(i))) {
                            createAtMessage.add(nextMessageList.get(i));
                        }
                    }
                }
                nextMessageList = createAtMessage;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return 0L;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            //TODO: FIX ME
            try {
                if (swipeLayout() != null) {
                    swipeLayout().post(new Runnable() {
                        @Override
                        public void run() {
                            if (swipeLayout() != null) {
                                swipeLayout().setRefreshing(true);
                            }
                        }
                    });
                }
                if (nextMessageList.isEmpty()) {
                    if (linearLayoutManager() != null) {
                        linearLayoutManager().setStackFromEnd(true);
                    }
                }
                //Note: This is done to avoid duplicates with same timestamp entries
                if (!messageList.isEmpty() && !nextMessageList.isEmpty() &&
                        messageList.get(0).equals(nextMessageList.get(nextMessageList.size() - 1))) {
                    nextMessageList.remove(nextMessageList.size() - 1);
                }

                if (!messageList.isEmpty() && !nextMessageList.isEmpty() &&
                        messageList.get(0).getCreatedAtTime().equals(nextMessageList.get(nextMessageList.size() - 1).getCreatedAtTime())) {
                    nextMessageList.remove(nextMessageList.size() - 1);
                }

                for (Message message : nextMessageList) {
                    selfDestructMessage(message);
                }

                if (initial) {
                    messageList.addAll(nextMessageList);
                    recyclerDetailConversationAdapter.searchString = searchString;
                    if (emptyTextView() != null) {
                        emptyTextView().setVisibility(messageList.isEmpty() ? VISIBLE : View.GONE);
                    }
                    if (!messageList.isEmpty()) {
                        recyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!TextUtils.isEmpty(searchString)) {
                                    if (linearLayoutManager() != null) {
                                        linearLayoutManager().scrollToPositionWithOffset(scrollToFirstSearchIndex(), 0);
                                    }
                                } else {
                                    if (linearLayoutManager() != null) {
                                        linearLayoutManager().scrollToPositionWithOffset(messageList.size() - 1, 0);
                                    }
                                }
                            }
                        });
                    }
                } else if (!nextMessageList.isEmpty()) {
                    if (linearLayoutManager() != null) {
                        linearLayoutManager().setStackFromEnd(true);
                    }
                    messageList.addAll(0, nextMessageList);
                    if (linearLayoutManager() != null) {
                        linearLayoutManager().scrollToPosition(nextMessageList.size() - 1);
                    }
                }

                conversationService.read(contact, channel);

                if (!messageList.isEmpty()) {
                    for (int i = messageList.size() - 1; i >= 0; i--) {
                        Message message = messageList.get(i);
                        if (!message.isRead() && !message.isTempDateType() && !message.isCustom()) {
                            if (message.getMessageId() != null) {
                                message.setRead(Boolean.TRUE);
                                messageDatabaseService.updateMessageReadFlag(message.getMessageId(), true);
                            }
                        } else {
                            break;
                        }
                    }
                }

                if (conversations != null && conversations.size() > 0) {
                    conversationList = conversations;
                }
                if (channel != null && channel.getMetadata() != null && !channel.getMetadata().isEmpty()) {
                    if (channel.isContextBasedChat()) {
                        Conversation conversation = new Conversation();
                        TopicDetail topic = new TopicDetail();
                        topic.setTitle(channel.getMetadata().get(Channel.GroupMetaDataType.TITLE.getValue()));
                        topic.setSubtitle(channel.getMetadata().get(Channel.GroupMetaDataType.PRICE.getValue()));
                        topic.setLink(channel.getMetadata().get(Channel.GroupMetaDataType.LINK.getValue()));
                        conversation.setTopicDetail(topic.getJson());
                        conversationList = new ArrayList<>();
                        conversationList.add(conversation);
                    }
                }
                if (conversationList != null && conversationList.size() > 0 && !onSelected) {
                    onSelected = true;
                    applozicContextSpinnerAdapter = new ApplozicContextSpinnerAdapter(getActivity(), conversationList);
                    if (applozicContextSpinnerAdapter != null) {
                        if (contextSpinner() != null) {
                            contextSpinner().setAdapter(applozicContextSpinnerAdapter);
                        }
                        if (contextFrameLayout() != null) {
                            contextFrameLayout().setVisibility(VISIBLE);
                        }
                        int i = 0;
                        for (Conversation c : conversationList) {
                            i++;
                            if (c.getId() != null && c.getId().equals(conversationId)) {
                                break;
                            }
                        }
                        if (contextSpinner() != null) {
                            contextSpinner().setSelection(i - 1, false);
                            contextSpinner().setOnItemSelectedListener(adapterView);
                        }
                    }
                }
                if (recyclerDetailConversationAdapter != null) {
                    recyclerDetailConversationAdapter.notifyDataSetChanged();
                }
                if (swipeLayout() != null) {
                    swipeLayout().post(new Runnable() {
                        @Override
                        public void run() {
                            if (swipeLayout() != null) {
                                swipeLayout().setRefreshing(false);
                            }
                        }
                    });
                }

                if (messageToForward != null) {
                    sendForwardMessage(messageToForward);
                    messageToForward = null;
                }

                if (!messageList.isEmpty()) {
                    channelKey = messageList.get(messageList.size() - 1).getGroupId();
                }
                if (initial) {
                    if (recordButtonWeakReference != null) {
                        ImageButton recordButton = recordButtonWeakReference.get();
                        if (recordButton != null) {
                            recordButton.setEnabled(true);
                        }
                    }
                    if (sendButton() != null) {
                        sendButton().setEnabled(true);
                    }
                    if (messageEditText() != null) {
                        messageEditText().setEnabled(true);
                    }
                }
                loadMore = !nextMessageList.isEmpty();
                createTemplateMessages();
                isAlreadyLoading = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class AttachmentAsyncTask extends AsyncTask<Void, Integer, Long> {

        File file;
        Uri uri;
        String mimeType;
        WeakReference<FragmentActivity> activityWeakReference;
        WeakReference<TextView> textViewWeakReference;
        WeakReference<RelativeLayout> relativeLayoutWeakReference;
        WeakReference<ImageView> imageViewLayoutWeakReference;
        WeakReference<AlCustomizationSettings> alCustomizationSettingsLayoutWeakReference;

        public AttachmentAsyncTask(Uri uri, File file, FragmentActivity activity) {
            this.file = file;
            this.uri = uri;
            this.activityWeakReference = new WeakReference<>(activity);
        }

        public void setTextViewWeakReference(TextView textView) {
            this.textViewWeakReference = new WeakReference<>(textView);
        }

        public void setRelativeLayoutWeakReference(RelativeLayout relativeLayout) {
            this.relativeLayoutWeakReference = new WeakReference<>(relativeLayout);

        }

        public void setImageViewLayoutWeakReference(ImageView imageViewLayoutWeakReference) {
            this.imageViewLayoutWeakReference = new WeakReference<>(imageViewLayoutWeakReference);
        }

        public void setAlCustomizationSettingsLayoutWeakReference(AlCustomizationSettings alCustomizationSettings) {
            this.alCustomizationSettingsLayoutWeakReference = new WeakReference<>(alCustomizationSettings);
        }

        @Override
        protected Long doInBackground(Void... params) {

            mimeType = URLConnection.guessContentTypeFromName(file.getName());
            if (alCustomizationSettingsLayoutWeakReference.get().isImageCompressionEnabled() && mimeType != null && (mimeType.startsWith("image"))) {
                FragmentActivity fragmentActivity = activityWeakReference.get();
                boolean isCompressionSuccess = AlBitmapUtils.compress(uri, file, fragmentActivity);
            }
            filePath = Uri.parse(file.getAbsolutePath()).toString();
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            ImageView container;
            TextView fileNameTextView = null;
            RelativeLayout attachmentRelativeLayout;

            if (imageViewLayoutWeakReference != null && textViewWeakReference != null && relativeLayoutWeakReference != null) {
                container = imageViewLayoutWeakReference.get();
                fileNameTextView = textViewWeakReference.get();
                fileNameTextView.setText(file.getName());

                attachmentRelativeLayout = relativeLayoutWeakReference.get();
                attachmentRelativeLayout.setVisibility(VISIBLE);

                if (mimeType != null && (mimeType.startsWith("image") || mimeType.startsWith("video"))) {
                    fileNameTextView.setVisibility(View.GONE);
                    int reqWidth = mediaContainer.getWidth();
                    int reqHeight = mediaContainer.getHeight();
                    if (reqWidth == 0 || reqHeight == 0) {
                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        FragmentActivity activityRef = activityWeakReference.get();
                        if (activityRef != null) {
                            activityRef.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        }
                        reqHeight = displaymetrics.heightPixels;
                        reqWidth = displaymetrics.widthPixels;
                    }
                    if (alCustomizationSettingsLayoutWeakReference != null) {
                        AlCustomizationSettings customizationSettings = alCustomizationSettingsLayoutWeakReference.get();
                        Bitmap previewThumbnail = FileUtils.getPreview(file.getAbsolutePath(), reqWidth, reqHeight, customizationSettings.isImageCompression(), mimeType);
                        container.setImageBitmap(previewThumbnail);
                    }
                } else {
                    fileNameTextView.setVisibility(VISIBLE);
                    container.setImageBitmap(null);
                }
            }
        }

    }

    @Override
    public void onAction(Context context, String action, Message message, Object object) {
        switch (action) {
            case "sendGuestList":
                List<ALGuestCountModel> guestCountModels = (List<ALGuestCountModel>) object;
                sendGuestListMessage(guestCountModels);
                break;

            case "sendHotelRating":
                sendMessage((String) object);
                break;

            case "sendHotelDetails":
                sendHotelDetailMessage((AlHotelBookingModel) object);
                break;

            case "sendRoomDetailsMessage":
                sendRoomDetailsMessage((AlHotelBookingModel) object);
                break;

            case "sendBookingDetails":
                sendBookingDetailsMessage((ALBookingDetailsModel) object);
                break;

            case "makePayment":
                makePaymentForBooking((ALRichMessageModel) object);
                break;

            case "listItemClick":
                sendMessage((String) object);
                break;
        }
    }

    public void sendGuestListMessage(List<ALGuestCountModel> guestList) {

        Map<String, String> metadata = new HashMap<>();
        metadata.put("guestTypeId", "ADULTS");
        metadata.put("isRoomGuestJSON", "true");
        metadata.put("roomGuestJson", GsonUtils.getJsonFromObject(guestList, List.class));

        StringBuilder message = new StringBuilder("");
        int count = 0;

        for (ALGuestCountModel guestModel : guestList) {
            message.append("Room ");
            message.append(count + 1);
            message.append(" Guest ");
            message.append(guestModel.getNoOfAdults());
            message.append(" Children ");
            message.append(guestModel.getNoOfChild());
            message.append(", ");
        }

        sendMessage(message.toString(), metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void sendHotelDetailMessage(AlHotelBookingModel hotel) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("hotelSelected", "true");
        metadata.put("resultIndex", String.valueOf(hotel.getResultIndex()));
        metadata.put("sessionId", hotel.getSessionId());
        metadata.put("skipBot", "true");

        String message = "Get room detail of " + hotel.getHotelName();

        sendMessage(message, metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void sendRoomDetailsMessage(AlHotelBookingModel hotel) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("HotelResultIndex", String.valueOf(hotel.getHotelResultIndex()));
        metadata.put("NoOfRooms", String.valueOf(hotel.getNoOfRooms()));
        metadata.put("RoomIndex", String.valueOf(hotel.getRoomIndex()));
        metadata.put("blockHotelRoom", "true");
        metadata.put("sessionId", hotel.getSessionId());
        metadata.put("skipBot", "true");

        String message = "Book Hotel " + hotel.getHotelName() + ", Room " + hotel.getRoomTypeName();

        sendMessage(message, metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void sendBookingDetailsMessage(ALBookingDetailsModel model) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("guestDetail", "true");
        metadata.put("personInfo", GsonUtils.getJsonFromObject(model.getPersonInfo(), ALBookingDetailsModel.ALBookingDetails.class));
        metadata.put("sessionId", model.getSessionId());
        metadata.put("skipBot", "true");

        sendMessage("Your details have been submitted", metadata, Message.ContentType.DEFAULT.getValue());
    }

    public void makePaymentForBooking(ALRichMessageModel model) {
        Intent intent = new Intent(getActivity(), PaymentActivity.class);
        intent.putExtra("formData", model.getFormData());
        intent.putExtra("formAction", model.getFormAction());
        if (getContext() != null) {
            getContext().startActivity(intent);
        }
    }
}

