package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.panhongyuan.mobilesafe.R;

/**
 * Created by pan on 17-3-16.
 */

public abstract class BaseSetupActivity extends Activity {
    private GestureDetector gestureDetector;//手势管理对象

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //创建手势管理对象，由手势管理对象管理手势动作
        //监听手势的移动
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //监听手势的移动
                if (e1.getX() - e2.getX() > 0) {
                    //由右向坐，调用子类的下一个方法，抽象方法
                    showNextPage();
                }
                if (e1.getX() - e2.getX() < 0) {
                    //由左向右，移动到上一页
                    showPrePage();
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    //监听屏幕上响应事件类型,1.屏幕按下，2.移动，3.抬起
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //2.通过手势处理类，接受多种事件，用作处理方法
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 下一个界面的抽象方法，由子类实现
     */
    protected abstract void showNextPage();

    /**
     * 上一个界面的抽象方法，由子类实现
     */
    protected abstract void showPrePage();

    /**
     * 下一页的点击事件
     *
     * @param view
     */
    public void nextPage(View view) {
        showNextPage();
    }

    /**
     * 下一页的点击事件
     *
     * @param view
     */
    public void prePage(View view) {
        showPrePage();
    }


}
