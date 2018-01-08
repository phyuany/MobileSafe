package com.panhongyuan.mobilesafe.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TabHost;

import com.panhongyuan.mobilesafe.R;

/**
 * Created by pan on 4/13/17.
 */

public class BaseCacheActivity extends TabActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_clear_cache);

        //1.生成选项卡1,newTabSpec参数可以作为一个唯一的标识
        TabHost.TabSpec tab1 = getTabHost().newTabSpec("clear_cache").setIndicator("缓存清理");
        //2.生成选项卡2
        TabHost.TabSpec tab2 = getTabHost().newTabSpec("sd_cache_clear").setIndicator("缓存清理");

        //3.告知选项卡后续操作
        tab1.setContent(new Intent(getApplicationContext(),CacheClearActivity.class));
        tab2.setContent(new Intent(getApplicationContext(),SDCacheActivity.class));

        //4.将所有选项卡维护到Host中
        getTabHost().addTab(tab1);
        getTabHost().addTab(tab2);
    }
}
