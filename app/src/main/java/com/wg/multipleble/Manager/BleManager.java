package com.wg.multipleble.Manager;

import static android.content.ContentValues.TAG;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wg.multipleble.CallBack.TwowaytoDataCallback;
import com.wg.multipleble.Interface.IconnSuccessfulListenner;
import com.wg.multipleble.Interface.IdataInteractionListenner;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class BleManager extends ObservableBleManager {

    private ScanResult scanResult;
    private  UUID SERVICE_UUID;  //蓝牙通讯服务
    private  UUID READ_UUID;  //读特征
    private  UUID WRITE_UUID;  //写特征 //服务

    private BluetoothGattCharacteristic readCharacteristic,writeCharacteristic;
    private boolean supported;
    private IconnSuccessfulListenner iconnSuccessfulListenner;
    private IdataInteractionListenner idataInteractionListenner;

    public BleManager(@NonNull Context context, ScanResult result) {
        super(context);
        this.scanResult = result;
        getUUID();
    }

    public BleManager(@NonNull Context context, @NonNull Handler handler) {
        super(context, handler);
    }

    public void setIconnSuccessfulListenner(IconnSuccessfulListenner listenner){
         this.iconnSuccessfulListenner = listenner;
    }

    public void setIdataInteractionListenner(IdataInteractionListenner listenner){
        this.idataInteractionListenner = listenner;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new DeviceBleManagerGattCallback();
    }

    /**
     * 自动获取设备的服务UUID
     */
    public void getUUID(){
        SERVICE_UUID = scanResult.getScanRecord().getServiceUuids().get(0).getUuid();
    }

    /**
     * 数据接收回调
     */
    private final TwowaytoDataCallback twowaytoDataCallback = new TwowaytoDataCallback() {
        @Override
        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
            super.onDataSent(device, data);
        }

        @Override
        public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            super.onDataReceived(device, data);
//            Log.d(TAG, "onDataReceived: "+data);
            if (idataInteractionListenner != null){
                idataInteractionListenner.DataReceiving(data);
            }
        }

        @Override
        public void onInvalidDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            super.onInvalidDataReceived(device, data);
            //Log.d(TAG, "onInvalidDataReceived: "+data);
        }
    };


    /**
     * 蓝牙连接回调
     */
    private class DeviceBleManagerGattCallback extends BleManagerGattCallback{
        @Override
        protected void initialize() {
            super.initialize();
            setNotificationCallback(readCharacteristic).with(twowaytoDataCallback);
            readCharacteristic(readCharacteristic).with(twowaytoDataCallback).enqueue();
            //enableNotifications(readCharacteristic).enqueue();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService twtService = gatt.getService(SERVICE_UUID);

            if (twtService != null){
                List<BluetoothGattCharacteristic> characteristicList = twtService.getCharacteristics();
                for (BluetoothGattCharacteristic characteristic : characteristicList){
                    if (characteristic.getProperties() == 12){
                         WRITE_UUID = characteristic.getUuid();
                    }else if (characteristic.getProperties() == 16){
                         READ_UUID = characteristic.getUuid();
                    }
                    Log.d("DeviceBleManager", "获取特征: "+characteristic.getUuid());
                    Log.d("DeviceBleManager", "特征的属性: "+characteristic.getProperties());
                }
                readCharacteristic = twtService.getCharacteristic(READ_UUID);
                writeCharacteristic = twtService.getCharacteristic(WRITE_UUID);
            }
            boolean twtWriteRequest = false;
            if (writeCharacteristic != null){
                final int writeProperties = writeCharacteristic.getProperties();
                twtWriteRequest = (writeProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }

            supported = readCharacteristic !=null && writeCharacteristic != null & twtWriteRequest;
            Log.d(TAG, "isRequiredServiceSupported: "+supported);
            //连接成功回调
            if (supported){
                iconnSuccessfulListenner.successfullCallback();
            }
            return supported;
        }

        @Override
        protected void onServicesInvalidated() {
            readCharacteristic = null;
            writeCharacteristic = null;
        }
    }

        public void startNotifications(){
            enableNotifications(readCharacteristic).enqueue();
        }

        public void stopNotifications(){
            disableNotifications(readCharacteristic).enqueue();
        }

        public void  SendData(int cmd){
            if (writeCharacteristic == null){
                return;
            }
            writeCharacteristic(writeCharacteristic
                    ,new Data(new byte[]{(byte)cmd})
                    ,BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT).with(twowaytoDataCallback).enqueue();
        }
}
