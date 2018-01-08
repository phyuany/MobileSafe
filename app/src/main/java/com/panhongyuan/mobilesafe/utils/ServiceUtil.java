package com.panhongyuan.mobilesafe.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ServiceInfo;

import java.util.List;

/**
 * Created by pan on 17-3-26.
 */

public class ServiceUtil {
    /**
     * 判断服务是否正在运行
     *
     * @param context     上下文环境
     * @param serviceName 判断是否正在运行的服务
     * @return 返回服务是否运行的状态
     */
    public static boolean isRunning(Context context, String serviceName) {
        //1.获取ActivityManager对象，可以获取当前手机正在运行的所有的服务
        ActivityManager mAM = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.获取手机正在运行的服务的集合,参数：最大的服务个数
        List<ActivityManager.RunningServiceInfo> runningServices = mAM.getRunningServices(100);
        //3.遍历所有服务的集合，拿到服务类的名称和传进来的服务类的名称做对比，如果一样，则证明服务正在运行
        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if (serviceName.equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
