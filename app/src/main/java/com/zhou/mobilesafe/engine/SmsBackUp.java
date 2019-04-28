package com.zhou.mobilesafe.engine;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Xml;
import android.widget.ProgressBar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Zhou on 2018/8/1.
 */

public class SmsBackUp {


    private static Cursor cursor;

    //需要用到的对象上下文环境,备份文件夹路径,进度条所在的对话框对象用于备份过程中进度的更新
    public static void backup(Context ctx, String path, CallBack  callBack){
        FileOutputStream fos = null;
        int index = 0;
        try {
            //1,获取备份短信写入的文件
            File file = new File(path);
            //2,获取内容解析器,获取短信数据库中数据
            cursor = ctx.getContentResolver().query(Uri.parse("content://sms/"), new String[]{"address", "date", "type", "body"}, null, null, null);
            //3,文件相应的输出流
            fos = new FileOutputStream(file);
            //4,序列化数据库中读取的数据,放置到xml中
            XmlSerializer xmlSerializer = Xml.newSerializer();
            //5,给次xml做相应设置
            xmlSerializer.setOutput(fos,"utf-8");
            //6,备份短信总数指定
            xmlSerializer.startDocument("utf-8",true);
            xmlSerializer.startTag(null,"smss");


            //A 如果传递进来的是对话框,指定对话框进度条的总数
            //B	如果传递进来的是进度条,指定进度条的总数
            //防止非空
            if (callBack != null){
                callBack.setMax(cursor.getCount());
            }


            while (cursor.moveToNext()){
                xmlSerializer.startTag(null,"sms");

                String address = cursor.getString(0);
                String date = cursor.getString(1);
                String type = cursor.getString(2);
                String body = cursor.getString(3);


                xmlSerializer.startTag(null,"address");
                xmlSerializer.text(address);
                xmlSerializer.endTag(null,"address");

                xmlSerializer.startTag(null,"date");
                xmlSerializer.text(date);
                xmlSerializer.endTag(null,"date");

                xmlSerializer.startTag(null,"type");
                xmlSerializer.text(type);
                xmlSerializer.endTag(null,"type");

                xmlSerializer.startTag(null,"body");
                xmlSerializer.text(body);
                xmlSerializer.endTag(null,"body");


                xmlSerializer.endTag(null,"sms");


                index++;
                //为了让用户能够看到当前的备份进度 不至于一闪而过   当前也是在子线程去更新UI，不会造成主线程的阻塞问题
                Thread.sleep(500);

                //A 如果传递进来的是对话框,指定对话框进度条的当前百分比
                //B	如果传递进来的是进度条,指定进度条的当前百分比
                if (callBack != null){
                    callBack.setProgress(index);
                }
            }

            xmlSerializer.endTag(null,"smss");
            xmlSerializer.endDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (cursor != null && fos != null){
                    cursor.close();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //回调
    //1.定义一个接口
    //2,定义接口中未实现的业务逻辑方法(短信总数设置,备份过程中短信百分比更新)
    //3.传递一个实现了此接口的类的对象(至备份短信的工具类中),接口的实现类,一定实现了上诉两个为实现方法(就决定了使用对话框,还是进度条)
    //4.获取传递进来的对象,在合适的地方(设置总数,设置百分比的地方)做方法的调用
    public interface CallBack{
        //短信总数设置为实现方法(由自己决定是用	对话框.setMax(max) 还是用	进度条.setMax(max))
        public void setMax(int max);
        //备份过程中短信百分比更新(由自己决定是用	对话框.setProgress(max) 还是用	进度条.setProgress(max))
        public void setProgress(int index);
    }
}
