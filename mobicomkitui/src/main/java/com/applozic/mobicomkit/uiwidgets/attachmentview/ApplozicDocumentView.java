package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.MobiComKitConstants;
import com.applozic.mobicomkit.api.attachment.AttachmentManager;
import com.applozic.mobicomkit.api.attachment.AttachmentTask;
import com.applozic.mobicomkit.api.attachment.AttachmentViewProperties;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.database.MessageDatabaseService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;

import java.io.File;

/**
 * Created by devashish on 22/07/16.
 */
public class ApplozicDocumentView {

    private static final String TAG = "ApplozicDocumentView";
    RelativeLayout mainLayout;
    RelativeLayout downloadInProgressLayout;
    RelativeLayout downloadedLayout;
    RelativeLayout previewLayout;
    RelativeLayout retryLayout;
    TextView sizeTextView;
    TextView fileText;
    Message message;
    Context context;
    AttachmentViewProperties attachmentViewProperties;
    ProgressBar progressBar;
    ImageView uploadDownloadImage;
    SeekBar audioseekbar;
    Uri uri;
    String filePath, mimeType = null;
    ImageView docIcon;
    TextView audio_duration_textView;
    String audio_duration;
    private AttachmentTask mDownloadThread;
    private boolean mCacheFlag = false;
    private Handler mHandler = new Handler();


    public ApplozicDocumentView(Context context) {
        this.context = context;
    }

    public void inflateViewWithMessage(View rootview, Message message) {
        this.message = message;
        mainLayout = (RelativeLayout) rootview.findViewById(R.id.attachment_doc_relative_layout);
        downloadInProgressLayout = (RelativeLayout) rootview.findViewById(R.id.applozic_doc_download_progress_rl);
        downloadedLayout = (RelativeLayout) rootview.findViewById(R.id.applozic_doc_downloaded);
        previewLayout = (RelativeLayout) rootview.findViewById(R.id.download_doc_relative_layout);
        retryLayout = (RelativeLayout) rootview.findViewById(R.id.retry_doc_relative_layout);
        progressBar = (ProgressBar) rootview.findViewById(R.id.applozic_doc_download_progress);
        sizeTextView = (TextView) rootview.findViewById(R.id.applozic_doc_file_size);
        fileText = (TextView) rootview.findViewById(R.id.applozic_doc_file_name);
        uploadDownloadImage = (ImageView) rootview.findViewById(R.id.applozic_download_image);
        docIcon = (ImageView) rootview.findViewById(R.id.doc_icon);
        ImageView cancelIcon = (ImageView) rootview.findViewById(R.id.download_calcle_icon);
        audioseekbar = (SeekBar) rootview.findViewById(R.id.applozic_audio_seekbar);
        audio_duration_textView = (TextView) rootview.findViewById(R.id.audio_duration_textView);
        if (message.isTypeOutbox()) {
            progressBar.getIndeterminateDrawable().setColorFilter(R.color.applozic_green_color, android.graphics.PorterDuff.Mode.MULTIPLY);
            cancelIcon.setColorFilter(R.color.white, android.graphics.PorterDuff.Mode.MULTIPLY);


            if (message.getFileMetas() != null) {
                if (message.getFileMetas().getContentType().contains("audio")) {
                    setAudioIcons();
                    updateApplozicSeekBar();
                }
            } else if (message.getFilePaths() != null) {
                filePath = message.getFilePaths().get(0);
                mimeType = FileUtils.getMimeType(filePath);
                if (mimeType != null && mimeType.contains("audio")) {
                    setAudioIcons();
                    updateApplozicSeekBar();
                }
            }
            fileText.setTextColor(ContextCompat.getColor(context, R.color.message_text_color));
            audioseekbar.getProgressDrawable().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
            cancelIcon.setVisibility(View.GONE);
            docIcon.setColorFilter(0xffffffff);

        } else {
            progressBar.getIndeterminateDrawable().setColorFilter(R.color.black, android.graphics.PorterDuff.Mode.MULTIPLY);
            if (message.getFileMetas() != null) {
                if (message.getFileMetas().getContentType().contains("audio")) {
                    setAudioIcons();
                    updateApplozicSeekBar();
                }
            } else if (message.getFilePaths() != null) {
                filePath = message.getFilePaths().get(0);
                mimeType = FileUtils.getMimeType(filePath);
                if (mimeType != null && mimeType.contains("audio")) {
                    setAudioIcons();
                    updateApplozicSeekBar();
                }
            }
            cancelIcon.setVisibility(View.VISIBLE);
            cancelIcon.setColorFilter(R.color.black, android.graphics.PorterDuff.Mode.MULTIPLY);
            fileText.setTextColor(ContextCompat.getColor(context, R.color.message_text_color));
            audioseekbar.getProgressDrawable().setColorFilter(0xFFFFB242, PorterDuff.Mode.MULTIPLY);
        }

        if (!message.hasAttachment()) {
            return;
        }
        setupAttachmentView();
        registerEvents();
        if (message.isCanceled()) {
            showRetry();
        } else if (message.isAttachmentUploadInProgress() && !message.isCanceled()) {
            showUploadingProgress();
        } else if (AttachmentManager.isAttachmentInProgress(message.getKeyString())) {

            this.mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
            this.mDownloadThread.setAttachementViewNew(attachmentViewProperties);
            showDownloadInProgress();

        } else if (message.isAttachmentDownloaded()) {
            showDownloaded();
        } else {
            showPreview();
        }

        if (message.getFileMetas() != null && message.getFilePaths() == null) {
            if (!(message.getFileMetas().getContentType().contains("audio"))) {

                sizeTextView.setText(message.getFileMetas().getSizeInReadableFormat());
                fileText.setText(message.getFileMetas().getName());
                audioseekbar.setVisibility(View.GONE);
            } else {
                fileText.setVisibility(View.GONE);
                if (message.isAttachmentDownloaded()) {
                    audio_duration = ApplozicAudioManager.getInstance(context).refreshAudioDuration(filePath);
                    audio_duration_textView.setVisibility(View.VISIBLE);
                    audio_duration_textView.setText(audio_duration);
                } else {
                    audio_duration_textView.setVisibility(View.VISIBLE);
                    audio_duration_textView.setText("00:00");
                }
                setAudioIcons();
                audioseekbar.setVisibility(View.VISIBLE);
            }
        } else {
            if (message.getFilePaths() != null) {
                filePath = message.getFilePaths().get(0);
                mimeType = FileUtils.getMimeType(filePath);
                if (mimeType != null && !(mimeType.contains("audio"))) {
                    String fileName = new File(filePath).getName();
                    fileText.setText(fileName);
                    audioseekbar.setVisibility(View.GONE);
                } else {
                    if (message.isAttachmentDownloaded()) {
                        audio_duration = ApplozicAudioManager.getInstance(context).refreshAudioDuration(filePath);
                        audio_duration_textView.setVisibility(View.VISIBLE);
                        audio_duration_textView.setText(audio_duration);
                    } else {
                        audio_duration_textView.setVisibility(View.VISIBLE);
                        audio_duration_textView.setText("00:00");
                    }
                    fileText.setVisibility(View.GONE);
                    audioseekbar.setVisibility(View.VISIBLE);
                    setAudioIcons();
                }
            }
        }
    }

