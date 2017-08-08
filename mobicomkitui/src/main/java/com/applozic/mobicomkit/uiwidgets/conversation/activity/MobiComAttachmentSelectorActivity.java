package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.adapter.MobiComAttachmentGridViewAdapter;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FilePathFinder;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class MobiComAttachmentSelectorActivity extends AppCompatActivity {

    public static final String MULTISELECT_SELECTED_FILES = "multiselect.selectedFiles";
    public static final String MULTISELECT_MESSAGE = "multiselect.message";
    public static final String URI_LIST = "URI_LIST";
    public static String USER_ID = "USER_ID";
    public static String DISPLAY_NAME = "DISPLAY_NAME";
    public static String GROUP_ID = "GROUP_ID";
    public static String GROUP_NAME = "GROUP_NAME";
    private static int REQUEST_CODE_ATTACH_PHOTO = 10;
    AlCustomizationSettings alCustomizationSettings;
    FileClientService fileClientService;
    Uri imageUri;
    String userID, displayName, groupName;
    Integer groupID;
    Message message;
    MobiComUserPreference userPreferences;
    private String TAG = "MultiAttActivity";
    private Button sendAttachment;
    private Button cancelAttachment;
    private EditText messageEditText;
    private ConnectivityReceiver connectivityReceiver;
    private GridView galleryImagesGridView;
    private ArrayList<Uri> attachmentFileList = new ArrayList<Uri>();
    private MobiComAttachmentGridViewAdapter imagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobicom_multi_attachment_activity);
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }

        fileClientService = new FileClientService(this);
        userPreferences = MobiComUserPreference.getInstance(this);
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            userID = intent.getExtras().getString(USER_ID);
            displayName = intent.getExtras().getString(DISPLAY_NAME);
            groupID = intent.getExtras().getInt(GROUP_ID, 0);
            groupName = intent.getExtras().getString(GROUP_NAME);
            imageUri = (Uri) intent.getParcelableExtra(URI_LIST);
            if (imageUri != null) {
                attachmentFileList.add(imageUri);
            }
        }
        initViews();
        setUpGridView();
        fileClientService = new FileClientService(this);
        if (imageUri == null) {
            Intent getContentIntent = FileUtils.createGetContentIntent();
            getContentIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            Intent intentPick = Intent.createChooser(getContentIntent, getString(R.string.select_file));
            startActivityForResult(intentPick, REQUEST_CODE_ATTACH_PHOTO);
        }
        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * views initialisation.
     */
    private void initViews() {

        sendAttachment = (Button) findViewById(R.id.mobicom_attachment_send_btn);
        cancelAttachment = (Button) findViewById(R.id.mobicom_attachment_cancel_btn);
        galleryImagesGridView = (GridView) findViewById(R.id.mobicom_attachment_grid_View);
        messageEditText = (EditText) findViewById(R.id.mobicom_attachment_edit_text);


        cancelAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        sendAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (attachmentFileList.isEmpty()) {
                    Toast.makeText(getApplicationContext(), R.string.mobicom_select_attachment_text, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (imageUri != null) {
                    for (Uri uri : attachmentFileList) {
                        try {
                            String filePath = uri.getPath();
                            if (TextUtils.isEmpty(filePath)) {
                                Toast.makeText(MobiComAttachmentSelectorActivity.this, R.string.info_file_attachment_error, Toast.LENGTH_LONG).show();
                                return;
                            }
                            Message messageToSend = new Message();
                            if (groupID != 0) {
                                messageToSend.setGroupId(groupID);
                            } else {
                                messageToSend.setTo(userID);
                                messageToSend.setContactIds(userID);
                            }
                            messageToSend.setContentType(Message.ContentType.ATTACHMENT.getValue());
                            messageToSend.setRead(Boolean.TRUE);
                            messageToSend.setStoreOnDevice(Boolean.TRUE);
                            if (messageToSend.getCreatedAtTime() == null) {
                                messageToSend.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
                            }
                            messageToSend.setSendToDevice(Boolean.FALSE);
                            messageToSend.setType(Message.MessageType.MT_OUTBOX.getValue());
                            messageToSend.setMessage(messageEditText.getText().toString());
                            messageToSend.setDeviceKeyString(userPreferences.getDeviceKeyString());
                            messageToSend.setSource(Message.Source.MT_MOBILE_APP.getValue());
                            if (!TextUtils.isEmpty(filePath)) {
                                List<String> filePaths = new ArrayList<String>();
                                filePaths.add(filePath);
                                messageToSend.setFilePaths(filePaths);
                            }
                            Intent startConversationActivity = new Intent(MobiComAttachmentSelectorActivity.this, ConversationActivity.class);
                            if (groupID != 0) {
                                startConversationActivity.putExtra(ConversationUIService.GROUP_ID, groupID);
                                startConversationActivity.putExtra(ConversationUIService.GROUP_NAME, groupName);
                                startConversationActivity.putExtra(ConversationUIService.FORWARD_MESSAGE, GsonUtils.getJsonFromObject(messageToSend, messageToSend.getClass()));
                            } else {
                                startConversationActivity.putExtra(ConversationUIService.USER_ID, userID);
                                startConversationActivity.putExtra(ConversationUIService.DISPLAY_NAME, displayName);
                                startConversationActivity.putExtra(ConversationUIService.FORWARD_MESSAGE, GsonUtils.getJsonFromObject(messageToSend, messageToSend.getClass()));
                            }
                            startActivity(startConversationActivity);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    Intent intent = new Intent();
                    intent.putParcelableArrayListExtra(MULTISELECT_SELECTED_FILES, attachmentFileList);
                    intent.putExtra(MULTISELECT_MESSAGE, messageEditText.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }


            }
        });

    }

    /**
     * @param uri
     */
    private void addUri(Uri uri) {

        attachmentFileList.add(uri);
        Utils.printLog(MobiComAttachmentSelectorActivity.this,TAG, "attachmentFileList  :: " + attachmentFileList);


    }

    /**
     *
     */
    private void setUpGridView() {
        imagesAdapter = new MobiComAttachmentGridViewAdapter(MobiComAttachmentSelectorActivity.this, attachmentFileList, alCustomizationSettings, imageUri != null);
        galleryImagesGridView.setAdapter(imagesAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Activity.RESULT_OK) {
            Uri selectedFileUri = (intent == null ? null : intent.getData());
            Utils.printLog(MobiComAttachmentSelectorActivity.this,TAG, "selectedFileUri :: " + selectedFileUri);
            if(selectedFileUri != null) {
                String fileName = null;
                try {
                    int maxFileSize = alCustomizationSettings.getMaxAttachmentSizeAllowed() * 1024 * 1024;
                    Cursor returnCursor =
                            getContentResolver().query(selectedFileUri, null, null, null, null);
                    if (returnCursor != null) {
                        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        returnCursor.moveToFirst();
                        Long fileSize = returnCursor.getLong(sizeIndex);
                        returnCursor.close();
                        if (fileSize > maxFileSize) {
                            Toast.makeText(this, R.string.info_attachment_max_allowed_file_size, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    String mimeType = FileUtils.getMimeTypeByContentUriOrOther(this, selectedFileUri);
                    if (TextUtils.isEmpty(mimeType)) {
                        return;
                    }
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    fileName = FileUtils.getFileName(this, selectedFileUri);
                    String fileFormat = FileUtils.getFileFormat(fileName);
                    if (TextUtils.isEmpty(fileFormat)) {
                        return;
                    }
                    String fileNameToWrite = timeStamp + "." + fileFormat;
                    File mediaFile = FileClientService.getFilePath(fileNameToWrite, getApplicationContext(), mimeType);
                    new FileTaskAsync(mediaFile, selectedFileUri, this).execute((Void) null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
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

    public class FileTaskAsync extends AsyncTask<Void, Integer, Boolean> {
        Context context;
        FileClientService fileClientService;
        File file;
        Uri uri;
        ProgressDialog progressDialog;

        public FileTaskAsync(File file, Uri uri, Context context) {
            this.context = context;
            this.file = file;
            this.uri = uri;
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
            fileClientService.writeFile(uri, file);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (file == null) {
                return;
            }
            Uri uri = Uri.parse(file.getAbsolutePath());
            addUri(uri);
            imagesAdapter.notifyDataSetChanged();
        }

    }
}
