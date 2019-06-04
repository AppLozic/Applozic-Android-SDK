package com.applozic.mobicomkit.uiwidgets.conversation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALStoragePermission;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALStoragePermissionListener;

import java.util.ArrayList;
import java.util.List;

import static com.applozic.mobicomkit.uiwidgets.conversation.fragment.MultimediaOptionFragment.RESULT_OK;

/**
 * Created by reytum on 19/3/16.
 */
public class MultimediaOptionsGridView  extends Activity {
    public PopupWindow showPopup;
    public FragmentActivity context;
    GridView multimediaOptions;
    private Uri capturedImageUri;
    private ALStoragePermissionListener storagePermissionListener;

    public MultimediaOptionsGridView(FragmentActivity context, GridView multimediaOptions) {
        this.context = context;
        this.multimediaOptions = multimediaOptions;
        if (context instanceof ALStoragePermissionListener) {
            storagePermissionListener = (ALStoragePermissionListener) context;
        } else {
            storagePermissionListener = new ALStoragePermissionListener() {
                @Override
                public boolean isPermissionGranted() {
                    return false;
                }

                @Override
                public void checkPermission(ALStoragePermission storagePermission) {

                }
            };
        }
    }

    public void setMultimediaClickListener(final List<String> keys) {
        capturedImageUri = null;

        multimediaOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                executeMethod(keys.get(position));
            }
        });
    }

    public void executeMethod(String key) {
        if (key.equals(context.getResources().getString(R.string.al_location))) {
            ((ConversationActivity) context).processLocation();
        } else if (key.equals(context.getString(R.string.al_camera))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).isTakePhoto(true);
                ((ConversationActivity) context).processCameraAction();
            } else {
                storagePermissionListener.checkPermission(new ALStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).isTakePhoto(true);
                            ((ConversationActivity) context).processCameraAction();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_file))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).isAttachment(true);
                ((ConversationActivity) context).processAttachment();
            } else {
                storagePermissionListener.checkPermission(new ALStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).isAttachment(true);
                            ((ConversationActivity) context).processAttachment();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_audio))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).showAudioRecordingDialog();
            } else {
                storagePermissionListener.checkPermission(new ALStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).showAudioRecordingDialog();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_video))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).isTakePhoto(false);
                ((ConversationActivity) context).processVideoRecording();
            } else {
                storagePermissionListener.checkPermission(new ALStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).isTakePhoto(false);
                            ((ConversationActivity) context).processVideoRecording();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_contact))) {
            if (storagePermissionListener.isPermissionGranted()) {
                ((ConversationActivity) context).processContact();
            } else {
                storagePermissionListener.checkPermission(new ALStoragePermission() {
                    @Override
                    public void onAction(boolean didGrant) {
                        if (didGrant) {
                            ((ConversationActivity) context).processContact();
                        }
                    }
                });
            }
        } else if (key.equals(context.getString(R.string.al_price))) {
            new ConversationUIService(context).sendPriceMessage();
        } else if (key.equals(context.getString(R.string.al_speak))){
            ((ConversationActivity) context).SpeechToWord();

        }
        multimediaOptions.setVisibility(View.GONE);
    }



}