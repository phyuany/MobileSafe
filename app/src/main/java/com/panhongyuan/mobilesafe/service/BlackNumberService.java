package com.panhongyuan.mobilesafe.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.panhongyuan.mobilesafe.db.dao.BlackNumberDao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by pan on 17-4-2.
 */

public class BlackNumberService extends Service {

    private InnerSmsReceiver mInnerSmsReceiver;
    private BlackNumberDao mDao;
    private TelephonyManager mTm;
    private MyPhoneStateListener myPhoneStateListener;
    private MyContentObserver mContentObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mDao = BlackNumberDao.getInstance(getApplicationContext());

        //1.拦截短信
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        //设置优先级
        intentFilter.setPriority(1000);

        //短信接收者对象
        mInnerSmsReceiver = new InnerSmsReceiver();
        //注册监听事件
        registerReceiver(mInnerSmsReceiver, intentFilter);

        //2.拦截电话
        //1.获取电话管理者对象
        mTm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //2.监听电话状态
        myPhoneStateListener = new MyPhoneStateListener();
        mTm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        super.onCreate();
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
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //一旦响铃，就挂断电话，aidl文件中，挂断电话号码的方法放到了aidl文件中
                    endCall(incomingNumber);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    /**
     * 拦截电话的实现
     */
    private void endCall(String phone) {
        int mode = mDao.getMode(phone);
        //android.os.ServiceManager方法对开发者隐藏，所以不能直接调用，需要反射调用
        if (mode == 2 || mode == 3) {
            //拦截电话
            try {
                //1.获取ServiceManager字节码文件
                Class<?> clazz = Class.forName("android.os.ServiceManager");//可能出现InvocationTargetException
                //2.获取方法
                Method method = clazz.getMethod("getService", String.class);//可能出现NoSuchMethodException
                //3.反射调用此方法
                IBinder iBinder = (IBinder) method.invoke(null, TELEPHONY_SERVICE);//可能出现IllegalAccessException
                //4.调用获取aidl文件对象的方法
                ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
                //5.调用在aidl中隐藏的endCall方法
                iTelephony.endCall();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        //电话拦截之后，删除被拦截的电话的记录
        //6.在内容解析器上注册内容观察者，通过内容观察者观察数据库的变化
        mContentObserver = new MyContentObserver(new Handler(), phone);
        getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"), true, mContentObserver);
    }

    /**
     * 定义内容观察者
     */
    private class MyContentObserver extends ContentObserver {
        private final String phone;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public MyContentObserver(Handler handler, String phone) {
            super(handler);
            this.phone = phone;
        }

        /**
         * 数据库中指定calls表发生改变之后调用此方法
         *
         * @param selfChange
         */
        @Override
        public void onChange(boolean selfChange) {
            //插入一条数据后进行删除,下一行代码需要增加android.permission.WRITE_CALL_LOG权限
            getContentResolver().delete(Uri.parse("content://call_log/calls"), "number = ?", new String[]{phone});
            super.onChange(selfChange);
        }
    }

    @Override
    public void onDestroy() {
        //取消注册广播接收者
        if (mInnerSmsReceiver != null) {
            unregisterReceiver(mInnerSmsReceiver);
        }
        //注销内容观察者
        if (mContentObserver != null) {
            getContentResolver().unregisterContentObserver(mContentObserver);
        }
        //取消对电话状态的监听
        if (myPhoneStateListener != null) {
            mTm.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        super.onDestroy();
    }

    /**
     * 短信接收者类
     */
    private class InnerSmsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取短信内容，获取发送短信的号码，如果此号码在黑名单中，并且拦截模式位1或3，则拦截短信
            //1.获取短信内容
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            //2.循环遍历短信
            for (Object object : objects) {
                //3.获取短信对象
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) object);
                //4.获取短信对象的基本信息
                //获取号码
                String originatingAddress = sms.getOriginatingAddress();
               /*
               获取的短信内容
               String messageBody = sms.getMessageBody();
              */
                int mode = mDao.getMode(originatingAddress);

                if (mode == 1 || mode == 3) {
                    //拦截短信,终止广播，使系统短信应用也接收不到短信，拦截短信(android 4.4版本失效	短信数据库,删除)
                    abortBroadcast();
                }
            }
        }
    }

}
