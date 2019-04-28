package com.zhou.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Zhou on 2018/7/18.
 */

public class StreamUtil {
    /**
     *
     * @param in 流对象
     * @return   流转换成字符串 返回null表示异常
     */
    public static String toString(InputStream in){
        //1.在读取的过程中，将读取的内容存储在缓存中，然后一次性的将字符串返回
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //读流操作，一直读到没有为止（循环）
        int len  = -1;
        byte[] bytes = new byte[1024];
        try {
            while((len = in.read(bytes)) != -1){
                baos.write(bytes,0,len);
            }
            in.close();
            String content = new String(baos.toByteArray());
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
