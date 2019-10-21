package com.applozic.mobicomkit.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.AsyncTask;

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
import com.chaos.view.PinView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;

import java.util.List;

public class PhoneVerification extends AppCompatActivity {
    TextView tvIsValidPhone;
    EditText edtPhone;
    Button btnValidate;

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

        //registerUser();
       UserAuth();
    }


    private void registerUser() {
        // get full number
        // Create a CognitoUserAttributes object and add user attributes
        final CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        final SignUpHandler signupCallback = new SignUpHandler() {
            @Override
            public void onSuccess(CognitoUser user, boolean signUpConfirmationState
                    , CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
                final String fullNumber = ccp.getFullNumberWithPlus()+(String.valueOf(phoneNum.getText()));
                // Sign-up was successful
                Log.i(TAG, "sign up success...is confirmed: " + signUpConfirmationState);
                // Check if this user (cognitoUser) needs to be confirmed
                if (!signUpConfirmationState) {
                    Log.i(TAG, "sign up success...not confirmed, verification code sent to: "
                            + cognitoUserCodeDeliveryDetails.getDestination() + cognitoUserCodeDeliveryDetails.getDeliveryMedium());

                    phonenumberText.setText(cognitoUserCodeDeliveryDetails.getDestination());

                    layout1.setVisibility(View.GONE);
                    //layout2.setVisibility(View.VISIBLE);
                    // go to page verification code and send full number as params
                    Intent intent = new Intent(PhoneVerification.this, LoginAWS.class);
                    Log.i(TAG,fullNumber);
                    intent.putExtra("fullNumber",fullNumber);
                    startActivity(intent);




                } else
                //if (signUpConfirmationState)
                {
                    // The user has already been confirmed
                    Log.i(TAG, "sign up success...confirmed");

                   /* final String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));

                    CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);
                    CognitoUser thisUser = cognitoSettings.getUserPool()
                            .getUser(String.valueOf(fullNumber));
                    new ResendConfirmationCodeAsyncTask().execute(thisUser);*/
                }
            }

            @Override
            public void onFailure(Exception exception) {
                // if username exist show interface verif code
                if (exception.getMessage().contains("UsernameExistsException")) {
                    Log.i(TAG, "UsernameExistsException...");
                  /*  final String fullNumber = ccp.getFullNumberWithPlus()+(String.valueOf(phoneNum.getText()));

                    CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);
                    CognitoUser thisUser = cognitoSettings.getUserPool()
                            .getUser(String.valueOf(fullNumber));

                    new ResendConfirmationCodeAsyncTask().execute(thisUser);*/
                   // layout1.setVisibility(View.GONE);
                   // layout2.setVisibility(View.VISIBLE);
                }
                // Sign-up failed, check exception for the cause
                Log.i(TAG, "sign up failure: " + exception.getLocalizedMessage());

            }
        };

        sendCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //userAttributes.addAttribute("given_name", ccp+phoneNumber);
                String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));
                // add attributes
                userAttributes.addAttribute("phone_number", fullNumber);
                //userAttributes.addAttribute("token", "");


                CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);
                Log.i("TAG", "phoneNumber" + fullNumber);
                cognitoSettings.getUserPool().signUpInBackground(fullNumber, fullNumber, userAttributes
                        , null, signupCallback);
            }
        });
    }

  /*  private void VerifUser() {
        verifyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));

                String verificationCode = verifyCodeET.getText().toString();
                Log.i(TAG, "verificationCode " + verificationCode + fullNumber);

                new ConfirmTask().execute(String.valueOf(verificationCode)
                        , String.valueOf(fullNumber));
            }
        });

    }*/

   /* private class ConfirmTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            final String[] result = new String[1];

            // Callback handler for confirmSignUp API
            final GenericHandler confirmationCallback = new GenericHandler() {

                @Override
                public void onSuccess() {
                    // User was successfully confirmed
                    result[0] = "Succeeded!";
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
            Intent intent = new Intent(PhoneVerification.this, LoginAWS.class);
            startActivity(intent);
        }
    }*/


   /* public void onClickResend(View v) {
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
        }
    }*/

   // Just for test => will be deleted after update
   public void UserAuth(){
      // final String fullNumber = ccp.getFullNumberWithPlus() + (String.valueOf(phoneNum.getText()));
      final String fullNumber = "+21692071299";

       final AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
           @Override
           public void onSuccess(final CognitoUserSession userSession, CognitoDevice newDevice) {

               Log.i(TAG, "Login successfull, can get tokens here!");

               // add token
               final CognitoUserAttributes userAttributes = new CognitoUserAttributes();

               CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);

               CognitoUser user = cognitoSettings.getUserPool().getCurrentUser();
               Log.i(TAG, "Success add attribute token");

             /*  CognitoUser thisUser = cognitoSettings.getUserPool()
                       .getUser(String.valueOf(fullNumber));*/

             /*  UpdateAttributesHandler handler = new UpdateAttributesHandler() {
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
               userAttributes.addAttribute("custom:Accesstoken", userSession.getAccessToken().getJWTToken());
               Log.i(TAG, "Success add attribute1 "+ userSession.getAccessToken());
               Log.i(TAG, "Success add attribute2 "+ userSession.getAccessToken().getJWTToken());


               // userAttributes.addAttribute("custom:Refreshtoken", userSession.getAccessToken().getJWTToken());

               user.updateAttributesInBackground(userAttributes, handler);*/

               Intent intent = new Intent(PhoneVerification.this, LoginActivity.class);
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
               CognitoSettings cognitoSettings = new CognitoSettings(PhoneVerification.this);

               CognitoUser thisUser = cognitoSettings.getUserPool()
                       .getUser(String.valueOf(fullNumber));
            // Sign in the user
               Log.i(TAG, "in button clicked....");

               thisUser.getSessionInBackground(authenticationHandler);
          // }
      // });
   }


}
