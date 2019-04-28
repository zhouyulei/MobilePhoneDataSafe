package com.zhou.mobilesafe.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Zhou on 2018/7/18.
 * 为了怕忘记写 .show() 直接将他封装成一个工具类，由类名直接掉静态方法得来
 */

public class ToastUtil {
    public static void show(Context ctx, String msg){
        Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show();
    }
}
