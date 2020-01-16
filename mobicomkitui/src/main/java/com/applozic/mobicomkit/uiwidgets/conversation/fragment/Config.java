package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.os.Environment;

/**
 * Created by Nozha GH
 */
public class Config {
    //path storage videos
    public String  videopath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearMe/";

    // path storage  concat video
    public String  videopathConcat =  Environment.getExternalStorageDirectory()+"/HearMe/video/";

    public String url_linguistic_server = "http://51.91.11.164:3001/translate?";


}
