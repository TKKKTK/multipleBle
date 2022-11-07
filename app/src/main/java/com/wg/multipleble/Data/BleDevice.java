package com.wg.multipleble.Data;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

/**
 * 蓝牙设备实体类
 */
public class BleDevice implements Parcelable {
    private String name;
    private String address;
    private BluetoothDevice device;
    private int rssi;
    private ScanResult scanResult;

    @SuppressLint("MissingPermission")
    public BleDevice(ScanResult result){
        device = result.getDevice();
        name = device.getName();
        address = device.getAddress();
        rssi = result.getRssi();
        scanResult = result;
    }


    protected BleDevice(Parcel in) {
        name = in.readString();
        address = in.readString();
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        rssi = in.readInt();
        scanResult = in.readParcelable(ScanResult.class.getClassLoader());
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel in) {
            return new BleDevice(in);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }

    public void setScanResult(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(address);
        parcel.writeParcelable(device, i);
        parcel.writeInt(rssi);
        parcel.writeParcelable(scanResult,i);
    }
}
