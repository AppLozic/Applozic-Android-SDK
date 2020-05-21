package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;

import com.applozic.mobicomkit.uiwidgets.R;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*
 * Created by Nozha GH
 */
public class sign_to_text extends AppCompatActivity {
    // call database
    public final DatabaseHelper helper = new DatabaseHelper(this);
    GridView gv;
    ImageButton nextBtn, prevBtn;
    TextView dataPaginate;
    public final int ITEMS_PER_PAGE = 6;
    private int currentPage = 0;
    int LAST_PAGE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_to_text);

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        //Get Select item
        final String CONTACT_ID = extras.getString("CONTACT_ID");
        final String Type = extras.getString("type");


        ArrayList getAllCat = helper.getAllCategory("List");

        Log.i("TAG", "getAllCat" + getAllCat);



        Log.i("TAG", "getAll" +  helper.getAllData());



        final int TOTAL_NUM_ITEMS = getAllCat.size();

        final int ITEMS_REMAINING = TOTAL_NUM_ITEMS % ITEMS_PER_PAGE;
         LAST_PAGE = TOTAL_NUM_ITEMS / ITEMS_PER_PAGE;

        final int totalPages = TOTAL_NUM_ITEMS / ITEMS_PER_PAGE;

        dataPaginate = (TextView) findViewById(R.id.dataPaginate);
        dataPaginate.setText((currentPage+1) + " / " + (totalPages+1));
        gv = (GridView) findViewById(R.id.gv);
        nextBtn = (ImageButton) findViewById(R.id.nextBtn);
        prevBtn = (ImageButton) findViewById(R.id.prevBtn);
        prevBtn.setEnabled(false);
        prevBtn.getBackground().setAlpha(45);

        toggleButtons(totalPages);


        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(sign_to_text.this, generatePage(currentPage, LAST_PAGE, ITEMS_REMAINING).get(i), Toast.LENGTH_SHORT).show();
            }
        });

        gv.setAdapter(new CustomGrid(sign_to_text.this,CONTACT_ID,Type, generatePage(currentPage, LAST_PAGE, ITEMS_REMAINING)));

        nextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                currentPage += 1;
                dataPaginate.setText((currentPage+1) + " / " + (totalPages+1));

                // enableDisableButtons();
                gv.setAdapter(new CustomGrid(sign_to_text.this,CONTACT_ID,Type, generatePage(currentPage, LAST_PAGE, ITEMS_REMAINING)));
                toggleButtons(totalPages);

            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage -= 1;
                dataPaginate.setText((currentPage+1) + " / " + (totalPages+1));

                gv.setAdapter(new CustomGrid(sign_to_text.this,CONTACT_ID, Type, generatePage(currentPage, LAST_PAGE, ITEMS_REMAINING)));

                toggleButtons(totalPages);
            }
        });


    }

    private void toggleButtons(int totalPages) {
        Log.i("TAG", "items "+ currentPage + totalPages + LAST_PAGE);

        if (currentPage == totalPages && totalPages != 0 ) {
            nextBtn.setEnabled(false);
            nextBtn.getBackground().setAlpha(45);
            prevBtn.setEnabled(true);
            prevBtn.getBackground().setAlpha(0xFF);
        } else if (currentPage == 0 && LAST_PAGE != currentPage) {
            prevBtn.setEnabled(false);
            prevBtn.getBackground().setAlpha(45);
            nextBtn.setEnabled(true);
            nextBtn.getBackground().setAlpha(0xFF);
        } else if (currentPage >= 1 && currentPage <= 6) {
            nextBtn.setEnabled(true);
            prevBtn.setEnabled(true);
            nextBtn.getBackground().setAlpha(0xFF);
            prevBtn.getBackground().setAlpha(0xFF);
        }
        else if (currentPage == LAST_PAGE){
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            prevBtn.getBackground().setAlpha(45);
            nextBtn.getBackground().setAlpha(45);
        }



    }

    public ArrayList<String> generatePage(int currentPage, int LAST_PAGE, int ITEMS_REMAINING) {

        Log.i("TAG", "generatePage " + currentPage + LAST_PAGE + ITEMS_REMAINING);
        ArrayList getAllCat = helper.getAllCategory("List");

        int startItem = currentPage * ITEMS_PER_PAGE;
        int numOfData = ITEMS_PER_PAGE;

        ArrayList<String> pageData = new ArrayList<>();


        if (currentPage == LAST_PAGE && ITEMS_REMAINING > 0) {
            for (int i = startItem; i < startItem + ITEMS_REMAINING; i++) {
                pageData.add(String.valueOf(getAllCat.get(i)));
            }
        } else {
            for (int i = startItem; i <= startItem + numOfData ; i++) {
                pageData.add(String.valueOf(getAllCat.get(i)) );
            }
        }

        Log.i("TAG", "pageData " + pageData);
        return pageData;
    }


}


