package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class RequestHandler {
    private static String TAG = "RequestHandler";
    public static String sendPost(String r_url , JSONObject postDataParams) throws Exception {

        URL url = new URL(r_url);

        Log.i(TAG,"r_url : "+ r_url);
        Log.i(TAG,"postDataParams : "+postDataParams);



        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //conn.setReadTimeout(20000);
        //conn.setConnectTimeout(20000);
        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestMethod("GET");
        //conn.setDoInput(true);
        //conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(os, "UTF-8"));
        writer.write(encodeParams(postDataParams));
        writer.flush();
        writer.close();
        os.close();

       Log.i(TAG,"Response Code :: " + conn.getResponseCode());

        int responseCode=conn.getResponseCode(); // To Check for 200
        if (responseCode == HttpsURLConnection.HTTP_OK) {

            BufferedReader in=new BufferedReader( new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer("");
            String line="";
            while((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }

            Log.i(TAG,"response : "+sb.toString());
            return sb.toString();
        }
        return null;
    }
    public static ArrayList sendGet(String url, String postDataParams) throws Exception {
        URL obj = new URL(url+postDataParams);
        ArrayList<String> list = new ArrayList<String>();

        Log.i(TAG,"r_url : "+ obj);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        System.out.println("Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // connection ok
            BufferedReader in = new BufferedReader(new InputStreamReader( con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            Log.i(TAG,"response : "+response.toString());

            JSONObject json = new JSONObject(response.toString());
            String getRes =  json.getString("signText");

            //add result to array
            list.add(getRes);
            Log.i(TAG,"response list: "+list);


            return list;

        } else {
            return null;
        }
    }
    private static String encodeParams(JSONObject params) throws Exception {
        Log.i(TAG,"params : "+params);
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();
        while(itr.hasNext()){
            String key= itr.next();
            Object value = params.get(key);
            if (first)
                first = false;
            else
                result.append(" ");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        Log.i(TAG,"encodeParams : "+result.toString());
        return result.toString();
    }
}
