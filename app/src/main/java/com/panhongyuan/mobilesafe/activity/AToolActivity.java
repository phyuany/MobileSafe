package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.engine.SmsBackUp;

import java.io.File;

/**
 * Created by pan on 17-3-25.
 */

public class AToolActivity extends Activity {

    private TextView tv_query_phone_address;
    private TextView tv_sms_backup;
    private ProgressBar pb_bar;
    private ProgressDialog progressDialog;
    private TextView tv_common_number_query;
    private TextView tv_app_lock;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atool);
        //电话归属地查询的方法
        initPhoneAddress();
        //短信备份的方法
        initSmsBackUp();
        //常用号码查询
        initCommonNumberQuery();
        //初始化程序锁
        initAppLock();
    }

    /**
     * 初始化程序锁的方法
     */
    private void initAppLock() {
        tv_app_lock = (TextView) findViewById(R.id.tv_app_lock);
        tv_app_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AppLockActivity.class));
            }
        });
    }

    /**
     * 常用号码查询
     */
    private void initCommonNumberQuery() {
        tv_common_number_query = (TextView) findViewById(R.id.tv_common_number_query);
        tv_common_number_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CommonNumberQueryActivity.class));
            }
        });
    }

    /**
     * 短信备份的实现
     */
    private void initSmsBackUp() {
        tv_sms_backup = (TextView) findViewById(R.id.tv_sms_backup);
        pb_bar = (ProgressBar) findViewById(R.id.pb_bar);
        tv_sms_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出短信备份对话框
                showSmsBackUpDialog();
            }
        });
    }

    /**
     * 弹出短信备份对话框
     */
    private void showSmsBackUpDialog() {
        //1.创建一个带进度条的对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.ic_launcher);
        progressDialog.setTitle("短信备份");
        //2.指定进度条的样式
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //3.展示进度条
        progressDialog.show();
        //4.调用短信的备份方法
        new Thread() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sms.xml";
                SmsBackUp.backup(getApplicationContext(), path, new SmsBackUp.CallBack() {
                    @Override
                    public void setMax(int max) {
                        //由开发者自己决定使用对话框还是进度条
                        progressDialog.setMax(max);
                        pb_bar.setMax(max);
                    }

                    @Override
                    public void setProgress(int index) {//中兴v5 MIUI在此处有bug,执行到第24条短信就会停止更新进度并且隐藏掉对话框
                        progressDialog.setProgress(index);
                        pb_bar.setProgress(index);
                    }
                });
                //关闭对话框
                progressDialog.dismiss();
            }
        }.start();
    }

    /**
     * 电话归属地查询的方法
     */
    private void initPhoneAddress() {
        tv_query_phone_address = (TextView) findViewById(R.id.tv_query_phone_address);
        tv_query_phone_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), QueryActivity.class);
                startActivity(intent);
            }
        });
    }
}
