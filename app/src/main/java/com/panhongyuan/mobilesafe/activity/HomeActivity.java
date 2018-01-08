package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.Md5Util;
import com.panhongyuan.mobilesafe.utils.SpUtil;
import com.panhongyuan.mobilesafe.utils.ToastUtil;

/**
 * Created by pan on 17-3-8.    测试代码在AndroidStudio中都使用英文字母作为标签标记，shift+f11快捷键即可找到
 */

public class HomeActivity extends Activity {

    private GridView gv_home;
    private String[] mTitleStr;
    private int[] mDrawableIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //初始化UI
        initUI();
        //初始化数据
        initData();
    }

    /**
     * 初始化数据的方法
     */
    private void initData() {
        //准备数据，文字九组，图片九张
        mTitleStr = new String[]{
                "手机防盗", "通信卫士", "软件管理", "进程管理", "流量统计", "手机杀毒", "缓存清理", "高级工具", "设置中心"
        };
        mDrawableIds = new int[]{
                R.drawable.home_safe,
                R.drawable.home_callmsgsafe,
                R.drawable.home_apps,
                R.drawable.home_taskmanager,
                R.drawable.home_netmanager,
                R.drawable.home_trojan,
                R.drawable.home_sysoptimize,
                R.drawable.home_tools,
                R.drawable.home_settings
        };

        //九宫格设置数据适配器
        gv_home.setAdapter(new MyAdapter());
        //注册九宫格点击事件
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * @param parent
             * @param view
             * @param position  点中条目的索引
             * @param id
             */
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //开启对话框
                        showDialog();
                        break;
                    case 1:
                        //跳转到通信卫士模块
                        startActivity(new Intent(getApplicationContext(), BlackNumberActivity.class));
                        break;
                    case 2:
                        //
                        startActivity(new Intent(getApplicationContext(), AppManagerActivity.class));
                        break;
                    case 3:
                        //进程管理
                        startActivity(new Intent(getApplicationContext(), ProcessManagerActivity.class));
                        break;
                    case 4:
                        //流量管理
                        startActivity(new Intent(getApplicationContext(), TrafficActivity.class));
                        break;
                    case 5:
                        //手机杀毒
                        startActivity(new Intent(getApplicationContext(), AntiVirusActivity.class));
                        break;
                    case 6:
                        //缓存管理
                        startActivity(new Intent(getApplicationContext(),BaseCacheActivity.class));
                        //startActivity(new Intent(getApplicationContext(),CacheClearActivity.class));
                        break;
                    case 7:
                        //跳转到高级工具界面部分
                        startActivity(new Intent(getApplicationContext(), AToolActivity.class));
                        break;
                    case 8:
                        Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                        startActivity(intent);
                        //开启动画
                        overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
                        break;
                }
            }
        });
    }

    /**
     * 弹出密码确认对话框
     */
    private void showDialog() {
        String psd = SpUtil.getString(this, ConstantValue.MOBILE_SAFE_PSD, "");
        if (TextUtils.isEmpty(psd)) {
            //如果没有设置过密码，打开初始设置密码对话框
            showSetPsdDialog();
        } else {
            //如果之前设置过密码，打开确认密码对话框
            showConfirmPsdDialog();
        }
    }

    /**
     * 弹出设置密码的对话框
     */
    private void showSetPsdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //因为需要自定义对话框的样式，所以调用builder的create()方法和setView()方法
        final AlertDialog dialog = builder.create();

        final View view = View.inflate(getApplicationContext(), R.layout.dialog_set_psd, null);
        //在设置对话框时，设置左上右下的内边界距都为0
        dialog.setView(view, 0, 0, 0, 0);
        //将对话框显示出来
        dialog.show();

        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_set_psd = (EditText) view.findViewById(R.id.et_set_psd);
                EditText et_confirm_psd = (EditText) view.findViewById(R.id.et_confirm_psd);

                String psd = et_set_psd.getText().toString();
                String confirmPsd = et_confirm_psd.getText().toString();

                if (!TextUtils.isEmpty(psd) && !TextUtils.isEmpty(confirmPsd)) {
                    if (psd.equals(confirmPsd)) {
                        //进入新界面
                        Intent intent = new Intent(getApplicationContext(), SetupOverActivity.class);
                        startActivity(intent);
                        dialog.dismiss();

                        //存储密码
                        SpUtil.putString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD, Md5Util.encoder(confirmPsd));
                    } else {
                        ToastUtil.show(getApplicationContext(), "确认密码错误");
                    }
                } else {
                    ToastUtil.show(getApplicationContext(), "请设置密码");
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 确认密码的对话框
     */
    private void showConfirmPsdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //因为需要自定义对话框的样式，所以调用builder的create()方法和setView()方法
        final AlertDialog dialog = builder.create();

        final View view = View.inflate(getApplicationContext(), R.layout.dialog_confirm_psd, null);
        dialog.setView(view, 0, 0, 0, 0);
        //将对话框显示出来
        dialog.show();

        Button bt_submit = (Button) view.findViewById(R.id.bt_submit);
        Button bt_cancel = (Button) view.findViewById(R.id.bt_cancel);

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et_confirm_psd = (EditText) view.findViewById(R.id.et_confirm_psd);

                String confirmPsd = et_confirm_psd.getText().toString();

                if (!TextUtils.isEmpty(confirmPsd)) {
                    String psd = SpUtil.getString(getApplicationContext(), ConstantValue.MOBILE_SAFE_PSD, "");
                    if (psd.equals(Md5Util.encoder(confirmPsd))) {
                        //进入新界面
//                        Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                        Intent intent = new Intent(getApplication(), SetupOverActivity.class);
                        startActivity(intent);
                        //开启滑屏动画
                        overridePendingTransition(R.anim.next_in_anim, R.anim.next_out_anim);
                        dialog.dismiss();
                    } else {
                        ToastUtil.show(getApplicationContext(), "确认密码错误");
                    }
                } else {
                    ToastUtil.show(getApplicationContext(), "请输入密码");
                }
            }
        });
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        gv_home = (GridView) findViewById(R.id.gv_home);
    }

    /**
     * GridView的适配器
     */
    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            //返回条目的总数
            return mTitleStr.length;
        }

        @Override
        public Object getItem(int position) {
            return mTitleStr[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(), R.layout.gridview_item, null);
            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_title);

            tv_title.setText(mTitleStr[position]);
            iv_icon.setImageResource(mDrawableIds[position]);
            return view;
        }
    }
}
