package com.applozic.mobicomkit.sample;

import android.content.Context;
import android.util.Log;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseAccess {

    private String TAG = "DynamoDb_Demo";

    /*taken from official Amazon sample app -
    see: https://github.com/awslabs/aws-sdk-android-samples*/
    /*see blogs for more examples of using Document API:
    https://aws.amazon.com/blogs/mobile/using-amazon-dynamodb-document-api-with-aws-mobile-sdk-for-android-part-1/
    and:
    https://aws.amazon.com/blogs/mobile/using-amazon-dynamodb-document-api-with-the-aws-mobile-sdk-for-android-part-2/*/

    private final String COGNITO_IDENTITY_POOL_ID = "eu-west-1:16ef245d-d292-40e2-8cce-b61dad918976";
    private final Regions COGNITO_IDENTITY_POOL_REGION = Regions.EU_WEST_1;
    private final String DYNAMODB_TABLE = "corpus_hearme";
    private Context context;
    private CognitoCachingCredentialsProvider credentialsProvider;
    private AmazonDynamoDBClient dbClient;
    private Table dbTable;

    /**
     * This class is a singleton - storage for the current instance.
     */
    private static volatile DatabaseAccess instance;


    private DatabaseAccess(Context context) {
        this.context = context;

        CognitoSettings cognitoSettingsOut = new CognitoSettings(context);
        CognitoUser currentUser = cognitoSettingsOut.getUserPool().getCurrentUser();
        // Create a new credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(context, COGNITO_IDENTITY_POOL_ID, COGNITO_IDENTITY_POOL_REGION);


        AuthenticationHandler callback = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                //Log.i(TAG, "Access token: " + userSession.getAccessToken().getJWTToken());
                //  Log.i(TAG, "ID token dynammodb: " + userSession.getIdToken().getJWTToken());
                // Log.i(TAG, "Refresh token: " + userSession.getRefreshToken());

                Map<String, String> logins = new HashMap<>();
                logins.put("cognito-idp.eu-west-1.amazonaws.com/eu-west-1_avsgY0cXS", userSession.getIdToken().getJWTToken());
                credentialsProvider.setLogins(logins);
                credentialsProvider.getCredentials();


              /*  Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(userSession.getRefreshToken());
                Log.i(TAG, "Refresh token: " + json.toString());*/
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {

            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {

            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {

            }

            @Override
            public void onFailure(Exception exception) {
                Log.i(TAG, "Failure getting tokens: " + exception.getLocalizedMessage());
            }
        };

        currentUser.getSession(callback);

        // Create a connection to the DynamoDB service
        dbClient = new AmazonDynamoDBClient(credentialsProvider);

        /*MUST SET db client REGION HERE ELSE DEFAULTS TO US_EAST_1*/
        dbClient.setRegion(Region.getRegion(Regions.EU_WEST_1));

        // Create a table reference
        dbTable = Table.loadTable(dbClient, DYNAMODB_TABLE);
    }

    /**
     * Singleton pattern - retrieve an instance of the DatabaseAccess
     */
    public static synchronized DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    /*gets all the data*/
    //
    public List<Map<String, AttributeValue>> getAllData() throws JSONException {
        List<String> results = new ArrayList<String>();
        List<Map<String, AttributeValue>> rows = new ArrayList<>();


        try {
            Map<String, String> attributeNames = new HashMap<String, String >();
            attributeNames.put("#status", "status");

            Map<String, AttributeValue> attributeValues = new HashMap<String, AttributeValue>();
            attributeValues.put(":val", new AttributeValue().withN("1"));

            ScanRequest scanRequest = new ScanRequest()

                    .withTableName(DYNAMODB_TABLE)
                    .withFilterExpression("#status = :val")
                    .withExpressionAttributeNames(attributeNames)
                    .withExpressionAttributeValues(attributeValues); //contains values for :val

            ScanResult scanResult = dbClient.scan(scanRequest);
             rows.addAll(scanResult.getItems());
            /*Log.i(TAG, "get All Result" + scanResult.getItems());

            for (int i = 0; i < rows.size(); i++){

                results.add(context.getResources().getString(R.string.spinnerTags));
                results.add(rows.get(i).get("tag").getS());
            }*/

        }catch (Exception e ){
            Log.i(TAG, "getItem " + e);

        }
       return rows;
    }

}

