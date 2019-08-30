package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class details_sign_to_text extends AppCompatActivity {

    // call database
    public final DatabaseHelper helper = new DatabaseHelper(this);
    GridView gv;
    ImageButton nextBtn, prevBtn;
    TextView dataPaginate, not_sub_cat;
    public final int ITEMS_PER_PAGE = 6;
    private int currentPage = 0;
    protected Contact contact;
    AppContactService appContactService;
    public final ConcatWithFFMpeg concat = new ConcatWithFFMpeg();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_sign_to_text);
        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        //Get Select item
        String subCategory = extras.getString("subCategory");
        final String CONTACT_ID = extras.getString("CONTACT_ID");
        Log.i("TAG", "subCategory " + subCategory);


        final ArrayList getAllCat = helper.getAllCategory(subCategory);
        Log.i("TAG", "getAllCat" + getAllCat);


        final int TOTAL_NUM_ITEMS = getAllCat.size();

        final int ITEMS_REMAINING = TOTAL_NUM_ITEMS % ITEMS_PER_PAGE;
        final int LAST_PAGE = TOTAL_NUM_ITEMS / ITEMS_PER_PAGE;

        final int totalPages = TOTAL_NUM_ITEMS / ITEMS_PER_PAGE;

        dataPaginate = (TextView) findViewById(R.id.dataPaginate);
        dataPaginate.setText((currentPage + 1) + " / " + (totalPages + 1));
        gv = (GridView) findViewById(R.id.gv);


        nextBtn = (ImageButton) findViewById(R.id.nextBtn);
        prevBtn = (ImageButton) findViewById(R.id.prevBtn);
        not_sub_cat = (TextView) findViewById(R.id.not_sub_cat);
        LinearLayout main_edit_text_linear_layout = (LinearLayout) findViewById(R.id.main_edit_text_linear_layout);
        not_sub_cat.setVisibility(View.INVISIBLE);
        prevBtn.setEnabled(false);
        prevBtn.getBackground().setAlpha(45);

        final EditText footerText = (EditText) findViewById(R.id.conversation_message);
        ImageButton send = (ImageButton) findViewById(R.id.conversation_send);

        //retrive data from SharedPreferences
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String msg = preferences.getString(CONTACT_ID, "");
        Log.i("TAG", "SharedPreferences signtext" + msg);

        footerText.setText(msg);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Log.i("TAG", "***conversation_send***" + CONTACT_ID);
                //  final String message = preferences.getString(CONTACT_ID, "");
                Log.i("TAG", "***conversation_send***" + msg);


               /* Intent intent = new Intent(details_sign_to_text.this, ConversationActivity.class);
                intent.putExtra(ConversationUIService.USER_ID, CONTACT_ID);
                intent.putExtra(ConversationUIService.DEFAULT_TEXT, footerText.getText().toString());
                intent.putExtra(ConversationUIService.TAKE_ORDER, true); //Skip chat list for showing on back press
                startActivity(intent);*/
                concat.loadFFMpegBinary(details_sign_to_text.this);
                concat.sendVideoMessage(details_sign_to_text.this, footerText.getText().toString().trim(), CONTACT_ID);
                footerText.setText("");
                Intent intent = new Intent(details_sign_to_text.this, ConversationActivity.class);
                intent.putExtra(ConversationUIService.USER_ID, CONTACT_ID);
                intent.putExtra(ConversationUIService.DEFAULT_TEXT, "");
                intent.putExtra(ConversationUIService.TAKE_ORDER, true); //Skip chat list for showing on back press
                startActivity(intent);


            }
        });


        Log.i("TAG", "footerText " + footerText);
        // gv.setAdapter(new CustomGridDetails(details_sign_to_text.this, generatePage(currentPage, LAST_PAGE, ITEMS_REMAINING, getAllCat), footerText, CONTACT_ID));
        if (getAllCat.size() != 0) {

            gv.setAdapter(new CustomGridDetails(details_sign_to_text.this, generatePage(currentPage, LAST_PAGE, ITEMS_REMAINING, getAllCat), footerText, CONTACT_ID));


        } else {
            dataPaginate.setVisibility(View.INVISIBLE);
            prevBtn.setVisibility(View.INVISIBLE);
            nextBtn.setVisibility(View.INVISIBLE);
            main_edit_text_linear_layout.setVisibility(View.INVISIBLE);

            not_sub_cat.setVisibility(View.VISIBLE);

        }

        nextBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                currentPage += 1;
                dataPaginate.setText((currentPage + 1) + " / " + (totalPages + 1));

                // enableDisableButtons();
                gv.setAdapter(new CustomGridDetails(details_sign_to_text.this, generatePage(currentPage, LAST_PAGE, ITEMS_REMAINING, getAllCat), footerText, CONTACT_ID));
                toggleButtons(totalPages);

            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage -= 1;
                dataPaginate.setText((currentPage + 1) + " / " + (totalPages + 1));

                gv.setAdapter(new CustomGridDetails(details_sign_to_text.this, generatePage(currentPage, LAST_PAGE, ITEMS_REMAINING, getAllCat), footerText, CONTACT_ID));

                toggleButtons(totalPages);
            }
        });

      /*  ArrayList Favorite = helper.getFavorite();
        Log.i("TAG", "Favorite " + Favorite);

        for (int j = 0; j < Favorite.size(); j++) {
            Log.i("TAG", "Favorite " + i);
            gv.setItemChecked(i, true);

        }*/


    }

    private void toggleButtons(int totalPages) {
        if (currentPage == totalPages) {
            nextBtn.setEnabled(false);
            nextBtn.getBackground().setAlpha(45);
            prevBtn.setEnabled(true);
            prevBtn.getBackground().setAlpha(0xFF);
        } else if (currentPage == 0) {
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

    }

    public ArrayList<String> generatePage(int currentPage, int LAST_PAGE, int ITEMS_REMAINING, ArrayList getAllCat) {

        Log.i("TAG", "generatePage" + currentPage + LAST_PAGE + ITEMS_REMAINING);

        int startItem = currentPage * ITEMS_PER_PAGE;
        int numOfData = ITEMS_PER_PAGE;

        ArrayList<String> pageData = new ArrayList<>();


        if (currentPage == LAST_PAGE && ITEMS_REMAINING > 0) {
            for (int i = startItem; i < startItem + ITEMS_REMAINING; i++) {
                pageData.add(String.valueOf(getAllCat.get(i)));
            }
        } else {
            for (int i = startItem; i < startItem + numOfData; i++) {
                pageData.add(String.valueOf(getAllCat.get(i)));
            }
        }

        Log.i("TAG", "pageData " + pageData);
        return pageData;
    }

}

