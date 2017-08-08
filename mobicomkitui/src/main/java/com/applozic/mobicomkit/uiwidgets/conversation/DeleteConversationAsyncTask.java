package com.applozic.mobicomkit.uiwidgets.conversation;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.applozic.mobicomkit.api.conversation.Message;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;

import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by devashish on 9/2/15.
 */
public class DeleteConversationAsyncTask extends AsyncTask<Void, Integer, Long> {

    private Message message;
    private Contact contact;
    private MobiComConversationService conversationService;
    private boolean isThreaddelete = false;
    private ProgressDialog progressDialog;
    private Context context;
    private Channel channel;
    private Integer conversationId;


    public DeleteConversationAsyncTask(MobiComConversationService conversationService, Message message, Contact contact) {
        this.message = message;
        this.contact = contact;
        this.conversationService = conversationService;
    }

    public DeleteConversationAsyncTask(MobiComConversationService conversationService, Contact contact, Channel channel, Integer conversationId, Context context) {
        this.contact = contact;
        this.context = context;
        this.channel = channel;
        this.conversationId = conversationId;
        this.conversationService = conversationService;
        this.isThreaddelete = true;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (isThreaddelete) {
            progressDialog = ProgressDialog.show(context, "",
                    context.getString(R.string.delete_thread_text), true);
        }
    }

    @Override
    protected Long doInBackground(Void... params) {
        if (isThreaddelete) {
            conversationService.deleteSync(contact, channel, conversationId);
        } else {
            conversationService.deleteMessage(message, contact);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
