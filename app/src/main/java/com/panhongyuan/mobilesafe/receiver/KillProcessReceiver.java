package com.panhongyuan.mobilesafe.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.panhongyuan.mobilesafe.engine.ProcessInfoProvider;
import com.panhongyuan.mobilesafe.service.RacketService;

/**
 * Created by pan on 17-4-9.
 */

public class KillProcessReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //杀死进程
        ProcessInfoProvider.killAll(context);
        //开启火箭清理的服务，此处代码后期再维护
        //context.startService(new Intent(context.getApplicationContext(), RacketService.class));
    }
}
