package com.panhongyuan.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.panhongyuan.mobilesafe.db.AppLockOpenHelper;
import com.panhongyuan.mobilesafe.db.domain.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pan on 17-4-09.
 */

public class AppLockDao {

    private final AppLockOpenHelper appLockOpenHelper;
    private final Context context;

    //BlackNumberDao单立模式
    //1.私有化构造方法
    private AppLockDao(Context context) {
        this.context = context;
        //创建数据库以及其表结构
        appLockOpenHelper = new AppLockOpenHelper(context);
    }

    //2.声明一个当前类的对象
    private static AppLockDao appLockDao = null;

    //3.提供一个共有静态一个方法,创建一个实例
    public static AppLockDao getInstance(Context context) {
        if (appLockDao == null) {
            appLockDao = new AppLockDao(context);
        }
        return appLockDao;
    }

    //插入方法
    public void insert(String packageName) {
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("packagename", packageName);

        db.insert("applock", null, contentValues);

        db.close();

        //数据增加时,通过内容管理者发通知，在服务里内容观察者捕获
        context.getContentResolver().notifyChange(Uri.parse("content://applock/change"),null);
    }

    //删除方法
    public void delete(String packageName) {
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("packagename", packageName);

        db.delete("applock", "packagename = ?", new String[]{packageName});

        db.close();

        //删除过程数据发生改变，通过内容管理者发通知，在服务里内容观察者捕获
        context.getContentResolver().notifyChange(Uri.parse("content://applock/change"), null);
    }

    //查询所有
    public List<String> findAll() {
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("applock", new String[]{"packagename"}, null, null, null, null, null);

        List<String> lockPackageList = new ArrayList<>();
        while (cursor.moveToNext()) {
            lockPackageList.add(cursor.getString(0));
        }

        cursor.close();
        db.close();

        return lockPackageList;
    }

}
