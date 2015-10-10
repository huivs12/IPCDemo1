package com.thh.ipcdemo1;

import android.app.ActivityManager;
import android.app.Application;
import android.util.Log;

/**
 * Created by TangHui on 2015/10/10.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("thh", "[MyApplication onCreate] application start, process name:"+getProcessName());
    }

    private String getProcessName(){
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo:
            activityManager.getRunningAppProcesses()) {
            if (runningAppProcessInfo.pid == android.os.Process.myPid()) {
                return runningAppProcessInfo.processName;
            }
        }
        return null;
    }
}
