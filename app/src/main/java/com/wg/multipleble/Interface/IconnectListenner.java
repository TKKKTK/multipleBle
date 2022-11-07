package com.wg.multipleble.Interface;

import android.bluetooth.BluetoothDevice;

import com.wg.multipleble.Data.BleDevice;

public interface IconnectListenner {
    void DeviceConnect(BleDevice bleDevice,int position);
    void DeviceTaget(BluetoothDevice bluetoothDevice);

}
