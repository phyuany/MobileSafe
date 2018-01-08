package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.db.domain.AppInfo;
import com.panhongyuan.mobilesafe.engine.AppInfoProvider;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by pan on 17-4-4.
 */

public class AppManagerActivity extends Activity implements View.OnClickListener {

    private List<AppInfo> mAppInfoList;
    private ListView lv_app_list;
    private List<AppInfo> mCustomerList;
    private List<AppInfo> mSystemList;
    private TextView tv_des;
    private AppInfo mAppInfo;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MyAdapter myAdapter = new MyAdapter();
            lv_app_list.setAdapter(myAdapter);

            if (tv_des != null && mCustomerList != null) {
                tv_des.setText("用户应用(" + mCustomerList.size() + ")");
            }
        }
    };
    private PopupWindow popupWindow;

    /**
     * 实现OnClickListener接口为实现的方法
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_uninstall:
                if (mAppInfo.isSystem) {
                    ToastUtil.show(getApplicationContext(), "此应用为系统应用\n不能卸载！");
                } else {
                    //卸载应用
                    Intent intent = new Intent("android.intent.action.DELETE");
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse("package:" + mAppInfo.packageName));
                    startActivity(intent);
                }
                break;
            case R.id.tv_start:
                //通过桌面去启动指定的应用
                PackageManager pm = getPackageManager();
                //通过Launcher开启指定包名意图，开启意图对象去开启应用
                Intent launchIntentForPackage = pm.getLaunchIntentForPackage(mAppInfo.packageName);
                if (launchIntentForPackage != null) {
                    startActivity(launchIntentForPackage);
                } else {
                    ToastUtil.show(getApplicationContext(), "此应用不能被开启");
                }
                break;
            case R.id.tv_share:
                //分享到第三方社交平台，此处通过短信应用向外发送短信
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, "分享一个应用，应用名称为：" + mAppInfo.name);
                //设置data和type
                intent.setType("text/plain");
                startActivity(intent);
                break;
        }
        //点击popup菜单执行新任务后，使其消失
        popupWindow.dismiss();
    }

    /**
     * 从另一个界面回退后执行的方法
     */
    @Override
    protected void onResume() {
        //再一次获取数据
        getData();
        super.onResume();
    }

    private class MyAdapter extends BaseAdapter {

        /**
         * 获取适配器中条目类型的总数，修改成两种（纯文字，图片＋文字）
         *
         * @return
         */
        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount() + 1;
        }

        /**
         * 指定索引指向条目类型（条目状态码指定0（复用系统），1）
         *
         * @param position
         * @return
         */
        @Override
        public int getItemViewType(int position) {
            if (position == 0 || position == mCustomerList.size() + 1) {
                //返回0，代表纯文本条目状态码
                return 0;
            } else {
                //返回1．代表图片＋文字条目状态码
                return 1;
            }
        }

        //在ListView中添加两个描述条目

        @Override
        public int getCount() {
            //多出的两条为标题TextView
            return mAppInfoList.size() + 2;
        }

        /**
         * 获取ListView的条目
         *
         * @param position
         * @return
         */
        @Override
        public AppInfo getItem(int position) {
            if (position == 0 || position == mCustomerList.size() + 1) {
                return null;
            } else {
                if (position < mCustomerList.size() + 1) {
                    return mCustomerList.get(position - 1);
                } else {
                    //返回对应系统条目对象
                    return mSystemList.get(position - mCustomerList.size() - 2);
                }
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);

            if (type == 0) {
                //展示灰色纯文本条目
                ViewTitleHolder holder = null;
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.listview_app_item_title, null);

                    holder = new ViewTitleHolder();
                    holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewTitleHolder) convertView.getTag();
                }
                if (position == 0) {
                    holder.tv_title.setText("用户应用(" + mCustomerList.size() + ")");
                } else {
                    holder.tv_title.setText("系统应用(" + mSystemList.size() + ")");
                }
                return convertView;
            } else {
                //展示图片＋文字条目
                ViewHolder holder = null;
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.listview_app_item, null);

                    holder = new ViewHolder();
                    holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                    holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                    holder.tv_path = (TextView) convertView.findViewById(R.id.tv_path);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.iv_icon.setBackgroundDrawable(getItem(position).icon);
                holder.tv_name.setText(getItem(position).name);
                if (getItem(position).isSdCard) {
                    holder.tv_path.setText("sd卡应用");
                } else {
                    holder.tv_path.setText("手机应用");
                }

                return convertView;
            }
        }
    }

    public class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_path;
    }

    public class ViewTitleHolder {
        TextView tv_title;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_manager);
        //加载显示的ProgressBar
        //初始化标题
        initTitle();
        //初始化应用程序显示列表
        initList();
    }

    /**
     * 初始化应用程序列表的实现
     */
    private void initList() {
        lv_app_list = (ListView) findViewById(R.id.lv_app_list);
        tv_des = (TextView) findViewById(R.id.tv_des);
        //执行获取数据的方法
        getData();
        //注册ListView滚动监听事件
        lv_app_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                /*
                *
                * 滚动过程中调用方法
                * AbsListView中的View就是ListView对象
                * firstVisibleItem代表第一个可见条目
                * totalItemCount代表当前屏幕可见条目总数
                *
                * */
                if (mCustomerList != null && mSystemList != null) {
                    if (firstVisibleItem >= mCustomerList.size() + 1) {
                        //滚动到了系统条目
                        tv_des.setText("系统应用(" + mSystemList.size() + ")");
                    } else {
                        //滚动到了用户应用条目
                        tv_des.setText("用户应用(" + mCustomerList.size() + ")");
                    }
                }
            }
        });

        //注册条目点击事件
        lv_app_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * @param parent
             * @param view  点中条目的view对象
             * @param position
             * @param id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || position == mCustomerList.size() + 1) {
                    return;
                } else {
                    if (position < mCustomerList.size() + 1) {
                        mAppInfo = mCustomerList.get(position - 1);
                    } else {
                        //返回对应系统条目对象
                        mAppInfo = mSystemList.get(position - mCustomerList.size() - 2);
                    }
                    //弹出窗体
                    showPopupWindow(view);
                }
            }
        });
    }

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                mAppInfoList = AppInfoProvider.getAppInfoList(getApplicationContext());

                //将所有的应用分类
                mSystemList = new ArrayList<AppInfo>();
                mCustomerList = new ArrayList<AppInfo>();
                for (AppInfo appInfo : mAppInfoList) {
                    if (appInfo.isSystem) {
                        //系统应用
                        mSystemList.add(appInfo);
                    } else {
                        //用户应用
                        mCustomerList.add(appInfo);
                    }
                }

                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    /**
     * 弹出窗体
     */
    private void showPopupWindow(View view) {
        View popupView = View.inflate(getApplicationContext(), R.layout.popupwindow_layout, null);

        TextView tv_uninstall = (TextView) popupView.findViewById(R.id.tv_uninstall);
        TextView tv_start = (TextView) popupView.findViewById(R.id.tv_start);
        TextView tv_share = (TextView) popupView.findViewById(R.id.tv_share);

        tv_uninstall.setOnClickListener(this);
        tv_start.setOnClickListener(this);
        tv_share.setOnClickListener(this);

        //透明动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);//保留动画最初位置

        //缩放动画,依赖于x,y的中间做扩展
        ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setFillAfter(true);

        //设置动画集合，让其同时执行
        AnimationSet animationSet = new AnimationSet(true);//true共享插补器，即共享一个数学函数
        //添加两个动画
        animationSet.addAnimation(alphaAnimation);
        animationSet.addAnimation(scaleAnimation);

        //执行动画
        popupView.startAnimation(animationSet);


        //1.创建窗体对象，指定宽高
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        //2.设置透明背景，使其响应回退按钮,ColorDrawable对象没有设置参数即为透明背景
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        //3.指定窗体位置
        popupWindow.showAsDropDown(view, 350, -view.getHeight());
    }

    private void initTitle() {
        //1.获取磁盘(手机内存，内部存储，不是运行内存)可用大小，磁盘的路径
        String path = Environment.getDataDirectory().getAbsolutePath();
        //2.获取sd(外部存储)卡的可用大小
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        //3.获取以上两个路径下文件夹的可用大小
        String memoryAvailSpace = Formatter.formatFileSize(this, getAvailSpace(path));
        String sdMemoryAvailSpace = Formatter.formatFileSize(this, getAvailSpace(sdPath));

        TextView tv_memory = (TextView) findViewById(R.id.tv_memory);
        TextView tv_sd_memory = (TextView) findViewById(R.id.tv_sd_memory);

        tv_memory.setText("手机可用：" + memoryAvailSpace);
        tv_sd_memory.setText("磁盘可用：" + sdMemoryAvailSpace);
    }

    /**
     * StatFs对象调用getBlockSize方法获取区块的大小返回值结果为byte,如果使用int 类型的返回值，则最大只能代表两个G，所以使用long类型返回值
     *
     * @param path
     * @return
     */
    private long getAvailSpace(String path) {
        //获取磁盘可用大小,使用StatFs类
        StatFs statFs = new StatFs(path);
        //1.获取可用区块的个数
        long count = statFs.getAvailableBlocks();
        //2.区块的大小
        long size = statFs.getBlockSize();
        //区块大小＊可用控件个数＝可用空间大小
        return count * size;
    }


}
