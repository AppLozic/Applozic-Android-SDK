package com.applozic.mobicomkit.sample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;
import com.applozic.mobicomkit.uiwidgets.conversation.fragment.DatabaseHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import android.os.AsyncTask;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.util.IOUtils;

/**
 * Created by Nozha GH
 */
public class DownloadDictionary extends AppCompatActivity {

    static final Integer READ_STORAGE_PERMISSION_REQUEST_CODE = 0x3;
    //call videoPath
    Config config = new Config();

    Context context = this;
    public static final String ACTION_MANAGE_STORAGE = "android.os.storage.action.MANAGE_STORAGE";
    //Download File
    ProgressBar pb;
    Dialog dialog;
    int downloadedSize = 0;
    int totalSize = 0;
    TextView cur_val;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    StorageReference ref;
    private ProgressBar progressBar;
    private VideoView videoView;
    private static TextView details;
    private TextView ProgressPerc;
    private AmazonS3Client s3Client;
    private BasicAWSCredentials credentials;

    final DatabaseHelper helper = new DatabaseHelper(this);
    private static final String TAG = "Cognito";
    private CognitoCachingCredentialsProvider credentialsProvider;
    private Map<String, AttributeValue> result;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_dictionary);

        AWSMobileClient.getInstance().initialize(this).execute();


        videoView = (VideoView) findViewById(R.id.VideoView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar_storage);


        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        Map<String, AttributeValue> data = (HashMap<String, AttributeValue>) intent.getSerializableExtra("tag");

        result = data;
        Log.i(TAG, "dataTags" + data);
        //dataTags{tag={S: LSF,}, size={N: 0,}, status={N: 1,}, hash={S: [B@aaaa50,}, id={N: 0,}, length={N: 0,}}
        // check if space device available
        CheckFreeMemory(data);
        startProgress();
    }

    // Calculate available espace storage
    void CheckFreeMemory(Map<String, AttributeValue> data) {
        //details.setText("Vérification de l'espace");
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } else {
            bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        }
        long megAvailable = bytesAvailable;

        Log.i("TAG", "AvailableMB : " + megAvailable);
        Log.i("TAG", "AvailableMB2 : " + bytesAvailable);
        // test
        if (Long.parseLong(data.get("size").getN()) <= megAvailable) {
            //if space exists then download
            proceed(data.get("tag").getS() + ".json");
        } else {
            //details.setText("Oups ! espace requise non disponible");

            stopProgress();
            AlertDialog alertDialog = new AlertDialog.Builder(DownloadDictionary.this, R.style.AlertDialogStyle).create();
            alertDialog.setTitle(getString(R.string.text_alert));
            alertDialog.setMessage(getString(R.string.checkSpace));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok_alert),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    public void startProgress() {
        progressBar.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.intro));
        videoView.start();

    }

    public void stopProgress() {
        progressBar.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);

    }

    //    ReadFile(new File(config.videopath + fileName + "/" + fileName + ".json"), fileName, size);
    // Parsing json and store in DB
    public void ReadFile(String file) {
        try {
            //  final File myFile = new File(getExternalFilesDir(config.videopath),file);
            final File myFile = new File(getExternalFilesDir(config.videopath) + "/" + result.get("tag").getS() + "/", file);


            Log.i(TAG, "myFile ReadFile" + myFile);
            FileInputStream stream = new FileInputStream(myFile);
            String jsonStr = null;
            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
            JSONArray jsonArry = new JSONArray(jsonStr);
            Log.i(TAG, "***jsonObjjjjj***  " + jsonArry + jsonArry.length());
            //[{"name":"chiffre","category":"List"},{"name":"zero","category":"chiffre"},{"name":"1","category":"chiffre"},{"name":"2","category":"chiffre"},{"name":"3","category":"chiffre"}]
            for (int i = 0; i < jsonArry.length(); i++) {
                // for (int j = 0; j < jsonArry.getJSONObject(i).length(); j++) {

                JSONObject e = jsonArry.getJSONObject(i);
                Log.i(TAG, "jsonArry.getJSONObject(i)" + jsonArry.getJSONObject(i));
                Log.i(TAG, "ejsonArry " + e + jsonArry.getJSONObject(i).length());
                //{"name":"chiffre","category":"List"}
                //call download video
                proceed("video/" + e.getString("name") + ".mp4");

                helper.insert(e.getString("name"), e.getString("category"), 1);
                Log.i(TAG, "MyInsert " + e.getString("name") + e.getString("category"));
                //}
            }
            Log.i(TAG, "test"+ "hello");

            //get folder size
            /*long FolderSize = getFileFolderSize(new File(config.videopath));

            String size = result.get("size").getN();

            Log.i("TAG", "FolderSize : " + FolderSize+ " size in database : "+size);

            // check if size in Internal storage equal to size in firebase
            if (FolderSize >= Long.parseLong(size)) {
                //Insert size in DB
                helper.insertInConfig(Long.parseLong(size), FolderSize);
                //go intent conversation
                goConversation();
            }else{
                //redowload
                CheckFreeMemory(result);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TAG", "***Exception***  " + e);

        }
    }

    //call encdoe
   /* public byte[] callEncode(final String name) {

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    encodeByte(name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        return new byte[0];
    }*/

    //encode videos to  bytes
  /*  private byte[] encodeByte(String name) {

        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();

            InputStream is = ucon.getInputStream();


            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            //We create an array of bytes
            byte[] data = new byte[50];
            int current = 0;

            while ((current = bis.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, current);
            }

            Log.d("TAG", "ByteArray: " + buffer.toByteArray());
            // get function decode
            decodeByte(buffer.toByteArray(), name, tag, size);


            return buffer.toByteArray();
        } catch (Exception e) {
            Log.d("TAG", "ErrorByteArray: " + e.toString());
        }
        return null;
    }*/

    // decode bytes to videos
 /*   public FileOutputStream decodeByte(byte[] data, String name, String tag, String size) throws IOException {
        Log.i("TAG", "sizeeee" + size);
        byte[] decodedBytes = data;

        FileOutputStream out = null;

        boolean closed = false;

        try {
            File myFile = new File(getExternalFilesDir(config.videopath), "");

            out = new FileOutputStream(
                    myFile
                            + "/" + tag + "/" + name + ".mp4");
            Log.d("TAG", "FilePath: " + out);

            Log.d("TAG", "decodedBytes: " + decodedBytes);

            out.write(decodedBytes);
            out.close();

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("ErrorDec", e.toString());

        } finally {
            if (out != null)
                out.flush();
        }


        //get folder size
        long FolderSize = getFileFolderSize(new File(config.videopath + tag + "/"));
        Log.i("TAG", "FolderSize : " + FolderSize);

        // check if size in Internal storage equal to size in firebase
        if (FolderSize == Long.parseLong(size)) {
            //Insert size in DB
            helper.insertInConfig(Long.parseLong(size), FolderSize);
            //go intent conversation
            goConversation();
        }


        return out;

    }*/
    // get size folder in internal storage
    public long getFileFolderSize(File dir) {
        File myFile = new File(getExternalFilesDir(String.valueOf(dir)), "");
        Log.i(TAG, "getFileFolderSize" + myFile);
        long size = 0;
        if (myFile.isDirectory()) {
            for (File file : myFile.listFiles()) {
                //if (file.isFile()) {
                size += file.length();
                // }
            }
        }
        return size * 1000;
    }

    public void goConversation() {

        Intent intent = new Intent(DownloadDictionary.this, ConversationActivity.class);
        DownloadDictionary.this.startActivity(intent);
       // DownloadDictionary.this.finish();
    }

    private void proceed(final String file) {
        final CognitoSettings cognitoSettings = new CognitoSettings(this);

        /*Identity pool credentials provider*/
        Log.i(TAG, "getting Identity Pool credentials provider");
        credentialsProvider = cognitoSettings.getCredentialsProvider();

        /*get user - User Pool*/
        Log.i(TAG, "getting user Pool user");
        CognitoUser currentUser = cognitoSettings.getUserPool().getCurrentUser();

        /*get token for logged in user - user pool*/
        Log.i(TAG, "calling getSessionInBackground....");
        currentUser.getSessionInBackground(new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {

                if (userSession.isValid()) {
                    Log.i(TAG, "user session valid, getting token...");
                    // Get id token from CognitoUserSession.
                    String idToken = userSession.getIdToken().getJWTToken();

                    if (idToken.length() > 0) {
                        // Set up as a credentials provider.
                        Log.i(TAG, "got id token - setting credentials using token");
                        Map<String, String> logins = new HashMap<>();
                        logins.put("cognito-idp.eu-west-1.amazonaws.com/eu-west-1_avsgY0cXS", idToken);
                        credentialsProvider.setLogins(logins);

                        Log.i(TAG, "using credentials for the logged in user");

                        /*refresh provider off main thread*/
                        Log.i(TAG, "refreshing credentials provider in asynctask..");
                        new RefreshAsyncTask().execute(file);

                    } else {
                        Log.i(TAG, "no token...");
                    }
                } else {
                    Log.i(TAG, "user session not valid - using identity pool credentials - guest user");
                }

                //performAction(action);
                downloadWithTransferUtility(file);

            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                Log.i(TAG, " Not logged in! using identity pool credentials for guest user");

                // performAction(action);
                downloadWithTransferUtility(file);
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
//                not used
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
//                not used
            }

            @Override
            public void onFailure(Exception exception) {
                Log.i(TAG, "error getting session: " + exception.getLocalizedMessage());
//                proceed using guest user credentials

                //performAction(action);
                downloadWithTransferUtility(file);
            }
        });
    }

    /*private void performAction(int action) {
        switch (action) {
            case 1:
                //uploadWithTransferUtility();
                break;
            case 2:
                downloadWithTransferUtility();
        }
    }*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()... clearing credentials provider");
        /*clear the cached/saved credentials so we don't use them for guest user if not logged in*/
        credentialsProvider.clear();
    }

    private void downloadWithTransferUtility(final String file) {

        final File myFile = new File(getExternalFilesDir(config.videopath) + "/" + result.get("tag").getS() + "/", file);

        //details.setText("Vérification du fichier existe ou non ");
        if (myFile.exists()) {
            //details.setText("Suppression avec succés");
            myFile.delete();
        }

        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);

        try {
            /*need to clear cached files at some stage*/
            //final File myFile = new File(Environment.getExternalStorageDirectory(),"bonjour.mp4");
            Log.i(TAG, "MyFile downloadWithTransferUtility" + myFile.getAbsolutePath());

            TransferUtility transferUtility =
                    TransferUtility.builder()
                            .context(getApplicationContext())
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .s3Client(s3Client)
                            //.s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                            .build();

            TransferObserver downloadObserver =
                    transferUtility.download(
                            //video/bonjour.mp4
                            file, myFile);

            // Attach a listener to the observer to get state update and progress notifications
            downloadObserver.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.
                        Log.i(TAG, "state change, file complete");

                    }
                }
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int) percentDonef;
                    Log.i(TAG, "   ID:" + id + "   bytesCurrent: "
                            + bytesCurrent + "   bytesTotal: " + bytesTotal + " " + percentDone + "%");
                }
                @Override
                public void onError(int id, Exception ex) {
                    // Handle errors
                    Log.e(TAG, "error downloading video: " + ex);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();

            Log.i(TAG, "downloading video error: " + e);
        }
    }

    private class RefreshAsyncTask extends AsyncTask<String, Void, String> {

        /* @Override
         protected String doInBackground(Integer... integers) {
             Log.i(TAG, "in asynctask doInBackground()");
             credentialsProvider.refresh();
            // return integers[0];
         }*/
        @Override
        protected void onPreExecute() {
         //   startProgress();
        }

        @Override
        protected String doInBackground(String... strings) {
            credentialsProvider.refresh();

            return strings[0];
        }

        @Override
        protected void onPostExecute(String file) {
            stopProgress();
            Log.i(TAG, "in asynctask onPostExecute()");
            String extension = file.substring(file.lastIndexOf("."));

            //downloadWithTransferUtility(file);
            if (extension.equals(".json")) {
                Log.i(TAG,"contains  "+ "file .json" +extension);
                ReadFile(file);
            }

            if (!extension.equals(".json") ) {
                Log.i(TAG,"contains no  "+ "file .json" + extension);
                //get folder size
                long FolderSize = getFileFolderSize(new File(config.videopath));

                String size = result.get("size").getN();

                Log.i("TAG", "FolderSize : " + FolderSize + " size in database : " + size);

                // check if size in Internal storage equal to size in firebase
                if (FolderSize >= Long.parseLong(size)) {
                    //Insert size in DB
                    helper.insertInConfig(Long.parseLong(size), FolderSize);
                    //go intent conversation
                    goConversation();
                } else {
                    //redowload
                    //CheckFreeMemory(result);
                }
            }

        }
    }
}






