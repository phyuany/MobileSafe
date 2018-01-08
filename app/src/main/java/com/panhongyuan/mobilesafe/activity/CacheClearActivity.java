package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

/**
 * Created by pan on 4/13/17.
 */

public class CacheClearActivity extends Activity {

    private int index = 0;
    private LinearLayout ll_add_text;
    private TextView tv_name;
    private ProgressBar pb_bar;
    private Button bt_clear;
    private IPackageStatsObserver.Stub mStatsObserver;
    private String tag = "CacheClearActivity";
    private PackageManager mPm;
    final private int UPDATE_CACHE_APP = 100;//更新UI的状态码
    final private int CHECK_CACHE_APP = 101;//检查缓存更新UI状态码
    final private int CHECK_FINISH = 102;//检查缓存结束状态码
    final private int CLEAN_CACHE = 103;//清楚缓存结束的状态码
    final private int CHECK_STAND_ALONE_FINISH = 104;//清除单独的app缓存结束的状态码

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_CACHE_APP:
                    //8.在线性布局中添加有缓存应用的条目
                    final View view = View.inflate(getApplicationContext(), R.layout.linearlayout_cache_item, null);
                    ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
                    TextView name = (TextView) view.findViewById(R.id.tv_name);
                    TextView tv_memory_info = (TextView) view.findViewById(R.id.tv_memory_info);
                    ImageView iv_delete = (ImageView) view.findViewById(R.id.iv_delete);

                    final CacheInfo cacheInfo = (CacheInfo) msg.obj;
                    iv_icon.setBackgroundDrawable(cacheInfo.icon);
                    name.setText(cacheInfo.name);
                    tv_memory_info.setText(Formatter.formatFileSize(getApplicationContext(), cacheInfo.cacheSize));

                    ll_add_text.addView(view, 0);

                    //注册清楚单个选中应用的缓存内容
                    iv_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            //反射调用deleteApplicationCacheFiles
                            //反射调用
                            try {
                                //1.获取指定类字节码文件
                                Class<?> aClass = Class.forName("android.content.pm.PackageManager");
                                //2.获取调用的方法对象
                                Method method = aClass.getMethod("deleteApplicationCacheFiles", String.class, IPackageDataObserver.class);

                                //3.获取对象调用方法
                                method.invoke(mPm, cacheInfo.packageName, new IPackageDataObserver.Stub() {
                                    @Override
                                    public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                                        //删除单个应用缓存结束后调用的方法，需要android.permission.DELETE_CACHE_FILES权限
                                        Message msg = Message.obtain();
                                        msg.what = CHECK_STAND_ALONE_FINISH;
                                        msg.obj = view;
                                        mHandler.sendMessage(msg);
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    break;
                case CHECK_CACHE_APP:
                    tv_name.setText((String) msg.obj);
                    break;
                case CHECK_FINISH:
                    tv_name.setText("扫描完成");
                    break;
                case CLEAN_CACHE:
                    //从线性布局中清楚所有条目
                    ll_add_text.removeAllViews();
                    ToastUtil.show(getApplicationContext(), "已清理所有应用缓存");
                    break;
                case CHECK_STAND_ALONE_FINISH:
                    //提示用户清理缓存成功，并把当前清理缓存的条目删除
                    Log.i(tag, "删除单个应用缓存成功");
                    ToastUtil.show(getApplicationContext(),"已清理当前应用缓存");
                    View viewItem = (View) msg.obj;
                    //删除当前点击的条目
                    ll_add_text.removeView(viewItem);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chache_clear);

        initUI();

        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
//        在子线程
        new Thread() {
            @Override
            public void run() {
                //1.获取包管理对象
                PackageManager pm = getPackageManager();
                //2.获取手机安装的所有应用
                List<PackageInfo> installedPackages = pm.getInstalledPackages(0);
                //3.设置进度条的最大值
                pb_bar.setMax(installedPackages.size());
                //4.遍历每一个应用，获取缓存信息（名称，缓存大小，图标，包名）
                for (PackageInfo packageInfo : installedPackages) {
                    //包名作为查询缓存信息的必要条件
                    String packageName = packageInfo.packageName;
                    //获取应用缓存大小
                    getPackageCache(packageName);

                    //无时间规律地更新UI
                    try {
                        Thread.sleep(10 + new Random().nextInt(200));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    index++;
                    pb_bar.setProgress(index);

                    //每循环一次，就将我们检测应用的名称发给主线程显示
                    Message msg = Message.obtain();
                    msg.what = CHECK_CACHE_APP;
                    String name = null;
                    try {
                        name = mPm.getApplicationInfo(packageName, 0).loadLabel(mPm).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    msg.obj = name;
                    mHandler.sendMessage(msg);
                }
                Message msg = Message.obtain();
                msg.what = CHECK_FINISH;
                mHandler.sendMessage(msg);
            }
        }.start();

    }

    /**
     * 通过应用包名获取应用缓存大小
     *
     * @param packageName
     */
    private void getPackageCache(String packageName) {
        //调用API获取应用缓存大小
        mPm = getPackageManager();

        mStatsObserver = new IPackageStatsObserver.Stub() {
            @Override
            public void onGetStatsCompleted(PackageStats pStats,
                                            boolean succeeded) {
                //4.获取缓存大小
                long cacheSize = pStats.cacheSize;
                //5.判断应用缓存大小是否大于0
                CacheInfo cacheInfo = null;
                if (cacheSize > 0) {
                    //6.告诉主线程更新UI
                    Message msg = Message.obtain();
                    msg.what = UPDATE_CACHE_APP;

                    try {
                        //7.维护有缓存应用的javaBean
                        cacheInfo = new CacheInfo();
                        cacheInfo.cacheSize = cacheSize;
                        cacheInfo.packageName = pStats.packageName;
                        cacheInfo.name = mPm.getApplicationInfo(pStats.packageName, 0).loadLabel(mPm).toString();
                        cacheInfo.icon = mPm.getApplicationInfo(pStats.packageName, 0).loadIcon(mPm);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //带数据发送消息
                    msg.obj = cacheInfo;
                    mHandler.sendMessage(msg);
                }
            }
        };

        //参数1：获取缓存应用的包名，2.aidl文件指向类对应对象,以下方法对开发者不开放，需要反射调用
        // mPm.getPackageSizeInfo("com.android.browser", mStatsObserver);

        //反射调用
        try {
            //1.获取指定类字节码文件
            Class<?> aClass = Class.forName("android.content.pm.PackageManager");
            //2.获取调用的方法对象
            Method method = aClass.getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);

            //3.获取对象调用方法
            method.invoke(mPm, packageName, mStatsObserver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class CacheInfo {
        public String name;
        public Drawable icon;
        public String packageName;
        public long cacheSize;
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        bt_clear = (Button) findViewById(R.id.bt_clear);
        pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
        tv_name = (TextView) findViewById(R.id.tv_name);
        ll_add_text = (LinearLayout) findViewById(R.id.ll_add_text);

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //反射调用
                try {
                    //1.获取指定类字节码文件
                    Class<?> aClass = Class.forName("android.content.pm.PackageManager");
                    //2.获取调用的方法对象
                    Method method = aClass.getMethod("freeStorageAndNotify", long.class, IPackageDataObserver.class);

                    //3.获取对象调用方法
                    method.invoke(mPm, Long.MAX_VALUE, new IPackageDataObserver.Stub() {
                        @Override
                        public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                            //清楚缓存后调用的方法（清楚缓存需要调用权限）
                            Message msg = Message.obtain();
                            msg.what = CLEAN_CACHE;
                            mHandler.sendMessage(msg);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
