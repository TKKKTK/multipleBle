package com.wg.multipleble;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wg.multipleble.Service.BackgroundService;

public class BaseActivity extends AppCompatActivity {

    public BackgroundService.TwtBinder twtBinder;

    //通过服务连接类获取twtBinder对象
    public ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) { //服务连接成功时调用
            twtBinder = (BackgroundService.TwtBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { //服务断开连接时调用

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //绑定后台服务
        Intent startIntent = new Intent(this,BackgroundService.class);
        bindService(startIntent,connection,BIND_AUTO_CREATE);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
