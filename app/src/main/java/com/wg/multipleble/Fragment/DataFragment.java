package com.wg.multipleble.Fragment;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wg.multipleble.Adpter.DataListAdapter;
import com.wg.multipleble.ChartDataActivity;
import com.wg.multipleble.Data.DataPacket;
import com.wg.multipleble.Interface.IdataInteractionListenner;
import com.wg.multipleble.MainActivity;
import com.wg.multipleble.Manager.BleManager;
import com.wg.multipleble.Manager.DecoratorBleManager;
import com.wg.multipleble.Manager.DecoratorParent;
import com.wg.multipleble.R;
import com.wg.multipleble.Service.BackgroundService;
import com.wg.multipleble.Util.ObjectPool;
import com.wg.multipleble.Util.FileDownload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import no.nordicsemi.android.ble.ConnectRequest;
import no.nordicsemi.android.ble.data.Data;

public class DataFragment extends BaseFragment implements View.OnClickListener {

    private ImageButton dataSend,dataClose,listTop,listBottom,chartData,download,link;
    private RecyclerView recyclerView;
    private DataListAdapter dataListAdapter;
    private List<DataPacket> dataPackets = new ArrayList<>();
    public  BluetoothDevice bluetoothDevice;
    private BleManager bleManager;
    private ConnectRequest connectRequest;
    private DecoratorBleManager decoratorBleManager;
    private boolean isResivice = false; //是否开启接收数据的线程
    private int count;
    private BackgroundService.TwtBinder twtBinder;
    private String CacheFileName;
    private Queue<DataPacket> dataPacketQueue = new LinkedList<DataPacket>();
    private FileDownload fileDownload;
    private String file_name;


    public DataFragment(DecoratorBleManager decoratorBleManager,BluetoothDevice bluetoothDevice) {

        this.bluetoothDevice = bluetoothDevice;


//        this.decoratorBleManager = decoratorBleManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_data_layout,container,false);
        this.decoratorBleManager = ObjectPool.getBleManager(bluetoothDevice.getAddress());
        MainActivity activity = (MainActivity) getActivity();
        //this.decoratorBleManager = activity.twtBinder.getBleManager(0);
        initView(view);
        setCallback();
        setOnClickEvent();
        Log.d(getClass().getSimpleName(), "onCreateView: ");
        //this.bleManager.disconnect().enqueue();
        return view;
    }

    public void initView(View view){
        dataSend = view.findViewById(R.id.data_send);
        dataClose = view.findViewById(R.id.data_close);
        listTop = view.findViewById(R.id.list_top);
        listBottom = view.findViewById(R.id.list_bottom);
        link = view.findViewById(R.id.link);
        chartData = view.findViewById(R.id.chart_data);
        download = view.findViewById(R.id.download);
        recyclerView = view.findViewById(R.id.data_list_rv);
        dataListAdapter = new DataListAdapter(dataPackets);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(dataListAdapter);

    }

    public void setCallback(){
        if (decoratorBleManager.bleManager != null){
            decoratorBleManager.bleManager.setIdataInteractionListenner(new IdataInteractionListenner() {
                @Override
                public void DataReceiving(Data data) {
                    DataPacket dataPacket = new DataPacket(data,getTimeRecord());
                    if (dataPackets.size()>=200){
                        dataPackets.clear();
                        dataListAdapter.notifyDataSetChanged();
                        count = 0;
                    }
                    dataPackets.add(dataPacket);
                    Log.d(TAG, "主页面里面的数据列表容器长度: " + dataPackets.size());
                    dataPacketQueue.add(dataPacket);
                    count++;
                }
            });
        }
    }

