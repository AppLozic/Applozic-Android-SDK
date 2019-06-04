package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AlphabetIndexer;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.api.notification.VideoCallNotificationHelper;
import com.applozic.mobicomkit.channel.database.ChannelDatabaseService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComKitActivityInterface;
import com.applozic.mobicommons.ApplozicService;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.emoticon.EmojiconHandler;
import com.applozic.mobicommons.emoticon.EmoticonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by adarsh on 4/7/15.
 */
public class QuickConversationAdapter extends RecyclerView.Adapter implements Filterable {

    private static Map<Short, Integer> messageTypeColorMap = new HashMap<Short, Integer>();

    static {
        messageTypeColorMap.put(Message.MessageType.INBOX.getValue(), R.color.message_type_inbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX.getValue(), R.color.message_type_outbox);
        messageTypeColorMap.put(Message.MessageType.OUTBOX_SENT_FROM_DEVICE.getValue(), R.color.message_type_outbox_sent_from_device);
        messageTypeColorMap.put(Message.MessageType.MT_INBOX.getValue(), R.color.message_type_mt_inbox);
        messageTypeColorMap.put(Message.MessageType.MT_OUTBOX.getValue(), R.color.message_type_mt_outbox);
        messageTypeColorMap.put(Message.MessageType.CALL_INCOMING.getValue(), R.color.message_type_incoming_call);
        messageTypeColorMap.put(Message.MessageType.CALL_OUTGOING.getValue(), R.color.message_type_outgoing_call);
    }

    public ImageLoader contactImageLoader, channelImageLoader;
    public String searchString = null;
    private Context context;
    private MessageDatabaseService messageDatabaseService;
    private List<Message> messageList;
    private BaseContactService contactService;
    private EmojiconHandler emojiconHandler;
    private List<Message> originalList;
    private TextAppearanceSpan highlightTextSpan;
    private AlCustomizationSettings alCustomizationSettings;
    private View view;
    private ConversationUIService conversationUIService;
    public SharedPreferences MarkList;
    public void setAlCustomizationSettings(AlCustomizationSettings alCustomizationSettings) {
        this.alCustomizationSettings = alCustomizationSettings;
    }

