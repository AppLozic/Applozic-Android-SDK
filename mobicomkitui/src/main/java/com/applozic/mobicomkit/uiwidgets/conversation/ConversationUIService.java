package com.applozic.mobicomkit.uiwidgets.conversation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.RegisteredUsersAsyncTask;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.ApplozicMqttIntentService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.contact.ContactService;
import com.applozic.mobicomkit.feed.RegisteredUsersApiResponse;
import com.applozic.mobicomkit.feed.TopicDetail;
import com.applozic.mobicomkit.listners.AlChannelListener;
import com.applozic.mobicomkit.listners.AlContactListener;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelDeleteTask;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelLeaveMember;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComAttachmentSelectorActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.ConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MessageInfoFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MultimediaOptionFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.sign_to_text;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.text_to_sign;
import com.applozic.mobicomkit.uiwidgets.people.activity.MobiComKitPeopleActivity;
import com.applozic.mobicomkit.uiwidgets.people.fragment.UserProfileFragment;
import com.applozic.mobicommons.commons.core.utils.LocationInfo;
import com.applozic.mobicommons.commons.core.utils.Support;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;

import java.io.File;
import java.util.ArrayList;


public class ConversationUIService {

    public static final int REQUEST_CODE_CONTACT_GROUP_SELECTION = 1011;
    public static final String CONVERSATION_FRAGMENT = "ConversationFragment";
    public static final String MESSGAE_INFO_FRAGMENT = "messageInfoFagment";
    public static final String USER_PROFILE_FRAMENT = "userProfilefragment";
    public static final String PARENT_CLIENT_GROUP_ID = "parentClientGroupId";
    public static final String PARENT_GROUP_KEY = "parentGroupKey";
    public static final String QUICK_CONVERSATION_FRAGMENT = "QuickConversationFragment";
    public static final String FORWARD_MESSAGE = "forwardMessage";
    public static final String CLIENT_GROUP_ID = "clientGroupId";
    public static final String DISPLAY_NAME = "displayName";
    public static final String TAKE_ORDER = "takeOrder";
    public static final String USER_ID = "userId";
    public static final String GROUP_ID = "groupId";
    public static final String GROUP_ID_LIST_CONTACTS = "groupIdListContacts";
    public static final String GROUP_NAME_LIST_CONTACTS = "groupIdNameContacts";
    public static final String GROUP_NAME = "groupName";
    public static final String FIRST_TIME_MTEXTER_FRIEND = "firstTimeMTexterFriend";
    public static final String CONTACT_ID = "contactId";
    public static final String CONTEXT_BASED_CHAT = "contextBasedChat";
    public static final String FROM_GROUP_DELETE = "fromGroupDelete";
    public static final String CONTACT_NUMBER = "contactNumber";
    public static final String APPLICATION_ID = "applicationId";
    public static final String DEFAULT_TEXT = "defaultText";
    public static final String FINAL_PRICE_TEXT = "Final agreed price ";
    public static final String PRODUCT_TOPIC_ID = "topicId";
    public static final String PRODUCT_IMAGE_URL = "productImageUrl";
    public static final String CONTACT = "CONTACT";
    public static final String GROUP = "group-";
    public static final String SUCCESS = "success";
    public static final String SEARCH_STRING = "searchString";
    public static final String CONVERSATION_ID = "CONVERSATION_ID";
    public static final String TOPIC_ID = "TOPIC_ID";
    private static final String TAG = "ConversationUIService";
    private static final String APPLICATION_KEY_META_DATA = "com.applozic.application.key";
    FileClientService fileClientService;
    private FragmentActivity fragmentActivity;
    private BaseContactService baseContactService;
    private MobiComUserPreference userPreference;
    private Conversation conversation;
    private TopicDetail topicDetailsParcelable;
    private Contact contact;
    private NotificationManager notificationManager;
    private boolean isActionMessageHidden;

    public ConversationUIService(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
        this.baseContactService = new AppContactService(fragmentActivity);
        this.userPreference = MobiComUserPreference.getInstance(fragmentActivity);
        this.notificationManager = (NotificationManager) fragmentActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        this.fileClientService = new FileClientService(fragmentActivity);
        isActionMessageHidden = ApplozicClient.getInstance(fragmentActivity).isActionMessagesHidden();
    }

    public MobiComQuickConversationFragment getQuickConversationFragment() {

        MobiComQuickConversationFragment quickConversationFragment = (MobiComQuickConversationFragment) UIService.getFragmentByTag(fragmentActivity, QUICK_CONVERSATION_FRAGMENT);

        if (quickConversationFragment == null) {
            quickConversationFragment = new MobiComQuickConversationFragment();
            ConversationActivity.addFragment(fragmentActivity, quickConversationFragment, QUICK_CONVERSATION_FRAGMENT);
        }
        return quickConversationFragment;
    }

    public ConversationFragment getConversationFragment() {

        ConversationFragment conversationFragment = (ConversationFragment) UIService.getFragmentByTag(fragmentActivity, CONVERSATION_FRAGMENT);

        if (conversationFragment == null) {
            Contact contact = ((ConversationActivity) fragmentActivity).getContact();
            Channel channel = ((ConversationActivity) fragmentActivity).getChannel();
            Integer conversationId = ((ConversationActivity) fragmentActivity).getConversationId();
            conversationFragment = ConversationFragment.newInstance(contact, channel, conversationId, null);
            ConversationActivity.addFragment(fragmentActivity, conversationFragment, CONVERSATION_FRAGMENT);
        }
        return conversationFragment;
    }

