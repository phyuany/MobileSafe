package com.panhongyuan.mobilesafe.global;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by pan on 4/14/17.
 */

public class MyApplication extends Application {
    private String tag = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        //捕获全局异常，设置默认的没有捕获的异常
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                //在获取到未捕获的异常后处理的方法
                e.printStackTrace();
                Log.i(tag, "捕获到了一个程序的异常：" + e);
                //将捕获的异常存储到SD卡中
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "jkdev.log";
                File file = new File(path);

                try {
                    PrintWriter printWriter = new PrintWriter(file);
                    e.printStackTrace(printWriter);
                    printWriter.close();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
                //上传到程序员服务器中
                // 推出错误界面,结束应用
                System.exit(0);
            }
        });
    }
}
