package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.graphics.Bitmap;

public class Item {
    //Bitmap image;
    String video;
    String title;

    public Item(String video, String title) {
        super();
        this.video = video;
        this.title = title;
    }
    public String getImage() {
        return video;
    }
    public void setImage(String video) {
        this.video = video;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

}
