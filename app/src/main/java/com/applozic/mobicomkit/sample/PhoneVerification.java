package com.applozic.mobicomkit.sample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler;
import com.applozic.mobicomkit.Applozic;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.chaos.view.PinView;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.amazonaws.services.cognitoidentityprovider.model.AttributeDataType.DateTime;

public class PhoneVerification extends AppCompatActivity {
    TextView tvIsValidPhone;
    EditText edtPhone;
    Button btnValidate;

    LinearLayout layout;


    LinearLayout layout1, layout2;
    private String phoneNumber;
    private Button sendCodeButton;
    private Button verifyCodeButton;
    private Button button3;

    private EditText phoneNum;
    private TextView phonenumberText, title;

    private String mVerificationId;
    private CountryCodePicker ccp;
    private PinView verifyCodeET;
    private int totalAttemptsSend;


    private static final String TAG = "Cognito PhoneVerif";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        layout1 = (LinearLayout) findViewById(R.id.layout1);
        layout2 = (LinearLayout) findViewById(R.id.layout2);
        sendCodeButton = (Button) findViewById(R.id.submit1);
        verifyCodeButton = (Button) findViewById(R.id.submit2);
        phonenumberText = (TextView) findViewById(R.id.phonenumberText);
        tvIsValidPhone = (TextView) findViewById(R.id.tvIsValidPhone);
        phoneNum = (EditText) findViewById(R.id.phonenumber);
        title = (TextView) findViewById(R.id.title);
        //edtCountryCode = (EditText) findViewById(R.id.edtCountryCode);
        layout1.setVisibility(View.VISIBLE);
        verifyCodeET = (PinView) findViewById(R.id.pinView);

        ccp = (CountryCodePicker) findViewById(R.id.ccp);
        ccp.setAutoDetectedCountry(true);
        title.setText(R.string.titleSignIn);

