package com.applozic.mobicomkit.uiwidgets.people.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserClientService;
import com.applozic.mobicomkit.api.account.user.UserService;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.PictureUploadPopUpFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.ApplozicPermissions;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.people.contact.Contact;

import java.io.File;
import java.lang.ref.WeakReference;


public class ProfileFragment extends Fragment {

    public static final int REQUEST_CODE_ATTACH_PHOTO = 101;
    public static final int REQUEST_CODE_TAKE_PHOTO = 102;
    public static final int REQUEST_REMOVE_PHOTO = 102;
    public static final String ProfileFragmentTag = "ProfileFragment";
    public static final int PROFILE_UPDATED = 1001;
    public static final int LINE_WIDTH = 2;
    public static final float LEFT_MARGIN = 7.0f;
    private static final String TAG = "ProfileFragment";
    AppContactService contactService;
    Contact userContact;
    AlCustomizationSettings alCustomizationSettings;
    private ImageView img_profile;
    private ImageView selectImageProfileIcon, statusEdit, displayNameEdit, contactEdit;
    private Button logoutbtn;
    private TextView displayNameText;
    private TextView statusText;
    private TextView contactNumberText;
    private String DEFAULT_CONATCT_IMAGE = "applozic_default_contactImg.jpeg";
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread
    private String changedStatusString;
    private String displayName;
    private String contactNumber;
    private ApplozicPermissions applozicPermissions;
    private RelativeLayout applozicProfileContactLayout;

    public void setApplozicPermissions(ApplozicPermissions applozicPermissions) {
        this.applozicPermissions = applozicPermissions;
    }

    public void setAlCustomizationSettings(AlCustomizationSettings alCustomizationSettings) {
        this.alCustomizationSettings = alCustomizationSettings;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.al_activity_profile, container, false);
        img_profile = (ImageView) view.findViewById(R.id.applozic_user_profile);
        statusEdit = (ImageView) view.findViewById(R.id.status_edit_btn);
        displayNameEdit = (ImageView) view.findViewById(R.id.displayName_edit_btn);
        contactEdit = (ImageView) view.findViewById(R.id.contact_edit_btn);
        selectImageProfileIcon = (ImageView) view.findViewById(R.id.applozic_user_profile_camera);
        logoutbtn = (Button) view.findViewById(R.id.applozic_profile_logout);
        displayNameText = (TextView) view.findViewById(R.id.applozic_profile_displayname);
        statusText = (TextView) view.findViewById(R.id.applozic_profile_status);
        contactNumberText = (TextView) view.findViewById(R.id.applozic_profile_contact);
        applozicProfileContactLayout =  (RelativeLayout)view.findViewById(R.id.applozic_profile_contact_section_rl);

        setupDeviderView(view, R.id.applozic_profile_section_rl, R.id.applozic_profile_verticalline_rl);
        setupDeviderView(view, R.id.applozic_datausage_section_rl, R.id.applozic_datausage_verticalline_rl);
        setupDeviderView(view, R.id.applozic_notification_section_rl, R.id.applozic_notification_verticalline_rl);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        toolbar.setClickable(false);
        toolbar.setTitle(getString(R.string.applozic_user_profile_heading));
        toolbar.setSubtitle("");
        setHasOptionsMenu(true);

        contactService = new AppContactService(getActivity());
        userContact = contactService.getContactById(MobiComUserPreference.getInstance(getActivity()).getUserId());
        if (!TextUtils.isEmpty(userContact.getDisplayName())) {
            displayNameText.setText(userContact.getDisplayName());
        }
        if (!TextUtils.isEmpty(userContact.getStatus())) {
            statusText.setText(userContact.getStatus());
        }
        if (!TextUtils.isEmpty(userContact.getContactNumber())) {
            contactNumberText.setText(userContact.getContactNumber());
        }else {
            applozicProfileContactLayout.setVisibility(View.GONE);
        }

