package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MessageBuilder;
import com.applozic.mobicommons.people.contact.Contact;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

public class ConcatWithFFMpeg extends AppCompatActivity {
    protected Contact contact;
    String TAG;
    Config config = new Config();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Loading ffmpeg
    public void loadFFMpegBinary(Context context) {
        FFmpeg ffmpeg;
         TAG  = "FFmpeg";
        Log.i(TAG, "***start loadFFMpegBinary***");
        //initUI();
        ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    //  showUnsupportedExceptionDialog();
                    Log.i(TAG, "Load ffmpeg");

                }
            });
        } catch (FFmpegNotSupportedException e) {
            // showUnsupportedExceptionDialog();
            Log.e(TAG, "Exception " + e);

        }
    }

    // replace text message with video message
    public void sendVideoMessage(Context context, String resultTranslation, String msg, String contact) {
        final DatabaseHelper helper = new DatabaseHelper(context);
        final ArrayList AllCategory = helper.getAllCategoryWithoutCondition();

        File myFile = new File(context.getExternalFilesDir(config.videopath)+"/LSF", "video");
        File myFileConcat = new File(config.videopathConcat);
        Log.i(TAG, "myFileConcat" + myFileConcat);


        Log.i(TAG, "***sendVideoMessage***");
        String[] items = (resultTranslation.trim().split("\\s+"));

        Log.i(TAG, "***sendVideoMessage msg*** " + resultTranslation);
        Log.i(TAG, "***sendVideoMessage items length*** " + items.length);

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "VID_" + timeStamp + "_" + ".mp4";

        if (items.length == 1) {
            if (AllCategory.contains(items[0].toLowerCase())) {
                String message = myFile + "/" + items[0].toLowerCase()+".mp4";
                Log.i(TAG, "***sendVideoMessage OneItem***" + message);

                new MessageBuilder(context)
                        .setContentType(Message.ContentType.ATTACHMENT.getValue())
                        .setTo(contact)
                        //setGroupId()
                        .setFilePath(message)
                        .setMessage(msg)
                        .send();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                preferences.edit().remove(contact).commit();
            } else {
                Toast.makeText(context, "words does not exists", Toast.LENGTH_SHORT).show();
            }
        } else {

            String outputFile = myFileConcat + "/" + imageFileName;


            Log.i(TAG, "***sendVideoMessage MultipleItem***");

            ArrayList<String> list = new ArrayList<String>();

            for (int i = 0; i < items.length; i++) {

                if (AllCategory.contains(items[i].toLowerCase())) {

                    list.add(myFile + "/" + items[i].toLowerCase() + ".mp4");

                    Log.d(TAG, "******Concatenating*******" + items[i]);
                }
            }
            String generatelist = generateList(context, new ArrayList[]{list});


            String[] cmd = new String[]{
                    "-f",
                    "concat",
                    "-safe",
                    "0",
                    "-i",
                    generatelist,
                    "-c",
                    "copy",
                    outputFile
            };
            // Generating list of paths
            //Log.d(TAG, "******Concatenating*******" + generatelist);

            execFFmpegBinary(new File(String.valueOf(generatelist)), context, cmd, outputFile, msg, contact);
        }
    }

    // Generating list
    private String generateList(Context context, ArrayList[] inputs) {
        File list;
        Writer writer = null;
        Log.d(null, "*** start generate List***");

        try {
            File myFileConcat = new File(config.videopathConcat);
            list = File.createTempFile("ffmpeg-list", ".txt", myFileConcat);

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(list)));
            for (Object input : inputs[0]) {

                writer.write("file '" + input + "'\n");
                Log.d(TAG, "Writing to list file: file '" + input + "'");
            }
        } catch (IOException e) {
            Log.e(TAG, "Generate list ffmpeg IOException " + e);
            e.printStackTrace();
            return "/";
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        Log.d(TAG, "Wrote list file to " + list.getAbsolutePath());
        return list.getAbsolutePath();
    }


    private void execFFmpegBinary(final File list, final Context context, final String[] command, final String outputFile, final String msg, final String contact) {
        Log.d(TAG, "***execFFmpegBinary***" + command);
        final FFmpeg ffmpeg;

        try {
            ffmpeg = FFmpeg.getInstance(context);

            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    // addTextViewToLayout("FAILED with output : "+s);
                    Log.e(TAG, "execFFmpeg onFailure" + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "execFFmpeg onSuccess" + s);
                    Log.d(TAG, "execFFmpeg File" + list);


                    //addTextViewToLayout("SUCCESS with output : "+s);
                    new MessageBuilder(context)
                            .setContentType(Message.ContentType.ATTACHMENT.getValue())
                            .setTo(contact)
                            //setGroupId()
                            .setFilePath(outputFile)
                            .setMessage(msg)
                            .send();
                    list.delete();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    preferences.edit().remove(contact).commit();

                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    // addTextViewToLayout("progress : "+s);
                    // progressDialog.setMessage("Processing\n"+s);
                }

                @Override
                public void onStart() {
                    // outputLayout.removeAllViews();

                    Log.d(TAG, "Started command : ffmpeg " + command);
                    // progressDialog.setMessage("Processing...");
                    // progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    // progressDialog.dismiss();

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
            Log.e(TAG, "error for : ffmpeg " + e);

        }
    }


}
