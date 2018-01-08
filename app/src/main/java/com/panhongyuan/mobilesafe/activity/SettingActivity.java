package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.service.AddressService;
import com.panhongyuan.mobilesafe.service.BlackNumberService;
import com.panhongyuan.mobilesafe.service.WatchDogService;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.ServiceUtil;
import com.panhongyuan.mobilesafe.utils.SpUtil;
import com.panhongyuan.mobilesafe.view.SettingClickView;
import com.panhongyuan.mobilesafe.view.SettingItemView;

/**
 * Created by pan on 17-3-10.
 */

public class SettingActivity extends Activity {

    private String[] mToastStyleDes;
    private int mToastStyle;
    private SettingClickView scv_toast_style;
    private SettingClickView scv_toast_location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        //初始化更新方法
        initUpdate();
        //是否现实电话号码对属地的方法
        initAddress();
        //初始化来电显示控件
        initToastStyle();
        //归属地显示位置设置
        initLocation();
        //黑名单设置
        initBlackNumber();
        // 程序锁设置
        initAppLock();
    }

    /**
     * 程序锁设置
     */
    private void initAppLock() {
        final SettingItemView siv_app_lock = (SettingItemView) findViewById(R.id.siv_app_lock);
        boolean isRunning = ServiceUtil.isRunning(getApplicationContext(), "com.panhongyuan.mobilesafe.service.WatchDogService");
        siv_app_lock.setCheck(isRunning);

        siv_app_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_app_lock.isCheck();
                siv_app_lock.setCheck(!isCheck);
                if (!isCheck) {
                    //开启服务
                    startService(new Intent(getApplicationContext(), WatchDogService.class));
                } else {
                    stopService(new Intent(getApplicationContext(), WatchDogService.class));
                }
            }
        });
    }

    /**
     * 黑名单设置的实现
     */
    private void initBlackNumber() {
        final SettingItemView siv_blacknumber = (SettingItemView) findViewById(R.id.siv_blacknumber);
        boolean isRunning = ServiceUtil.isRunning(getApplicationContext(), "com.panhongyuan.mobilesafe.service.BlackNumberService");
        siv_blacknumber.setCheck(isRunning);

        siv_blacknumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_blacknumber.isCheck();
                siv_blacknumber.setCheck(!isCheck);
                if (!isCheck) {
                    //开启服务
                    startService(new Intent(getApplicationContext(), BlackNumberService.class));
                } else {
                    stopService(new Intent(getApplicationContext(), BlackNumberService.class));
                }
            }
        });
    }

    /**
     * 归属地设置的位置设置实现
     */
    private void initLocation() {
        scv_toast_location = (SettingClickView) findViewById(R.id.scv_toast_location);
        scv_toast_location.setTitle("归属地提示框设置");
        scv_toast_location.setDes("设置归属地提示框的位置");

        scv_toast_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ToastLocationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initToastStyle() {
        scv_toast_style = (SettingClickView) findViewById(R.id.scv_toast_style);
        //设置描述内容
        scv_toast_style.setTitle("设置归属地的显示风格");
        //1.设置描述文字内容
        mToastStyleDes = new String[]{"透明", "橙色", "蓝色", "灰色", "绿色"};
        //2.获取Toast样式索引值
        mToastStyle = SpUtil.getInt(getApplicationContext(), ConstantValue.TOAST_STYLE, 0);
        //3.通过索引，获取字符串内容，显示给描述的内容控件
        scv_toast_style.setDes(mToastStyleDes[mToastStyle]);
        //4.监听点击事件，弹出对话框
        scv_toast_style.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //5.显示Toast样式设置对话框
                showToastStyleDialog();
            }
        });
    }

    /**
     * 显示Toast样式设置对话框的实现
     */
    private void showToastStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle("请选择归属地显示样式");
        /*选择单个条目的事件监听,参数：
        1.String类型的数组，即描述颜色文字的数组，
        2.弹出对话框的时候选中条目的索引值，
        3.点击某一个条目后触发的事件的监听着*/
        builder.setSingleChoiceItems(mToastStyleDes, mToastStyle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //（1.记录选中的值，2.关闭对话框,3.显示色值文字）
                SpUtil.putInt(getApplicationContext(), ConstantValue.TOAST_STYLE, which);
                dialog.dismiss();
                scv_toast_style.setDes(mToastStyleDes[which]);
            }
        });
        //  消极按钮
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 是否现实电话号码对属地的方法
     */
    private void initAddress() {
        final SettingItemView siv_address = (SettingItemView) findViewById(R.id.siv_address);
        boolean isRunning = ServiceUtil.isRunning(getApplicationContext(), "com.panhongyuan.mobilesafe.service.AddressService");
        //对服务是否开启的状态作显示
        siv_address.setCheck(isRunning);
        //点击过程中的状态切换过程
        siv_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取之前的显示状态
                boolean check = siv_address.isCheck();
                //将现有的状态取反，切换状态
                siv_address.setCheck(!check);
                if (!check) {
                    //开启服务
                    startService(new Intent(getApplicationContext(), AddressService.class));
                } else {
                    //关闭服务
                    stopService(new Intent(getApplicationContext(), AddressService.class));
                }
            }
        });
    }

    /**
     * 初始化更新的实现
     */
    private void initUpdate() {
        final SettingItemView siv_update = (SettingItemView) findViewById(R.id.siv_update);
        //获取已有的开关状态，用作显示
        boolean open_update = SpUtil.getBoolean(this, ConstantValue.OPEN_UPDATE, false);
        //是否显示，根据上一次结果做决定
        siv_update.setCheck(open_update);
        siv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_update.isCheck();
                //将原有选中状态取反
                siv_update.setCheck(!isCheck);
                //将取反的状态传入到SP中
                SpUtil.putBoolean(getApplication(), ConstantValue.OPEN_UPDATE, !isCheck);
            }
        });
    }
}
