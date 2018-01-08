package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

/**
 * Created by pan on 17-4-10.
 */

public class EnterPsdActivity extends Activity {

    private String packageName;
    private TextView tv_app_name;
    private ImageView iv_app_icon;
    private Button bt_submit;
    private EditText et_pad;
    private String tag = "EnterPsdActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //获取包名
        packageName = getIntent().getStringExtra("packagename");
        Log.i(tag, "试图解锁的应用......." + packageName);
        setContentView(R.layout.activity_enter_psd);
        //初始化UI
        initUI();
        //初始化数据
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //获取传递过来的包名获取拦截应用的图标以及名称
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            Drawable icon = applicationInfo.loadIcon(pm);
            iv_app_icon.setBackgroundDrawable(icon);
            tv_app_name.setText(applicationInfo.loadLabel(pm).toString());

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String psd = et_pad.getText().toString();
                if (!TextUtils.isEmpty(psd)) {
                    if (psd.equals("123")) {
                        //解锁，进入应用，进入应用后，告知开门狗，不要再监听此监听的应用
                        finish();
                        //发送广播
                        Intent intent = new Intent("android.intent.action.SKIP");
                        intent.putExtra("packagename", packageName);
                        sendBroadcast(intent);
                    } else {
                        ToastUtil.show(getApplicationContext(), "密码错误");
                    }
                } else {
                    ToastUtil.show(getApplicationContext(), "请输入密码");
                }
            }
        });
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        tv_app_name = (TextView) findViewById(R.id.tv_app_name);
        iv_app_icon = (ImageView) findViewById(R.id.iv_app_icon);
        et_pad = (EditText) findViewById(R.id.et_pad);
        bt_submit = (Button) findViewById(R.id.bt_submit);
    }


    /**
     * 重写回退按钮执行的方法
     */
    @Override
    public void onBackPressed() {
        //跳转到主界面
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
        //结束本界面
        finish();
        super.onBackPressed();
    }
}
