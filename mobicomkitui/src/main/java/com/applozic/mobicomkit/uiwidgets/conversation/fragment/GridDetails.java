package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.VideoView;

import com.applozic.mobicomkit.uiwidgets.R;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.applozic.mobicomkit.uiwidgets.R;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;

import java.util.ArrayList;

public class GridDetails extends BaseAdapter {

    private Context mContext;
    private final ArrayList<String> web;
    //private final int[] Imageid;
    private final ArrayList<String> Imageid;
    public GridDetails(Context c, ArrayList<String> web,ArrayList<String> Imageid ) {
       mContext = c;
       this.Imageid = Imageid;
       this.web = web;
    }


    @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return web.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            // TODO Auto-generated method stub
            View grid;

            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {

                grid = new View(mContext);
                grid = inflater.inflate(R.layout.activity_grid_details, null);


                final TextView textView = (TextView) grid.findViewById(R.id.text);

                VideoView videoView = (VideoView)grid.findViewById(R.id.video);

                textView.setText(web.get(position));
                videoView.setVideoPath(Imageid.get(position));
                videoView.start();


            } else {
                grid = (View) convertView;
            }

            return grid;
        }

    }


