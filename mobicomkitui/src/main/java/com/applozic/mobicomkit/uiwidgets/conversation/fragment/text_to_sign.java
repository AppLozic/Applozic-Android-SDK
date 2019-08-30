package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.ApplozicService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextWatcher;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.security.AccessController.getContext;

/*
 * Created by Nozha GH
 */
public class text_to_sign extends AppCompatActivity {

    // call database
    public final DatabaseHelper helper = new DatabaseHelper(this);

    protected ImageButton sendButton;
    // List view
    private ListView lv;
    Config config = new Config();

    // Listview Adapter
    ArrayAdapter<String> adapter;
    Button translation;
    EditText inputResult;
    MultiAutoCompleteTextView inputMsg;
    String contentMsg;
    TextView no_traduction;
    public final ConcatWithFFMpeg concat = new ConcatWithFFMpeg();


    private VideoView videoView;
    ArrayList<String> videoArray = new ArrayList<String>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_sign);

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        //Get Select item
        final String CONTACT_ID = extras.getString("CONTACT_ID");

        no_traduction = (TextView) findViewById(R.id.no_traduction);
        no_traduction.setVisibility(View.INVISIBLE);
        final ArrayList AllCategory = helper.getAllCategoryWithoutCondition();
        Log.i("TAG", "AllCategory " + AllCategory);

        final File myFileVideo = new File(getExternalFilesDir(config.videopath), "");


        inputMsg = (MultiAutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView1);
        inputResult = (EditText) findViewById(R.id.conversation_message);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, AllCategory);

        inputMsg.setAdapter(adapter);
        inputMsg.setTokenizer(new SpaceTokenizer());

        translation = (Button) findViewById(R.id.translation);
        translation.setEnabled(false);
        translation.getBackground().setAlpha(45);


        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVisibility(View.INVISIBLE);

        translation.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                videoView.setVisibility(View.VISIBLE);
                contentMsg = inputMsg.getText().toString();

                Log.i("TAG", "contentMsg" + contentMsg);
                final String[] splited = contentMsg.trim().split("\\s+");
                Log.i("TAG", "splitedcontentMsg" + Arrays.toString(splited));

                for (int i = 0; i < splited.length; i++) {
                    if (AllCategory.contains(splited[i])) {
                        Log.i("TAG", "CheckMsgcontains" + true);
                        videoArray.add(splited[i]);
                    }
                    Log.i("TAG", "CheckvideoArray" + String.valueOf(videoArray));

                }


                if (videoArray.size() != 0) {

                    inputResult.setText(contentMsg);
                    final String path = myFileVideo + "/LSF/" + videoArray.get(0) + ".mp4";
                    videoView.setVideoPath(path);
                    videoView.start();


                    Log.i("TAG", "CheckvideoArray size " + videoArray.size());

                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            for (int i = 0; i < splited.length - 1; i++) {
                                String path = myFileVideo + "/LSF/" + videoArray.get(i) + ".mp4";
                                Log.i("TAG", "pathofvideo : " + path);
                                Log.i("TAG", "pathofvideo size: " + splited.length);
                                videoView.setVideoPath(path);
                            }
                            videoView.start();

                        }
                    });

                } else if (videoArray.size() == 0) {
                    videoView.setVisibility(View.INVISIBLE);
                    no_traduction.setVisibility(View.VISIBLE);
                }
            }
        });


        inputMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("TAG", "ChangedListener onTextChanged" + s);

                // TODO Auto-generated method stub
                translation.setEnabled(true);
                translation.getBackground().setAlpha(0xFF);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i("TAG", "ChangedListener beforeTextChanged" + s);
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.i("TAG", "ChangedListener afterTextChanged" + s);
                videoArray.clear();
                // contentMsg = "";
                contentMsg = inputMsg.getText().toString();
                inputResult.setText("");


                // TODO Auto-generated method stub
                if (s.length() == 0) {
                    translation.setEnabled(false);
                    translation.getBackground().setAlpha(45);

                }
            }
        });


        sendButton = (ImageButton) findViewById(R.id.conversation_send);

        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                concat.loadFFMpegBinary(text_to_sign.this);
                String message = videoArray.toString().replaceAll("\\[|\\]|,", "");

                concat.sendVideoMessage(text_to_sign.this, inputResult.getText().toString(), CONTACT_ID);

                Intent intent = new Intent(text_to_sign.this, ConversationActivity.class);
                intent.putExtra(ConversationUIService.USER_ID, CONTACT_ID);
                intent.putExtra(ConversationUIService.DEFAULT_TEXT, "");
                intent.putExtra(ConversationUIService.TAKE_ORDER, true); //Skip chat list for showing on back press
                startActivity(intent);
            }
        });

    }


}