        final Context context = getActivity().getApplicationContext();
        mImageLoader = new ImageLoader(context, img_profile.getHeight()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return contactService.downloadContactImage(context, (Contact) data);
            }
        };
        //For profile image
        selectImageProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPhotoOption();
            }
        });

        logoutbtn.setVisibility(View.GONE);
        logoutbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    final String logoutActivity = ApplozicSetting.getInstance(getActivity()).getActivityCallback(ApplozicSetting.RequestCode.USER_LOOUT);
                    if (!TextUtils.isEmpty(logoutActivity)) {
                        new UserClientService(getActivity()).logout();
                        Intent intent = new Intent(getActivity(), Class.forName(logoutActivity));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        getActivity().finish();
                        return;
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        statusEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.status);

                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton(R.string.ok_alert, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changedStatusString = input.getText().toString();
                        Contact contact = new Contact();
                        contact.setStatus(changedStatusString);
                        new ProfilePictureUpload(contact, getActivity(), null, statusText, null).execute((Void[]) null);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        displayNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.display_name));
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                builder.setView(input);


                builder.setPositiveButton(getString(R.string.ok_alert), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        displayName = input.getText().toString();
                        if (!displayName.trim().isEmpty() && !TextUtils.isEmpty(displayName)) {
                            Contact contact = new Contact();
                            contact.setFullName(displayName);
                            new ProfilePictureUpload(contact, getActivity(), displayNameText, null, null).execute((Void[]) null);
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        contactEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.profile_contact));
                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
                builder.setView(input);


                builder.setPositiveButton(getString(R.string.ok_alert), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        contactNumber = input.getText().toString();

                        if (!contactNumber.trim().isEmpty() && !TextUtils.isEmpty(contactNumber)) {
                            Contact contact = new Contact();
                            contact.setContactNumber(contactNumber);
                            new ProfilePictureUpload(contact, getActivity(), null, null, contactNumberText).execute((Void[]) null);
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        mImageLoader.setImageFadeIn(false);
        mImageLoader.setLoadingImage(R.drawable.applozic_ic_contact_picture_180_holo_light);
        mImageLoader.loadImage(userContact, img_profile);
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.refresh).setVisible(false);
        menu.findItem(R.id.menu_search).setVisible(false);
    }

    private void setupDeviderView(View view, int parentLayout, int childVerticalLineLayout) {
        final RelativeLayout layout = (RelativeLayout) view.findViewById(parentLayout);
        final RelativeLayout childLayout = (RelativeLayout) view.findViewById(childVerticalLineLayout);
        ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();


        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int height = layout.getMeasuredHeight();
                float marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LEFT_MARGIN, getActivity().getResources().getDisplayMetrics());
                float liineWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, LINE_WIDTH, getActivity().getResources().getDisplayMetrics());
                RelativeLayout.LayoutParams layoutPrams = new RelativeLayout.LayoutParams((int) liineWidth, height);
                layoutPrams.setMargins((int) marginPx, 0, 0, 0);
                childLayout.setLayoutParams(layoutPrams);
            }
        });
    }


    public void processPhotoOption() {
        try {
        if (PermissionsUtils.isCameraPermissionGranted(getContext()) && !PermissionsUtils.checkSelfForStoragePermission(getActivity())) {

            new Handler().post(new Runnable() {
                public void run() {
                    FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
                    DialogFragment fragment = new PictureUploadPopUpFragment();
                    fragment.setTargetFragment(ProfileFragment.this, REQUEST_CODE_ATTACH_PHOTO);
                    FragmentTransaction fragmentTransaction = supportFragmentManager
                            .beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag("PhotosAttachmentFragment");
                    if (prev != null) {
                        fragmentTransaction.remove(prev);
                    }
                    fragmentTransaction.addToBackStack(null);
                    fragment.show(fragmentTransaction, "PhotosAttachmentFragment");
                }
            });

        } else {
            if (Utils.hasMarshmallow()) {
                if (PermissionsUtils.checkSelfForCameraPermission(getActivity())) {
                    applozicPermissions.requestCameraPermissionForProfilePhoto();
                } else {
                    applozicPermissions.requestStoragePermissionsForProfilePhoto();
                }
            } else {
                processPhotoOption();
            }
        }
        } catch (Exception e) {

        }
    }

    public void handleProfileimageUpload(boolean isSaveFile, Uri imageUri, File file) {
        img_profile.setImageDrawable(null);
        img_profile.setImageURI(imageUri);
        new ProfilePictureUpload(isSaveFile, imageUri, file, getActivity()).execute((Void[]) null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, intent);
            File file = FileClientService.getFilePath(DEFAULT_CONATCT_IMAGE, getContext(), "image", true);
            if (file == null || !file.exists()) {
                Utils.printLog(getActivity(),TAG, "file not found,exporting it from drawable");
                Bitmap bm = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.applozic_ic_contact_picture_180_holo_light);
                String filePath = ImageUtils.saveImageToInternalStorage(FileClientService.getFilePath(DEFAULT_CONATCT_IMAGE, getActivity().getApplicationContext(), "image", true), bm);
                file = new File(filePath);
            }
            handleProfileimageUpload(false, Uri.parse(file.getAbsolutePath()), file);
        } else {
            Utils.printLog(getActivity(),TAG, "Activity result failed with code: " + resultCode);
        }
    }

    class ProfilePictureUpload extends AsyncTask<Void, Void, Boolean> {

        Context context;
        Uri fileUri;
        String displayName;
        String status;
        File file;
        FileClientService fileClientService;
        UserService userService;
        boolean isSaveFile;
        WeakReference<TextView> weakReferenceStatus;
        WeakReference<TextView> weakReferenceDisplayName;
        WeakReference<TextView> weakReferenceContactNumber;
        String contactNumber;
        private ProgressDialog progressDialog;


        public ProfilePictureUpload(boolean isSaveFile, Uri fileUri, File file, Context context) {
            this.context = context;
            this.fileUri = fileUri;
            this.isSaveFile = isSaveFile;
            this.file = file;
            this.fileClientService = new FileClientService(getActivity());
            this.userService = UserService.getInstance(context);

        }

        public ProfilePictureUpload(Contact contact, Context context, TextView displayNameTextView, TextView statusTextView, TextView contactNumberTextView) {
            this.context = context;
            this.status = contact.getStatus();
            this.displayName = contact.getFullName();
            this.contactNumber = contact.getContactNumber();
            this.weakReferenceStatus = new WeakReference<TextView>(statusTextView);
            this.weakReferenceDisplayName = new WeakReference<TextView>(displayNameTextView);
            this.weakReferenceContactNumber = new WeakReference<TextView>(contactNumberTextView);
            this.fileClientService = new FileClientService(getActivity());
            this.userService = UserService.getInstance(context);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, "",
                    context.getString(R.string.applozic_contacts_loading_info), true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String response = null;
                String filePath = null;
                if (fileUri != null) {
                    if (isSaveFile) {
                        fileClientService.writeFile(fileUri, file);
                    }
                    response = fileClientService.uploadProfileImage(file.getAbsolutePath());
                    filePath = file.getAbsolutePath();
                }
                userService.updateDisplayNameORImageLink(displayName, response, filePath, status, contactNumber);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.printLog(context, ProfileFragment.class.getName(), "Exception");

            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (weakReferenceStatus != null) {
                TextView statusTextView = weakReferenceStatus.get();
                if (statusTextView != null && !TextUtils.isEmpty(changedStatusString)) {
                    statusTextView.setText(changedStatusString);
                }
            }
            if (weakReferenceDisplayName != null) {
                TextView displayNameTextView = weakReferenceDisplayName.get();
                if (displayNameTextView != null && !TextUtils.isEmpty(displayName)) {
                    displayNameTextView.setText(displayName);
                }
            }

            if (weakReferenceContactNumber != null) {
                TextView contactNumberTextView = weakReferenceContactNumber.get();
                if (contactNumberTextView != null && !TextUtils.isEmpty(contactNumber)) {
                    contactNumberTextView.setText(contactNumber);
                }
            }
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }

    }

}
