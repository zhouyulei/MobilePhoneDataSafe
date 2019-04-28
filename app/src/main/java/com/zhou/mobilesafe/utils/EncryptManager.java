package com.zhou.mobilesafe.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;

/**
 * Created by zhouyulei on 2019/3/3.
 */

public class EncryptManager {
    private static final String TAG = AES.class.getSimpleName();

    /**
     * 加密
     * @param rules 加密规则
     * @param plainText 加密前的明文
     * @return 加密后的密文 16进制字符串
     */
    public static String encrypt(String rules,String plainText){
        if (TextUtils.isEmpty(plainText) || TextUtils.isEmpty(rules)){
            return null;
        }
        byte[] encryptResult = AES.encrypt(rules,plainText);
        if (encryptResult != null){
            return AES.parseByte2HexStr(encryptResult);
        }
        return null;
    }

    /**
     * 解密
     * @param rules
     * @param encryptText 加密后的密文 16进制字符串
     * @return 加密前的明文
     */
    public static String decrypt(String rules,String encryptText){
        if (TextUtils.isEmpty(encryptText) || TextUtils.isEmpty(rules)){
            return null;
        }
        byte[] decryptResult = AES.decrypt(rules,AES.parseHexStr2Byte(encryptText));
        if (decryptResult != null){
            try {
                return new String(decryptResult,"utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
