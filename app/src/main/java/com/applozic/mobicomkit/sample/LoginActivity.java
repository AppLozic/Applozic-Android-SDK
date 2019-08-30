package com.applozic.mobicomkit.sample;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.listners.AlLoginHandler;
import com.applozic.mobicomkit.listners.AlPushNotificationHandler;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.contact.DeviceContactSyncService;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.ALContactProcessor;
import com.applozic.mobicommons.people.contact.Contact;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import android.widget.MediaController;

import org.json.JSONArray;
import org.json.JSONObject;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    static final String videopath = Environment.getExternalStorageDirectory() + java.io.File.separator + "HearMe/LSF/bonjour.mp4";
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_CONTACTS = 1;
    private static final int REQUEST_STORAGE = 1;

    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
    };


    LinearLayout layout;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    //flag variable for exiting the application
    private boolean exit = false;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mUserIdView;
    private EditText mPhoneNumberView;
    private EditText mPasswordView;
    private EditText mDisplayName;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    //CallbackManager callbackManager;
    private TextView mTitleView;
    private Spinner mSpinnerView;
    private Spinner sp;
    private int touchCount = 0;
    private MobiComUserPreference mobiComUserPreference;
    private boolean isDeviceContactSync = false;
    private VideoView videoView;
    private MediaController mediaController;

    //call config
    Config config  = new Config();;



    //private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(this);


        Applozic.init(this, getString(R.string.application_key));

        setContentView(R.layout.activity_login);



        FirebaseUser UserFire = FirebaseAuth.getInstance().getCurrentUser();
        //user.getPhoneNumber()

        setupUI(findViewById(R.id.layout));
        layout = (LinearLayout) findViewById(R.id.footerSnack);

        // Set up the login form.
        // mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        if (Utils.hasMarshmallow()) {
            showRunTimePermission();
        } else {
            if (isDeviceContactSync) {
                Intent intent = new Intent(this, DeviceContactSyncService.class);
                DeviceContactSyncService.enqueueWork(this, intent);
            }
        }
        mPhoneNumberView = (EditText) findViewById(R.id.phoneNumber);
        //  mUserIdView = (EditText) findViewById(R.id.userId);
        // mPasswordView = (EditText) findViewById(R.id.password);
        mDisplayName = (EditText) findViewById(R.id.displayName);
   /*    mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(User.AuthenticationType.APPLOZIC);
                    return true;
                }
                return false;
            }
        });*/



        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // callbackManager = CallbackManager.Factory.create();
        sp = (Spinner) findViewById(R.id.sp);
        mSpinnerView = (Spinner) findViewById(R.id.spinner_for_url);
        mSpinnerView.setVisibility(View.INVISIBLE);
        mTitleView = (TextView) findViewById(R.id.textViewTitle);

      /*  String test = Environment.getExternalStorageDirectory() + java.io.File.separator + "HearMe/LSF/LSF.json";
        File f = new File(test);
        Toast.makeText(LoginActivity.this, ""+test, Toast.LENGTH_SHORT).show();


        if (f.exists()) {
            // Do Whatever you want sdcard exists
            Toast.makeText(LoginActivity.this, "Sdcard Exists", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(LoginActivity.this, "Sdcard not Exists", Toast.LENGTH_SHORT).show();
        }*/
        //Connect to database firebase
        DatabaseReference   reference = FirebaseDatabase.getInstance()
                .getReferenceFromUrl(config.getUrlFireBase);

        DatabaseReference tagNameRef = reference.child("tag");
        //show me only data here status is 1
        Query query =  reference.orderByChild("status").equalTo(1);


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> List_LS = new ArrayList<String>();
                final int listsize = List_LS.size() - 1;
                // add hint as last item
                List_LS.add(getString(R.string.spinner_title));


                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        Log.d("TAG","dataBase "+ issue);
                        Log.d("TAG","Querry"+ issue.getKey());
                        String tag = issue.getKey();
                        List_LS.add(tag);

                        // List_data = issue.getValue(String.class);
                        Log.d("TAG","ListOfTags " + List_LS);

                    }

                    final Map<String, Object> List_data = (Map<String, Object>) dataSnapshot.getValue();



                    ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this,android.R.layout.simple_spinner_item,List_LS);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp.setAdapter(adapter);
                    sp.setSelection(listsize);

                    sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            final  String selectedItemText = (String) parent.getItemAtPosition(position);
                            // Notify the selected item text
                            if(!selectedItemText.equals(getString(R.string.spinner_title))) {
                             /*  Intent mainActvity = new Intent(LoginActivity.this, DownloadDictionary.class);

                                mainActvity.putExtra("selectedItem", selectedItemText);
                                Log.i("TAG", "selectedItem" + selectedItemText);

                                mainActvity.putExtra("dataObject", (Serializable) List_data.get(selectedItemText));
                               // Log.i("TAG", "NewList" + NewList_data);


                                startActivity(mainActvity);*/
                                mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

                                mEmailSignInButton.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                               /*     Intent mainActvity = new Intent(LoginActivity.this, DownloadDictionary.class);

                                    mainActvity.putExtra("selectedItem", selectedItemText);
                                    Log.i("TAG", "selectedItem" + selectedItemText);

                                    mainActvity.putExtra("dataObject", (Serializable) List_data.get(selectedItemText));
                                    // Log.i("TAG", "NewList" + NewList_data);


                                    startActivity(mainActvity);*/
                                        //Use to got to chats
                                        Utils.toggleSoftKeyBoard(LoginActivity.this, true);
                                        attemptLogin(User.AuthenticationType.APPLOZIC, selectedItemText, List_data);

                                    }
                                });


                            }else if(selectedItemText.equals(getString(R.string.spinner_title))){

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            Log.d("TAG","getSelectedItemS Nothing");


                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        // play video
      /*  String videopath = Environment.getExternalStorageDirectory() + java.io.File.separator + "HearMe/LFS/bonjour.mp4";
        videoView = (VideoView) findViewById(R.id.VideoView);
        videoView.setVideoPath(videopath);
        mediaController = new MediaController(LoginActivity.this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.start();*/
        mTitleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                touchCount += 1;
                if (touchCount == 5) {
                    mSpinnerView.setVisibility(View.VISIBLE);
                    touchCount = 0;

                } else {
                    Toast.makeText(getApplicationContext(), getBaseContext().getString(R.string.click_more) + Integer.toString(5 - touchCount), Toast.LENGTH_SHORT).show();
                }
            }
        });

        mobiComUserPreference = MobiComUserPreference.getInstance(this);
        mSpinnerView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mobiComUserPreference.setUrl(adapterView.getItemAtPosition(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }


    public void setupUI(View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    Utils.toggleSoftKeyBoard(LoginActivity.this, true);
                    return false;
                }
            });
        }
        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }


    private void populateAutoComplete() {
        if (Utils.isBetweenGingerBreadAndKitKat()) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        } else if (Utils.hasMarshmallow()) {
            showRunTimePermission();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @SuppressLint("ResourceType")
    public void attemptLogin(User.AuthenticationType authenticationType, final String selectedItemText, final Map<String, Object> List_data) {
        if (mAuthTask != null) {
            return;
        }

        // Log.i("TAG", "NewList" + NewList_data);
        // Reset errors.
        // mUserIdView.setError(null);
        // mEmailView.setError(null);
        //  mPasswordView.setError(null);
        mDisplayName.setError(null);

        // Store values at the time of the login attempt.
        // String email = mEmailView.getText().toString();
        //  String phoneNumber = mPhoneNumberView.getText().toString();
        //  String userId = user.getPhoneNumber()
        //mUserIdView.getText().toString().trim();
        //String password = mPasswordView.getText().toString();
        String displayName = mDisplayName.getText().toString();

        boolean cancel = false;
        View focusView = null;

     /*   if (TextUtils.isEmpty(mUserIdView.getText().toString()) || mUserIdView.getText().toString().trim().length() == 0) {
            mUserIdView.setError(getString(R.string.error_field_required));
            focusView = mUserIdView;
            cancel = true;
        }*/
        // Check for a valid password, if the user entered one.
       /* if ((TextUtils.isEmpty(mPasswordView.getText().toString()) || mPasswordView.getText().toString().trim().length() == 0) && !isPasswordValid(mPasswordView.getText().toString())) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }*/

        // Check for a valid email address.
    /*    if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        // Check if name empty
        if (TextUtils.isEmpty(displayName)) {
            // mDisplayName.setError(getString(R.string.error_field_required));
            //    alert.alert(  getApplicationContext(), getString(R.string.error_field_required));
            // TODO Auto-generated method stub
            // custom dialog
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.videoview);
            dialog.setTitle("titre");

            // set the custom dialog components - text, image and button
            TextView text = (TextView) dialog.findViewById(R.id.text1);
            text.setText(R.string.error_field_required);

            // dialog.setContentView(R.string.error_field_required);
            dialog.show();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            lp.copyFrom(dialog.getWindow().getAttributes());
            dialog.getWindow().setAttributes(lp);
            final VideoView videoview = (VideoView) dialog.findViewById(R.id.VideoView);
            videoview.setVideoPath(videopath);
            videoview.start();
            focusView = mDisplayName;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            FirebaseUser UserFire = FirebaseAuth.getInstance().getCurrentUser();
            Log.i(TAG, "Test is:" + UserFire.getPhoneNumber());

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            // callback for login process

            User user = new User();
            user.setUserId(UserFire.getPhoneNumber());
            //(userId);
            //  user.setEmail(email);
            // user.setPassword(password);
            user.setDisplayName(displayName);
            user.setContactNumber(UserFire.getPhoneNumber());
            //(phoneNumber);
            user.setAuthenticationTypeId(authenticationType.getValue());

            //starting DownloadDictionary
            Intent mainActvity = new Intent(LoginActivity.this, DownloadDictionary.class);
            // mainActvity.putExtra("PLAYER1NAME",List_data);
            //mainActvity.putExtra("PLAYER2NAME",selectedItemText);
            mainActvity.putExtra("selectedItem", selectedItemText);
            Log.i("TAG", "selectedItem" + selectedItemText);

            mainActvity.putExtra("dataObject", (Serializable) List_data.get(selectedItemText));

            startActivity(mainActvity);
            finish();


            Applozic.connectUser(this, user, new AlLoginHandler() {
                @Override
                public void onSuccess(RegistrationResponse registrationResponse, final Context context) {
                    // After successful registration with Applozic server the callback will come here
                    mAuthTask = null;
                    showProgress(false);
                    //Basic setting for context based chat enable...
                    Log.i("TAG", "registrationResponse"+ registrationResponse);

                    ApplozicClient.getInstance(context).setContextBasedChat(true);

                    Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                    activityCallbacks.put(ApplozicSetting.RequestCode.USER_LOOUT, LoginActivity.class.getName());
                    ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);

                   /* if (isDeviceContactSync) {
                        Intent intent = new Intent(context, DeviceContactSyncService.class);
                        DeviceContactSyncService.enqueueWork(context, intent);
                    }*/

                   // buildContactData();

                    ApplozicClient.getInstance(context).enableDeviceContactSync(isDeviceContactSync);


                    //Start FCM registration....

                    Applozic.registerForPushNotification(context, Applozic.getInstance(context).getDeviceRegistrationId(), new AlPushNotificationHandler() {
                        @Override
                        public void onSuccess(RegistrationResponse registrationResponse) {

                        }

                        @Override
                        public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                        }
                    });

                    //starting main MainActivity
                 /*   Intent mainActvity = new Intent(context, MainActivity.class);
                    startActivity(mainActvity);
                    Intent intent = new Intent(context, ConversationActivity.class);
                    if (ApplozicClient.getInstance(LoginActivity.this).isContextBasedChat()) {
                        intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                    }
                    startActivity(intent);
                    finish();*/


                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                    // If any failure in registration the callback  will come here
                    mAuthTask = null;
                    showProgress(false);

                    // mEmailSignInButton.setVisibility(View.VISIBLE);
                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                    alertDialog.setTitle(getString(R.string.text_alert));
                    alertDialog.setMessage(exception == null ? registrationResponse.getMessage() : exception.getMessage());
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
            });

            //      mEmailSignInButton.setVisibility(View.INVISIBLE);
            mSpinnerView.setVisibility(View.INVISIBLE);


        }
    }
    /**
     * Don't use this method...this is only for demo purpose..
     */
    private void buildContactData() {

        Context context = getApplicationContext();
        AppContactService appContactService = new AppContactService(context);
        // avoid each time update ....
        if (!appContactService.isContactExists("adarshk")) {

            List<Contact> contactList = new ArrayList<Contact>();
            //Adarsh....
            Contact contact = new Contact();
            contact.setUserId("adarshk");
            contact.setFullName("John");
            contact.setImageURL("R.drawable.couple");
            contactList.add(contact);

            Contact contactRaj = new Contact();
            contactRaj.setUserId("raj");
            contactRaj.setFullName("rajni");
            contactRaj.setImageURL("https://fbcdn-profile-a.akamaihd.net/hprofile-ak-xap1/v/t1.0-1/p200x200/12049601_556630871166455_1647160929759032778_n.jpg?oh=7ab819fc614f202e144cecaad0eb696b&oe=56EBA555&__gda__=1457202000_85552414c5142830db00c1571cc50641");
            contactList.add(contactRaj);


            //Adarsh
            Contact contact2 = new Contact();
            contact2.setUserId("rathan");
            contact2.setFullName("Liz");
            contact2.setImageURL("R.drawable.liz");
            contactList.add(contact2);

            Contact contact3 = new Contact();
            contact3.setUserId("clem");
            contact3.setFullName("Clement");
            contact3.setImageURL("R.drawable.shivam");
            contactList.add(contact3);

            Contact contact4 = new Contact();
            contact4.setUserId("shanki.gupta");
            contact4.setFullName("Bill");
            contact4.setImageURL("R.drawable.contact_shanki");
            contactList.add(contact4);

            Contact contact6 = new Contact();
            contact6.setUserId("krishna");
            contact6.setFullName("Krishi");
            contact6.setImageURL("R.drawable.girl");
            contactList.add(contact6);

            Contact contact7 = new Contact();
            contact7.setUserId("heather");
            contact7.setFullName("Heather");
            contact7.setImageURL("R.drawable.heather");
            contactList.add(contact7);

            appContactService.addAll(contactList);
        }
    }


    private boolean isEmailValid(String email) {

        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

   /* private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }*/

    @Override
    public void onBackPressed() {

        if (exit) {
            finish();
        } else {
            Toast.makeText(this, this.getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();
            exit = true;

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);
        }

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void showRunTimePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestContactsPermissions();

        } else {
            if (isDeviceContactSync) {
                Intent intent = new Intent(this, DeviceContactSyncService.class);
                DeviceContactSyncService.enqueueWork(this, intent);
            }
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }



    private void requestContactsPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CONTACTS)) {

            Snackbar.make(layout, R.string.contact_permission,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok_alert, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS,
                                    PERMISSION_ALL);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == PERMISSION_ALL) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.contact_permission_granted);

                if (isDeviceContactSync) {
                    Intent intent = new Intent(this, DeviceContactSyncService.class);
                    DeviceContactSyncService.enqueueWork(this, intent);
                }

                new SetupEmailAutoCompleteTask().execute(null, null);

            } else {
                showSnackBar(R.string.contact_permission_granted);
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showSnackBar(int resId) {
        Snackbar.make(layout, resId,
                Snackbar.LENGTH_SHORT)
                .show();
    }


    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<String>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

}
