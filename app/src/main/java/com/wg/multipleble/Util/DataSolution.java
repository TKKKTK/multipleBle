package com.wg.multipleble.Util;

import com.wg.multipleble.Data.EchartsData;
import com.wg.multipleble.Data.UiEchartsData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import no.nordicsemi.android.ble.data.Data;

/**
 * 肌电数据解析
 */
public class DataSolution {
    private static DataSolution _instance;

    private DataSolution(){

    }

    public static DataSolution getInstance(){
        if (_instance == null){
            _instance = new DataSolution();
        }
        return _instance;
    }

    public List<UiEchartsData> solution(Data data){
         String hexString = data.toString();
         String[] arr = hexString.split(" ");
         String[] arr1 = arr[1].split("-");

         //记录原始数据值
        int[] dataArr = new int[arr1.length];
        //存放截取的数据
        int[] subData = new int[120];
        //存放反转后的数据
        int[][] echartsData = new int[40][3];
        //存放3字节转4字节的数据
        int[] _3byteTo4byte = new int[40];
        //存放最后一位包序号
        int serialNum = 0;

        for (int i = 0;i < arr1.length;i ++){
             dataArr[i] = Integer.valueOf(arr1[i],16); //16字节字符串转
        }
        serialNum = dataArr[dataArr.length-1]; // 拿到包序号
        System.arraycopy(dataArr,2,subData,0,120);
        for (int i = 0; i < echartsData.length;i++){
            System.arraycopy(subData,i*3,echartsData[i],0,3);
        }
        //高低位反转
        for (int i = 0; i<echartsData.length;i++){
            Stack<Integer> stack = new Stack<Integer>();
            for (int j = 0;j<echartsData[i].length;j++){
                stack.push(echartsData[i][j]);
            }
            for (int z = 0;z<echartsData[i].length;z++){
                echartsData[i][z]=stack.pop();
            }
        }
        //三字节转四字节整型
        for (int i = 0;i < echartsData.length;i++){
            _3byteTo4byte[i] = byteToInt(echartsData[i]);
        }

        //一个通道五个点，一共有八个通道
        List<UiEchartsData> uiEchartsDataList = new ArrayList<>();
        for (int i = 0;i < 8; i++){
            UiEchartsData uiEchartsData = new UiEchartsData(); //存放一个通道中的五个点
            List<EchartsData> echartsDataList = new ArrayList<>();//新建一个存放五个点的数据
            for (int j = 0;j<5;j++){
                EchartsData chartData = new EchartsData();
                chartData.setDataPoint(_3byteTo4byte[j*8+i]); //获取该数据的点
                chartData.setTime(getTimeRecord());
                echartsDataList.add(chartData);
            }
            uiEchartsData.setListPacket(echartsDataList);
            uiEchartsDataList.add(uiEchartsData);
        }
        //单独添加一个通道,用来看是否丢包
        UiEchartsData serialNumData = new UiEchartsData();
        List<EchartsData> serialNumDataList = new ArrayList<>(); //新建一个列表用于存放九个点的数据
        //前面4个点用1000补齐
        for (int i = 0; i < 4;i++){
            EchartsData data1 = new EchartsData();
            data1.setDataPoint(1000);
            data1.setTime(getTimeRecord());
            serialNumDataList.add(data1);
        }
        EchartsData orderData = new EchartsData();
        orderData.setDataPoint(serialNum);
        orderData.setTime(getTimeRecord());
        serialNumDataList.add(orderData);
        serialNumData.setListPacket(serialNumDataList);
        uiEchartsDataList.add(serialNumData);

        return uiEchartsDataList; //返回八个通道的数据,每个通道五个点
    }


    //三字节转四字节整型
    private int byteToInt(int[] bytes){
        int DataInt = 0;
        for (int i = 0;i < bytes.length; i++){
            DataInt = (DataInt << 8)|bytes[i];
        }
        //Log.d("移位后的整型数据：", "byteToInt: "+DataInt);
        if ((DataInt & 0x00800000) == 0x00800000){
            DataInt |= 0xFF000000;
        }else {
            DataInt &= 0x00FFFFFF;
        }
        return DataInt;
    }

    public String getTimeRecord(){
        return new SimpleDateFormat("HH:mm:ss:SS").format(new Date().getTime());
    }
}
