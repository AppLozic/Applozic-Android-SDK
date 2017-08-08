package com.applozic.mobicomkit.sample;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.applozic.mobicomkit.uiwidgets.conversation.ConversationUIService;
import com.applozic.mobicomkit.uiwidgets.conversation.activity.ConversationActivity;

/**
 * Created by sunil on 30/9/16.
 */
public class InitiateDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private EditText inputEditText;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inputEditText = new EditText(getActivity());
        return new AlertDialog.Builder(getActivity()).setTitle(R.string.initiate_chat_info).setMessage(R.string.enter_user_id_info)
                .setPositiveButton(R.string.start, this).setNegativeButton(R.string.cancel, null).setView(inputEditText).create();
    }

    @Override
    public void onClick(DialogInterface dialog, int position) {

        switch (position) {
            case -1:
                String editTextValue = inputEditText.getText().toString();
                if (TextUtils.isEmpty(editTextValue) || inputEditText.getText().toString().trim().length() == 0) {
                    Toast.makeText(getActivity(), R.string.empty_user_id_info, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), ConversationActivity.class);
                intent.putExtra(ConversationUIService.USER_ID, editTextValue);
                startActivity(intent);
                dialog.dismiss();
                break;
            case -2:
                dialog.dismiss();
                break;
        }

    }

}
