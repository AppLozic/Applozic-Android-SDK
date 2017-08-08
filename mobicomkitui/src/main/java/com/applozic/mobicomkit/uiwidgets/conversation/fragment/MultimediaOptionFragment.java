package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.uiwidgets.R;

import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobiComAttachmentSelectorActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.MobicomLocationActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: shai
 * Date: 6/15/14
 * Time: 12:02 PM
 */
public class MultimediaOptionFragment extends DialogFragment {
    public static final int RESULT_OK = -1;
    public static final int REQUEST_CODE_SEND_LOCATION = 10;
    public static final int REQUEST_CODE_TAKE_PHOTO = 11;
    public static final int REQUEST_CODE_ATTACH_PHOTO = 12;
    public static final int REQUEST_MULTI_ATTCAHMENT = 16;
    public static final int REQUEST_CODE_ATTACHE_AUDIO = 13;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY = 14;
    public static final int REQUEST_CODE_CONTACT_SHARE = 15;
    private Uri capturedImageUri;
    private int menuOptionsResourceId = R.array.multimediaOptions_sms;

    public MultimediaOptionFragment() {

    }

    public Uri getCapturedImageUri() {
        return capturedImageUri;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        capturedImageUri = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(menuOptionsResourceId, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        ((ConversationActivity) getActivity()).processLocation();
                        break;
                    case 1:
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        // Ensure that there's a camera activity to handle the intent
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile;

                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                            String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";

                            photoFile = FileClientService.getFilePath(imageFileName, getActivity(), "image/jpeg");

                            // Continue only if the File was successfully created
                            if (photoFile != null) {
                                capturedImageUri = Uri.fromFile(photoFile);
                                ConversationActivity.setCapturedImageUri(capturedImageUri);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                                getActivity().startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
                            }
                        }
                        break;
                    case 2:

                        Intent intentPick = new Intent(getActivity(), MobiComAttachmentSelectorActivity.class);
                        getActivity().startActivityForResult(intentPick, REQUEST_MULTI_ATTCAHMENT);
                        break;
                    case 3:
                        ((ConversationActivity) getActivity()).showAudioRecordingDialog();
                        break;
                    case 4:

                        // create new Intentwith with Standard Intent action that can be
                        // sent to have the camera application capture an video and return it.
                        intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "VID_" + timeStamp + "_" + ".mp4";

                        File fileUri = FileClientService.getFilePath(imageFileName, getActivity(), "video/mp4");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileUri));
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                        ((ConversationActivity) (getActivity())).setVideoFileUri(Uri.fromFile(fileUri));
                        getActivity().startActivityForResult(intent, REQUEST_CODE_CAPTURE_VIDEO_ACTIVITY);
                        break;

                    case 5:
                        //Sharing contact.
                        intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                        getActivity().startActivityForResult(intent, REQUEST_CODE_CONTACT_SHARE);
                        break;

                    case 6:
                        new ConversationUIService(getActivity()).sendPriceMessage();
                        break;
                    default:
                }
            }
        });
        return builder.create();
    }

    public void show(FragmentManager supportFragmentManager, int resourceId) {
        this.menuOptionsResourceId = resourceId;
        show(supportFragmentManager, "Attachment options");
    }
}
