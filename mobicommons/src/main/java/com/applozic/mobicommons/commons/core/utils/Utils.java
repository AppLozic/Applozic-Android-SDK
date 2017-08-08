package com.applozic.mobicommons.commons.core.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


/**
 * This class contains static utility methods.
 */
public class Utils {

    private static final String TAG = "Utils";

    // Prevents instantiation.
    private Utils() {
    }

    /**
     * Enables strict mode. This should only be called when debugging the application and is useful
     * for finding some potential bugs or best practice violations.
     */
    @TargetApi(11)
    public static void enableStrictMode() {
        // Strict mode is only available on gingerbread or later
        if (Utils.hasGingerbread()) {

            // Enable all thread strict mode policies
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Enable all VM strict mode policies
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            // Honeycomb introduced some additional strict mode features
            if (Utils.hasHoneycomb()) {
                // Flash screen when thread policy is violated
                threadPolicyBuilder.penaltyFlashScreen();
                // For each activity class, set an instance limit of 1. Any more instances and
                // there could be a memory leak.
               /* vmPolicyBuilder
                        .setClassInstanceLimit(ContactActivity.class, 1);*/
            }

            // Use builders to enable strict mode policies
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    /**
     * Uses static final constants to detect if the device's platform version is Froyo or
     * later.
     */
    public static boolean hasFroyo() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Gingerbread or
     * later.
     */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb or
     * later.
     */
    public static boolean hasHoneycomb() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;


    }

    /**
     * Uses static final constants to detect if the device's platform version is Honeycomb MR1 or
     * later.
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * Uses static final constants to detect if the device's platform version is ICS or
     * later.
     */

    public static boolean hasICS() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= 24;
    }


    public static boolean isBetweenGingerBreadAndKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT;
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get package name: " + e);
        }
        return -1;
    }

    public static void toggleSoftKeyBoard(Activity activity, boolean hide) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            return;
        }
        if (hide) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        } else {
            inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static boolean isNetworkAvailable(final Activity activity, final String errorMessage) {
        if (activity == null) {
            return true;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        return activeNetworkInfo != null;
    }

    public static String getDeviceInformation() {

        return "Phone Model = " + Build.MODEL + "::Android Version = " + Build.VERSION.SDK_INT + ":::MANUFACTURER  = " + Build.MANUFACTURER + "::Overall PRODUCT = " + Build.PRODUCT;
    }

    public static Uri getUserImageUri(Context context) {
        if (Build.VERSION.SDK_INT > 14) {
            String[] mProjection = new String[]
                    {
                            ContactsContract.Profile._ID,
                            ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
                            ContactsContract.Profile.LOOKUP_KEY,
                            ContactsContract.Profile.PHOTO_THUMBNAIL_URI
                    };
            Cursor mProfileCursor =
                    context.getContentResolver().query(
                            ContactsContract.Profile.CONTENT_URI,
                            mProjection, null, null, null);

            try {
                if (mProfileCursor.moveToFirst()) {
                    String photo = mProfileCursor.getString(mProfileCursor.getColumnIndex(ContactsContract.Profile.PHOTO_THUMBNAIL_URI));
                    if (!TextUtils.isEmpty(photo)) {
                        return Uri.parse(photo);
                    }
                }
            } finally {
                if (mProfileCursor != null) {
                    mProfileCursor.close();
                }
            }
        }
        return null;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting());

    }

    public static String getMetaDataValue(Context context, String metaDataName) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                return ai.metaData.getString(metaDataName);

            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static int getLauncherIcon(Context context) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return ai.icon;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static Integer getMetaDataValueForResources(Context context, String metaDataName) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData != null) {
                Integer metaDataValue = ai.metaData.getInt(metaDataName);
                return metaDataValue == 0 ? null : metaDataValue;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }


    public static String getMetaDataValueForReceiver(Context context, String componentName, String metaDataName) {
        try {
            ActivityInfo ai = context.getPackageManager().getReceiverInfo(new ComponentName(context, componentName), PackageManager.GET_META_DATA);
            return ai.metaData.getString(metaDataName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String makePlaceHolders(int len) {
        if (len < 1) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }


    public static CharSequence getStyleString(String name) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(name);
        return builder;
    }

    public static CharSequence getStyledStringForContact(String displayName, String message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(displayName).append(": ").append(message);
        return builder;
    }

    public static CharSequence getStyledStringForChannel(String name, String channelName, String message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(name).append(" @ ").append(channelName).append(": ").append(message);
        return builder;
    }

    public static CharSequence getStyleStringForMessage(String message) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(message);
        return builder;
    }

    public static String getTimeDurationInFormat(Long timeInMillis) {

        long timeInSecond = timeInMillis / 1000;
        String formattedDuration;

        if (timeInSecond < 60) {
            return (timeInSecond + " Sec");
        } else {
            formattedDuration = timeInSecond / 60 + " Min";
            if (timeInSecond % 60 > 0) {
                formattedDuration = formattedDuration + " " + timeInSecond % 60 + " Sec";
            }
        }

        return formattedDuration;
    }

    public static void printLog(Context context, String tag, String message) {
        try{
            if(context != null) {
                boolean isDebuggable = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
                if (isDebuggable) {
                    Log.i(tag, message);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
