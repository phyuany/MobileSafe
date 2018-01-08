package com.panhongyuan.mobilesafe.service;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.panhongyuan.mobilesafe.R;
import com.panhongyuan.mobilesafe.activity.BackGroundActivity;

public class RacketService extends Service {
    //进程清理
    private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
    private WindowManager mWM;
    private View viewToast;
    private int mScreenWidth;
    private int mScreenHeight;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            mParams.y = (Integer) msg.obj;
            //更新到火箭上(窗体)
            mWM.updateViewLayout(viewToast, mParams);
        }

        ;
    };

    @Override
    public void onCreate() {
        //1,初始化窗体对象
        mWM = (WindowManager) getSystemService(WINDOW_SERVICE);

        //获取屏幕宽度
        mScreenWidth = mWM.getDefaultDisplay().getWidth();
        mScreenHeight = mWM.getDefaultDisplay().getHeight();

        //2,初始化小火箭布局
        showRocket();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (mWM != null && viewToast != null) {
            mWM.removeView(viewToast);
        }
        super.onDestroy();
    }

    private void showRocket() {
        //给吐司定义出来的参数(宽高,类型,触摸方式)
        final WindowManager.LayoutParams params = mParams;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE	因为吐司需要根据手势去移动,所以必须要能触摸
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        params.format = PixelFormat.TRANSLUCENT;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;//不在和吐司类型相互绑定,通话的类型相互绑定
        //将吐司放置在左上角显示
        params.gravity = Gravity.TOP + Gravity.LEFT;
        params.setTitle("Toast");

        //定义吐司布局xml--->view挂载到屏幕上

        viewToast = View.inflate(this, R.layout.rocket_view, null);

        ImageView iv_rocket = (ImageView) viewToast.findViewById(R.id.im_rock);
        //获取设置了动画的背景,然后让此背景执行
        AnimationDrawable drawable = (AnimationDrawable) iv_rocket.getBackground();
//		iv_rocket.startAnimation(drawable);
        //获取背景图片后,让其动起来
        drawable.start();

        iv_rocket.setOnTouchListener(new OnTouchListener() {
            private int startX;
            private int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //获取按下的xy坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //获取移动xy坐标和按下的xy坐标做差,做差得到的值小火箭移动的距离
                        //移动过程中做容错处理
                        //第一次移动到的位置,作为第二次移动的初始位置
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();

                        int disX = moveX - startX;
                        int disY = moveY - startY;

                        params.x = params.x + disX;
                        params.y = params.y + disY;

                        //在窗体中仅仅告知吐司的左上角的坐标
                        if (params.x < 0) {
                            params.x = 0;
                        }
                        if (params.y < 0) {
                            params.y = 0;
                        }

                        if (params.x > mScreenWidth - viewToast.getWidth()) {
                            params.x = mScreenWidth - viewToast.getWidth();
                        }

                        if (params.y > mScreenHeight - 22 - viewToast.getHeight()) {
                            params.y = mScreenHeight - 22 - viewToast.getHeight();
                        }

                        //告知吐司在窗体上刷新
                        mWM.updateViewLayout(viewToast, params);

                        //在第一次移动完成后,将最终坐标作为第二次移动的起始坐标
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        //手指放开的时候,如果放手坐标,则指定区域内
                        if (params.x > 100 && params.x < 800 && params.y > 300) {
                            //火箭的发射
                            sendRocket();
                            //在开启火箭过程中,去开启一个新的activity,activity透明,在此activity中放置两张图片(淡入淡出效果)
                            /*Intent intent = new Intent(getApplicationContext(),BackgroundActivity.class);
                            //指定开启新的activity任务栈
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);*/
                            Intent intent = new Intent(getApplicationContext(), BackGroundActivity.class);
                            //开启火箭后关闭了唯一的activity对应的任务栈，所以此处耀通知系统重新开启一个任务栈
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                        break;
                }
                return true;
            }
        });

        mWM.addView(viewToast, params);
    }

    protected void sendRocket() {
        new Thread() {
            public void run() {
                for (int i = 0; i < 30; i++) {
                    int y = 300 - i * 30;
                    //睡眠
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //通过消息机制,将y轴坐标作为主线程火箭竖直方向上的显示位置
                    Message msg = Message.obtain();
                    msg.obj = y;
                    mHandler.sendMessage(msg);
                }
            }

            ;
        }.start();

    }
}