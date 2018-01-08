package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.db.dao.BlackNumberDao;
import com.panhongyuan.mobilesafe.db.domain.BlackNumberInfo;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

import java.util.List;

import static com.panhongyuan.mobilesafe.R.id.tv_mode;
import static com.panhongyuan.mobilesafe.R.id.tv_phone;

/**
 * Created by pan on 17-3-30.
 */

/*
* 代码优化过程
* 1.复用convertView
* 2.对findViewById次数的优化，使用ViewHolder
* 3.将ViewHolder定义成静态，不会创建多个对象
* 4.分页算法，如果右多个条目的时候，可以做一个分页算法，约定每一次加载20条，加载的20条中按逆序返回*/
public class BlackNumberActivity extends Activity {

    private BlackNumberDao mDao;
    private List<BlackNumberInfo> mBlackNumberInfoList;
    private ListView lv_blacknumber;
    private MyAdapter myAdapter;
    private Button bt_add;
    private int mode = 1;
    private boolean mIsLoad = false;
    private int mCount;//数据库中总条目的数量

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //4.告知ListView可以去适配数据适配器
            if (myAdapter == null) {
                myAdapter = new MyAdapter();
                lv_blacknumber.setAdapter(myAdapter);
            } else {
                myAdapter.notifyDataSetChanged();
            }
        }
    };

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mBlackNumberInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mBlackNumberInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
           /* View view = null;
            if (convertView == null) {
                view = View.inflate(getApplicationContext(), R.layout.listview_blacknumber_item, null);
            } else {
                view = convertView;
            }*/

            //复用ViewHolder的步骤1
            ViewHolder holder = null;
            //优化代码，复用convertView,复用之前找到的控件，将findViewById的过程封装到convertView为空的代码块中
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.listview_blacknumber_item, null);
                //减少findViewById的次数
                //复用ViewHolder的步骤3
                holder = new ViewHolder();
                //复用ViewHolder的步骤4
                holder.tv_phone = (TextView) convertView.findViewById(tv_phone);
                holder.tv_mode = (TextView) convertView.findViewById(tv_mode);
                holder.iv_delete = (ImageView) convertView.findViewById(R.id.iv_delete);
                //复用ViewHolder的步骤5
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            //删除操作
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //1.数据库的删除
                    mDao.delete(mBlackNumberInfoList.get(position).phone);
                    //2.集合中的删除，通知适配器更新界面
                    mBlackNumberInfoList.remove(position);
                    //3.通知适配器
                    if (myAdapter != null) {
                        myAdapter.notifyDataSetChanged();
                    }
                }
            });

            holder.tv_phone.setText(mBlackNumberInfoList.get(position).phone);
            int mode = Integer.parseInt(mBlackNumberInfoList.get(position).mode);

            switch (mode) {
                case 1:
                    holder.tv_mode.setText("拦截短信");
                    break;
                case 2:
                    holder.tv_mode.setText("拦截电话");
                    break;
                case 3:
                    holder.tv_mode.setText("拦截全部");
                    break;
            }

            return convertView;
        }
    }

    //复用ViewHolder的步骤1
    static class ViewHolder {
        TextView tv_phone;
        TextView tv_mode;
        ImageView iv_delete;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blacknumber);
        //初始化控件
        initUI();

        //初始化数据
        initData();
    }

    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.获取数据库操作对象
                mDao = BlackNumberDao.getInstance(getApplicationContext());
                //2.查询所有电话号码的集合
                mBlackNumberInfoList = mDao.find(0);
                //获取数据库中总条目的数量
                mCount = mDao.getCount();
                //3.通过消息机制通知主线程更新UI
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    private void initUI() {
        bt_add = (Button) findViewById(R.id.bt_add);
        lv_blacknumber = (ListView) findViewById(R.id.lv_blacknumber);

        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        //将听ListView的滚动状态
        lv_blacknumber.setOnScrollListener(new AbsListView.OnScrollListener() {
            //滚动过程中，状态改变调用此方法
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                /*
                1.飞速滚动：AbsListView.OnScrollListener.SCROLL_STATE_FLING;
                2.空闲状态：AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
                3.用手触摸滚动：AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;*/
                if (mBlackNumberInfoList != null) {//容错处理
                    //条件1,滚动到停止状态，条件2：最后一个条目可见（最后一个条目的索引值>=数据适配器中的总条目减去1）
                    if (scrollState == SCROLL_STATE_IDLE && lv_blacknumber.getLastVisiblePosition() >= mBlackNumberInfoList.size() - 1 && !mIsLoad) {
                        //mIsLoad是防止重复加载的变量，如果当前正在加载，则将mIsLoad改为true,加载完毕后，再将mIsLoad改为false
                        // 如果下一次需要加载执行的时候，会判断MIsLoad变量，是否位false,如果为true,则需要等待本次加载完成

                        //容错处理
                        if (mCount > mBlackNumberInfoList.size()) {
                            //加载下一页数据
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //1.获取数据库操作对象
                                    mDao = BlackNumberDao.getInstance(getApplicationContext());
                                    //2.查询所有电话号码的集合
                                    List<BlackNumberInfo> moreData = mDao.find(mBlackNumberInfoList.size());
                                    //3.添加下一页数据的过程
                                    mBlackNumberInfoList.addAll(moreData);
                                    //4.通过消息机制通知主线程更新UI
                                    mHandler.sendEmptyMessage(0);
                                }
                            }).start();
                        }
                    }
                }
            }

            //滚动过程中调用的方法
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();

        View view = View.inflate(getApplicationContext(), R.layout.dialog_add_blacknumber, null);
        dialog.setView(view, 0, 0, 0, 0);

        final EditText et_phone = (EditText) view.findViewById(R.id.et_phone);
        RadioGroup rg_group = (RadioGroup) view.findViewById(R.id.rb_group);

        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

        //监听切换条目监听过程
        rg_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_sms:
                        mode = 1;
                        break;
                    case R.id.rb_phone:
                        mode = 2;
                        break;
                    case R.id.rb_all:
                        mode = 3;
                        break;

                }
            }
        });

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.获取输入的电话号码
                String phone = et_phone.getText().toString();
                if (!TextUtils.isEmpty(phone)) {
                    //2.数据库插入电话黑名单数据
                    mDao.insert(phone, mode + "");
                    //3.更新界面显示，让数据库和集合保持同步（1.数据库中，数据重新读一遍，2.手动向集合中添加一个对象），第二种方法比较好，不费时
                    BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
                    blackNumberInfo.phone = phone;
                    blackNumberInfo.mode = mode + "";
                    //4.将对象插入到集合的最顶部
                    mBlackNumberInfoList.add(0, blackNumberInfo);
                    //5.通知数据适配器按照适配器中数据去刷新内容
                    if (myAdapter != null) {
                        myAdapter.notifyDataSetChanged();
                    }
                    //6.隐藏对话框
                    dialog.dismiss();
                } else {
                    ToastUtil.show(getApplicationContext(), "请输入拦截号码");
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
