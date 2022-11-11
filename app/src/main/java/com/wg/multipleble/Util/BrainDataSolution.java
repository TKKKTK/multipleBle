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
 * 解析八通道脑电数据
 */
public class BrainDataSolution {
    private static BrainDataSolution _instance;
    private BrainDataSolution(){

    }

    public static BrainDataSolution getInstance(){
        if (_instance == null){
            _instance = new BrainDataSolution();
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
        int[] subData = new int[216];
        //存放反转后的数据
        int[][] echartsData = new int[72][3];
        //存放3字节转4字节的数据
        int[] _3byteTo4byte = new int[72];

        for (int i = 0;i < arr1.length;i ++){
            dataArr[i] = Integer.valueOf(arr1[i],16); //16字节字符串转
        }
        System.arraycopy(dataArr,2,subData,0,216);
        for (int i = 0; i < echartsData.length;i++){
            System.arraycopy(subData,i*3,echartsData[i],0,3);
        }
        //高低位反转echartsData = {int[72][]@13384}
//        for (int i = 0; i<echartsData.length;i++){
//            Stack<Integer> stack = new Stack<Integer>();
//            for (int j = 0;j<echartsData[i].length;j++){
//                stack.push(echartsData[i][j]);
//            }
//            for (int z = 0;z<echartsData[i].length;z++){
//                echartsData[i][z]=stack.pop();
//            }
//        }
        //三字节转四字节整型
        for (int i = 0;i < echartsData.length;i++){
            _3byteTo4byte[i] = byteToInt(echartsData[i]);
        }

        //一个通道五个点，一共有八个通道
        List<UiEchartsData> uiEchartsDataList = new ArrayList<>();
        for (int i = 0;i < 8; i++){
            UiEchartsData uiEchartsData = new UiEchartsData(); //存放一个通道中的九个点
            List<EchartsData> echartsDataList = new ArrayList<>();//新建一个存放九个点的数据
            for (int j = 0;j<9;j++){
                EchartsData chartData = new EchartsData();
                chartData.setDataPoint(_3byteTo4byte[j*8+i]); //获取该数据的点
                chartData.setTime(getTimeRecord());
                echartsDataList.add(chartData);
            }
            uiEchartsData.setListPacket(echartsDataList);
            uiEchartsDataList.add(uiEchartsData);
        }


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
