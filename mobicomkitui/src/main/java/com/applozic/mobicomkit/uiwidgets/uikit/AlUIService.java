package com.applozic.mobicomkit.uiwidgets.uikit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.applozic.mobicomkit.api.account.user.MobiComUserPreference;
import com.applozic.mobicomkit.api.conversation.MobiComConversationService;
import com.applozic.mobicomkit.channel.service.ChannelService;
import com.applozic.mobicomkit.contact.AppContactService;
import com.applozic.mobicomkit.contact.BaseContactService;
import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelDeleteTask;
import com.applozic.mobicomkit.uiwidgets.async.ApplozicChannelLeaveMember;
import com.applozic.mobicomkit.uiwidgets.conversation.DeleteConversationAsyncTask;
import com.applozic.mobicommons.commons.core.utils.Utils;
import com.applozic.mobicommons.people.channel.Channel;
import com.applozic.mobicommons.people.channel.ChannelUtils;
import com.applozic.mobicommons.people.contact.Contact;

/**
 * Created by ashish on 30/05/18.
 */

public class AlUIService {
    private Context context;
    private BaseContactService contactService;

    public AlUIService(Context context) {
        this.context = context;
        contactService = new AppContactService(context);
    }

    /**
     * This method takes channel as argument and creates a dialog alerting the user to delete the group.
     * If clicked yes, the channel delete task will be started.
     *
     * @param channel The group object that is to be deleted
     */
    public void deleteGroupConversation(final Channel channel) {

        if (!Utils.isInternetAvailable(context)) {
            showToastMessage(context.getString(R.string.you_dont_have_any_network_access_info));
            return;
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context).
                setPositiveButton(R.string.channel_deleting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        final ProgressDialog progressDialog = ProgressDialog.show(context, "",
                                context.getString(R.string.deleting_channel_user), true);
                        ApplozicChannelDeleteTask.TaskListener channelDeleteTask = new ApplozicChannelDeleteTask.TaskListener() {
                            @Override
                            public void onSuccess(String response) {

                            }

                            @Override
                            public void onFailure(String response, Exception exception) {
                                showToastMessage(context.getString(Utils.isInternetAvailable(context) ? R.string.applozic_server_error : R.string.you_dont_have_any_network_access_info));
                            }

                            @Override
                            public void onCompletion() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                            }
                        };
                        ApplozicChannelDeleteTask applozicChannelDeleteTask = new ApplozicChannelDeleteTask(context, channelDeleteTask, channel);
                        applozicChannelDeleteTask.execute((Void) null);
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setMessage(context.getString(R.string.delete_channel_messages_and_channel_info).replace(context.getString(R.string.group_name_info), channel.getName()).replace(context.getString(R.string.groupType_info), Channel.GroupType.BROADCAST.getValue().equals(channel.getType()) ? context.getString(R.string.broadcast_string) : context.getString(R.string.group_string)));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    /**
     * This method deletes the conversation with a contact or channel.
     *
     * @param contact The contact object whose conversation thread is to be deleted
     * @param channel The group object whose conversation thread is to be deleted
     */
    public void deleteConversationThread(final Contact contact, final Channel channel) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context).
                setPositiveButton(R.string.delete_conversation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new DeleteConversationAsyncTask(new MobiComConversationService(context), contact, channel, null, context).execute();

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        String name = "";
        if (channel != null) {
            if (Channel.GroupType.GROUPOFTWO.getValue().equals(channel.getType())) {
                String userId = ChannelService.getInstance(context).getGroupOfTwoReceiverUserId(channel.getKey());
                if (!TextUtils.isEmpty(userId)) {
                    Contact withUserContact = contactService.getContactById(userId);
                    name = withUserContact.getDisplayName();
                }
            } else {
                name = ChannelUtils.getChannelTitleName(channel, MobiComUserPreference.getInstance(context).getUserId());
            }
        } else {
            name = contact.getDisplayName();
        }
        alertDialog.setTitle(context.getString(R.string.dialog_delete_conversation_title).replace("[name]", name));
        alertDialog.setMessage(context.getString(R.string.dialog_delete_conversation_confir).replace("[name]", name));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    /**
     * This method takes the channel object and removes the logged in user from this channel.
     *
     * @param channel the group object from which the logged in user decides to leave.
     */
    public void channelLeaveProcess(final Channel channel) {
        if (!Utils.isInternetAvailable(context)) {
            showToastMessage(context.getString(R.string.you_dont_have_any_network_access_info));
            return;
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context).
                setPositiveButton(R.string.channel_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ApplozicChannelLeaveMember.ChannelLeaveMemberListener applozicLeaveMemberListener = new ApplozicChannelLeaveMember.ChannelLeaveMemberListener() {
                            @Override
                            public void onSuccess(String response, Context context) {
                            }

                            @Override
                            public void onFailure(String response, Exception e, Context context) {
                                showToastMessage(context.getString(Utils.isInternetAvailable(context) ? R.string.applozic_server_error : R.string.you_dont_have_any_network_access_info));
                            }
                        };
                        ApplozicChannelLeaveMember applozicChannelLeaveMember = new ApplozicChannelLeaveMember(context, channel.getKey(), MobiComUserPreference.getInstance(context).getUserId(), applozicLeaveMemberListener);
                        applozicChannelLeaveMember.setEnableProgressDialog(true);
                        applozicChannelLeaveMember.execute((Void) null);

                    }
                });
        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        alertDialog.setMessage(context.getString(R.string.exit_channel_message_info).replace(context.getString(R.string.group_name_info), channel.getName()).replace(context.getString(R.string.groupType_info), Channel.GroupType.BROADCAST.getValue().equals(channel.getType()) ? context.getString(R.string.broadcast_string) : context.getString(R.string.group_string)));
        alertDialog.setCancelable(true);
        alertDialog.create().show();
    }

    /**
     * Creates a Toast message for displaying warnings and error messages
     *
     * @param messageToShow The message that will be displayed in the toast
     */
    private void showToastMessage(final String messageToShow) {
        Toast toast = Toast.makeText(context, messageToShow, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
