package com.applozic.mobicomkit.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by sunil on 21/12/2016.
 */

public class SplashScreenActivity extends Activity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    DatabaseHelper helper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.splash_screen_layout);

        // get size config
        final ArrayList getData = helper.getDataConfig();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Applozic.isConnected(SplashScreenActivity.this) && getData.get(0).equals(getData.get(1))) {
                    Intent mainIntent = new Intent(SplashScreenActivity.this, ConversationActivity.class);
                    SplashScreenActivity.this.startActivity(mainIntent);
                    SplashScreenActivity.this.finish();
                } else {
                    Intent mainIntent = new Intent(SplashScreenActivity.this, MainScreen.class);
                    SplashScreenActivity.this.startActivity(mainIntent);
                    SplashScreenActivity.this.finish();
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
