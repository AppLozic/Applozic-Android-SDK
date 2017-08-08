package com.applozic.mobicomkit.contact;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicommons.commons.core.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by devashish on 27/12/14.
 */
public class ContactService {

    private Context context;

    public ContactService(Context context) {
        this.context = context;
    }

    /**
     * @param contactData
     * @return
     */
    public File vCard(Uri contactData) throws Exception {
        Cursor cursor = context.getContentResolver().query(contactData, null, null, null, null);
        cursor.moveToFirst();
        String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "CONTACT_" + timeStamp + "_" + ".vcf";

        File outputFile = FileClientService.getFilePath(imageFileName, context.getApplicationContext(), "text/x-vcard");
        BufferedReader br = null;
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        br = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        try {
            String line;
            if (br != null) {
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] buf = sb.toString().trim().getBytes();
        if (!MobiComVCFParser.validateData(sb.toString())) {
            Utils.printLog(context,"vCard ::", sb.toString().toString());
            throw new Exception("contact exported is not proper in proper format");
        }
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile.getAbsoluteFile());
        fileOutputStream.write(buf);
        fileOutputStream.close();
        return outputFile;
    }


}
