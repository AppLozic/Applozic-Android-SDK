package com.applozic.mobicomkit.sample;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.UserLogoutTask;
import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageIntentService;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.feed.TopicDetail;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicConversationCreateTask;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.people.channel.Conversation;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, EcommerceFragment.OnFragmentInteractionListener {
    public static final String TAKE_ORDER = "takeOrder";
    public static final String TAG = "MainActivity";
    public static final String TAKE_ORDER_USERID_METADATA = "com.applozic.take.order.userId";
    private static final String CONVERSATION_FRAGMENT = "ConversationFragment";
    private UserLogoutTask userLogoutTask;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */

    private CharSequence mTitle;

    public MainActivity() {

    }

    public static void addFragment(FragmentActivity fragmentActivity, Fragment fragmentToAdd, String fragmentTag) {

        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();

        //Fragment activeFragment = UIService.getActiveFragment(fragmentActivity);
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        /*if (null != activeFragment) {
            fragmentTransaction.hide(activeFragment);
        }*/

        fragmentTransaction.replace(R.id.container, fragmentToAdd,
                fragmentTag);

        if (supportFragmentManager.getBackStackEntryCount() > 1) {
            supportFragmentManager.popBackStack();
        }
        fragmentTransaction.addToBackStack(fragmentTag);
        fragmentTransaction.commit();
        supportFragmentManager.executePendingTransactions();
        //Log.i(TAG, "BackStackEntryCount: " + supportFragmentManager.getBackStackEntryCount());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        MobiComUserPreference userPreference = MobiComUserPreference.getInstance(this);
        if (!userPreference.isRegistered()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return;
        }

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        //Put Support Contact Data
        buildSupportContactData();

        /*else {
            Intent intent = new Intent(this, ConversationActivity.class);
                startActivity(intent);
        }*/

        /*ApplozicSetting.getInstance(this).setColor(ApplozicSetting.CUSTOM_MESSAGE_BACKGROUND_COLOR, Color.parseColor("#FFB3E5FC"));
        Message message = new Message("contact@applozic.com", "hey! here's a match <3");
        new MobiComMessageService(this, MessageIntentService.class).sendCustomMessage(message);*/
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        if (position == 1) {
            Intent intent = new Intent(this, ConversationActivity.class);
            if (ApplozicClient.getInstance(this).isContextBasedChat()) {
                intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
            }
            startActivity(intent);
            return;
        }/*
        if (position == 1) {
            ConversationFragment conversationFragment = new ConversationFragment();
            Contact contact = new Contact(this, "mobicomkit");
            mTitle = getString(R.string.user_id);
            addFragment(this, conversationFragment, "conversationFragment");
            conversationFragment.loadConversation(contact);
            return;

        }*/
        if (position == 0) {
            mTitle = getString(R.string.ecommerce);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, EcommerceFragment.newInstance("", ""))
                    .commit();
            return;
        }

        if (position == 2) {

            UserLogoutTask.TaskListener userLogoutTaskListener = new UserLogoutTask.TaskListener() {

                @Override
                public void onSuccess(Context context) {
                    userLogoutTask = null;
                    Toast.makeText(getBaseContext(),getBaseContext().getString(R.string.log_out_successful), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(Exception exception) {
                    userLogoutTask = null;
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle(getString(R.string.text_alert));
                    alertDialog.setMessage(exception.toString());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_alert),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    if (!isFinishing()) {
                        alertDialog.show();
                    }
                }
            };

            userLogoutTask = new UserLogoutTask(userLogoutTaskListener, this);
            userLogoutTask.execute((Void) null);

        }

        if (position == 3) {
            Map<String, String> messageMetaData = new HashMap<>();
            messageMetaData.put(Message.MetaDataType.KEY.getValue(), Message.MetaDataType.HIDDEN.getValue());
            Message message = new Message();
            MobiComUserPreference userPreferences = MobiComUserPreference.getInstance(MainActivity.this);
            message.setContactIds("android");
            message.setTo("android");
            message.setContentType(Message.ContentType.CUSTOM.getValue());
            message.setMessage("this is meta data hidden");
            message.setMetadata(messageMetaData);
            message.setStoreOnDevice(Boolean.TRUE);
            message.setRead(Boolean.TRUE);
            message.setCreatedAtTime(System.currentTimeMillis() + userPreferences.getDeviceTimeOffset());
            message.setSendToDevice(Boolean.FALSE);
            message.setType(Message.MessageType.MT_OUTBOX.getValue());
            message.setDeviceKeyString(userPreferences.getDeviceKeyString());
            message.setSource(Message.Source.MT_MOBILE_APP.getValue());
            new MobiComConversationService(MainActivity.this).sendMessage(message, MessageIntentService.class);
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

//    public void takeOrder(View v) {
//        Intent takeOrderIntent = new Intent(this, ConversationActivity.class);
//        takeOrderIntent.putExtra(TAKE_ORDER, true);
//        takeOrderIntent.putExtra(ConversationUIService.USER_ID, Utils.getMetaDataValue(this, TAKE_ORDER_USERID_METADATA));
//        takeOrderIntent.putExtra(ConversationUIService.DEFAULT_TEXT, "Hello I am interested in your property, Can we chat?");
//        takeOrderIntent.putExtra(ConversationUIService.PRODUCT_TOPIC_ID, "Ebco Strip Light Connection Cord 4");
//        takeOrderIntent.putExtra(ConversationUIService.PRODUCT_IMAGE_URL, "https://www.applozic.com/resources/sidebox/images/applozic.png");
//        // takeOrderIntent.putExtra(ConversationUIService.APPLICATION_ID,"applozic-sample-app");
//        startActivity(takeOrderIntent);
//    }

    public void takeOrder(View v) {
        Conversation conversation = buildConversation();
        ApplozicConversationCreateTask applozicConversationCreateTask;

        ApplozicConversationCreateTask.ConversationCreateListener conversationCreateListener = new ApplozicConversationCreateTask.ConversationCreateListener() {
            @Override
            public void onSuccess(Integer conversationId, Context context) {
                Log.i(TAG, "ConversationID is:" + conversationId);
                Intent takeOrderIntent = new Intent(context, ConversationActivity.class);
                takeOrderIntent.putExtra(TAKE_ORDER, true);
                takeOrderIntent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                takeOrderIntent.putExtra(ConversationUIService.USER_ID, "usertest2");
                takeOrderIntent.putExtra(ConversationUIService.DEFAULT_TEXT, R.string.intrest_in_chat);
                takeOrderIntent.putExtra(ConversationUIService.CONVERSATION_ID,conversationId);
                startActivity(takeOrderIntent);

            }

            @Override
            public void onFailure(Exception e, Context context) {

            }
        };
        applozicConversationCreateTask = new ApplozicConversationCreateTask(MainActivity.this, conversationCreateListener, conversation);
        applozicConversationCreateTask.execute((Void) null);

    }

    private Conversation buildConversation() {

        Conversation conversation = new Conversation();
        conversation.setUserId("usertest2");
        conversation.setTopicId("Topic#Id#Test");
        TopicDetail topic = new TopicDetail();
        topic.setTitle(getResources().getString(R.string.TestTopic2));
        topic.setSubtitle(getResources().getString(R.string.Topic_1));
        topic.setLink("https://www.applozic.com/resources/sidebox/images/applozic.png");
        topic.setKey1(getResources().getString(R.string.Qty));
        topic.setValue1("1000");
        topic.setKey2(getResources().getString(R.string.Price));
        topic.setValue2(getResources().getString(R.string.rs));
        conversation.setSenderSmsFormat(MobiComUserPreference.getInstance(this).getUserId(), "SENDER SMS  FORMAT");
        conversation.setReceiverSmsFormat("usertest2", "RECEIVER SMS FORMAT");
        conversation.setTopicDetail(topic.getJson());
        return conversation;
    }

    public void initiateChatClick(View v) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        DialogFragment fragment = new InitiateDialogFragment();
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("InitiateDialogFragment");
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        fragment.show(fragmentTransaction, "InitiateDialogFragment");
    }

    public void groupChat(View v) {
        Intent groupChat = new Intent(this, ConversationActivity.class);
        groupChat.putExtra(TAKE_ORDER, true);
        groupChat.putExtra(ConversationUIService.GROUP_ID, 21276);
        groupChat.putExtra(ConversationUIService.GROUP_NAME, "sdlkfmsd:supplier2");
        startActivity(groupChat);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
           /* case 2:
                mTitle = getString(R.string.title_section2);
                break;*/
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void buildSupportContactData() {
        Context context = getApplicationContext();
        AppContactService appContactService = new AppContactService(context);
        // avoid each time update ....
        if (!appContactService.isContactExists(getString(R.string.support_contact_userId))) {
            Contact contact = new Contact();
            contact.setUserId(getString(R.string.support_contact_userId));
            contact.setFullName(getString(R.string.support_contact_display_name));
            contact.setContactNumber(getString(R.string.support_contact_number));
            contact.setImageURL(getString(R.string.support_contact_image_url));
            contact.setEmailId(getString(R.string.support_contact_emailId));
            appContactService.add(contact);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

}
