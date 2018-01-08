package com.panhongyuan.mobilesafe.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Debug;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.db.domain.ProcessInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pan on 17-4-8.
 */

public class ProcessInfoProvider {
    /**
     * 获取进程总数
     *
     * @param context
     * @return
     */
    public static int getProcessCount(Context context) {
        //1.获取ActivityManager对象
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.获取正在运行进程的集合
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        //3.返回集合的总数
        return runningAppProcesses.size();
    }

    /**
     * 获取可用内存大小
     *
     * @param context
     * @return 返回可用的内存数
     */
    //获取可用空间大小
    public static long getAvailSpace(Context context) {
        //1.获取ActivityManager对象
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.构建存储可用内存对象
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        //3.给memoryInfo对象赋值
        am.getMemoryInfo(memoryInfo);
        //4.获取memoryInfo中相应可用空间大小
        return memoryInfo.availMem;
    }

    /**
     * @param ctx
     * @return 返回总的内存数    单位为bytes 返回0说明异常
     */
    public static long getTotalSpace(Context ctx) {
        /*

		此方式不兼容API8的版本，memoryInfo.totalMem此方法必须在API16上可用

		//1,获取activityManager
		ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		//2,构建存储可用内存的对象
		MemoryInfo memoryInfo = new MemoryInfo();
		//3,给memoryInfo对象赋(可用内存)值
		am.getMemoryInfo(memoryInfo);
		//4,获取memoryInfo中相应可用内存大小
		return memoryInfo.totalMem;*/

        //内存大小写入文件中,读取proc/meminfo文件,读取第一行,获取数字字符,转换成bytes返回
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader("proc/meminfo");
            bufferedReader = new BufferedReader(fileReader);
            String lineOne = bufferedReader.readLine();
            //将字符串转换成字符的数组
            char[] charArray = lineOne.toCharArray();
            //循环遍历每一个字符,如果此字符的ASCII码在0到9的区域内,说明此字符有效
            StringBuffer stringBuffer = new StringBuffer();
            for (char c : charArray) {
                if (c >= '0' && c <= '9') {
                    stringBuffer.append(c);
                }
            }
            return Long.parseLong(stringBuffer.toString()) * 1024;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null && bufferedReader != null) {
                    fileReader.close();
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * 获取进程相关信息
     *
     * @param context
     * @return
     */
    public static List<ProcessInfo> getProcessInfo(Context context) {
        //获取进程相关信息

        List<ProcessInfo> processInfoList = new ArrayList<ProcessInfo>();

        //1.获取Activity管理者对象和包管理者对象
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = context.getPackageManager();

        //2.获取正在运行的应用的集合
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();


        //3.循环遍历以上集合获取进程相关信息（名称，包名，图标，使用内存大小，是否为系统进程）
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            ProcessInfo processInfo = new ProcessInfo();
            //4.获取进程的名称
            processInfo.packageName = info.processName;
            //5.获取进程占用空间大小（传递一个进程对应的pid数组进来）,
            Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{info.pid});
            //6.返回数组中索引为0的对象，为当前进程信息的对象
            Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
            //7.获取已经使用的内存大小
            processInfo.memSize = memoryInfo.getTotalPrivateDirty() * 1024;
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(processInfo.packageName, 0);
                //8.获取应用名称
                processInfo.name = applicationInfo.loadLabel(pm).toString();
                //9.获取应用图标
                processInfo.icon = applicationInfo.loadIcon(pm);
                //10.判断是否为系统进程
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                    processInfo.isSystem = true;
                } else {
                    processInfo.isSystem = false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                //处理异常
                //1.没有名称使用包名作为名称
                processInfo.name = info.processName;
                //2.没有图标使用Android系统资源下默认应用图标作为图标
                processInfo.icon = context.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
                //3.出现以上情况的应用一般位系统应用，做一个系统应用标识
                processInfo.isSystem = true;
                e.printStackTrace();
            }
            processInfoList.add(processInfo);
        }
        return processInfoList;
    }


    /**
     * 杀死进程的方法
     *
     * @param context     上下文环境
     * @param processInfo 要杀死的进程javaBean对象
     */
    public static void killProcess(Context context, ProcessInfo processInfo) {
        //1.获取Activity管理者对象和包管理者对象
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.按照包名，杀死指定的进程（增加权限）
        am.killBackgroundProcesses(processInfo.packageName);
    }

    /**
     * 杀死所有进程的方法
     *
     * @param context 传递的上下文环境用于获取Activity管理者对象和获取应用包名
     */
    public static void killAll(Context context) {
        //1.获取Activity管理者对象和包管理者对象
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.获取正在运行的应用的集合
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        //3.循环遍历所有的进程，并把他们杀死
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            //4.除了本身以外，其他的进程都要结束
            if (info.processName.equals(context.getPackageName())) {
                continue;
            }
            //使用Activity管理者对象，结束正在运行的进程
            am.killBackgroundProcesses(info.processName);
        }
    }
}
