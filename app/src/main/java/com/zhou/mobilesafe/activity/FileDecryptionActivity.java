package com.zhou.mobilesafe.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


        import android.annotation.SuppressLint;
        import android.app.Activity;
        import android.content.ContentUris;
        import android.content.Context;
        import android.content.Intent;
        import android.database.Cursor;
        import android.media.MediaPlayer;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Environment;
        import android.provider.DocumentsContract;
        import android.provider.MediaStore;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;

public class FileDecryptionActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "FileDecryptionActivity";
    private TextView btn_path; //点击按钮，跳转到相应的系统文件管理器
    private TextView tv_path;//显示选中的文件 显示绝对路径
    private String path;//获取到的绝对路径
    private EditText etDecrypt; //解密输入密钥
    private MediaPlayer mPlayer;
    private Button btn_play, btn_pause, btn_replay, btn_stop;
    private Button mDecryptionButton;//解密按钮
    private android.widget.VideoView vv_video;
    private boolean isPlaying;


    // 外部存储的根目录下 用于测试
    private File sdCard = Environment.getExternalStorageDirectory();
    //private File oldFile;
    private File oldFile = new File(sdCard, "heida.jpg");

    private FileInputStream fis = null;
    private FileOutputStream fos = null;
    private boolean isSuccess = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_decryption);

        //去寻找路径
        btn_path = (TextView) findViewById(R.id.btn_path);
        tv_path = findViewById(R.id.tv_path);
        btn_path.setOnClickListener(new View.OnClickListener() {
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


        etDecrypt = (EditText) findViewById(R.id.et_decrypt);//解密密钥

        vv_video = (android.widget.VideoView) findViewById(R.id.vv_videoview);

        btn_play = (Button) findViewById(R.id.btn_play);
        btn_pause = (Button) findViewById(R.id.btn_pause);
        btn_replay = (Button) findViewById(R.id.btn_replay);
        btn_stop = (Button) findViewById(R.id.btn_stop);

        btn_play.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_replay.setOnClickListener(this);
        btn_stop.setOnClickListener(this);


        mDecryptionButton = (Button) findViewById(R.id.decryptionButton);
        mDecryptionButton.setOnClickListener(this);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = getPath(uri);
                tv_path.setText(path);
                Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4以后
                //path = getPath(this, uri);
                path = FileUtils.getRealPathFromUriAboveApi19(FileDecryptionActivity.this, uri);

                tv_path.setText(path);
                Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
            } else {// 4.4以下下系统调用方法

                //path = getRealPathFromURI(uri);
                path = FileUtils.getRealPathFromUriBelowAPI19(FileDecryptionActivity.this, uri);

                tv_path.setText(path);
                Toast.makeText(FileDecryptionActivity.this, path + "222222", Toast.LENGTH_SHORT).show();
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

//      oldFile = new File(sdCard, mFilePath.getText().toString());

        if (TextUtils.isEmpty(path)){
            Toast.makeText(getApplicationContext(),"请先选择文件路径",Toast.LENGTH_LONG).show();
            return;
        }

        //注意 从sp中取出来的密码 是经过加密的
        String psd = SpUtil.getString(getApplication(), ConstantValue.File_Encrypt_PSD, "");
        //获得用户输入的解密密钥
        String et_psd = etDecrypt.getText().toString();


        oldFile = new File(path);

        switch (view.getId()) {
            case R.id.btn_play:
                play(0);
                break;
            case R.id.btn_pause:
                pause();
                break;
            case R.id.btn_replay:
                replay();
                break;
            case R.id.btn_stop:
                stop();
                break;

            case R.id.decryptionButton:
                //先判断用户输入密钥是否为空
                if (!TextUtils.isEmpty(et_psd)){
                    //这里进行密钥校验的时候，注意先将用户输入的解密密钥MD加密后校验
                    if (psd.equals(Md5Util.encoder(et_psd))){
                        // 解密保存
                        long destarTime=System.currentTimeMillis();
                        isSuccess = true;
                        byte[] oldByte = new byte[(int) oldFile.length()];
                        try {
                            fis = new FileInputStream(oldFile);
                            fis.read(oldByte);
                            byte[] newByte = AESUtils.decryptFile(etDecrypt.getText().toString(), oldByte);
                            // 解密
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
                        }
                        try {
                            fis.close();
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (isSuccess){
                            Toast.makeText(this, "解密成功", Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(this, "解密失败", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Log.i("TAG", "解密保存成功");
                        //测试解密耗时
                        long deendTime=System.currentTimeMillis();
                        long deTime=deendTime-destarTime;
                        Log.i("TAG", path+"+++"+"解密耗时："+Long.toString(deTime) + "ms");
                        //System.out.println(deTime);
                    }else{
                        Toast.makeText(getApplicationContext(),"解密密钥输入错误",Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"解密密钥不能为空",Toast.LENGTH_LONG).show();
                    return;
                }
                break;

            default:
                break;
        }

    }

    protected void play(int msec) {
        System.out.println("获取视频文件地址");
        //Log.d(TAG, " 获取视频文件地址");
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this, "视频文件路径错误", Toast.LENGTH_LONG).show();
            return;
        }
        System.out.println("指定视频源路径");
        //Log.d(TAG, "指定视频源路径");
        vv_video.setVideoPath(file.getAbsolutePath());
        System.out.println("开始播放");
        //Log.d(TAG, "开始播放");
        vv_video.start();

        // 按照初始位置播放
        vv_video.seekTo(msec);


        // 播放之后设置播放按钮不可用
        btn_play.setEnabled(false);

        vv_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // 在播放完毕被回调
                btn_play.setEnabled(true);
            }
        });

        vv_video.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // 发生错误重新播放
                play(0);
                isPlaying = false;
                return false;
            }
        });
    }

    /**
     * 重新开始播放
     */
    protected void replay() {
        if (vv_video != null && vv_video.isPlaying()) {
            vv_video.seekTo(0);
            Toast.makeText(this, "重新播放", Toast.LENGTH_LONG).show();
            btn_pause.setText("暂停");
            return;
        }
        isPlaying = false;
        play(0);

    }

    /**
     * 暂停或继续
     */
    protected void pause() {
        if (btn_pause.getText().toString().trim().equals("继续")) {
            btn_pause.setText("暂停");
            vv_video.start();
            Toast.makeText(this, "继续播放", Toast.LENGTH_LONG).show();
            return;
        }
        if (vv_video != null && vv_video.isPlaying()) {
            vv_video.pause();
            btn_pause.setText("继续");
            Toast.makeText(this, "暂停播放", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 停止播放
     */
    protected void stop() {
        if (vv_video != null && vv_video.isPlaying()) {
            vv_video.stopPlayback();
            btn_play.setEnabled(true);
            isPlaying = false;
        }
    }

    public void cancel(View view) {
        finish();
    }
}
