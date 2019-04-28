package com.zhou.mobilesafe.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zhou.mobilesafe.db.BlackNumberOpenHelper;
import com.zhou.mobilesafe.db.domain.BlackNumberInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zhou on 2018/7/30.
 */

public class BlackNumberDao {
    private BlackNumberOpenHelper blackNumberOpenHelper;
    private int count;

    //BlackNumberDao单例模式
    //1,私有化构造方法
    private BlackNumberDao(Context ctx){
        //创建数据库以及其表结构
        blackNumberOpenHelper = new BlackNumberOpenHelper(ctx);
    }
    //2,声明一个当前类的对象
    private static BlackNumberDao blackNumberDao = null;
    //3,提供一个静态方法,如果当前类的对象为空,创建一个新的
    public static BlackNumberDao getInstance(Context ctx){
        if(blackNumberDao == null){
            blackNumberDao = new BlackNumberDao(ctx);
        }
        return blackNumberDao;
    }

    /**增加一个条目
     * @param phone	拦截的电话号码
     * @param mode	拦截类型(1:短信	2:电话	3:拦截所有(短信+电话))
     */
    public void insert(String phone,String mode){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("phone",phone);
        values.put("mode",mode);
        db.insert("blacknumber",null,values);
        db.close();
    }
    /**从数据库中删除一条电话号码
     * @param phone	删除电话号码
     */
    public void delete(String phone){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        db.delete("blacknumber","phone = ?",new String[]{phone});
        db.close();
    }
    /**
     * 根据电话号码去,更新拦截模式
     * @param phone	更新拦截模式的电话号码
     * @param mode	要更新为的模式(1:短信	2:电话	3:拦截所有(短信+电话)
     */
    public void update(String phone,String mode){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("mode",mode);
        db.update("blacknumber",values,"phone = ?",new String[]{phone});
        db.close();
    }
    /**
     * @return	查询到数据库中所有的号码以及拦截类型所在的集合
     */
    public List<BlackNumberInfo> findAll(){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        Cursor cursor = db.query("blacknumber", new String[]{"phone", "mode"}, null, null, null, null, "_id desc");

        List<BlackNumberInfo> blackNumberList = new ArrayList<>();
        while (cursor.moveToNext()){
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.setPhone(cursor.getString(0));
            blackNumberInfo.setMode(cursor.getString(1));
            blackNumberList.add(blackNumberInfo);
        }
        cursor.close();
        db.close();
        return blackNumberList;
    }
    /**
     * 每次查询20条数据
     * @param index	查询的索引值
     */
    public List<BlackNumberInfo> find(int index){
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();

        String sql = "select phone,mode from blacknumber order by _id desc limit ?,10";
        Cursor cursor = db.rawQuery(sql, new String[]{index + ""});
        List<BlackNumberInfo> blackNumberList = new ArrayList<>();
        while (cursor.moveToNext()){
            BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
            blackNumberInfo.setPhone(cursor.getString(0));
            blackNumberInfo.setMode(cursor.getString(1));
            blackNumberList.add(blackNumberInfo);
        }
        cursor.close();
        db.close();
        return blackNumberList;
    }

    /**
     * @return	数据库中数据的总条目个数,返回0代表没有数据或异常
     */
    public int getCount(){
        int count = 0;
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select count(*) from blacknumber;", null);
        while(cursor.moveToNext()){
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    /**
     * @return	根据传入的电话号码 返回对应的模式码
     */
    public int getMode(String phone){
        int mode = 0;
        SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
        //Cursor cursor = db.query("blacknumber",new String[]{"mode"},"phone = ?",new String[]{phone},null,null,null);
        Cursor cursor = db.query("blacknumber", new String[]{"mode"}, "phone = ?", new String[]{phone}, null, null,null);
        while(cursor.moveToNext()){
            mode = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return mode;
    }

}
