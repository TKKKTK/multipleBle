package com.wg.multipleble;

import static android.content.ContentValues.TAG;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wg.multipleble.Adpter.FragmentVPAdapter;
import com.wg.multipleble.Data.DataPacket;
import com.wg.multipleble.Fragment.DeviceFragment;
import com.wg.multipleble.Fragment.ScanFragment;
import com.wg.multipleble.Manager.BleManager;
import com.wg.multipleble.Util.FileDownload;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.nordicsemi.android.ble.ConnectRequest;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final static int REQUEST_PERMISSION_CODE = 1;
    private ViewPager viewPager;
    private LinearLayout ble_scan,ble_data;
    private ImageView iv_ble_scan,iv_ble_data;
    private TextView tv_ble_scan,tv_ble_data;
    private FragmentVPAdapter fragmentVPAdapter;
    private List<Fragment> fragmentList;
    private static final int SAVE_CODE = 200;
    private List<DataPacket> dataPacketList = new ArrayList<>();
    private String dataConent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkNeedPermissions();
        requestPermission();
        initView();
        initData();
        fragmentVPAdapter = new FragmentVPAdapter(getSupportFragmentManager(),fragmentList);
        viewPager.setAdapter(fragmentVPAdapter);
        setEvent();
    }

    private void initData() {
        fragmentList = new ArrayList<>();
        ScanFragment scanFragment = new ScanFragment();
        DeviceFragment deviceFragment = new DeviceFragment();

        fragmentList.add(scanFragment);
        fragmentList.add(deviceFragment);

    }


    private void initView(){
        viewPager = findViewById(R.id.vp);

        ble_scan = (LinearLayout) findViewById(R.id.ble_scan);
        ble_data = (LinearLayout) findViewById(R.id.ble_data);

        iv_ble_scan = (ImageView) findViewById(R.id.iv_ble_scan);
        iv_ble_data = (ImageView) findViewById(R.id.iv_ble_data);

        tv_ble_scan = (TextView) findViewById(R.id.tv_ble_scan);
        tv_ble_data = (TextView) findViewById(R.id.tv_ble_data);
    }

    private void setNavGray(){

        iv_ble_scan.setSelected(false);
        tv_ble_scan.setTextColor(getResources().getColor(R.color.gray));

        iv_ble_data.setSelected(false);
        tv_ble_data.setTextColor(getResources().getColor(R.color.gray));
    }

    /**
     * 设置对应的事件
     */
    private void setEvent(){
        /**
         * 设置viewPager的监听回调
         */
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                 onViewPagerSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ble_scan.setOnClickListener(this);
        ble_data.setOnClickListener(this);
        /**
         * 首次进入默认选中扫描界面
         */
        onViewPagerSelected(0);
    }

    public void onViewPagerSelected(int position) {
        setNavGray();
         switch (position){
             case 0:
                 iv_ble_scan.setSelected(true);
                 tv_ble_scan.setTextColor(getResources().getColor(R.color.black));
                 break;
             case 1:
                 iv_ble_data.setSelected(true);
                 tv_ble_data.setTextColor(getResources().getColor(R.color.black));
                 break;
             case 2:
                 break;
         }
    }

    @Override
    public void onClick(View view) {
         int id = view.getId();
         switch (id){
             case R.id.ble_scan:
                 onViewPagerSelected(0);
                 viewPager.setCurrentItem(0);
                 break;
             case R.id.ble_data:
                 onViewPagerSelected(1);
                 viewPager.setCurrentItem(1);
                 break;
         }
    }

    /**
     * 移动到指定的页面
     * @param postion
     */
    public void setViewPager(int postion){
        Log.d(TAG, "setViewPager: ");
        onViewPagerSelected(postion);
        viewPager.setCurrentItem(postion);
    }

    /**
     * 获取碎片并给碎片传值
     */
    public void ByValue(BluetoothDevice device, BleManager bleManager, ConnectRequest connectRequest){
        DeviceFragment deviceFragment = (DeviceFragment) fragmentList.get(1);
        deviceFragment.addDevice(device,bleManager,connectRequest);
    }

    /**
     * 创建一个碎片
     */
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //transaction.replace(R.id.fcv_fragment,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * 选择对应的保存路径
     */
    public void openFileSave(String conent){
        this.dataConent = conent;
        Uri uri = MediaStore.Files.getContentUri("external");
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/txt");
        intent.putExtra(Intent.EXTRA_TITLE, getTimeRecord()+".txt");

        // Optionally, specify a URI for the directory that should be opened in
        // the system file picker when your app creates the document.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        startActivityIfNeeded(intent,SAVE_CODE);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /**
         * 文件保存结果回调
         */
        if (requestCode == SAVE_CODE && resultCode == RESULT_OK){
//            Log.d(TAG, "onActivityResult: "+data.getData());
            FileDownload fileDownload = new FileDownload(dataConent,MainActivity.this);
            fileDownload.saveToUri(data.getData());
        }
    }

    public String getTimeRecord(){
        return new SimpleDateFormat("HH:mm:ss:SS").format(new Date().getTime());
    }


    //动态权限申请
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, perms, REQUEST_PERMISSION_CODE);
            }
        }
    }

    private boolean checkNeedPermissions(){
        boolean isPermit = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //多个权限一起申请
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 1);
            isPermit = true;
        }
        return isPermit;
    }

}