package com.applozic.mobicomkit.sample;

import android.content.ClipData;
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
import com.amazonaws.mobileconnectors.dynamodbv2.document.ScanOperationConfig;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Search;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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

    /*gets a contact for given primary key*/
    public Document getItem() {
        Document result = dbTable.getItem(new Primitive("LSF"));
        Log.i(TAG,"getItem "+ result);
        return result;
    }

    /*gets all the data*/
    public List<Document> getAllData() throws JSONException {
        /*using scan to get all data*/
        ScanOperationConfig scanConfig = new ScanOperationConfig();
        List<String> attributeList = new ArrayList<>();

        attributeList.add("LSF");

       scanConfig.withAttributesToGet(attributeList);
       Search searchResult = dbTable.scan(scanConfig);

        Log.i(TAG,"getAllData "+searchResult.getAllResults());
        String json = new Gson().toJson(searchResult.getAllResults());

        JSONArray jsonObj = new JSONArray(json);
        HashMap<String, Object> myMap = new Gson().fromJson(jsonObj.toString(), HashMap.class);
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonObj);


        // Log.i(TAG, "getAllData "+getJson(myMap));



        return searchResult.getAllResults();
    }

    //passing the reponse.getItems()
    public static Object getJson(List<Map<String,AttributeValue>> mapList) {
        List<Object> finalJson= new ArrayList();
        for(Map<String,AttributeValue> eachEntry : mapList) {
            finalJson.add(mapToJson(eachEntry));
        }
        return finalJson;
    }


    public static Map<String,Object> mapToJson(Map<String,AttributeValue> keyValueMap){
        Map<String,Object> finalKeyValueMap = new HashMap();
        for(Map.Entry<String, AttributeValue> entry : keyValueMap.entrySet())
        {
            if(entry.getValue().getM() == null) {
                finalKeyValueMap.put(entry.getKey(),entry.getValue().getS());
            }
            else {
                finalKeyValueMap.put(entry.getKey(),mapToJson(entry.getValue().getM()));
            }
        }
        return finalKeyValueMap;
    }

    public static Map<String,Object> mapToJsonc(Map map){
        Map<String,Object> finalKeyValueMap = new HashMap();
        Iterator it = map.entrySet().iterator();

        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            Map obj1 = (Map) pair.getValue();
            if(obj1.get("M") == null) {
                if(obj1.get("N") != null)
                    finalKeyValueMap.put(pair.getKey().toString(),obj1.get("N"));
                else if(obj1.get("S") != null)
                    finalKeyValueMap.put(pair.getKey().toString(),obj1.get("S"));
            }
            else {
                Map obj2 = (Map) pair.getValue();
                Map obj3 = (Map) obj2.get("M");
                finalKeyValueMap.put(pair.getKey().toString(),mapToJson(obj3));
            }

        }
        Log.i("DynamoDb_Demo", "getAllData "+finalKeyValueMap.toString());

        System.out.println(finalKeyValueMap.toString());
        return finalKeyValueMap;
    }


    /*for getting unknown data types*/
    private void getUnknowDataTypes(Document doc) {

        Document retrievedDoc = doc.get("LSF").asDocument();

        Log.i(TAG, "in getUnknowDataTypes()...");
        try {
            String jsonString = Document.toJson(retrievedDoc);
            /*convert to json object*/
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();

            /*use json to get attribute value*/
            String carColor = jsonObject.get("LSF").getAsString();
            Log.i(TAG, "Car color: " + carColor);

            /*convert to pretty print format*/
            Gson gsonBuild = new GsonBuilder().setPrettyPrinting().create();
            String jsonBuild = gsonBuild.toJson(jsonObject);
            Log.i(TAG, "Car map: " + jsonBuild);

        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "error converting map to json: " + e.getLocalizedMessage());
        }
    }





}

