package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by devashish on 02/03/16.
 */
public class AudioMessageFragment extends DialogFragment {

    Button cancel, send;
    TextView txtcount, audioRecordingText;
    ImageButton record;
    CountDownTimer t;
    private MediaRecorder audioRecorder;
    private String outputFile = null;
    private int cnt;
    private boolean isRecordring;

    public static AudioMessageFragment newInstance() {

        AudioMessageFragment f = new AudioMessageFragment();
        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.mobicom_audio_message_layout, container, false);

        this.getDialog().setTitle("Voice Message");
        this.getDialog().setCancelable(Boolean.TRUE);
        this.getDialog().setCanceledOnTouchOutside(Boolean.FALSE);

        record = (ImageButton) v.findViewById(R.id.audio_mic_imageview);
        send = (Button) v.findViewById(R.id.audio_send);
        cancel = (Button) v.findViewById(R.id.audio_cancel);
        txtcount = (TextView) v.findViewById(R.id.txtcount);
        audioRecordingText = (TextView) v.findViewById(R.id.audio_recording_text);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFileName = "AUD_" + timeStamp + "_" + ".m4a";

        outputFile = FileClientService.getFilePath(audioFileName, getActivity().getApplicationContext(), "audio/m4a").getAbsolutePath();
        prepareMediaRecorder();

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (isRecordring) {
                        AudioMessageFragment.this.stopRecording();
                        cancel.setVisibility(View.VISIBLE);
                        send.setVisibility(View.VISIBLE);
                    } else {
                        cancel.setVisibility(View.GONE);
                        send.setVisibility(View.GONE);
                        if (audioRecorder == null) {
                            prepareMediaRecorder();
                        }
                        audioRecordingText.setText(getResources().getString(R.string.stop));
                        audioRecorder.prepare();
                        audioRecorder.start();
                        isRecordring = true;
                        record.setImageResource(R.drawable.applozic_audio_mic_inverted);
                        t.cancel();
                        t.start();
                        cnt = 0;
                    }

                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isRecordring) {
                    AudioMessageFragment.this.stopRecording();
                }

                File file = new File(outputFile);
                if (file != null) {
                    Utils.printLog(getContext(),"AudioFRG:", "File deleted...");
                    file.delete();
                }
                AudioMessageFragment.this.dismiss();

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException, SecurityException, IllegalStateException {

                //IF recording is running stoped it ...
                if (isRecordring) {
                    stopRecording();
                }
                //FILE CHECK ....
                if (!(new File(outputFile).exists())) {
                    Toast.makeText(getContext(), R.string.audio_recording_send_text, Toast.LENGTH_SHORT).show();
                    return;
                }
                ConversationUIService conversationUIService = new ConversationUIService(getActivity());
                conversationUIService.sendAudioMessage(outputFile);
                AudioMessageFragment.this.dismiss();

            }
        });
        // Set Timer
        t = new CountDownTimer(Long.MAX_VALUE, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                cnt++;
                long millis = cnt;
                int seconds = (int) (millis / 60);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                txtcount.setText(String.format("%d:%02d:%02d", minutes, seconds, millis));

            }

            @Override
            public void onFinish() {

            }
        };
        return v;
    }

    public void stopRecording() {

        if (audioRecorder != null) {
            try {
                audioRecorder.stop();
            } catch (RuntimeException stopException) {
                Utils.printLog(getContext(),"AudioMsgFrag:", "Runtime exception.This is thrown intentionally if stop is called just after start");
            } finally {
                audioRecorder.release();
                audioRecorder = null;
                isRecordring = false;
                record.setImageResource(R.drawable.applozic_audio_normal);
                audioRecordingText.setText(getResources().getText(R.string.start_text));
                t.cancel();
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

        return audioRecorder;
    }
}
