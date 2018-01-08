package com.panhongyuan.mobilesafe.engine;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Xml;

import com.panhongyuan.mobilesafe.utils.ToastUtil;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by pan on 17-4-3.
 */

public class SmsBackUp {
    private static int index = 0;

    //备份短信的方法
    public static void backup(Context context, String path, CallBack callBack) {
        Cursor cursor = null;
        FileOutputStream fos = null;
        try {
            //需要上下文环境，备份的文件夹，进度条所在的对话框对象用于备份过程中的更新
            //1.获取备份短信写入的文件
            File file = new File(path);
            //2.获取内容解析器,获取短信数据库中数据的内容
            cursor = context.getContentResolver().query(Uri.parse("content://sms/"), new String[]{"address", "date", "type", "body"}, null, null, null);
            //3.将文件相应的输出流获取出来
            fos = new FileOutputStream(file);


            //4.序列化数据库读取数据，保存到xml中
            // 加载xml文档
            XmlSerializer xmlSerializer = Xml.newSerializer();
            //5.给ｘｍｌ做相应的设置
            xmlSerializer.setOutput(fos, "utf-8");
            //DTD(xml规范),下面一行代码第二个参数为true，不依赖于任何DTD存在
            xmlSerializer.startDocument("utf-8", true);
            //参数1．命名空间
            xmlSerializer.startTag(null, "smss");

            //6.备份短信的总数作为对话框的最大值
            //a.如果传进来的时对话框，指定对话框的总数
            //b.如果传进来的是进度条，指定进度条的总数
            if (callBack != null) {
                callBack.setMax(cursor.getCount());
            }

            /*for (int i = 1; i < 30; i++) {
                cursor.moveToNext();
                System.out.println("获取第" + i + "条数据为：");
                System.out.println("号码：" + cursor.getString(0));
                System.out.println("时间：" + cursor.getString(1));
                System.out.println("类型：" + cursor.getString(2));
                System.out.println("内容：" + cursor.getString(3));
                Thread.sleep(1000);
                index++;
                callBack.setProgress(index);
            }*/
            //7.读取数据库中的每一条数据，写入到xml中
            while (cursor.moveToNext()) {
                xmlSerializer.startTag(null, "sms");
                //第1个字段
                xmlSerializer.startTag(null, "address");
                xmlSerializer.text(cursor.getString(0));
                xmlSerializer.endTag(null, "address");
                //第2个字段
                xmlSerializer.startTag(null, "date");
                xmlSerializer.text(cursor.getString(1));
                xmlSerializer.endTag(null, "date");
                //第3个字段
                xmlSerializer.startTag(null, "type");
                xmlSerializer.text(cursor.getString(2));
                xmlSerializer.endTag(null, "type");
                //第4个字段
                xmlSerializer.startTag(null, "body");
                xmlSerializer.text(cursor.getString(3));
                xmlSerializer.endTag(null, "body");

                xmlSerializer.endTag(null, "sms");

                //8.每循环一次，就需要使进度条叠加一次
                Thread.sleep(20);
                index++;
                //progressDialog可以子线程更新进度，是一个比较特殊的控件
                if (callBack != null) {
                    callBack.setProgress(index);
                }
            }

            xmlSerializer.endTag(null, "smss");
            xmlSerializer.endDocument();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null && fos != null) {
                    cursor.close();
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 回调编写
     * 1.定义一个接口
     * 2,定义接口中未实现的业务逻辑方法
     * 3.传递一个实现了此接口的类的对象,一定实现了上诉接口未实现方法
     * 4.获取传递进来的对象,在合适的地方,做方法的调用
     */
    public interface CallBack {
        //短信总数设置为实现方法（自己决定使用对话框的设置还是用进度条的设置）
        void setMax(int max);

        //备份过程进度百分比的更新
        void setProgress(int index);
    }
}
