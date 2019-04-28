package com.zhou.mobilesafe.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.utils.ConstantValue;
import com.zhou.mobilesafe.utils.EncryptManager;
import com.zhou.mobilesafe.utils.SpUtil;

public class SendMessageActivity extends AppCompatActivity {

    private IntentFilter sendFilter; //为动态的注册广播接收者提供重要的参数
    private SendStatusReceiver sendStatusReceiver;//创建广播接收者对象
    private EditText et_encrypt;
    private Button bt_select_number;
    private EditText et_phone_number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        InitView();
    }
    private void InitView() {
        //找到 加密aes密钥按钮的控件
        Button bt_send = (Button) findViewById(R.id.bt_send);
        //找到发送按钮的控件
        Button bt_cancel = (Button) findViewById(R.id.bt_cancel);
        //找到发送目的人的控件
        et_phone_number = (EditText) findViewById(R.id.phone_edit_text);

        //获取联系人电话号码回显过程
        String phone = SpUtil.getString(getApplicationContext(), ConstantValue.CONTACT_MESSAGE, "");
        et_phone_number.setText(phone);

        //点击选择联系人的对话框
        bt_select_number = findViewById(R.id.bt_select_number);
        //当点击选择联系人按钮之后 跳转到一个保存好的 联系人界面
        bt_select_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactListActivity.class);
                //需要从联系人界面 把结果返回
                startActivityForResult(intent,0);
            }
        });


        //加密密钥
        et_encrypt = (EditText) findViewById(R.id.et_encrypt);
        //获取联系人电话号码回显过程
        String aes_psd = SpUtil.getString(getApplicationContext(), ConstantValue.AES_PSD, "");
        et_encrypt.setText(aes_psd);


        //找到发送内容的控件
        final EditText msgInput = (EditText) findViewById(R.id.content_edit_text);

        //动态的去注册广播接收者 为发送短信设置要监听的广播
        sendStatusReceiver = new SendStatusReceiver();
        //创建IntentFilter 对象
        sendFilter = new IntentFilter();
        //添加要注册的 action
        sendFilter.addAction("SENT_SMS_ACTION");
        //参数准备妥当 动态地去注册发送短信的广播接收者
        registerReceiver(sendStatusReceiver, sendFilter);


        //点击发送 加密后的短信密文
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //获取目的人的真实号码
                String address = et_phone_number.getText().toString();
                if (TextUtils.isEmpty(address)){
                    Toast.makeText(getApplicationContext(),"输入联系人不能为空",Toast.LENGTH_LONG).show();
                    return;
                }

                //判断密钥是否为空
                if (TextUtils.isEmpty(et_encrypt.getText().toString())){
                    Toast.makeText(getApplicationContext(),"输入加密密钥不能为空",Toast.LENGTH_LONG).show();
                    return;
                }

                //获取发送短信的真实内容,AES加密以后的密文展示
                String content = msgInput.getText().toString();
                //判断短信内容是否为空
                if (TextUtils.isEmpty(content)){
                    Toast.makeText(getApplicationContext(),"输入短信内容不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                //短信内容AES加密
                String contents = EncryptManager.encrypt(et_encrypt.getText().toString(), content);

                Toast.makeText(getApplicationContext(), "加密发送中，请稍后...", Toast.LENGTH_SHORT).show();

                //配置发送短信的参数
                SmsManager smsManager = SmsManager.getDefault();
                Intent sentIntent = new Intent("SENT_SMS_ACTION");
                PendingIntent pi = PendingIntent.getBroadcast(SendMessageActivity.this, 0, sentIntent, 0);
                //短信已经通过密文发送出去
                smsManager.sendTextMessage(address, null, contents.toString(), pi, null);
            }
        });

        //点击取消按钮
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    class SendStatusReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResultCode() == RESULT_OK) {
                //发送成功
                Toast.makeText(context, "发送成功", Toast.LENGTH_LONG).show();
            } else {
                //发送失败
                Toast.makeText(context, "发送失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //返回到当前界面的时候，接收结果的方法
        if (data != null){
            String phone = data.getStringExtra("phone");
            //2,将特殊字符过滤(中划线转换成空字符串)  调用链
            phone = phone.replace("-", "").replace(" ", "").trim();
            et_phone_number.setText(phone);
            //3,存储联系人至sp中
            SpUtil.putString(getApplicationContext(), ConstantValue.CONTACT_MESSAGE,phone);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在Activity摧毁的时候停止监听
        unregisterReceiver(sendStatusReceiver);
    }
}
