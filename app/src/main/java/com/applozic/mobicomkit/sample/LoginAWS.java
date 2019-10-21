package com.applozic.mobicomkit.sample;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


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
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.UpdateAttributesHandler;
import com.chaos.view.PinView;
import com.stfalcon.smsverifycatcher.OnSmsCatchListener;
import com.stfalcon.smsverifycatcher.SmsVerifyCatcher;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginAWS extends AppCompatActivity {

    private static final String TAG = "Cognito LoginAWS";
    private PinView verifyCodeET;
    private SmsVerifyCatcher smsVerifyCatcher;
    private Button verifyCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_aws);
       // final String   editTextUsername = "+21692071299";


        String number;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                number= null;
            } else {
                number= extras.getString("fullNumber");
            }
        } else {
            number= (String) savedInstanceState.getSerializable("fullNumber");
        }



        //   final EditText editTextPassword = findViewById(R.id.editTextPassword);
        verifyCodeET = (PinView) findViewById(R.id.pinView);
        verifyCodeButton = (Button) findViewById(R.id.buttonLogin);

        // final String verificationCode = verifyCodeET.getText().toString();
        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
            @Override
            public void onSmsCatch(String message) {
                String code = parseCode(message);//Parse verification code
                verifyCodeET.setText(code);
                //then you can send verification code to server
            }
        });

        VerifUser(number);
    }

    /**
     * Parse verification code
     *
     * @param message sms message
     * @return only four numbers from massage string
     */
    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{6}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }

    @Override
    protected void onStart() {
        super.onStart();
        smsVerifyCatcher.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        smsVerifyCatcher.onStop();
    }

    /**
     * need for Android 6 real time permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void VerifUser(final String number) {
        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));

                String verificationCode = verifyCodeET.getText().toString();
                Log.i(TAG, "verificationCode " + verificationCode + number);

                new LoginAWS.ConfirmTask().execute(String.valueOf(verificationCode)
                        , String.valueOf(number));
            }
        });

    }

    private class ConfirmTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(final String... strings) {
            final String[] result = new String[1];

            // Callback handler for confirmSignUp API
            final GenericHandler confirmationCallback = new GenericHandler() {

                @Override
                public void onSuccess() {
                    // User was successfully confirmed
                    result[0] = "Succeeded!";
                    UserAuth(strings[1]);

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

            CognitoSettings cognitoSettings = new CognitoSettings(LoginAWS.this);

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
            Intent intent = new Intent(LoginAWS.this, LoginAWS.class);
            startActivity(intent);
        }
    }


    public void UserAuth(final String number){
    //    final String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));
//       final String fullNumber = "+21692071299";

        final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(final CognitoUserSession userSession, CognitoDevice newDevice) {

                Log.i(TAG, "Login successfull, can get tokens here!");

                // add token
                final CognitoUserAttributes userAttributes = new CognitoUserAttributes();

                CognitoSettings cognitoSettings = new CognitoSettings(LoginAWS.this);

                CognitoUser user = cognitoSettings.getUserPool().getCurrentUser();
                UpdateAttributesHandler handler = new UpdateAttributesHandler() {
                    @Override
                    public void onSuccess(List<CognitoUserCodeDeliveryDetails> list) {
                        // set
                        Log.i(TAG, "Success add attribute token");
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        exception.printStackTrace();
                        // resend code failed
                        Log.e(TAG, "add attribute token"+exception.toString());
                        // set listener
                    }
                };
                userAttributes.addAttribute("token", userSession.getAccessToken().getJWTToken());
                user.updateAttributesInBackground(userAttributes, handler);

                Intent intent = new Intent(LoginAWS.this, LoginActivity.class);
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
                        , String.valueOf(number), null);

                // Pass the user sign-in credentials to the continuation
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                // Allow the sign-in to continue
                authenticationContinuation.continueTask();

            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {

                Log.i(TAG, "in getMFACode()....");

                // if Multi-factor authentication is required; get the verification code from user
                // multiFactorAuthenticationContinuation.setMfaCode(verificationCode);

                // Allow the sign-in process to continue
                // multiFactorAuthenticationContinuation.continueTask();
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

        //Button buttonLogin = findViewById(R.id.buttonLogin);
        //buttonLogin.setOnClickListener(new View.OnClickListener() {
        //   @Override
        //  public void onClick(View v) {
        CognitoSettings cognitoSettings = new CognitoSettings(LoginAWS.this);

        CognitoUser thisUser = cognitoSettings.getUserPool()
                .getUser(String.valueOf(number));
        // Sign in the user
        Log.i(TAG, "in button clicked....");

        thisUser.getSessionInBackground(authenticationHandler);
        // }
        // });
    }



}
