package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;

import android.support.v7.widget.RecyclerView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.AlLinearLayoutManager;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.DividerItemDecoration;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.RecyclerViewPositionHelper;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.QuickConversationAdapter;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by devashish on 10/2/15.
 */
public class MobiComQuickConversationFragment extends Fragment implements SearchListFragment {

    public static final String QUICK_CONVERSATION_EVENT = "quick_conversation";
    protected RecyclerView recyclerView = null;
    protected ImageButton fabButton;
    protected TextView emptyTextView;
    protected Button startNewButton;
    protected SwipeRefreshLayout swipeLayout;
    protected int listIndex;
    protected Map<String, Message> latestMessageForEachContact = new HashMap<String, Message>();
    protected List<Message> messageList = new ArrayList<Message>();
    protected QuickConversationAdapter recyclerAdapter = null;
    protected boolean loadMore = false;
    protected SyncCallService syncCallService;
    ConversationUIService conversationUIService;
    AlCustomizationSettings alCustomizationSettings;
    String searchString;
    private Long minCreatedAtTime;
    private DownloadConversation downloadConversation;
    private BaseContactService baseContactService;
    private Toolbar toolbar;
    private MessageDatabaseService messageDatabaseService;
    private int visibleThreshold = 5;
    private int currentPage = 0;
    private int previousTotalItemCount = 0;
    private boolean loading = true;
    private int startingPageIndex = 0;
    private ProgressBar progressBar;
    RecyclerViewPositionHelper recyclerViewPositionHelper;
    AlLinearLayoutManager linearLayoutManager;
    int position;
    boolean isAlreadyLoading = false;
    int pastVisiblesItems, visibleItemCount, totalItemCount;


    public RecyclerView getRecyclerView() {
        return recyclerView;
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
        syncCallService = SyncCallService.getInstance(getActivity());
        conversationUIService = new ConversationUIService(getActivity());
        baseContactService = new AppContactService(getActivity());
        messageDatabaseService = new MessageDatabaseService(getActivity());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Thread(new Runnable() {
            @Override
            public void run() {
                MobiComUserPreference.getInstance(getActivity()).setDeviceTimeOffset(DateUtils.getTimeDiffFromUtc());
            }
        }));
        setHasOptionsMenu(true);
        BroadcastService.lastIndexForChats = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View list = inflater.inflate(R.layout.mobicom_message_list, container, false);

        recyclerView = (RecyclerView) list.findViewById(R.id.messageList);
        recyclerView.setBackgroundColor(getResources().getColor(R.color.conversation_list_all_background));

        if (messageList != null && !messageList.contains(null)) {
            messageList.add(null);
        }
        recyclerAdapter = new QuickConversationAdapter(getContext(), messageList, null);
        //recyclerAdapter.setRecyclerView(recyclerView);
        recyclerAdapter.setAlCustomizationSettings(alCustomizationSettings);

        linearLayoutManager = new AlLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(AlLinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(recyclerAdapter);
        //recyclerView.addItemDecoration(new FooterItemDecoration(getContext(), recyclerView, R.layout.mobicom_message_list_header_footer));

        /**
         * WishTrip custom code
         **/
//        toolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
//        toolbar.setClickable(false);
        /**
         * WishTrip custom code -- END
         **/


        fabButton = (ImageButton) list.findViewById(R.id.fab_start_new);
        loading = true;
        LinearLayout individualMessageSendLayout = (LinearLayout) list.findViewById(R.id.individual_message_send_layout);
        LinearLayout extendedSendingOptionLayout = (LinearLayout) list.findViewById(R.id.extended_sending_option_layout);

        individualMessageSendLayout.setVisibility(View.GONE);
        extendedSendingOptionLayout.setVisibility(View.GONE);

        //setLoadingProgressBar();

        //View spinnerLayout = inflater.inflate(R.layout.mobicom_message_list_header_footer, null);
        //progressBar = (ProgressBar) spinnerLayout.findViewById(R.id.load_more_progressbar);
        //recyclerView.addFooterView(spinnerLayout);
        //linearLayoutManager.addView(spinnerLayout);

        View view = recyclerView.getChildAt(messageList.size());
        if (view != null) {
            progressBar = view.findViewById(R.id.load_more_progressbar);
        }

        //spinner = (ProgressBar) spinnerLayout.findViewById(R.id.spinner);
        emptyTextView = (TextView) list.findViewById(R.id.noConversations);
        emptyTextView.setTextColor(Color.parseColor(alCustomizationSettings.getNoConversationLabelTextColor().trim()));

        //startNewButton = (Button) spinnerLayout.findViewById(R.id.start_new_conversation);

        fabButton.setVisibility(alCustomizationSettings.isStartNewFloatingButton() ? View.VISIBLE : View.GONE);

        swipeLayout = (SwipeRefreshLayout) list.findViewById(R.id.swipe_container);
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView.setLongClickable(true);
        registerForContextMenu(recyclerView);

        return list;
    }

