package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

/**
 * Created by pan on 17-3-13.
 */

public class Setup3Activity extends BaseSetupActivity {

    private Button bt_select_number;//点击选择联系人的按钮
    private EditText et_phone_number;//输入号码的编辑框

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_activity3);
        //初始化控件
        initUI();
    }

    @Override
    protected void showNextPage() {
        String phone = et_phone_number.getText().toString();
//        String contact_phone = SpUtil.getString(getApplicationContext(), ConstantValue.CONTACT_PHONE, "");
        //在SP存储相关联系人之后可以进入下一页
        if (!TextUtils.isEmpty(phone)) {
            Intent intent = new Intent(getApplicationContext(), Setup4Activity.class);
            startActivity(intent);
            finish();
            //开启下一页平移动画
            overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
            //如果输入的是电话号码，则需要去保存
            SpUtil.putString(getApplicationContext(), ConstantValue.CONTACT_PHONE, phone);
        } else {
            ToastUtil.show(getApplicationContext(), "请输入电话号码");
        }
    }

    @Override
    protected void showPrePage() {
        Intent intent = new Intent(getApplicationContext(), Setup2Activity.class);
        startActivity(intent);
        finish();
        //上一页的移动画
        overridePendingTransition(R.anim.pre_in_anim, R.anim.pre_out_anim);
    }

    /**
     * 初始化控件的方法
     */
    private void initUI() {
        //输入号码的编辑框
        et_phone_number = (EditText) findViewById(R.id.et_phone_number);
        //获取联系人电话号码的回显
        String phone = SpUtil.getString(getApplicationContext(), ConstantValue.CONTACT_PHONE, "");
        et_phone_number.setText(phone);
        //点击选择联系人的按钮
        bt_select_number = (Button) findViewById(R.id.bt_select_number);
        bt_select_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            //带回结果时，返回到当前界面时接收到结果作处理
            String phone = data.getStringExtra("phone");
            //将特殊字符过滤,将中划线和文本中的空格过滤掉，在调用trim()方法过滤前端与后端空格
            phone = phone.replace("-", "").replace(" ", "").trim();
            //将phone显示到EditText
            et_phone_number.setText(phone);
            //3.存储联系人到SP中
            SpUtil.putString(getApplicationContext(), ConstantValue.CONTACT_PHONE, phone);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
