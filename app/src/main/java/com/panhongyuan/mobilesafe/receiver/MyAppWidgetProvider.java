package com.panhongyuan.mobilesafe.receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.panhongyuan.mobilesafe.service.UpdateWidgetService;

/**
 * Created by pan on 17-4-9.
 */

public class MyAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        //创建第一个窗体小部件，调用的方法
        super.onReceive(context, intent);
    }

    //以下三个方法在创建窗体时调用的方法

    /**
     * 创建第一个小控件调用的方法
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        //开启服务（onCreate）
        context.startService(new Intent(context, UpdateWidgetService.class));
        super.onEnabled(context);
    }

    /**
     * 创建多一个窗体小部件调用的方法
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //开启服务
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    /**
     * 当窗体小部件的宽高发生改变的时候调用的方法，创建小部件的时候也会调用此方法，即系统视为窗体控件的宽高为0
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param newOptions
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * 删除最后一个小部件的时候也会调用此方法
     *
     * @param context
     */
    //以下两个方法在删除时调用
    @Override
    public void onDisabled(Context context) {
        //关闭服务
        context.stopService(new Intent(context, UpdateWidgetService.class));
        super.onDisabled(context);
    }

    /**
     * 删除窗体小部件的时候会调用的方法，删除最后一个小部件的时候也会调用此方法
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
}
