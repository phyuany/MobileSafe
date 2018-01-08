package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.engine.AddressDao;

/**
 * Created by pan on 17-3-25.
 */

public class QueryActivity extends Activity {
    private String tag = "QueryActivity";
    private EditText et_phone;
    private Button bt_query;
    private TextView tv_query_result;//用于现查询结果的TextView
    private String mAddress;//查询到的地址

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tv_query_result.setText(mAddress);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_address);
        //初始化控件
        initUI();
    }

    /**
     * 初始化控件
     */
    private void initUI() {
        et_phone = (EditText) findViewById(R.id.et_phone);
        bt_query = (Button) findViewById(R.id.bt_query);
        tv_query_result = (TextView) findViewById(R.id.tv_query_result);
        //1.点击查询按钮，注册监听事件
        bt_query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = et_phone.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    //2.开启子线程进行查询操作
                    query(phone);
                } else {
                    //如果文本为空，执行窗口抖动动画
                    Animation shake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
                    //interpolator插补器
                    /*//数学函数自定义插补器
                    shake.setInterpolator(new Interpolator() {
                        //y = ax + b
                        @Override
                        public float getInterpolation(float input) {
                            return 0;
                        }
                    });*/
                    et_phone.startAnimation(shake);
                    //手机震动效果
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    //震动500毫秒
                    vibrator.vibrate(500);
                    //有规律的震动，参数：1.震动的规则(不震动时间，震动时间，不震动时间，震动时间，不震动时间......)，2.震动的次数,-1代表不重复
                    //vibrator.vibrate(new long[]{2000,5000,200,3000},-1);
                }
            }
        });

        //实时查询，监听输入框的变化
        et_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //改变之前
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //改变时
            }

            @Override
            public void afterTextChanged(Editable s) {
                //改变之后
                String phone = et_phone.getText().toString();
                query(phone);
            }
        });
    }

    /**
     * 查询号码归属地的方法
     *
     * @param phone 需要查询的号码
     */
    private void query(final String phone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAddress = AddressDao.getAddress(phone);
                //3.消息机制，发送空消息，告知主线程立即更新TextView现实内容
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }
}
