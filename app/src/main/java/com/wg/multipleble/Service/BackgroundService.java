package com.wg.multipleble.Service;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.wg.multipleble.Manager.DecoratorBleManager;

import java.util.ArrayList;
import java.util.List;

public class BackgroundService extends Service {

    private TwtBinder twtBinder = new TwtBinder();
    private List<DecoratorBleManager> bleManagerList = new ArrayList<>();

    public BackgroundService() {
    }

    public class TwtBinder extends Binder{

        //添加蓝牙管理者对象
        public void addBleManager(DecoratorBleManager decoratorBleManager){
                bleManagerList.add(decoratorBleManager);
        }

        //获取蓝牙管理者对象
        public DecoratorBleManager getBleManager(int index){
            return bleManagerList.get(index);
        }

    }

    /**
     * 在服务开启时调用
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 在服务绑定时调用
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: 服务已绑定!!");
        return twtBinder;
    }
}
