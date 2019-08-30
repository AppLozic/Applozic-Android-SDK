package com.applozic.mobicomkit.sample;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    /**
     * Created by Lincoln on 05/05/16.
     */
        SharedPreferences pref;
        SharedPreferences.Editor editor;
        Context _context;

        // shared pref mode
        int PRIVATE_MODE = 0;

        // Shared preferences file name
        private static final String PREF_NAME = "androidhive-welcome";

        private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

        public PreferenceManager(Context context) {
            this._context = context;
            pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
            editor = pref.edit();
        }

        public void setFirstTimeLaunch(boolean isFirstTime) {
            editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
            editor.commit();
        }

        public boolean isFirstTimeLaunch() {
            return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
        }


}