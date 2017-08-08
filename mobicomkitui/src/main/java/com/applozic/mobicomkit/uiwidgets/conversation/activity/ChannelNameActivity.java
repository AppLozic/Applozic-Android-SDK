package com.applozic.mobicomkit.uiwidgets.conversation.activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.feed.GroupInfoUpdate;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.PictureUploadPopUpFragment;
import com.applozic.mobicomkit.uiwidgets.instruction.ApplozicPermissions;
import com.applozic.mobicomkit.uiwidgets.people.fragment.ProfileFragment;
import com.applozic.mobicomkit.uiwidgets.uilistener.MobicomkitUriListener;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Channel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sunil on 10/3/16.
 */
public class ChannelNameActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, MobicomkitUriListener, RemoveInterfaceListener {

    public static final String CHANNEL_NAME = "CHANNEL_NAME";
    public static final String CHANNEL_IMAGE_URL = "IMAGE_URL";
    public static final int REQUEST_CODE_ATTACH_PHOTO = 101;
    private static final String TAG = "ChannelNameActivity";
    String oldChannelName;
    ActionBar mActionBar;
    GroupInfoUpdate groupInfoUpdate;
    File profilePhotoFile;
    FileClientService fileClientService;
    private EditText channelName;
    private Button ok, cancel;
    private ImageView selectImageProfileIcon;
    private ImageView applozicGroupProfileIcon;
    private LinearLayout layout;
    private Uri imageChangeUri;
    private Snackbar snackbar;
    private ApplozicPermissions applozicPermissions;
    private AlCustomizationSettings alCustomizationSettings;
    private ConnectivityReceiver connectivityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_channel_name_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        layout = (LinearLayout) findViewById(R.id.footerAd);
        applozicPermissions = new ApplozicPermissions(this, layout);
        mActionBar.setTitle(getString(R.string.update_channel_title_name));
        selectImageProfileIcon = (CircleImageView) findViewById(R.id.applozic_group_profile_camera);
        applozicGroupProfileIcon = (ImageView) findViewById(R.id.applozic_group_profile);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        fileClientService = new FileClientService(this);
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }

        if(!TextUtils.isEmpty(alCustomizationSettings.getThemeColorPrimary()) && !TextUtils.isEmpty(alCustomizationSettings.getThemeColorPrimaryDark())){
            mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(alCustomizationSettings.getThemeColorPrimary())));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor(alCustomizationSettings.getThemeColorPrimaryDark()));
            }
        }
        int drawableResourceId = getResources().getIdentifier(alCustomizationSettings.getAttachCameraIconName(), "drawable", getPackageName());
        selectImageProfileIcon.setImageResource(drawableResourceId);

        if (getIntent().getExtras() != null) {
            String groupInfoJson = getIntent().getExtras().getString(ChannelInfoActivity.GROUP_UPDTAE_INFO);
            groupInfoUpdate = (GroupInfoUpdate) GsonUtils.getObjectFromJson(groupInfoJson, GroupInfoUpdate.class);
        }

        if (groupInfoUpdate != null && !TextUtils.isEmpty(groupInfoUpdate.getLocalImagePath())) {
            File file = new File(groupInfoUpdate.getLocalImagePath());
            Uri uri = Uri.parse(file.getAbsolutePath());
            if (uri != null) {
                Utils.printLog(this,"ChannelNameActivity::", uri.toString());
                applozicGroupProfileIcon.setImageURI(uri);
            }
        } else {
            applozicGroupProfileIcon.setImageResource(R.drawable.applozic_group_icon);

        }
        channelName = (EditText) findViewById(R.id.newChannelName);
        channelName.setText(groupInfoUpdate.getNewName());
        ok = (Button) findViewById(R.id.channelNameOk);
        cancel = (Button) findViewById(R.id.channelNameCancel);
        selectImageProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImagePicker();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (channelName.getText().toString().equals(groupInfoUpdate.getNewName()) && imageChangeUri == null || groupInfoUpdate.getNewName() == null) {
                    ChannelNameActivity.this.finish();
                }
                if (TextUtils.isEmpty(channelName.getText().toString()) || channelName.getText().toString().trim().length() == 0) {

                    Toast.makeText(ChannelNameActivity.this, getString(R.string.channel_name_empty), Toast.LENGTH_SHORT).show();
                    ChannelNameActivity.this.finish();

                } else {
                    Intent intent = new Intent();
                    groupInfoUpdate.setNewName(channelName.getText().toString());
                    if (imageChangeUri != null && profilePhotoFile != null) {
                        groupInfoUpdate.setNewlocalPath(profilePhotoFile.getAbsolutePath());
                        groupInfoUpdate.setContentUri(imageChangeUri.toString());
                    }
                    intent.putExtra(ChannelInfoActivity.GROUP_UPDTAE_INFO, GsonUtils.getJsonFromObject(groupInfoUpdate, GroupInfoUpdate.class));
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelNameActivity.this.finish();

            }
        });
        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionsUtils.REQUEST_STORAGE) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.storage_permission_granted);
                processImagePicker();
            } else {
                showSnackBar(R.string.storage_permission_not_granted);
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSnackBar(R.string.phone_camera_permission_granted);
                processImagePicker();
            } else {
                showSnackBar(R.string.phone_camera_permission_not_granted);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(intent);
            if (resultCode == RESULT_OK) {
                if (intent == null) {
                    return;
                }
                if (imageChangeUri != null) {
                    imageChangeUri = result.getUri();
                    applozicGroupProfileIcon.setImageDrawable(null); // <--- added to force redraw of ImageView
                    applozicGroupProfileIcon.setImageURI(imageChangeUri);
                } else {
                    imageChangeUri = result.getUri();
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
                    applozicGroupProfileIcon.setImageDrawable(null); // <--- added to force redraw of ImageView
                    applozicGroupProfileIcon.setImageURI(imageChangeUri);
                    profilePhotoFile = FileClientService.getFilePath(imageFileName, this, "image/jpeg");
                    fileClientService.writeFile(imageChangeUri, profilePhotoFile);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Utils.printLog(this,ChannelNameActivity.class.getName(),this.getString(R.string.applozic_Cropping_failed)+result.getError());
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            handleOnActivityResult(requestCode, intent);
        }
    }

    public void handleOnActivityResult(int requestCode, Intent intent) {

        switch (requestCode) {

            case ProfileFragment.REQUEST_CODE_ATTACH_PHOTO:
                Uri selectedFileUri = (intent == null ? null : intent.getData());
                imageChangeUri = null;
                beginCrop(selectedFileUri);
                break;

            case ProfileFragment.REQUEST_CODE_TAKE_PHOTO:
                beginCrop(imageChangeUri);
                break;
        }
    }

    void beginCrop(Uri imageUri) {
        try {
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.OFF)
                    .setMultiTouchEnabled(true)
                    .start(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public Uri getCurrentImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
        profilePhotoFile = FileClientService.getFilePath(imageFileName, getApplicationContext(), "image/jpeg");
        imageChangeUri = FileProvider.getUriForFile(this, Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME) + ".provider", profilePhotoFile);
        return imageChangeUri;
    }

    public void showSnackBar(int resId) {
        snackbar = Snackbar.make(layout, resId,
                Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void processImagePicker() {

        if (PermissionsUtils.isCameraPermissionGranted(this) && !PermissionsUtils.checkSelfForStoragePermission(this)) {

            new Handler().post(new Runnable() {
                public void run() {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    Channel channel = ChannelService.getInstance(ChannelNameActivity.this).getChannel(groupInfoUpdate.getGroupId());
                    DialogFragment fragment = PictureUploadPopUpFragment.newInstance(true, TextUtils.isEmpty(channel.getImageUrl()));
                    FragmentTransaction fragmentTransaction = supportFragmentManager
                            .beginTransaction();
                    Fragment prev = getSupportFragmentManager().findFragmentByTag("PhotosAttachmentFragment");
                    if (prev != null) {
                        fragmentTransaction.remove(prev);
                    }
                    fragmentTransaction.addToBackStack(null);
                    fragment.show(fragmentTransaction, "PhotosAttachmentFragment");
                }
            });

        } else {
            if (Utils.hasMarshmallow()) {
                if (PermissionsUtils.checkSelfForCameraPermission(this)) {
                    applozicPermissions.requestCameraPermission();
                } else {
                    applozicPermissions.requestStoragePermissions();
                }
            } else {
                processImagePicker();
            }
        }
    }

    @Override
    public void removeCallBack() {
        try {
            new ProfilePictureUpload(this, applozicGroupProfileIcon, groupInfoUpdate).execute((Void) null);

        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ProfilePictureUpload extends AsyncTask<Void, Void, Boolean> {
        Context context;
        WeakReference<ImageView> weakReferenceImageView;
        FileClientService fileClientService;
        ChannelService channelService;
        String responseForChannelUpdate;
        WeakReference<GroupInfoUpdate> groupInfoUpdateWeakReference;
        private ProgressDialog progressDialog;

        public ProfilePictureUpload(Context context, ImageView imageView, GroupInfoUpdate groupInfoUpdate) {
            this.context = context;
            this.weakReferenceImageView = new WeakReference<ImageView>(imageView);
            this.fileClientService = new FileClientService(context);
            this.channelService = ChannelService.getInstance(context);
            this.groupInfoUpdateWeakReference = new WeakReference<GroupInfoUpdate>(groupInfoUpdate);

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
                GroupInfoUpdate giu = groupInfoUpdateWeakReference.get();
                if (giu != null) {
                    giu.setImageUrl("");
                    giu.setNewName(null);
                    responseForChannelUpdate = channelService.updateChannel(giu);
                    if (!TextUtils.isEmpty(responseForChannelUpdate) && MobiComKitConstants.SUCCESS.equals(responseForChannelUpdate)) {
                        channelService.updateChannelLocalImageURI(giu.getGroupId(), null);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            progressDialog.dismiss();
            if (!TextUtils.isEmpty(responseForChannelUpdate) && MobiComKitConstants.SUCCESS.equals(responseForChannelUpdate)) {
                ImageView imageView = weakReferenceImageView.get();
                if (imageView != null) {
                    imageChangeUri = null;
                    imageView.setImageDrawable(null); // <--- added to force redraw of ImageView
                    imageView.setImageResource(R.drawable.applozic_group_icon);
                }
            }
        }

    }

}
