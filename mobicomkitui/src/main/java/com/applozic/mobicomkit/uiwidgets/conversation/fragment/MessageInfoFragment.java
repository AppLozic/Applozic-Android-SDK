package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.attachment.AttachmentView;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.attachment.FileMeta;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageInfo;
import com.applozic.mobicomkit.api.conversation.MessageInfoResponse;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComMessageService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.contact.MobiComVCFParser;
import com.applozic.mobicomkit.contact.VCFContactData;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicommons.commons.core.utils.DateUtils;
import com.applozic.mobicommons.commons.core.utils.LocationUtils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageInfoFragment extends Fragment {

    public static final String MESSAGE_ARGUMENT_KEY = "MESSAGE";
    Message message = null;
    AttachmentView attachmentView;
    MessageInfoResponse messageInfoResponse;
    MessageInfoAsyncTask messageInfoAsyncTask;
    private ImageLoader contactImageLoader, locationImageLoader;
    private RecyclerView readListView;
    private RecyclerView deliveredListView;

    public MessageInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        init();

        View view = inflater.inflate(R.layout.applozic_message_info, container, false);
        Bundle bundle = getArguments();
        String messageJson = bundle.getString(MESSAGE_ARGUMENT_KEY);
        message = (Message) GsonUtils.getObjectFromJson(messageJson, Message.class);

        attachmentView = (AttachmentView) view.findViewById(R.id.applozic_message_info_attachmentview);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.applozic_message_info_progress_bar);
        attachmentView.setProressBar(progressBar);
        attachmentView.setVisibility(message.hasAttachment() ? View.VISIBLE : View.GONE);


        RelativeLayout defaultRelativeLayout = (RelativeLayout) view.findViewById(R.id.applozic_message_info_default_layout);
        TextView textView = (TextView) view.findViewById(R.id.applozic_message_info_message_text);
        readListView = (RecyclerView) view.findViewById(R.id.applozic_message_info_read_list);
        deliveredListView = (RecyclerView) view.findViewById(R.id.applozic_message_info_delivered_list_view);
        readListView.setHasFixedSize(true);
        deliveredListView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView.LayoutManager mLayoutManagerForDev = new LinearLayoutManager(getActivity());
        readListView.setLayoutManager(mLayoutManager);
        readListView.setClickable(true);
        deliveredListView.setLayoutManager(mLayoutManagerForDev);
        deliveredListView.setClickable(true);


        ImageView locationImageView = (ImageView) view.findViewById(R.id.static_mapview);
        final LinearLayout mainContactShareLayout = (LinearLayout) view.findViewById(R.id.contact_share_layout);

        RelativeLayout chatLocation = (RelativeLayout) view.findViewById(R.id.chat_location);

        if (message.hasAttachment() && !message.isContactMessage() && !message.isLocationMessage()) {
            textView.setVisibility(View.GONE);
            attachmentView.setMessage(message);
            chatLocation.setVisibility(View.GONE);
            defaultRelativeLayout.setVisibility(View.GONE);
            defaultRelativeLayout.setVisibility(View.VISIBLE);
            setupAttachmentView(message, defaultRelativeLayout);

        } else {

            defaultRelativeLayout.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(message.getMessage());

        }
        if (message.isLocationMessage()) {
            defaultRelativeLayout.setVisibility(View.GONE);
            chatLocation.setVisibility(View.VISIBLE);
            locationImageLoader.setImageFadeIn(false);
            locationImageLoader.setLoadingImage(R.drawable.applozic_map_offline_thumbnail);
            locationImageLoader.loadImage(LocationUtils.loadStaticMap(message.getMessage()), locationImageView);
            textView.setVisibility(View.GONE);
        } else {
            chatLocation.setVisibility(View.GONE);

        }

        if (message.isContactMessage()) {
            chatLocation.setVisibility(View.GONE);
            defaultRelativeLayout.setVisibility(View.GONE);
            setupContactShareView(message, mainContactShareLayout);
            textView.setVisibility(View.GONE);
        } else {
            mainContactShareLayout.setVisibility(View.GONE);
        }

        messageInfoAsyncTask = new MessageInfoAsyncTask(message.getKeyString(), getActivity());
        messageInfoAsyncTask.execute();
        return view;
    }


    private void init() {
        if (contactImageLoader == null) {
            contactImageLoader = new ImageLoader(getContext(), getListPreferredItemHeight()) {
                @Override
                protected Bitmap processBitmap(Object data) {
                    BaseContactService contactService = new AppContactService(getContext());
                    return contactService.downloadContactImage(getContext(), (Contact) data);
                }
            };
            contactImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_holo_light);
            contactImageLoader.addImageCache(getActivity().getSupportFragmentManager(), 0.1f);
        }

        if (locationImageLoader == null) {
            locationImageLoader = new ImageLoader(getContext(), ImageUtils.getLargestScreenDimension((Activity) getContext())) {
                @Override
                protected Bitmap processBitmap(Object data) {
                    FileClientService fileClientService = new FileClientService(getContext());
                    return fileClientService.loadMessageImage(getContext(), (String) data);
                }
            };
            locationImageLoader.setImageFadeIn(false);
            locationImageLoader.addImageCache(((FragmentActivity) getContext()).getSupportFragmentManager(), 0.1f);
        }

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        toolbar.setClickable(false);
        toolbar.setTitle(getString(R.string.applozic_message_info));
        toolbar.setSubtitle("");

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.dial).setVisible(false);
        menu.removeItem(R.id.start_new);
        menu.removeItem(R.id.conversations);
        menu.removeItem(R.id.deleteConversation);
        menu.removeItem(R.id.refresh);

    }

    private int getListPreferredItemHeight() {
        final TypedValue typedValue = new TypedValue();

        getActivity().getTheme().resolveAttribute(
                android.R.attr.listPreferredItemHeight, typedValue, true);
        final DisplayMetrics metrics = new DisplayMetrics();

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (int) typedValue.getDimension(metrics);
    }

    private void setupAttachmentView(Message message, RelativeLayout defaultRelativeLayout) {

        FileMeta fileMeta = message.getFileMetas();
        ImageView attachmentInconView = (ImageView) defaultRelativeLayout.findViewById(R.id.applozic_message_info_attachment_icon);
        TextView attachmentFilename = (TextView) defaultRelativeLayout.findViewById(R.id.applozic_message_info_attachment_filename);
        TextView messageText = (TextView) defaultRelativeLayout.findViewById(R.id.messageText);

        if (TextUtils.isEmpty(message.getMessage())) {
            messageText.setVisibility(View.GONE);
        }
        if (message.getMessage() != null) {
            messageText.setText(message.getMessage());
        }
        if (fileMeta.getContentType().contains("image")) {

            attachmentView.setVisibility(View.VISIBLE);
            attachmentInconView.setVisibility(View.GONE);
            attachmentFilename.setVisibility(View.GONE);

        } else {

            attachmentView.setVisibility(View.GONE);
            attachmentInconView.setVisibility(View.VISIBLE);
            attachmentFilename.setVisibility(View.VISIBLE);
            attachmentFilename.setText(fileMeta.getName());

        }

    }

    /**
     * Set up contectMessage
     *
     * @param message
     * @param mainContactShareLayout
     */
    private void setupContactShareView(final Message message, LinearLayout mainContactShareLayout) {
        mainContactShareLayout.setVisibility(View.VISIBLE);
        MobiComVCFParser parser = new MobiComVCFParser();
        try {

            VCFContactData data = parser.parseCVFContactData(message.getFilePaths().get(0));
            ImageView shareContactImage = (ImageView) mainContactShareLayout.findViewById(R.id.contact_share_image);
            TextView shareContactName = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_tv_name);
            TextView shareContactNo = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_tv_no);
            TextView shareEmailContact = (TextView) mainContactShareLayout.findViewById(R.id.contact_share_emailId);
            (mainContactShareLayout.findViewById(R.id.divider)).setVisibility(View.GONE);
            Button addContactButton = (Button) mainContactShareLayout.findViewById(R.id.contact_share_add_btn);
            addContactButton.setVisibility(View.GONE);
            shareContactName.setText(data.getName());

            if (data.getProfilePic() != null) {
                shareContactImage.setImageBitmap(data.getProfilePic());
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
        } catch (Exception e) {
            Log.e("DetailedConvAdapter", "Exception in parsing", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (messageInfoAsyncTask != null) {
            messageInfoAsyncTask.cancel(true);
        }
    }

    public class MessageInfoAsyncTask extends AsyncTask<Void, Integer, Long> {

        String messageKey;
        MobiComMessageService messageService;

        public MessageInfoAsyncTask(String messageKey, Context context) {
            this.messageKey = messageKey;
            this.messageService = new MobiComMessageService(context, MessageIntentService.class);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


        }

        @Override
        protected Long doInBackground(Void... params) {
            try {
                messageInfoResponse = messageService.getMessageInfoResponse(messageKey);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            //Populating view....

            if (!MessageInfoFragment.this.isVisible()) {
                return;
            }
            if (messageInfoResponse == null) {
                Toast.makeText(getContext(), getString(R.string.applozic_message_info_no_network), Toast.LENGTH_SHORT).show();
                return;
            }
            if (messageInfoResponse.getReadByUserList() != null) {
                ContactsAdapter readAdapter = new ContactsAdapter(messageInfoResponse.getReadByUserList());
                readListView.setAdapter(readAdapter);
            }


            if (messageInfoResponse.getDeliverdToUserList() != null) {
                ContactsAdapter deliveredAdapter = new ContactsAdapter(messageInfoResponse.getDeliverdToUserList());
                deliveredListView.setAdapter(deliveredAdapter);
            }


        }

    }

    public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {

        List<MessageInfo> messageInfoList;
        BaseContactService contactService;

        public ContactsAdapter(List<MessageInfo> messageInfoList) {
            this.contactService = new AppContactService(getContext());
            this.messageInfoList = messageInfoList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contact_users_layout, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            MessageInfo messageInfo = messageInfoList.get(position);
            String contactNumber;
            char firstLetter;
            Contact contact = contactService.getContactById(messageInfo.getUserId());
            holder.displayName.setText(contact.getDisplayName());
            long timeStamp = messageInfo.isRead() ? messageInfo.getReadAtTime() :
                    (messageInfo.getDeliveredAtTime() == null ? 0 : messageInfo.getDeliveredAtTime());
            if (timeStamp != 0) {

                holder.lastSeenAtTextView.setVisibility(View.VISIBLE);
                holder.lastSeenAtTextView.setText(String.valueOf(DateUtils.getDateAndTimeInDefaultFormat(timeStamp)));

            } else {
                holder.lastSeenAtTextView.setVisibility(View.GONE);
                holder.lastSeenAtTextView.setText("");
            }

            if (contact != null && !TextUtils.isEmpty(contact.getDisplayName())) {
                contactNumber = contact.getDisplayName().toUpperCase();
                firstLetter = contact.getDisplayName().toUpperCase().charAt(0);
                if (firstLetter != '+') {
                    holder.alphabeticImage.setText(String.valueOf(firstLetter));
                } else if (contactNumber.length() >= 2) {
                    holder.alphabeticImage.setText(String.valueOf(contactNumber.charAt(1)));
                }
                Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
                GradientDrawable bgShape = (GradientDrawable) holder.alphabeticImage.getBackground();
                bgShape.setColor(getContext().getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
            }

            if (contact.isDrawableResources()) {
                int drawableResourceId = getContext().getResources().getIdentifier(contact.getrDrawableName(), "drawable", getContext().getPackageName());
                holder.circleImageView.setImageResource(drawableResourceId);
            } else {
                contactImageLoader.loadImage(contact, holder.circleImageView, holder.alphabeticImage);
            }
        }

        @Override
        public int getItemCount() {
            return messageInfoList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView displayName, alphabeticImage, adminTextView, lastSeenAtTextView;
            CircleImageView circleImageView;

            public MyViewHolder(View view) {
                super(view);
                displayName = (TextView) view.findViewById(R.id.displayName);
                alphabeticImage = (TextView) view.findViewById(R.id.alphabeticImage);
                circleImageView = (CircleImageView) view.findViewById(R.id.contactImage);
                adminTextView = (TextView) view.findViewById(R.id.adminTextView);
                lastSeenAtTextView = (TextView) view.findViewById(R.id.lastSeenAtTextView);
            }
        }
    }
}