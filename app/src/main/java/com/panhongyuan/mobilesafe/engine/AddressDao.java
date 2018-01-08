package com.panhongyuan.mobilesafe.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.panhongyuan.mobilesafe.utils.ToastUtil;

/**
 * Created by pan on 17-3-25.
 */

public class AddressDao {
    //指定访问数据库的路径
    public static String path = "data/data/com.panhongyuan.mobilesafe/files/address.db";
    private static String subPhone;
    private static String tag = "AddressDao";
    private static String mAddress = "未知号码";//从数据库查询的归属地地址

    /**
     * @param phone 查询的电话号码
     */
    //开启数据库链接，然后进行访问
    public static String getAddress(String phone) {
        //每次查询之前，都给地址一个未知电话
        mAddress = "未知号码";

        //1.开启数据库链接（以制度形式打开）
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);

        //1.正则表达式，匹配手机号码
        //手机号码的正则表达式
        String regularExpression = "^1[3-8]\\d{9}";
        if (phone.matches(regularExpression)) {
            //2.获取手机号码，截取前7为来查询
            subPhone = phone.substring(0, 7);
            //3.查询数据库，匹配data1的数据
            Cursor cursor = db.query("data1", new String[]{"outkey"}, "id = ?", new String[]{subPhone}, null, null, null);
            //4.如果查到数据，则往下执行
            if (cursor.moveToNext()) {
                String outKey = cursor.getString(0);
                //5.查询表2,以outKey作为外键查询匹配的归属地
                Cursor data2 = db.query("data2", new String[]{"location"}, "id = ?", new String[]{outKey}, null, null, null);
                if (data2.moveToNext()) {
                    mAddress = data2.getString(0);
                    Log.i(tag, "从数据库获取的测试值为" + mAddress);
                } else {
                    mAddress = "未知号码";
                }
            }
        } else {
            int length = phone.length();
            switch (length) {
                case 3://110,112,119,120,114
                    mAddress = "报警电话";
                    break;
                case 4:
                    mAddress = "模拟器";
                    break;
                case 5:
                    mAddress = "服务点好";
                    break;
                case 7:
                    mAddress = "本地电话";
                    break;
                case 11:
                    //3+8(区号+座机号码)，查询data2
                    String area = phone.substring(1, 3);
                    Cursor cursor = db.query("data2", new String[]{"location"}, "area = ?", new String[]{area}, null, null, null);
                    if (cursor.moveToNext()) {
                        mAddress = cursor.getString(0);
                    } else {
                        mAddress = "未知号码";
                    }
                    break;
                case 12:
                    //(4+8)区号+座机号码
                    String area1 = phone.substring(1, 4);
                    Cursor cursor1 = db.query("data2", new String[]{"location"}, "area = ?", new String[]{area1}, null, null, null);
                    if (cursor1.moveToNext()) {
                        mAddress = cursor1.getString(0);
                    } else {
                        mAddress = "未知号码";
                    }
                    break;
            }
        }
        return mAddress;
    }
}