    public void onQuickConversationFragmentItemClick(View view, Contact contact) {
        TextView textView = (TextView) view.findViewById(R.id.unreadSmsCount);
        textView.setVisibility(View.GONE);
        openConversationFragment(contact, null, null);
    }

    public void openConversationFragment(final Contact contact, final Integer conversationId, final String searchString) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ConversationFragment conversationFragment = (ConversationFragment) UIService.getFragmentByTag(fragmentActivity, CONVERSATION_FRAGMENT);
                if (conversationFragment == null) {
                    conversationFragment = ConversationFragment.newInstance(contact, null, conversationId, searchString);
                    ((MobiComKitActivityInterface) fragmentActivity).addFragment(conversationFragment);
                } else {
                    UserProfileFragment userProfileFragment = (UserProfileFragment) UIService.getFragmentByTag(fragmentActivity, ConversationUIService.USER_PROFILE_FRAMENT);
                    MessageInfoFragment messageInfoFragment = (MessageInfoFragment) UIService.getFragmentByTag(fragmentActivity, ConversationUIService.MESSGAE_INFO_FRAGMENT);
                    if (userProfileFragment != null || messageInfoFragment != null) {
                        if (fragmentActivity.getSupportFragmentManager() != null) {
                            fragmentActivity.getSupportFragmentManager().popBackStackImmediate();
                        }
                    }
                    conversationFragment.loadConversation(contact, conversationId, searchString);
                }
            }
        });
    }

    public void openConversationFragment(final Channel channel, final Integer conversationId, final String searchString) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ConversationFragment conversationFragment = (ConversationFragment) UIService.getFragmentByTag(fragmentActivity, CONVERSATION_FRAGMENT);
                if (conversationFragment == null) {
                    conversationFragment = ConversationFragment.newInstance(null, channel, conversationId, searchString);
                    ((MobiComKitActivityInterface) fragmentActivity).addFragment(conversationFragment);
                } else {
                    UserProfileFragment userProfileFragment = (UserProfileFragment) UIService.getFragmentByTag(fragmentActivity, ConversationUIService.USER_PROFILE_FRAMENT);
                    MessageInfoFragment messageInfoFragment = (MessageInfoFragment) UIService.getFragmentByTag(fragmentActivity, ConversationUIService.MESSGAE_INFO_FRAGMENT);
                    if (userProfileFragment != null || messageInfoFragment != null) {
                        if (fragmentActivity.getSupportFragmentManager() != null) {
                            fragmentActivity.getSupportFragmentManager().popBackStackImmediate();
                        }
                    }
                    conversationFragment.loadConversation(channel, conversationId, searchString);
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        try {
            if ((requestCode == MultimediaOptionFragment.REQUEST_CODE_ATTACH_PHOTO ||
                    requestCode == MultimediaOptionFragment.REQUEST_CODE_TAKE_PHOTO)
                    && resultCode == Activity.RESULT_OK) {
                Uri selectedFileUri = (intent == null ? null : intent.getData());
                File file = null;
                if (selectedFileUri == null) {
                    file = ((ConversationActivity) fragmentActivity).getFileObject();
                    selectedFileUri = ((ConversationActivity) fragmentActivity).getCapturedImageUri();
                }

                if (selectedFileUri != null) {
                    selectedFileUri = ((ConversationActivity) fragmentActivity).getCapturedImageUri();
                    file = ((ConversationActivity) fragmentActivity).getFileObject();
                }
                MediaScannerConnection.scanFile(fragmentActivity,
                        new String[]{file.getAbsolutePath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
                if (getConversationFragment() != null) {
                    getConversationFragment().loadFile(selectedFileUri, file);
                }
                Utils.printLog(fragmentActivity, TAG, "File uri: " + selectedFileUri);
            }

            if (requestCode == REQUEST_CODE_CONTACT_GROUP_SELECTION && resultCode == Activity.RESULT_OK) {
                checkForStartNewConversation(intent);
            }
            if (requestCode == MultimediaOptionFragment.REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY && resultCode == Activity.RESULT_OK) {

                Uri selectedFilePath = ((ConversationActivity) fragmentActivity).getVideoFileUri();

                File file = ((ConversationActivity) fragmentActivity).getFileObject();

                if (!(file != null && file.exists())) {
                    FileUtils.getLastModifiedFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera/").renameTo(file);
                }

                if (selectedFilePath != null && getConversationFragment() != null) {
                    getConversationFragment().loadFile(selectedFilePath, file);
                    getConversationFragment().sendMessage("", Message.ContentType.VIDEO_MSG.getValue());
                }
            }

            if (requestCode == MultimediaOptionFragment.REQUEST_CODE_CONTACT_SHARE && resultCode == Activity.RESULT_OK) {

                try {
                    File vCradFile = new ContactService(fragmentActivity).vCard(intent.getData());

                    if (vCradFile != null && getConversationFragment() != null) {
                        getConversationFragment().sendMessage(Message.ContentType.CONTACT_MSG.getValue(), vCradFile.getAbsolutePath());
                    }

                } catch (Exception e) {
                    Toast.makeText(fragmentActivity, fragmentActivity.getString(R.string.applozic_failed_to_load_contact), Toast.LENGTH_SHORT).show();
                    Log.e("Exception::", "Exception", e);
                }
            }
            if (requestCode == MultimediaOptionFragment.REQUEST_MULTI_ATTCAHMENT && resultCode == Activity.RESULT_OK) {

                ArrayList<Uri> attachmentList = intent.getParcelableArrayListExtra(MobiComAttachmentSelectorActivity.MULTISELECT_SELECTED_FILES);
                String messageText = intent.getStringExtra(MobiComAttachmentSelectorActivity.MULTISELECT_MESSAGE);

                //TODO: check performance, we might need to put in each posting in separate thread.

                if (getConversationFragment() != null) {
                    for (Uri info : attachmentList) {
                        getConversationFragment().sendMessage(messageText, Message.ContentType.ATTACHMENT.getValue(), info.toString());
                    }
                }

            }

            if (requestCode == MultimediaOptionFragment.REQUEST_CODE_SEND_LOCATION && resultCode == Activity.RESULT_OK) {
                Double latitude = intent.getDoubleExtra("latitude", 0);
                Double longitude = intent.getDoubleExtra("longitude", 0);
                //TODO: put your location(lat/lon ) in constructor.
                LocationInfo info = new LocationInfo(latitude, longitude);
                String locationInfo = GsonUtils.getJsonFromObject(info, LocationInfo.class);
                sendLocation(locationInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteConversationThread(final Contact contact, final Channel channel) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragmentActivity).
                setPositiveButton(R.string.delete_conversation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteConversationAsyncTask(new MobiComConversationService(fragmentActivity), contact, channel, null, fragmentActivity).execute();

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        String name = "";
        if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(fragmentActivity).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = baseContactService.getContactById(userId);
                    name = withUserContact.getDisplayName();
                }
            } else {
                name = ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(fragmentActivity).getUserId());
            }
        } else if (contact != null) {
            name = contact.getDisplayName();
        }
        alertDialog.setTitle(fragmentActivity.getString(R.string.dialog_delete_conversation_title).replace("[name]", name));
        alertDialog.setMessage(fragmentActivity.getString(R.string.dialog_delete_conversation_confir).replace("[name]", name));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void deleteGroupConversation(final Channel channel) {

        if (!Utils.isInternetAvailable(fragmentActivity)) {
            showToastMessage(fragmentActivity.getString(R.string.you_dont_have_any_network_access_info));
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragmentActivity).
                setPositiveButton(R.string.channel_deleting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final ProgressDialog progressDialog = ProgressDialog.show(fragmentActivity, "",
                                fragmentActivity.getString(R.string.deleting_channel_user), true);
                        ApplozicChannelDeleteTask.TaskListener channelDeleteTask = new ApplozicChannelDeleteTask.TaskListener() {
                            @Override
                            public void onSuccess(String response) {
                                Log.i(TAG, "Channel deleted response:" + response);

                            }

                            @Override
                            public void onFailure(String response, Exception exception) {
                                showToastMessage(fragmentActivity.getString(Utils.isInternetAvailable(fragmentActivity) ? R.string.applozic_server_error : R.string.you_dont_have_any_network_access_info));
                            }

                            @Override
                            public void onCompletion() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                            }
                        };
                        ApplozicChannelDeleteTask applozicChannelDeleteTask = new ApplozicChannelDeleteTask(fragmentActivity, channelDeleteTask, channel);
                        applozicChannelDeleteTask.execute((Void) null);
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setMessage(fragmentActivity.getString(R.string.delete_channel_messages_and_channel_info).replace(fragmentActivity.getString(R.string.group_name_info), channel.getName()).replace(fragmentActivity.getString(R.string.groupType_info), Channel.GroupType.BROADCAST.getValue().equals(channel.getType()) ? fragmentActivity.getString(R.string.broadcast_string) : fragmentActivity.getString(R.string.group_string)));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void channelLeaveProcess(final Channel channel) {
        if (!Utils.isInternetAvailable(fragmentActivity)) {
            showToastMessage(fragmentActivity.getString(R.string.you_dont_have_any_network_access_info));
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragmentActivity).
                setPositiveButton(R.string.channel_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ApplozicChannelLeaveMember.ChannelLeaveMemberListener applozicLeaveMemberListener = new ApplozicChannelLeaveMember.ChannelLeaveMemberListener() {
                            @Override
                            public void onSuccess(String response, Context context) {
                            }

                            @Override
                            public void onFailure(String response, Exception e, Context context) {
                                showToastMessage(fragmentActivity.getString(Utils.isInternetAvailable(fragmentActivity) ? R.string.applozic_server_error : R.string.you_dont_have_any_network_access_info));
                            }
                        };
                        ApplozicChannelLeaveMember applozicChannelLeaveMember = new ApplozicChannelLeaveMember(fragmentActivity, channel.getKey(), MobiComUserPreference.getInstance(fragmentActivity).getUserId(), applozicLeaveMemberListener);
                        applozicChannelLeaveMember.setEnableProgressDialog(true);
                        applozicChannelLeaveMember.execute((Void) null);

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setMessage(fragmentActivity.getString(R.string.exit_channel_message_info).replace(fragmentActivity.getString(R.string.group_name_info), channel.getName()).replace(fragmentActivity.getString(R.string.groupType_info), Channel.GroupType.BROADCAST.getValue().equals(channel.getType()) ? fragmentActivity.getString(R.string.broadcast_string) : fragmentActivity.getString(R.string.group_string)));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    public void updateLatestMessage(Message message, String formattedContactNumber) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        if (getQuickConversationFragment() != null) {
            getQuickConversationFragment().updateLatestMessage(message, formattedContactNumber);
        }
    }

    public void removeConversation(Message message, String formattedContactNumber) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        if (getQuickConversationFragment() != null) {
            getQuickConversationFragment().removeConversation(message, formattedContactNumber);
        }
    }

    public void addMessage(Message message) {
        if (message.isUpdateMessage() || !message.getHidden()) {
            if (!BroadcastService.isQuick()) {
                return;
            }

            MobiComQuickConversationFragment fragment = (MobiComQuickConversationFragment) UIService.getFragmentByTag(fragmentActivity, QUICK_CONVERSATION_FRAGMENT);
            if (fragment != null) {
                if (message.isHidden()) {
                    fragment.refreshView();
                } else {
                    fragment.addMessage(message);
                }
            }
        }
    }

    public void updateLastMessage(String keyString, String userId) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        if (getQuickConversationFragment() != null) {
            getQuickConversationFragment().updateLastMessage(keyString, userId);
        }
    }

    public boolean isBroadcastedToGroup(Integer channelKey) {
        if (!BroadcastService.isIndividual()) {
            return false;
        }
        if (getConversationFragment() != null) {
            return getConversationFragment().isBroadcastedToChannel(channelKey);
        }
        return false;
    }

    public void syncMessages(Message message, String keyString) {
        if (!message.isHidden() && !message.isVideoNotificationMessage()) {
            if (BroadcastService.isIndividual()) {
                ConversationFragment conversationFragment = getConversationFragment();
                if (conversationFragment != null && conversationFragment.isMsgForConversation(message)
                        && !Message.GroupMessageMetaData.TRUE.getValue().equals(message.getMetaDataValueForKey(Message.GroupMessageMetaData.HIDE_KEY.getValue()))) {
                    conversationFragment.addMessage(message);
                }
            }

            if (!Message.MetaDataType.ARCHIVE.getValue().equals(message.getMetaDataValueForKey(Message.MetaDataType.KEY.getValue()))
                    || !(isActionMessageHidden && message.isActionMessage())) {
                updateLastMessage(message);
            }
        }
    }

    public void updateLastMessage(Message message) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        if (getQuickConversationFragment() != null) {
            getQuickConversationFragment().updateLastMessage(message);
        }
    }

    public void downloadConversations(boolean showInstruction) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        if (getQuickConversationFragment() != null) {
            getQuickConversationFragment().downloadConversations(showInstruction, null);
        }
    }

    public void setLoadMore(boolean loadMore) {
        if (!BroadcastService.isQuick()) {
            return;
        }
        if (getQuickConversationFragment() != null) {
            getQuickConversationFragment().setLoadMore(loadMore);
        }
    }

    public void updateMessageKeyString(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        String userId = message.getContactIds();
        ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null && !TextUtils.isEmpty(userId) && conversationFragment.getContact() != null && userId.equals(conversationFragment.getContact().getUserId()) ||
                conversationFragment.getCurrentChannelKey(message.getGroupId())) {
            conversationFragment.updateMessageKeyString(message);
        }
    }

    public void deleteMessage(String keyString, String userId) {
        updateLastMessage(keyString, userId);
        if (BroadcastService.isIndividual() && getConversationFragment() != null) {
            getConversationFragment().deleteMessageFromDeviceList(keyString);
        }
    }

    public void updateLastSeenStatus(String contactId) {
        if (BroadcastService.isQuick() && getQuickConversationFragment() != null) {
            getQuickConversationFragment().updateLastSeenStatus(contactId);
            return;
        }
        if (BroadcastService.isIndividual()) {
            final ConversationFragment conversationFragment = getConversationFragment();
            if (conversationFragment != null && conversationFragment.getContact() != null && contactId.equals(conversationFragment.getContact().getContactIds()) || conversationFragment.getChannel() != null) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        conversationFragment.updateLastSeenStatus();
                    }
                });
                thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();
            }
        }
    }

    public void updateDeliveryStatusForContact(String contactId) {
        updateStatus(contactId, false);
    }

    public void updateReadStatusForContact(String contactId) {
        updateStatus(contactId, true);
    }

    private void updateStatus(String contactId, boolean markRead) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null && !TextUtils.isEmpty(contactId) && conversationFragment.getContact() != null && contactId.equals(conversationFragment.getContact().getContactIds())) {
            conversationFragment.updateDeliveryStatusForAllMessages(markRead);
        }
    }

    public void updateDeliveryStatus(Message message, String formattedContactNumber) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment != null && conversationFragment.isMessageForCurrentConversation(message)) {
            conversationFragment.updateDeliveryStatus(message);
        }
    }

    public void deleteConversation(Contact contact, Integer channelKey, String response) {
        if (BroadcastService.isIndividual()) {
            if ("success".equals(response) && getConversationFragment() != null) {
                getConversationFragment().clearList();
            } else {
                if (!Utils.isInternetAvailable(fragmentActivity)) {
                    Toast.makeText(fragmentActivity, fragmentActivity.getString(R.string.you_need_network_access_for_delete), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(fragmentActivity, fragmentActivity.getString(R.string.delete_conversation_failed), Toast.LENGTH_SHORT).show();
                }
            }

        }
        if (BroadcastService.isQuick() && getQuickConversationFragment() != null) {
            getQuickConversationFragment().removeConversation(contact, channelKey, response);
        }
    }

    public void updateUploadFailedStatus(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        if (getConversationFragment() != null) {
            getConversationFragment().updateUploadFailedStatus(message);
        }
    }

    public void updateDownloadFailed(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        if (getConversationFragment() != null) {
            getConversationFragment().downloadFailed(message);
        }
    }

    public void updateDownloadStatus(Message message) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        if (getConversationFragment() != null) {
            getConversationFragment().updateDownloadStatus(message);
        }
    }

    public void updateChannelName() {
        if (BroadcastService.isQuick() && getQuickConversationFragment() != null) {
            getQuickConversationFragment().updateChannelName();
        }
    }

    public void updateTypingStatus(String userId, String isTypingStatus) {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        ConversationFragment conversationFragment = getConversationFragment();
        Utils.printLog(fragmentActivity, TAG, "Received typing status for: " + userId);
        if (conversationFragment != null && conversationFragment.getContact() != null && userId.equals(conversationFragment.getContact().getContactIds()) || conversationFragment.getChannel() != null) {
            conversationFragment.updateUserTypingStatus(userId, isTypingStatus);
        }

    }

    public void updateChannelSync(boolean isMetaDataUpdate) {
        if (BroadcastService.isChannelInfo()) {
            BroadcastService.sendUpdateGroupInfoBroadcast(fragmentActivity, BroadcastService.INTENT_ACTIONS.UPDATE_GROUP_INFO.toString());
        }
        if (BroadcastService.isIndividual() && getConversationFragment() != null) {
            if (!isMetaDataUpdate) {
                getConversationFragment().updateChannelTitleAndSubTitle();
            } else {
                getConversationFragment().updateContextBasedGroup();
            }
        }
    }

    public void updateChannelSync() {
        updateChannelSync(false);
    }

    public void updateTitleAndSubtitle() {
        if (!BroadcastService.isIndividual()) {
            return;
        }
        if (BroadcastService.isIndividual() && getConversationFragment() != null) {
            getConversationFragment().updateTitleForOpenGroup();
        }
    }

    public void updateUserInfo(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }

        if (BroadcastService.isQuick() && getQuickConversationFragment() != null) {
            getQuickConversationFragment().updateUserInfo(userId);
            return;
        }
        if (userId.equals(BroadcastService.currentUserProfileUserId)) {
            UserProfileFragment userProfileFragment = (UserProfileFragment) UIService.getFragmentByTag(fragmentActivity, ConversationUIService.USER_PROFILE_FRAMENT);
            if (userProfileFragment != null && userId.equals(BroadcastService.currentUserProfileUserId)) {
                userProfileFragment.refreshContactData();
            }
        }
        if (BroadcastService.isIndividual()) {
            ConversationFragment conversationFragment = getConversationFragment();
            if (conversationFragment != null && conversationFragment.getContact() != null && userId.equals(conversationFragment.getContact().getContactIds()) || conversationFragment.getChannel() != null) {
                conversationFragment.reload();
            }
        }

    }


    public void updateConversationRead(String currentId, boolean isGroup) {
        if (TextUtils.isEmpty(currentId)) {
            return;
        }
        if (!BroadcastService.isIndividual()) {
            notificationManager.cancel(currentId.hashCode());
        }
        if (BroadcastService.isQuick() && getQuickConversationFragment() != null) {
            getQuickConversationFragment().updateConversationRead(currentId, isGroup);
        }
    }

    public void startContactActivityForResult(Intent intent, Message message, String messageContent, String[] userIdArray) {
        if (message != null) {
            intent.putExtra(MobiComKitPeopleActivity.FORWARD_MESSAGE, GsonUtils.getJsonFromObject(message, message.getClass()));
        }
        if (messageContent != null) {
            intent.putExtra(MobiComKitPeopleActivity.SHARED_TEXT, messageContent);
        }
        if (userIdArray != null) {
            intent.putExtra(MobiComKitPeopleActivity.USER_ID_ARRAY, userIdArray);
        }

        fragmentActivity.startActivityForResult(intent, REQUEST_CODE_CONTACT_GROUP_SELECTION);
    }

    public void startContactActivityForResult() {
        startContactActivityForResult(null, null);
    }

    public void startContactActivityForResult(final Message message, final String messageContent) {
        AlCustomizationSettings alCustomizationSettings;
        String jsonString = FileUtils.loadSettingsJsonFile(fragmentActivity.getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        if (alCustomizationSettings.getTotalOnlineUsers() > 0 && Utils.isInternetAvailable(fragmentActivity)) {
            processLoadUsers(false, message, messageContent, alCustomizationSettings.getTotalRegisteredUserToFetch(), alCustomizationSettings.getTotalOnlineUsers());
        } else if (alCustomizationSettings.getTotalRegisteredUserToFetch() > 0 && (alCustomizationSettings.isRegisteredUserContactListCall() || ApplozicSetting.getInstance(fragmentActivity).isRegisteredUsersContactCall()) && !userPreference.getWasContactListServerCallAlreadyDone()) {
            if (Utils.isInternetAvailable(fragmentActivity)) {
                processLoadUsers(true, message, messageContent, alCustomizationSettings.getTotalRegisteredUserToFetch(), alCustomizationSettings.getTotalOnlineUsers());
            }
        } else {
            Intent intent = new Intent(fragmentActivity, MobiComKitPeopleActivity.class);
            startContactActivityForResult(intent, message, messageContent, null);
        }
    }

    public void sendPriceMessage() {

        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(fragmentActivity);
            alertDialog.setTitle("Price");
            alertDialog.setMessage("Enter your amount");

            final EditText inputText = new EditText(fragmentActivity);
            inputText.setInputType(InputType.TYPE_CLASS_NUMBER);
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            inputText.setLayoutParams(linearParams);
            alertDialog.setView(inputText);

            alertDialog.setPositiveButton(fragmentActivity.getString(R.string.send_text),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (!TextUtils.isEmpty(inputText.getText().toString()) && getConversationFragment() != null) {
                                getConversationFragment().sendMessage(inputText.getText().toString(), Message.ContentType.PRICE.getValue());
                            }
                        }
                    });

            alertDialog.setNegativeButton(fragmentActivity.getString(R.string.cancel_text),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            if (!fragmentActivity.isFinishing()) {
                alertDialog.show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendAudioMessage(String selectedFilePath) {

        Utils.printLog(fragmentActivity, "ConversationUIService:", "Send audio message ...");

        if (getConversationFragment() != null) {
            getConversationFragment().sendMessage(Message.ContentType.AUDIO_MSG.getValue(), selectedFilePath);
        }

    }

    public void updateMessageMetadata(String keyString) {
        if (getConversationFragment() != null) {
            getConversationFragment().updateMessageMetadata(keyString);
        }
    }

    public void muteUserChat(boolean mute, String userId) {
        if (getConversationFragment() != null && getConversationFragment().getContact() != null && getConversationFragment().getContact().getUserId().equals(userId)) {
            getConversationFragment().muteUser(mute);
        }
    }

    public void onMqttConnected() {
        // TODO Maybe if someone wants the connection status, send a callback from here
    }

    public void startMessageInfoFragment(String messageJson) {

        MessageInfoFragment messageInfoFragment = (MessageInfoFragment) UIService.getFragmentByTag(fragmentActivity, MESSGAE_INFO_FRAGMENT);
        if (messageInfoFragment == null) {
            messageInfoFragment = new MessageInfoFragment();
            Bundle bundle = new Bundle();
            bundle.putString(MessageInfoFragment.MESSAGE_ARGUMENT_KEY, messageJson);
            messageInfoFragment.setArguments(bundle);
            ConversationActivity.addFragment(fragmentActivity, messageInfoFragment, MESSGAE_INFO_FRAGMENT);
        }
    }

    public void checkForStartNewConversation(Intent intent) {
        Contact contact = null;
        Channel channel = null;
        Integer conversationId = null;

        if (Intent.ACTION_SEND.equals(intent.getAction()) && intent.getType() != null) {
            if ("text/plain".equals(intent.getType())) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    startContactActivityForResult(null, sharedText);
                }
            } else if (intent.getType().startsWith("image/")) {
                //Todo: use this for image forwarding
            }
        }

        final Uri uri = intent.getData();
        if (uri != null) {
            //Note: This is used only for the device contacts
            Long contactId = intent.getLongExtra(CONTACT_ID, 0);
            if (contactId == 0) {
                //Todo: show warning that the user doesn't have any number stored.
                return;
            }
            contact = baseContactService.getContactById(String.valueOf(contactId));
        }

        Integer channelKey = intent.getIntExtra(GROUP_ID, -1);
        String clientGroupId = intent.getStringExtra(CLIENT_GROUP_ID);
        String channelName = intent.getStringExtra(GROUP_NAME);

        if (!TextUtils.isEmpty(clientGroupId)) {
            channel = ChannelService.getInstance(fragmentActivity).getChannelByClientGroupId(clientGroupId);
            if (channel == null) {
                return;
            }
        } else if (channelKey != -1 && channelKey != null && channelKey != 0) {
            channel = ChannelService.getInstance(fragmentActivity).getChannel(channelKey);
        }

        if (channel != null && !TextUtils.isEmpty(channelName) && TextUtils.isEmpty(channel.getName())) {
            channel.setName(channelName);
            ChannelService.getInstance(fragmentActivity).updateChannel(channel);
        }

        String contactNumber = intent.getStringExtra(CONTACT_NUMBER);

        boolean firstTimeMTexterFriend = intent.getBooleanExtra(FIRST_TIME_MTEXTER_FRIEND, false);
        if (!TextUtils.isEmpty(contactNumber)) {
            contact = baseContactService.getContactById(contactNumber);
            if (BroadcastService.isIndividual() && getConversationFragment() != null) {
                getConversationFragment().setFirstTimeMTexterFriend(firstTimeMTexterFriend);
            }
        }

        String userId = intent.getStringExtra(USER_ID);
        if (TextUtils.isEmpty(userId)) {
            userId = intent.getStringExtra("contactId");
        }

        if (!TextUtils.isEmpty(userId)) {
            contact = baseContactService.getContactById(userId);
        }
        String searchString = intent.getStringExtra(SEARCH_STRING);
        String applicationId = intent.getStringExtra(APPLICATION_ID);
        if (contact != null) {
            contact.setApplicationId(applicationId);
            baseContactService.upsert(contact);
        }
        String fullName = intent.getStringExtra(DISPLAY_NAME);
        if (contact != null && TextUtils.isEmpty(contact.getFullName()) && !TextUtils.isEmpty(fullName)) {
            contact.setFullName(fullName);
            baseContactService.upsert(contact);
            new UserClientService(fragmentActivity).updateUserDisplayName(userId, fullName);
        }
        String messageJson = intent.getStringExtra(MobiComKitConstants.MESSAGE_JSON_INTENT);
        if (!TextUtils.isEmpty(messageJson)) {
            Message message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);
            if (message.getGroupId() != null) {
                channel = ChannelService.getInstance(fragmentActivity).getChannelByChannelKey(message.getGroupId());
                if (channel.getParentKey() != null && channel.getParentKey() != 0) {
                    BroadcastService.parentGroupKey = channel.getParentKey();
                    MobiComUserPreference.getInstance(fragmentActivity).setParentGroupKey(channel.getParentKey());
                }
            } else {
                contact = baseContactService.getContactById(message.getContactIds());
            }
            conversationId = message.getConversationId();
        }
        if (conversationId == null) {
            conversationId = intent.getIntExtra(CONVERSATION_ID, 0);
        }
        if (conversationId != 0 && conversationId != null && getConversationFragment() != null) {
            getConversationFragment().setConversationId(conversationId);
        } else {
            conversationId = null;
        }

        boolean support = intent.getBooleanExtra(Support.SUPPORT_INTENT_KEY, false);
        if (support) {
            contact = new Support(fragmentActivity).getSupportContact();
        }

        String defaultText = intent.getStringExtra(ConversationUIService.DEFAULT_TEXT);
        if (!TextUtils.isEmpty(defaultText) && getConversationFragment() != null) {
            getConversationFragment().setDefaultText(defaultText);
        }

        String forwardMessage = intent.getStringExtra(MobiComKitPeopleActivity.FORWARD_MESSAGE);
        if (!TextUtils.isEmpty(forwardMessage)) {
            Message messageToForward = (Message) GsonUtils.getObjectFromJson(forwardMessage, Message.class);
            if (getConversationFragment() != null) {
                getConversationFragment().forwardMessage(messageToForward, contact, channel);
            }
        }

        if (contact != null) {
            openConversationFragment(contact, conversationId, searchString);
        }

        if (channel != null) {
            openConversationFragment(channel, conversationId, searchString);
        }
        String productTopicId = intent.getStringExtra(ConversationUIService.PRODUCT_TOPIC_ID);
        String productImageUrl = intent.getStringExtra(ConversationUIService.PRODUCT_IMAGE_URL);
        if (!TextUtils.isEmpty(productTopicId) && !TextUtils.isEmpty(productImageUrl)) {
            try {
                FileMeta fileMeta = new FileMeta();
                fileMeta.setContentType("image");
                fileMeta.setBlobKeyString(productImageUrl);
                if (getConversationFragment() != null) {
                    getConversationFragment().sendProductMessage(productTopicId, fileMeta, contact, Message.ContentType.TEXT_URL.getValue());
                }
            } catch (Exception e) {
            }
        }

        String sharedText = intent.getStringExtra(MobiComKitPeopleActivity.SHARED_TEXT);
        if (!TextUtils.isEmpty(sharedText) && getConversationFragment() != null) {
            getConversationFragment().sendMessage(sharedText);
        }
    }

    public void processStartNewConversation(Contact contact, Channel channel, Integer conversationId, String searchString, Intent intent) {
        String channelName = intent.getStringExtra(GROUP_NAME);
        if (channel != null && !TextUtils.isEmpty(channelName) && TextUtils.isEmpty(channel.getName())) {
            channel.setName(channelName);
            ChannelService.getInstance(fragmentActivity).updateChannel(channel);
        }

        if (conversationId == null) {
            conversationId = intent.getIntExtra(CONVERSATION_ID, 0);
        }
        if (conversationId != 0 && conversationId != null && getConversationFragment() != null) {
            getConversationFragment().setConversationId(conversationId);
        } else {
            conversationId = null;
        }

        boolean support = intent.getBooleanExtra(Support.SUPPORT_INTENT_KEY, false);
        if (support) {
            contact = new Support(fragmentActivity).getSupportContact();
        }

        String defaultText = intent.getStringExtra(ConversationUIService.DEFAULT_TEXT);
        if (!TextUtils.isEmpty(defaultText) && getConversationFragment() != null) {
            getConversationFragment().setDefaultText(defaultText);
        }

        String forwardMessage = intent.getStringExtra(MobiComKitPeopleActivity.FORWARD_MESSAGE);
        if (!TextUtils.isEmpty(forwardMessage)) {
            Message messageToForward = (Message) GsonUtils.getObjectFromJson(forwardMessage, Message.class);
            if (getConversationFragment() != null) {
                getConversationFragment().forwardMessage(messageToForward, contact, channel);
            }
        }

        if (contact != null) {
            openConversationFragment(contact, conversationId, searchString);
        }

        if (channel != null) {
            openConversationFragment(channel, conversationId, searchString);
        }
        String productTopicId = intent.getStringExtra(ConversationUIService.PRODUCT_TOPIC_ID);
        String productImageUrl = intent.getStringExtra(ConversationUIService.PRODUCT_IMAGE_URL);
        if (!TextUtils.isEmpty(productTopicId) && !TextUtils.isEmpty(productImageUrl)) {
            try {
                FileMeta fileMeta = new FileMeta();
                fileMeta.setContentType("image");
                fileMeta.setBlobKeyString(productImageUrl);
                if (getConversationFragment() != null) {
                    getConversationFragment().sendProductMessage(productTopicId, fileMeta, contact, Message.ContentType.TEXT_URL.getValue());
                }
            } catch (Exception e) {
            }
        }

        String sharedText = intent.getStringExtra(MobiComKitPeopleActivity.SHARED_TEXT);
        if (!TextUtils.isEmpty(sharedText) && getConversationFragment() != null) {
            getConversationFragment().sendMessage(sharedText);
        }
    }

    void showToastMessage(final String messageToShow) {
        Toast toast = Toast.makeText(fragmentActivity, messageToShow, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void reconnectMQTT() {
        try {
            if (((MobiComKitActivityInterface) fragmentActivity).getRetryCount() <= 3) {
                if (Utils.isInternetAvailable(fragmentActivity)) {
                    Utils.printLog(fragmentActivity, TAG, "Reconnecting to mqtt.");
                    ((MobiComKitActivityInterface) fragmentActivity).retry();
                    Intent intent = new Intent(fragmentActivity, ApplozicMqttIntentService.class);
                    intent.putExtra(ApplozicMqttIntentService.SUBSCRIBE, true);
                    ApplozicMqttIntentService.enqueueWork(fragmentActivity, intent);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendLocation(String position) {
        if (getConversationFragment() != null) {
            getConversationFragment().sendMessage(position, Message.ContentType.LOCATION.getValue());
        }
    }

    public void processLoadUsers(boolean isRegisteredUserCall, final Message message, final String messageContent, int totalRegisteredUsers, int totalOnlineUser) {

        final ProgressDialog progressDialog = ProgressDialog.show(fragmentActivity, "",
                fragmentActivity.getString(R.string.applozic_contacts_loading_info), true);

        RegisteredUsersAsyncTask.TaskListener usersAsyncTaskTaskListener = new RegisteredUsersAsyncTask.TaskListener() {
            @Override
            public void onSuccess(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                try {
                    if (registeredUsersApiResponse != null) {
                        userPreference.setWasContactListServerCallAlreadyDone(true);
                        Intent intent = new Intent(fragmentActivity, MobiComKitPeopleActivity.class);
                        startContactActivityForResult(intent, message, messageContent, null);
                    }

                    if (userIdArray != null && userIdArray.length > 0) {
                        Intent intent = new Intent(fragmentActivity, MobiComKitPeopleActivity.class);
                        startContactActivityForResult(intent, message, messageContent, userIdArray);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray, Exception exception) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                String error = fragmentActivity.getString(Utils.isInternetAvailable(fragmentActivity) ? R.string.applozic_server_error : R.string.you_need_network_access_for_block_or_unblock);
                Toast toast = Toast.makeText(fragmentActivity, error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onCompletion() {

            }
        };
        RegisteredUsersAsyncTask usersAsyncTask;
        if (isRegisteredUserCall) {
            usersAsyncTask = new RegisteredUsersAsyncTask(fragmentActivity, usersAsyncTaskTaskListener, totalRegisteredUsers, 0l, message, messageContent, true);
        } else {
            usersAsyncTask = new RegisteredUsersAsyncTask(fragmentActivity, usersAsyncTaskTaskListener, totalOnlineUser, message, messageContent);
        }
        usersAsyncTask.execute((Void) null);

    }

    public void processSL2T() {
        ConversationFragment conversationFragment = getConversationFragment();
        if (conversationFragment.getChannel() != null) {
            Intent intent = new Intent(fragmentActivity, sign_to_text.class);
            intent.putExtra("CONTACT_ID", String.valueOf(conversationFragment.getChannel().getKey()));
            intent.putExtra("type", "group");
            fragmentActivity.startActivity(intent);
        }else {
            Intent intent = new Intent(fragmentActivity, sign_to_text.class);
            intent.putExtra("CONTACT_ID", conversationFragment.getContact().getContactIds());
            intent.putExtra("type", "user");

            fragmentActivity.startActivity(intent);
        }
    }

    public void processT2SL() {
        ConversationFragment conversationFragment = getConversationFragment();

        if (conversationFragment.getChannel() != null) {
            Intent intent = new Intent(fragmentActivity, text_to_sign.class);
            intent.putExtra("CONTACT_ID", String.valueOf(conversationFragment.getChannel().getKey()));
            intent.putExtra("type", "group");
            fragmentActivity.startActivity(intent);
        }else {
            Intent intent = new Intent(fragmentActivity, text_to_sign.class);
            intent.putExtra("CONTACT_ID", conversationFragment.getContact().getContactIds());
            intent.putExtra("type", "user");
            fragmentActivity.startActivity(intent);
        }    }



}
