package com.panhongyuan.mobilesafe.db.domain;

import android.graphics.drawable.Drawable;

/**
 * Created by pan on 17-4-6.
 */

public class AppInfo {
    //（名称，包名，图标，（内存，sd卡），（系统，用户）
    public String name;
    public String packageName;
    public Drawable icon;
    public boolean isSdCard;
    public boolean isSystem;
}
