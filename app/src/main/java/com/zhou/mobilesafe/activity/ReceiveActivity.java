package com.zhou.mobilesafe.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.utils.Base64Utils;
import com.zhou.mobilesafe.utils.RSAUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiveActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private TextView Tv_address;
    private TextView Tv_body;
    private TextView Tv_time;
    private ListView listview;
    private List<Map<String, Object>> dataList;
    private SimpleAdapter simple_adapter;
    private EditText et_decry00;
    private EditText et_decry01;
    private EditText et_decry02;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        //初始化UI控件
        InitUI();

        RefreshList();
    }

    private void InitUI() {
        //找到短信发送者的电话号码
        Tv_address = (TextView) findViewById(R.id.tv_address);
        //找到短信发送者的发送内容
        Tv_body = (TextView) findViewById(R.id.tv_body);
        //获取到短信接收到的时间
        Tv_time = (TextView) findViewById(R.id.tv_time);

        //解密密钥第一段
        et_decry00 = findViewById(R.id.et_decrypasswod00);

        //解密密钥第二段
        et_decry01 = findViewById(R.id.et_decrypasswod01);

        //解密密钥最终
        et_decry02 = findViewById(R.id.et_decrypasswod02);

        //找到要显示的 整个短信接收内容的listview
        listview = (ListView) findViewById(R.id.list_receive);

        dataList = new ArrayList<Map<String, Object>>();

        listview.setOnItemClickListener(this);
    }

    private void RefreshList() {
        //从短信数据库读取信息
        Uri uri = Uri.parse("content://sms/");
        String[] projection = new String[]{"address", "body", "date"};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, "date desc");
        startManagingCursor(cursor);

        //此处为了简化代码提高效率，仅仅显示3条最近短信
        for (int i = 0; i < 3; i++) {
            //从手机短信数据库获取信息
            if(cursor.moveToNext()) {
                //获取到短信的发送人
                String address = cursor.getString(cursor.getColumnIndex("address"));
                //获取到短信实体
                String body = cursor.getString(cursor.getColumnIndex("body"));
                //获取到接收的时间
                long longDate = cursor.getLong(cursor.getColumnIndex("date"));
                //将获取到的时间转换为我们想要的方式
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date d = new Date(longDate);
                String time = dateFormat.format(d);

                //准备listView要显示的数据
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("address", address);
                map.put("body", body+"body");
                map.put("time", time+" time");
                //把map加入到集合中
                dataList.add(map);
            }
        }

        //第三个参数from 是我们map的键数组   第四个参数是to 是要显示到哪个控件上的id数组
        simple_adapter = new SimpleAdapter(this, dataList, R.layout.activity_receive_list_item,
                new String[]{"address", "body", "time"}, new int[]{R.id.tv_address, R.id.tv_body, R.id.tv_time});

        //给listview设置数据适配器
        listview.setAdapter(simple_adapter);
    }

    //当点击 listview条目的时候
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        //获取listview中此个item中的内容
        //content的内容格式如下 ：{body=[B@43c2da70body, address=+8615671562394address, time=2015-12-24 11:55:50time}
        System.out.println(i);
        if (i == 0){
            //用于展示解密信息的条目
            Toast.makeText(getApplicationContext(),"点击了进行解密短信的条目1",Toast.LENGTH_SHORT).show();

            String content = listview.getItemAtPosition(i) + "";
            //substring(int beginIndex, int endIndex);截取一段子字符串，包含头不包含尾  indexOf(String str);返回指定字符串在此字符串中第一次出现的索引
            String body = content.substring(content.indexOf("body=") + 5, content.indexOf("body,"));
            String address = content.substring(content.indexOf("address=") + 8, content.lastIndexOf(","));
            String time = content.substring(content.indexOf("time=") + 5, content.indexOf(" time}"));


            //使用bundle存储数据发送给下一个ReceiveActivity_show
            Intent intent=new Intent(ReceiveActivity.this,ReceiveActivity_show.class);
            Bundle bundle = new Bundle();
            bundle.putString("body", body);
            bundle.putString("address", address);
            bundle.putString("time", time);
            bundle.putString("decryptPassword",et_decry02.getText().toString());
            //bundle.putString("encryptPassword",encryptPassword);
            intent.putExtras(bundle);
            startActivity(intent);
        }else if (i == 1){
            //这里要写入 接收短信中 关于AES已经被加密的内容
            String content = listview.getItemAtPosition(i) + "";
            //substring(int beginIndex, int endIndex);截取一段子字符串，包含头不包含尾  indexOf(String str);返回指定字符串在此字符串中第一次出现的索引
            String body = content.substring(content.indexOf("body=") + 5, content.indexOf("body,"));
            Toast.makeText(getApplicationContext(),"点击了进行解密密钥的条目2+++"+body.toString(),Toast.LENGTH_LONG).show();
            //把这后一小段密钥设置到相应的控件上
            et_decry01.setText(body);

        }else if (i == 2){
            //这里要写入 接收短信中 关于AES已经被加密的内容
            String content = listview.getItemAtPosition(i) + "";
            //substring(int beginIndex, int endIndex);截取一段子字符串，包含头不包含尾  indexOf(String str);返回指定字符串在此字符串中第一次出现的索引
            String body = content.substring(content.indexOf("body=") + 5, content.indexOf("body,"));
            Toast.makeText(getApplicationContext(),"点击了进行解密密钥的条目3+++"+body.toString(),Toast.LENGTH_SHORT).show();
            //把这前一小段密钥设置到相应的控件上
            et_decry00.setText(body);
        }else{
            Toast.makeText(getApplicationContext(),"点击了其他条目",Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick(View view) {
        String en_psd = et_decry00.getText().toString().trim() + et_decry01.getText().toString().trim();
        //et_decry02.setText(en_psd);


        //从文件中得到私钥
        InputStream inPrivate = null;
        try {
            inPrivate = getResources().getAssets().open("private_key.pem");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO异常");
        }
        PrivateKey privateKey = null;
        try {
            privateKey = RSAUtils.loadPrivateKey(inPrivate);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("出现了异常");
        }

        // 因为RSA加密后的内容经Base64再加密转换了一下，所以先Base64解密回来再给RSA解密
        byte[] decryptByte = RSAUtils.decryptData(Base64Utils.decode(en_psd), privateKey);

        String decryptStr = new String(decryptByte);
        Toast.makeText(getApplicationContext(),decryptStr.toString(),Toast.LENGTH_SHORT).show();

        et_decry02.setText(decryptStr);
    }
}
