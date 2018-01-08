package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.service.LockScreenService;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.ServiceUtil;
import com.panhongyuan.mobilesafe.utils.SpUtil;

/**
 * Created by pan on 17-4-8.
 */

public class ProcessSettingActivity extends Activity {

    private CheckBox cb_show_system;
    private CheckBox cb_lock_clear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_setting);

        initSystemShow();

        initLockScreenClear();
    }

    /**
     * 锁屏清理
     */
    private void initLockScreenClear() {
        cb_lock_clear = (CheckBox) findViewById(R.id.cb_lock_clear);

        //根据锁屏清理服务是否开启去决定单选框是否选中
        boolean isRunning = ServiceUtil.isRunning(getApplicationContext(), "com.panhongyuan.mobilesafe.service.LockScreenService");
        if (isRunning) {
            cb_lock_clear.setText("锁屏清理已开启");
        } else {
            cb_lock_clear.setText("锁屏清理已关闭");
        }
        //选中状态的维护
        cb_lock_clear.setChecked(isRunning);

        //对他的选中状态进行监听
        cb_lock_clear.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //isCheck作为是否选中的状态
                if (isChecked) {
                    cb_lock_clear.setText("锁屏清理已开启");
                    //开启服务
                    startService(new Intent(getApplicationContext(), LockScreenService.class));
                } else {
                    cb_lock_clear.setText("锁屏清理已开启");
                    //关闭服务
                    stopService(new Intent(getApplicationContext(), LockScreenService.class));
                }
            }
        });
    }

    private void initSystemShow() {
        cb_show_system = (CheckBox) findViewById(R.id.cb_show_system);

        //对之前进行存储的状态进行回显
        boolean showSystem = SpUtil.getBoolean(getApplicationContext(), ConstantValue.SHOW_SYSTEM, false);
        if (showSystem) {
            cb_show_system.setText("显示系统进程");
        } else {
            cb_show_system.setText("隐藏系统进程");
        }

        //回显单选狂的选中状态
        cb_show_system.setChecked(showSystem);

        //对他的选中状态进行监听
        cb_show_system.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //isCheck作为是否选中的状态
                if (isChecked) {
                    cb_show_system.setText("显示系统进程");
                } else {
                    cb_show_system.setText("隐藏系统进程");
                }
                SpUtil.putBoolean(getApplicationContext(), ConstantValue.SHOW_SYSTEM, isChecked);
            }
        });
    }
}
