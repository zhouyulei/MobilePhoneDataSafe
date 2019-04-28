package com.zhou.mobilesafe.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.engine.SmsBackUp;
import java.io.File;

public class MessageEncryptActivity extends AppCompatActivity {

    private TextView tv_sms_backup;
    private ProgressBar pb_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messageencrypt);
        //初始化UI控件
        InitUI();

        //短信备份方法
        initSmsBackUp();
    }

    private void InitUI() {
        //找到发送加密AES密钥的按钮
        TextView send_aes=(TextView)findViewById(R.id.bt_send_aes);

        //找到发送短信的按钮
        TextView send_message=(TextView)findViewById(R.id.bt_send_message);
        //找到接收短信的按钮
        TextView receive=(TextView)findViewById(R.id.bt_receive);

        /*点击发送加密AES密钥的按钮 跳转到发送密钥的界面*/
        send_aes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MessageEncryptActivity.this,SendAESEncryptPsdActivity.class);
                startActivity(intent);
            }
        });

        /*点击发送按钮 跳转到发送短信的界面*/
        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MessageEncryptActivity.this,SendMessageActivity.class);
                startActivity(intent);
            }
        });

        /*点击接收按钮 跳转到接收到短信的界面*/
        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MessageEncryptActivity.this,ReceiveActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 短信备份的方法
     */
    private void initSmsBackUp() {
        tv_sms_backup = findViewById(R.id.tv_sms_backup);

        pb_bar = findViewById(R.id.pb_bar);

        tv_sms_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSmsBackUpDialog();
            }
        });
    }


    private void showSmsBackUpDialog() {
        //1,创建一个带进度条的对话框
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.ic_launcher02);
        progressDialog.setTitle("短信备份");
        //2,指定进度条的样式为水平
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //3,展示进度条
        progressDialog.show();

        //4,直接调用备份短信方法即可
        new Thread(){
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sms.xml";
                //使用对话框
                //SmsBackUp.backup(getApplicationContext(),path,progressDialog);

                //使用控件上的progressBar
                //SmsBackUp.backup(getApplicationContext(),path,pb_bar);

                //使用回调
                SmsBackUp.backup(getApplicationContext(), path, new SmsBackUp.CallBack() {
                    @Override
                    public void setMax(int max) {
                        progressDialog.setMax(max);  //用对话框去显示最大值
                        pb_bar.setMax(max);          //用进度条去显示最大值
                    }

                    @Override
                    public void setProgress(int index) {
                        progressDialog.setProgress(index); //用对话框去显示进度
                        pb_bar.setProgress(index);         //用进度条去显示最大值
                    }
                });

                progressDialog.dismiss();
            }
        }.start();

    }
}
