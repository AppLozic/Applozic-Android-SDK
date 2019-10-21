package com.applozic.mobicomkit.sample;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
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
    private final String KEY = "AKIAJ4MFOD3DFJO7CY7Q";
    private final String SECRET = "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";

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_dictionary);

        AWSMobileClient.getInstance().initialize(this).execute();

        videoView = (VideoView) findViewById(R.id.VideoView);



        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        //Get Select item
        String selectedItem = extras.getString("selectedItem");

        //Get list of select object
        Map<String, String> List_data = (HashMap<String, String>) intent.getSerializableExtra("dataObject");
        // String List_data = extras.getString("NewList");

        Log.v("TAG", "***ListObject*** " + List_data);
        Log.i("TAG", "***SelectItem***  " + selectedItem);

        String urlCorpus = List_data.get("url-corpus");
        String urlJson = List_data.get("url-json");
        final String size = List_data.get("size");
        final String tag = List_data.get("tag");
        final String hash = List_data.get("hash");
        Log.i("TAG", "***Size***  " + size);
        Log.i("TAG", "***TestKey***  " + urlCorpus);
        Log.i("TAG", "***TestKey***  " + urlJson);

        FreeMemory(size, tag, urlJson, urlCorpus, hash);
        //downloadFileAWS();
    }

    // Parsing json and store in DB
    public void ReadFile(File file, String tag, String size) {
        try {
            File myFile = new File(getExternalFilesDir(String.valueOf(file)), "");
            Log.i("TAG", "myFile" + myFile);
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
            Log.i("TAG", "***jsonObjjjjj***  " + jsonArry);
            for (int i = 0; i < jsonArry.length(); i++) {

                Log.e("Message", "loop");
                JSONObject e = jsonArry.getJSONObject(i);
                byte[] bytes = callEncode(e.getString("path"), e.getString("name"), tag, size);
                Log.e("TAG", "getString" + e.getString("path"));
                Log.e("TAG", "encodeByte" + bytes);

                helper.insert(e.getString("name"), e.getString("category"), 1);
                Log.i("TAG", "MyInsert " + e.getString("name") + e.getString("path") + e.getString("category"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("TAG", "***Exception***  " + e);

        }
    }

    //call encdoe
    public byte[] callEncode(final String path, final String name, final String tag, final String size) {
        Log.i("TAG", "***callEncode***  " + path);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    encodeByte(path, name, tag, size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        return new byte[0];
    }

    //encode videos to  bytes
    private byte[] encodeByte(String url, String name, String tag, String size) {

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
    }

    // decode bytes to videos
    public FileOutputStream decodeByte(byte[] data, String name, String tag, String size) throws IOException {
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

    }

    // Calculate available espace storage
    void FreeMemory(String size, String tag, String urlJson, String urlCorpus, String hash) {

        //details.setText("Vérification de l'espace");
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } else {
            bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        }
        long megAvailable = bytesAvailable;

        Log.e("TAG", "AvailableMB : " + megAvailable);
        Log.e("TAG", "AvailableMB2 : " + bytesAvailable);

        // test
        if (Long.parseLong(size) <= megAvailable) {
            //details.setText("Great ! espace requise disponible");


            //if espace exists then download
            download(urlJson, tag, ".json", hash, size);

        } else {
            //details.setText("Oups ! espace requise non disponible");
        }
    }

    //download JSON
    public void download(String url, final String tag, final String extension, final String hash, final String size) {

        Log.e("TAG", "*******Download********");
        Log.e("TAG", "videopathdown" + config.videopath);

        // storageReference= FirebaseStorage.getInstance().getReference();
        //ref=storageReference.child(url);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference httpsReference = storage.getReferenceFromUrl(url);
        // check if file exists
        File dir = new File(config.videopath + tag + extension);
        //details.setText("Vérification du fichier existe ou non ");
        if (dir.exists()) {
            //details.setText("Suppression avec succés ");
            dir.delete();
        }
        httpsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

            @Override
            public void onSuccess(Uri uri) {
                String url = uri.toString();
                Log.e("TAG", "uri: " + uri.toString());
                try {
                    downloadFile(DownloadDictionary.this, tag, extension, config.videopath + tag, url, hash, size);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                Log.e("TAG", "uri: " + uri.toString());
                //Handle whatever you're going to do with the URL here
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAG", "ExceptionInDownload " + e);
            }
        });
    }

    void downloadFile(Context context, final String fileName, final String fileExtension, String destinationDirectory, String url, final String hash, final String size) throws IOException, NoSuchAlgorithmException {
        Log.e("TAG", "*******DownloadFile********");
        // details.setText("Début de Téléchargement" + fileName + fileExtension);

        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        final long downloadId = manager.enqueue(request);

        final ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.progressBar_storage);


        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                while (downloading) {
                    Log.i("TAG", "downloading " + downloading);
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadId);

                    final Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    final int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;

                        runOnUiThread(new Runnable() {
                            //   @Override
                            public void run() {
                                // Stuff that updates the UI
                                //    getMd5OfFile(new File(videopath + fileName + "/" + fileName + ".zip"), hash, videopath, fileName);
                                ReadFile(new File(config.videopath + fileName + "/" + fileName + ".json"), fileName, size);
                            }
                        });

                    }

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // details.setText("Téléchargement en cours...");
                            //ProgressPerc.setText((int) dl_progress);
                            mProgressBar.setProgress((int) dl_progress);
                            int bytesdown = bytes_downloaded / (1024 * 1024);
                            int bytestotal = bytes_total / (1024 * 1024);
                            // ProgressPerc.setText(bytesdown + "/" + bytestotal + " Mo");
                            //   details.setText(statusMessage(cursor));
                        }
                    });

                    cursor.close();

                }
            }

        }).start();
    }

    // get size folder in internal storage
    public long getFileFolderSize(File dir) {
        File myFile = new File(getExternalFilesDir(String.valueOf(dir)), "");
        long size = 0;
        if (myFile.isDirectory()) {

            for (File file : myFile.listFiles()) {
                if (file.isFile()) {
                    size += file.length();
                }
            }
        }
        Log.i("TAG", "getFileFolderSize: " + size);

        return size;
    }

    public void goConversation() {

        Intent intent = new Intent(context, ConversationActivity.class);
        startActivity(intent);
        finish();
    }



    private void proceed() {
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
                        logins.put("cognito-idp.eu-west-1.amazonaws.com/eu-west-1_3CLusdQiz", idToken);
                        credentialsProvider.setLogins(logins);

                        Log.i(TAG, "using credentials for the logged in user");

                        /*refresh provider off main thread*/
                        Log.i(TAG, "refreshing credentials provider in asynctask..");
                        new RefreshAsyncTask().execute();

                    } else {
                        Log.i(TAG, "no token...");
                    }
                } else {
                    Log.i(TAG, "user session not valid - using identity pool credentials - guest user");
                }

                downloadWithTransferUtility();
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                Log.i(TAG, " Not logged in! using identity pool credentials for guest user");

                downloadWithTransferUtility();
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

                downloadWithTransferUtility();
            }
        });
    }

    private class RefreshAsyncTask extends AsyncTask<Integer, Void, Integer> {

        @Override
        protected Integer doInBackground(Integer... integers) {
            Log.i(TAG, "in asynctask doInBackground()");
            credentialsProvider.refresh();
            return integers[0];
        }

        @Override
        protected void onPostExecute(Integer action) {
            Log.i(TAG, "in asynctask onPostExecute()");

            downloadWithTransferUtility();
        }
    }

    private void downloadWithTransferUtility() {

        AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);


        try {
            /*need to clear cached files at some stage*/
            File outputDir = getCacheDir();
            final File tempCacheFile = File.createTempFile("bonjour", ".mp4", outputDir);
            final File myFile = new File(Environment.getExternalStorageDirectory(),"bonjour.mp4");
            Log.i("TAG","MyFile" + myFile.getAbsolutePath());


            TransferUtility transferUtility =
                    TransferUtility.builder()
                            .context(getApplicationContext())
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .s3Client(s3Client)
                            .build();

            TransferObserver downloadObserver =
                    transferUtility.download(
                            "bonjour.mp4", myFile);

            // Attach a listener to the observer to get state update and progress notifications
            downloadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        // Handle a completed upload.
                        Log.i(TAG, "state change, image download complete");

                        // Bitmap bmp = BitmapFactory.decodeFile(tempCacheFile.getAbsolutePath());
                        //imageView.setImageBitmap(bmp);
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
                    Log.i(TAG, "error downloading image: " + ex.getLocalizedMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "downloading image error: " + e.getLocalizedMessage());
        }
    }

}






