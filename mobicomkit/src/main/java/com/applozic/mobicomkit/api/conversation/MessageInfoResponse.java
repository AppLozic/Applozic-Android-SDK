package com.applozic.mobicomkit.api.conversation;

import com.applozic.mobicommons.json.JsonMarker;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by devashish on 28/03/16.
 */
public class MessageInfoResponse extends JsonMarker {

    @SerializedName("response")
    List<MessageInfo> messageInfoList;

    public List<MessageInfo> getMessageInfoList() {
        return messageInfoList;
    }

    public void setMessageInfoList(List<MessageInfo> messageInfoList) {
        this.messageInfoList = messageInfoList;
    }

    public List<MessageInfo> getDeliverdToUserList() {

        if (this.messageInfoList == null) {
            return null;
        }
        List<MessageInfo> deliverdToUserList = new ArrayList<MessageInfo>();

        for (MessageInfo messageInfo : messageInfoList) {
            if (!messageInfo.isRead()) {
                deliverdToUserList.add(messageInfo);
            }
        }
        return deliverdToUserList;
    }

    public List<MessageInfo> getReadByUserList() {

        if (this.messageInfoList == null) {
            return null;
        }
        List<MessageInfo> readList = new ArrayList<MessageInfo>();

        for (MessageInfo messageInfo : messageInfoList) {
            if (messageInfo.isRead()) {
                readList.add(messageInfo);
            }
        }
        return readList;
    }

}
