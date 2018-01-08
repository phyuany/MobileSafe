package com.panhongyuan.mobilesafe.engine;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pan on 17-4-8.
 */

public class CommonnumDao {
    //指定访问数据库的路径
    public static String path = "data/data/com.panhongyuan.mobilesafe/files/commonnum.db";

    //2.开启数据库
    public List<Group> getGroup() {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = db.query("classlist", new String[]{"name", "idx"}, null, null, null, null, null, null);

        List<Group> groupList = new ArrayList<Group>();
        while (cursor.moveToNext()) {

            Group group = new Group();

            group.name = cursor.getString(0);
            group.idx = cursor.getString(1);

            group.childList = getChild(group.idx);

            groupList.add(group);
        }
        cursor.close();
        db.close();

        return groupList;
    }

    //获取每个组中孩子节点的数据
    public List<Child> getChild(String idx) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
        //Cursor cursor = db.query("tab", new String[]{"name", "idx"}, null, null, null, null, null, null);
        Cursor cursor = db.rawQuery("select * from table" + idx + ";", null);
        List<Child> childList = new ArrayList<Child>();
        while (cursor.moveToNext()) {

            Child child = new Child();

            child._id = cursor.getString(0);
            child.number = cursor.getString(1);
            child.name = cursor.getString(2);

            childList.add(child);
        }
        cursor.close();
        db.close();

        return childList;
    }

    public class Group {
        public String name;
        public String idx;
        public List<Child> childList;
    }

    public class Child {
        public String _id;
        public String name;
        public String number;
    }
}
