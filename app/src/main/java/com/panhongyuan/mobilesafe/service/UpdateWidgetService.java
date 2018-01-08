package com.panhongyuan.mobilesafe.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.RemoteViews;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.engine.ProcessInfoProvider;
import com.panhongyuan.mobilesafe.receiver.MyAppWidgetProvider;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pan on 17-4-9.
 */

public class UpdateWidgetService extends Service {

    private Timer mTimer;
    private InnerReceiver mInnerReceiver;
    private String tag = "UpdateWidgetService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //关闭进程总数和可用内存数的更新过程（使用定时器）
        startTime();
        //注册开锁或者解锁的广播接收者
        IntentFilter intentFilter = new IntentFilter();
        //开锁的action
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        //锁屏action
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);

        mInnerReceiver = new InnerReceiver();
        registerReceiver(mInnerReceiver, intentFilter);

        super.onCreate();
    }

    private class InnerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                //开启窗口定时更新
                startTime();
            } else {
                //关闭窗口定时更新
                cancelTimerTask();
            }
        }
    }

    /**
     * 取消定时任务的方法
     */
    private void cancelTimerTask() {
        //mTimer取消定时任务
        if (mTimer != null) {
            mTimer.cancel();
            //使其引用为空
            mTimer = null;
        }
    }

    /**
     * 定时器的方法
     */
    private void startTime() {
        mTimer = new Timer();
        //参数：1.任务，2.延迟执行第一次任务的时间，3.不断执行的时间间隔
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //ui定时刷新
                updateAppWidget();
                //测试代码
                Log.i(tag, "5秒一次的定时任务正在运行中.....");
            }
        }, 0, 5000);
    }

    /**
     * 更新UI的方法
     */
    private void updateAppWidget() {
        //1.获取AppWidget的管理对象
        AppWidgetManager awm = AppWidgetManager.getInstance(getApplicationContext());
        //2.获取窗体小部件布局装换成的View对象,参数：1.包名，2.布局ID
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.process_widget);
        //3.给窗体小部件内部的控件赋值
        remoteViews.setTextViewText(R.id.tv_process_count, "进程总数：" + ProcessInfoProvider.getProcessCount(getApplicationContext()));
        //4.显示可使用的内存大小
        String fileSize = Formatter.formatFileSize(getApplicationContext(), ProcessInfoProvider.getAvailSpace(getApplicationContext()));
        remoteViews.setTextViewText(R.id.tv_process_memory, "可用内存大小为:" + fileSize);

        //点击跳转的实现
        //创建一个实际意图
        Intent intent = new Intent("android.intent.action.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        //点击窗体小部件，进入应用参数：1.在那个控件上响应事件，2.延期的意图，延期意图在此用于点击之后开启一个Activity,所以调用getActivity方法
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.ll_root, pendingIntent);

        //通过延期意图发送广播，在广播接收者中杀死进程
        Intent broadCastIntent = new Intent("android.intent.KILL_BACKGROUND_PROCESS");
        PendingIntent broadcast = PendingIntent.getBroadcast(getApplicationContext(), 0, broadCastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        //通过点击按钮发送广播
        remoteViews.setOnClickPendingIntent(R.id.btn_clear, broadcast);

        //窗体控件的更新
        ComponentName componentName = new ComponentName(getApplicationContext(), MyAppWidgetProvider.class);
        //更新窗体小部件
        awm.updateAppWidget(componentName, remoteViews);
    }

    /**
     * 移除窗体最后一个部件的时候调用的方法，取消定时任务
     */
    @Override
    public void onDestroy() {
        //取消注册广播锁屏接收者
        if (mInnerReceiver != null) {
            unregisterReceiver(mInnerReceiver);
        }
        //取消定时任务
        cancelTimerTask();
        super.onDestroy();
    }


}
