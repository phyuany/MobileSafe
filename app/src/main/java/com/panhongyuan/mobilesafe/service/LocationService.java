package com.panhongyuan.mobilesafe.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;

/**
 * Created by pan on 17-3-19.
 */

public class LocationService extends Service {

    boolean isNotSend = true;//记录是否未发送过短信
    private String tag = "LocationService";//打印日志的tag

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //获取经纬度坐标
        //1.获取管理者对象
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //2.创建一个准则对象，以最优的方式获取经纬度，criteria：准则
        Criteria criteria = new Criteria();//谷歌提供的Criteria类
        //允许话费
        criteria.setCostAllowed(true);
        //设置精确度
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //位置管理对象获取一个最优的位置提供方式,参数：1.准则对象，2.返回true时我们提供的某一个提供方式就能获取经纬度坐标
        String bestProvider = lm.getBestProvider(criteria, true);

        //检查位置权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //3.在一定的时间间隔或者移动距离之后获取经纬度坐标
        lm.requestLocationUpdates(bestProvider, 0, 0, new MyLocationListener());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 位置坚挺者
     */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            //获取经纬度坐标的改变
            //获取经度
            double longitude = location.getLongitude();
            //获取纬度
            double latitude = location.getLatitude();

            //发送短信
            //获取短信管理者对象,发送一次短信即可
            if (isNotSend) {
                Log.i(tag, "开始尝试发送短信");
                String phone = SpUtil.getString(getApplicationContext(), ConstantValue.CONTACT_PHONE, null);
                //没有给安全号码发送消息的花则给安全号码发送一次短信
                SmsManager smsManager = SmsManager.getDefault();
                //第二个参数：scAddress，在中国不支持，所以传null
                smsManager.sendTextMessage(SpUtil.getString(getApplicationContext(), ConstantValue.CONTACT_PHONE, null), null, "您手机所在的地址坐标为:\n经度：" + longitude + "\n纬度：" + latitude, null, null);
                isNotSend = false;
                Log.i(tag, "短信测试代码结束");
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
