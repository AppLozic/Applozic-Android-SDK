package com.applozic.mobicomkit.uiwidgets.attachmentview;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.SeekBar;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by Rahul-PC on 28-02-2017.
 */

public class ApplozicAudioManager {
    private static ApplozicAudioManager myObj;
    private final TelephonyManager mTelephonyMgr;
    public ApplozicDocumentView currentView;
    Map<String, MediaPlayer> pool = new HashMap<>();
    Context context;
    int maxsize = 5;
    String TAG = "ApplozicAudioManager";
    String audio_duration;
    int hours, minute, second, duration;
    PhoneStateListener mPhoneStateListener;

    private ApplozicAudioManager(Context context) {
        this.context = context;
        mTelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        mTelephonyMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public static ApplozicAudioManager getInstance(Context context) {
        try {
            if (myObj == null) {
                myObj = new ApplozicAudioManager(context.getApplicationContext());
            }
            return myObj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void play(Uri uri, final ApplozicDocumentView view) {
        final String key = view.message.getKeyString();
        MediaPlayer mp = pool.get(key);
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.pause();
                return;
            } else {
                mp.seekTo(mp.getCurrentPosition());
                mp.start();
            }
        } else {
            mp = new MediaPlayer();
            if (pool.size() >= maxsize) {
                Map.Entry<String, MediaPlayer> entry = pool.entrySet().iterator().next();
                String first = entry.getKey();
                pool.remove(first);
            }
            pool.put(key, mp);
        }
        pauseOthersifPlaying();
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(context, R.string.unable_to_play_requested_audio_file, Toast.LENGTH_LONG).show();
                return false;
            }
        });
        try {
            if (context != null) {
                mp.setDataSource(context, uri);
                mp.prepare();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mp.start();
        if (currentView != null) {
            currentView.setAudioIcons();
        }
        this.currentView = view;
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
                pool.remove(key);
                currentView.setAudioIcons();
            }
        });
        currentView.audioseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (getMediaPlayer(key) != null) {
                        getMediaPlayer(key).seekTo(progress);
                    }
                }
            }
        });
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    if (getMediaPlayer(key) != null) {
                        getMediaPlayer(key).pause();
                        currentView.setAudioIcons();
                    }
                }
            }
        };
        mTelephonyMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void pauseOthersifPlaying() {
        MediaPlayer m;
        Iterator it = pool.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            {
                m = (MediaPlayer) pair.getValue();
                if (m.isPlaying()) {
                    m.pause();
                }
            }
        }
    }

    int getAudioState(String key) {
        MediaPlayer mp = pool.get(key);
        if (mp != null) {
            if (mp.isPlaying()) {
                return 1;
            }
            return 0;
        }
        return -1;
    }

    public MediaPlayer getMediaPlayer(String key) {
        return pool.get(key);
    }

    public void audiostop() {
        MediaPlayer temp;
        Iterator it = pool.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            {
                temp = (MediaPlayer) pair.getValue();
                temp.stop();
                temp.release();
            }
        }
        pool.clear();
    }

    public String refreshAudioDuration(String filePath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
            duration = duration / 1000;
            hours = duration / 3600;
            minute = duration / 60;
            second = (duration % 60) + 1;
            mediaPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
        audio_duration = String.format("%02d:%02d", minute, second);
        return audio_duration;
    }
}