    private void showRetry() {
        if (isDownloadRequire()) {
            showPreview();
        } else {
            mainLayout.setVisibility(View.VISIBLE);
            retryLayout.setVisibility(View.VISIBLE);

            uploadDownloadImage.setImageResource(R.drawable.circle_arrow_upload);
            downloadInProgressLayout.setVisibility(View.GONE);
            downloadedLayout.setVisibility(View.GONE);
            previewLayout.setVisibility(View.GONE);
        }
    }

    private void showUploadingProgress() {
        Utils.printLog(context, TAG, "showUploadingProgress :: ");
        showDownloadInProgress();
    }

    private void setupAttachmentView() {
        attachmentViewProperties = new AttachmentViewProperties(mainLayout.getWidth(), mainLayout.getHeight(), context, message);
        if (mDownloadThread == null && AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
            mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
            if (mDownloadThread != null)
                mDownloadThread.setAttachementViewNew(attachmentViewProperties);
        }
    }

    public void hideView(boolean hideView) {
        mainLayout.setVisibility(hideView ? View.GONE : View.VISIBLE);
    }

    public void showPreview() {
        mainLayout.setVisibility(View.VISIBLE);
        previewLayout.setVisibility(View.VISIBLE);
        uploadDownloadImage.setImageResource(R.drawable.circle_arrow_down_download);
        downloadInProgressLayout.setVisibility(View.GONE);
        downloadedLayout.setVisibility(View.GONE);
        retryLayout.setVisibility(View.GONE);
    }

