package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.RemoveInterfaceListener;
import com.applozic.mobicomkit.uiwidgets.people.fragment.ProfileFragment;
import com.applozic.mobicomkit.uiwidgets.uilistener.MobicomkitUriListener;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.util.List;

/**
 * Created by sunil on 25/5/16.
 */
public class PictureUploadPopUpFragment extends DialogFragment {

    public static final String REMOVE_PHOTO = "REMOVE_PHOTO";
    public static final String REMOVE_OPTION = "REMOVE_OPTION";
    private static final String TAG = "PictureUploadPopUpFrag";
    LinearLayout removeLayout, galleryLayout, cameraLayout;
    boolean removePhoto;
    boolean disableRemoveOption;
    Bundle bundle;

    public static PictureUploadPopUpFragment newInstance(boolean removePhoto, boolean disableRemoveOption) {
        PictureUploadPopUpFragment f = new PictureUploadPopUpFragment();
        Bundle args = new Bundle();
        args.putBoolean(REMOVE_PHOTO, removePhoto);
        args.putBoolean(REMOVE_OPTION, disableRemoveOption);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.attach_photo_popup_window_layout, container, false);
        bundle = getArguments();
        getDialog().setCancelable(Boolean.TRUE);
        cameraLayout = (LinearLayout) view.findViewById(R.id.upload_camera_layout);
        removeLayout = (LinearLayout) view.findViewById(R.id.upload_remove_image_layout);
        removeLayout.setVisibility(View.GONE);

        if (bundle != null) {
            removePhoto = bundle.getBoolean(REMOVE_PHOTO);
            disableRemoveOption = bundle.getBoolean(REMOVE_OPTION);
        }
        if (disableRemoveOption) {
            removeLayout.setVisibility(View.GONE);
        }
        removeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (removePhoto) {
                    ((RemoveInterfaceListener) getActivity()).removeCallBack();
                } else {
                    getTargetFragment().onActivityResult(ProfileFragment.REQUEST_REMOVE_PHOTO, Activity.RESULT_OK, getActivity().getIntent());
                }
                getDialog().dismiss();
            }
        });
        galleryLayout = (LinearLayout) view.findViewById(R.id.upload_gallery_layout);
        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                Intent getContentIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                getActivity().startActivityForResult(getContentIntent, ProfileFragment.REQUEST_CODE_ATTACH_PHOTO);
            }
        });

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                imageCapture();
            }
        });

        return view;

    }

    public void imageCapture() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (!(getActivity() instanceof MobicomkitUriListener)) {
            Utils.printLog(getContext(),TAG, "Activity must implement MobicomkitUriListener to get image file uri");
            return;
        }

        if (cameraIntent.resolveActivity(getContext().getPackageManager()) != null) {

            Uri capturedImageUri = ((MobicomkitUriListener) getActivity()).getCurrentImageUri();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(getActivity().getContentResolver(), "a Photo", capturedImageUri);

                cameraIntent.setClipData(clip);
                cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            } else {
                List<ResolveInfo> resInfoList =
                        getActivity().getPackageManager()
                                .queryIntentActivities(cameraIntent, PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getActivity().grantUriPermission(packageName, capturedImageUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getActivity().grantUriPermission(packageName, capturedImageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
            getActivity().startActivityForResult(cameraIntent, ProfileFragment.REQUEST_CODE_TAKE_PHOTO);
        }
    }
}
