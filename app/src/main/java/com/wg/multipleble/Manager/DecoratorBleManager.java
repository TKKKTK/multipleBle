package com.wg.multipleble.Manager;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class DecoratorBleManager{
    public BleManager bleManager;

    public DecoratorBleManager(BleManager bleManager){
          this.bleManager = bleManager;
    }
    /**
     * 开启通知
     */
    public void startNotifications(){
        this.bleManager.startNotifications();
    }

    /**
     * 关闭通知
     */
    public void stopNotifications(){
        this.bleManager.stopNotifications();
    }

    /**
     * 发送指令
     */
    public void sendLink(int link){
        this.bleManager.SendData(link);
    }

    public void disconnect(){
        this.bleManager.disconnect().enqueue();
    }
}