    public void setOnClickEvent(){
        dataSend.setOnClickListener(this);
        dataClose.setOnClickListener(this);
        listTop.setOnClickListener(this);
        listBottom.setOnClickListener(this);
        link.setOnClickListener(this);
        download.setOnClickListener(this);
        chartData.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.data_send:
                if (decoratorBleManager != null) {
                    this.file_name = getTimeRecord()+".txt";
                    MainActivity mainActivity = (MainActivity)getActivity();
                    this.fileDownload = new FileDownload(mainActivity,file_name);
                    //获取Uri
                    fileDownload.CreateUri();
                    DataListThread dataListThread = new DataListThread();
                    new Thread(dataListThread).start();
                    isResivice = true;
                    DataCacheThread dataCacheThread = new DataCacheThread();
                    new Thread(dataCacheThread).start();
                    decoratorBleManager.startNotifications();
                }
                dataSend.setVisibility(View.GONE);
                dataClose.setVisibility(View.VISIBLE);
                break;
            case R.id.data_close:
                if (decoratorBleManager != null){
                    isResivice = false;
                    decoratorBleManager.stopNotifications();
                }
                dataClose.setVisibility(View.GONE);
                dataSend.setVisibility(View.VISIBLE);
                //存入最后一次数据
                WriteDataCache();
                break;
            case R.id.list_top:
                recyclerView.scrollToPosition(0);
                break;
            case R.id.list_bottom:
                recyclerView.scrollToPosition(dataPackets.size()-1);
                break;
            case R.id.link:
                LinkDialog();
                break;
            case R.id.chart_data:
                Intent intent = new Intent(getActivity(), ChartDataActivity.class);
                intent.putExtra(ChartDataActivity.BLEMANAGER,bluetoothDevice.getAddress().toString());
                ObjectPool.addBleManager(bluetoothDevice.getAddress().toString(),decoratorBleManager);
                startActivity(intent);
                break;
            case R.id.download:
                if (decoratorBleManager != null){
                    isResivice = false;
                    decoratorBleManager.stopNotifications();
                }

                break;
        }
    }

    /**
     * 弹出框指令发送
     */
    public void LinkDialog(){
        final String[] items3 = new String[]{"1", "2", "3"};//创建item
        AlertDialog alertDialog3 = new AlertDialog.Builder(getActivity())
                .setTitle("发送指令")
                .setIcon(R.mipmap.ic_launcher)
                .setItems(items3, new DialogInterface.OnClickListener() {//添加列表
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                         switch (i){
                             case 0:
                                 isResivice = true;
                                 decoratorBleManager.sendLink(1);
                                 break;
                             case 1:
                                 isResivice = false;
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

    class DataListThread extends Thread{
        @Override
        public void run() {
            super.run();
            while (isResivice){
                try {
                    Thread.sleep(1000);
                    Message message = new Message();
                    message.what = 1;
                    message.arg1 = count;
                    handler.sendMessage(message);
                    count = 0;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what ==1){
                int nums = msg.arg1;
                dataListAdapter.notifyItemRangeChanged(dataPackets.size()-1,nums);
                recyclerView.scrollToPosition(dataPackets.size()-1);

            }
        }
    };

    /**
     * 数据缓存线程
     */
    class DataCacheThread extends Thread{
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void run() {
            super.run();
            while (isResivice){
                try {
                    Thread.sleep(5000);
                    WriteDataCache();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 断开连接
     * @return
     */
    public void disconnect(){
        decoratorBleManager.disconnect();
        dataPackets.clear();
        dataListAdapter.notifyDataSetChanged();
    }

    public String getTimeRecord(){
        return new SimpleDateFormat("HH:mm:ss:SS").format(new Date().getTime());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //dataPackets.clear();
    }


    /**
     * 写入数据缓存
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void WriteDataCache(){
        fileDownload.saveCacheData(DataByString());
    }

    /**
     * 把数据拼接成字符串
     */
    public String DataByString(){
        StringBuilder stringBuilder = new StringBuilder();
        while (!dataPacketQueue.isEmpty()){
            DataPacket dataPacket = dataPacketQueue.poll();
            if (dataPacket != null){
                stringBuilder.append(dataPacket.getData().toString());
                stringBuilder.append("  ");
            }
        }
        return stringBuilder.toString();
    }
}
