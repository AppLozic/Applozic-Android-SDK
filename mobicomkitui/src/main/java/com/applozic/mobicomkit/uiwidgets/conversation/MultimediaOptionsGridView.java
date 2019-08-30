package com.applozic.mobicomkit.uiwidgets.conversation;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.MobiComQuickConversationFragment;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.sign_to_text;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.text_to_sign;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALStoragePermission;
import com.applozic.mobicomkit.uiwidgets.uilistener.ALStoragePermissionListener;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.List;

/**
 * Created by reytum on 19/3/16.
 */
public class MultimediaOptionsGridView {
    public PopupWindow showPopup;
    FragmentActivity context;
    GridView multimediaOptions;
    private Uri capturedImageUri;
    private ALStoragePermissionListener storagePermissionListener;
    protected Contact contact;


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

    public  void executeMethod(String key) {
        Log.i("TAG", "***conversation_send1***" + contact);


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
        } else if (key.equals(":SL2T")) {
            new ConversationUIService(context).processSL2T();
        } else if (key.equals(":T2SL")) {
            new ConversationUIService(context).processT2SL();
        }
        multimediaOptions.setVisibility(View.GONE);
    }
}