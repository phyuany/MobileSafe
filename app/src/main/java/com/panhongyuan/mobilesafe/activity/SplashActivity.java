package com.panhongyuan.mobilesafe.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;
import com.panhongyuan.mobilesafe.utils.StreamUtil;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    /**
     * 更新新版本的状态码
     */
    private static final int UPDATE_VERSION = 100;
    /**
     * 进入主界面的状态码
     */
    private static final int ENTER_HOME = 101;
    /**
     * 异常状态码
     */
    private static final int IO_ERROR = 102;
    /**
     * JSON解析异常
     */
    private static final int JSON_ERROR = 103;

    private TextView tv_version_name;
    private int mLocalVersionCode;
    private String tag = "SplashActivity";
    private String versionDes;
    private String downloadUrl;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VERSION:
                    //弹出对话框，提示用户更新
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    //进入应用程序主界面
                    enterHome();
                    break;
                case IO_ERROR:
                    ToastUtil.show(getApplicationContext(), "IO_ERROR，IO读取异常");
                    enterHome();
                    break;
                case JSON_ERROR:
                    ToastUtil.show(getApplicationContext(), "JSON_ERROR，JSON解析异常");
                    enterHome();
                    break;
            }
        }
    };
    private RelativeLayout rl_root;


    /**
     * 弹出对话框，提示用户更新
     */
    private void showUpdateDialog() {
        //对话框依赖于activity存在
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("版本更新");
        //设置描述内容
        builder.setMessage(versionDes);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //下载apk,03.08,18:25
                downloadApk();
            }
        });
        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //取消对话框，进入主界面
                enterHome();
            }
        });
        //点击取消的事件
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //即使用户点击取消，也让用户进入程序的主界面
                enterHome();
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void downloadApk() {
        //apk的下载链接地址，放置apk的所在路径

        //1.判断SD卡是否挂载
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //2.获取SD卡路径
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "mobileSafe.apk";
            //3.发送请求获取apk,并且放到指定路径中
            HttpUtils httpUtils = new HttpUtils();
            //4.发送请求，传递参数（下载地址，存放路径）
            httpUtils.download(downloadUrl, path, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    //下载成功
                    Log.i(tag, "下载成功");
                    File result = responseInfo.result;
                    //安装apk
                    installApk(result);
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    //下载失败
                    Log.i(tag, "下载失败");
                }

                @Override
                public void onStart() {
                    super.onStart();
                    Log.i(tag, "开始下载");
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    super.onLoading(total, current, isUploading);
                    Log.i(tag, "下载中......");
                    Log.i(tag, "total......" + total);
                    Log.i(tag, "current......" + current);
                }
            });
        }
    }


    /**
     * 安装apk
     *
     * @param file 安装的文件
     */
    private void installApk(File file) {
        //进入应用安装界面
        String fileName = file.getAbsolutePath();
        Uri uri = Uri.fromFile(new File(fileName));
        //开启intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //设置安装数据和安装类型
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        /*
        *或者把intent.setDataAndType(data,type)分开写;
        * */
//        startActivity(intent);
        //开启有返回结果的Intent
        startActivityForResult(intent, 0);
    }

    /**
     * 开启一个Activity的返回结果
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //当开启安装的界面返回后，执行进入主界面的方法
        enterHome();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 进入程序主界面的实现
     */
    private void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        //开启新界面后关闭导航界面
        finish();
        //开启滑屏动画
        overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //初始化UI
        initUI();
        //初始化数据
        initData();
        //初始化动画
        initAnimation();
        //初始化数据库
        initDB();
        //生成快捷方式
        initShotCut();
    }

    /**
     * 生成快捷方式
     */
    private void initShotCut() {

        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //快捷方式的名称
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        shortcut.putExtra("duplicate", false); //不允许重复创建

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        //快捷方式的图标
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

        sendBroadcast(shortcut);
    }

    /**
     * 初始化数据库
     */
    private void initDB() {
        //1.归属地数据库初始化
        initDB("address.db");
        //2.常用号码数据库拷贝过程
        initDB("commonnum.db");
        //3.拷贝病毒数据库
        initDB("antivirus.db");
    }

    /**
     * 初始化,拷贝数据库到files文件夹下面
     *
     * @param dbName
     */
    private void initDB(String dbName) {
        //在files文件夹下创建同名数据库文件
        File filesDir = getFilesDir();
        File file = new File(filesDir, dbName);
        if (file.exists()) {
            return;
        }
        //2.读取第三方资产目录下的文件
        InputStream stream = null;
        FileOutputStream fos = null;
        try {
            stream = getAssets().open(dbName);
            //3.将读取的内容写入到指定文件夹的文件去
            fos = new FileOutputStream(file);
            //4.每次的读取内容大小
            byte[] bytes = new byte[1024];
            //临时变量
            int tmp = -1;
            while ((tmp = stream.read(bytes)) != -1) {
                fos.write(bytes, 0, tmp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (stream != null && fos != null) {
                try {
                    stream.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 初始化动画的实现
     */
    private void initAnimation() {
        //由完全透明到完全不透明
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        //设置动画时间为3秒
        alphaAnimation.setDuration(3000);
        //给布局设置动画
        rl_root.startAnimation(alphaAnimation);
    }

    /**
     * 初始化数据的方法
     */
    private void initData() {
        //1.获取用户版本名称
        tv_version_name.setText(getVersionName());
        //2.检测是否有更新，如果右更新，提示用户下载
        mLocalVersionCode = getVersionCode();
        //3.获取服务器版本号（客户端发请求，服务端发响应，使用json或者xml）,服务器返回200代表成功，以流的形式返回数据
        /*更新版本的名称
        * 新版本描述信息
        * 服务器版本号
        * apk下载地址*/
        //判断是否开启更新功能，如果开启，即进行更新
        if (SpUtil.getBoolean(this, ConstantValue.OPEN_UPDATE, false)) {
            checkVersion();
        } else {
            //senEmptyMessageDelayed()方法可发送状态码，并且handler对象接受到之后延时处理
            mHandler.sendEmptyMessageDelayed(ENTER_HOME, 4000);
        }
    }

    /**
     * 检测版本号
     */
    private void checkVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                long startTime = System.currentTimeMillis();//记录开始请求网络的时间戳
                try {
                    //1.封装URL,发送请求获取数据，参数为json的链接地址,10.0.2.2为模拟器访问本机tomcat的可用ip
                    URL url = new URL(ConstantValue.StaticStingValues.UPDATE_URL);
                    //2.开启一个链接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //3.设置常见请求头
                    //请求超时
                    connection.setConnectTimeout(2000);
                    //c读取超时
                    connection.setReadTimeout(2000);
                    //默认为post请求方式
                    //4.获取请求成功的响应码
                    if (connection.getResponseCode() == 200) {
                        //5.以流的方式保存下来
                        InputStream is = connection.getInputStream();
                        //6.将流转换成字符串
                        String json = StreamUtil.streamToString(is);
                        Log.i(tag, json);
                        //7.json解析
                        JSONObject jsonObject = new JSONObject(json);
                        String versionName = jsonObject.getString("versionName");
                        versionDes = jsonObject.getString("versionDes");
                        String versionCode = jsonObject.getString("versionCode");
                        downloadUrl = jsonObject.getString("downloadUrl");

                        Log.i(tag, versionName);
                        Log.i(tag, versionDes);
                        Log.i(tag, versionCode);
                        Log.i(tag, downloadUrl);

                        //8.服务器版本号与本地版本号进行对比
                        if (mLocalVersionCode < Integer.parseInt(versionCode)) {
                            //提示用户更新
                            msg.what = UPDATE_VERSION;
                        } else {
                            //进入主界面
                            msg.what = ENTER_HOME;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what = IO_ERROR;
                } catch (JSONException e) {
                    e.printStackTrace();
                    msg.what = JSON_ERROR;
                } finally {
                    //指定睡眠时间，请求网络小于4秒，强制其睡眠4秒
                    long endTime = System.currentTimeMillis();//记录请求网络结束时的时间戳
                    if (endTime - startTime < 3000) {
                        try {
                            Thread.sleep(3000 - (endTime - startTime));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendMessage(msg);
                    }
                }
            }

        }).start();
    }

    /**
     * 返回版本号
     *
     * @return 非0则代表获取成功
     */
    private int getVersionCode() {
        //1.获取包管理对象
        PackageManager pm = getPackageManager();
        //2.从包管理对象中获取版本信息.第二个参数是0,代表基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            //3.获取对应版本名称
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取版本名称
     *
     * @return 返回应用版本名称，返回null代表异常
     */
    private String getVersionName() {
        //1.获取包管理对象
        PackageManager pm = getPackageManager();
        //2.从包管理对象中获取版本信息.第二个参数是0,代表基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            //3.获取对应版本名称
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化UI的方法
     */
    private void initUI() {
        tv_version_name = (TextView) findViewById(R.id.tv_version_name);
        rl_root = (RelativeLayout) findViewById(R.id.rl_root);
    }
}
