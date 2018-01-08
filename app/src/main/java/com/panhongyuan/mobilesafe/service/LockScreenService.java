package com.panhongyuan.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.panhongyuan.mobilesafe.engine.ProcessInfoProvider;

/**
 * Created by pan on 17-4-8.
 */

public class LockScreenService extends Service {

    private IntentFilter intentFilter;
    private InnerReceiver innerReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 开启广播接收者时调用的方法
     */
    @Override
    public void onCreate() {
        //锁屏的action
        intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

        innerReceiver = new InnerReceiver();
        registerReceiver(innerReceiver, intentFilter);

        super.onCreate();
    }

    /**
     * 关闭广播接收者时调用的方法
     */
    @Override
    public void onDestroy() {
        if (innerReceiver != null) {
            unregisterReceiver(innerReceiver);
        }
        super.onDestroy();
    }

    private class InnerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //清理手机进程
            ProcessInfoProvider.killAll(getApplicationContext());
        }
    }
}
