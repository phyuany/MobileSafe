package com.panhongyuan.mobilesafe.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.engine.CommonnumDao;

import java.util.List;

import static android.view.Gravity.CENTER;

/**
 * Created by pan on 17-4-8.
 */

public class CommonNumberQueryActivity extends Activity {

    private ExpandableListView elv_common_numb;
    private List<CommonnumDao.Group> mGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_numb);
        //初始化UI
        initUI();
        //初始化数据
        initData();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        CommonnumDao commonnumDao = new CommonnumDao();
        mGroup = commonnumDao.getGroup();

        final MyAdapter myAdapter = new MyAdapter();
        elv_common_numb.setAdapter(myAdapter);

        //给可扩展的List注册点击事件
        elv_common_numb.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            /**
             * @param parent
             * @param v
             * @param groupPosition
             * @param childPosition
             * @param id
             * @return 在isChildSelectable方法中返回true代表响应子列表的点击事件，如果isChildSelectable方法中返回false，而此处返回true，则代表将事件自己去做管理
             */
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                startCall(myAdapter.getChild(groupPosition, childPosition).number);
                return false;
            }
        });
    }

    /**
     * 拨打电话的方法
     *
     * @param number
     */
    private void startCall(String number) {
        //开启拨打电话界面
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        //权限判断
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(intent);
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        elv_common_numb = (ExpandableListView) findViewById(R.id.elv_common_numb);
    }

    private class MyAdapter extends BaseExpandableListAdapter {
        @Override
        public int getGroupCount() {
            return mGroup.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mGroup.get(groupPosition).childList.size();
        }

        @Override
        public CommonnumDao.Group getGroup(int groupPosition) {
            return mGroup.get(groupPosition);
        }

        @Override
        public CommonnumDao.Child getChild(int groupPosition, int childPosition) {
            return mGroup.get(groupPosition).childList.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            TextView textView = new TextView(getApplicationContext());
            textView.setText(getGroup(groupPosition).name);
            textView.setGravity(CENTER);
            textView.setBackgroundColor(Color.parseColor("#84FFFF"));
            textView.setPadding(8, 20, 8, 20);
            textView.setTextColor(Color.BLUE);
            //dpi 等同于 ppi, 即像素密度,下一行代码设置文字大小像素为dp
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(), R.layout.elv_child_item, null);
            TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
            TextView tv_number = (TextView) view.findViewById(R.id.tv_number);

            tv_name.setText(getChild(groupPosition, childPosition).name);
            tv_number.setText(getChild(groupPosition, childPosition).number);

            return view;
        }

        /**
         * 孩子节点是否响应事件
         *
         * @param groupPosition
         * @param childPosition
         * @return 返回true代表子列表可以响应点击事件
         */
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
