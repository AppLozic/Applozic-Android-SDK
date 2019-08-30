package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.os.Environment;

/**
 * Created by Nozha GH
 */
public class Config {

    // url firebase
    public String getUrlFireBase = "https://phoneauthentification-d60ac.firebaseio.com";

    //path storage videos
    public String  videopath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearMe/";

    // path storage  concat video
    public String  videopathConcat =  Environment.getExternalStorageDirectory()+"/HearMe/video/";

}
