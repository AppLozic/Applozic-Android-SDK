package com.applozic.mobicomkit.api.attachment;

import android.content.Context;

import com.applozic.mobicomkit.api.MobiComKitClientService;
import com.applozic.mobicomkit.api.conversation.Message;

/**
 * Created by devashish on 22/07/16.
 */
public class AttachmentViewProperties {

    public AttachmentViewProperties(int width, int height, Context context, Message message) {
        this.width = width;
        this.height = height;
        this.context = context;
        this.message = message;
    }

    private int width;
    private int height;
    private Context context;
    private Message message;

    public String getImageUrl(){
        //file url...
        if (message == null || message.getFileMetas() == null ) {
            return null;
        }
        return new MobiComKitClientService(getContext().getApplicationContext()).getFileUrl() + message.getFileMetas().getBlobKeyString();
    }


    public Message getMessage() {
        return message;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Context getContext() {
        return context;
    }
}
