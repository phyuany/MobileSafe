package com.panhongyuan.mobilesafe.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by pan on 17-3-8.
 */

public class ToastUtil {
    public static void show(Context context, String string) {
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }
}
