package com.applozic.mobicomkit.sample;


import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.regions.Regions;

public class CognitoSettings {

    public static String userPoolId = "eu-west-1_avsgY0cXS";
    private String clientId = "59bs6tj70jicrtgh0th4hohepe";
    private String clientSecret = "bki5v4jhmih0s62vc17l6k7bc7ks15iofjt254ff179fjjvsjkr";
    private Regions cognitoRegion = Regions.EU_WEST_1;

    private String identityPoolId = "eu-west-1:16ef245d-d292-40e2-8cce-b61dad918976";

    private Context context;



    public CognitoSettings(Context context) {
        this.context = context;
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Regions getCognitoRegion() {
        return cognitoRegion;
    }

    /*the entry point for all interactions with your user pool from your application*/
    public CognitoUserPool getUserPool() {
        return new CognitoUserPool(context, userPoolId, clientId
                , clientSecret, cognitoRegion);
    }

    public CognitoCachingCredentialsProvider getCredentialsProvider() {
        return new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                identityPoolId, // Identity pool ID
                cognitoRegion// Region;
        );
    }

}
