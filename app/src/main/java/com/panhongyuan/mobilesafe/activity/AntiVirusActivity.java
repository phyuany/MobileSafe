package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.engine.VirusDao;
import com.panhongyuan.mobilesafe.utils.Md5Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by pan on 4/11/17.
 */

public class AntiVirusActivity extends Activity {

    final private int SCANNING = 100;
    final private int FINISH = 101;
    private LinearLayout ll_add_text;
    private ProgressBar pb_bar;
    private TextView tv_name;
    private ImageView iv_scanning;
    private int index = 0;


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCANNING:
                    //1.显示正在扫描应用的名称
                    ScanInfo info = (ScanInfo) msg.obj;
                    tv_name.setText(info.name);
                    //2.在线性布局中添加一个正在扫描的应用的TextView
                    TextView textView = new TextView(getApplicationContext());
                    if (info.isVirus) {
                        //是病毒
                        textView.setTextColor(Color.RED);
                        textView.setText("发现病毒：" + info.name);
                    } else {
                        //不是病毒
                        textView.setTextColor(Color.BLACK);
                        textView.setText("扫描安全：" + info.name);
                    }
                    //在LinearLayout顶端添加TextView
                    ll_add_text.addView(textView, 0);
                    break;
                case FINISH:
                    tv_name.setText("扫描完成");
                    //停止正在执行的旋转动画
                    iv_scanning.clearAnimation();
                    //告知用户卸载包含病毒的应用
                    unInstallVirus();
                    break;
            }
        }
    };

    /**
     * 卸载病毒
     */
    private void unInstallVirus() {
        for (ScanInfo scanInfo : mVirusScanInfoList) {
            String packageName = scanInfo.packageName;
            //卸载应用
            Intent intent = new Intent("android.intent.action.DELETE");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
        }
    }

    private List<ScanInfo> mVirusScanInfoList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anit_virus);

        //初始化UI
        initUI();
        //初始化动画
        initAnimation();
        //查询病毒
        checkVirus();
    }

    private void checkVirus() {
        new Thread() {
            @Override
            public void run() {
                //获取数据库中所有病毒的md5码
                List<String> virusList = VirusDao.getVirusList();

                //获取手机应用签名文件的md5码
                //1.获取包管理对象
                PackageManager pm = getPackageManager();
                //2.获取所有应用的签名文件（GET_SIGNATURES已安装文件的签名文件+GET_UNINSTALLED_PACKAGES卸载玩应用的残余文件）
                List<PackageInfo> packageInfoList = pm.getInstalledPackages(PackageManager.GET_SIGNATURES + PackageManager.GET_UNINSTALLED_PACKAGES);

                //设置进度条最大值
                pb_bar.setMax(packageInfoList.size());

                //3.遍历应用的集合
                //记录所有应用的集合
                List<ScanInfo> scanInfoList = new ArrayList<>();
                //创建记录病毒的集合
                mVirusScanInfoList = new ArrayList<>();

                for (PackageInfo packageInfo : packageInfoList) {

                    ScanInfo scanInfo = new ScanInfo();

                    //获取签名文件的数组
                    Signature[] signatures = packageInfo.signatures;
                    //获取签名文件的第一位，然后进行md5,将此md5和病毒数据库中的md5做对比
                    Signature signature = signatures[0];
                    String string = signature.toCharsString();
                    //32位的字符串，16进制字符
                    String encoder = Md5Util.encoder(string);


                    //4.比对应用是否为病毒
                    if (virusList.contains(encoder)) {
                        //5.记录病毒
                        scanInfo.isVirus = true;
                        mVirusScanInfoList.add(scanInfo);
                    } else {
                        scanInfo.isVirus = false;
                    }
                    //6.维护对象的包名以及应用名称
                    scanInfo.packageName = packageInfo.packageName;
                    scanInfo.name = packageInfo.applicationInfo.loadLabel(pm).toString();
                    //把应用添加到已扫描的集合中
                    scanInfoList.add(scanInfo);
                    //7.扫描玩应用，需要更新进度条
                    index++;
                    pb_bar.setProgress(index);

                    //睡眠代码
                    try {
                        //取值任意扫描时间50到149毫秒
                        Thread.sleep(50 + new Random().nextInt(100));
                    } catch (Exception e) {

                    }

                    //8.在子线程中发送告知主线程更新UI(1.顶部扫描应用的名称,2.扫描过程中往线性布局添加View)
                    Message msg = Message.obtain();
                    msg.what = SCANNING;
                    msg.obj = scanInfo;

                    mHandler.sendMessage(msg);

                }
                //9.扫描结束
                Message msg = Message.obtain();
                msg.what = FINISH;

                mHandler.sendMessage(msg);
            }
        }.start();
    }

    class ScanInfo {
        public boolean isVirus;
        public String packageName;
        public String name;
    }

    /**
     * 初始化动画的方法
     */
    private void initAnimation() {
        RotateAnimation rotateAnimation = new RotateAnimation(
                0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        rotateAnimation.setDuration(1000);
        //指定动画一直旋转
        rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
        //保持动画结束后的状态
        rotateAnimation.setFillAfter(true);

        //执行动画
        iv_scanning.startAnimation(rotateAnimation);
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        //找到控件
        iv_scanning = (ImageView) findViewById(R.id.iv_scanning);
        tv_name = (TextView) findViewById(R.id.tv_name);
        pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
        ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);


    }
}
