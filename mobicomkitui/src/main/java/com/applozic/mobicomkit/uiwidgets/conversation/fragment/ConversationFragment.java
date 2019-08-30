package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.api.conversation.SyncCallService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.uiwidgets.ApplozicApplication;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.MultimediaOptionsGridView;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.MobicomMultimediaPopupAdapter;
import com.applozic.mobicommons.commons.core.utils.LocationUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ConversationFragment extends MobiComConversationFragment implements SearchListFragment {

    public static final int ATTCHMENT_OPTIONS = 6;
    private static final String TAG = "ConversationFragment";
    private final static String CONTACT = "CONTACT";
    private final static String CHANNEL = "CHANNEL";
    private final static String CONVERSATION_ID = "CONVERSATION_ID";
    private final static String SEARCH_STRING = "SEARCH_STRING";
    InputMethodManager inputMethodManager;
    Bundle bundle;
    private MultimediaOptionsGridView popupGrid;
    private List<String> attachmentKey = new ArrayList<>();
    private List<String> attachmentText = new ArrayList<>();
    private List<String> attachmentIcon = new ArrayList<>();

    public static ConversationFragment newInstance(Contact contact, Channel channel, Integer conversationId, String searchString) {
        ConversationFragment f = new ConversationFragment();
        Bundle args = new Bundle();
        if (contact != null) {
            args.putSerializable(CONTACT, contact);
        }
        if (channel != null) {
            args.putSerializable(CHANNEL, channel);
        }
        if (conversationId != null) {
            args.putInt(CONVERSATION_ID, conversationId);
        }
        args.putString(SEARCH_STRING, searchString);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.messageIntentClass = MessageIntentService.class;
        bundle = getArguments();
        if (bundle != null) {
            contact = (Contact) bundle.getSerializable(CONTACT);
            channel = (Channel) bundle.getSerializable(CHANNEL);
            currentConversationId = bundle.getInt(CONVERSATION_ID);
            searchString = bundle.getString(SEARCH_STRING);
            if (searchString != null) {
                SyncCallService.refreshView = true;
            }
        }
    }

    public void attachLocation(Location mCurrentLocation) {
        String address = LocationUtils.getAddress(getActivity(), mCurrentLocation);
        if (!TextUtils.isEmpty(address)) {
            address = "Address: " + address + "\n";
        } else {
            address = "";
        }
        this.messageEditText.setText(address + "http://maps.google.com/?q=" + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.title = getResources().getString(R.string.chats);
        this.conversationService = new MobiComConversationService(getActivity());
        hideExtendedSendingOptionLayout = true;

        View view = super.onCreateView(inflater, container, savedInstanceState);
        populateAttachmentOptions();

        if (alCustomizationSettings.isHideAttachmentButton()) {

            attachButton.setVisibility(View.GONE);
            messageEditText.setPadding(20, 0, 0, 0);
        }
        sendType.setSelection(1);

        messageEditText.setHint(R.string.enter_message_hint);

        multimediaPopupGrid.setVisibility(View.GONE);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.secret_message_timer_array, R.layout.mobiframework_custom_spinner);
        adapter.setDropDownViewResource(R.layout.mobiframework_custom_spinner);

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);

        messageEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multimediaPopupGrid.setVisibility(View.GONE);
            }
        });


        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (contact != null && !contact.isBlocked() || channel != null) {
                    if (attachmentLayout.getVisibility() == View.VISIBLE) {
                        Toast.makeText(getActivity(), R.string.select_file_count_limit, Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                if (channel != null) {
                    if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                        String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                        if (!TextUtils.isEmpty(userId)) {
                            Contact withUserContact = appContactService.getContactById(userId);
                            if (withUserContact.isBlocked()) {
                                userBlockDialog(false, withUserContact, true);
                            } else {
                                processAttachButtonClick(view);
                            }
                        }
                    } else {
                        processAttachButtonClick(view);
                    }
                } else if (contact != null) {
                    if (contact.isBlocked()) {
                        userBlockDialog(false, contact, false);
                    } else {
                        processAttachButtonClick(view);
                    }
                }
            }
        });
        return view;
    }

    @Override
    protected void processMobiTexterUserCheck() {

    }

    public void updateTitle() {
        //((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(ApplozicApplication.TITLE);
        super.updateTitle();
    }

    public void hideMultimediaOptionGrid() {
        if (multimediaPopupGrid.getVisibility() == View.VISIBLE) {
            multimediaPopupGrid.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            recyclerDetailConversationAdapter.getFilter().filter(null);
        } else {
            recyclerDetailConversationAdapter.getFilter().filter(newText);
        }
        return true;
    }


    void processAttachButtonClick(View view) {
        MobicomMultimediaPopupAdapter mobicomMultimediaPopupAdapter = new MobicomMultimediaPopupAdapter(getActivity(), attachmentIcon, attachmentText);
        mobicomMultimediaPopupAdapter.setAlCustomizationSettings(alCustomizationSettings);
        multimediaPopupGrid.setAdapter(mobicomMultimediaPopupAdapter);

        int noOfColumn = (attachmentKey.size() == ATTCHMENT_OPTIONS) ? 3 : attachmentKey.size();
        multimediaPopupGrid.setNumColumns(noOfColumn);
        multimediaPopupGrid.setVisibility(View.VISIBLE);
        if (inputMethodManager.isActive()) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        MultimediaOptionsGridView itemClickHandler = new MultimediaOptionsGridView(getActivity(), multimediaPopupGrid);
        itemClickHandler.setMultimediaClickListener(attachmentKey);

    }

    private void populateAttachmentOptions() {

        if (attachmentKey != null && attachmentKey.size() > 0) {
            attachmentKey.clear();
            attachmentText.clear();
            attachmentIcon.clear();
        }

        String[] allKeys = getResources().getStringArray(R.array.multimediaOptions_without_price_key);
        String[] allValues = getResources().getStringArray(R.array.multimediaOptions_without_price_text);
        String[] allIcons = getResources().getStringArray(R.array.multimediaOptionIcons_without_price);

        Map<String, Boolean> maps = alCustomizationSettings.getAttachmentOptions();

        for (int index = 0; index < allKeys.length; index++) {

            String key = allKeys[index];
            if (maps == null || maps.get(key) == null || maps.get(key)) {
                attachmentKey.add(key);
                attachmentText.add(allValues[index]);
                attachmentIcon.add(allIcons[index]);
            }
        }
    }

    public void reload() {

        try {
            StringBuffer stringBufferTitle = new StringBuffer();
            if (contact != null) {
                Contact updatedInfoContact = appContactService.getContactById(contact.getContactIds());
                if (updatedInfoContact.isDeleted()) {
                    Utils.toggleSoftKeyBoard(getActivity(), true);
                    bottomlayoutTextView.setText(R.string.user_has_been_deleted_text);
                    userNotAbleToChatLayout.setVisibility(View.VISIBLE);
                    individualMessageSendLayout.setVisibility(View.GONE);
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
                }
                if (updatedInfoContact != null && (!TextUtils.isEmpty(contact.getDisplayName())) && (!contact.getDisplayName().equals(updatedInfoContact.getDisplayName()))) {
                    stringBufferTitle.append(updatedInfoContact.getDisplayName());
                }
            } else if (channel != null) {
                if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                    String userId = ChannelService.getInstance(getActivity()).getGroupOfTwoReceiverUserId(channel.getKey());
                    if (!TextUtils.isEmpty(userId)) {
                        Contact withUserContact = appContactService.getContactById(userId);
                        if (withUserContact != null && (!TextUtils.isEmpty(contact.getDisplayName())) && (!contact.getDisplayName().equals(withUserContact.getDisplayName()))) {
                            stringBufferTitle.append(withUserContact.getDisplayName());
                        }
                    }
                }
            }
            if (!TextUtils.isEmpty(stringBufferTitle)) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(stringBufferTitle.toString());
            }
            recyclerDetailConversationAdapter.refreshContactData();
            recyclerDetailConversationAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}