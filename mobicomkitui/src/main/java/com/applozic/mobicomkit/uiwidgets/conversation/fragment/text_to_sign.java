package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
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
import android.widget.CheckBox;
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
import java.util.StringTokenizer;

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
    TextView no_Translation;
    String resultTranslation;
    ArrayList AllCategory;
    String TAG = "text_to_sign";
    public final ConcatWithFFMpeg concat = new ConcatWithFFMpeg();


    private VideoView videoView;
    ArrayList<String> videoArray = new ArrayList<String>();
    private File myFileVideo;
    private CheckBox sendToServer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_sign);

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        //Get Select item
        final String CONTACT_ID = extras.getString("CONTACT_ID");

        no_Translation = (TextView) findViewById(R.id.no_traduction);
        no_Translation.setVisibility(View.INVISIBLE);
        AllCategory = helper.getAllCategoryWithoutCondition();
        Log.i(TAG, "AllCategory " + AllCategory);

        myFileVideo = new File(getExternalFilesDir(config.videopath), "");

        inputMsg = (MultiAutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView1);
        inputResult = (EditText) findViewById(R.id.conversation_message);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, AllCategory);

        inputMsg.setAdapter(adapter);
        inputMsg.setTokenizer(new SpaceTokenizer());

        translation = (Button) findViewById(R.id.translation);
        translation.setEnabled(false);
        translation.getBackground().setAlpha(45);


        videoView = (VideoView) findViewById(R.id.videoView);
        sendToServer = (CheckBox) findViewById(R.id.sendToServer);
        videoView.setVisibility(View.INVISIBLE);

        //translation message written with user
        translation.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                //call get api server
               Boolean check =  sendToServer.isChecked();
               if (check){
                   new RequestAsync().execute();
               }else{
                   localTranslation();
               }

            }
        });


        inputMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i(TAG, "ChangedListener onTextChanged" + s);

                // TODO Auto-generated method stub
                translation.setEnabled(true);
                translation.getBackground().setAlpha(0xFF);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.i(TAG, "ChangedListener beforeTextChanged" + s);
                // TODO Auto-generated method stub
            }
            @Override
            public void afterTextChanged(Editable s) {
                Log.i(TAG, "ChangedListener afterTextChanged" + s);
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

                concat.sendVideoMessage(text_to_sign.this, resultTranslation ,inputResult.getText().toString(), CONTACT_ID);

                Intent intent = new Intent(text_to_sign.this, ConversationActivity.class);
                intent.putExtra(ConversationUIService.USER_ID, CONTACT_ID);
                intent.putExtra(ConversationUIService.DEFAULT_TEXT, "");
                intent.putExtra(ConversationUIService.TAKE_ORDER, true); //Skip chat list for showing on back press
                startActivity(intent);
            }
        });

    }

   private void localTranslation(){
        resultTranslation = "";
       videoView.setVisibility(View.VISIBLE);
       contentMsg = inputMsg.getText().toString();

       resultTranslation = contentMsg;

       Log.i(TAG, "contentMsg" + contentMsg);
       final String[] splited = contentMsg.trim().split("\\s+");
       Log.i(TAG, "splitedcontentMsg" + Arrays.toString(splited));

       for (int i = 0; i < splited.length; i++) {
           if (AllCategory.contains(splited[i])) {
               Log.i(TAG, "CheckMsgcontains" + true);
               videoArray.add(splited[i]);
           }
           Log.i(TAG, "CheckvideoArray" + String.valueOf(videoArray));

       }

       if (videoArray.size() != 0) {

           inputResult.setText(contentMsg);
           final String path = myFileVideo + "/LSF/video/" + videoArray.get(0) + ".mp4";
           videoView.setVideoPath(path);
           videoView.start();


           Log.i(TAG, "CheckvideoArray size " + videoArray.size());

           videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

               @Override
               public void onCompletion(MediaPlayer mp) {
                   for (int i = 0; i < splited.length - 1; i++) {
                       String path = myFileVideo + "/LSF/video/" + videoArray.get(i) + ".mp4";
                       Log.i(TAG, "pathofvideo : " + path);
                       Log.i(TAG, "pathofvideo size: " + splited.length);
                       videoView.setVideoPath(path);
                   }
                   videoView.start();

               }
           });

       } else if (videoArray.size() == 0) {
           videoView.setVisibility(View.INVISIBLE);
           no_Translation.setVisibility(View.VISIBLE);
       }
   }

    class RequestAsync extends AsyncTask<String,String,ArrayList<String>> {
        @SuppressLint("WrongThread")
        @Override
        protected ArrayList doInBackground(String... strings) {

            contentMsg = inputMsg.getText().toString();
            final String[] splited = contentMsg.trim().split("\\s+");


            try {
                 String result = "";

                //GET Request
                for (int i = 0; i < splited.length; i++) {

                    result += splited[i]+ " ";

                }

                return RequestHandler.sendGet(config.url_linguistic_server, "text="+result);
            }
            catch(Exception e){
                Log.e("RequestHandler","Exception: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> s) {
            if(s!=null){
                resultTranslation = "";
                Log.i("RequestHandler", "result " + s);
                //Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                for (String text : s)
                {
                    resultTranslation += text + "\t";
                    Log.i("RequestHandler", "resultTranslation " + resultTranslation);
                }

                showVideos(s);
            }else{
                videoView.setVisibility(View.INVISIBLE);
                no_Translation.setVisibility(View.VISIBLE);
            }
        }
    }

    private  void showVideos(final ArrayList outputVid){
        videoView.setVisibility(View.VISIBLE);
        // if (videoArray.size() != 0) {
        Log.i("RequestHandler", "outputVid "+outputVid);
            inputResult.setText(contentMsg);
            final String path = myFileVideo + "/LSF/video/" + outputVid.get(0) + ".mp4";
            videoView.setVideoPath(path);
            videoView.start();


            Log.i(TAG, "CheckvideoArray size " + videoArray.size());

            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    for (int i = 0; i < outputVid.size() - 1; i++) {
                        String path = myFileVideo + "/LSF/video/" + videoArray.get(i) + ".mp4";
                        Log.i(TAG, "pathofvideo : " + path);
                        Log.i(TAG, "pathofvideo size: " + outputVid.size());
                        videoView.setVideoPath(path);
                    }
                    videoView.start();
                }
            });

       /* } else if (videoArray.size() == 0) {
            videoView.setVisibility(View.INVISIBLE);
            no_traduction.setVisibility(View.VISIBLE);
        }*/
    }

}
