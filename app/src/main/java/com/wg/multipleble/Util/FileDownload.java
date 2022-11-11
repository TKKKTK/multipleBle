package com.wg.multipleble.Util;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.wg.multipleble.Data.DataPacket;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * android:requestLegacyExternalStorage="false" false代表采用分区存储
 * 为true 代表的是采用之前的存储方式
 */
public class FileDownload {
    private List<DataPacket> dataList;
    private String file_name;
    private Context context;
    private List echartsData;
    private String dataContent;
    private Uri insert;

    public FileDownload(List<DataPacket> dataPackets, String file_name, Context context){
        this.dataList = dataPackets;
        this.file_name = file_name;
        this.context = context;
    }

    public FileDownload(String content,Context context){
        this.dataContent = content;
        this.context = context;
    }

    public FileDownload(List<DataPacket> dataPackets, Context context){
        this.dataList = dataPackets;
        this.context = context;
    }

    public FileDownload(String file_name, Context context, List echartsData) {
        this.file_name = file_name;
        this.context = context;
        this.echartsData = echartsData;
    }

    public FileDownload(Context context, List echartsData){
        this.context = context;
        this.echartsData = echartsData;
    }

    public FileDownload(Context context,String file_name){
        this.context = context;
        this.file_name = file_name;
    }

    public void CreateUri(){
        //采取分区存储方式
        //在Android R 下创建文件
        //获取到一个路径
        Uri uri = MediaStore.Files.getContentUri("external");
        //创建一个ContentValue对象，用来给存储文件数据的数据库进行插入操作
        ContentValues contentValues = new ContentValues();
        //首先创建zee.txt要存储的路径 要创建的文件的上一级存储目录
        String path = Environment.DIRECTORY_DOWNLOADS + "/ZEE";
        //给路径的字段设置键值对
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/ZEE");
        //设置文件的名字
        contentValues.put(MediaStore.Downloads.DISPLAY_NAME,file_name);
        contentValues.put(MediaStore.Downloads.TITLE,"wg");

        //插入一条数据，然后把生成的这个文件的路径返回回来
         this.insert = context.getContentResolver().insert(uri,contentValues);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public String save(){
        String content = listToString(); // 存储内容解析
        //判断当前android 版本是否支持分区存储
        if (!Environment.isExternalStorageLegacy()){
            //采取分区存储方式
            //在Android R 下创建文件
            //获取到一个路径
            Uri uri = MediaStore.Files.getContentUri("external");
            //创建一个ContentValue对象，用来给存储文件数据的数据库进行插入操作
            ContentValues contentValues = new ContentValues();
            //首先创建zee.txt要存储的路径 要创建的文件的上一级存储目录
            String path = Environment.DIRECTORY_DOWNLOADS + "/ZEE";
            Log.d(TAG, "createFile: "+path);
            //给路径的字段设置键值对
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/ZEE");
            //设置文件的名字
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME,file_name);
            contentValues.put(MediaStore.Downloads.TITLE,"Zee");

            //插入一条数据，然后把生成的这个文件的路径返回回来
            Uri insert = context.getContentResolver().insert(uri,contentValues);

            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(insert);
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                bos.write(content.getBytes());
                bos.close();
                return path;
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            String filePath = "/sdcard/";
            //传统File方式
            File file = new File(filePath+file_name);
            OutputStream outputStream = null;
            if (!file.exists()){
                try {
                    file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(content.getBytes());
                outputStream.close();
                return filePath;
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
      return null;
    }

    /**
     * 每隔一段时间写入外存数据
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void saveCacheData(String content){
        //判断当前android 版本是否支持分区存储
        if (!Environment.isExternalStorageLegacy()){
            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(insert,"rwa");
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                bos.write(content.getBytes());
                bos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            String filePath = "/sdcard/";
            //传统File方式
            File file = new File(filePath+file_name);
            OutputStream outputStream = null;
            if (!file.exists()){
                try {
                    file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(content.getBytes());
                outputStream.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void saveEchartsData(){
        String content = echartsListString(); // 存储内容解析
        //判断当前android 版本是否支持分区存储
        if (!Environment.isExternalStorageLegacy()){
            //采取分区存储方式
            //在Android R 下创建文件
            //获取到一个路径
            Uri uri = MediaStore.Files.getContentUri("external");
            //创建一个ContentValue对象，用来给存储文件数据的数据库进行插入操作
            ContentValues contentValues = new ContentValues();
            //首先创建zee.txt要存储的路径 要创建的文件的上一级存储目录
            String path = Environment.DIRECTORY_DOWNLOADS + "/ZEE";
            //给路径的字段设置键值对
            contentValues.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/ZEE");
            //设置文件的名字
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME,file_name);
            contentValues.put(MediaStore.Downloads.TITLE,"Zee");

            //插入一条数据，然后把生成的这个文件的路径返回回来
            Uri insert = context.getContentResolver().insert(uri,contentValues);

            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(insert);
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                bos.write(content.getBytes());
                bos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            String filePath = "/sdcard/";
            //传统File方式
            File file = new File(filePath+file_name);
            OutputStream outputStream = null;
            if (!file.exists()){
                try {
                    file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(content.getBytes());
                outputStream.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据uri保存16进制数据
     */

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void save(Uri uri){
        String content = listToString(); // 存储内容解析
        //判断当前android 版本是否支持分区存储
        if (!Environment.isExternalStorageLegacy()){
            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(uri);
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                bos.write(content.getBytes());
                bos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            //String filePath = "/sdcard/";
            //传统File方式
            File file = new File(uri.getPath());
            OutputStream outputStream = null;
            if (!file.exists()){
                try {
                    file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(content.getBytes());
                outputStream.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    /**
     * 根据对应的uri来进行存储
     * @param uri
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void saveToUri(Uri uri){
        String content = this.dataContent; // 存储内容解析
        //判断当前android 版本是否支持分区存储
        if (!Environment.isExternalStorageLegacy()){
            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(uri);
                BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                bos.write(content.getBytes());
                bos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            //String filePath = "/sdcard/";
            //传统File方式
            File file = new File("/sdcard/"+getTimeRecord()+".txt");
            OutputStream outputStream = null;
            if (!file.exists()){
                try {
                    file.createNewFile();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(content.getBytes());
                outputStream.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    private String echartsListString(){
        StringBuilder stringBuilder = new StringBuilder();
         if (echartsData.size()>0){
             for (Object dataPoint : echartsData){
                 stringBuilder.append(dataPoint);
                 stringBuilder.append(" ");
             }
         }
        return stringBuilder.toString();
    }

    private String listToString(){
        StringBuilder stringBuilder = new StringBuilder();
        if (dataList.size()>0){
            for (DataPacket dataPacket : dataList){
                stringBuilder.append(dataPacket.getData().toString().substring(4));
                stringBuilder.append("  ");
            }
        }
        return stringBuilder.toString();
    }

    public String getTimeRecord(){
        return new SimpleDateFormat("HH:mm:ss:SS").format(new Date().getTime());
    }


    /**
     * 选择保存路径
     */


}
