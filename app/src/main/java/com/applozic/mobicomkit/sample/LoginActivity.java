package com.applozic.mobicomkit.sample;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicomkit.ApplozicClient;
import com.applozic.mobicomkit.api.account.register.RegistrationResponse;
import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.account.user.PushNotificationTask;
import com.applozic.mobicomkit.api.account.user.User;
import com.applozic.mobicomkit.api.account.user.UserLoginTask;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.uiwidgets.ApplozicSetting;
import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicommons.commons.core.utils.PermissionsUtils;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.contact.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_CONTACTS = 1;
    private static String[] PERMISSIONS_CONTACT = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS};
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
    private int touchCount = 0;
    private MobiComUserPreference mobiComUserPreference;
    //private LoginButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(this);

        setContentView(R.layout.activity_login);
        setupUI(findViewById(R.id.layout));
        layout = (LinearLayout) findViewById(R.id.footerSnack);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPhoneNumberView = (EditText) findViewById(R.id.phoneNumber);
        mUserIdView = (EditText) findViewById(R.id.userId);
        mPasswordView = (EditText) findViewById(R.id.password);
        mDisplayName = (EditText) findViewById(R.id.displayName);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(User.AuthenticationType.APPLOZIC);
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Utils.toggleSoftKeyBoard(LoginActivity.this, true);
                attemptLogin(User.AuthenticationType.APPLOZIC);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        //callbackManager = CallbackManager.Factory.create();

        mSpinnerView = (Spinner) findViewById(R.id.spinner_for_url);
        mSpinnerView.setVisibility(View.INVISIBLE);
        mTitleView = (TextView) findViewById(R.id.textViewTitle);
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
    public void attemptLogin(User.AuthenticationType authenticationType) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserIdView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mDisplayName.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String phoneNumber = mPhoneNumberView.getText().toString();
        String userId = mUserIdView.getText().toString().trim();
        String password = mPasswordView.getText().toString();
        String displayName = mDisplayName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(mUserIdView.getText().toString()) || mUserIdView.getText().toString().trim().length() == 0) {
            mUserIdView.setError(getString(R.string.error_field_required));
            focusView = mUserIdView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if ((TextUtils.isEmpty(mPasswordView.getText().toString()) || mPasswordView.getText().toString().trim().length() == 0) && !isPasswordValid(mPasswordView.getText().toString())) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            /*mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;*/
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            // callback for login process
            final Activity activity = LoginActivity.this;
            UserLoginTask.TaskListener listener = new UserLoginTask.TaskListener() {

                @Override
                public void onSuccess(RegistrationResponse registrationResponse, final Context context) {
                    mAuthTask = null;
                    showProgress(false);

                    //Basic settings...

                    ApplozicClient.getInstance(context).setContextBasedChat(true).setHandleDial(true);

                    Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                    activityCallbacks.put(ApplozicSetting.RequestCode.USER_LOOUT, LoginActivity.class.getName());
                    ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);

                    //Set activity callbacks
                    /*Map<ApplozicSetting.RequestCode, String> activityCallbacks = new HashMap<ApplozicSetting.RequestCode, String>();
                    activityCallbacks.put(ApplozicSetting.RequestCode.MESSAGE_TAP, MainActivity.class.getName());
                    ApplozicSetting.getInstance(context).setActivityCallbacks(activityCallbacks);*/

                    //Start GCM registration....

                    PushNotificationTask.TaskListener pushNotificationTaskListener = new PushNotificationTask.TaskListener() {
                        @Override
                        public void onSuccess(RegistrationResponse registrationResponse) {

                        }

                        @Override
                        public void onFailure(RegistrationResponse registrationResponse, Exception exception) {

                        }
                    };
                    PushNotificationTask pushNotificationTask = new PushNotificationTask(Applozic.getInstance(context).getDeviceRegistrationId(), pushNotificationTaskListener, context);
                    pushNotificationTask.execute((Void) null);

                    buildContactData();

                    //starting main MainActivity
                    Intent mainActvity = new Intent(context, MainActivity.class);
                    startActivity(mainActvity);
                    Intent intent = new Intent(context, ConversationActivity.class);
                    if (ApplozicClient.getInstance(LoginActivity.this).isContextBasedChat()) {
                        intent.putExtra(ConversationUIService.CONTEXT_BASED_CHAT, true);
                    }
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(RegistrationResponse registrationResponse, Exception exception) {
                    mAuthTask = null;
                    showProgress(false);

                    mEmailSignInButton.setVisibility(View.VISIBLE);
                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
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

            User user = new User();
            user.setUserId(userId);
            user.setEmail(email);
            user.setPassword(password);
            user.setDisplayName(displayName);
            user.setContactNumber(phoneNumber);
            user.setAuthenticationTypeId(authenticationType.getValue());

            mAuthTask = new UserLoginTask(user, listener, this);
            mEmailSignInButton.setVisibility(View.INVISIBLE);
            mSpinnerView.setVisibility(View.INVISIBLE);
            mAuthTask.execute((Void) null);
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

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 5;
    }

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

            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

    private void requestContactsPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_CONTACTS)) {

            Snackbar.make(layout, R.string.contact_permission,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok_alert, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(LoginActivity.this, PERMISSIONS_CONTACT,
                                            REQUEST_CONTACTS);
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_CONTACT, REQUEST_CONTACTS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        if (requestCode == REQUEST_CONTACTS) {
            if (PermissionsUtils.verifyPermissions(grantResults)) {
                showSnackBar(R.string.contact_permission_granted);
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