package com.zhou.mobilesafe.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Zhou on 2018/7/22.
 */

public class Md5Util {
    public static String encoder(String psd){

        try {
            //加盐处理
            psd = psd + "mobliesafe";
            //2.指定加密算法类型
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //2,将需要加密的字符串中转换成byte类型的数组,然后进行随机哈希过程
            byte[] bytes = digest.digest(psd.getBytes());
            //3,循环遍历bs,然后让其生成32位字符串,固定写法
            //4,拼接字符串过程
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : bytes){
                int i = b & 0xff;
                //int类型的i需要转换成16进制字符
                String hexString = Integer.toHexString(i);
                if(hexString.length()<2){
                    hexString = "0"+hexString;
                }
                stringBuffer.append(hexString);
            }
            //5.返回字符串
            return stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
