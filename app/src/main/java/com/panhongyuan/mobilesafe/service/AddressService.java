package com.panhongyuan.mobilesafe.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.engine.AddressDao;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

/**
 * Created by pan on 17-3-26.
 */

public class AddressService extends Service {

    private TelephonyManager mTm;
    private MyPhoneStateListener myPhoneStateListener;
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    private View mViwToast;
    private WindowManager mWM;
    private String mAddress;
    private TextView tv_toast;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            tv_toast.setText(mAddress);
            super.handleMessage(msg);
        }
    };
    private int[] mDrawables;
    private int mScreenHeight;
    private int mScreenWidth;
    private InnerOutCallReceiver mInnerOutCallReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //第一次开启，就需要去管理Toast对象
        //电话状态的监听(服务开启的时候需要监听，关闭的时候就不去监听)
        //1.获取电话管理者对象
        mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //2.监听电话状态
        myPhoneStateListener = new MyPhoneStateListener();
        mTm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        //获取窗体对象
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);

        //获取屏幕管理者对象，在获取屏幕的宽和高
        WindowManager mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenWidth = mWM.getDefaultDisplay().getWidth();
        mScreenHeight = mWM.getDefaultDisplay().getHeight();

        //监听拨出电话的广播，创建过滤器对象,通过代码，对广播接收者注册
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        //创建广播接收者
        mInnerOutCallReceiver = new InnerOutCallReceiver();
        //注册广播接收者，参数：1.广播接收者对象，2.过滤条件。需要注册权限
        registerReceiver(mInnerOutCallReceiver, intentFilter);

        super.onCreate();
    }

    /**
     * 拨出电话的广播接收者
     */
    private class InnerOutCallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //接收到拨出电话的广播后，显示归属地
            //获取拨出电话号码的字符串
            String resultData = getResultData();
            //显示到
            showToast(resultData);
        }
    }

    @Override
    public void onDestroy() {
        //取消电话状态的监听,参数：开启服务的时候的电话监听对象，
        //作非空判断
        if (mTm != null && myPhoneStateListener != null) {
            mTm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        //销毁服务时要取消注册电话拨出广播接收者
        if (mInnerOutCallReceiver != null) {
            unregisterReceiver(mInnerOutCallReceiver);
        }
        super.onDestroy();
    }

    /**
     * 电话拨出广播接收者对象
     */
    private class MyPhoneStateListener extends PhoneStateListener {
        //重写电话改变状态的监听方法,参数：1.状态，2.来电号码
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    //电话空闲状态,挂电话之后移除View对象
                    if (mViwToast != null) {
                        mWM.removeView(mViwToast);
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //摘机状态，至少有一个电话活动:
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //响铃状态
                    showToast(incomingNumber);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    /**
     * 显示Toast
     *
     * @param incomingNumber 来电号码
     */
    private void showToast(String incomingNumber) {
        final WindowManager.LayoutParams params = mParams;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        //响铃时显示Toast,和电话类型一致
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.setTitle("Toast");
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE使其默认能被触摸
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //指定Toast所在位置(指定在左上角)
        params.gravity = Gravity.LEFT + Gravity.TOP;

        //指定显示效果(布局文件)，拿到View对象之后将其挂载到窗体WindowsManager对象中
        mViwToast = View.inflate(getApplicationContext(), R.layout.toast_view, null);
        //获取控件
        tv_toast = (TextView) mViwToast.findViewById(R.id.tv_toast);
        //在窗体上挂载一个View,需要增加权限"android.permission.SYSTEM_ALERT_WINDOW",即在窗体上挂载View的权限


        tv_toast.setOnTouchListener(new View.OnTouchListener() {
            private int startX;
            private int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //按下时
                        //int startX = (int) event.getX();//以自己作为参照，获取触摸的点x轴的坐标值
                        //以屏幕作为参照，距离原点的距离
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //移动时
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();

                        int disX = moveX - startX;
                        int disY = moveY - startY;

                        params.x = params.x + disX;
                        params.y = params.y + disY;

                        //容错处理
                        if (params.x < 0) {
                            params.x = 0;
                        }
                        if (params.y < 0) {
                            params.y = 0;
                        }
                        if (params.x > mScreenWidth - mViwToast.getWidth()) {
                            params.x = mScreenWidth - mViwToast.getWidth();
                        }
                        if (params.y > mScreenHeight - mViwToast.getHeight() - 22) {
                            params.y = mScreenHeight - mViwToast.getHeight() - 22;
                        }

                        //告知窗体Toast需要按手势的移动去更改
                        mWM.updateViewLayout(mViwToast, params);

                        //更新初始位置的值，以便给下一个滑动做参照
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP:
                        //保存Toast的位置
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, params.x);
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, params.y);
                        break;
                }
                //返回true，响应拖拽事件
                return true;
            }
        });

        //读取SP 中Toast位置存储的值,在设置给params的x 和 y值
        params.x = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
        params.y = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);

        //SP中获取文字索引,匹配图片
        mDrawables = new int[]{
                R.drawable.call_locate_white,
                R.drawable.call_locate_orange,
                R.drawable.call_locate_blue,
                R.drawable.call_locate_gray, R.drawable.call_locate_green
        };
        int toastStyleIndex = SpUtil.getInt(getApplicationContext(), ConstantValue.TOAST_STYLE, 0);
        tv_toast.setBackgroundResource(mDrawables[toastStyleIndex]);

        mWM.addView(mViwToast, params);
        //获取电话号码之后需要做来电查询
        query(incomingNumber);
    }

    private void query(final String incomingNumber) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAddress = AddressDao.getAddress(incomingNumber);
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }


}
