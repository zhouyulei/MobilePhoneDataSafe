package com.zhou.mobilesafe.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhou.mobilesafe.R;
import com.zhou.mobilesafe.utils.CountTimer;

public class HomeActivity extends AppCompatActivity {

    private GridView gv_home;
    private String[] mTitleStrs;
    private int[] mMipmapIds;

    private CountTimer countTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //初始化UI
        initUI();

        //初始化数据
        initData();

        //倒计时操作
        init();

    }
//---------------------------------------------------------------------------------------------------------------------------
    private void init() {
        // TODO:  五分钟无人操作回主页面
        //backMain = new BackMain(1000 * 60 * 5, 1000, this);
        countTimer = new CountTimer(12000 , 1000, HomeActivity.this);
    }

    @Override
    protected void onResume() {

        timeStart();
        super.onResume();

    }
    //region 无操作 返回主页
    private void timeStart() {

        new Handler(getMainLooper()).post(new Runnable() {

            @Override
            public void run() {

                countTimer.start();

            }
        });
    }
    /**
     * 主要的方法，重写dispatchTouchEvent
     *
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            //获取触摸动作，如果ACTION_UP，计时开始。
            case MotionEvent.ACTION_UP:
                countTimer.start();
                break;
            //否则其他动作计时取消
            default:
                countTimer.cancel();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    protected void onPause() {
        super.onPause();
        countTimer.cancel();
    }
//---------------------------------------------------------------------------------------------------------------------------

    /**
     * 初始化UI的方法
     */
    private void initUI() {
        gv_home = findViewById(R.id.gv_home);
    }

    /**
     * 初始化 GridView 要展示的数据 九张图片 对应的各自文字介绍
     */
    private void initData() {
        //准备文字数据
        mTitleStrs = new String[]{"文件保护","短信保护","程序锁","骚扰拦截","软件管理","设置中心"};
        //准备图标数据
        mMipmapIds = new int[]{R.mipmap.home_fileencryption,R.mipmap.home_messageencry,R.mipmap.home_programlock,
                               R.mipmap.home_callmsginterception,R.mipmap.home_apps, R.mipmap.home_setting};
        //给GridView设置数据适配器
        gv_home.setAdapter(new MyAdapter());

        //给每个gridview条目设置点击事件
        gv_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        //跳转到文件保护模块
                        startActivity(new Intent(getApplicationContext(), FileProtectActivity.class));
                        break;
                    case 1:
                        //跳转到短信保护模块
                        startActivity(new Intent(getApplicationContext(), MessageEncryptActivity.class));
                        break;
                    case 2:
                        //跳转到程序锁模块
                        startActivity(new Intent(getApplicationContext(), AppLockLoginActivity.class));
                        break;
                    case 3:
                        //跳转到骚扰拦截页面
                        startActivity(new Intent(getApplicationContext(),BlackNumberActivity.class));
                        break;
                    case 4:
                        //跳转到软件管理页面
                        startActivity(new Intent(getApplicationContext(),AppManagerActivity.class));
                        break;
                    case 5:
                        //跳转到设置中心模块
                        Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    /**
     * GridView需要用到的数据适配器
     */
    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mTitleStrs.length;
        }

        @Override
        public Object getItem(int position) {
            return mTitleStrs[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            //防止内存溢出
            if (convertView == null){
                view = View.inflate(getApplicationContext(),R.layout.gridview_item,null);
            }else{
                view = convertView;
            }

            //找到每一个条目的id
            ImageView iv_icon = view.findViewById(R.id.iv_icon);
            TextView tv_title = view.findViewById(R.id.tv_title);

            //给每一个条目设置数据
            iv_icon.setBackgroundResource(mMipmapIds[position]);
            tv_title.setText(mTitleStrs[position]);

            return view;
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------

//--------------------------------------------------------------------------------------------------------------------------------

    /*@Override
    public void finish() {
        super.finish();

        Intent intent=new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(getApplicationContext(), HomeLoginActivity.class);
        getApplicationContext().startActivity(intent);
    }*/

    /*禁用返回键*/
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK)
            return true;//不执行父类点击事件
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
}
