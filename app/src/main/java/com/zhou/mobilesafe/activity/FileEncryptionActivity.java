package com.zhou.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.utils.AESUtils;
import com.zhou.mobilesafe.utils.ConstantValue;
import com.zhou.mobilesafe.utils.FileUtils;
import com.zhou.mobilesafe.utils.Md5Util;
import com.zhou.mobilesafe.utils.SpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileEncryptionActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "FileEncryptionActivity";
    private TextView btn; //点击按钮，跳转到相应的系统文件管理器
    private String path;//获取到的绝对路径
    private EditText etEncrypt; //加密输入密钥
    private Button mEncryptionButton;//加密按钮

    // 外部存储的根目录下 用于测试
    private File sdCard = Environment.getExternalStorageDirectory();
    private File oldFile = new File(sdCard, "heida.jpg");

    private FileInputStream fis = null;
    private FileOutputStream fos = null;
    private boolean isSuccess = true;
    private TextView et_path;//显示选中的文件 显示绝对路径


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_encryption);

        //去寻找路径
        btn= (TextView) findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent, 1);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "没有找到文件管理器", Toast.LENGTH_SHORT).show();
                }
            }
        });
        et_path = findViewById(R.id.et_path);//设置路径
        etEncrypt = (EditText) findViewById(R.id.et_encrypt);//加密密钥

        //找到加密按钮
        mEncryptionButton = (Button) findViewById(R.id.encryptionButton);
        mEncryptionButton.setOnClickListener(this);


    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = getPath(uri);
                et_path.setText(path);
                Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4以后
                //path = getPath(this, uri);
                path = FileUtils.getRealPathFromUriAboveApi19(FileEncryptionActivity.this, uri);

                et_path.setText(path);
                Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
            } else {// 4.4以下下系统调用方法

                //path = getRealPathFromURI(uri);
                path = FileUtils.getRealPathFromUriBelowAPI19(FileEncryptionActivity.this, uri);

                et_path.setText(path);
                Toast.makeText(FileEncryptionActivity.this, path + "222222", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getPath(Uri uri) {

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

//——————————————————————————————————————————————————————————————————————————————————————————————————————————-


    @Override
    public void onClick(View view) {

        if (TextUtils.isEmpty(path)){
            Toast.makeText(getApplicationContext(),"请先选择文件路径",Toast.LENGTH_LONG).show();
            return;
        }
        String psd = etEncrypt.getText().toString();
        //将本次设置的密码存储到sp中
        SpUtil.putString(getApplication(), ConstantValue.File_Encrypt_PSD, Md5Util.encoder(psd));

        oldFile = new File(path);
        switch (view.getId()) {
            case R.id.encryptionButton:
                if (!TextUtils.isEmpty(psd)){

                    // 加密保存
                    long enstarTime=System.currentTimeMillis();
                    isSuccess = true;
                    try {
                        fis = new FileInputStream(oldFile);
                        byte[] oldByte = new byte[(int) oldFile.length()];
                        fis.read(oldByte); // 读取
                        byte[] newByte = AESUtils.encryptFile(etEncrypt.getText().toString(), oldByte);
                        // 加密
                        fos = new FileOutputStream(oldFile);
                        fos.write(newByte);
                    } catch (FileNotFoundException e) {
                        isSuccess = false;
                        e.printStackTrace();
                    } catch (IOException e) {
                        isSuccess = false;
                        e.printStackTrace();
                    } catch (Exception e) {
                        isSuccess = false;
                        e.printStackTrace();
                    } finally {
                        try {
                            fis.close();
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (isSuccess){
                        Toast.makeText(this, "加密成功", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(this, "加密失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Log.i("TAG", "加密保存成功");
                    //测试加密耗时
                    long enendTime=System.currentTimeMillis();
                    long Time=enendTime-enstarTime;
                    Log.i("TAG", path+"+++"+"加密耗时："+Long.toString(Time) + "ms");
                }else{
                    Toast.makeText(getApplicationContext(),"加密密钥不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                break;
            default:
                break;
        }
    }

    //如果点击取消按钮，返回上一个Activity
    public void cancel(View view) {
        finish();
    }
}
