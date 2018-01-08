package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.db.dao.AppLockDao;
import com.panhongyuan.mobilesafe.db.domain.AppInfo;
import com.panhongyuan.mobilesafe.engine.AppInfoProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pan on 17-4-9.
 */

public class AppLockActivity extends Activity {

    private ListView lv_lock;
    private ListView lv_unlock;
    private Button bt_unlock;
    private Button bt_lock;
    private LinearLayout ll_unlock;
    private LinearLayout ll_lock;
    private TextView tv_unlock;
    private TextView tv_lock;
    private List<AppInfo> appInfoList;
    private List<AppInfo> mLockList;
    private List<AppInfo> mUnlockList;
    private AppLockDao mDao;
    private MyAdapter mLockAdapter;
    private MyAdapter mUnLockAdapter;
    private TranslateAnimation mTranslateAnimation;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //6.接收到消息，填充已加锁和未加锁的适配器
            //已加锁数据适配器
            mLockAdapter = new MyAdapter(true);
            lv_lock.setAdapter(mLockAdapter);
            //未加锁数据适配器
            mUnLockAdapter = new MyAdapter(false);
            lv_unlock.setAdapter(mUnLockAdapter);
        }
    };

    class MyAdapter extends BaseAdapter {
        private boolean isLock;

        /**
         * @param isLock true已加锁的数据适配器,false为未加锁的数据适配器
         */
        public MyAdapter(boolean isLock) {
            this.isLock = isLock;
        }

        @Override
        public int getCount() {
            if (isLock) {
                tv_lock.setText("已加锁应用" + mLockList.size());
                return mLockList.size();
            } else {
                tv_unlock.setText("未加锁应用" + mUnlockList.size());
                return mUnlockList.size();
            }
        }

        @Override
        public AppInfo getItem(int position) {
            if (isLock) {
                return mLockList.get(position);
            } else {
                return mUnlockList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.list_islock_item, null);
                holder = new ViewHolder();

                holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                holder.iv_lock = (ImageView) convertView.findViewById(R.id.iv_lock);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final AppInfo appInfo = getItem(position);
            holder.iv_icon.setBackgroundDrawable(appInfo.icon);
            holder.tv_name.setText(appInfo.name);
            if (isLock) {
                holder.iv_lock.setBackgroundResource(R.drawable.lock);
            } else {
                holder.iv_lock.setBackgroundResource(R.drawable.unlock);
            }

            //注册锁的点击事件
            final View finalConvertView = convertView;
            holder.iv_lock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //添加动画效果
                    finalConvertView.startAnimation(mTranslateAnimation);
                    //对动画的执行过程做事件监听，现将动画执行完成之后，再去移除集合中的数据，操作数据库
                    mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            //动画结束之后
                            //判断为已加锁或者未加锁
                            if (isLock) {
                                //已加锁变成已加锁
                                //1.已加锁的集合要删除一个条目，未加锁要增加一个条目，对象为getItem获取的对象
                                mLockList.remove(appInfo);
                                mUnlockList.add(appInfo);
                                //2.从已加锁的数据库中删除一条数据
                                mDao.delete(appInfo.packageName);
                                //3. 刷新数据适配器
                            } else {
                                //未加锁变成已加锁
                                //1.已加锁的集合添加一个条目，未加锁要移除一个条目，对象为getItem获取的对象
                                mLockList.add(appInfo);
                                mUnlockList.remove(appInfo);
                                //2.从已加锁的数据库中删除一条数据
                                mDao.insert(appInfo.packageName);
                                //3. 刷新数据适配器
                            }
                            //通知数据改变
                            mLockAdapter.notifyDataSetChanged();
                            mUnLockAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            //动画重复的时候调用的方法
                        }
                    });
                }
            });

            return convertView;
        }
    }

    static class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        ImageView iv_lock;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);
        //初始化UI
        initUI();
        //初始化控件
        initData();
        //初始化平移动画
        initAnimation();
    }

    /**
     * 初始化平移动画(平移自身宽度的大小)
     */
    private void initAnimation() {
        mTranslateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        mTranslateAnimation.setDuration(500);
    }

    /**
     * 初始化数据的方法
     * 区分已加锁和未加锁应用的集合
     */
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //1.获取所有手机中的应用
                appInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());
                //2.区分已加锁应用和未加锁应用
                mLockList = new ArrayList<>();
                mUnlockList = new ArrayList<>();
                //3.获取数据库中已加锁应用的集合
                mDao = AppLockDao.getInstance(getApplicationContext());
                List<String> lockPackageList = mDao.findAll();
                for (AppInfo appInfo : appInfoList) {
                    //4.如果循环到的应用的包名在数据库中，则说明是已加锁应用
                    if (lockPackageList.contains(appInfo.packageName)) {
                        mLockList.add(appInfo);
                    } else {
                        mUnlockList.add(appInfo);
                    }
                }
                //5.告知主线程，数据加载完毕
                mHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    private void initUI() {
        bt_unlock = (Button) findViewById(R.id.bt_unlock);
        bt_lock = (Button) findViewById(R.id.bt_lock);

        ll_unlock = (LinearLayout) findViewById(R.id.ll_unlock);
        ll_lock = (LinearLayout) findViewById(R.id.ll_lock);

        tv_unlock = (TextView) findViewById(R.id.tv_unlock);
        tv_lock = (TextView) findViewById(R.id.tv_lock);

        lv_unlock = (ListView) findViewById(R.id.lv_unlock);
        lv_lock = (ListView) findViewById(R.id.lv_lock);

        bt_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.已加锁应用显示，未加锁应用隐藏
                ll_lock.setVisibility(View.VISIBLE);
                ll_unlock.setVisibility(View.GONE);
                //2.未加锁按钮变浅色，已加锁按钮变深色
                bt_lock.setBackgroundResource(R.drawable.tab_right_pressed);
                bt_unlock.setBackgroundResource(R.drawable.tab_left_default);
            }
        });

        bt_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.已加锁应用隐藏，为加锁应用显示
                ll_lock.setVisibility(View.GONE);
                ll_unlock.setVisibility(View.VISIBLE);
                //2.未加锁变深色，已加锁变浅色
                bt_lock.setBackgroundResource(R.drawable.tab_right_default);
                bt_unlock.setBackgroundResource(R.drawable.tab_left_pressed);
            }
        });
    }
}
