package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.utils.ConstantValue;
import com.panhongyuan.mobilesafe.utils.SpUtil;

/**
 * Created by pan on 17-3-27.
 */

public class ToastLocationActivity extends Activity {

    private ImageView iv_drag;
    private Button bt_top;
    private Button bt_bottom;
    private WindowManager mWM;
    private int mScreenWidth;
    private int mScreenHeight;
    private long[] mHits = new long[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toast_location);
        //初始化控件
        initUI();
    }

    /**
     * 初始化控件的方法
     */
    private void initUI() {
        iv_drag = (ImageView) findViewById(R.id.iv_drag);
        bt_top = (Button) findViewById(R.id.bt_top);
        bt_bottom = (Button) findViewById(R.id.bt_bottom);

        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);
        mScreenHeight = mWM.getDefaultDisplay().getHeight();
        mScreenWidth = mWM.getDefaultDisplay().getWidth();

        int locationX = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_X, 0);
        int locationY = SpUtil.getInt(getApplicationContext(), ConstantValue.LOCATION_Y, 0);
        //ImageView在相对布局中，所以其所在位置的规则由相对布局提供
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        //将左上角的坐标作用在iv_drag对应规则的参数上
        layoutParams.leftMargin = locationX;
        layoutParams.topMargin = locationY;
        //将参数设置给iv_drag
        iv_drag.setLayoutParams(layoutParams);

        //当ImageView的位置在屏幕上半边时，底部的按钮为显示状态
        if (locationY > mScreenHeight / 2) {
            bt_bottom.setVisibility(View.INVISIBLE);
            bt_top.setVisibility(View.VISIBLE);
        } else {
            bt_bottom.setVisibility(View.VISIBLE);
            bt_top.setVisibility(View.INVISIBLE);
        }

        //监听点击事件
        iv_drag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1,原数组(要被拷贝的数组)
                //2,原数组的拷贝起始位置索引值
                //3,目标数组(原数组的数据---拷贝-->目标数组)
                //4,目标数组接受值的起始索引位置
                //5,拷贝的长度
                System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);//复制数组
                mHits[mHits.length - 1] = SystemClock.uptimeMillis();//获取系统时间戳
                if (mHits[mHits.length - 1] - mHits[0] < 500) {
                    //满足双击事件后使ImageView控件定位到屏幕中间位置，使图片的中心点与屏幕的中心点重合
                    int left = mScreenWidth / 2 - iv_drag.getWidth() / 2;
                    int top = mScreenHeight / 2 - iv_drag.getHeight() / 2;
                    int right = mScreenWidth / 2 + iv_drag.getWidth() / 2;
                    int bottom = mScreenHeight / 2 + iv_drag.getHeight() / 2;

                    //控件按以上规则作显示
                    iv_drag.layout(left, top, right, bottom);

                    //存储最终位置
                    SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, iv_drag.getLeft());
                    SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, iv_drag.getTop());
                }
            }
        });


        //监听ImageView控件的触摸事件
        iv_drag.setOnTouchListener(new View.OnTouchListener() {

            private int startX;
            private int startY;

            //对不同事件做出不同处理
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //按下时
                        //int startX = (int) event.getX();//以自己作为参照，获取触摸的点x轴的坐标值
                        //以屏幕作为参照，距离原点的距离
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //移动时
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();

                        int disX = moveX - startX;
                        int disY = moveY - startY;

                        //1.当前控件各个边距离屏幕左上方的位置
                        int left = iv_drag.getLeft() + disX;
                        int top = iv_drag.getTop() + disY;
                        int right = iv_drag.getRight() + disX;
                        int bottom = iv_drag.getBottom() + disY;

                        //移动之后左边缘不能超出屏幕,右边缘不能超出屏幕,上边缘不能小于0,下边缘不能超出屏幕
                        if (left < 0 || right > mScreenWidth || top < 0 || bottom > mScreenHeight - 22) {
                            return true;
                        }

                        //当ImageView的位置在屏幕上半边时，底部的按钮为显示状态
                        if (top > mScreenHeight / 2) {
                            bt_bottom.setVisibility(View.INVISIBLE);
                            bt_top.setVisibility(View.VISIBLE);
                        } else {
                            bt_bottom.setVisibility(View.VISIBLE);
                            bt_top.setVisibility(View.INVISIBLE);
                        }

                        //2.告知移动的控件，按计算出来的坐标去展示
                        iv_drag.layout(left, top, right, bottom);

                        //3.重置一次坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();

                        break;
                    case MotionEvent.ACTION_UP:
                        //4.离开屏幕时，存储移动的位置
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_X, iv_drag.getLeft());
                        SpUtil.putInt(getApplicationContext(), ConstantValue.LOCATION_Y, iv_drag.getTop());

                        break;
                }
                //在没有实现点击事件的情况下，返回false则不响应事件，返回true才会响应事件
                //既要响应点击事件，又要响应拖拽过程，则需要返回false，在源码中，onclick方法在手抬起之后执行
                return false;
            }
        });
    }
}
