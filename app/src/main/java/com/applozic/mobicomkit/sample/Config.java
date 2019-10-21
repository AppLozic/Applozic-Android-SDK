package com.applozic.mobicomkit.sample;

import android.os.Environment;

/**
 * Created by Nozha GH
 */
public class Config {

    // url firebase
    public String getUrlFireBase = "https://phoneauthentification-d60ac.firebaseio.com";

    //url storage videos
    public String  videopath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/HearMe/";

   public String  accessKey = "";
   public String secretKey = "";
   public String url_linguistic_server = "http://51.91.11.164:3001/translate?text=";



}
