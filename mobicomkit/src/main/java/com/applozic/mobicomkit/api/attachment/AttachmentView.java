/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.applozic.mobicomkit.api.attachment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.conversation.Message;

/**
 * This class extends the standard Android ImageView View class with some features
 * that are useful for downloading, decoding, and displaying Picasa images.
 */
public class AttachmentView extends ImageView {

    // Indicates if caching should be used
    private boolean mCacheFlag = true;

    // Status flag that indicates if onDraw has completed
    private boolean mIsDrawn;
    /*
     * Creates a weak reference to the ImageView in this object. The weak
     * reference prevents memory leaks and crashes, because it automatically tracks the "state" of
     * the variable it backs. If the reference becomes invalid, the weak reference is garbage-
     * collected.
     * This technique is important for referring to objects that are part of a component lifecycle.
     * Using a hard reference may cause memory leaks as the value continues to change; even worse,
     * it can cause crashes if the underlying component is destroyed. Using a weak reference to
     * a View ensures that the reference is more transitory in nature.
     */
    //private WeakReference<View> mThisView;

    // Contains the ID of the internal View
    private int mHideShowResId = -1;
    private ProgressBar proressBar;
    private RelativeLayout downloadProgressLayout;
    private Message message;
    // The Thread that will be used to download the file for this attachmentView
    // will be used to cancle it ,once the user will require it
    private AttachmentTask mDownloadThread;
    private Context context;

    /**
     * Creates an ImageDownloadView with no settings
     *
     * @param context A context for the View
     */
    public AttachmentView(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Creates an ImageDownloadView and gets attribute values
     *
     * @param context      A Context to use with the View
     * @param attributeSet The entire set of attributes for the View
     */
    public AttachmentView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Gets attributes associated with the attribute set
        //getAttributes(attributeSet);
    }

    /**
     * Creates an ImageDownloadView, gets attribute values, and applies a default style
     *
     * @param context      A context for the View
     * @param attributeSet The entire set of attributes for the View
     * @param defaultStyle The default style to use with the View
     */
    public AttachmentView(Context context, AttributeSet attributeSet, int defaultStyle) {
        super(context, attributeSet, defaultStyle);

    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    /*
     * This callback is invoked when the system attaches the ImageView to a Window. The callback
     * is invoked before onDraw(), but may be invoked after onMeasure()
     */
    @Override
    protected void onAttachedToWindow() {
        // Always call the supermethod first
        super.onAttachedToWindow();

        // If the sibling View is set and the parent of the ImageView is itself a View
        if ((this.mHideShowResId != -1) && ((getParent() instanceof View))) {

            // Gets a handle to the sibling View
            View localView = ((View) getParent()).findViewById(this.mHideShowResId);

            // If the sibling View contains something, make it the weak reference for this View
//            if (localView != null) {
//                this.mThisView = new WeakReference<View>(localView);
//            }
        }
    }

    /*
     * This callback is invoked when the ImageView is removed from a Window. It "unsets" variables
     * to prevent memory leaks.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /*
     * This callback is invoked when the system tells the View to draw itself. If the View isn't
     * already drawn, and its URL isn't null, it invokes a Thread to download the image. Otherwise,
     * it simply passes the existing Canvas to the super method
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (message != null && mCacheFlag) {
            Bitmap bitmap = (AttachmentManager.getInstance()).getBitMapFromCache(message.getKeyString());
            if (bitmap != null) {
                setImageBitmap(bitmap);
                return;
            }
        }
        // If the image isn't already drawn, and the URL is set
        if (!mIsDrawn && !AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
            // Starts downloading this View, using the current cache setting
            mDownloadThread = AttachmentManager.startDownload(this, mCacheFlag);
            // After successfully downloading the image, this marks that it's available.
            mIsDrawn = true;
        }
        if (mDownloadThread == null) {
            mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
            if (mDownloadThread != null)
                mDownloadThread.setAttachementView(this);
        }
    }

    @Override
    public void setImageBitmap(Bitmap paramBitmap) {
        super.setImageBitmap(paramBitmap);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
    }

    /*
     * Displays a drawable in the View
     */
    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
    }

    /*
     * Sets the URI for the Image
     */
    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
    }

    public String getImageUrl() {
        if (message == null || message.getFileMetas() == null) {
            return null;
        }
        return new MobiComKitClientService(getContext().getApplicationContext()).getFileUrl() + message.getFileMetas().getBlobKeyString();
    }

    public String getLocalPath() {
        return (message.getFilePaths() != null && !message.getFilePaths().isEmpty()) ? message.getFilePaths().get(0) : null;
    }

    public String contentType() {
        return message.getFileMetas().getContentType();
    }

    public ProgressBar getProressBar() {
        return proressBar;
    }

    public void setProressBar(ProgressBar proressBar) {
        this.proressBar = proressBar;
    }

    public RelativeLayout getDownloadProgressLayout() {
        return downloadProgressLayout;
    }

    public void setDownloadProgressLayout(RelativeLayout downloadProgressLayout) {
        this.downloadProgressLayout = downloadProgressLayout;
    }

    public void cancelDownload() {
        AttachmentManager.removeDownload(mDownloadThread, false);
        getDownloadProgressLayout().setVisibility(GONE);
        mIsDrawn = false;
    }

    public void setMCacheFlag(boolean cacheFlag) {
        this.mCacheFlag = cacheFlag;
    }

}
