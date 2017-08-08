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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.applozic.mobicomkit.broadcast.BroadcastService;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 * This class creates pools of background threads for downloading
 * Picasa images from the web, based on URLs retrieved from Picasa's featured images RSS feed.
 * The class is implemented as a singleton; the only way to get an PhotoManager instance is to
 * call {@link #getInstance}.
 * </p>
 * <p>
 * The class sets the pool size and cache size based on the particular operation it's performing.
 * The algorithm doesn't apply to all situations, so if you re-use the code to implement a pool
 * of threads for your own app, you will have to come up with your choices for pool size, cache
 * size, and so forth. In many cases, you'll have to set some numbers arbitrarily and then
 * measure the impact on performance.
 * </p>
 * <p>
 * This class actually uses two threadpools in order to limit the number of
 * simultaneous image decoding threads to the number of available processor
 * cores.
 * </p>
 * Finally, this class defines a handler that communicates back to the UI
 * thread to change the bitmap to reflect the state.
 */
@SuppressWarnings("unused")
public class AttachmentManager {

    /*
     * Status indicators
     */
    static final int DOWNLOAD_FAILED = -1;
    static final int DECODE_FAILED = -2;
    static final int DOWNLOAD_STARTED = 1;
    static final int DOWNLOAD_COMPLETE = 2;
    static final int DECODE_STARTED = 3;
    static final int TASK_COMPLETE = 4;
    private static final String TAG = "AttachmentManager";
    // Sets the size of the storage that's used to cache images
    // Sets the amount of time an idle thread will wait for a task before terminating
    private static final int KEEP_ALIVE_TIME = 1;

    // Sets the size of the storage that's used to cache images
    private static final int IMAGE_CACHE_SIZE = 1024 * 1024 * 4;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    // Sets the initial threadpool size to 8
    private static final int CORE_POOL_SIZE = 8;

    // Sets the maximum threadpool size to 8
    private static final int MAXIMUM_POOL_SIZE = 8;

    /**
     * NOTE: This is the number of total available cores. On current versions of
     * Android, with devices that use plug-and-play cores, this will return less
     * than the total number of cores. The total number of cores is not
     * available in current Android implementations.
     */
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    // A single instance of PhotoManager, used to implement the singleton pattern
    private static AttachmentManager sInstance = null;

    // A static block that sets class fields
    static {

        // The time unit for "keep alive" is in seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        // Creates a single static instance of PhotoManager
        sInstance = new AttachmentManager();
    }

    public final List<String> attachmentInProgress;
    public final List<AttachmentTask> attachmentTaskList;
    // A queue of Runnables for the image download pool
    private final BlockingQueue<Runnable> mDownloadWorkQueue;
    // A queue of Runnables for the image decoding pool
    private final BlockingQueue<Runnable> mDecodeWorkQueue;
    // A queue of PhotoManager tasks. Tasks are handed to a ThreadPool.
    private final Queue<AttachmentTask> mPhotoTaskWorkQueue;
    // A managed pool of background download threads
    private final ThreadPoolExecutor mDownloadThreadPool;
    //taking reference for future use ::
    // A managed pool of background decoder threads
    private final ThreadPoolExecutor mDecodeThreadPool;
    /*
     * Creates a cache of byte arrays indexed by image URLs. As new items are added to the
     * cache, the oldest items are ejected and subject to garbage collection.
     */
    private LruCache<String, Bitmap> mPhotoCache = null;
    // An object that manages Messages in a Thread
    private Handler mHandler;

    /**
     * Constructs the work queues and thread pools used to download and decode images.
     */
    private AttachmentManager() {

        attachmentInProgress = new ArrayList<String>();
        attachmentTaskList = new ArrayList<AttachmentTask>();
        /*
         * Creates a work queue for the pool of Thread objects used for downloading, using a linked
         * list queue that blocks when the queue is empty.
         */
        mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();

        /*
         * Creates a work queue for the pool of Thread objects used for decoding, using a linked
         * list queue that blocks when the queue is empty.
         */
        mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();

        /*
         * Creates a work queue for the set of of task objects that control downloading and
         * decoding, using a linked list queue that blocks when the queue is empty.
         */
        mPhotoTaskWorkQueue = new LinkedBlockingQueue<AttachmentTask>();

        /*
         * Creates a new pool of Thread objects for the download work queue
         */
        mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);

        /*
         * Creates a new pool of Thread objects for the decoding work queue
         */
        mDecodeThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDecodeWorkQueue);

        // Instantiates a new cache based on the cache size estimate
        mPhotoCache = new LruCache<String, Bitmap>(IMAGE_CACHE_SIZE) {

//            /*
//             * This overrides the default sizeOf() implementation to return the
//             * correct size of each cache entry.
//             */
//
//            @Override
//            protected int sizeOf(String paramURL, Bitmap image) {
//                return image.getByteCount();
//            }
        };
        /*
         * Instantiates a new anonymous Handler object and defines its
         * handleMessage() method. The Handler *must* run on the UI thread, because it moves photo
         * Bitmaps from the PhotoTask object to the View object.
         * To force the Handler to run on the UI thread, it's defined as part of the PhotoManager
         * constructor. The constructor is invoked when the class is first referenced, and that
         * happens when the View invokes startDownload. Since the View runs on the UI Thread, so
         * does the constructor and the Handler.
         */
        mHandler = new Handler(Looper.getMainLooper()) {

            /*
             * handleMessage() defines the operations to perform when the
             * Handler receives a new Message to process.
             */
            @Override
            public void handleMessage(Message inputMessage) {

                // Gets the image task from the incoming Message object.
                AttachmentTask attachmentTask = (AttachmentTask) inputMessage.obj;

                // Sets an PhotoView that's a weak reference to the
                // input ImageView
                AttachmentView localView = attachmentTask.getPhotoView();

                // If this input view isn't null
                if (localView != null) {

                    /*
                     * Gets the URL of the *weak reference* to the input
                     * ImageView. The weak reference won't have changed, even if
                     * the input ImageView has.
                     */
                    // URL localURL = localView.getLocation();

                    /*
                     * Compares the URL of the input ImageView to the URL of the
                     * weak reference. Only updates the bitmap in the ImageView
                     * if this particular Thread is supposed to be serving the
                     * ImageView.
                     */
                    //if (attachmentTask.getImageURL() == localURL)

                        /*
                         * Chooses the action to take, based on the incoming message
                         *
                         */
                    //TODO: show the status properly based on message status ...
                    switch (inputMessage.what) {

                        case DOWNLOAD_STARTED:
                            localView.getMessage().setAttDownloadInProgress(true);
                            if (localView.getProressBar() != null) {
                                localView.getProressBar().setVisibility(View.VISIBLE);
                            }
                            break;
                        case DOWNLOAD_COMPLETE:
                            if (localView.getProressBar() != null) {
                                localView.getProressBar().setProgress(70);
                            }
                            localView.getMessage().setAttDownloadInProgress(false);
                            break;
                        case DECODE_STARTED:
                            if (localView.getProressBar() != null) {
                                localView.getProressBar().setVisibility(View.VISIBLE);
                                localView.getProressBar().setProgress(90);
                            }
                            break;
                            /*
                             * The decoding is done, so this sets the
                             * ImageView's bitmap to the bitmap in the
                             * incoming message
                             */
                        case TASK_COMPLETE:

                            if (localView.getDownloadProgressLayout() != null && !localView.getMessage().isAttachmentUploadInProgress()) {
                                localView.getDownloadProgressLayout().setVisibility(View.GONE);
                            } else if (localView.getProressBar() != null) {
                                localView.getProressBar().setVisibility(View.GONE);
                            }
                            BroadcastService.sendMessageUpdateBroadcast(localView.getContext(), BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString(), localView.getMessage());
                            localView.setImageBitmap(attachmentTask.getImage());
                            recycleTask(attachmentTask);
                            break;
                        // The download failed, sets the background color to dark red
                        case DOWNLOAD_FAILED:
                            //localView.setStatusResource(R.drawable.imagedownloadfailed);
                            localView.getProressBar().setProgress(0);
                            localView.getMessage().setAttDownloadInProgress(false);
                            localView.getDownloadProgressLayout().setVisibility(View.GONE);
                            localView.setVisibility(View.INVISIBLE);
                            localView.cancelDownload();
                            BroadcastService.sendMessageUpdateBroadcast(localView.getContext(), BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString(), localView.getMessage());
                            Toast.makeText(localView.getContext(), "Download failed.", Toast.LENGTH_SHORT).show();
                            // Attempts to re-use the Task object
                            recycleTask(attachmentTask);
                            break;
                        default:
                            // Otherwise, calls the super method
                            super.handleMessage(inputMessage);
                    }
                } else if (attachmentTask.getAttachmentView() != null) {
                    AttachmentViewProperties attachmentView = attachmentTask.getAttachmentView();
                    switch (inputMessage.what) {

                        case DOWNLOAD_STARTED:
                            break;
                        case DOWNLOAD_COMPLETE:
                            break;
                        case DECODE_STARTED:
                            break;
                        case TASK_COMPLETE:
                            BroadcastService.sendMessageUpdateBroadcast(attachmentView.getContext(), BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_DONE.toString(), attachmentView.getMessage());
                            recycleTask(attachmentTask);
                            break;
                        // The download failed, sets the background color to dark red
                        case DOWNLOAD_FAILED:
                            //localView.setStatusResource(R.drawable.imagedownloadfailed);
                            attachmentView.getMessage().setAttDownloadInProgress(false);
                            BroadcastService.sendMessageUpdateBroadcast(attachmentView.getContext(), BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString(), attachmentView.getMessage());
                            Toast.makeText(attachmentView.getContext(), "Download failed.", Toast.LENGTH_SHORT).show();
                            // Attempts to re-use the Task object
                            recycleTask(attachmentTask);
                            break;
                        default:
                            // Otherwise, calls the super method
                            super.handleMessage(inputMessage);
                    }
                }
            }
        };
    }

    /**
     * Returns the PhotoManager object
     *
     * @return The global PhotoManager object
     */
    public static AttachmentManager getInstance() {

        return sInstance;
    }

    public static boolean isAttachmentInProgress(String messageKeyString) {
        boolean result = false;
        if (sInstance != null) {
            result = sInstance.attachmentInProgress.contains(messageKeyString);
        }
        return result;
    }

    /**
     * Cancels all Threads in the ThreadPool
     */
    public static void cancelAll() {

        /*
         * Creates an array of tasks that's the same size as the task work queue
         */
        AttachmentTask[] taskArray = new AttachmentTask[sInstance.mDownloadWorkQueue.size()];

        // Populates the array with the task objects in the queue
        sInstance.mDownloadWorkQueue.toArray(taskArray);

        // Stores the array length in order to iterate over the array
        int taskArraylen = taskArray.length;

        /*
         * Locks on the singleton to ensure that other processes aren't mutating Threads, then
         * iterates over the array of tasks and interrupts the task's current Thread.
         */
        synchronized (sInstance) {

            // Iterates over the array of tasks
            for (AttachmentTask aTaskArray : taskArray) {

                // Gets the task's current thread
                Thread thread = aTaskArray.mThreadThis;

                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }

    /**
     * Stops a download Thread and removes it from the threadpool
     *
     * @param downloaderTask The download task associated with the Thread
     */
    static public void removeDownload(AttachmentTask downloaderTask, boolean documentView) {

        // If the Thread object still exists and the download matches the specified URL
        if (downloaderTask != null) {
            /*
             * Locks on this class to ensure that other processes aren't mutating Threads.
             */
            synchronized (sInstance) {

                // Gets the Thread that the downloader task is running on
                Thread thread = downloaderTask.getCurrentThread();
                // If the Thread exists, posts an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                } else {
                    Log.i(TAG, "Thread is coming null");
                    if (downloaderTask.getAttachmentView() == null && downloaderTask.getPhotoView() == null) {
                        return;
                    }
                    Context context = documentView ? downloaderTask.getAttachmentView().getContext() : downloaderTask.getPhotoView().getContext();
                    com.applozic.mobicomkit.api.conversation.Message message = documentView ? downloaderTask.getAttachmentView().getMessage() : downloaderTask.getPhotoView().getMessage();
                    BroadcastService.sendMessageUpdateBroadcast(context, BroadcastService.INTENT_ACTIONS.MESSAGE_ATTACHMENT_DOWNLOAD_FAILD.toString(), message);
                }

            }
            /*
             * Removes the download Runnable from the ThreadPool. This opens a Thread in the
             * ThreadPool's work queue, allowing a task in the queue to start.
             */
            sInstance.mDownloadThreadPool.remove(downloaderTask.getHTTPDownloadRunnable());
        }
    }

    /**
     * Starts an image download and decode
     *
     * @param imageView The ImageView that will get the resulting Bitmap
     * @param cacheFlag Determines if caching should be used
     * @return The task instance that will handle the work
     */
    static public AttachmentTask startDownload(
            AttachmentView imageView,
            boolean cacheFlag) {

        /*
         * Gets a task from the pool of tasks, returning null if the pool is empty
         */
        AttachmentTask downloadTask = sInstance.mPhotoTaskWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == downloadTask) {
            downloadTask = new AttachmentTask();
        }

        // Initializes the task
        downloadTask.initializeDownloaderTask(AttachmentManager.sInstance, imageView, cacheFlag);

        // If image is already downloaded ...just pass-message as download complete
        if (!downloadTask.getMessage().isAttachmentDownloaded()) {

            /*
             * "Executes" the tasks' download Runnable in order to download the image. If no
             * Threads are available in the thread pool, the Runnable waits in the queue.
             */
            sInstance.mDownloadThreadPool.execute(downloadTask.getHTTPDownloadRunnable());
            sInstance.attachmentInProgress.add(downloadTask.getMessage().getKeyString());
            sInstance.attachmentTaskList.add(downloadTask);
            // Sets the display to show that the image is queued for downloading and decoding.
            if (imageView.getProressBar() != null) {
                imageView.getProressBar().setVisibility(View.VISIBLE);
            }
            //imageView.setStatusResource(R.drawable.imagequeued);

            // The image was cached, so no download is required.
        } else {

            /*
             * Signals that the download is "complete", because the byte array already contains the
             * undecoded image. The decoding starts.
             */
            //imageView.getProressBar().setVisibility(View.VISIBLE);
            sInstance.handleState(downloadTask, DOWNLOAD_COMPLETE);
        }

        // Returns a task object, either newly-created or one from the task pool
        return downloadTask;
    }


    static public AttachmentTask startDownload(AttachmentViewProperties attachmentViewProperties,
                                               boolean cacheFlag) {

        /*
         * Gets a task from the pool of tasks, returning null if the pool is empty
         */
        AttachmentTask downloadTask = sInstance.mPhotoTaskWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == downloadTask) {
            downloadTask = new AttachmentTask();
        }

        // Initializes the task
        downloadTask.initializeDownloaderTask(AttachmentManager.sInstance, attachmentViewProperties, cacheFlag);

        // If image is already downloaded ...just pass-message as download complete
        if (!downloadTask.getMessage().isAttachmentDownloaded()) {

            /*
             * "Executes" the tasks' download Runnable in order to download the image. If no
             * Threads are available in the thread pool, the Runnable waits in the queue.
             */
            sInstance.mDownloadThreadPool.execute(downloadTask.getHTTPDownloadRunnable());
            sInstance.attachmentInProgress.add(downloadTask.getMessage().getKeyString());
            sInstance.attachmentTaskList.add(downloadTask);
            // Sets the display to show that the image is queued for downloading and decoding.
            //imageView.setStatusResource(R.drawable.imagequeued);

            // The image was cached, so no download is required.
        } else {

            /*
             * Signals that the download is "complete", because the byte array already contains the
             * undecoded image. The decoding starts.
             */
            //imageView.getProressBar().setVisibility(View.VISIBLE);
            sInstance.handleState(downloadTask, DOWNLOAD_COMPLETE);
        }

        // Returns a task object, either newly-created or one from the task pool
        return downloadTask;
    }

    public static AttachmentTask getBGThreadForAttachment(String messageKeyString) {
        Log.d(TAG, "Worker length... " + sInstance.attachmentTaskList.size());
        synchronized (sInstance) {
            for (AttachmentTask r : sInstance.attachmentTaskList) {
                // Gets the task's current thread
                if (r.getMessage() != null && messageKeyString.equals(r.getMessage().getKeyString())) {
                    Log.i(TAG, "Found the thread for: " + messageKeyString);
                    return r;
                }
            }
        }
        Log.i(TAG, "Not found the thread for: " + messageKeyString);
        return null;
    }

    /**
     * Handles state messages for a particular task object
     *
     * @param photoTask A task object
     * @param state     The state of the task
     */
    @SuppressLint("HandlerLeak")
    public void handleState(AttachmentTask photoTask, int state) {
        switch (state) {

            // The task finished downloading and decoding the image
            case TASK_COMPLETE:

                // Puts the image into cache
                if (photoTask.isCacheEnabled()) {
                    // If the task is set to cache the results, put the buffer
                    // that was
                    // successfully decoded into the cache
                    if (photoTask != null && photoTask.getImage() != null && !TextUtils.isEmpty(photoTask.getMessage().getKeyString())) {
                        mPhotoCache.put(photoTask.getMessage().getKeyString(), photoTask.getImage());
                    }
                }
                // Gets a Message object, stores the state in it, and sends it to the Handler
                Message completeMessage = mHandler.obtainMessage(state, photoTask);
                completeMessage.sendToTarget();
                break;

            // The task finished downloading the image
            case DOWNLOAD_COMPLETE:
                /*
                 * If it is a image than decodes the image, by queuing the decoder object to run in the decoder
                 *
                 */
                if (photoTask.getPhotoView() != null && photoTask.getContentType() != null && photoTask.getContentType().contains("image")) {
                    mDecodeThreadPool.execute(photoTask.getPhotoDecodeRunnable());
                } else {
                    //We need not to cache the Data here ..as we have nothing to load
                    // ...directly sending TASK complete message is enough
                    mHandler.obtainMessage(TASK_COMPLETE, photoTask).sendToTarget();
                }

                // In all other cases, pass along the message without any other action.
            default:
                mHandler.obtainMessage(state, photoTask).sendToTarget();
                break;
        }

    }

    /**
     * Recycles tasks by calling their internal recycle() method and then putting them back into
     * the task queue.
     *
     * @param downloadTask The task to recycle
     */
    void recycleTask(AttachmentTask downloadTask) {

        // Frees up memory in the task
        downloadTask.recycle();

        // Puts the task object back into the queue for re-use.
        mPhotoTaskWorkQueue.offer(downloadTask);
    }

    public Bitmap getBitMapFromCache(String key) {
        if (mPhotoCache != null && !TextUtils.isEmpty(key)) {
            return mPhotoCache.get(key);
        }
        return null;
    }

}