        registerUser(); // Validate number after register
        VerifUser();
        //UserAuth();
       /* try {
            //storeAttempsSend();
            readAttemps("dateAttemps.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private long calculateDifferenceTime() {
        long resTime = 0;
        try {
            Date currentTime = new Date(System.currentTimeMillis());
            Date previousTime = new Date(readAttemps("dateAttemps.txt"));
            resTime = currentTime.getTime() - previousTime.getTime();

            Log.i("TAG", "calculateDifferenceTime" + resTime);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resTime;
    }

    // store date max 24h => 86400000 milliseconds
    private String storeDateAttemps() throws IOException {
        Date date = new Date();
        // Time in Milliseconds
        String fileName = "dateAttemps.txt";

        FileOutputStream outputStream;
        Date currentTime = new Date(System.currentTimeMillis());

        try {
            String textToWrite = String.valueOf(calculateDifferenceTime());
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(textToWrite.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readAttemps(fileName);
    }

    // store attemps send
    private String storeAttempsSend() throws IOException {

        String fileName = "sendAttemps.txt";
        totalAttemptsSend++;
        String textToWrite = String.valueOf(totalAttemptsSend);
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(textToWrite.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return readAttemps(fileName);


    }

    private String readAttemps(String name) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new
                File(getFilesDir() + File.separator + name)));

        String read;
        StringBuilder builder = new StringBuilder("");

        while ((read = bufferedReader.readLine()) != null) {
            builder.append(read);
        }
        Log.d(TAG, "Output" + builder.toString());
        bufferedReader.close();
        return builder.toString();

    }

    private boolean validateNumber(String countryCode, String phNumber) {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            //phoneNumber = phoneNumberUtil.parse(phNumber, "IN");  //if you want to pass region code
            phoneNumber = phoneNumberUtil.parse(phNumber, isoCode);
        } catch (NumberParseException e) {
            System.err.println(e);
        }

        return phoneNumberUtil.isValidNumber(phoneNumber);

    }

    private void registerUser() {
        // get full number
        // Create a CognitoUserAttributes object and add user attributes
        final CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        final SignUpHandler signupCallback = new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser user, boolean signUpConfirmationState
                    , CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                final String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));
                // Sign-up was successful
                Log.i(TAG, "sign up success...is confirmed: " + signUpConfirmationState);
                // Check if this user (cognitoUser) needs to be confirmed
                if (!signUpConfirmationState) {
                    Log.i(TAG, "sign up success...not confirmed, verification code sent to: "
                            + cognitoUserCodeDeliveryDetails.getDestination() + cognitoUserCodeDeliveryDetails.getDeliveryMedium());

                    phonenumberText.setText(cognitoUserCodeDeliveryDetails.getDestination());

                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.VISIBLE);
                    // go to page verification code and send full number as params
                   /* Intent intent = new Intent(PhoneVerification.this, LoginAWS.class);
                    Log.i(TAG,fullNumber);
                    intent.putExtra("fullNumber",fullNumber);
                    startActivity(intent);*/


                } else if (signUpConfirmationState) {
                    // The user has already been confirmed
                    Log.i(TAG, "sign up success...confirmed");
                    //resendCode();
                    //layout1.setVisibility(View.GONE);
                    //layout2.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Exception exception) {
                // if username exist show interface verif code
                if (exception.getMessage().contains("UsernameExistsException")) {
                    Log.i(TAG, "UsernameExistsException...");
                    resendCode();
                }
                // Sign-up failed, check exception for the cause
                Log.i(TAG, "sign up failure: " + exception.getLocalizedMessage());

            }
        };

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View focusView = null;

                if (TextUtils.isEmpty(phoneNum.getText().toString())) {
                    phoneNum.setError(getString(R.string.error_field_required));
                    focusView = phoneNum;
                } else {
                    Boolean isValid = validateNumber(ccp.getFullNumberWithPlus(), String.valueOf(phoneNum.getText()));

                    if (isValid) {
                       /* try {
                            if (storeDateAttemps()) {
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }*/
                        //userAttributes.addAttribute("given_name", ccp+phoneNumber);
                        String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));
                        // add attributes
                        userAttributes.addAttribute("phone_number", fullNumber);
                        //userAttributes.addAttribute("token", "");


                        CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);
                        Log.i(TAG, "phoneNumber" + fullNumber);
                        cognitoSettings.getUserPool().signUpInBackground(fullNumber, fullNumber, userAttributes
                                , null, signupCallback);
                    } else {
                        focusView = phoneNum;
                        phoneNum.setError(getString(R.string.error_phone_invalid));
                    }
                }
            }
        });
    }

    private void VerifUser() {
        verifyCodeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final String fullNumber = ccp.getFullNumberWithPlus() + phoneNum.getText().toString();

                String verificationCode = verifyCodeET.getText().toString();
                Log.i(TAG, "verificationCode " + verificationCode + fullNumber);

                new ConfirmTask().execute(String.valueOf(verificationCode)
                        , String.valueOf(fullNumber));
            }
        });

    }

    private class ConfirmTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            final String[] result = new String[1];

            // Callback handler for confirmSignUp API
            final GenericHandler confirmationCallback = new GenericHandler() {

                @Override
                public void onSuccess() {
                    // User was successfully confirmed
                    result[0] = "Succeeded!";
                    //layout1.setVisibility(View.GONE);
                    //layout2.setVisibility(View.GONE);
                    UserAuth();
                }

                @Override
                public void onFailure(Exception exception) {
                    // User confirmation failed. Check exception for the cause.
                    if (exception.getMessage().contains("CONFIRMED")) {

                        //Intent intent = new Intent(PhoneVerification.this, LoginActivity.class);
                        //startActivity(intent);
                    }
                    result[0] = "Failed: " + exception.getMessage();
                }
            };

            CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);

            CognitoUser thisUser = cognitoSettings.getUserPool().getUser(strings[1]);
            // This will cause confirmation to fail if the user attribute (alias) has been verified
            // for another user in the same pool
            thisUser.confirmSignUp(strings[0], false, confirmationCallback);

            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i(TAG, "Confirmation result: " + result);
            if (result.contains("Failed:")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PhoneVerification.this);
                builder.setTitle(R.string.text_alert);
                builder.setMessage(R.string.confirm_error);
                AlertDialog alert = builder.create();
                alert.show();
            } else {

            }
            /*Intent intent = new Intent(PhoneVerification.this, LoginAWS.class);
            startActivity(intent);*/
        }
    }

    public void resendCode() {
        final String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));

        CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);
        CognitoUser thisUser = cognitoSettings.getUserPool()
                .getUser(String.valueOf(fullNumber));

        new ResendConfirmationCodeAsync().execute(thisUser);

    }

    public void onClickResend(View v) {
        final String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));

        CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);
        CognitoUser thisUser = cognitoSettings.getUserPool()
                .getUser(String.valueOf(fullNumber));

        new ResendConfirmationCodeAsyncTask().execute(thisUser);
    }

    private class ResendConfirmationCodeAsyncTask extends AsyncTask<CognitoUser, Void, String> {

        @Override
        protected String doInBackground(CognitoUser... cognitoUsers) {

            final String[] result = new String[1];

            VerificationHandler resendConfCodeHandler = new VerificationHandler() {
                @Override
                public void onSuccess(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                    result[0] = "Confirmation code was successfully sent to: "
                            + cognitoUserCodeDeliveryDetails.getDestination();
                }

                @Override
                public void onFailure(Exception exception) {
                    result[0] = exception.getLocalizedMessage();
                }
            };
            //Request to resend registration confirmation code for a user, in current thread.//
            cognitoUsers[0].resendConfirmationCode(resendConfCodeHandler);

            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i(TAG, "Resend verification result: " + result);
            AlertDialog.Builder builder = new AlertDialog.Builder(PhoneVerification.this);
            builder.setTitle(R.string.text_alert);
            builder.setMessage(R.string.resend_msg);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private class ResendConfirmationCodeAsync extends AsyncTask<CognitoUser, Void, String> {

        @Override
        protected String doInBackground(CognitoUser... cognitoUsers) {

            final String[] result = new String[1];

            VerificationHandler resendConfCodeHandler = new VerificationHandler() {
                @Override
                public void onSuccess(CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                    result[0] = "Confirmation code was successfully sent to: "
                            + cognitoUserCodeDeliveryDetails.getDestination();
                }

                @Override
                public void onFailure(Exception exception) {
                    result[0] = exception.getLocalizedMessage();
                }
            };
            //Request to resend registration confirmation code for a user, in current thread.//
            cognitoUsers[0].resendConfirmationCode(resendConfCodeHandler);

            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i(TAG, "Resend verification result: " + result);
            if (result.contains("confirmed")) {
                UserAuth();
            } else {
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);
            }
        }
    }

    //

    public void UserAuth() {
//       final String fullNumber = "+21692071299";
        final String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));
        final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {

                Log.i(TAG, "Login successfull, can get tokens here!");

                Intent intent = new Intent(PhoneVerification.this, LoginActivity.class);
                                intent.putExtra("phone",fullNumber);
                                startActivity(intent);


                /*userSession contains the tokens*/

               /* Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(userSession);
                Log.i(TAG, "user session: "+json);*/
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation
                    , String userId) {

                Log.i(TAG, "in getAuthenticationDetails()....");

                /*need to get the userId & password to continue*/
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId
                        , String.valueOf(fullNumber), null);

                // Pass the user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                // Allow the sign-in to continue
                authenticationContinuation.continueTask();

            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

                Log.i(TAG, "in getMFACode()....");

                // if Multi-factor authentication is required; get the verification code from user
//                multiFactorAuthenticationContinuation.setMfaCode(mfaVerificationCode);

                // Allow the sign-in process to continue
//                multiFactorAuthenticationContinuation.continueTask();
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                Log.i(TAG, "in authenticationChallenge()....");
            }

            @Override
            public void onFailure(Exception exception) {
                Log.i(TAG, "Login failed: " + exception.getLocalizedMessage());
            }
        };

                CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);

                CognitoUser thisUser = cognitoSettings.getUserPool()
                        .getUser(String.valueOf(fullNumber));
                // Sign in the user
                Log.i(TAG, "in button clicked....");

                thisUser.getSessionInBackground(authenticationHandler);


    }

}
