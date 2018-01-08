package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;

/**
 * Created by pan on 17-3-12.
 */

public class SetupOverActivity extends Activity {

    private TextView tv_phone;
    private TextView tv_reset_setup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //四个导航界面设置完成，停留在功能设置完成界面，如果没有设置完成，跳到设置导航界面第一页
        boolean setup_over = SpUtil.getBoolean(this, ConstantValue.SETUP_OVER, false);
        if (setup_over) {
            //停留在设置完成界面
            setContentView(R.layout.activity_setup_over);
            //初始化控件
            initUI();
        } else {
            //跳转到设置导航界面第一页
            Intent intent = new Intent(this, Setup1Activity.class);
            startActivity(intent);
            finish();
            //开启滑屏动画
            overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
        }
    }

    /**
     * 初始化控件
     */
    private void initUI() {
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        //设置联系人号码
        String phone = SpUtil.getString(this, ConstantValue.CONTACT_PHONE, "");
        tv_phone.setText(phone);
        //重新设置条目被点击
        tv_reset_setup = (TextView) findViewById(R.id.tv_reset_setup);
        tv_reset_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Setup1Activity.class);
                startActivity(intent);
                finish();
                //开启滑屏动画
                overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
            }
        });
    }
}