    public QuickConversationAdapter(final Context context, List<Message> messageList, EmojiconHandler emojiconHandler) {
        this.context = context;
        this.emojiconHandler = emojiconHandler;
        this.contactService = new AppContactService(context);
        this.messageDatabaseService = new MessageDatabaseService(context);
        this.messageList = messageList;
        conversationUIService = new ConversationUIService((FragmentActivity) context);
        contactImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage((Activity) context, (Contact) data);
            }
        };
        contactImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);
        channelImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadGroupImage((Activity) context, (Channel) data);
            }
        };
        channelImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        channelImageLoader.setImageFadeIn(false);
        highlightTextSpan = new TextAppearanceSpan(context, R.style.searchTextHiglight);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            if (viewType == 2) {
                View v2 = inflater.inflate(R.layout.mobicom_message_list_header_footer, parent, false);
                return new FooterViewHolder(v2);
            } else {
                view = inflater.inflate(R.layout.mobicom_message_row_view, parent, false);
                return new Myholder(view);
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == 2) {
            FooterViewHolder myHolder = (FooterViewHolder) holder;
            myHolder.infoBroadCast.setVisibility(View.GONE);
        } else {
            Myholder myholder = (Myholder) holder;
            final Message message = getItem(position);
            myholder.smTime.setVisibility(View.GONE);
            if (message != null) {
                List<String> items = null;
                List<String> userIds = null;

                final Channel channel = ChannelDatabaseService.getInstance(context).getChannelByChannelKey(message.getGroupId());

                if (channel == null && message.getGroupId() == null) {
                    items = Arrays.asList(message.getTo().split("\\s*,\\s*"));
                    if (!TextUtils.isEmpty(message.getContactIds())) {
                        userIds = Arrays.asList(message.getContactIds().split("\\s*,\\s*"));
                    }
                }

                final Contact contactReceiver = contactService.getContactReceiver(items, userIds);

                myholder.contactImage.setVisibility(View.GONE);
                myholder.alphabeticTextView.setVisibility(View.GONE);

                if (contactReceiver != null) {
                    String contactInfo = contactReceiver.getDisplayName();
                    if (items != null && items.size() > 1) {
                        Contact contact2 = contactService.getContactById(items.get(1));
                        contactInfo = TextUtils.isEmpty(contactReceiver.getFirstName()) ? contactReceiver.getContactNumber() : contactReceiver.getFirstName() + ", "
                                + (TextUtils.isEmpty(contact2.getFirstName()) ? contact2.getContactNumber() : contact2.getFirstName()) + (items.size() > 2 ? " & others" : "");
                    }
                    myholder.smReceivers.setText(contactInfo);
                    contactImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);
                    processContactImage(contactReceiver, myholder.onlineTextView, myholder.alphabeticTextView, myholder.contactImage);
                } else if (message.getGroupId() != null) {
                    if (channel != null && Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                        contactImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);

                        if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                            Contact withUserContact = contactService.getContactById(ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey()));
                            if (withUserContact != null) {
                                myholder.smReceivers.setText(withUserContact.getDisplayName());
                                processContactImage(withUserContact, myholder.onlineTextView, myholder.alphabeticTextView, myholder.contactImage);
                            }
                        }
                    } else {
                        if (channel != null && Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                            channelImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);
                            myholder.contactImage.setImageResource(R.drawable.applozic_ic_contact_picture_holo_light);
                        } else {
                            channelImageLoader.setLoadingImage(R.drawable.applozic_group_icon);
                            myholder.contactImage.setImageResource(R.drawable.applozic_group_icon);
                        }
                        myholder.smReceivers.setText(ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(context).getUserId()));
                        myholder.alphabeticTextView.setVisibility(View.GONE);
                        myholder.contactImage.setVisibility(View.VISIBLE);

                        if (channel != null && !TextUtils.isEmpty(channel.getImageUrl())) {
                            channelImageLoader.loadImage(channel, myholder.contactImage);
                        } else if (channel != null && channel.isBroadcastMessage()) {
                            myholder.contactImage.setImageResource(R.drawable.applozic_ic_applozic_broadcast);
                        } else if (channel != null && Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())) {
                            channelImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);
                        } else {
                            channelImageLoader.setLoadingImage(R.drawable.applozic_group_icon);
                        }
                    }
                }

                myholder.onlineTextView.setVisibility(View.GONE);
                if (alCustomizationSettings.isOnlineStatusMasterList()) {
                    myholder.onlineTextView.setVisibility(contactReceiver != null && contactReceiver.isOnline() ? View.VISIBLE : View.GONE);
                }

                if (myholder.attachedFile != null) {
                    myholder.attachedFile.setText("");
                    myholder.attachedFile.setVisibility(View.GONE);
                }

                if (myholder.attachmentIcon != null) {
                    myholder.attachmentIcon.setVisibility(View.GONE);
                }
                if (message.isVideoCallMessage()) {
                    createVideoCallView(message, myholder.attachmentIcon, myholder.messageTextView);
                } else if (message.hasAttachment() && myholder.attachmentIcon != null && !(message.getContentType() == Message.ContentType.TEXT_URL.getValue())) {
                    //Todo: handle it for fileKeyStrings when filePaths is empty
                    String filePath = message.getFileMetas() == null && message.getFilePaths() != null ? message.getFilePaths().get(0).substring(message.getFilePaths().get(0).lastIndexOf("/") + 1) :
                            message.getFileMetas() != null ? message.getFileMetas().getName() : "";
                    myholder.attachmentIcon.setVisibility(View.VISIBLE);
                    myholder.attachmentIcon.setImageResource(R.drawable.applozic_ic_action_attachment);
                    myholder.messageTextView.setText(filePath);
                } else if (myholder.attachmentIcon != null && message.getContentType() == Message.ContentType.LOCATION.getValue()) {
                    myholder.attachmentIcon.setVisibility(View.VISIBLE);
                    myholder.attachmentIcon.setImageResource(R.drawable.mobicom_notification_location_icon);
                    myholder.messageTextView.setText(Utils.getString(context, R.string.Location));
                } else if (message.getContentType() == Message.ContentType.PRICE.getValue()) {
                    myholder.messageTextView.setText(EmoticonUtils.getSmiledText(context, ConversationUIService.FINAL_PRICE_TEXT + message.getMessage(), emojiconHandler));
                } else if (message.getContentType() == Message.ContentType.TEXT_HTML.getValue()) {
                    myholder.messageTextView.setText(Html.fromHtml(message.getMessage()));
                } else {
                    String messageSubString = (!TextUtils.isEmpty(message.getMessage()) ? message.getMessage().substring(0, Math.min(message.getMessage().length(), 50)) : "");
                    myholder.messageTextView.setText(EmoticonUtils.getSmiledText(context, messageSubString, emojiconHandler));
                }

                if (myholder.sentOrReceived != null) {
                    if (message.isCall()) {
                        myholder.sentOrReceived.setImageResource(R.drawable.applozic_ic_action_call_holo_light);
                        myholder.messageTextView.setTextColor(Utils.getColor(context, message.isIncomingCall() ? R.color.incoming_call : R.color.outgoing_call));
                    } else if (getItemViewType(position) == 0) {
                        myholder.sentOrReceived.setImageResource(R.drawable.mobicom_social_forward);
                    } else {
                        myholder.sentOrReceived.setImageResource(R.drawable.mobicom_social_reply);
                    }
                }
                if (myholder.createdAtTime != null) {
                    myholder.createdAtTime.setText(DateUtils.getFormattedDateAndTime(context, message.getCreatedAtTime(), R.string.JUST_NOW, R.plurals.MINUTES, R.plurals.HOURS));
                }
                int messageUnReadCount = 0;
                if (message.getGroupId() == null && contactReceiver != null && !TextUtils.isEmpty(contactReceiver.getContactIds())) {
                    messageUnReadCount = messageDatabaseService.getUnreadMessageCountForContact(contactReceiver.getContactIds());

                } else if (channel != null && channel.getKey() != null && channel.getKey() != 0) {
                    messageUnReadCount = messageDatabaseService.getUnreadMessageCountForChannel(channel.getKey());
                }
                if (messageUnReadCount > 0) {
                    myholder.unReadCountTextView.setVisibility(View.VISIBLE);
                    myholder.unReadCountTextView.setText(String.valueOf(messageUnReadCount));
                } else {
                    myholder.unReadCountTextView.setVisibility(View.GONE);
                }

                int startIndex = indexOfSearchQuery(message.getMessage());
                if (startIndex != -1) {

                    final SpannableString highlightedName = new SpannableString(message.getMessage());

                    // Sets the span to start at the starting point of the match and end at "length"
                    // characters beyond the starting point
                    highlightedName.setSpan(highlightTextSpan, startIndex,
                            startIndex + searchString.toString().length(), 0);

                    myholder.messageTextView.setText(highlightedName);
                }
            }
        }
    }

    public Message getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public int getItemViewType(int position) {
        return getItem(position) != null ? getItem(position).isTypeOutbox() ? 1 : 0 : 2;
    }

    private int indexOfSearchQuery(String message) {
        if (!TextUtils.isEmpty(searchString)) {
            return message.toLowerCase(Locale.getDefault()).indexOf(
                    searchString.toString().toLowerCase(Locale.getDefault()));
        }
        return -1;
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

    public void createVideoCallView(Message message, ImageView attachmentIcon, TextView messageTextView) {
        if (message.getMetadata() == null || message.getMetadata().isEmpty()) {
            if (attachmentIcon != null) {
                attachmentIcon.setImageResource(R.drawable.ic_videocam_white_24px);
                attachmentIcon.setColorFilter(R.color.applozic_green_color);
                return;
            }
        }

        if (messageTextView != null) {
            messageTextView.setText(VideoCallNotificationHelper.getStatus(message.getMetadata()));
        }

        if (attachmentIcon != null) {
            attachmentIcon.setVisibility(View.VISIBLE);
            if (VideoCallNotificationHelper.isMissedCall(message)) {
                attachmentIcon.setImageResource(R.drawable.ic_communication_call_missed);
            } else if (VideoCallNotificationHelper.isAudioCall(message)) {
                attachmentIcon.setImageResource(R.drawable.applozic_ic_action_call_holo_light);
            } else {
                attachmentIcon.setImageResource(R.drawable.ic_videocam_white_24px);
                attachmentIcon.setColorFilter(R.color.applozic_green_color);
            }
        }
    }

    private void processContactImage(Contact contact, TextView textView, TextView alphabeticTextView, CircleImageView contactImage) {
        try {
            String contactNumber = "";
            char firstLetter = 0;
            contactNumber = contact.getDisplayName().toUpperCase();
            firstLetter = contact.getDisplayName().toUpperCase().charAt(0);

            if (contact != null) {
                if (firstLetter != '+') {
                    alphabeticTextView.setText(String.valueOf(firstLetter));
                } else if (contactNumber.length() >= 2) {
                    alphabeticTextView.setText(String.valueOf(contactNumber.charAt(1)));
                }
                Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                GradientDrawable bgShape = (GradientDrawable) alphabeticTextView.getBackground();
                bgShape.setColor(ApplozicService.getContext(context).getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
            }

            alphabeticTextView.setVisibility(View.GONE);
            contactImage.setVisibility(View.VISIBLE);
            if (contact != null) {
                if (contact.isDrawableResources()) {
                    int drawableResourceId = ApplozicService.getContext(context).getResources().getIdentifier(contact.getrDrawableName(), "drawable", ApplozicService.getContext(context).getPackageName());
                    contactImage.setImageResource(drawableResourceId);
                } else {
                    contactImageLoader.loadImage(contact, contactImage, alphabeticTextView);
                }
            }
            textView.setVisibility(contact != null && contact.isOnline() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {

        }
    }

    public class Myholder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

        TextView smReceivers;
        TextView createdAtTime;
        TextView messageTextView;
        CircleImageView contactImage;
        TextView alphabeticTextView;
        TextView onlineTextView;
        ImageView sentOrReceived;
        TextView attachedFile;
        final ImageView attachmentIcon;
        TextView unReadCountTextView;
        TextView smTime;

        public Myholder(View itemView) {
            super(itemView);
            smReceivers = (TextView) itemView.findViewById(R.id.smReceivers);
            createdAtTime = (TextView) itemView.findViewById(R.id.createdAtTime);
            messageTextView = (TextView) itemView.findViewById(R.id.message);
            //ImageView contactImage = (ImageView) customView.findViewById(R.id.contactImage);
            contactImage = (CircleImageView) itemView.findViewById(R.id.contactImage);
            alphabeticTextView = (TextView) itemView.findViewById(R.id.alphabeticImage);
            onlineTextView = (TextView) itemView.findViewById(R.id.onlineTextView);
            sentOrReceived = (ImageView) itemView.findViewById(R.id.sentOrReceivedIcon);
            attachedFile = (TextView) itemView.findViewById(R.id.attached_file);
            attachmentIcon = (ImageView) itemView.findViewById(R.id.attachmentIcon);
            unReadCountTextView = (TextView) itemView.findViewById(R.id.unreadSmsCount);
            smTime = (TextView) itemView.findViewById(R.id.smTime);
            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            int itemPosition = this.getLayoutPosition();
            if (itemPosition < messageList.size() && itemPosition != -1) {
                final Message message = getItem(itemPosition);
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (message != null) {
                            final Channel channel = ChannelService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                            final Contact contact = new ContactDatabase(context).getContactById(channel == null ? message.getContactIds() : null);
                            if (context != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ((MobiComKitActivityInterface) context).onQuickConversationFragmentItemClick(view, contact, channel, message.getConversationId(), searchString);
                                    }
                                });
                            }

                        }
                    }
                });
                thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                thread.start();

            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = this.getLayoutPosition();

            if (messageList.size() <= position) {
                return;
            }
            Message message = messageList.get(position);
            menu.setHeaderTitle(R.string.conversation_options);

            String[] menuItems = ApplozicService.getContext(context).getResources().getStringArray(R.array.conversation_options_menu);

            boolean isUserPresentInGroup = false;
            boolean isChannelDeleted = false;
            Channel channel = null;
            if (message.getGroupId() != null) {
                channel = ChannelService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                if (channel != null) {
                    isChannelDeleted = channel.isDeleted();
                }
                isUserPresentInGroup = ChannelService.getInstance(context).processIsUserPresentInChannel(message.getGroupId());
            }

            for (int i = 0; i < menuItems.length; i++) {
                if(menuItems[i].equals(Utils.getString(context,R.string.Concern))){
                    continue;
                }

                if ((message.getGroupId() == null || (channel != null && (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType()) || Channel.GroupType.SUPPORT_GROUP.getValue().equals(channel.getType())))) && (menuItems[i].equals(Utils.getString(context, R.string.delete_group)) ||
                        menuItems[i].equals(Utils.getString(context, R.string.exit_group)))) {
                    continue;
                }

                if (menuItems[i].equals(Utils.getString(context, R.string.exit_group)) && (isChannelDeleted || !isUserPresentInGroup || (channel != null && Channel.GroupType.BROADCAST.getValue().equals(channel.getType())))) {
                    continue;
                }

                if (menuItems[i].equals(Utils.getString(context, R.string.delete_group)) && (isUserPresentInGroup || !isChannelDeleted)) {
                    continue;
                }
                if (menuItems[i].equals(Utils.getString(context, R.string.delete_conversation)) && !(alCustomizationSettings.isDeleteOption() || ApplozicSetting.getInstance(context).isDeleteConversationOption())) {
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

                if (messageList.size() <= position || position == -1) {
                    return true;
                }
                Message message = messageList.get(position);

                Channel channel = null;
                Contact contact = null;
                if (message.getGroupId() != null) {
                    channel = ChannelDatabaseService.getInstance(context).getChannelByChannelKey(message.getGroupId());
                } else {
                    if (contactService != null) {
                        contact = contactService.getContactById(message.getContactIds());
                    }
                }

                switch (item.getItemId()) {
                    case 0:
                        if (conversationUIService != null) {
                            if (channel != null && channel.isDeleted()) {
                                conversationUIService.deleteGroupConversation(channel);
                            } else {
                                conversationUIService.deleteConversationThread(contact, channel);
                            }
                        }
                        break;
                    case 1:
                        if (conversationUIService != null) {
                            conversationUIService.deleteGroupConversation(channel);
                        }
                        break;
                    case 2:
                        if (conversationUIService != null) {
                            conversationUIService.channelLeaveProcess(channel);
                        }
                        break;
                    case 3:
                        String MarkID = contact.getUserId();
                        MarkList = QuickConversationAdapter.this.context.getSharedPreferences("MarkList", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = MarkList.edit();
                        editor.putString("Mark_ID",MarkID);
                        editor.putBoolean("USER_ID", true);
                        editor.commit();
                        Toast.makeText(QuickConversationAdapter.this.context,"Mark Successfully !" ,Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        MarkList =context.getSharedPreferences("MarkList", Context.MODE_PRIVATE);
                        String MarkID2=MarkList.getString("Mark_ID",null);
                        if(MarkID2==null){
                            Toast.makeText(QuickConversationAdapter.this.context,"The Contact is not in Mark List !",Toast.LENGTH_SHORT).show();
                        }else{
                        MarkList.edit().clear().commit();
                        Toast.makeText(QuickConversationAdapter.this.context,"Cancel Mark Successfully !",Toast.LENGTH_LONG).show();}
                        break;
                        default:

                }
                return true;
            }
        };

    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        TextView infoBroadCast;
        ProgressBar loadMoreProgressBar;

        public FooterViewHolder(View itemView) {
            super(itemView);
            infoBroadCast = (TextView) itemView.findViewById(R.id.info_broadcast);
            loadMoreProgressBar = (ProgressBar) itemView.findViewById(R.id.load_more_progressbar);
        }
    }

}
