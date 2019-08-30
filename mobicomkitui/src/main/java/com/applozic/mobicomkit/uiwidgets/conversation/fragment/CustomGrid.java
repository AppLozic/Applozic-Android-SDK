package com.applozic.mobicomkit.uiwidgets.conversation.fragment;


import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.applozic.mobicomkit.uiwidgets.R;

import java.util.ArrayList;

/**
 * @author nozha.gh
 */
public class CustomGrid extends BaseAdapter {
    private Context mContext;
    private final ArrayList<String> gridViewString;
    Config config = new Config();
    private File myFile;
    private String CONTACT_ID;

    //private final int[] gridViewImageId;

    public CustomGrid(Context context,String CONTACT_ID, ArrayList<String> gridViewString) {
        mContext = context;
        File myFile = new File(mContext.getExternalFilesDir(config.videopath), "LSF");

        // this.gridViewImageId = gridViewImageId;
        this.gridViewString = gridViewString;
        this.myFile = myFile;
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
        View gridViewAndroid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            gridViewAndroid = new View(mContext);
            gridViewAndroid = inflater.inflate(R.layout.signtext, null);
            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.grid_text);
            final VideoView videoView = (VideoView) gridViewAndroid.findViewById(R.id.item_video);
            CardView card = (CardView) gridViewAndroid.findViewById(R.id.cardone);


            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, details_sign_to_text.class);
                    intent.putExtra("subCategory", gridViewString.get(i));
                    intent.putExtra("CONTACT_ID", CONTACT_ID);
                    mContext.startActivity(intent);


                }
            });

            textViewAndroid.setText(capitalize(gridViewString.get(i)));
            videoView.setVideoPath(String.valueOf(myFile) + "/" + gridViewString.get(i) + ".mp4");
            videoView.start();



            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();

                    //videoItem.start();
                    // TODO Auto-generated method stub
                    mp.setLooping(true);
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

}
