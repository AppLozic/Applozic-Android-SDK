package com.applozic.mobicomkit.uiwidgets.conversation.activity;

import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.applozic.mobicomkit.broadcast.ConnectivityReceiver;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.database.ContactDatabase;
import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.people.contact.ContactSelectionFragment;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.file.FileUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.SearchListFragment;
import com.applozic.mobicommons.people.channel.Channel;

/**
 * Created by sunil on 6/2/16.
 */
public class ContactSelectionActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static final String CHANNEL = "CHANNEL_NAME";
    public static final String CHANNEL_OBJECT = "CHANNEL";
    public static final String CHECK_BOX = "CHECK_BOX";
    public static final String IMAGE_LINK = "IMAGE_LINK";
    public static final String GROUP_TYPE = "GROUP_TYPE";
    public static final String CONTACTS_GROUP_ID = "CONTACTS_GROUP_ID";
    public static boolean isSearching = false;
    protected SearchView searchView;
    Channel channel;
    boolean disableCheckBox;
    int groupType;
    ContactDatabase contactDatabase;
    ContactSelectionFragment contactSelectionFragment;
    String contactsGroupId;
    AlCustomizationSettings alCustomizationSettings;
    private String name;
    private String imageUrl;
    private ActionBar mActionBar;
    private SearchListFragment searchListFragment;
    private boolean isSearchResultView = false;
    private String mSearchTerm;
    private AppContactService contactService;
    private ConnectivityReceiver connectivityReceiver;

    public static void addFragment(FragmentActivity fragmentActivity, Fragment fragmentToAdd, String fragmentTag) {
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.layout_child_activity, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1) {
            supportFragmentManager.popBackStack();
        }
        fragmentTransaction.addToBackStack(fragmentTag);
        fragmentTransaction.commitAllowingStateLoss();
        supportFragmentManager.executePendingTransactions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_select_layout);
        contactDatabase = new ContactDatabase(this);
        contactSelectionFragment = new ContactSelectionFragment();
        setSearchListFragment(contactSelectionFragment);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        contactService = new AppContactService(this);
        mActionBar = getSupportActionBar();
        String jsonString = FileUtils.loadSettingsJsonFile(getApplicationContext());
        if (!TextUtils.isEmpty(jsonString)) {
            alCustomizationSettings = (AlCustomizationSettings) GsonUtils.getObjectFromJson(jsonString, AlCustomizationSettings.class);
        } else {
            alCustomizationSettings = new AlCustomizationSettings();
        }
        if (!TextUtils.isEmpty(alCustomizationSettings.getThemeColorPrimary()) && !TextUtils.isEmpty(alCustomizationSettings.getThemeColorPrimaryDark())) {
            mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(alCustomizationSettings.getThemeColorPrimary())));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.parseColor(alCustomizationSettings.getThemeColorPrimaryDark()));
            }
        }
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        if (getIntent().getExtras() != null) {
            channel = (Channel) getIntent().getSerializableExtra(CHANNEL_OBJECT);
            disableCheckBox = getIntent().getBooleanExtra(CHECK_BOX, false);
            mActionBar.setTitle(R.string.channel_member_title);
            name = getIntent().getStringExtra(CHANNEL);
            imageUrl = getIntent().getStringExtra(IMAGE_LINK);
            contactsGroupId = getIntent().getStringExtra(CONTACTS_GROUP_ID);
            groupType = getIntent().getIntExtra(GROUP_TYPE, Channel.GroupType.PUBLIC.getValue().intValue());
        } else {
            mActionBar.setTitle(R.string.channel_members_title);
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHANNEL_OBJECT, channel);
        bundle.putBoolean(CHECK_BOX, disableCheckBox);
        bundle.putString(CHANNEL, name);
        bundle.putString(IMAGE_LINK, imageUrl);
        bundle.putInt(GROUP_TYPE, groupType);
        contactSelectionFragment.setArguments(bundle);
        if (contactsGroupId != null) {
            bundle.putString(CONTACTS_GROUP_ID, contactsGroupId);
        }
        addFragment(this, contactSelectionFragment, "ContactSelectionFragment");
        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_create_menu, menu);
        menu.removeItem(R.id.Next);
        if (disableCheckBox) {
            menu.removeItem(R.id.Done);
        }
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        if (Utils.hasICS()) {
            searchItem.collapseActionView();
        }
        searchView.setOnQueryTextListener(this);
        searchView.setSubmitButtonEnabled(true);
        searchView.setIconified(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            onSearchRequested();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        // Don't allow another search if this activity instance is already showing
        // search results. Only used pre-HC.
        return !isSearchResultView && super.onSearchRequested();
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        this.mSearchTerm = query;
        isSearching = false;
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        this.mSearchTerm = query;
        if (getSearchListFragment() != null) {
            getSearchListFragment().onQueryTextChange(query);
            isSearching = true;

            if (query.isEmpty()) {
                isSearching = false;
            }
        }
        return true;
    }

    public SearchListFragment getSearchListFragment() {
        return searchListFragment;
    }

    public void setSearchListFragment(SearchListFragment searchListFragment) {
        this.searchListFragment = searchListFragment;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (connectivityReceiver != null) {
                unregisterReceiver(connectivityReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
