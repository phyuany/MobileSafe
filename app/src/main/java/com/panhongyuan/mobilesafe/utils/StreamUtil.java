package com.panhongyuan.mobilesafe.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pan on 17-3-8.
 */

public class StreamUtil {
    /**
     * @param is 流对象
     * @return 流转换成字符串，返回null代表异常
     */
    public static String streamToString(InputStream is) {
        //1.在读取过程中，将读取内容存入缓存中，最后一次性转换成字符串
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //2.读流操作，读到没有为止
        byte[] buffer = new byte[1024];
        //3.记录读取内容的临时变量
        int tmp = -1;
        try {
            while ((tmp = is.read(buffer)) != -1) {
                bos.write(buffer);
            }
            //返回读取数据
            return bos.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
