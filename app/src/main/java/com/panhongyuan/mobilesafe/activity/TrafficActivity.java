package com.panhongyuan.mobilesafe.activity;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.panhongyuan.mobilesafe.R;


/**
 * Created by pan on 4/13/17.
 */

public class TrafficActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic);
    }
}


/* //获取移动流量
        //获取流量(Receive下载流量)
        long mobileRxBytes = TrafficStats.getMobileRxBytes();
        //获取移动总流量
        //获取流量（Total(手机总流量：上传+下载)）
        long mobileTxBytes = TrafficStats.getMobileTxBytes();

        //下载总流量（移动+WIFI）
        //获取总下载流量
        long totalRxBytes = TrafficStats.getTotalRxBytes();
        //获取上传+下载流量
        long totalTxBytes = TrafficStats.getTotalTxBytes();
        */