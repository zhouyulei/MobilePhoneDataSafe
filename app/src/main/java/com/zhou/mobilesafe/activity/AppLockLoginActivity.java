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

public class AppLockLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applocklogin);

        showDialog();
    }

    /*public void clickSetpsd(View view) {
        showDialog();
    }*/

    /**
     * 进入程序所界面之前 需要先输入密码的逻辑
     */
    private void showDialog() {
        String pwd = SpUtil.getString(this, ConstantValue.LOGIN_APPLOCK_PSD, "");
        //TextUtils.isEmpty(pwd)  与 pwd.equals("") 效果相同
        if (pwd.equals("")){
            //初始设置密码，弹出对话框
            showSetPsdDialog();
        }else{
            //进入应用手机AppLockActivity
            Intent intent = new Intent(getApplicationContext(), AppLockActivity.class);
            startActivity(intent);
        }
    }

    /**
     * 初始进入程序锁界面 需要 设置 密码 对话框
     */
    private void showSetPsdDialog() {
        //因为需要去自己定义对话框的展示样式,所以需要调用dialog.setView(view);
        //view是由自己编写的xml转换成的view对象xml----->view
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        final android.app.AlertDialog dialog = builder.create();
        final View view = View.inflate(this, R.layout.dialog_setapplock_psd, null);
        //让对话框显示一个自己定义的对话框界面效果
        //dialog.setView(view,0,0,0,0); 兼容低版本的写法
        dialog.setView(view);
        dialog.show();

        //注意在 view上 找到按钮控件
        Button bt_submit = view.findViewById(R.id.bt_submit);
        Button bt_cancel = view.findViewById(R.id.bt_cancel);

        //给对话框中按钮的 确认和取消 按钮 设置点击事件
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //找到 输入框 控件 id 这些必须在这个内部类里面去写，否则在这个内部类里面去调外面的方法 外面需要加final
                //在这个内部的逻辑会出错
                EditText et_set_psd = view.findViewById(R.id.et_set_psd);
                EditText et_confirm_psd = view.findViewById(R.id.et_confirm_psd);

                //取出输入框中 密码的内容
                String psd = et_set_psd.getText().toString();
                String confirmPsd = et_confirm_psd.getText().toString();

                if (!TextUtils.isEmpty(psd) && !TextUtils.isEmpty(confirmPsd)){
                    if (psd.equals(confirmPsd)){
                        //进入应用手机AppLockActivity
                        Intent intent = new Intent(getApplicationContext(), AppLockActivity.class);
                        startActivity(intent);
                        //跳转到新的界面以后隐藏对话框
                        dialog.dismiss();
                        //将本次设置的密码存储到sp中
                        SpUtil.putString(getApplication(),ConstantValue.LOGIN_APPLOCK_PSD, Md5Util.encoder(psd));
                    }else{
                        ToastUtil.show(getApplication(),"确认密码错误");
                    }
                }else{
                    ToastUtil.show(getApplication(),"两次输入密码都不能为空");
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //点击取消按钮，跳转到主界面
                Intent intent=new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(getApplicationContext(), HomeActivity.class);
                getApplicationContext().startActivity(intent);
            }
        });
    }

}
