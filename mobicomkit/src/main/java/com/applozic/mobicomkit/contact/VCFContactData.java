package com.applozic.mobicomkit.contact;

import android.graphics.Bitmap;

/**
 * Created by devashish on 09/03/16.
 */
public class VCFContactData {

    private String name;
    private String email;
    private Bitmap profilePic;
    private String telephoneNumber;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(Bitmap profilePic) {
        this.profilePic = profilePic;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public boolean isValid() {
        return (this.name != null && (this.telephoneNumber != null || this.email != null));

    }

}
