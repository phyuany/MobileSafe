package com.panhongyuan.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;

/**
 * Created by pan on 17-3-17.
 */

public class BootReceiver extends BroadcastReceiver {
    private String tag = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(tag, "重启成功，并且监听到相关的广播");
        //1.获取开机后手机sim卡的序列号
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //以下一行为测试代码
        String simSerialNumber = tm.getSimSerialNumber() + "xxx";
        //2.获取SP中的sim卡序列号
        String sim_number = SpUtil.getString(context, ConstantValue.SIM_NUMBER, "");
        //3.对比之后如果不一致
        if (!simSerialNumber.equals(sim_number)) {
            //4.发送短信给选中号码的人
            SmsManager smsManager = SmsManager.getDefault();
            //第二个参数：scAddress，在中国不支持，所以传null
            smsManager.sendTextMessage(ConstantValue.CONTACT_PHONE, null, "你好，您的手机可能被盗,来自极客卫士", null, null);
        }
    }
}
