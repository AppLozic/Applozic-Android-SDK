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
import android.util.Log;

import com.applozic.mobicomkit.api.conversation.Message;

import com.applozic.mobicommons.commons.image.PhotoDecodeRunnable;
import com.applozic.mobicommons.commons.image.PhotoDecodeRunnable.TaskRunnableDecodeMethods;
import com.applozic.mobicommons.file.FileUtils;

import java.lang.ref.WeakReference;

/**
 * This class manages PhotoDownloadRunnable and PhotoDownloadRunnable objects.  It does't perform
 * the download or decode; instead, it manages persistent storage for the tasks that do the work.
 * It does this by implementing the interfaces that the download and decode classes define, and
 * then passing itself as an argument to the constructor of a download or decode object. In effect,
 * this allows PhotoTask to start on a Thread, run a download in a delegate object, then
 * run a decode, and then start over again. This class can be pooled and reused as necessary.
 */
public class AttachmentTask implements
        AttachmentDownloader.TaskRunnableDownloadMethods, TaskRunnableDecodeMethods {

    /*
     * An object that contains the ThreadPool singleton.
     */
    private static AttachmentManager sPhotoManager;
    /*
     * Field containing the Thread this task is running on.
     */
    Thread mThreadThis;
    // A buffer for containing the bytes that make up the image
    byte[] mImageBuffer;
    /*
     * Creates a weak reference to the ImageView that this Task will populate.
     * The weak reference prevents memory leaks and crashes, because it
     * automatically tracks the "state" of the variable it backs. If the
     * reference becomes invalid, the weak reference is garbage- collected. This
     * technique is important for referring to objects that are part of a
     * component lifecycle. Using a hard reference may cause memory leaks as the
     * value continues to change; even worse, it can cause crashes if the
     * underlying component is destroyed. Using a weak reference to a View
     * ensures that the reference is more transitory in nature.
     */
    private WeakReference<AttachmentView> mImageWeakRef;
    private WeakReference<AttachmentViewProperties> mImageWeakRefNew;
    // The image's URL
    private String mImageURL;
    // The width and height of the decoded image
    private int mTargetHeight;
    private int mTargetWidth;
    // Is the cache enabled for this transaction?
    private boolean mCacheEnabled;
    private Context context;
    /*
     * Fields containing references to the two runnable objects that handle downloading and
     * decoding of the image.
     */
    private Runnable mDownloadRunnable;
    private Runnable mDecodeRunnable;
    // The decoded image
    private Bitmap mDecodedImage;
    // The Thread on which this task is currently running.
    private Thread mCurrentThread;
    private Message message;

    /**
     * Creates an PhotoTask containing a download object and a decoder object.
     */
    AttachmentTask() {
        // Create the runnables
        mDownloadRunnable = new AttachmentDownloader(this);
        mDecodeRunnable = new PhotoDecodeRunnable(this);
        sPhotoManager = AttachmentManager.getInstance();
    }

    /**
     * Initializes the Task
     *
     * @param photoManager A ThreadPool object
     * @param photoView    An ImageView instance that shows the downloaded image
     * @param cacheFlag    Whether caching is enabled
     */
    void initializeDownloaderTask(
            AttachmentManager photoManager,
            AttachmentView photoView,
            boolean cacheFlag) {
        // Sets this object's ThreadPool field to be the input argument
        sPhotoManager = photoManager;

        // Gets the URL for the View
        mImageURL = photoView.getImageUrl();
        message = photoView.getMessage();
        // Instantiates the weak reference to the incoming view
        setAttachementView(photoView);

        // Sets the cache flag to the input argument
        mCacheEnabled = cacheFlag;

        // Gets the width and height of the provided ImageView
        mTargetWidth = photoView.getWidth();
        mTargetHeight = photoView.getHeight();
        context = photoView.getContext().getApplicationContext();

    }

    void initializeDownloaderTask(
            AttachmentManager photoManager,
            AttachmentViewProperties photoView,
            boolean cacheFlag) {
        // Sets this object's ThreadPool field to be the input argument
        sPhotoManager = photoManager;

        // Gets the URL for the View
        mImageURL = photoView.getImageUrl();
        message = photoView.getMessage();
        // Instantiates the weak reference to the incoming view
        setAttachementViewNew(photoView);

        // Sets the cache flag to the input argument
        mCacheEnabled = cacheFlag;

        // Gets the width and height of the provided ImageView
        mTargetWidth = photoView.getWidth();
        mTargetHeight = photoView.getHeight();
        context = photoView.getContext();
    }

    public void setAttachementView(AttachmentView photoView) {
        mImageWeakRef = new WeakReference<AttachmentView>(photoView);
        this.message = photoView.getMessage();
        this.context = photoView.getContext();
    }

    public void setAttachementViewNew(AttachmentViewProperties photoView) {
        mImageWeakRefNew = new WeakReference<AttachmentViewProperties>(photoView);
        this.message = photoView.getMessage();
        this.context = photoView.getContext();
    }


    /**
     * Recycles an PhotoTask object before it's put back into the pool. One reason to do
     * this is to avoid memory leaks.
     */
    void recycle() {

        // Deletes the weak reference to the imageView
        if (null != mImageWeakRef) {
            mImageWeakRef.clear();
            mImageWeakRef = null;
        }

        if (null != mImageWeakRefNew) {
            mImageWeakRefNew.clear();
            mImageWeakRefNew = null;
        }
        // Releases references to the byte buffer and the BitMap
        mImageBuffer = null;
        mDecodedImage = null;
    }

    // Implements PhotoDownloadRunnable.getTargetWidth. Returns the global target width.
    @Override
    public int getTargetWidth() {
        return mTargetWidth;
    }

    // Implements PhotoDownloadRunnable.getTargetHeight. Returns the global target height.
    @Override
    public int getTargetHeight() {
        return mTargetHeight;
    }

    // Detects the state of caching
    boolean isCacheEnabled() {
        return mCacheEnabled;
    }


    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public Context getContext() {
        return context.getApplicationContext();
    }

    @Override
    public String getContentType() {
        //This is just an extra check yo avoid NPE ..but it should not come.
        if (message == null) {
            return null;
        } else if (message.isAttachmentUploadInProgress() || message.getFileMetas() == null) {
            //This block is written to handle the case when image upload is in progreass
            String filePath = message.getFilePaths().get(0);
            final String mimeType = FileUtils.getMimeType(filePath);
            return mimeType;

        } else if (message.getFileMetas() != null) {
            return message.getFileMetas().getContentType();
        }
        return null;
    }


    // Delegates handling the current state of the task to the PhotoManager object
    void handleState(int state) {
        sPhotoManager.handleState(this, state);
    }

    // Returns the image that PhotoDecodeRunnable decoded.
    Bitmap getImage() {
        return mDecodedImage;
    }

    // Implements ImageCoderRunnable.setImage(). Sets the Bitmap for the current image.
    @Override
    public void setImage(Bitmap decodedImage) {
        mDecodedImage = decodedImage;
    }

    // Returns the instance that downloaded the image
    Runnable getHTTPDownloadRunnable() {
        return mDownloadRunnable;
    }

    // Returns the instance that decode the image
    Runnable getPhotoDecodeRunnable() {
        return mDecodeRunnable;
    }

    // Returns the ImageView that's being constructed.
    public AttachmentView getPhotoView() {
        if (null != mImageWeakRef) {
            return mImageWeakRef.get();
        }
        return null;
    }

    public AttachmentViewProperties getAttachmentView() {
        if (null != mImageWeakRefNew) {
            return mImageWeakRefNew.get();
        }
        return null;
    }

    /*
     * Returns the Thread that this Task is running on. The method must first get a lock on a
     * static field, in this case the ThreadPool singleton. The lock is needed because the
     * Thread object reference is stored in the Thread object itself, and that object can be
     * changed by processes outside of this app.
     */
    public Thread getCurrentThread() {
        synchronized (sPhotoManager) {
            return mCurrentThread;
        }
    }

    /*
     * Sets the identifier for the current Thread. This must be a synchronized operation; see the
     * notes for getCurrentThread()
     */
    public void setCurrentThread(Thread thread) {
        synchronized (sPhotoManager) {
            mCurrentThread = thread;
        }
    }

    @Override
    public String getLocalPath() {
        if (getPhotoView() == null) {
            Log.e("AttachmentTask", "@@@photoView ia coming as null.." + getMessage().getKeyString());
            return null;
        }
        return getPhotoView().getLocalPath();
    }

    // Implements PhotoDownloadRunnable.setHTTPDownloadThread(). Calls setCurrentThread().
    @Override
    public void setDownloadThread(Thread currentThread) {
        setCurrentThread(currentThread);
    }

    /*
     * Implements PhotoDownloadRunnable.handleHTTPState(). Passes the download state to the
     * ThreadPool object.
     */

    @Override
    public void handleDownloadState(int state) {
        int outState;

        // Converts the download state to the overall state
        switch (state) {
            case AttachmentDownloader.HTTP_STATE_COMPLETED:
                outState = AttachmentManager.DOWNLOAD_COMPLETE;
                sPhotoManager.attachmentInProgress.remove(getMessage().getKeyString());
                sPhotoManager.attachmentTaskList.remove(this);
                break;
            case AttachmentDownloader.HTTP_STATE_FAILED:
                outState = AttachmentManager.DOWNLOAD_FAILED;
                sPhotoManager.attachmentInProgress.remove(getMessage().getKeyString());
                sPhotoManager.attachmentTaskList.remove(this);
                break;
            default:
                outState = AttachmentManager.DOWNLOAD_STARTED;
                break;
        }
        // Passes the state to the ThreadPool object.
        handleState(outState);
    }

    @Override
    public String getImageURL() {
        return getPhotoView().getImageUrl();
    }


    // Implements PhotoDecodeRunnable.setImageDecodeThread(). Calls setCurrentThread().
    @Override
    public void setImageDecodeThread(Thread currentThread) {
        setCurrentThread(currentThread);
    }


    /*
     * Implements PhotoDecodeRunnable.handleDecodeState(). Passes the decoding state to the
     * ThreadPool object.
     */
    @Override
    public void handleDecodeState(int state) {
        int outState;

        // Converts the decode state to the overall state.
        switch (state) {
            case PhotoDecodeRunnable.DECODE_STATE_COMPLETED:
                outState = AttachmentManager.TASK_COMPLETE;
                break;
            case PhotoDecodeRunnable.DECODE_STATE_FAILED:
                outState = AttachmentManager.DECODE_FAILED;
                break;
            default:
                outState = AttachmentManager.DECODE_STARTED;
                break;
        }

        // Passes the state to the ThreadPool object.
        handleState(outState);
    }

    public Message getMessageDetail() {
        return null;
    }
}
