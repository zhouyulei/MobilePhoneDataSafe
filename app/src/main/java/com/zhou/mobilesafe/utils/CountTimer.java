package com.zhou.mobilesafe.utils;


import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;

import com.zhou.mobilesafe.activity.HomeLoginActivity;

/**
 * Created by zhouyulei on 2019/2/25.
 */

public class CountTimer extends CountDownTimer {

    private Context context;

    /**
     * 参数 millisInFuture       倒计时总时间（如60S，120s等）
     * 参数 countDownInterval    渐变时间（每次倒计1s）
     */
    public CountTimer(long millisInFuture, long countDownInterval, Context context) {
        super(millisInFuture, countDownInterval);
        this.context = context;
    }

    // 计时完毕时触发
    @Override
    public void onFinish() {

        //防止再按返回键再跳回原来界面，需要将原来的context的那个Activity界面再栈里面删除掉
        Intent intent=new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, HomeLoginActivity.class);
        context.startActivity(intent);

        //context.startActivity(new Intent(context, HomeLoginActivity.class));
    }

    // 计时过程显示
    @Override
    public void onTick(long millisUntilFinished) {

    }

}