    public void showDownloadInProgress() {
        Utils.printLog(context, TAG, "showDownloadInProgress :: ");
        mainLayout.setVisibility(View.VISIBLE);
        downloadInProgressLayout.setVisibility(View.VISIBLE);
        previewLayout.setVisibility(View.GONE);
        downloadedLayout.setVisibility(View.GONE);
        retryLayout.setVisibility(View.GONE);
    }

    public void showDownloaded() {
        Utils.printLog(context, TAG, "showDownloaded :: ");
        mainLayout.setVisibility(View.VISIBLE);
        downloadedLayout.setVisibility(View.VISIBLE);
        previewLayout.setVisibility(View.GONE);
        downloadInProgressLayout.setVisibility(View.GONE);
        retryLayout.setVisibility(View.GONE);
    }

    public void registerEvents() {
        previewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!AttachmentManager.isAttachmentInProgress(message.getKeyString())) {
                    // Starts downloading this View, using the current cache setting
                    mDownloadThread = AttachmentManager.startDownload(attachmentViewProperties, mCacheFlag);
                    // After successfully downloading the image, this marks that it's available.
                    showDownloadInProgress();
                }
                if (mDownloadThread == null) {
                    mDownloadThread = AttachmentManager.getBGThreadForAttachment(message.getKeyString());
                    if (mDownloadThread != null)
                        mDownloadThread.setAttachementViewNew(attachmentViewProperties);
                }
            }
        });

        downloadedLayout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                final String mimeType = FileUtils.getMimeType(message.getFileMetas().getName());
                if (Utils.hasNougat()) {
                    uri = FileProvider.getUriForFile(context, Utils.getMetaDataValue(context, MobiComKitConstants.PACKAGE_NAME) + ".provider", new File(message.getFilePaths().get(0)));
                } else {
                    uri = Uri.fromFile(new File(message.getFilePaths().get(0)));
                    Log.i(TAG, uri.toString());
                }
                if (mimeType != null && mimeType.contains("audio")) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE},
                                10);
                    } else {
                        ApplozicAudioManager.getInstance(context).play(uri, ApplozicDocumentView.this);
                        setAudioIcons();
                        updateApplozicSeekBar();
                    }
                } else {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, mimeType);
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, R.string.info_app_not_found_to_open_file, Toast.LENGTH_LONG).show();
                    }
                }
            }

        });

        downloadInProgressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDownload();
            }
        });

        retryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message.setCanceled(false);
                MessageDatabaseService messageDatabaseService = new MessageDatabaseService(context);
                messageDatabaseService.updateCanceledFlag(message.getMessageId(), 0);
                Intent intent = new Intent(context, MessageIntentService.class);
                intent.putExtra(MobiComKitConstants.MESSAGE_JSON_INTENT, GsonUtils.getJsonFromObject(message, Message.class));
                context.startService(intent);
                showDownloadInProgress();

            }
        });
    }

    public void setAudioIcons() {
        int state = ApplozicAudioManager.getInstance(context).getAudioState(message.getKeyString());
        Utils.printLog(context, "statee:", String.valueOf(state));
        if (state == 1) {
            docIcon.setImageResource(R.drawable.ic_pause_circle_outline);
        } else {
            docIcon.setImageResource(R.drawable.ic_play_circle_outline);
        }
    }

    public boolean isUploadRequire() {
        return (message.hasAttachment() && message.isTypeOutbox() && !message.isSentToServer());
    }

    public boolean isDownloadRequire() {
        return (message.hasAttachment() && message.isSentToServer() && message.getFileMetas() != null && message.getFilePaths() != null);
    }

    public void cancelDownload() {
        if (mDownloadThread == null) {
            if (message.isAttachmentUploadInProgress()) {
                message.setCanceled(true);
            }
            return;
        }
        AttachmentManager.removeDownload(mDownloadThread, true);
        getDownloadProgressLayout().setVisibility(View.GONE);
        showPreview();
    }

    public View getDownloadProgressLayout() {
        return downloadInProgressLayout;
    }

    public void updateApplozicSeekBar() {
        MediaPlayer mediaplayer = ApplozicAudioManager.getInstance(context).getMediaPlayer(message.getKeyString());
        if (mediaplayer == null) {
            audioseekbar.setProgress(0);
        } else if (mediaplayer.isPlaying()) {
            audioseekbar.setMax(mediaplayer.getDuration());
            audioseekbar.setProgress(mediaplayer.getCurrentPosition());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    updateApplozicSeekBar();
                }
            };
            mHandler.postDelayed(runnable, 500);
        } else {
            audioseekbar.setMax(mediaplayer.getDuration());
            audioseekbar.setProgress(mediaplayer.getCurrentPosition());
        }
    }
}