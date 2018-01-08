package com.panhongyuan.mobilesafe.receiver;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.service.LocationService;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;

/**
 * Created by pan on 17-3-18.
 */

public class SmsReceiver extends BroadcastReceiver {
    private ComponentName mDeviceAdmin;//组件对象
    private DevicePolicyManager mDPM;//设备的管理者对象

    @Override
    public void onReceive(Context context, Intent intent) {
        //组件对象，参数：1.上下文，2.广播接收者对应的字节码文件；组件对象还可以作为设备管理器是否激活的标识
        mDeviceAdmin = new ComponentName(context, DeviceAdmin.class);
        //获取设备的管理者对象
        mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

        //1.判断是否开启防盗保护
        boolean open_security = SpUtil.getBoolean(context, ConstantValue.OPEN_SECURITY, false);
        if (open_security) {
            //2.获取短信内容
            Object[] objects = (Object[]) intent.getExtras().get("pdus");
            //3.循环遍历短信
            for (Object object : objects) {
                //4.获取短信对象
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) object);
                //5.获取短信对象的基本信息
                /*
                获取号码
                String originatingAddress = sms.getOriginatingAddress();
                */
                String messageBody = sms.getMessageBody();
                //6.判断是否包含音乐的关键字
                if (messageBody.contains("#*alarm*#")) {
                    //7.播放音乐（准备音乐，使用MediaPlayer类的create静态方法,创建媒体播放类MediaPlayer的对象,第一个参数为上下文环境，第二个参数为要播放的对象）
                    MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.ylzs);
                    //使音乐一直循环
                    mediaPlayer.setLooping(true);
                    //播放音乐
                    mediaPlayer.start();
                    SmsManager smsManager = SmsManager.getDefault();
                    //第二个参数：scAddress，在中国不支持，所以传null
                    smsManager.sendTextMessage(SpUtil.getString(context, ConstantValue.CONTACT_PHONE, null), null, "手机已经播放报警音乐", null, null);
                }
                if (messageBody.contains("#*location*#")) {
                    //8.开启获取位置服务
                    context.startService(new Intent(context, LocationService.class));
                }
                if (messageBody.contains("#*wipedata*#")) {
                    //9.销毁数据
                    if (mDPM.isAdminActive(mDeviceAdmin)) {
                        //如果已经激活设备管理器，就让其清除数据
                        mDPM.wipeData(0);
                        //清除外部内存卡
                        //mDPM.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
                    } else {
                        //如果没有激活设备管理器，提示开启
                        Toast.makeText(context, "请激活设备管理器", Toast.LENGTH_SHORT).show();
                    }
                }
                if (messageBody.contains("#*lockscreen*#")) {
                    //10.远程锁屏
                    if (mDPM.isAdminActive(mDeviceAdmin)) {
                        //如果已经激活设备管理器，就让其锁屏
                        mDPM.lockNow();
                        //锁屏之后设置锁屏密码,如果传入空字符时密码为空,第二个参数的含义：不要让其他管理员再次更改密码，直到用户输入密码
                        mDPM.resetPassword("32123", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                    } else {
                        //如果没有激活设备管理器，提示开启
                        Toast.makeText(context, "请激活设备管理器", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
