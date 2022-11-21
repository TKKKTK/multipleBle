package com.wg.multipleble;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;

import com.github.mikephil.charting.charts.LineChart;
import com.wg.multipleble.Data.EchartsData;
import com.wg.multipleble.Data.UiEchartsData;
import com.wg.multipleble.EDFlib.EDFException;
import com.wg.multipleble.EDFlib.EDFwriter;
import com.wg.multipleble.Interface.IdataInteractionListenner;
import com.wg.multipleble.Manager.DecoratorBleManager;
import com.wg.multipleble.Util.BrainDataSolution;
import com.wg.multipleble.Util.DataSolution;
import com.wg.multipleble.Util.LineChartUtil;
import com.wg.multipleble.Util.ObjectPool;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import no.nordicsemi.android.ble.data.Data;

public class ChartDataActivity extends BaseActivity implements View.OnClickListener{
    public static final String BLEMANAGER = "bleManager";
    private DecoratorBleManager decoratorBleManager;
    private Button data_start,data_stop,link,start_save,download,back,sign;
    private LineChart lineChart;
    private LineChartUtil lineChartUtil;
    private boolean isSignClick = false; //是否点击记录标签按钮
    private boolean isSave = false; //是否点击保存按钮
    private Queue<List<UiEchartsData>> listQueue = new LinkedList<>(); // 暂存八个通道的数据
    private EDFwriter hdl = null; //EDF文件存储对象
    private List<Queue<int[]>> edfTWriteList = new ArrayList<>(); //存放edf解析后将要存入edf中的数据
    private List<List<EchartsData>> echartsDataList = new ArrayList<>(); //用于edf记录数据标签
    private int T = 500; //硬件的采样频率
    private int count = 0; //用于记录周期数
    private int total = 0; //用于记录打标签的时间
    private boolean isEnd = false; //判断是否是最后一次存入数据
    private DeviceType deviceType; //用于记录是什么类型的设备
    private int step = 5; //用于记录每个通道的点数(一个包中)
    private WriteEDFThread mWriteEDFThread;
    private int countTest = 0; //用于测试记录包的总数
    private boolean isBreak = false; //用于记录是否退出子线程里面的循环
    private String startSaveTime; //用于记录开始存储的时间
    private String stopSaveTime; //用于记录结束存储的时间


