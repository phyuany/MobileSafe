package com.panhongyuan.mobilesafe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.panhongyuan.mobilesafe.R;

/**
 * Created by pan on 17-3-10.
 */

public class SettingClickView extends RelativeLayout {

    private TextView tv_des;
    private TextView tv_title;

    public SettingClickView(Context context) {
        this(context, null);
    }

    public SettingClickView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingClickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.setting_click_view, this);
        /*
        *等同于下面两行代码
        *View view = View.inflate(context, R.layout.setting_item_view, null);
        *this.addView(view);
        * */
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_des = (TextView) findViewById(R.id.tv_des);
    }

    /**
     * @param title 设置的标题内容
     */
    public void setTitle(String title) {
        tv_title.setText(title);
    }

    /**
     * @param description 设置的描述内容
     */
    public void setDes(String description) {
        tv_des.setText(description);
    }
}