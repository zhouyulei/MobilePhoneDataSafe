package com.zhou.mobilesafe.activity;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.utils.ConstantValue;
import com.zhou.mobilesafe.utils.Md5Util;
import com.zhou.mobilesafe.utils.SpUtil;
import com.zhou.mobilesafe.utils.ToastUtil;

public class EnterPsdActivity extends AppCompatActivity {

    private TextView tv_app_name;
    private ImageView iv_app_icon;
    private EditText et_psd;
    private Button bt_submit;
    private String packagename;
    private Button bt_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_psd);

        //获取包名
        packagename = getIntent().getStringExtra("packagename");
        initUI();

        initData();

    }

    private void initData() {

        //通过传递过来的包名获取拦截应用的图标以及名称
        PackageManager pm = getPackageManager();

        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packagename, 0);
            Drawable icon = applicationInfo.loadIcon(pm);
            iv_app_icon.setBackgroundDrawable(icon);
            tv_app_name.setText(applicationInfo.loadLabel(pm).toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String psd = et_psd.getText().toString();
                //取出 sp中存储的密码  和 输入框中 密码的内容
                //注意 从sp中取出来的密码 是经过加密的
                String getPsd = SpUtil.getString(getApplication(), ConstantValue.LOGIN_APPLOCK_PSD, "");
                if(!TextUtils.isEmpty(psd)){
                    //那么这个 用 et 输入进来的密码也需要 进行加密之后和sp里面取出来的进行比对
                    if(getPsd.equals(Md5Util.encoder(psd))){

                        ToastUtil.show(getApplicationContext(), "密码输入正确，即将跳转！");

                        //解锁,进入应用,告知看门口不要再去监听以及解锁的应用,发送广播
                        Intent intent = new Intent("android.intent.action.SKIP");
                        intent.putExtra("packagename",packagename);
                        sendBroadcast(intent);

                        finish();
                    }else{
                        ToastUtil.show(getApplicationContext(), "密码错误，请重新输入");
                    }
                }else{
                    ToastUtil.show(getApplicationContext(), "请输入密码");
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //清空密码
                et_psd.setText("");

                //onBackPressed();

                /*Intent intent = new Intent();// 创建Intent对象
                intent.setAction(Intent.ACTION_MAIN);// 设置Intent动作
                intent.addCategory(Intent.CATEGORY_HOME);// 设置Intent种类
                startActivity(intent);// 将Intent传递给Activity*/
            }
        });
    }

    /*初始化UI界面*/
    private void initUI() {
        tv_app_name = (TextView) findViewById(R.id.tv_app_name);
        iv_app_icon = (ImageView) findViewById(R.id.iv_app_icon);

        et_psd = (EditText) findViewById(R.id.et_psd);
        bt_submit = (Button) findViewById(R.id.bt_submit);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);

    }

    /*@Override
    public void onBackPressed() {

        finish();

        //通过隐式意图，跳转到桌面
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }*/

    /*禁用返回键*/
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK)
            return true;//不执行父类点击事件
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
}
