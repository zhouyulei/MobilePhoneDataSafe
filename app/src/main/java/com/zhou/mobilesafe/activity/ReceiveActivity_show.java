package com.zhou.mobilesafe.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.utils.EncryptManager;

public class ReceiveActivity_show extends AppCompatActivity {

    private TextView Address_show;
    private TextView Time_show;
    private TextView Early_body_show;
    private TextView Late_body_show;
    //private String encryptPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_show);

        initUI();
    }

    private void initUI() {
        //找到显示 发送者的控件
        Address_show = (TextView) findViewById(R.id.address_show);
        //找到显示 接收时间的控件
        Time_show = (TextView) findViewById(R.id.time_show);
        //找到要显示 被加密后的密文原文本控件
        Early_body_show = (TextView) findViewById(R.id.early_body_show);
        //找到要显示 解析后的明文短信的文本控件
        Late_body_show = (TextView) findViewById(R.id.late_body_show);

        //接收内容和id
        Bundle bundle = this.getIntent().getExtras();
        String body = bundle.getString("body");
        String time = bundle.getString("time");
        String address = bundle.getString("address");
        String decryPassword = bundle.getString("decryptPassword");//获取解密密钥
        //encryptPassword = bundle.getString("encryptPassword");//获取加密密钥，用于对比

        //将接受内容 显示到相对应的控件上
        Address_show.setText(address);
        Early_body_show.setText(body);
        Time_show.setText(time);

        //对短信消息进行解密后显示在Late_body_show的textview中

        /*String real_content = EncryptManager.decrypt(decryPassword,body);
        Late_body_show.setText(real_content);*/

        if (!TextUtils.isEmpty(decryPassword)){
            String real_content = EncryptManager.decrypt(decryPassword,body);
            Late_body_show.setText(real_content);
        }else{
            Toast.makeText(getApplicationContext(),"解密密钥必须与加密密钥一致且不能为空",Toast.LENGTH_LONG).show();
        }
    }
}
