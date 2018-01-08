package com.panhongyuan.mobilesafe.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.panhongyuan.mobilesafe.activity.EnterPsdActivity;
import com.panhongyuan.mobilesafe.db.dao.AppLockDao;

import java.util.List;

/**
 * Created by pan on 17-4-10.
 */

public class WatchDogService extends Service {

    private AppLockDao appLockDao;
    private List<String> mPackageNameList;//拦截的包名集合
    private String tag = "WatchDogService";
    private boolean isWatch;
    private String mSkipPackageName;//解锁的包名
    private String packageName;//拦截的包名
    private InnerReceiver mInnerReceiver;
    private MyContentObserver mContentObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        appLockDao = AppLockDao.getInstance(getApplicationContext());
        //开启开门狗循环
        isWatch = true;
        watch();

        //定义广播应用解锁成功的广播接收者对象
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SKIP");

        mInnerReceiver = new InnerReceiver();
        registerReceiver(mInnerReceiver, intentFilter);

        //创建内容观察者,用于观察数据库的变化，一旦数据库有删除或者添加，让appLockDao重新获取一次数据
        mContentObserver = new MyContentObserver(new Handler());
        //注册内容观察者
        getContentResolver().registerContentObserver(Uri.parse("content://applock/change"), true, mContentObserver);//第二个参数为true,代表不匹配，“change”之后的uri,即uri需要一模一样才能接收到消息
        super.onCreate();
    }


    private class MyContentObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserver(Handler handler) {
            super(handler);
        }

        /**
         * 一旦数据库发生改变的时候调用的方法，重新获取加锁应用包名集合
         *
         * @param selfChange
         */
        @Override
        public void onChange(boolean selfChange) {
            new Thread() {
                @Override
                public void run() {
                    mPackageNameList = appLockDao.findAll();
                }
            }.start();
            super.onChange(selfChange);
        }
    }


    /**
     * 检测加锁应用已经解锁的广播接收者
     */
    private class InnerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取发送广播传递过来的包名
            mSkipPackageName = intent.getStringExtra("packagename");
            Log.i(tag, "解锁的包名为：" + mSkipPackageName);
        }
    }

    @Override
    public void onDestroy() {
        //停止看门狗的循环
        isWatch = false;
        //注销广播接收者
        if (mInnerReceiver != null) {
            unregisterReceiver(mInnerReceiver);
        }
        if (mContentObserver != null) {
            getContentResolver().unregisterContentObserver(mContentObserver);
        }
        //注销内容观察者
        super.onDestroy();
    }

    public void watch() {
        //1.子线程开启一个循环
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获取加锁应用集合集合
                mPackageNameList = appLockDao.findAll();
                Log.i(tag, "所有已加锁应用：");
                for (String string : mPackageNameList) {
                    Log.i(tag, "" + string);
                }
                while (isWatch) {
                    //2.检测正在开启的应用，任务栈
                    //3.获取Activity的管理者对象
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    //4.获取正在运行的任务栈
                    List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
                    ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
                    //5.获取栈顶的应用的包名
                    packageName = runningTaskInfo.topActivity.getPackageName();
                    Log.i(tag, "栈顶应用......" + packageName);
                    //6.拿此包名和加锁应用集合做对比
                    if (mPackageNameList.contains(packageName)) {
                        //如果检测到当前应用程序已经解锁，则不再拦截
                        if (!packageName.equals(mSkipPackageName)) {
                            //7.弹出拦截界面
                         /*
                        * 开启Activity的四种模式
                        * 1.stander标准
                        * 2.singleTop
                        * 3.singleTask
                        * 4.singleInstance
                        *
                        * 在EnterPsdActivity的activity注册代码中声明开启任务栈的方式为singleInstance
                        * */
                            Intent intent = new Intent(getApplicationContext(), EnterPsdActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("packagename", packageName);
                            startActivity(intent);
                        }
                    }
                    //睡眠一下
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
