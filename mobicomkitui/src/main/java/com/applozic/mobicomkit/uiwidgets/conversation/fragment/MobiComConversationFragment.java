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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserBlockTask;
import com.applozic.mobicomkit.api.attachment.AttachmentView;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.ConversationReadService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageClientService;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.conversation.selfdestruct.DisappearingMessageTask;
import com.applozic.mobicomkit.api.conversation.service.ConversationService;
import com.applozic.mobicomkit.api.notification.MuteNotificationAsync;
import com.applozic.mobicomkit.api.notification.MuteNotificationRequest;
import com.applozic.mobicomkit.api.people.UserIntentService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.MobiComVCFParser;
import com.applozic.mobicomkit.contact.VCFContactData;
import com.applozic.mobicomkit.feed.ApiResponse;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.attachmentview.ApplozicAudioManager;
import com.applozic.mobicomkit.uiwidgets.attachmentview.ApplozicAudioRecordManager;
import com.applozic.mobicomkit.uiwidgets.attachmentview.ApplozicDocumentView;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationListView;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.DeleteConversationAsyncTask;
import com.applozic.mobicomkit.uiwidgets.conversation.MessageCommunicator;
import com.applozic.mobicomkit.uiwidgets.conversation.UIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ChannelInfoActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.ApplozicContextSpinnerAdapter;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.DetailedConversationAdapter;
import com.applozic.mobicomkit.uiwidgets.instruction.InstructionUtil;
import com.applozic.mobicomkit.uiwidgets.people.fragment.UserProfileFragment;
import com.applozic.mobicomkit.uiwidgets.schedule.ConversationScheduler;
import com.applozic.mobicomkit.uiwidgets.schedule.ScheduledTimeHolder;
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
import java.lang.reflect.Type;
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
abstract public class MobiComConversationFragment extends Fragment implements View.OnClickListener, GestureDetector.OnGestureListener {

    //Todo: Increase the file size limit
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
    protected ImageButton sendButton;
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
    protected ConversationListView listView = null;
    protected List<Message> messageList = new ArrayList<Message>();
    protected DetailedConversationAdapter conversationAdapter = null;
    protected Drawable sentIcon;
    protected Drawable deliveredIcon;
    protected ImageButton emoticonsBtn;
    protected Support support;
    protected MultimediaOptionFragment multimediaOptionFragment = new MultimediaOptionFragment();
    protected boolean hideExtendedSendingOptionLayout;
    protected SyncCallService syncCallService;
    protected ApplozicContextSpinnerAdapter applozicContextSpinnerAdapter;
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
    ApplozicDocumentView applozicDocumentView;
    ImageView slideImageView;
    private EmojiconHandler emojiIconHandler;
    private Bitmap previewThumbnail;
    protected TextView isTyping, bottomlayoutTextView;
    private LinearLayout statusMessageLayout;
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
    private ImageView audioRecordIconImageView;

    public static int dp(float value) {
        return (int) Math.ceil(1 * value);
    }