    protected View.OnClickListener startNewConversation() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MobiComKitActivityInterface) getActivity()).startContactActivityForResult();
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        /**
         * WishTrip custom code
         **/

//        if (alCustomizationSettings.isStartNewButton() || ApplozicSetting.getInstance(getContext()).isStartNewButtonVisible()) {
//            menu.findItem(R.id.start_new).setVisible(true);
//        }
//        if (alCustomizationSettings.isStartNewGroup() || ApplozicSetting.getInstance(getContext()).isStartNewGroupButtonVisible()) {
//            menu.findItem(R.id.conversations).setVisible(true);
//        }
//        if (alCustomizationSettings.isRefreshOption()) {
//            menu.findItem(R.id.refresh).setVisible(true);
//        }
//        if (alCustomizationSettings.isProfileOption()) {
//            menu.findItem(R.id.applozicUserProfile).setVisible(true);
//        }
//        if (alCustomizationSettings.isMessageSearchOption()) {
//            menu.findItem(R.id.menu_search).setVisible(true);
//        }
//        if (alCustomizationSettings.isBroadcastOption()) {
//            menu.findItem(R.id.broadcast).setVisible(true);
//        }
//        if (alCustomizationSettings.isLogoutOption()) {
//            menu.findItem(R.id.logout).setVisible(true);
//        }

        /**
         * WishTrip custom code -- END
         **/
    }

    public void addMessage(final Message message) {
        if (this.getActivity() == null) {
            return;
        }

        if(message.isIgnoreMessageAdding(getActivity())){
            return;
        }

        final Context context = getActivity();
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                message.processContactIds(context);
                Message recentMessage;
                if (message.getGroupId() != null) {
                    recentMessage = latestMessageForEachContact.get(ConversationUIService.GROUP + message.getGroupId());
                } else {
                    recentMessage = latestMessageForEachContact.get(message.getContactIds());
                }

                if (recentMessage != null && message.getCreatedAtTime() >= recentMessage.getCreatedAtTime()) {
                    messageList.remove(recentMessage);
                } else if (recentMessage != null) {
                    return;
                }
                if (message.getGroupId() != null) {
                    latestMessageForEachContact.put(ConversationUIService.GROUP + message.getGroupId(), message);
                } else {
                    latestMessageForEachContact.put(message.getContactIds(), message);
                }
                messageList.add(0, message);
                recyclerAdapter.notifyDataSetChanged();
                //listView.smoothScrollToPosition(messageList.size());
                //listView.setSelection(0);
                emptyTextView.setVisibility(View.GONE);
                emptyTextView.setText(!TextUtils.isEmpty(alCustomizationSettings.getNoConversationLabel()) ? alCustomizationSettings.getNoConversationLabel() : getResources().getString(R.string.no_conversation));
                // startQNewButton.setVisibility(View.GONE);
            }
        });
    }

    public void refreshView() {
        if (!getUserVisibleHint()) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        try {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        if (recyclerAdapter != null) {
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });

        } catch (Exception e) {
            Utils.printLog(getActivity(), "AL", "Exception while updating view .");
        }
    }


    public void updateUserInfo(final String userId) {
        if (!getUserVisibleHint()) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        if (getActivity() != null) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Message message = latestMessageForEachContact.get(userId);
                        if (message != null) {
                            int index = messageList.indexOf(message);
                            View view = recyclerView.getChildAt(index - linearLayoutManager.findFirstVisibleItemPosition());
                            Contact contact = baseContactService.getContactById(userId);
                            if (view != null && contact != null) {
                                ImageView contactImage = (ImageView) view.findViewById(R.id.contactImage);
                                TextView displayNameTextView = (TextView) view.findViewById(R.id.smReceivers);
                                displayNameTextView.setText(contact.getDisplayName());
                                recyclerAdapter.contactImageLoader.loadImage(contact, contactImage);
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (Exception ex) {
                        Utils.printLog(getActivity(), "AL", "Exception while updating view .");
                    }
                }
            });
        }

    }

    public void updateLastMessage(String keyString, String userId) {
        for (Message message : messageList) {
            if (message.getKeyString() != null && message.getKeyString().equals(keyString)) {
                List<Message> lastMessage;
                if (message.getGroupId() != null) {
                    lastMessage = messageDatabaseService.getLatestMessageByChannelKey(message.getGroupId());
                } else {
                    lastMessage = messageDatabaseService.getLatestMessage(message.getContactIds());
                }
                if (lastMessage.isEmpty()) {
                    removeConversation(message, userId);
                } else {
                    deleteMessage(lastMessage.get(0), userId);
                }
                break;
            }
        }
    }

    public void updateLastMessage(Message message) {
        if (message == null) {
            return;
        }
        List<Message> lastMessage = new ArrayList<>();
        if (message.getGroupId() != null) {
            lastMessage = messageDatabaseService.getLatestMessageByChannelKey(message.getGroupId());
        } else {
            lastMessage = messageDatabaseService.getLatestMessage(message.getContactIds());
        }
        if (lastMessage.isEmpty()) {
            removeConversation(message, message.getContactIds());
        } else {
            deleteMessage(lastMessage.get(0), message.getContactIds());
        }
    }

    public String getLatestContact() {
        if (messageList != null && !messageList.isEmpty()) {
            Message message = messageList.get(0);
            return message.getTo();
        }
        return null;
    }

    public void updateChannelName() {
        if (this.getActivity() == null) {
            return;
        }

        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    recyclerAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public void deleteMessage(final Message message, final String userId) {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Message recentMessage;
                if (message.getGroupId() != null) {
                    recentMessage = latestMessageForEachContact.get(ConversationUIService.GROUP + message.getGroupId());
                } else {
                    recentMessage = latestMessageForEachContact.get(message.getContactIds());
                }
                if (recentMessage != null && message.getCreatedAtTime() <= recentMessage.getCreatedAtTime()) {
                    if (message.getGroupId() != null) {
                        latestMessageForEachContact.put(ConversationUIService.GROUP + message.getGroupId(), message);
                    } else {
                        latestMessageForEachContact.put(message.getContactIds(), message);
                    }

                    messageList.set(messageList.indexOf(recentMessage), message);

                    recyclerAdapter.notifyDataSetChanged();
                    if (messageList.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    public void updateLatestMessage(Message message, String userId) {
        deleteMessage(message, userId);
    }

    public void removeConversation(final Message message, final String userId) {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (message.getGroupId() != null && message.getGroupId() != 0) {
                    latestMessageForEachContact.remove(ConversationUIService.GROUP + message.getGroupId());
                } else {
                    latestMessageForEachContact.remove(message.getContactIds());
                }
                messageList.remove(message);
                recyclerAdapter.notifyDataSetChanged();
                checkForEmptyConversations();
            }
        });
    }

    public void removeConversation(final Contact contact, final Integer channelKey, String response) {
        if (getActivity() == null) {
            return;
        }
        if ("success".equals(response)) {
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Message message = null;
                    if (channelKey != null && channelKey != 0) {
                        message = latestMessageForEachContact.get(ConversationUIService.GROUP + channelKey);
                    } else {
                        message = latestMessageForEachContact.get(contact.getUserId());
                    }
                    if (message == null) {
                        return;
                    }
                    messageList.remove(message);
                    if (channelKey != null && channelKey != 0) {
                        latestMessageForEachContact.remove(ConversationUIService.GROUP + channelKey);
                    } else {
                        latestMessageForEachContact.remove(contact.getUserId());
                    }
                    recyclerAdapter.notifyDataSetChanged();
                    checkForEmptyConversations();
                }
            });
        } else {
            if (!Utils.isInternetAvailable(getActivity())) {
                Toast.makeText(getActivity(), getString(R.string.you_need_network_access_for_delete), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.delete_conversation_failed), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void checkForEmptyConversations() {
        boolean isLodingConversation = (downloadConversation != null && downloadConversation.getStatus() == AsyncTask.Status.RUNNING);
        if (latestMessageForEachContact.isEmpty() && !isLodingConversation) {
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText(!TextUtils.isEmpty(alCustomizationSettings.getNoConversationLabel()) ? alCustomizationSettings.getNoConversationLabel() : getResources().getString(R.string.no_conversation));
            //startNewButton.setVisibility(applozicSetting.isStartNewButtonVisible() ? View.VISIBLE : View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            // startNewButton.setVisibility(View.GONE);
        }
    }

    public void setLoadMore(boolean loadMore) {
        this.loadMore = loadMore;
    }

    @Override
    public void onPause() {
        super.onPause();
        listIndex = linearLayoutManager.findFirstVisibleItemPosition();
        BroadcastService.currentUserId = null;
        if (recyclerView != null) {
            BroadcastService.lastIndexForChats = linearLayoutManager.findFirstVisibleItemPosition();
        }
        if (recyclerAdapter != null) {
            recyclerAdapter.contactImageLoader.setPauseWork(false);
            recyclerAdapter.channelImageLoader.setPauseWork(false);
        }
    }

    @Override
    public void onResume() {
        //Assigning to avoid notification in case if quick conversation fragment is opened....

        /**
         * WishTrip custom code
         **/

//        toolbar.setTitle(getResources().getString(R.string.chats));
//        toolbar.setSubtitle("");

        /**
         *  WishTrip custom code -- END
         **/

        BroadcastService.selectMobiComKitAll();
        super.onResume();
        if (recyclerView != null) {
            if (recyclerView.getChildCount() > listIndex) {
                recyclerView.scrollToPosition(listIndex);
            } else {
                //recyclerView.scrollToPosition(0);
            }
        }
        if (!isAlreadyLoading) {
            latestMessageForEachContact.clear();
            messageList.clear();
            downloadConversations(false, searchString);
        }
        if (swipeLayout != null) {
            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                public void onRefresh() {
                    SyncMessages syncMessages = new SyncMessages();
                    syncMessages.execute();
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //FlurryAgent.logEvent(QUICK_CONVERSATION_EVENT);
        //listView.setAdapter(conversationAdapter);
        // startNewButton.setOnClickListener(startNewConversation());
        fabButton.setOnClickListener(startNewConversation());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerAdapter != null) {
                    recyclerAdapter.contactImageLoader.setPauseWork(newState == RecyclerView.SCROLL_STATE_DRAGGING);
                    recyclerAdapter.channelImageLoader.setPauseWork(newState == RecyclerView.SCROLL_STATE_DRAGGING);
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {
                    visibleItemCount = linearLayoutManager.getChildCount();
                    totalItemCount = linearLayoutManager.getItemCount();
                    pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if (totalItemCount > previousTotalItemCount) {
                            if (!messageList.isEmpty()) {
                                loading = false;
                            }
                            previousTotalItemCount = totalItemCount;
                            currentPage++;
                        }
                    }

                    if ((totalItemCount - visibleItemCount) == 0) {
                        return;
                    }
                    if (totalItemCount <= 5) {
                        return;
                    }
                    if (loadMore && !loading && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        DownloadConversation downloadConversation = new DownloadConversation(recyclerView, false, listIndex, visibleItemCount, totalItemCount);
                        downloadConversation.setQuickConversationAdapterWeakReference(recyclerAdapter);
                        downloadConversation.setTextViewWeakReference(emptyTextView);
                        downloadConversation.setSwipeRefreshLayoutWeakReference(swipeLayout);
                        downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        loading = true;
                        loadMore = false;
                    }
                }
            }
        });
    }


    public void downloadConversations() {
        downloadConversations(false, null);
    }

    public void downloadConversations(boolean showInstruction, String searchString) {
        minCreatedAtTime = null;
        downloadConversation = new DownloadConversation(recyclerView, true, 1, 0, 0, showInstruction, searchString);
        downloadConversation.setQuickConversationAdapterWeakReference(recyclerAdapter);
        downloadConversation.setTextViewWeakReference(emptyTextView);
        downloadConversation.setSwipeRefreshLayoutWeakReference(swipeLayout);
        downloadConversation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (recyclerAdapter != null) {
            recyclerAdapter.searchString = searchString;
        }
    }

    public void updateLastSeenStatus(final String userId) {

        if (alCustomizationSettings == null) {
            return;
        }

        if (!alCustomizationSettings.isOnlineStatusMasterList()) {
            return;
        }
        if (getActivity() == null) {
            return;
        }
        if (MobiComUserPreference.getInstance(getContext()).getUserId().equals(userId)) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (recyclerAdapter != null) {
                        recyclerAdapter.notifyDataSetChanged();
                    }
                } catch (Exception ex) {
                    Utils.printLog(getActivity(), "AL", "Exception while updating online status.");
                }
            }
        });
    }

    public void updateConversationRead(final String currentId, final boolean isGroup) {
        if (getActivity() == null) {
            return;
        }
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Message message = null;
                    if (isGroup) {
                        message = latestMessageForEachContact.get(ConversationUIService.GROUP + currentId);
                    } else {
                        message = latestMessageForEachContact.get(currentId);
                    }

                    if (message != null) {
                        int index = messageList.indexOf(message);
                        if (index != -1) {
                            View view = recyclerView.getChildAt(index - linearLayoutManager.findFirstVisibleItemPosition());
                            if (view != null) {
                                TextView unreadCountTextView = (TextView) view.findViewById(R.id.unreadSmsCount);
                                unreadCountTextView.setVisibility(View.GONE);
                            }
                        }
                    }
                } catch (Exception ex) {
                    Utils.printLog(getActivity(), "AL", "Exception while updating Unread count...");
                }
            }
        });
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.searchString = newText;
        if (TextUtils.isEmpty(newText)) {
            downloadConversations(false, null);
        } else {
            downloadConversations(false, newText);
        }
        return true;
    }

    public class DownloadConversation extends AsyncTask<Void, Integer, Long> {

        private RecyclerView view;
        private int firstVisibleItem;
        private int amountVisible;
        private int totalItems;
        private boolean initial;
        private boolean showInstruction;
        private List<Message> nextMessageList = new ArrayList<Message>();
        private Context context;
        private boolean loadMoreMessages;
        private String searchString;
        private WeakReference<SwipeRefreshLayout> swipeRefreshLayoutWeakReference;
        private WeakReference<QuickConversationAdapter> quickConversationAdapterWeakReference;
        private WeakReference<TextView> textViewWeakReference;

        public void setQuickConversationAdapterWeakReference(QuickConversationAdapter quickConversationAdapterWeakReference) {
            this.quickConversationAdapterWeakReference = new WeakReference<QuickConversationAdapter>(quickConversationAdapterWeakReference);
        }

        public void setTextViewWeakReference(TextView emptyTextViewWeakReference) {
            this.textViewWeakReference = new WeakReference<TextView>(emptyTextViewWeakReference);
        }


        public void setSwipeRefreshLayoutWeakReference(SwipeRefreshLayout swipeRefreshLayout) {
            this.swipeRefreshLayoutWeakReference = new WeakReference<SwipeRefreshLayout>(swipeRefreshLayout);
        }


        public DownloadConversation(RecyclerView view, boolean initial, int firstVisibleItem, int amountVisible, int totalItems, boolean showInstruction, String searchString) {
            this.context = getActivity();
            this.view = view;
            this.initial = initial;
            this.firstVisibleItem = firstVisibleItem;
            this.amountVisible = amountVisible;
            this.totalItems = totalItems;
            this.showInstruction = showInstruction;
            this.searchString = searchString;
        }

        public DownloadConversation(RecyclerView view, boolean initial, int firstVisibleItem, int amountVisible, int totalItems) {
            this(view, initial, firstVisibleItem, amountVisible, totalItems, false, null);
            loadMoreMessages = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isAlreadyLoading = true;
            if (loadMoreMessages) {
                if (!messageList.contains(null)) {
                    messageList.add(null);
                }
                quickConversationAdapterWeakReference.get().notifyItemInserted(messageList.size() - 1);
                //progressBar.setVisibility(View.VISIBLE);
            } else {
                if (swipeRefreshLayoutWeakReference != null) {
                    final SwipeRefreshLayout swipeRefreshLayout = swipeRefreshLayoutWeakReference.get();
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setEnabled(true);
                        swipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(true);
                            }
                        });
                    }
                }
            }
        }

        protected Long doInBackground(Void... voids) {
            if (initial) {
                nextMessageList = syncCallService.getLatestMessagesGroupByPeople(searchString, MobiComUserPreference.getInstance(context).getParentGroupKey());
                if (!nextMessageList.isEmpty()) {
                    minCreatedAtTime = nextMessageList.get(nextMessageList.size() - 1).getCreatedAtTime();
                }
            } else if (!messageList.isEmpty()) {
                listIndex = firstVisibleItem;
                Long createdAt;
                if (messageList.size() >= 2 && messageList.contains(null)) {
                    createdAt = messageList.isEmpty() ? null : messageList.get(messageList.size() - 2).getCreatedAtTime();
                } else {
                    createdAt = messageList.isEmpty() ? null : messageList.get(messageList.size() - 1).getCreatedAtTime();
                }
                minCreatedAtTime = (minCreatedAtTime == null ? createdAt : Math.min(minCreatedAtTime, createdAt));
                nextMessageList = syncCallService.getLatestMessagesGroupByPeople(minCreatedAtTime, searchString, MobiComUserPreference.getInstance(context).getParentGroupKey());
            }

            return 0L;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            if (!loadMoreMessages) {
                if (swipeRefreshLayoutWeakReference != null) {
                    final SwipeRefreshLayout swipeRefreshLayout = swipeRefreshLayoutWeakReference.get();
                    swipeRefreshLayout.setEnabled(true);
                    swipeRefreshLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }

            if (!TextUtils.isEmpty(searchString)) {
                messageList.clear();
                latestMessageForEachContact.clear();

            }

            for (Message currentMessage : nextMessageList) {
                if (currentMessage.isSentToMany()) {
                    continue;
                }
                Message recentSms;
                if (currentMessage.getGroupId() != null) {
                    recentSms = latestMessageForEachContact.get(ConversationUIService.GROUP + currentMessage.getGroupId());
                } else {
                    recentSms = latestMessageForEachContact.get(currentMessage.getContactIds());
                }

                if (recentSms != null) {
                    if (currentMessage.getCreatedAtTime() >= recentSms.getCreatedAtTime()) {
                        if (currentMessage.getGroupId() != null) {
                            latestMessageForEachContact.put(ConversationUIService.GROUP + currentMessage.getGroupId(), currentMessage);
                        } else {
                            latestMessageForEachContact.put(currentMessage.getContactIds(), currentMessage);
                        }
                        messageList.remove(recentSms);
                        messageList.add(currentMessage);
                    }
                } else {
                    if (currentMessage.getGroupId() != null) {
                        latestMessageForEachContact.put(ConversationUIService.GROUP + currentMessage.getGroupId(), currentMessage);
                    } else {
                        latestMessageForEachContact.put(currentMessage.getContactIds(), currentMessage);
                    }

                    messageList.add(currentMessage);
                }
            }
            if (loadMoreMessages) {
                if (messageList.contains(null)) {
                    messageList.remove(null);
                }
                quickConversationAdapterWeakReference.get().notifyDataSetChanged();
                //progressBar.setVisibility(View.GONE);
            }
            if (quickConversationAdapterWeakReference != null) {
                QuickConversationAdapter quickConversationAdapter = quickConversationAdapterWeakReference.get();
                if (quickConversationAdapter != null) {
                    quickConversationAdapter.notifyDataSetChanged();
                }
            }
            if (initial) {
                if (textViewWeakReference != null) {
                    TextView emptyTextView = textViewWeakReference.get();
                    if (emptyTextView != null) {
                        emptyTextView.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
                        if (!TextUtils.isEmpty(searchString) && messageList.isEmpty()) {
                            emptyTextView.setText(!TextUtils.isEmpty(alCustomizationSettings.getNoSearchFoundForChatMessages()) ? alCustomizationSettings.getNoSearchFoundForChatMessages() : getResources().getString(R.string.search_not_found_for_messages));
                        } else if (TextUtils.isEmpty(searchString) && messageList.isEmpty()) {
                            emptyTextView.setText(!TextUtils.isEmpty(alCustomizationSettings.getNoConversationLabel()) ? alCustomizationSettings.getNoConversationLabel() : getResources().getString(R.string.no_conversation));
                        }
                    }
                }
                if (!messageList.isEmpty()) {
                    if (recyclerView != null) {
                        if (recyclerAdapter.getItemCount() > BroadcastService.lastIndexForChats) {
                            recyclerView.scrollToPosition(BroadcastService.lastIndexForChats);
                            BroadcastService.lastIndexForChats = 0;
                        } else {
                            recyclerView.scrollToPosition(0);
                        }
                    }
                }
            } else {
                if (!loadMoreMessages) {
                    recyclerView.scrollToPosition(firstVisibleItem);
                }
            }
            if (!nextMessageList.isEmpty()) {
                loadMore = true;
            }
            isAlreadyLoading = false;
        }
    }

    private class SyncMessages extends AsyncTask<Void, Integer, Long> {
        SyncMessages() {
        }

        @Override
        protected Long doInBackground(Void... params) {
            syncCallService.syncMessages(null);
            return 1l;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            swipeLayout.setRefreshing(false);
        }
    }
}