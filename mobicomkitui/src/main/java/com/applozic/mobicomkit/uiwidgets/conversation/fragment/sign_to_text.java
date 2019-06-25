package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.Paginator;
import com.applozic.mobicomkit.uiwidgets.R;
import com.google.android.gms.common.api.Response;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.io.File;
import java.util.ArrayList;

public class sign_to_text extends AppCompatActivity {

    //GridLayout mainGrid;
    GridView gridView;
    ArrayList<Item> gridArray = new ArrayList<Item>();
   // CustomGridViewAdapter customGridAdapter;
    CustomGrid customGrid;

    static String urlv = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearMe/LSF/";



    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_sign_to_text);

          /*  CustomGrid adapter = new CustomGrid(sign_to_text.this, web, imageId);
            grid = (GridView) findViewById(R.id.grid);
            grid.setAdapter(adapter);
            grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Toast.makeText(sign_to_text.this, "You Clicked at " + web[+position], Toast.LENGTH_SHORT).show();

                }
            });*/


        //set grid view item
        Bitmap homeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.list);
        Bitmap userIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.grid);
        File myFile = new File(getExternalFilesDir(urlv), "jour.mp4");


        gridArray.add(new Item(String.valueOf(myFile),"1"));
        gridArray.add(new Item(String.valueOf(myFile),"2"));
        gridArray.add(new Item(String.valueOf(myFile),"3"));
        gridArray.add(new Item(String.valueOf(myFile),"4"));
        gridArray.add(new Item(String.valueOf(myFile),"5"));
        gridArray.add(new Item(String.valueOf(myFile),"6"));
        gridArray.add(new Item(String.valueOf(myFile),"7"));
        gridArray.add(new Item(String.valueOf(myFile),"8"));
        gridArray.add(new Item(String.valueOf(myFile),"9"));
        gridArray.add(new Item(String.valueOf(myFile),"10"));
        gridArray.add(new Item(String.valueOf(myFile),"11"));
        gridArray.add(new Item(String.valueOf(myFile),"12"));
        gridArray.add(new Item(String.valueOf(myFile),"13"));
        gridArray.add(new Item(String.valueOf(myFile),"14"));
        gridArray.add(new Item(String.valueOf(myFile),"15"));
        gridArray.add(new Item(String.valueOf(myFile),"16"));
        gridArray.add(new Item(String.valueOf(myFile),"17"));
        gridArray.add(new Item(String.valueOf(myFile),"18"));
        gridArray.add(new Item(String.valueOf(myFile),"19"));
        gridArray.add(new Item(String.valueOf(myFile),"20"));
        gridArray.add(new Item(String.valueOf(myFile),"21"));
        gridArray.add(new Item(String.valueOf(myFile),"22"));
        gridArray.add(new Item(String.valueOf(myFile),"23"));
        gridArray.add(new Item(String.valueOf(myFile),"24"));

        Log.i("TAG","CheckPath : "+String.valueOf(myFile));



        gridView = (GridView) findViewById(R.id.grid);
        customGrid = new CustomGrid(this, R.layout.grid_single, gridArray);
        gridView.setAdapter(customGrid);


       /* gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()  {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(sign_to_text.this, "You Clicked at " + position, Toast.LENGTH_SHORT).show();
                Log.i("TAG","AdapterViewGrid"+ parent);
                Log.i("TAG","AdapterViewGrid"+ position);


            }
        });*/

    }




}


