package com.zhou.mobilesafe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.service.BlackNumberService;
import com.zhou.mobilesafe.service.WatchDogService;
import com.zhou.mobilesafe.utils.ConstantValue;
import com.zhou.mobilesafe.utils.ServiceUtil;
import com.zhou.mobilesafe.utils.SpUtil;
import com.zhou.mobilesafe.view.SettingClickView;
import com.zhou.mobilesafe.view.SettingItemView;

/**
 * Created by Zhou on 2018/7/19.
 */

public class SettingActivity extends AppCompatActivity{

    private SettingClickView scv_toast_style;
    private String[] mToastStyleDes;
    private int toast_style;
    private SettingItemView siv_blacknumber;
    private SettingItemView siv_app_lock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //版本更新的 开关 初始化UI
        initUpdate();

        //设置黑名单拦截设置的方法
        initBlackNumber();

        //设置开启程序锁的方法
        initAppLock();

    }

    /**
     * 设置开启程序锁的方法
     */
    private void initAppLock() {
        siv_app_lock = findViewById(R.id.siv_app_lock);
        //判断当前服务是否开启
        Boolean isRunning = ServiceUtil.isRunning(getApplicationContext(), "com.zhou.mobilesafe.service.WatchDogService");
        //把当前服务开启的状态与 条目的选中状态绑定
        siv_app_lock.setCheck(isRunning);

        //当条目点击的时候调用的方法
        siv_app_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_app_lock.isCheck();
                siv_app_lock.setCheck(!isCheck);

                if (!isCheck){
                    startService(new Intent(getApplicationContext(),WatchDogService.class));
                }else{
                    stopService(new Intent(getApplicationContext(),WatchDogService.class));
                }
            }
        });
    }

    //设置黑名单拦截设置的方法
    private void initBlackNumber() {
        siv_blacknumber = findViewById(R.id.siv_blacknumber);
        //判断当前服务是否开启
        Boolean isRunning = ServiceUtil.isRunning(getApplicationContext(), "com.zhou.mobilesafe.service.BlackNumberService");
        //把当前服务开启的状态与 条目的选中状态绑定
        siv_blacknumber.setCheck(isRunning);

        //当条目点击的时候调用的方法
        siv_blacknumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isCheck = siv_blacknumber.isCheck();
                siv_blacknumber.setCheck(!isCheck);

                if (!isCheck){
                    startService(new Intent(getApplicationContext(),BlackNumberService.class));
                }else{
                    stopService(new Intent(getApplicationContext(),BlackNumberService.class));
                }
            }
        });

    }


    /**
     * 版本更新开关
     */
    private void initUpdate() {

        //先找到控件位置
        final SettingItemView siv_update = findViewById(R.id.siv_update);

        //获取已有的开关状态，用作显示  将"open_date"封装到  ConstantValue.OPEN_UPDATE,这个默认值false,表示如果读取不到的情况下，默认为不更新
        boolean open_update = SpUtil.getBoolean(getApplication(), ConstantValue.OPEN_UPDATE, false);
        //状态是否被选中，由上次读取出来的结果做决定
        siv_update.setCheck(open_update);

        siv_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果之前是选中的,点击过后,变成未选中
                //如果之前是未选中的,点击过后,变成选中

                //获取之前的选中状态
                boolean isCheck = siv_update.isCheck();
                //将原有状态取反,等同上诉的两部操作
                siv_update.setCheck(!isCheck);

                //在改变了状态以后，需要写入到sp到，保存这个状态
                SpUtil.putBoolean(getApplication(),ConstantValue.OPEN_UPDATE,!isCheck);
            }
        });
    }
}
