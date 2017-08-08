package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.file.FileUtils;

import java.util.ArrayList;


public class MobiComAttachmentGridViewAdapter extends BaseAdapter {

    public static final int REQUEST_CODE = 100;
    ImageButton deleteButton;
    ImageView galleryImageView;
    TextView fileSize;
    ImageView attachmentImageView;
    TextView fileName;
    AlCustomizationSettings alCustomizationSettings;
    boolean disableNewAttachment;
    private Context context;
    private ArrayList<Uri> uris;


    public MobiComAttachmentGridViewAdapter(Context context, ArrayList<Uri> uris, AlCustomizationSettings alCustomizationSettings, boolean disableNewAttachment) {
        this.context = context;
        this.alCustomizationSettings = alCustomizationSettings;
        this.uris = uris;
        this.disableNewAttachment = disableNewAttachment;
    }

    @Override
    public int getCount() {
        //Extra one item is added
        return uris.size() + (disableNewAttachment ? 0 : 1);
    }


    @Override
    public Object getItem(int i) {
        return uris.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.mobicom_attachment_gridview_item, viewGroup, false);//Inflate layout
        }
        deleteButton = (ImageButton) view.findViewById(R.id.mobicom_attachment_delete_btn);
        galleryImageView = (ImageView) view.findViewById(R.id.galleryImageView);
        fileSize = (TextView) view.findViewById(R.id.mobicom_attachment_file_size);
        attachmentImageView = (ImageView) view.findViewById(R.id.mobicom_attachment_image);
        fileName = (TextView) view.findViewById(R.id.mobicom_attachment_file_name);

        galleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (position < getCount() - 1) {
                    return;
                }

                if (getCount() > alCustomizationSettings.getMaxAttachmentAllowed()) {
                    Toast.makeText(context, R.string.mobicom_max_attachment_warning, Toast.LENGTH_LONG).show();
                    return;
                }

                Intent getContentIntent = FileUtils.createGetContentIntent();
                getContentIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                Intent intentPick = Intent.createChooser(getContentIntent, context.getString(R.string.select_file));
                ((Activity) context).startActivityForResult(intentPick, REQUEST_CODE);
            }
        });

        if (disableNewAttachment) {
            deleteButton.setVisibility(View.GONE);
        }
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uris.remove(position);
                notifyDataSetChanged();
            }
        });

        if (position == getCount() - 1) {
            if (!disableNewAttachment) {
                setNewAttachmentView();
                return view;
            }
        } else {
            if (!disableNewAttachment) {
                deleteButton.setVisibility(View.VISIBLE);
            }
        }

        try {
            Uri uri = (Uri) getItem(position);
            Bitmap previewBitmap = ImageUtils.getPreview(context, uri);
            if (previewBitmap != null) {
                setGalleryView(previewBitmap);
            } else {
                setAttachmentView(uri);
            }
            fileSize.setText(FileUtils.getSize(context, uri));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }


    private void setAttachmentView(Uri uri) {
        attachmentImageView.setVisibility(View.VISIBLE);
        fileName.setVisibility(View.VISIBLE);
        fileName.setText(FileUtils.getFileName(uri));
        galleryImageView.setImageBitmap(null);
    }

    private void setGalleryView(Bitmap previewBitmap) {
        galleryImageView.setImageBitmap(previewBitmap);
        fileName.setVisibility(View.GONE);
        attachmentImageView.setVisibility(View.GONE);
    }

    private void setNewAttachmentView() {
        deleteButton.setVisibility(View.GONE);
        galleryImageView.setImageResource(R.drawable.applozic_ic_action_add);
        fileName.setVisibility(View.GONE);
        attachmentImageView.setVisibility(View.GONE);
        fileSize.setText(R.string.New_Attachment);
    }

}