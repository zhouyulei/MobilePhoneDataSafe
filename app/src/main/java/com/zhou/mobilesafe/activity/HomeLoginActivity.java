package com.zhou.mobilesafe.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.utils.ConstantValue;
import com.zhou.mobilesafe.utils.Md5Util;
import com.zhou.mobilesafe.utils.SpUtil;
import com.zhou.mobilesafe.utils.ToastUtil;

public class HomeLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectContentView();
    }

    /**
     * 进入登入认证之前 需要选择 进入哪个 布局界面
     */
    private void selectContentView() {
        String pwd = SpUtil.getString(this, ConstantValue.LOGIN_SAFE_PSD, "");
        //TextUtils.isEmpty(pwd)  与 pwd.equals("") 效果相同
        if (pwd.equals("")){
            //初始设置密码
            setContentView(R.layout.activity_homelogin_setpsd);
            initUiSetPsd();
        }else{
            //再次进入 需要确认密码的逻辑
            setContentView(R.layout.activity_homelogin_confirmpsd);
            initUiConfirmPsd();
        }
    }


    /**
     * 初始进入手机登入认证界面 需要 设置 密码 对话框
     */
    private void initUiSetPsd() {

        Button bt_submit = findViewById(R.id.bt_submit);
        Button bt_cancel = findViewById(R.id.bt_cancel);


        //给对话框中按钮的 确认和取消 按钮 设置点击事件
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //找到 输入框 控件 id 这些必须在这个内部类里面去写，否则在这个内部类里面去调外面的方法 外面需要加final
                //在这个内部的逻辑会出错
                EditText et_set_psd = findViewById(R.id.et_set_psd);
                EditText et_confirm_psd = findViewById(R.id.et_confirm_psd);

                //取出输入框中 密码的内容
                String psd = et_set_psd.getText().toString();
                String confirmPsd = et_confirm_psd.getText().toString();

                if (!TextUtils.isEmpty(psd) && !TextUtils.isEmpty(confirmPsd)){
                    if (psd.equals(confirmPsd)){
                        //进入应用手机HomeActivity
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);

                        //将本次设置的密码存储到sp中
                        SpUtil.putString(getApplication(),ConstantValue.LOGIN_SAFE_PSD, Md5Util.encoder(psd));
                    }else{
                        ToastUtil.show(getApplication(),"确认密码错误,两次密码必须一致");
                    }
                }else{
                    ToastUtil.show(getApplication(),"两次输入密码都不能为空");
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过隐式意图，跳转到桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        });
    }
    /**
     * 再次进入 确认密码 对话框
     */
    private void initUiConfirmPsd() {

        //注意在 view上 找到按钮控件
        Button bt_submit = findViewById(R.id.bt_submit);
        Button bt_cancel = findViewById(R.id.bt_cancel);

        //给对话框中按钮的 确认和取消 按钮 设置点击事件
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //找到 输入框 控件 id 这些必须在这个内部类里面去写，否则在这个内部类里面去调外面的方法 外面需要加final
                //在这个内部的逻辑会出错
                EditText et_confirm_psd = findViewById(R.id.et_confirm_psd);

                //取出 sp中存储的密码  和 输入框中 密码的内容
                //注意 从sp中取出来的密码 是经过加密的
                String psd = SpUtil.getString(getApplication(), ConstantValue.LOGIN_SAFE_PSD, "");
                String confirmPsd = et_confirm_psd.getText().toString();

                if (!TextUtils.isEmpty(confirmPsd)){
                    //那么这个 用 et 输入进来的密码也需要 进行加密之后和sp里面取出来的进行比对
                    if (psd.equals(Md5Util.encoder(confirmPsd))){
                        //进入应用手机主界面模块，开启一个新的Activity
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);

                    }else{
                        ToastUtil.show(getApplication(),"确认密码错误");
                    }
                }else{
                    ToastUtil.show(getApplication(),"确认密码不能为空");
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过隐式意图，跳转到桌面
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        });
    }


}
