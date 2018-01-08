package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pan on 17-3-14.
 */

public class ContactListActivity extends Activity {

    private ListView lv_contact;
    private String tag = "ContactListActivity";
    private List<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();

    /**
     * 主线程消息机制
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //8.填充数据
            mAdapter = new MyAdapter();
            lv_contact.setAdapter(mAdapter);
        }
    };
    private MyAdapter mAdapter;


    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return contactList.size();
        }

        @Override
        public HashMap<String, String> getItem(int position) {
            return contactList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(), R.layout.listview_contact_item, null);

            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            TextView tv_phone = (TextView) view.findViewById(R.id.tv_phone);

            tv_name.setText(getItem(position).get("name"));
            tv_phone.setText(getItem(position).get("phone"));
            return view;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        //初始化控件
        initUI();
        //初始化数据
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        //读取系统联系人可能是个耗时操作，所以开启子线程，读取数据库
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.获取内容解析器的对象
                ContentResolver contentResolver = getContentResolver();
                //2.解析器匹配查询联系人数据表的过程
                /*
                *系统联系人数据库中表结构
                *   raw_contacts    联系人表：   表中contact_id 为联系人唯一性id
                *   data    用户信息表：raw_contact_id作为外键，和raw_contacts中的contact_id作关联查询
                *           获取data1字段，包含电话号码以及联系人名称
                *           mimetypes_id字段，包含当前data1对应的数据类型
                *   mimetypes   类型表：获取data表中的mimetype_id和mimetype_id作关联查询，获取指向的信息类型
                *               电话号码:"vnd.android.cursor.item/phone_v2"
                *               用户名称:"vnd.android.cursor.item/name"
                * */
                Cursor cursor = contentResolver.query(Uri.parse("content://com.android.contacts/raw_contacts"), new String[]{"contact_id"}, null, null, null);
                contactList.clear();
                //3.循坏遍历游标，直到没有数据为止
                while (cursor.moveToNext()) {
                    String id = cursor.getString(0);
                    Log.i(tag, "id:" + id);//打印出联系人对应的唯一性ID
                    //4.查询第二张表,根据用户唯一性的id值，查询data表和mimetype表生成的试图，获取data以及mimetype字段
                    Cursor indexCursor = contentResolver.query(Uri.parse("content://com.android.contacts/data"),
                            new String[]{"data1", "mimetype"},
                            "raw_contact_id=?",
                            new String[]{id},
                            null);
                    //5.循环遍历获取电话号码和姓名，数据类型
                    HashMap<String, String> hashMap = new HashMap<>();
                    while (indexCursor.moveToNext()) {
                        String data = indexCursor.getString(0);
                        String type = indexCursor.getString(1);

                        //6.区分类型填充数据
                        if (type.equals("vnd.android.cursor.item/phone_v2")) {
                            //数据非空
                            if (!TextUtils.isEmpty(data)) {
                                hashMap.put("phone", data);
                            }
                        } else if (type.equals("vnd.android.cursor.item/name")) {
                            if (!TextUtils.isEmpty(data)) {
                                hashMap.put("name", data);
                            }
                        }
                    }
                    indexCursor.close();
                    contactList.add(hashMap);
                }
                cursor.close();
                //7.通过消息机制,发送一个空的消息告诉主线程已经填充好数据
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    /**
     * 初始化控件
     */
    private void initUI() {
        lv_contact = (ListView) findViewById(R.id.lv_contact);
        //注册点击事件
        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //1.获取点击条目索引指向集合中的对象
                if (mAdapter != null) {
                    HashMap<String, String> hashMap = mAdapter.getItem(position);
                    //2.获取当前条目指向集合对应的电话号码
                    String phone = hashMap.get("phone");
                    //3.此电话号码需要给第三个导航界面使用
                    Intent intent = new Intent();
                    intent.putExtra("phone", phone);
                    //setResult方法中第一个参数ResultCode没有任何意义，所以传一个0
                    setResult(0, intent);
                    //结束当前界面
                    finish();
                }
            }
        });
    }

}
