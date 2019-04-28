package com.zhou.mobilesafe.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import com.zhou.mobilesafe.activity.EnterPsdActivity;
import com.zhou.mobilesafe.db.dao.AppLockDao;

import java.util.List;

public class WatchDogService extends Service {

    private AppLockDao mDao;
    private boolean isWatch;
    private List<String> mPacknameList;
    private String mSkipPackagename;
    private InnerReceiver mInnerReceiver;
    private MyContentObserver mContentObserver;

    public WatchDogService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        //维护一个看门狗的死循环,让其时刻监测现在开启的应用,是否为程序锁中要去拦截的应用
        mDao = AppLockDao.getInstance(this);
        isWatch = true;
        watch();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SKIP");

        mInnerReceiver = new InnerReceiver();
        registerReceiver(mInnerReceiver,intentFilter);

        //注册一个内容观察者,观察数据库的变化,一旦数据有删除或者添加,则需要让mPacknameList重新获取一次数据
        mContentObserver = new MyContentObserver(new Handler());
        getContentResolver().registerContentObserver(Uri.parse("content://applock/change"),true, mContentObserver);

        super.onCreate();
    }

    class  MyContentObserver extends ContentObserver{
        public MyContentObserver(Handler handler) {
            super(handler);
        }
        //一旦数据库发生改变时候调用方法,重新获取包名所在集合的数据
        @Override
        public void onChange(boolean selfChange) {
            new Thread(){
                @Override
                public void run() {
                    mPacknameList = mDao.findAll();
                }
            }.start();
            super.onChange(selfChange);
        }
    }

    class InnerReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //获取发送广播过程中传递过来的包名,跳过次包名检测过程
            mSkipPackagename = intent.getStringExtra("packagename");
        }
    }

    private void watch() {
        new Thread(){
            //1,子线程中,开启一个可控死循环
            @Override
            public void run() {
                mPacknameList = mDao.findAll();
                while (isWatch){
                    //2.监测现在正在开启的应用,任务栈
                    //3.获取activity管理者对象
                    ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    //4.获取正在开启应用的任务栈
                    List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
                    ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
                    //5.获取栈顶的activity,然后在获取此activity所在应用的包名
                    String packagename = runningTaskInfo.topActivity.getPackageName();

                    //如果任务栈指向应用有切换,将mSkipPackagename空字符串

                    //6.拿此包名在已加锁的包名集合中去做比对,如果包含次包名,则需要弹出拦截界面
                    if (mPacknameList.contains(packagename)){
                        //如果现在检测的程序,以及解锁了,则不需要去弹出拦截界面
                        if (!packagename.equals(mSkipPackagename)){
                            //弹出拦截界面
                            Intent intent = new Intent(getApplicationContext(), EnterPsdActivity.class);
                            //在服务里面 添加activity需要 添加开启任务栈
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            //发送要加锁的包名
                            intent.putExtra("packagename",packagename);
                            startActivity(intent);
                        }
                    }

                    //在子线程里面 让其睡眠一下 不能一直开启看门狗循环 消耗内存太大
                    try {
                        Thread.sleep(100);  //300
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        //停止看门狗循环
        isWatch = false;
        //注销广播接受者
        if(mInnerReceiver!=null){
            unregisterReceiver(mInnerReceiver);
        }
        //注销内容观察者
        if(mContentObserver!=null){
            getContentResolver().unregisterContentObserver(mContentObserver);
        }
        super.onDestroy();
    }
}
