package com.applozic.mobicomkit.feed;


import com.applozic.mobicommons.json.JsonMarker;

import java.util.List;

/**
 * Created by sunil on 13/6/16.
 */
public class UserDetailListFeed extends JsonMarker {

    private List<String> userIdList;
    private List<String> phoneNumberList;
    private boolean contactSync;

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    public List<String> getPhoneNumberList() {
        return phoneNumberList;
    }

    public void setPhoneNumberList(List<String> phoneNumberList) {
        this.phoneNumberList = phoneNumberList;
    }

    public boolean isContactSync() {
        return contactSync;
    }

    public void setContactSync(boolean contactSync) {
        this.contactSync = contactSync;
    }

    @Override
    public String toString() {
        return "UserDetailListFeed{" +
                "userIdList=" + userIdList +
                ", phoneNumberList=" + phoneNumberList +
                '}';
    }
}
