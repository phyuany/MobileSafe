package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.db.domain.ProcessInfo;
import com.panhongyuan.mobilesafe.engine.ProcessInfoProvider;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pan on 17-4-8.
 */

public class ProcessManagerActivity extends Activity implements View.OnClickListener {

    private TextView tv_memory_info;
    private TextView tv_process_count;
    private ListView lv_process_list;
    private Button bt_all;
    private Button bt_reverse;
    private Button bt_clear;
    private Button bt_setting;
    private List<ProcessInfo> mProcessInfoList;
    private ArrayList<ProcessInfo> mSystemList;
    private ArrayList<ProcessInfo> mCustomerList;
    private MyAdapter myAdapter;
    private TextView tv_des;
    private ProcessInfo mProcessInfo;
    private int mProcessCount;
    private long mAvailSpace;
    private long mStrTotalSpace;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            myAdapter = new MyAdapter();
            lv_process_list.setAdapter(myAdapter);
        }
    };

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
            if (SpUtil.getBoolean(getApplicationContext(), ConstantValue.SHOW_SYSTEM, false)) {
                //多出的两条为标题TextView,以下代码一定不要返回 mProcessInfoList.size() + 2, 而应该返回 mCustomerList.size() + mSystemList.size() + 2，否则在删除条目之后会造成所以不匹配的出 NAR 错误
                return mCustomerList.size() + mSystemList.size() + 2;
            } else {
                return mCustomerList.size();
            }
        }

        /**
         * 获取ListView的条目
         *
         * @param position
         * @return
         */
        @Override
        public ProcessInfo getItem(int position) {
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
                    holder.tv_title.setText("用户进程(" + mCustomerList.size() + ")");
                } else {
                    holder.tv_title.setText("系统进程(" + mSystemList.size() + ")");
                }
                return convertView;
            } else {
                //展示图片＋文字条目
                ViewHolder holder = null;
                if (convertView == null) {
                    convertView = View.inflate(getApplicationContext(), R.layout.listview_process_item, null);

                    holder = new ViewHolder();
                    holder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                    holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                    holder.tv_memory_info = (TextView) convertView.findViewById(R.id.tv_memory_info);
                    holder.cb_box = (CheckBox) convertView.findViewById(R.id.cb_box);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.iv_icon.setBackgroundDrawable(getItem(position).icon);
                holder.tv_name.setText(getItem(position).name);
                String strSize = Formatter.formatFileSize(getApplicationContext(), getItem(position).memSize);
                holder.tv_memory_info.setText(strSize);

                //如果进程条目为本应用，使它不能被选中，即将Check消失(隐藏)
                if (getItem(position).packageName.equals(getPackageName())) {
                    holder.cb_box.setVisibility(View.GONE);
                } else {
                    holder.cb_box.setVisibility(View.VISIBLE);
                }

                holder.cb_box.setChecked(getItem(position).isCheck);

                return convertView;
            }
        }
    }

    public class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_memory_info;
        CheckBox cb_box;
    }

    public class ViewTitleHolder {
        TextView tv_title;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_manager);
        //初始化控件
        initUI();
        //初始化标题数据
        initTitleData();
        //初始化列表数据
        initListData();
    }

    /**
     * 初始化列表数据的方法
     */
    private void initListData() {
        getData();
    }

    /**
     * 初始化标题数据的方法
     */
    private void initTitleData() {
        mProcessCount = ProcessInfoProvider.getProcessCount(getApplicationContext());
        tv_process_count.setText("进程总数:" + mProcessCount);

        //获取可用内存大小，并且格式化
        mAvailSpace = ProcessInfoProvider.getAvailSpace(getApplicationContext());
        String strAvailSpace = Formatter.formatFileSize(getApplicationContext(), mAvailSpace);

        //获取运行内存大小，并且格式化
        mStrTotalSpace = ProcessInfoProvider.getTotalSpace(getApplicationContext());
        String strTotalSpace = Formatter.formatFileSize(getApplicationContext(), mStrTotalSpace);

        tv_memory_info.setText("剩余/总共:" + strAvailSpace + "/" + strTotalSpace);
    }

    /**
     * 初始化控件的方法
     */
    private void initUI() {
        tv_process_count = (TextView) findViewById(R.id.tv_process_count);
        tv_memory_info = (TextView) findViewById(R.id.tv_memory_info);

        tv_des = (TextView) findViewById(R.id.tv_des);
        lv_process_list = (ListView) findViewById(R.id.lv_process_list);

        bt_all = (Button) findViewById(R.id.bt_select_all);
        bt_reverse = (Button) findViewById(R.id.bt_select_reverse);
        bt_clear = (Button) findViewById(R.id.bt_clear);
        bt_setting = (Button) findViewById(R.id.bt_setting);

        bt_all.setOnClickListener(this);
        bt_reverse.setOnClickListener(this);
        bt_clear.setOnClickListener(this);
        bt_setting.setOnClickListener(this);

        //注册ListView滚动监听事件
        lv_process_list.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                        tv_des.setText("系统进程(" + mSystemList.size() + ")");
                    } else {
                        //滚动到了用户应用条目
                        tv_des.setText("用户进程(" + mCustomerList.size() + ")");
                    }
                }
            }
        });
        //注册ListView点击条目监听事件
        lv_process_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
                        mProcessInfo = mCustomerList.get(position - 1);
                    } else {
                        //返回对应系统条目对象
                        mProcessInfo = mSystemList.get(position - mCustomerList.size() - 2);
                    }
                    if (mProcessInfo != null && !mProcessInfo.packageName.equals(getPackageName())) {
                        //选中条目不为空并且和本应用包名不一致，再去做状态取反操作
                        mProcessInfo.isCheck = !mProcessInfo.isCheck;
                        //通过选中条目的对象，找到对应的CheckBox
                        CheckBox cb_box = (CheckBox) view.findViewById(R.id.cb_box);
                        cb_box.setChecked(mProcessInfo.isCheck);
                    }
                }
            }
        });
    }

    /**
     * 实现点击方法
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_select_all:
                selectAll();
                break;
            case R.id.bt_select_reverse:
                selectReverse();
                break;
            case R.id.bt_clear:
                clearAll();
                break;
            case R.id.bt_setting:
                setting();
                break;
        }
    }

    /**
     * 设置的实现方法
     */
    private void setting() {
        Intent intent = new Intent(getApplicationContext(), ProcessSettingActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //通知数据适配器刷新
        if (myAdapter != null) {
            myAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 清理选中的进程
     */
    private void clearAll() {
        //1.获取选中的进程
        //2.创建一个耀杀死的进程的集合
        List<ProcessInfo> killProcessInfoList = new ArrayList<ProcessInfo>();
        for (ProcessInfo processInfo : mCustomerList) {
            if (processInfo.packageName.equals(getPackageName())) {
                continue;//终止本次循环，继续下一次循环
            }
            if (processInfo.isCheck) {
                //被选中的条目，不能在集合循环过程中不能移除集合中的对象
                // 3.记录要杀死的用户进程
                killProcessInfoList.add(processInfo);
            }
        }
        for (ProcessInfo processInfo : mSystemList) {
            if (processInfo.isCheck) {
                //被选中的条目，不能在集合循环过程中不能移除集合中的对象
                // 4.记录要杀死的系统进程
                killProcessInfoList.add(processInfo);
            }
        }
        //5.循环遍历killProcessList,然后移除mCustomerList和mSystemList中的对象
        long totalReleaseSpace = 0;//定义释放的空间
        for (ProcessInfo processInfo : killProcessInfoList) {
            //6.判断当前进程所在集合，然后从集合中移除
            if (mCustomerList.contains(processInfo)) {
                mCustomerList.remove(processInfo);
            }
            if (mSystemList.contains(processInfo)) {
                mSystemList.remove(processInfo);
            }
            //7.杀死记录在killProcessInfoList中的进程
            ProcessInfoProvider.killProcess(getApplicationContext(), processInfo);
            //记录所放空间的大小
            totalReleaseSpace += processInfo.memSize;
        }
        //8.在集合改变之后，通知数据适配器刷新UI
        if (myAdapter != null) {
            myAdapter.notifyDataSetChanged();
        }
        //9.进程总数的更新：原进程总数 - 杀死的进程总数
        mProcessCount -= killProcessInfoList.size();
        //10.更新可用剩余空间,（释放空间 + 原有空间 = 当前剩余空间）
        mAvailSpace += totalReleaseSpace;
        //11.更新进程总数和剩余空间的大小
        tv_process_count.setText("进程总数:" + mProcessCount);
        tv_memory_info.setText("剩余空间/总共空间" + Formatter.formatFileSize(getApplicationContext(), mAvailSpace) + "/" + Formatter.formatFileSize(getApplicationContext(), mStrTotalSpace));
        //12.通过toast告知用户释放的空间和杀死的进程,使用占位符指定数据，格式化输出
        String totalRelease = Formatter.formatFileSize(getApplicationContext(), totalReleaseSpace);
        ToastUtil.show(getApplicationContext(), String.format("杀死了%d个进程，释放了%s空间", killProcessInfoList.size(), totalRelease));
    }

    private void selectReverse() {
        //1.将所有集合中的isCheck设置为false
        for (ProcessInfo processInfo : mCustomerList) {
            if (processInfo.packageName.equals(getPackageName())) {
                continue;//终止本次循环，继续下一次循环
            }
            processInfo.isCheck = !processInfo.isCheck;
        }
        for (ProcessInfo processInfo : mSystemList) {
            processInfo.isCheck = !processInfo.isCheck;
        }
        //2.通知适配器刷新
        if (myAdapter != null) {
            myAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 全选实现的方法
     */
    private void selectAll() {
        //1.将所有集合中的isCheck设置为true
        for (ProcessInfo processInfo : mCustomerList) {
            if (processInfo.packageName.equals(getPackageName())) {
                continue;//终止本次循环，继续下一次循环
            }
            processInfo.isCheck = true;
        }
        for (ProcessInfo processInfo : mSystemList) {
            processInfo.isCheck = true;
        }
        //2.通知适配器刷新
        if (myAdapter != null) {
            myAdapter.notifyDataSetChanged();
        }
    }

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                mProcessInfoList = ProcessInfoProvider.getProcessInfo(getApplicationContext());

                //将所有的应用分类
                mSystemList = new ArrayList<ProcessInfo>();
                mCustomerList = new ArrayList<ProcessInfo>();

                for (ProcessInfo info : mProcessInfoList) {
                    if (info.isSystem) {
                        //系统应用
                        mSystemList.add(info);
                    } else {
                        //用户应用
                        mCustomerList.add(info);
                    }
                }

                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }
}
