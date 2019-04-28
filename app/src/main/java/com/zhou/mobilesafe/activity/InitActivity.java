package com.zhou.mobilesafe.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.utils.ConstantValue;
import com.zhou.mobilesafe.utils.SpUtil;
import com.zhou.mobilesafe.utils.StreamUtil;
import com.zhou.mobilesafe.utils.ToastUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class InitActivity extends AppCompatActivity {

    private TextView tv_version_name;
    private int mLocalVersionCode;

    //更新新版本的状态码
    private static final int UPADATE_VERSION = 100;
    //进入主界面的状态码
    private static final int ENTER_HOME = 101;
    //出现异常的状态码
    private static final int URL_ERROR = 102;
    private static final int IO_ERROR = 103;
    private static final int JSON_ERROR = 104;

    private String mVersionDes;
    private String downloadUrl;
    private RelativeLayout rl_root;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPADATE_VERSION:
                    //弹出对话框，提示用户更新
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    //进入应用程序主界面 跳转activity的过程
                    enterHomeLogin();
                    break;
                case URL_ERROR:
                    ToastUtil.show(getApplication(),"url异常");
                    //虽然出现异常出错了，但是不能影响用户正常使用
                    enterHomeLogin();
                    break;
                case IO_ERROR:
                    ToastUtil.show(getApplication(),"读取异常");
                    //虽然出现异常出错了，但是不能影响用户正常使用
                    enterHomeLogin();
                    break;
                case JSON_ERROR:
                    ToastUtil.show(getApplication(),"json解析异常");
                    //虽然出现异常出错了，但是不能影响用户正常使用
                    enterHomeLogin();
                    break;
            }
        }
    };


    //程序先从onCreate方法开始看起
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        //初始化UI
        initUI();

        //初始化数据
        initDate();

        //设置动画渐变过程
        initAnimation();

        //初始化数据库
        initKey();
    }


    /**
     * 添加淡入动画效果
     */
    private void initAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(2000);
        rl_root.startAnimation(alphaAnimation);
    }

    /**
     * 初始化UI的方法
     */
    private void initUI() {
        tv_version_name = findViewById(R.id.tv_version_name);
        rl_root = findViewById(R.id.rl_root);
    }


    /**
     * 初始化数据的方法
     */
    private void initDate() {
        //1,应用版本名称
        String versionName = getVersionName();
        tv_version_name.setText("版本名称" + versionName);

        //检测(本地版本号和服务器版本号比对)是否有更新,如果有更新,提示用户下载(member)
        //2,获取本地版本号
        mLocalVersionCode = getVersionCode();

        //3.获取服务器的版本号（客户端发请求，服务端给响应，（json,xml））
        //http://www.oxxx.com/update.json  返回200表示请求成功，以流的方式把数据读取下来
        /*
        * json中的内容包含：
        * 1.更新版本的版本名称
        * 2.新版本的描述信息
        * 3.服务器端版本号
        * 4.新版本apk下载地址
        * */

        /*
         * 在splash界面，是否要去检测版本更新，
         * 需要检测sp中存入的在设置界面里面的自动更新设置，如果返回为true,那么就去做更新的逻辑
         * 如果读取不到，默认返回 false,不去做更新的逻辑
         */
        if (SpUtil.getBoolean(getApplication(), ConstantValue.OPEN_UPDATE,false)){
            checkVersion();//校验版本号
        }else{
            //注意，在UI线程，不能直接开子线程做睡眠的操作，必须用到消息机制的api
            //方法1：延时 响应操作
            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    enterHomeLogin();
                }
            },4000);*/
            //方法2：延时 响应操作，在发送消息4秒后去处理,ENTER_HOME状态码指向的消息
            mHandler.sendEmptyMessageDelayed(ENTER_HOME, 4000);
        }
    }

    /**
     * 获取本地的版本名称
     * @return  应用版本名称 返回null代表 异常
     */
    public String getVersionName() {
        //1.获取包管理者
        PackageManager pm = getPackageManager();
        try {
            //2,从包的管理者对象中,获取指定包名的基本信息(版本名称,版本号),传0代表获取基本信息
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            //返回版本名称

            String versionName = info.versionName;
            return versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //如果出现异常返回空
        return null;
    }

    /**
     * 返回本地的版本号
     * @return  非0即表示成功
     */
    public int getVersionCode() {
        //1,包管理者对象packageManager
        PackageManager pm = getPackageManager();
        try {
            //2,从包的管理者对象中,获取指定包名的基本信息(版本名称,版本号),传0代表获取基本信息
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            //3,获取版本名称
            int versionCode = info.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 检测版本号
     */
    private void checkVersion() {
        new Thread(){
            @Override
            public void run() {
                //发送请求获取数据,参数则为请求json的链接地址
                //http://10.0.2.2:8080/update74.json ,这个ip地址仅限于模拟器访问电脑tomcat
                Message msg = Message.obtain();
                long startTime = System.currentTimeMillis();
                try {
                    //1.封装url地址
                    URL url = new URL("http://138.138.0.45:8080/update.json");
                    //2.开启一个链接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    //3.设置常见请求参数
                    conn.setConnectTimeout(2000);//设置能不能连接上，设置请求的超时时间
                    conn.setReadTimeout(2000);//在已经连接成功的条件下，设置读取的超时时间
                    conn.setRequestMethod("GET");//设置请求方法
                    //4.获取请求成功的状态码
                    if (conn.getResponseCode() == 200){
                        //5.以流的形式，将数据获取下来
                        InputStream is = conn.getInputStream();
                        //6.将流转换成字符串（工具类封装）
                        String json = StreamUtil.toString(is);
                        //打印到控制台 看是否已经转换成功
                        Log.i("TAG",json);
                        //7.json解析
                        JSONObject jsonObject = new JSONObject(json);
                        String versionName = jsonObject.getString("versionName");
                        mVersionDes = jsonObject.getString("versionDes");
                        String versionCode = jsonObject.getString("versionCode");
                        downloadUrl = jsonObject.getString("downloadUrl");
                        //打印Log
                        Log.i("TAG",versionName);
                        Log.i("TAG", mVersionDes);
                        Log.i("TAG",versionCode);
                        Log.i("TAG", downloadUrl);

                        //8.比对版本号（服务器版本号 > 本地版本号，提示用户更新）
                        if (mLocalVersionCode < Integer.parseInt(versionCode)){
                            //弹出对话框（UI）,需要用到消息机制
                            msg.what = UPADATE_VERSION;
                        }else{
                            //直接进入应用程序主界面
                            msg.what = ENTER_HOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    msg.what = URL_ERROR;
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what = IO_ERROR;
                } catch (JSONException e) {
                    e.printStackTrace();
                    msg.what = JSON_ERROR;
                }finally {
                    //在finally代码块里面 无论怎么样 都会将消息 发送出去
                    //在 执行完 网络请求之后 如果还不够4s钟，那么就强制让他在此处睡眠 剩下的时间 主要是为了在splash界面以停留
                    long endTime = System.currentTimeMillis();
                    if ((endTime - startTime) < 4000){
                        try {
                            Thread.sleep(4000-(endTime - startTime));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(msg);
                }
            }
        }.start();

        //开子线程的另一种写法
        /*new Thread(new Runnable() {
			@Override
			public void run() {

			}
		});*/
    }

    /**
     * 弹出对话框,提示用户更新
     */
    private void showUpdateDialog() {
        //对话框 依赖与activity而存在，必须写this
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //设置左上角图标
        builder.setIcon(R.drawable.ic_launcher);
        //设置标题
        builder.setTitle("版本更新");
        //设置描述内容
        builder.setMessage(mVersionDes);

        //积极按钮，点击立即更新
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //下载apk,apk链接地址，downloadUrl
                downloadApk();
            }
        });

        //消极按钮，点击表示取消更新，那么就直接进去程序主界面
        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enterHomeLogin();
            }
        });

        //如果用户在弹出的对话框时 没有点击任何按钮，如果直接点击了 返回键 那么要确保程序继续进入主界面
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enterHomeLogin();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    /**
     * 进入程序主界面的方法
     */
    private void enterHomeLogin() {
        Intent intent = new Intent(getApplicationContext(),HomeLoginActivity.class);
        startActivity(intent);

        //在开启一个新的界面后,将导航界面关闭(导航界面只可见一次)
        finish();
    }

    /**
     * 下载 apk
     */
    private void downloadApk() {
        //apk下载链接地址
        //1.判断sd卡是否可用，是否挂载上
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            //2.获取sd卡路径
            String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + File.separator + "update.apk";
            //3.发送请求，获取apk,并且放置到指定路径
            HttpUtils httpUtils = new HttpUtils();
            httpUtils.download(downloadUrl, path, new RequestCallBack<File>() {
                @Override
                public void onStart() {
                    Log.i("TAG","刚刚开始下载");
                    super.onStart();
                }

                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    Log.i("TAG","下载成功");
                    File file = responseInfo.result;
                    //提示用户去安装
                    installApk(file);
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    Log.i("TAG","下载失败");
                }

                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    //下载过程中的方法(下载apk总大小,当前的下载位置,是否正在下载)
                    Log.i("TAG", "下载中........");
                    Log.i("TAG", "total = "+total);
                    Log.i("TAG", "current = "+current);
                    super.onLoading(total, current, isUploading);
                }
            });
        }
    }

    /**
     * 安装对应的apk
     * @param file  安装文件
     */
    protected void installApk(File file) {
        //系统应用界面 ，这里使用隐式意图，去系统源码里面找到对应
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        //文件作为数据源
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        //如果用户 在弹出的安装 界面时，不点击 安装了，如果点击了取消，那该怎么办呢？
        startActivityForResult(intent,0);
    }

    /**
     * 开启一个activity后，返回结果 调用的方法
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        enterHomeLogin();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 初始化数据库
     */
    private void initKey() {
        //1.私钥
        initRsaKey("private_key.pem");
        //2.公钥
        initRsaKey("rsa_public_key.pem");
    }

    /**
     * 拷贝Key文件 到files文件夹下
     * @param dbName 数据库名称
     */
    private void initRsaKey(String dbName) {
        File filesDir = getFilesDir();
        File file = new File(filesDir, dbName);
        if (file.exists()){
            return;
        }
        InputStream in = null;
        FileOutputStream fos = null;
        try {
            in = getAssets().open(dbName);
            fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int len = -1;
            while((len = in.read(bytes)) != -1){
                fos.write(bytes,0,len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (in != null && fos != null){
                try {
                    in.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