    public void setEmojiIconHandler(EmojiconHandler emojiIconHandler) {
        this.emojiIconHandler = emojiIconHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String jsonString = FileUtils.loadSettingsJsonFile(getActivity().getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        applozicDocumentView = new ApplozicDocumentView(getContext());
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
        listView = (ConversationListView) list.findViewById(R.id.messageList);
        ((ConversationActivity) getActivity()).setChildFragmentLayoutBGToTransparent();
        listView.setDivider(null);
        messageList = new ArrayList<Message>();
        multimediaPopupGrid = (GridView) list.findViewById(R.id.mobicom_multimedia_options1);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        toolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        toolbar.setClickable(true);
        mainEditTextLinearLayout = (LinearLayout) list.findViewById(R.id.main_edit_text_linear_layout);
        individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        slideImageView = (ImageView) list.findViewById(R.id.slide_image_view);
        sendButton = (ImageButton) individualMessageSendLayout.findViewById(R.id.conversation_send);
        mainEditTextLinearLayout = (LinearLayout) list.findViewById(R.id.main_edit_text_linear_layout);
        audioRecordFrameLayout = (FrameLayout) list.findViewById(R.id.audio_record_frame_layout);
        Configuration config = getResources().getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
                sendButton.setScaleX(-1);
                mainEditTextLinearLayout.setBackgroundResource(R.drawable.applozic_chat_left_icon);
                audioRecordFrameLayout.setBackgroundResource(R.drawable.applozic_chat_left_icon);
                slideImageView.setImageResource(R.drawable.slide_arrow_right);

            }
        }
        extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);

        statusMessageLayout = (LinearLayout) list.findViewById(R.id.status_message_layout);
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
                        downloadConversation = new DownloadConversation(listView, true, 1, 0, 0, contact, channel, conversation.getId());
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
        imageViewRLayout = (RelativeLayout) list.findViewById(R.id.imageViewRLayout);
        imageViewForAttachmentType = (ImageView) list.findViewById(R.id.imageViewForAttachmentType);
        spinnerLayout = inflater.inflate(R.layout.mobicom_message_list_header_footer, null);
        infoBroadcast = (TextView) spinnerLayout.findViewById(R.id.info_broadcast);
        spinnerLayout.setVisibility(View.GONE);
        emptyTextView = (TextView) list.findViewById(R.id.noConversations);
        emptyTextView.setTextColor(Color.parseColor(alCustomizationSettings.getNoConversationLabelTextColor().trim()));
        emoticonsBtn.setOnClickListener(this);
        listView.addHeaderView(spinnerLayout);
        sentIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_sent);
        deliveredIcon = getResources().getDrawable(R.drawable.applozic_ic_action_message_delivered);

        listView.setLongClickable(true);
        GradientDrawable bgShape = (GradientDrawable) sendButton.getBackground();
        bgShape.setColor(Color.parseColor(alCustomizationSettings.getSendButtonBackgroundColor().trim()));
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
        listView.setMessageEditText(messageEditText);

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


        sendButton.setOnTouchListener(new View.OnTouchListener() {


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
                    x = x + ApplozicAudioRecordAnimation.getX(sendButton);
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
                        //Log.i(TAG, "typing started event...");
                        typingStarted = true;
                        Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
                        intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
                        intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
                        intent.putExtra(ApplozicMqttIntentService.TYPING, typingStarted);
                        sendButton.setImageResource(R.drawable.mobicom_ic_action_send);
                        getActivity().startService(intent);
                    } else if (s.toString().trim().length() == 0 && typingStarted) {
                        //Log.i(TAG, "typing stopped event...");
                        typingStarted = false;
                        Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
                        intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
                        intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
                        intent.putExtra(ApplozicMqttIntentService.TYPING, typingStarted);
                        sendButton.setImageResource(R.drawable.applozic_mic_pressed);
                        getActivity().startService(intent);
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
                emoticonsFrameLayout.setVisibility(View.GONE);
            }
        });

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
                        Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
                        intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
                        intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
                        intent.putExtra(ApplozicMqttIntentService.TYPING, typingStarted);
                        getActivity().startService(intent);
                    }
                    emoticonsFrameLayout.setVisibility(View.GONE);

                    multimediaPopupGrid.setVisibility(View.GONE);
                }
            }

        });

        messageEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    Utils.toggleSoftKeyBoard(getActivity(), true);
                    return true;
                }
                return false;
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {
                                              if (!isToastVisible && !typingStarted) {
                                                  vibrate();
                                                  errorEditTextView.requestFocus();
                                                  errorEditTextView.setError(getResources().getString(R.string.hold_to_record_release_to_send));
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
                                              sendButton.setVisibility(VISIBLE);
                                              sendButton.setImageResource(R.drawable.applozic_mic_pressed);
                                              errorEditTextView.setVisibility(VISIBLE);
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
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                                         @Override
                                         public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                                             if (conversationAdapter != null) {
                                                 conversationAdapter.contactImageLoader.setPauseWork(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING);
                                             }
                                         }

                                         @Override
                                         public void onScroll(AbsListView view, int firstVisibleItem, int amountVisible,
                                                              int totalItems) {
                                             if (loadMore) {
                                                 int topRowVerticalPosition =
                                                         (listView == null || listView.getChildCount() == 0) ?
                                                                 0 : listView.getChildAt(0).getTop();
                                                 swipeLayout.setEnabled(topRowVerticalPosition >= 0);
                                             }
                                         }
                                     }
        );

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (channel != null) {
                    if (channel.isDeleted()) {
                        return;
                    }
                    if (alCustomizationSettings.isGroupInfoScreenVisible() && !Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
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
        //Adding fragment for emoticons...
//        //Fragment emojiFragment = new EmojiconsFragment(this, this);
//        Fragment emojiFragment = new EmojiconsFragment();
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.add(R.id.emojicons_frame_layout, emojiFragment).commit();
        return list;
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
        outputFile = FileClientService.getFilePath(audioFileName, getContext().getApplicationContext(), "audio/m4a").getAbsolutePath();
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

        sendButton.getParent()
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
                    Toast.makeText(getActivity(), getActivity().getString(R.string.internet_connection_not_available), Toast.LENGTH_SHORT).show();
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

    protected void processSendMessage() {
        if (!TextUtils.isEmpty(messageEditText.getText().toString().trim()) || !TextUtils.isEmpty(filePath)) {
            String inputMessage = messageEditText.getText().toString();
            String[] inputMsg = inputMessage.toLowerCase().split(" ");
            List<String> userInputList = Arrays.asList(inputMsg);

            boolean disjointResult = (restrictedWords == null) ? true : disjoint(restrictedWords, userInputList);

            if (disjointResult) {

                sendMessage(messageEditText.getText().toString().trim());
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
                    conversationAdapter.notifyDataSetChanged();
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
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (conversationAdapter != null) {
                    messageList.clear();
                    conversationAdapter.notifyDataSetChanged();
                }
                if (applozicContextSpinnerAdapter != null) {
                    contextFrameLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    public void updateMessage(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Note: Removing and adding the same message again as the new sms object will contain the keyString.
                messageList.remove(message);
                messageList.add(message);
                conversationAdapter.notifyDataSetChanged();
            }
        });
    }

    public void addMessage(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Todo: Handle disappearing messages.
                boolean added = updateMessageList(message, false);
                if (added) {
                    //Todo: update unread count
                    conversationAdapter.notifyDataSetChanged();
                    listView.smoothScrollToPosition(messageList.size());
                    listView.setSelection(messageList.size());
                    emptyTextView.setVisibility(View.GONE);
                    currentConversationId = message.getConversationId();
                    channelKey = message.getGroupId();
                    if (Message.MessageType.MT_INBOX.getValue().equals(message.getType())) {
                        try {
                            messageDatabaseService.updateReadStatusForKeyString(message.getKeyString());
                            Intent intent = new Intent(getActivity(), ConversationReadService.class);
                            intent.putExtra(ConversationReadService.SINGLE_MESSAGE_READ, true);
                            intent.putExtra(ConversationReadService.CONTACT, contact);
                            intent.putExtra(ConversationReadService.CHANNEL, channel);
                            getActivity().startService(intent);
                        } catch (Exception e) {
                            Utils.printLog(getContext(), TAG, "Got exception while read");
                        }
                    }
                }

                selfDestructMessage(message);
            }
        });
    }

    protected abstract void processMobiTexterUserCheck();

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        if (view.getId() == R.id.messageList) {
            menu.setHeaderTitle(R.string.messageOptions);

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            int positionInSmsList = info.position - 1;
            if (positionInSmsList < 0 || messageList.isEmpty()) {
                return;
            }
            Message message = messageList.get(positionInSmsList);

            if (message.isTempDateType() || message.isCustom() || message.isChannelCustomMessage()) {
                return;
            }

            String[] menuItems = getResources().getStringArray(R.array.menu);

            for (int i = 0; i < menuItems.length; i++) {

                if (!(message.isGroupMessage() && message.isTypeOutbox() && message.isSentToServer()) && menuItems[i].equals(getResources().getString(R.string.info))) {
                    continue;
                }

                if ((message.hasAttachment() || message.getContentType() == Message.ContentType.LOCATION.getValue() || message.isVideoOrAudioCallMessage()) &&
                        menuItems[i].equals(getResources().getString(R.string.copy))) {
                    continue;
                }
                if (((channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || message.isCall() || (message.hasAttachment() && !message.isAttachmentDownloaded())) && (menuItems[i].equals(getResources().getString(R.string.forward)) ||
                        menuItems[i].equals(getResources().getString(R.string.resend)))) {
                    continue;
                }
                if (menuItems[i].equals(getResources().getString(R.string.resend)) && (!message.isSentViaApp() || message.isSentToServer() || message.isVideoOrAudioCallMessage())) {
                    continue;
                }

                if (menuItems[i].equals(getResources().getString(R.string.reply)) &&  (!alCustomizationSettings.isReplyOption() || message.isAttachmentUploadInProgress() || TextUtils.isEmpty(message.getKeyString()) || !message.isSentToServer() || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || (message.hasAttachment() && !message.isAttachmentDownloaded()) || channel != null && !ChannelService.getInstance(getActivity()).processIsUserPresentInChannel(channel.getKey()) || message.isVideoOrAudioCallMessage() || contact != null && contact.isDeleted() ) ) {
                    continue;
                }

                if (menuItems[i].equals(getResources().getString(R.string.delete)) && (TextUtils.isEmpty(message.getKeyString()) || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())))) {
                    continue;
                }
                if (menuItems[i].equals(getResources().getString(R.string.info)) && (TextUtils.isEmpty(message.getKeyString()) || (channel != null && Channel.GroupType.OPEN.getValue().equals(channel.getType())) || message.isVideoOrAudioCallMessage())) {
                    continue;
                }
                if (menuItems[i].equals(getResources().getString(R.string.share)) && (message.isAttachmentUploadInProgress() || message.getFilePaths() == null || !(new File(message.getFilePaths().get(0)).exists()))) {
                    continue;
                }

                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

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
                if (alCustomizationSettings.isMuteOption()) {
                    menu.findItem(R.id.unmuteGroup).setVisible(!channel.isDeleted() && channel.isNotificationMuted());
                    menu.findItem(R.id.muteGroup).setVisible(!channel.isDeleted() && !channel.isNotificationMuted());
                }
            }
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
            menu.findItem(R.id.deleteConversation).setVisible(alCustomizationSettings.isDeleteOption());
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
                    try {
                        String activityName = ApplozicSetting.getInstance(getActivity()).getActivityCallback(ApplozicSetting.RequestCode.VIDEO_CALL);
                        Class activityToOpen = Class.forName(activityName);
                        Intent intent = new Intent(getActivity(), activityToOpen);
                        intent.putExtra("CONTACT_ID", contact.getUserId());
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        if (id == R.id.muteGroup) {
            muteGroupChat();
        }
        if (id == R.id.unmuteGroup) {
            umuteGroupChat();
        }
        return false;
    }

    public void loadConversation(final Contact contact, final Channel channel, final Integer conversationId, final String searchString) {
        if (downloadConversation != null) {
            downloadConversation.cancel(true);
        }

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
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
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
        setContact(contact);
        setChannel(channel);

        unregisterForContextMenu(listView);
        clearList();
        updateTitle();
        swipeLayout.setEnabled(true);
        loadMore = true;
        if (selfDestructMessageSpinner != null) {
            selfDestructMessageSpinner.setSelection(0);
        }

        if (contact != null) {
            conversationAdapter = new DetailedConversationAdapter(getActivity(),
                    R.layout.mobicom_message_row_view, messageList, contact, messageIntentClass, emojiIconHandler);
            conversationAdapter.setAlCustomizationSettings(alCustomizationSettings);
        } else if (channel != null) {
            conversationAdapter = new DetailedConversationAdapter(getActivity(),
                    R.layout.mobicom_message_row_view, messageList, channel, messageIntentClass, emojiIconHandler);
            conversationAdapter.setAlCustomizationSettings(alCustomizationSettings);
        }
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(conversationAdapter);
        registerForContextMenu(listView);

        processMobiTexterUserCheck();

        if (contact != null) {
            if (!TextUtils.isEmpty(contact.getContactIds())) {
                NotificationManager notificationManager =
                        (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(contact.getContactIds().hashCode());
            }
        }

        if (channel != null) {
            NotificationManager notificationManager =
                    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(String.valueOf(channel.getKey()).hashCode());
        }

        downloadConversation = new DownloadConversation(listView, true, 1, 0, 0, contact, channel, conversationId);
        AsyncTaskCompat.executeParallel(downloadConversation);

         /*  if (contact != null && support.isSupportNumber(contact.getFormattedContactNumber())) {
            sendType.setSelection(1);
            extendedSendingOptionLayout.setVisibility(View.GONE);
            messageEditText.setHint(R.string.enter_support_query_hint);
        } else {
            messageEditText.setHint(R.string.enter_mt_message_hint);
        }*/
        if (hideExtendedSendingOptionLayout) {
            extendedSendingOptionLayout.setVisibility(View.GONE);
        }
        emoticonsFrameLayout.setVisibility(View.GONE);

        if (contact != null) {
            Intent intent = new Intent(getActivity(), UserIntentService.class);
            intent.putExtra(UserIntentService.USER_ID, contact.getUserId());
            getActivity().startService(intent);

        }

        if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Intent intent = new Intent(getActivity(), UserIntentService.class);
                    intent.putExtra(UserIntentService.USER_ID, userId);
                    getActivity().startService(intent);
                }
            } else {
                updateChannelSubTitle();
            }
        }

        InstructionUtil.showInstruction(getActivity(), R.string.instruction_go_back_to_recent_conversation_list, MobiComKitActivityInterface.INSTRUCTION_DELAY, BroadcastService.INTENT_ACTIONS.INSTRUCTION.toString());
    }


    public void updateLastSeenStatus() {
        if (this.getActivity() == null) {
            return;
        }
        if (contact != null) {
            contact = appContactService.getContactById(contact.getContactIds());
        }

        if (contact != null) {
            processUpdateLastSeenStatus(contact);
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

        if(userNotAbleToChatLayout != null && individualMessageSendLayout != null){
            userNotAbleToChatLayout.setVisibility(withUserContact.isDeleted() ? VISIBLE : View.GONE);
            individualMessageSendLayout.setVisibility(withUserContact.isDeleted() ? View.GONE : VISIBLE);
            bottomlayoutTextView.setText(R.string.user_has_been_deleted_text);
        }

        if (menu != null) {
            menu.findItem(R.id.userBlock).setVisible(alCustomizationSettings.isBlockOption() ? !withUserContact.isDeleted() && !withUserContact.isBlocked(): alCustomizationSettings.isBlockOption());
            menu.findItem(R.id.userUnBlock).setVisible(alCustomizationSettings.isBlockOption() ? !withUserContact.isDeleted() && withUserContact.isBlocked() : alCustomizationSettings.isBlockOption());
            menu.findItem(R.id.refresh).setVisible(alCustomizationSettings.isRefreshOption() ? !withUserContact.isDeleted() : alCustomizationSettings.isRefreshOption());

        }

        if (withUserContact.isBlocked() || withUserContact.isBlockedBy() || withUserContact.isDeleted()) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (withUserContact != null) {
                    if (withUserContact.isConnected()) {
                        typingStarted = false;
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getActivity().getString(R.string.user_online));
                    } else if (withUserContact.getLastSeenAt() != 0) {
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getActivity().getString(R.string.subtitle_last_seen_at_time) + " " + DateUtils.getDateAndTimeForLastSeen(withUserContact.getLastSeenAt()));
                    } else {
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                    }
                }
            }

        });
    }

    public void updateChannelSubTitle() {
        channelUserMapperList = ChannelService.getInstance(getActivity()).getListOfUsersFromChannelUserMapper(channel.getKey());
        if (channelUserMapperList != null && channelUserMapperList.size() > 0) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = appContactService.getContactById(userId);
                    if (withUserContact != null) {
                        if (withUserContact.isBlocked()) {
                            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                        } else {
                            if (withUserContact.isConnected()) {
                                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getActivity().getString(R.string.user_online));
                            } else if (withUserContact.getLastSeenAt() != 0) {
                                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getActivity().getString(R.string.subtitle_last_seen_at_time) + " " + DateUtils.getDateAndTimeForLastSeen(withUserContact.getLastSeenAt()));
                            } else {
                                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                            }
                        }
                    }
                }

            } else {
                StringBuffer stringBuffer = new StringBuffer();
                Contact contactDisplayName;
                String youString = null;
                int i = 0;
                for (ChannelUserMapper channelUserMapper : channelUserMapperList) {
                    i++;
                    if (i > 20)
                        break;
                    contactDisplayName = appContactService.getContactById(channelUserMapper.getUserKey());
                    if (!TextUtils.isEmpty(channelUserMapper.getUserKey())) {
                        if (MobiComUserPreference.getInstance(getActivity()).getUserId().equals(channelUserMapper.getUserKey())) {
                            youString = getString(R.string.you_string);
                        } else {
                            stringBuffer.append(contactDisplayName.getDisplayName()).append(",");
                        }
                    }
                }
                if (!TextUtils.isEmpty(stringBuffer)) {
                    if (channelUserMapperList.size() <= 20) {
                        if (!TextUtils.isEmpty(youString)) {
                            stringBuffer.append(youString).append(",");
                        }
                        int lastIndex = stringBuffer.lastIndexOf(",");
                        String userIds = stringBuffer.replace(lastIndex, lastIndex + 1, "").toString();
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(userIds);
                    } else {
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(stringBuffer.toString());
                    }
                } else {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(youString);
                }
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

    public void updateUploadFailedStatus(Message message) {
        int i = messageList.indexOf(message);
        if (i != -1) {
            messageList.get(i).setCanceled(true);
            conversationAdapter.notifyDataSetChanged();
        }

    }

    public void downloadFailed(final Message message) {
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = messageList.indexOf(message);
                if (index != -1) {
                    View view = listView.getChildAt(index -
                            listView.getFirstVisiblePosition() + 1);

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
                        View view = listView.getChildAt(index -
                                listView.getFirstVisiblePosition() + 1);
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
                        View view = listView.getChildAt(index -
                                listView.getFirstVisiblePosition() + 1);
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
                        listView.smoothScrollToPosition(messageList.size());
                        listView.setSelection(messageList.size());
                        emptyTextView.setVisibility(View.GONE);
                        conversationAdapter.notifyDataSetChanged();
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
        sendButton.setVisibility(VISIBLE);
        sendButton.setImageResource(R.drawable.mobicom_ic_action_send);
        errorEditTextView.setVisibility(View.GONE);
        filePath = Uri.parse(file.getAbsolutePath()).toString();
        if (TextUtils.isEmpty(filePath)) {
            Utils.printLog(getContext(), TAG, "Error while fetching filePath");
            attachmentLayout.setVisibility(View.GONE);
            Toast.makeText(getActivity(), R.string.info_file_attachment_error, Toast.LENGTH_LONG).show();
            return;
        }
        String mimeType = getActivity().getContentResolver().getType(uri);
        Cursor returnCursor =
                getActivity().getContentResolver().query(uri, null, null, null, null);
        if (returnCursor != null) {
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            Long fileSize = returnCursor.getLong(sizeIndex);
            int maxFileSize = alCustomizationSettings.getMaxAttachmentSizeAllowed() * 1024 * 1024;
            if (fileSize > maxFileSize) {
                Toast.makeText(getActivity(), R.string.info_attachment_max_allowed_file_size, Toast.LENGTH_LONG).show();
                return;
            }
            attachedFile.setText(returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
            returnCursor.close();
        }
        attachmentLayout.setVisibility(VISIBLE);
        if (mimeType != null && (mimeType.startsWith("image") || mimeType.startsWith("video"))) {
            attachedFile.setVisibility(View.GONE);
            int reqWidth = mediaContainer.getWidth();
            int reqHeight = mediaContainer.getHeight();
            if (reqWidth == 0 || reqHeight == 0) {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                reqHeight = displaymetrics.heightPixels;
                reqWidth = displaymetrics.widthPixels;
            }
            previewThumbnail = FileUtils.getPreview(filePath, reqWidth, reqHeight, alCustomizationSettings.isImageCompression(), mimeType);
            mediaContainer.setImageBitmap(previewThumbnail);
        } else {
            attachedFile.setVisibility(VISIBLE);
            mediaContainer.setImageBitmap(null);
        }
    }

    public synchronized boolean updateMessageList(Message message, boolean update) {
        boolean toAdd = !messageList.contains(message);
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
                return ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(getActivity()).getUserId());
            }
        }
        return "";
    }

    public void onClickOnMessageReply(Message message) {
        if (message != null) {
            if (listView != null) {
                int height = listView.getHeight();
                int itemHeight = listView.getChildAt(0).getHeight();
                int index = messageList.indexOf(message);
                if (index != -1) {
                    listView.requestFocusFromTouch();
                    ((ListView) listView).setSelectionFromTop(index + 1, height / 2 - itemHeight / 2);
                    listView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (listView != null) {
                                try {
                                    if (listView.isFocused()) {
                                        listView.clearFocus();
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
        messageToSend.setFileMetaKeyStrings(fileMetaKeyStrings);
        messageToSend.setFileMetas(fileMetas);
        if (!TextUtils.isEmpty(ApplozicClient.getInstance(getActivity()).getMessageMetaData())) {
            Type mapType = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> messageMetaDataMap = null;
            try {
                messageMetaDataMap = new Gson().fromJson(ApplozicClient.getInstance(getActivity()).getMessageMetaData(), mapType);
                messageToSend.setMetadata(messageMetaDataMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            messageToSend.setMetadata(this.messageMetaData);
        }

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
                if (!userPreferences.getUserId().equals(channelUserMapper.getUserKey())) {
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
                    View view = listView.getChildAt(index - listView.getFirstVisiblePosition() + 1);
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
                        if (createdAtTime != null && messageListItem.getKeyString() != null && messageListItem.isTypeOutbox() && !messageListItem.isCall() && !messageListItem.getDelivered() && !messageListItem.isCustom() && !messageListItem.isChannelCustomMessage() && messageListItem.getScheduledAt() == null) {
                            createdAtTime.setCompoundDrawablesWithIntrinsicBounds(null, null, support.isSupportNumber(getCurrentUserId()) ? deliveredIcon : sentIcon, null);
                        }
                    }
                }
            }
        });
    }
    public void updateDownloadStatus(final Message message) {
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
                        View view = listView.getChildAt(index - listView.getFirstVisiblePosition() + 1);
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
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isTypingStatus.equals("1")) {
                    if (channel != null) {
                        if (!MobiComUserPreference.getInstance(getActivity()).getUserId().equals(typingUserId)) {
                            Contact displayNameContact = appContactService.getContactById(typingUserId);
                            if (displayNameContact.isBlocked() || displayNameContact.isBlockedBy()) {
                                return;
                            }
                            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getActivity().getString(R.string.is_typing));
                            } else {
                                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(displayNameContact.getDisplayName() + " " + getActivity().getString(R.string.is_typing));
                            }
                        }
                    } else {
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getActivity().getString(R.string.is_typing));
                    }
                } else {
                    if (channel != null) {
                        if (!MobiComUserPreference.getInstance(getActivity()).getUserId().equals(typingUserId)) {
                            Contact displayNameContact = appContactService.getContactById(typingUserId);
                            if (displayNameContact.isBlocked() || displayNameContact.isBlockedBy()) {
                                return;
                            }
                            updateChannelSubTitle();
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
            Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
            intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
            intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
            intent.putExtra(ApplozicMqttIntentService.TYPING, false);
            getActivity().startService(intent);
            typingStarted = false;
        }
        Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
        intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
        intent.putExtra(ApplozicMqttIntentService.UN_SUBSCRIBE_TO_TYPING, true);
        getActivity().startService(intent);
        if (conversationAdapter != null) {
            conversationAdapter.contactImageLoader.setPauseWork(false);
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
                stringBufferTitle.append(ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(getActivity()).getUserId()));
            }
        }
        if (stringBufferTitle != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(stringBufferTitle.toString());
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
        alertDialog.setTitle(getActivity().getString(R.string.dialog_delete_conversation_title).replace("[name]", getNameForInviteDialog()));
        alertDialog.setMessage(getActivity().getString(R.string.dialog_delete_conversation_confir).replace("[name]", getNameForInviteDialog()));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position - 1;
        if (messageList.size() <= position) {
            return true;
        }
        Message message = messageList.get(position);
        if (message.isTempDateType() || message.isCustom()) {
            return true;
        }

        switch (item.getItemId()) {
            case 0:
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(message.getMessage());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(getContext().getString(R.string.copied_message), message.getMessage());
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
                        shareUri = FileProvider.getUriForFile(getActivity(), Utils.getMetaDataValue(getActivity(), MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
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
                startActivity(Intent.createChooser(shareIntent, getString(R.string.send_message_to)));
                break;

            case 6:
                try {
                    Configuration config = getActivity().getResources().getConfiguration();
                    messageMetaData = new HashMap<>();
                    String displayName;
                    if (message.getGroupId() != null) {
                        if (MobiComUserPreference.getInstance(getActivity()).getUserId().equals(message.getContactIds()) || TextUtils.isEmpty(message.getContactIds())) {
                            displayName = getString(R.string.you_string);
                        } else {
                            displayName = appContactService.getContactById(message.getContactIds()).getDisplayName();
                        }
                    } else {
                        if (message.isTypeOutbox()) {
                            displayName = getString(R.string.you_string);
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
                                messageTextView.setText(getString(R.string.photo_string));
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
                                messageTextView.setText(getString(R.string.video_string));
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
                                messageTextView.setText(getString(R.string.audio_string));
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
                                    messageTextView.setText(getString(R.string.contact_string));
                                    messageTextView.append(" " + data.getName());
                                }
                            } catch (Exception e) {
                                imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_person_white);
                                messageTextView.setText(getString(R.string.contact_string));
                            }
                            galleryImageView.setVisibility(View.GONE);
                            imageViewRLayout.setVisibility(View.GONE);
                        } else {
                            imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_action_attachment);
                            if (TextUtils.isEmpty(message.getMessage())) {
                                messageTextView.setText(getString(R.string.attachment_string));
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
                        messageTextView.setText(getString(R.string.al_location_string));
                        imageViewForAttachmentType.setImageResource(R.drawable.applozic_ic_location_on_white_24dp);
                        imageViewForAttachmentType.setColorFilter(ContextCompat.getColor(getActivity(), R.color.apploizc_lite_gray_color));
                        messageImageLoader.setLoadingImage(R.drawable.applozic_map_offline_thumbnail);
                        messageImageLoader.loadImage(LocationUtils.loadStaticMap(message.getMessage()), galleryImageView);
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
                String ns = Context.NOTIFICATION_SERVICE;
                NotificationManager nMgr = (NotificationManager) getActivity().getSystemService(ns);
                nMgr.cancel(BroadcastService.currentUserId.hashCode());
            }

            Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
            intent.putExtra(ApplozicMqttIntentService.CHANNEL, channel);
            intent.putExtra(ApplozicMqttIntentService.SUBSCRIBE_TO_TYPING, true);
            getActivity().startService(intent);

            if (downloadConversation != null) {
                downloadConversation.cancel(true);
            }

            if (contact != null) {
                if (contact.isDeleted()) {
                    individualMessageSendLayout.setVisibility(View.GONE);
                    userNotAbleToChatLayout.setVisibility(VISIBLE);
                } else {
                    userNotAbleToChatLayout.setVisibility(View.GONE);
                }
                contact = appContactService.getContactById(contact.getContactIds());
                if (contact.isBlocked() || contact.isBlockedBy()) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                } else {
                    updateLastSeenStatus();
                }
            }

            if (SyncCallService.refreshView) {
                messageList.clear();
                SyncCallService.refreshView = false;
            }

            if (channel != null && !ChannelService.getInstance(getActivity()).processIsUserPresentInChannel(channel.getKey())) {
                Channel newChannel = ChannelService.getInstance(getActivity()).getChannelByChannelKey(channel.getKey());
                if (newChannel != null && newChannel.getType() != null && Channel.GroupType.OPEN.getValue().equals(newChannel.getType())) {
                    MobiComUserPreference.getInstance(getActivity()).setNewMessageFlag(true);
                }
            }

            if (messageList.isEmpty()) {
                loadConversation(contact, channel, currentConversationId, null);
            } else if (MobiComUserPreference.getInstance(getActivity()).getNewMessageFlag()) {
                loadnewMessageOnResume(contact, channel, currentConversationId);
            }

            MobiComUserPreference.getInstance(getActivity()).setNewMessageFlag(false);
        }
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                downloadConversation = new DownloadConversation(listView, false, 1, 1, 1, contact, channel, currentConversationId);
                AsyncTaskCompat.executeParallel(downloadConversation);
            }
        });
        if (channel != null) {
            updateChannelTitle();

            if (channel.getType() != null && !Channel.GroupType.OPEN.getValue().equals(channel.getType())) {
                boolean present = ChannelService.getInstance(getActivity()).processIsUserPresentInChannel(channel.getKey());
                hideSendMessageLayout(channel.isDeleted() || !present);

            } else {
                hideSendMessageLayout(channel.isDeleted());
            }
            if (ChannelService.isUpdateTitle) {
                updateChannelSubTitle();
                ChannelService.isUpdateTitle = false;
            }
        }

    }

    private void hideSendMessageLayout(boolean hide) {
        if (hide) {
            individualMessageSendLayout.setVisibility(View.GONE);
            userNotAbleToChatLayout.setVisibility(VISIBLE);
        } else {
            userNotAbleToChatLayout.setVisibility(View.GONE);

        }

    }

    public void updateChannelTitleAndSubTitle() {
        if (channel != null) {
            Channel channelInfo = ChannelService.getInstance(getActivity()).getChannelInfo(channel.getKey());

            if (channelInfo.isDeleted()) {
                channel.setDeletedAtTime(channelInfo.getDeletedAtTime());
                individualMessageSendLayout.setVisibility(View.GONE);
                userNotAbleToChatLayout.setVisibility(VISIBLE);
                userNotAbleToChatTextView.setText(R.string.group_has_been_deleted_text);
            } else {
                if ((!ChannelService.getInstance(getActivity()).processIsUserPresentInChannel(channel.getKey())
                        && userNotAbleToChatLayout != null
                        && !Channel.GroupType.OPEN.getValue().equals(channel.getType()))) {

                    individualMessageSendLayout.setVisibility(View.GONE);
                    userNotAbleToChatLayout.setVisibility(VISIBLE);
                }
            }

            updateChannelTitle();
            updateChannelSubTitle();
        }
    }


    public void updateChannelTitle() {
        if (!Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
            Channel newChannel = ChannelService.getInstance(getActivity()).getChannelByChannelKey(channel.getKey());
            if (newChannel != null && !TextUtils.isEmpty(channel.getName()) && !channel.getName().equals(newChannel.getName())) {
                title = ChannelUtils.getChannelTitleName(newChannel, MobiComUserPreference.getInstance(getActivity()).getUserId());
                channel = newChannel;
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
            }
        }
    }

    public void updateTitleForOpenGroup() {
        try {
            if (channel != null) {
                Channel newChannel = ChannelService.getInstance(getActivity()).getChannelByChannelKey(channel.getKey());
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(newChannel.getName());
            }
            updateChannelSubTitle();
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
        downloadConversation = new DownloadConversation(listView, true, 1, 0, 0, contact, channel, conversationId);
        downloadConversation.execute();
    }

    public int ScrollToFirstSearchIndex() {

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
                getActivity().getString(R.string.please_wait_info), true);

        UserBlockTask.TaskListener listener = new UserBlockTask.TaskListener() {

            @Override
            public void onSuccess(ApiResponse apiResponse) {
                if (block && typingStarted) {
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                    Intent intent = new Intent(getActivity(), ApplozicMqttIntentService.class);
                    intent.putExtra(ApplozicMqttIntentService.CONTACT, contact);
                    intent.putExtra(ApplozicMqttIntentService.STOP_TYPING, true);
                    getActivity().startService(intent);
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

        final CharSequence[] items = {getString(R.string.eight_Hours), getString(R.string.one_week), getString(R.string.one_year)};
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
                .setTitle(getResources().getString(R.string.mute_group_for))
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        count = 0;
        t.cancel();
        ((ConversationActivity) getActivity()).setChildFragmentLayoutBG();
        ApplozicAudioManager.getInstance(getContext()).audiostop();
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

    public class DownloadConversation extends AsyncTask<Void, Integer, Long> {

        private AbsListView view;
        private int firstVisibleItem;
        private int amountVisible;
        private int totalItems;
        private boolean initial;
        private Contact contact;
        private Channel channel;
        private Integer conversationId;
        private List<Conversation> conversationList;
        private List<Message> nextMessageList = new ArrayList<Message>();

        public DownloadConversation(AbsListView view, boolean initial, int firstVisibleItem, int amountVisible, int totalItems, Contact contact, Channel channel, Integer conversationId) {
            this.view = view;
            this.initial = initial;
            this.firstVisibleItem = firstVisibleItem;
            this.amountVisible = amountVisible;
            this.totalItems = totalItems;
            this.contact = contact;
            this.channel = channel;
            this.conversationId = conversationId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            emptyTextView.setVisibility(View.GONE);
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
            if (initial) {
                sendButton.setEnabled(false);
                messageEditText.setEnabled(false);
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
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(true);
                }
            });
            if (nextMessageList.isEmpty()) {
                listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
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
                conversationAdapter.searchString = searchString;
                emptyTextView.setVisibility(messageList.isEmpty() ? VISIBLE : View.GONE);
                if (!messageList.isEmpty()) {
                    listView.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(searchString)) {
                                int height = listView.getHeight();
                                int itemHeight = listView.getChildAt(0).getHeight();
                                listView.requestFocusFromTouch();
                                ((ListView) listView).setSelectionFromTop(ScrollToFirstSearchIndex() + 1, height / 2 - itemHeight / 2);
                            } else {
                                listView.setSelection(messageList.size() - 1);

                            }
                        }
                    });
                }
            } else if (!nextMessageList.isEmpty()) {
                listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
                messageList.addAll(0, nextMessageList);
                listView.setSelection(nextMessageList.size());
            }

            conversationService.read(contact, channel);

            if (!messageList.isEmpty()) {
                for (int i = messageList.size() - 1; i >= 0; i--) {
                    Message message = messageList.get(i);
                    if (!message.isRead() && !message.isTempDateType() && !message.isCustom()) {
                        message.setRead(Boolean.TRUE);
                        messageDatabaseService.updateMessageReadFlag(message.getMessageId(), true);
                    } else {
                        break;
                    }
                }
            }

            if (conversations != null && conversations.size() > 0) {
                conversationList = conversations;
            }
            if (conversationList != null && conversationList.size() > 0 && !onSelected) {
                onSelected = true;
                applozicContextSpinnerAdapter = new ApplozicContextSpinnerAdapter(getActivity(), conversationList);
                if (applozicContextSpinnerAdapter != null) {
                    contextSpinner.setAdapter(applozicContextSpinnerAdapter);
                    contextFrameLayout.setVisibility(VISIBLE);
                    int i = 0;
                    for (Conversation c : conversationList) {
                        i++;
                        if (c.getId().equals(conversationId)) {
                            break;
                        }
                    }
                    contextSpinner.setSelection(i - 1, false);
                    contextSpinner.setOnItemSelectedListener(adapterView);
                }
            }
            if (conversationAdapter != null) {
                conversationAdapter.notifyDataSetChanged();
            }
            swipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeLayout.setRefreshing(false);
                }
            });

            if (messageToForward != null) {
                sendForwardMessage(messageToForward);
                messageToForward = null;
            }

            if (!messageList.isEmpty()) {
                channelKey = messageList.get(messageList.size() - 1).getGroupId();
            }
            if (initial) {
                sendButton.setEnabled(true);
                messageEditText.setEnabled(true);
            }
            loadMore = !nextMessageList.isEmpty();
        }

    }
}
