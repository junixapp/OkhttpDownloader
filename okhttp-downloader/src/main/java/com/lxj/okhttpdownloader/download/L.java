package com.lxj.okhttpdownloader.download;

import android.util.Log;

/**
 * Created by dance on 2017/3/26.
 */

public class L {
    private static final String TAG = "OkhttpDownloader";
    private static boolean isDebug = true;
    public static void d(String msg){
        if(isDebug){
            Log.d(TAG,msg);
        }
    }
    public static void e(String msg){
        if(isDebug){
            Log.e(TAG,msg);
        }
    }
}
