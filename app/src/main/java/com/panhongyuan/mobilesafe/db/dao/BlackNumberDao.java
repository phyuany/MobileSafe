package com.panhongyuan.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.panhongyuan.mobilesafe.db.BlackNumberOpenHelper;
import com.panhongyuan.mobilesafe.db.domain.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pan on 17-3-30.
 */

public class BlackNumberDao {

    private final BlackNumberOpenHelper blackNumberOpenHelper;

    //BlackNumberDao单立模式
    //1.私有化构造方法
    private BlackNumberDao(Context context) {
        //创建数据库以及其表结构
        blackNumberOpenHelper = new BlackNumberOpenHelper(context);
    }

    //2.声明一个当前类的对象
    private static BlackNumberDao blackNumberDao = null;

    //3.提供一个共有静态一个方法,创建一个实例
    public static BlackNumberDao getInstance(Context context) {
        if (blackNumberDao == null) {
            blackNumberDao = new BlackNumberDao(context);
        }
        return blackNumberDao;
    }

    /**
     * （1）增，增加一个条目
     *
     * @param phone 拦截的号码
     * @param mode  拦截的模式（1.短信，2.电话，3.全部）
     */
    public void insert(String phone, String mode) {
        //开启数据库准备做写入操作
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        contentValues.put("mode", mode);
        db.insert("blacknumber", null, contentValues);

        db.close();
    }

    /**
     * （2）删，删除数据库中电话号码的方法
     *
     * @param phone 删除的电话好号码
     */
    public void delete(String phone) {
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();

        db.delete("blacknumber", "phone = ?", new String[]{phone});

        db.close();
    }

    /**
     * （3）改，更改数据库的方法
     *
     * @param phone 更新的电话
     * @param mode  更新的模式(1.短信，2.电话，3.全部）
     */
    public void update(String phone, String mode) {
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("mode", mode);
        db.update("blacknumber", contentValues, "phone=?", new String[]{phone});

        db.close();
    }

    /**
     * （4）查，查询数据库
     *
     * @return 查询到数据库中的所有拦截的号码以及拦截类型所在的集合
     */
    public List<BlackNumberInfo> findAll() {
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();

        Cursor cursor = db.query("blacknumber", new String[]{"phone", "mode"}, null, null, null, null, "_id desc");
        List<BlackNumberInfo> blackNumberInfos = new ArrayList<>();
        while (cursor.moveToNext()) {
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.phone = cursor.getString(0);
            blackNumberInfo.mode = cursor.getString(1);
            blackNumberInfos.add(blackNumberInfo);
        }
        //关闭容器和数据库
        cursor.close();
        db.close();

        return blackNumberInfos;
    }

    /**
     * 查询20条数据
     *
     * @param index 查询的索引值
     * @return  查询到数据数据列表
     */
    public List<BlackNumberInfo> find(int index) {
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("select phone,mode from blacknumber order by _id desc limit ?,20;", new String[]{index + ""});
        List<BlackNumberInfo> blackNumberInfos = new ArrayList<>();
        while (cursor.moveToNext()) {
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.phone = cursor.getString(0);
            blackNumberInfo.mode = cursor.getString(1);
            blackNumberInfos.add(blackNumberInfo);
        }
        //关闭容器和数据库
        cursor.close();
        db.close();

        return blackNumberInfos;
    }

    /**
     * @return 获取数据库中总的条目数，返回0代表异常
     */
    public int getCount() {
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("select count(*) from blacknumber;", null);
        if (cursor.moveToNext()) {
            count = cursor.getInt(0);
        }
        //关闭容器和数据库
        cursor.close();
        db.close();
        return count;
    }

    /**
     * @param phone 作为查询条件查询的电话号码
     * @return 传入电话号码的拦截模式，1.代表短信，2.代表电话，3.代表所有拦截方式，0.代表内有此条数据
     */
    public int getMode(String phone) {
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        int mode = 0;
        Cursor cursor = db.query("blacknumber", new String[]{"mode"}, "phone = ?", new String[]{phone}, null, null, null);
        if (cursor.moveToNext()) {
            mode = cursor.getInt(0);
        }
        //关闭容器和数据库
        cursor.close();
        db.close();
        return mode;
    }

}
