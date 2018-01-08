package com.panhongyuan.mobilesafe.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pan on 17-3-11.
 */
public class Md5Util {

    private static StringBuffer stringBuffer;

    public static String encoder(String psd) {
        try {
            psd = psd + "panhongyuan.com";
            //1.指定加密算法
            MessageDigest digest = MessageDigest.getInstance("MD5");
            //2.将需要转化的字符串转化为byte数组，然后进行hash算法，得到hash过后的bytes
            byte[] bytes = digest.digest(psd.getBytes());
            //3.循环遍历数组，让其生成32位的字符串，这是固定写法
            stringBuffer = new StringBuffer();
            for (byte b : bytes) {
                int i = b & 0xff;
                //int类型的i需要转化成16进制的字符
                String hexString = Integer.toHexString(i);
                if (hexString.length() < 2) {
                    hexString = "0" + hexString;
                }
                stringBuffer.append(hexString);
                System.out.println("MD5加密过后的字符：" + hexString);
            }
            System.out.print(stringBuffer.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return stringBuffer.toString();
    }
}
