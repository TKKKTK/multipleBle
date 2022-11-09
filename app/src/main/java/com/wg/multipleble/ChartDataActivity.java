package com.wg.multipleble;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.github.mikephil.charting.charts.LineChart;
import com.wg.multipleble.Adpter.ChartFragmentAdapter;
import com.wg.multipleble.Data.UiEchartsData;
import com.wg.multipleble.Fragment.ChartFragment;
import com.wg.multipleble.Interface.IdataInteractionListenner;
import com.wg.multipleble.Manager.DecoratorBleManager;
import com.wg.multipleble.Util.DataSolution;
import com.wg.multipleble.Util.LineChartUtil;
import com.wg.multipleble.Util.ObjectPool;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.ble.data.Data;

public class ChartDataActivity extends BaseActivity implements View.OnClickListener{
    public static final String BLEMANAGER = "bleManager";
    private DecoratorBleManager decoratorBleManager;
    private Button data_start,data_stop,link,start_save,download,back;
    private LineChart lineChart;
    private LineChartUtil lineChartUtil;

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
    }

    private void addEvent(){
        data_start.setOnClickListener(this);
        data_stop.setOnClickListener(this);
        link.setOnClickListener(this);
        start_save.setOnClickListener(this);
        download.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    /**
     * 设置数据接收回调
     * @param
     */
    public void setCallBack(){
         if (decoratorBleManager.bleManager != null){
             decoratorBleManager.bleManager.setIdataInteractionListenner(new IdataInteractionListenner() {
                 @Override
                 public void DataReceiving(Data data) {
                     Log.d("ChartDataActivity", "DataReceiving: "+data.toString());
                    List<UiEchartsData> uiEchartsDataList = DataSolution.getInstance().solution(data);
                    lineChartUtil.UpdateData(uiEchartsDataList);
                 }
             });
         }
    }


    @Override
    public void onClick(View v) {
      int id = v.getId();
      switch (id){
          case R.id.data_start:
              decoratorBleManager.startNotifications();
              break;
          case R.id.data_stop:
              decoratorBleManager.stopNotifications();
              break;
          case R.id.data_link:
              LinkDialog();
              break;
          case R.id.start_save_data:

              break;
          case R.id.download_file:

              break;
          case R.id.back:
              finish();
              break;
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


}
