package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.media.MediaRecorder;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.io.File;

/**
 * Created by Rahul-PC on 17-07-2017.
 */

public class ApplozicAudioRecordManager implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    FragmentActivity context;
    String audioFileName, timeStamp;
    private MediaRecorder audioRecorder;
    private String outputFile = null;
    private boolean isRecordring;


    public ApplozicAudioRecordManager(FragmentActivity context) {
        this.context = context;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void recordAudio() {
        try {

            if (isRecordring) {
                ApplozicAudioRecordManager.this.stopRecording();

            } else {
                if (audioRecorder == null) {
                    prepareMediaRecorder();
                }
                audioRecorder.prepare();
                audioRecorder.start();
                isRecordring = true;
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void cancelAudio() {
        if (isRecordring) {
            ApplozicAudioRecordManager.this.stopRecording();
        }

        File file = new File(outputFile);
        if (file != null) {
            Utils.printLog(context, "AudioFRG:", "File deleted...");
            file.delete();
        }

    }

    public void sendAudio() {

        //IF recording is running stoped it ...
        if (isRecordring) {
            stopRecording();
        }

        //FILE CHECK ....
        if (!(new File(outputFile).exists())) {
            Toast.makeText(context, R.string.tap_on_mic_button_to_record_audio, Toast.LENGTH_SHORT).show();
            return;
        }
        ConversationUIService conversationUIService = new ConversationUIService(context);
        conversationUIService.sendAudioMessage(outputFile);


    }


    public void stopRecording() {

        if (audioRecorder != null) {
            try {
                audioRecorder.stop();
            } catch (RuntimeException stopException) {
                Utils.printLog(context, "AudioMsgFrag:", "Runtime exception.This is thrown intentionally if stop is called just after start");
            } finally {
                audioRecorder.release();
                audioRecorder = null;
                isRecordring = false;

            }

        }

    }

    public MediaRecorder prepareMediaRecorder() {

        audioRecorder = new MediaRecorder();
        audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        audioRecorder.setAudioEncodingBitRate(256);
        audioRecorder.setAudioChannels(1);
        audioRecorder.setAudioSamplingRate(44100);
        audioRecorder.setOutputFile(outputFile);
        audioRecorder.setOnInfoListener(this);
        audioRecorder.setOnErrorListener(this);

        return audioRecorder;
    }


    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }
}