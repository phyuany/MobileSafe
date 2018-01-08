package com.panhongyuan.mobilesafe.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * Created by pan on 17-3-10.
 * 能够获取焦点的TextView
 */

public class FocusTextView extends android.support.v7.widget.AppCompatTextView {
    //使用java代码构建控件
    public FocusTextView(Context context) {
        super(context);
    }

    //由系统调用（带属性+上下文构造方法）
    public FocusTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    //由系统调用（带属性+上下文+布局文件中定义样式的构造方法）
    public FocusTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //重写获取焦点的方法,在调用时，默认让其获取焦点
    @Override
    public boolean isFocused() {
        return true;
    }
}