    public enum DeviceType{
        iFocus,
        EMG,
        brain
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart_layout);
        initView();
        addEvent();
        Intent intent = getIntent();
        String key = intent.getStringExtra(BLEMANAGER);
        this.decoratorBleManager = ObjectPool.getBleManager(key);
        setCallBack();
        DeviceSelect();
    }

    private void initView(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.hide();
            actionBar.setDisplayShowCustomEnabled(true);
        }
        lineChart = findViewById(R.id.linechart);
        lineChartUtil = new LineChartUtil(lineChart);
        data_start = findViewById(R.id.data_start);
        data_stop = findViewById(R.id.data_stop);
        link = findViewById(R.id.data_link);
        start_save = findViewById(R.id.start_save_data);
        download = findViewById(R.id.download_file);
        back = findViewById(R.id.back);
        sign = findViewById(R.id.sign);
    }

    private void addEvent(){
        data_start.setOnClickListener(this);
        data_stop.setOnClickListener(this);
        link.setOnClickListener(this);
        start_save.setOnClickListener(this);
        download.setOnClickListener(this);
        back.setOnClickListener(this);
        sign.setOnClickListener(this);
    }

    /**
     * 设置数据接收回调
     * @param
     */
    public void setCallBack(){
         if (decoratorBleManager.bleManager != null){
             decoratorBleManager.bleManager.setIdataInteractionListenner(new IdataInteractionListenner() {
                 @RequiresApi(api = Build.VERSION_CODES.O)
                 @Override
                 public void DataReceiving(Data data) {
                 //Log.d("ChartDataActivity", "DataReceiving: "+data.toString());
                     List<UiEchartsData> uiEchartsDataList = null;
                   //判断是什么类型的数据
                   switch (deviceType){
                       case iFocus:
                           break;
                       case EMG:
                           step = 5;
                           uiEchartsDataList = DataSolution.getInstance().solution(data);
                           break;
                       case brain:
                           step = 9;
                           uiEchartsDataList = BrainDataSolution.getInstance().solution(data);
                           break;
                   }
                      for (int j = 0; j < uiEchartsDataList.get(0).getListPacket().size();j++){ //八个通道时遍历第一个通道就可以
                          if (isSignClick){
                              //给每个通道的第j个点打上标签
                              for (int i = 0; i < uiEchartsDataList.size();i++){
                                  //获取第j个点
                                  EchartsData echartsData = uiEchartsDataList.get(i).getListPacket().get(j);
                                  echartsData.setRecord(true);
                                  uiEchartsDataList.get(i).getListPacket().set(j,echartsData);
                              }
                              isSignClick = false;
                          }
                      }
                      //是否开启保存
                      if (isSave){
                          listQueue.add(uiEchartsDataList);
                      }

                    lineChartUtil.UpdateData(uiEchartsDataList);
                 }
             });
         }
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onClick(View v) {
      int id = v.getId();
      switch (id){
          case R.id.data_start:
              decoratorBleManager.startNotifications();
              data_start.setVisibility(View.GONE);
              data_stop.setVisibility(View.VISIBLE);
              break;
          case R.id.data_stop:
              decoratorBleManager.stopNotifications();
              data_stop.setVisibility(View.GONE);
              data_start.setVisibility(View.VISIBLE);
              break;
          case R.id.data_link:
              LinkDialog();
              break;
          case R.id.start_save_data:
               StartSave();
               mWriteEDFThread = new WriteEDFThread();
               mWriteEDFThread.start();
              break;
          case R.id.download_file:
               ImportFile();
              break;
          case R.id.back:
              finish();
              break;
          case R.id.sign:
              isSignClick = true;
              break;
      }
    }

    /**
     * 开启保存
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void StartSave(){
        SetEDF();
        initDataEdf();
        isEnd = false;
        isSave = true;
        start_save.setEnabled(false);
        download.setEnabled(true);
        startSaveTime = getTimeRecord();

    }

    /**
     * 文件导出
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void ImportFile(){
        //关闭保存
//        mWriteEDFThread.interrupt();
//        mWriteEDFThread = null;
        isSave = false;
        isEnd = true;
        writeEDF();
        start_save.setEnabled(true);
        download.setEnabled(false);
        closeEdf();
        stopSaveTime = getTimeRecord();
        Log.d(TAG, "文件的开始存储时间: "+startSaveTime);
        Log.d(TAG, "文件的结束存储时间: "+stopSaveTime);
    }

    /**
     * 写入EDF文件的线程
     */
    class WriteEDFThread extends Thread{
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            super.run();
            while(isSave){
                try {
                    Thread.sleep(1500);
                    writeEDF(); // 写入edf数据
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * 设置EDF相关参数
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void SetEDF(){
        int sf1=T, // 通道1的采样频率
                edfsignals = 9; //通道数
        try
        {
            hdl = new EDFwriter(getTimeRecord()+".bdf", EDFwriter.EDFLIB_FILETYPE_BDFPLUS, edfsignals,ChartDataActivity.this);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        catch(EDFException e)
        {
            e.printStackTrace();
            return;
        }

        for (int i = 0;i < edfsignals; i++){
            //设置信号的最大物理值
            hdl.setPhysicalMaximum(i, (int) Math.pow(2,23)-1);
            //设置信号的最小物理值
            hdl.setPhysicalMinimum(i, (int) Math.pow(-2,23));
            //设置信号的最大数字值
            hdl.setDigitalMaximum(i, (int) Math.pow(2,23)-1);
            //设置信号的最小数字值
            hdl.setDigitalMinimum(i, (int) Math.pow(-2,23));
            //设置信号的物理单位
            hdl.setPhysicalDimension(i, String.format("uV"));

            //设置采样频率
            hdl.setSampleFrequency(i, sf1);

            //设置信号标签
            hdl.setSignalLabel(i, String.format("sine"+T+"HZ", 0 + 1));
        }
        /**
         * 开头写入标签
         */
        hdl.writeAnnotation(0, -1, "Recording starts");

    }

    /**
     * 写入EDF
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void writeEDF(){
        try {
            edfDataJoint();
            int err = 0;
            while(!edfTWriteList.get(0).isEmpty()){
                for (int i = 0; i < edfTWriteList.size();i++ ){
                    int[] buf = edfTWriteList.get(i).poll();
                    try {
                        err = hdl.writeDigitalSamples(buf);
                        if(err != 0)
                        {
                            System.out.printf("writePhysicalSamples() returned error: %d\n", err);
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }catch (Exception e){
            isSave = false;
            isEnd = true;
            writeEDF();
            closeEdf();
        }


    }

    /**
     * 写入中间信号标签
     */
    public void writeSfSign(){
       int size =echartsDataList.get(0).size();
       //拿到一个通道的数据
        List<EchartsData> dataList = echartsDataList.get(0);

        for (int i = 0;i < size;i++){
            total ++;
            EchartsData echartsData = dataList.get(i);
            if (echartsData.isRecord()){
                hdl.writeAnnotation(calculateTime(total),-1,"Recording");
            }
        }

       for (int i = 0; i < echartsDataList.size(); i++){
             echartsDataList.get(i).clear(); //打完标签后清空数据
       }
    }

    /**
     * 脑电数据中写入标签需要整周期，整周期的写入
     */
    public void writeTSFSign(){
        //拿到一个通道的数据
        List<EchartsData> dataList = echartsDataList.get(0);
        for (int i = 0; i < T; i++){
            total++;
            EchartsData echartsData = dataList.get(i);
            if (echartsData.isRecord()){
                hdl.writeAnnotation(calculateTime(total),-1,"Recording");
            }
        }

        for (int i = 0; i < echartsDataList.size();i++){
            for (int j = 0; j < T;j++){
                echartsDataList.get(i).remove(0); //打完标签后移除一个周期的元素
            }
        }
    }

    /**
     * 关闭edf文件流
     */
    public void closeEdf(){
        /**
         * 结尾写入标签
         */
        hdl.writeAnnotation(calculateTime(total), -1, "Recording ends");
        try
        {
            hdl.close();
            //Toast.makeText(EchartsActivity.this, "导出EDF文件成功", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return;
        }
        catch(EDFException e)
        {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 计算当前点的时间 -- 用于记录标签
     * @param total
     * @return
     */
    private long calculateTime(int total){
        float totalTime = (float) total/T * 10000;
        return (long)totalTime;
    }

    /**
     * EDF数据拼接
     */
    public void edfDataJoint(){
        //
        if (listQueue.isEmpty()){
            return;
        }
        int length = listQueue.size();
        int f = 0;

        while (f<length){
//            if (isBreak){
//                isBreak = false;
//                break;
//            }
           List<UiEchartsData> uiEchartsDataList = listQueue.poll();
           f++;
           for (int i = 0;i < uiEchartsDataList.size();i++){ //总共八个通道
               List<EchartsData> echartsData = uiEchartsDataList.get(i).getListPacket(); //得到每个通道的五个点
               List<EchartsData> echartsPointList = echartsDataList.get(i);
               for (int j = 0;j < echartsData.size();j++){
                   echartsPointList.add(echartsData.get(j));
               }
               echartsDataList.set(i,echartsPointList); //添加edf打标签的数据
           }
            count += step;
            countTest += step;
           //判断是否是最后一次存入数据
           if (isEnd){
                int num = echartsDataList.get(0).size();
                //buf数根据周期数取整
               double TInt = Math.ceil ((double) num/T); //向上取整
               for (int i = 0; i < edfTWriteList.size();i++){
                   Log.d("edfDataJoint", "剩余的数量==> "+ echartsDataList.get(i).size());
                   //拿到每个通道的数据
                   List<EchartsData> pointList = echartsDataList.get(i);
                   Queue<int[]> edfIntBuf = edfTWriteList.get(i);
                   int index = 0;
                   int a = 0;
                   for (int j = 0; j < TInt; j++){
                       a++;
                       Log.d("edfDataJoint", "新建buf容器个数==>"+a);
                        int[] buf1 = new int[T];
                        for (int k = 0; k < T;k++){
                            if (index < num){
                                buf1[k] = pointList.get(index).getDataPoint();
                                index ++;
                            }else {
                                break;
                            }
                        }
                     edfIntBuf.add(buf1);
                     edfTWriteList.set(i,edfIntBuf);
                   }
               }
               writeSfSign(); //写入信号标签
               return;
           }else {
               switch (deviceType){
                   case iFocus:
                       break;
                   case EMG:
                       EmgEdfJoint();
                       break;
                   case brain:
                       BrainEdfJoint();
                       break;
               }
           }

        }
    }

    /**
     * 脑电EDF数据拼接
     */
    public void BrainEdfJoint(){
         if (count > T){
             for (int i = 0; i < edfTWriteList.size();i++){
                 //拿到每个通道的数据
                 List<EchartsData> echartsPoint = echartsDataList.get(i);
                 int[] buf = new int[T];
                 for (int j = 0; j < T; j++){
                     buf[j] = echartsPoint.get(j).getDataPoint();
                 }
                 Queue<int[]> edfQueue = edfTWriteList.get(i);
                 edfQueue.add(buf);
                 edfTWriteList.set(i,edfQueue);
             }
             count = count - T;
             //满一个周期做判断打一次标签
             writeTSFSign();
             //isBreak = true;
         }
    }

    /**
     * 肌电EDF数据拼接
     */
    public void EmgEdfJoint(){
        if (count % T == 0){
            for (int i = 0; i < edfTWriteList.size();i++){
                //拿到每个通道的数据
                List<EchartsData> echartsPoint = echartsDataList.get(i);
                int[] buf = new int[T];
                for (int j = 0; j < T; j++){
                    buf[j] = echartsPoint.get(j).getDataPoint();
                }
                Queue<int[]> edfQueue = edfTWriteList.get(i);
                edfQueue.add(buf);
                edfTWriteList.set(i,edfQueue);
            }
            //满一个周期做判断打一次标签
            writeSfSign();
            //isBreak = true;
        }
    }

    /**
     * 初始化edf数据容器
     */
     public void initDataEdf(){
         count = 0;
         total = 0;
         edfTWriteList.clear();
         echartsDataList.clear();
         for (int i = 0; i < 9; i++){
             Queue<int[]> edfQueue = new LinkedList<>();
             List<EchartsData> dataList = new ArrayList<>();
             edfTWriteList.add(edfQueue);
             echartsDataList.add(dataList);
         }
     }


    /**
     * 弹出框指令发送
     */
    public void LinkDialog(){
        final String[] items3 = new String[]{"1", "2", "3"};//创建item
        AlertDialog alertDialog3 = new AlertDialog.Builder(this)
                .setTitle("发送指令")
                .setIcon(R.mipmap.ic_launcher)
                .setItems(items3, new DialogInterface.OnClickListener() {//添加列表
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                decoratorBleManager.sendLink(1);
                                break;
                            case 1:
                                decoratorBleManager.sendLink(2);
                                break;
                            case 2:
                                decoratorBleManager.sendLink(3);
                                break;
                        }
                    }
                })
                .create();
        alertDialog3.show();
    }


    /**
     * 设备选择
     * @return
     */
    public void DeviceSelect(){
        final String[] items3 = new String[]{"iFocus", "EMG", "brain"};//创建item
        AlertDialog alertDialog3 = new AlertDialog.Builder(this)
                .setTitle("发送指令")
                .setIcon(R.mipmap.ic_launcher)
                .setItems(items3, new DialogInterface.OnClickListener() {//添加列表
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                deviceType = DeviceType.iFocus;
                                break;
                            case 1:
                                deviceType = DeviceType.EMG;
                                T = 250;
                                break;
                            case 2:
                                deviceType = DeviceType.brain;
                                T = 500;
                                break;
                        }
                    }
                })
                .create();
        alertDialog3.show();
    }



    public String getTimeRecord(){
        return new SimpleDateFormat("HH_mm_ss_SS").format(new Date().getTime());
    }

}
