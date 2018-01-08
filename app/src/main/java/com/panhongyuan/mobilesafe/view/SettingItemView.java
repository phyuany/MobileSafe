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

public class SettingItemView extends RelativeLayout {

    private static final String NAMESPACE = "http://schemas.android.com/apk/res/com.panhongyuan.mobilesafe";
    private CheckBox cb_box;
    private TextView tv_des;
    private String tag = "SettingItemView";
    private String mDestitle;
    private String mDesoff;
    private String mDeson;

    public SettingItemView(Context context) {
        this(context, null);
    }

    public SettingItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.setting_item_view, this);
        /*
        *等同于下面两行代码
        *View view = View.inflate(context, R.layout.setting_item_view, null);
        *this.addView(view);
        * */
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_des = (TextView) findViewById(R.id.tv_des);
        cb_box = (CheckBox) findViewById(R.id.cb_box);

        //获取自定义以及原生属性的操作在此处写代码
        initAttrs(attrs);
        //获取自定义的标题字符串赋值给自定义控件的标题
        tv_title.setText(mDestitle);
    }

    /**
     * @param attributeSet 构造方法中维护好的自定义属性
     */
    private void initAttrs(AttributeSet attributeSet) {
        //以下注释部分为测试代码
        /*Log.i(tag, "属性集合数目为：" + String.valueOf(attributeSet.getAttributeCount()));
        for (int i = 0; i < attributeSet.getAttributeCount(); i++) {
            Log.i(tag, "key:" + attributeSet.getAttributeName(i));
            Log.i(tag, "value:" + attributeSet.getAttributeValue(i));
        }*/
        //通过命名空间获取属性值
        mDestitle = attributeSet.getAttributeValue(NAMESPACE, "destitle");
        mDesoff = attributeSet.getAttributeValue(NAMESPACE, "desoff");
        mDeson = attributeSet.getAttributeValue(NAMESPACE, "deson");
        Log.i(tag, "mDesoff:" + mDesoff);
        Log.i(tag, "mDestitle:" + mDestitle);
        Log.i(tag, "mDeson:" + mDeson);
    }

    /**
     *
     * * @return 判断当前当前条目是否开启 返回当前View是否属于选中状态 true位开启，false位关闭
     */
    public boolean isCheck() {
        return cb_box.isChecked();
    }

    /**
     * 设置当前条目的选中状态，点击过程调用此方法
     *
     * @param isCheck 作为是否开启的变量，有点击过程中作决定
     */
    public void setCheck(boolean isCheck) {
        cb_box.setChecked(isCheck);
        if (isCheck) {
            tv_des.setText(mDeson);
        } else {
            tv_des.setText(mDesoff);
        }
    }

}