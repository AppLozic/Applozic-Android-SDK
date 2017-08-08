package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.RegisteredUsersAsyncTask;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.feed.RegisteredUsersApiResponse;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sunil on 3/2/16.
 */


public class ChannelCreateActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, MobicomkitUriListener, RemoveInterfaceListener {

    public static final String ACTION_FINISH_CHANNEL_CREATE =
            "channelCreateActivity.ACTION_FINISH";
    private static final int REQUEST_CODE_ATTACH_PHOTO = 901;
    private static final String TAG = "ChannelCreateActivity";
    public static String GROUP_TYPE = "GroupType";
    public static String CONTACTS_GROUP_ID = "ContactsGroupId";
    public String contactsGroupId;
    MobiComUserPreference userPreference;
    AlCustomizationSettings alCustomizationSettings;
    ConnectivityReceiver connectivityReceiver;
    File profilePhotoFile;
    FileClientService fileClientService;
    private EditText channelName;
    private CircleImageView circleImageView;
    private View focus;
    private ActionBar mActionBar;
    private ImageView uploadImageButton;
    private Uri imageChangeUri;
    private String groupIconImageLink;
    private int groupType;
    private LinearLayout layout;
    private Snackbar snackbar;
    private ApplozicPermissions applozicPermissions;
    private FinishActivityReceiver finishActivityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_create_activty_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        connectivityReceiver = new ConnectivityReceiver();
        userPreference = MobiComUserPreference.getInstance(ChannelCreateActivity.this);
        mActionBar = getSupportActionBar();
        if(!TextUtils.isEmpty(alCustomizationSettings.getThemeColorPrimary()) && !TextUtils.isEmpty(alCustomizationSettings.getThemeColorPrimaryDark())){
            mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(alCustomizationSettings.getThemeColorPrimary())));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor(alCustomizationSettings.getThemeColorPrimaryDark()));
            }
        }
        mActionBar.setTitle(R.string.channel_create_title);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        finishActivityReceiver = new FinishActivityReceiver();
        registerReceiver(finishActivityReceiver, new IntentFilter(ACTION_FINISH_CHANNEL_CREATE));
        layout = (LinearLayout) findViewById(R.id.footerAd);
        applozicPermissions = new ApplozicPermissions(this, layout);
        channelName = (EditText) findViewById(R.id.channelName);
        circleImageView = (CircleImageView) findViewById(R.id.channelIcon);
        uploadImageButton = (CircleImageView) findViewById(R.id.applozic_channel_profile_camera);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processImagePicker();
            }
        });

        int drawableResourceId = getResources().getIdentifier(alCustomizationSettings.getAttachCameraIconName(), "drawable", getPackageName());
        uploadImageButton.setImageResource(drawableResourceId);

        fileClientService = new FileClientService(this);
        if (getIntent() != null) {
            groupType = getIntent().getIntExtra(GROUP_TYPE, Channel.GroupType.PUBLIC.getValue().intValue());
            contactsGroupId = getIntent().getStringExtra(CONTACTS_GROUP_ID);
        }
       /* groupType = getIntent().getIntExtra(GROUP_TYPE, Channel.GroupType.PRIVATE.getValue().intValue());
        if(groupType.equals(Channel.GroupType.BROADCAST.getValue().intValue())){
            circleImageView.setImageResource(R.drawable.applozic_ic_applozic_broadcast);
            uploadImageButton.setVisibility(View.GONE);
        }*/
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_create_menu, menu);
        menu.removeItem(R.id.Done);
        menu.findItem(R.id.menu_search).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.Next) {
            boolean check = true;
            if (channelName.getText().toString().trim().length() == 0 || TextUtils.isEmpty(channelName.getText().toString())) {
                focus = channelName;
                focus.requestFocus();
                check = false;
            }
            if (check) {
                Utils.toggleSoftKeyBoard(ChannelCreateActivity.this, true);
                if (alCustomizationSettings.getTotalRegisteredUserToFetch() > 0 && (alCustomizationSettings.isRegisteredUserContactListCall() || ApplozicSetting.getInstance(this).isRegisteredUsersContactCall())&& !userPreference.getWasContactListServerCallAlreadyDone()) {
                    processDownloadRegisteredUsers();
                } else {
                    Intent intent = new Intent(ChannelCreateActivity.this, ContactSelectionActivity.class);
                    intent.putExtra(ContactSelectionActivity.CHANNEL, channelName.getText().toString());
                    if (!TextUtils.isEmpty(groupIconImageLink)) {
                        intent.putExtra(ContactSelectionActivity.IMAGE_LINK, groupIconImageLink);
                    }
                    if (contactsGroupId != null) {
                        intent.putExtra(ContactSelectionActivity.CONTACTS_GROUP_ID, contactsGroupId);
                    }
                    intent.putExtra(ContactSelectionActivity.GROUP_TYPE, groupType);
                    startActivity(intent);
                }

            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void processDownloadRegisteredUsers() {
        final ProgressDialog progressDialog = ProgressDialog.show(ChannelCreateActivity.this, "",
                getString(R.string.applozic_contacts_loading_info), true);

        RegisteredUsersAsyncTask.TaskListener usersAsyncTaskTaskListener = new RegisteredUsersAsyncTask.TaskListener() {
            @Override
            public void onSuccess(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                userPreference.setWasContactListServerCallAlreadyDone(true);
                Intent intent = new Intent(ChannelCreateActivity.this, ContactSelectionActivity.class);
                intent.putExtra(ContactSelectionActivity.CHANNEL, channelName.getText().toString());
                if (!TextUtils.isEmpty(groupIconImageLink)) {
                    intent.putExtra(ContactSelectionActivity.IMAGE_LINK, groupIconImageLink);
                }
                intent.putExtra(ContactSelectionActivity.GROUP_TYPE, groupType);
                startActivity(intent);

            }

            @Override
            public void onFailure(RegisteredUsersApiResponse registeredUsersApiResponse, String[] userIdArray, Exception exception) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                String error = getString(Utils.isInternetAvailable(ChannelCreateActivity.this) ? R.string.applozic_server_error : R.string.you_need_network_access_for_block_or_unblock);
                Toast toast = Toast.makeText(ChannelCreateActivity.this, error, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onCompletion() {

            }
        };
        RegisteredUsersAsyncTask usersAsyncTask = new RegisteredUsersAsyncTask(ChannelCreateActivity.this, usersAsyncTaskTaskListener, alCustomizationSettings.getTotalRegisteredUserToFetch(), userPreference.getRegisteredUsersLastFetchTime(), null, null, true);
        usersAsyncTask.execute((Void) null);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(intent);
                if (resultCode == RESULT_OK) {
                    if (intent == null) {
                        return;
                    }
                    if (imageChangeUri != null) {
                        imageChangeUri = result.getUri();
                        circleImageView.setImageDrawable(null); // <--- added to force redraw of ImageView
                        circleImageView.setImageURI(imageChangeUri);
                        new ProfilePictureUpload(true, profilePhotoFile, imageChangeUri, ChannelCreateActivity.this).execute((Void[]) null);
                    } else {
                        imageChangeUri = result.getUri();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
                        circleImageView.setImageDrawable(null); // <--- added to force redraw of ImageView
                        circleImageView.setImageURI(imageChangeUri);
                        profilePhotoFile = FileClientService.getFilePath(imageFileName, this, "image/jpeg");
                        new ProfilePictureUpload(true, profilePhotoFile, imageChangeUri, ChannelCreateActivity.this).execute((Void[]) null);
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this, this.getString(R.string.applozic_Cropping_failed) + result.getError(), Toast.LENGTH_LONG).show();
                }
            }
            if (resultCode == Activity.RESULT_OK) {
                handleOnActivityResult(requestCode, intent);
            }
        } catch (Exception e) {
            Utils.printLog(this, TAG, "exception in profile image");
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
    public void removeCallBack() {
        try {
            imageChangeUri = null;
            groupIconImageLink = null;
            circleImageView.setImageDrawable(null); // <--- added to force redraw of ImageView
            circleImageView.setImageResource(R.drawable.applozic_group_icon);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showSnackBar(int resId) {
        snackbar = Snackbar.make(layout, resId,
                Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (finishActivityReceiver != null) {
                unregisterReceiver(finishActivityReceiver);
            }
            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void processImagePicker() {

        if (PermissionsUtils.isCameraPermissionGranted(this) && !PermissionsUtils.checkSelfForStoragePermission(this)) {

            new Handler().post(new Runnable() {
                public void run() {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    DialogFragment fragment = PictureUploadPopUpFragment.newInstance(true, imageChangeUri == null);
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
    public Uri getCurrentImageUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
        profilePhotoFile = FileClientService.getFilePath(imageFileName, getApplicationContext(), "image/jpeg");
        imageChangeUri = FileProvider.getUriForFile(this, Utils.getMetaDataValue(this, MobiComKitConstants.PACKAGE_NAME) + ".provider", profilePhotoFile);
        return imageChangeUri;
    }

    class ProfilePictureUpload extends AsyncTask<Void, Void, Boolean> {

        Context context;
        Uri fileUri;
        String displayName;
        File file;
        boolean isSaveFile;
        FileClientService fileClientService;
        private ProgressDialog progressDialog;

        public ProfilePictureUpload(boolean isSaveFile, File file, Uri fileUri, Context context) {
            this.context = context;
            this.fileUri = fileUri;
            this.file = file;
            this.isSaveFile = isSaveFile;
            this.fileClientService = new FileClientService(context);

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
                if (fileUri != null) {
                    String filePath = file.getAbsolutePath();
                    if (isSaveFile) {
                        fileClientService.writeFile(fileUri, file);
                    }
                    groupIconImageLink = fileClientService.uploadProfileImage(filePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

    }

    private final class FinishActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals(ACTION_FINISH_CHANNEL_CREATE))
                finish();
        }
    }

}
