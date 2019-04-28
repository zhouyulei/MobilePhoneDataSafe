package com.zhou.mobilesafe.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.zhou.mobilesafe.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {

    private ListView lv_contact;
    private List<HashMap<String,String>> contactList = new ArrayList<HashMap<String,String>>();
    private MyAdapter mAdapter;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            mAdapter = new MyAdapter();
            lv_contact.setAdapter(mAdapter);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        
        initUI();

        //初始化数据
        initdata();

    }

    private void initUI() {
        lv_contact = findViewById(R.id.lv_contact);
        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //1,获取点中条目的索引指向集合中的对象
                if (mAdapter != null){
                    HashMap<String,String> hashMap = (HashMap<String, String>) mAdapter.getItem(position);
                    //2,获取当前条目指向集合对应的电话号码
                    String phone = hashMap.get("phone");
                    //3,此电话号码需要给第三个导航界面使用

                    //4,在结束此界面回到前一个导航界面的时候,需要将数据返回过去
                    Intent intent = new Intent();
                    intent.putExtra("phone",phone);
                    setResult(0,intent);
                    finish();
                }
            }
        });
    }

    /**
     * 获取系统联系人数据方法，注意可能会读取很多的联系人信息，有耗时操作，所以需要开子线程
     */
    private void initdata() {
        new Thread(){
            @Override
            public void run() {

                //开始使用list之前 先把它清空
                contactList.clear();

                //1.获取内容解析器对象
                ContentResolver contentResolver = getContentResolver();
                //2.做查询系统联系人数据库表过程(读取联系人权限)
                Cursor cursor = contentResolver.query(Uri.parse("content://com.android.contacts/raw_contacts"),
                        new String[]{"contact_id"}, null, null, null);
                //3.循环游标 直到没有数据为止
                while (cursor.moveToNext()){
                    String id = cursor.getString(0);
                    Log.i("TAG", "id = "+id);
                    //4,根据用户唯一性id值,查询data表和mimetype表生成的视图,获取data以及mimetype字段
                    Cursor indexCursor = contentResolver.query(Uri.parse("content://com.android.contacts/data"),
                            new String[]{"data1", "mimetype"},
                            "raw_contact_id = ?", new String[]{id}, null);
                    //5,循环获取每一个联系人的电话号码以及姓名,数据类型  把姓名和号码 暂存到 hashmap里面
                    HashMap<String,String> hashMap = new HashMap<String, String>();
                    while(indexCursor.moveToNext()){
                        String data = indexCursor.getString(0);
                        String type = indexCursor.getString(1);

                        //6,区分类型去给hashMap填充数据
                        if (type.equals("vnd.android.cursor.item/phone_v2")){
                            if (!TextUtils.isEmpty(data)){
                                hashMap.put("phone",data);
                            }
                        }else if (type.equals("vnd.android.cursor.item/name")){
                            if (!TextUtils.isEmpty(data)){
                                hashMap.put("name",data);
                            }
                        }
                    }
                    indexCursor.close();
                    //把hashmap里面的东西 存到集合中
                    contactList.add(hashMap);
                }
                cursor.close();
                //7,消息机制,发送一个空的消息,告知主线程可以去使用子线程已经填充好的数据集合
                mHandler.sendEmptyMessage(0);
            }
        }.start();
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return contactList.size();
        }

        @Override
        public HashMap<String,String> getItem(int position) {
            return contactList.get(position);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;
            if (convertView == null){
                view = View.inflate(getApplicationContext(), R.layout.listview_contact_item, null);
            }else{
                view = convertView;
            }
            TextView tv_name = view.findViewById(R.id.tv_name);
            TextView tv_phone = view.findViewById(R.id.tv_phone);

            tv_name.setText(getItem(position).get("name"));
            tv_phone.setText(getItem(position).get("phone"));

   /*         tv_name.setText(contactList.get(position).get("name"));
            tv_phone.setText(contactList.get(position).get("phone"));*/
            return view;
        }
    }
}











