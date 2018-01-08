package com.panhongyuan.mobilesafe.engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.panhongyuan.mobilesafe.db.domain.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pan on 17-4-6.
 */

public class AppInfoProvider {
    /**
     * 返回当前手机所有应用相关信息（名称，包名，图标，（手机内存，sd卡），（系统，用户））
     *
     * @param context 获取包管理者对象上下文
     * @return 返回包含安装应用相关信息的对象
     */
    public static List<AppInfo> getAppInfoList(Context context) {

        //1.获取包管理者对象
        PackageManager pm = context.getPackageManager();
        //2.获取安装在手机的应用相关集合
        List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);

        List<AppInfo> appInfoList = new ArrayList<AppInfo>();


        //3.循环遍历应用信息的集合
        for (PackageInfo packageInfo : packageInfoList) {
            AppInfo appInfo = new AppInfo();
            //4.获取应用包名
            appInfo.packageName = packageInfo.packageName;
            //5.应用名称
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            appInfo.name = applicationInfo.loadLabel(pm).toString();
            //6.获取图标
            appInfo.icon = applicationInfo.loadIcon(pm);//以下代码判断是否位系统应用的if语句不理解
            //7.判断是否为应用程序(每一个手机应用对应的flag都不一样)
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                //系统应用
                appInfo.isSystem = true;
            } else {
                //非系统应用
                appInfo.isSystem = false;
            }
            //8.判断是否为sd卡中安装的应用
            if ((applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE) {
                //系统应用
                appInfo.isSdCard = true;
            } else {
                //非系统应用
                appInfo.isSdCard = false;
            }
            appInfoList.add(appInfo);
        }
        return appInfoList;
    }
}
