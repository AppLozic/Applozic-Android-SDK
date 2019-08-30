
package com.applozic.mobicomkit.uiwidgets.conversation.fragment;


import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.applozic.mobicomkit.uiwidgets.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author nozha.gh
 */
public class CustomGridDetails extends BaseAdapter {
    private Context mContext;
    private final ArrayList<String> gridViewString;
    Config config = new Config();
    private File myFile;
    public static String VariableMsg;
    //  TextView tv;
    TextView msg;
    String CONTACT_ID;


    public CustomGridDetails(Context context, ArrayList<String> gridViewString, EditText footerText, String CONTACT_ID) {
        mContext = context;

        File myFile = new File(mContext.getExternalFilesDir(config.videopath), "LSF");

        this.gridViewString = gridViewString;
        this.myFile = myFile;
        this.msg = footerText;
        this.CONTACT_ID = CONTACT_ID;


    }


    @Override
    public int getCount() {
        return gridViewString.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup parent) {
        final DatabaseHelper helper = new DatabaseHelper(mContext);
        View gridViewAndroid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        if (convertView == null) {


            gridViewAndroid = new View(mContext);


            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            final SharedPreferences.Editor editor = preferences.edit();


            gridViewAndroid = inflater.inflate(R.layout.activity_grid_details, null);
            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.grid_text);
            final VideoView videoView = (VideoView) gridViewAndroid.findViewById(R.id.item_video);
            ImageView retrive = (ImageView) gridViewAndroid.findViewById(R.id.retrive);
            ImageView add = (ImageView) gridViewAndroid.findViewById(R.id.add);

            add.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    Log.i("TAG", "YourAREClicked Add");

                    Log.i("TAG", "footerTextGridDetails " + msg.getText().toString());
                    msg.setText(msg.getText().toString() + " " + gridViewString.get(i));
                    editor.putString(CONTACT_ID, gridViewString.get(i));
                    editor.apply();


                }
            });

            retrive.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    Log.i("TAG", "YourAREClicked Retrive" + msg.getText().toString());

                    // String NumberEntered = msg.getText().toString().substring(0, msg.getText().toString().length());
                    String globalMsg = replaceLast(msg.getText().toString(), gridViewString.get(i), "");
                    Log.i("TAG", "replaceLast " + replaceLast(msg.getText().toString(), gridViewString.get(i), ""));

                    msg.setText(globalMsg);
                    editor.putString(CONTACT_ID, globalMsg);
                    editor.apply();


                }
            });


            textViewAndroid.setText(capitalize(gridViewString.get(i)));
            videoView.setVideoPath(String.valueOf(myFile) + "/" + gridViewString.get(i) + ".mp4");
            Log.i("TAG", "GridDetails " + String.valueOf(myFile) + "/" + gridViewString.get(i) + ".mp4");
            //videoView.seekTo(100);
            videoView.start();
            videoView.requestFocus();
            videoView.setKeepScreenOn(true);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    mp.setLooping(true);
                    videoView.start();
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoView.resume();
                }
            });


            CheckBox star = (CheckBox) gridViewAndroid.findViewById(R.id.star);

          /*  ArrayList Favorite = helper.getFavorite();
            Log.i("TAG", "Favorite " + Favorite);
            for (int j = 0; j < Favorite.size(); j++) {
                String ii = (String) Favorite.get(j);

                Log.i("TAG", "Favorite " + (ii));
               // gridViewAndroid.star.setChecked(Favorite.get(j));


            }*/


            star.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                    int valueChecked = (isChecked) ? 1 : 0;
                    Log.i("TAG", "Star checked" + valueChecked);
                    Log.i("TAG", "Star checked" + isChecked + gridViewString.get(i));
                    helper.updateFavorite(valueChecked, gridViewString.get(i));
                }
            });

        } else {
            gridViewAndroid = (View) convertView;


        }

        return gridViewAndroid;
    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    public static String replaceLast(String text, String substituir, String substituto) {
        // Retourne l'index de la dernière occurrence de "replace"
        int pos = text.lastIndexOf(substituir);
        if (pos > -1) {
            // retourne les caractères après "remplacer"
            return text.substring(0, pos)
                    + substituto
                    + text.substring(pos + substituir.length(), text.length());
        } else
            return text;
    }


}
