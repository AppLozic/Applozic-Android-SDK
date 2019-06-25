package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
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
import android.widget.Toast;
import android.text.TextWatcher;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static java.security.AccessController.getContext;

public class text_to_sign extends AppCompatActivity {
    protected ImageButton sendButton;
    // List view
    private ListView lv;

    // Listview Adapter
    ArrayAdapter<String> adapter;

    // Search EditText
    EditText inputText;
    EditText inputResult;
    static String urlv = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearMe/LSF/video/";
    static String url = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearMe/LSF/lsf.json";

    private VideoView videoView = null;
    String[] videoArray = {"1.mp4", "2.mp4", "3.mp4"};


    // ArrayList for Listview
    ArrayList<HashMap<String, String>> productList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_sign);

        final File myFileVideo = new File(getExternalFilesDir(urlv), "");




        inputText = (EditText) findViewById(R.id.inputText);
        inputResult = (EditText) findViewById(R.id.inputResult);
        videoView = (VideoView)findViewById(R.id.videoView);


        //disable inputResult
       // inputResult.setFocusable(false);

        String path = myFileVideo +"/"+  videoArray[0];


        videoView.setVideoPath(path);
        videoView.start();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp)
            {
                for(int i=0;i<videoArray.length;i++) {
                    String path = myFileVideo + "/" + videoArray[i];
                    Log.i("TAG","pathofvideo : "+ path);
                    videoView.setVideoPath(path);
                }
                    videoView.start();

            }
        });


        sendButton = (ImageButton) findViewById(R.id.conversation_send);

        inputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                //   text_to_sign.this.adapter.getFilter().filter(cs);
                String content = inputText.getText().toString(); //gets you the contents of edit text

                inputResult.setText(content);
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String content = inputText.getText().toString(); //gets you the contents of edit text
                Intent intent = new Intent(text_to_sign.this, ConversationActivity.class);
                intent.putExtra("takeOrder", true);
                intent.putExtra(ConversationUIService.USER_ID, "userId");//RECEIVER USERID
                intent.putExtra(ConversationUIService.DEFAULT_TEXT, content);
             //   intent.putExtra(ConversationUIService.DISPLAY_NAME,"display name");
                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT,true);
              //  intent.putExtra(ConversationUIService.CONVERSATION_ID,conversationId);
                startActivity(intent);


            }
        });

        readFromFile();
    }
    private String readFromFile() {
        File myFile = new File(getExternalFilesDir(url), "");


        String ret = "";

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(myFile);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                ret = stringBuilder.toString();
               JSONObject obj=new JSONObject(ret);
             //   JSONArray jArray = new JSONArray(ret);

               /* if(obj.has("bonjour")){
                    // It exists, do your stuff
                    Log.i("TAG","exists");
                } else {
                    // It doesn't exist, do nothing
                    Log.i("TAG","Notexists");

                }*/
              //  for (int i = 0; i < obj.length(); i++) {
                  //  JSONObject object = obj.optJSONObject(String.valueOf(i));
                    Iterator<String> iterator = obj.keys();
                    while(iterator.hasNext()) {
                    String key = iterator.next();

                    Log.i("TAG", "keyy" + key);

                    try {


                        JSONArray value = (JSONArray) obj.get(key);
                        Log.i("TAG", "keyyValue" + value);
                        for (int i = 0; i < value.length(); i++) {
                            JSONObject object = value.optJSONObject(i);
                            //Log.i("TAG", "keyyValue2222" + object);
                           // String check = object.getString("bonjour");

                            if (object.has("texte")) {
                                String texte = object.getString("texte");
                                Log.i("TAG","existssss" + texte);

                                JsonArray newObject = new JsonArray();
                                newObject.add(texte);


                                Log.i("TAG","newObject" + newObject);

                                if(texte.contains("bonjour")){
                                    // It exists, do your stuff
                                    Log.i("TAG","exists");
                                } else {
                                    // It doesn't exist, do nothing
                                    Log.i("TAG", "Notexists");

                                }

                            }

                        }


                       /* for (int i = 0; i < obj.length(); i++) {
                            JSONObject object = obj.optJSONObject(String.valueOf(i));
                            //  JSONObject value = obj.getJSONObject(key);
                            // JSONObject value=new JSONObject(key);

                            Log.i("TAG", "keyyValue" + object);

                        }*/
                    } catch (Exception e) {
                        Log.i("TAG", "ExceptionKey" + e);
                    }
               // }
                }



                    //Log.i("TAG", "NAMES_LIST: " + names);
               // Log.i("TAG", "VIDEOS_LIST: " + videos);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            try {
                if(inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return ret;

    }




}
