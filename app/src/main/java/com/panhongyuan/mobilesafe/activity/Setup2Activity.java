package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;
import com.panhongyuan.mobilesafe.utils.ToastUtil;
import com.panhongyuan.mobilesafe.view.SettingItemView;

/**
 * Created by pan on 17-3-13.
 */

public class Setup2Activity extends BaseSetupActivity {

    private SettingItemView siv_sim_bound;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_activity2);
        //初始化控件
        initUI();
    }

    @Override
    protected void showNextPage() {
        String sim_number = SpUtil.getString(this, ConstantValue.SIM_NUMBER, "");
        if (!TextUtils.isEmpty(sim_number)) {
            Intent intent = new Intent(getApplicationContext(), Setup3Activity.class);
            startActivity(intent);
            finish();
            //开启下一页平移动画
            overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
        } else {
            ToastUtil.show(getApplicationContext(), "请绑定sim卡");
        }
    }

    @Override
    protected void showPrePage() {
        Intent intent = new Intent(getApplicationContext(), Setup1Activity.class);
        startActivity(intent);
        finish();
        //上一页的移动画
        overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
    }

    /**
     * 初始化控件
     */
    private void initUI() {
        siv_sim_bound = (SettingItemView) findViewById(R.id.siv_sim_bound);
        //1.读取已有绑定状态
        String sim_number = SpUtil.getString(this, ConstantValue.SIM_NUMBER, "");
        //2.判断序列卡号是否为空
        if (TextUtils.isEmpty(sim_number)) {
            siv_sim_bound.setCheck(false);
        } else {
            siv_sim_bound.setCheck(true);
        }
        //3.注册点击事件
        siv_sim_bound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //4.获取原有状态
                boolean isCheck = siv_sim_bound.isCheck();//内部类访问局部变量时，局部变量需要加final关键字，访问成员变量时，不需要加final关键字
                //5.将原有的状态取反，存储序列卡号
                siv_sim_bound.setCheck(!isCheck);
                if (!isCheck) {
                    //6.存储序列卡号
                    //6.1获取TelephoneManager
                    TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    //6.2获取对应的sim卡序列号
                    String simSerialNumber = manager.getSimSerialNumber();
                    //6.3存储序列号
                    SpUtil.putString(getApplicationContext(), ConstantValue.SIM_NUMBER, simSerialNumber);
                } else {
                    //7.将序列卡号从SP节点中删除
                    SpUtil.remove(getApplicationContext(), ConstantValue.SIM_NUMBER);
                }

            }
        });
    }
}
