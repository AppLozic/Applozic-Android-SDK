package com.applozic.mobicomkit.uiwidgets.people.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applozic.mobicomkit.broadcast.BroadcastService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.alphanumbericcolor.AlphaNumberColorUtil;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.people.contact.Contact;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sunil on 27/5/16.
 */
public class UserProfileFragment extends Fragment {

    Contact contact;
    CardView name_cardView, email_cardView, status_cardView, phone_cardView;
    TextView name, email, phone, status;
    ImageLoader contactImageLoader;
    TextView alphabeticTextView;
    CircleImageView contactImage;
    AppContactService baseContactService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseContactService = new AppContactService(getActivity());
        final Context context = getActivity().getApplicationContext();
        contactImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) getContext())) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return baseContactService.downloadContactImage(context, (Contact) data);
            }
        };
        contactImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_180_holo_light);
        contactImageLoader.addImageCache((getActivity()).getSupportFragmentManager(), 0.1f);
        contactImageLoader.setImageFadeIn(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.user_profile_fragment_layout, container, false);
        name_cardView = (CardView) view.findViewById(R.id.applzoic_name_cardView);
        email_cardView = (CardView) view.findViewById(R.id.applzoic_email_cardview);
        status_cardView = (CardView) view.findViewById(R.id.applzoic_last_sean_status_cardView);
        phone_cardView = (CardView) view.findViewById(R.id.applozic_user_phone_cardview);
        name = (TextView) view.findViewById(R.id.userName);
        status = (TextView) view.findViewById(R.id.applozic_user_status);
        email = (TextView) view.findViewById(R.id.email);
        phone = (TextView) view.findViewById(R.id.phone);
        contactImage = (CircleImageView) view.findViewById(R.id.contactImage);
        alphabeticTextView = (TextView) view.findViewById(R.id.alphabeticImage);

        Bundle bundle = getArguments();
        if (bundle != null) {
            contact = (Contact) bundle.getSerializable(ConversationUIService.CONTACT);
            contact = baseContactService.getContactById(contact.getContactIds());
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(contact.getDisplayName());
            ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle("");
            name.setText(contact.getDisplayName());
            char firstLetter = contact.getDisplayName().toUpperCase().charAt(0);
            String contactNumber = contact.getDisplayName().toUpperCase();
            if (firstLetter != '+') {
                alphabeticTextView.setText(String.valueOf(firstLetter));
            } else if (contactNumber.length() >= 2) {
                alphabeticTextView.setText(String.valueOf(contactNumber.charAt(1)));
            }
            Character colorKey = AlphaNumberColorUtil.alphabetBackgroundColorMap.containsKey(firstLetter) ? firstLetter : null;
            GradientDrawable bgShape = (GradientDrawable) alphabeticTextView.getBackground();
            bgShape.setColor(getActivity().getResources().getColor(AlphaNumberColorUtil.alphabetBackgroundColorMap.get(colorKey)));
            if (contact.isDrawableResources()) {
                int drawableResourceId = getResources().getIdentifier(contact.getrDrawableName(), "drawable", getActivity().getPackageName());
                contactImage.setImageResource(drawableResourceId);
            } else {
                contactImageLoader.loadImage(contact, contactImage, alphabeticTextView);
            }


            name.setText(contact.getDisplayName());

            if (!TextUtils.isEmpty(contact.getEmailId())) {
                email_cardView.setVisibility(View.VISIBLE);
                email.setText(contact.getEmailId());
            }
            if (!TextUtils.isEmpty(contact.getStatus())) {
                status_cardView.setVisibility(View.VISIBLE);
                status.setText(contact.getStatus());
            }
            if (!TextUtils.isEmpty(contact.getContactNumber())) {
                phone_cardView.setVisibility(View.VISIBLE);
                phone.setText(contact.getContactNumber());
            } else {
                phone_cardView.setVisibility(View.GONE);
            }

        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (contact != null) {
            BroadcastService.currentUserProfileUserId = contact.getUserId();
            refreshContactData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BroadcastService.currentUserProfileUserId = null;
    }

    public void refreshContactData() {
        if (contact != null) {
            Contact updateContact = baseContactService.getContactById(contact.getContactIds());
            if (updateContact != null && (!TextUtils.isEmpty(contact.getImageURL())) && (!contact.getImageURL().equals(updateContact.getImageURL()))) {
                contactImageLoader.loadImage(updateContact, contactImage);
            }
            if (!TextUtils.isEmpty(updateContact.getStatus())) {
                status_cardView.setVisibility(View.VISIBLE);
                status.setText(updateContact.getStatus());
            }

            if (!TextUtils.isEmpty(updateContact.getContactNumber())) {
                phone_cardView.setVisibility(View.VISIBLE);
                phone.setText(updateContact.getContactNumber());
            }
            if (updateContact != null && (!TextUtils.isEmpty(contact.getDisplayName())) && (!contact.getDisplayName().equals(updateContact.getDisplayName()))) {
                name_cardView.setVisibility(View.VISIBLE);
                name.setText(updateContact.getDisplayName());
                reload();
            }
        }
    }

    void reload() {
        StringBuffer stringBufferTitle = new StringBuffer();
        if (contact != null) {
            Contact updatedInfoContact = baseContactService.getContactById(contact.getContactIds());
            if (updatedInfoContact != null && (!TextUtils.isEmpty(contact.getDisplayName())) && (!contact.getDisplayName().equals(updatedInfoContact.getDisplayName()))) {
                stringBufferTitle.append(updatedInfoContact.getDisplayName());
            }
        }
        if (stringBufferTitle != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(stringBufferTitle.toString());
        }
    }

}
