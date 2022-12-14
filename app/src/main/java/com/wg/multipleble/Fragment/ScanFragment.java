package com.wg.multipleble.Fragment;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.wg.multipleble.Adpter.ScanAdpter;
import com.wg.multipleble.Data.BleDevice;
import com.wg.multipleble.Interface.IconnSuccessfulListenner;
import com.wg.multipleble.Interface.IconnectListenner;
import com.wg.multipleble.MainActivity;
import com.wg.multipleble.Manager.BleManager;
import com.wg.multipleble.Manager.DecoratorBleManager;
import com.wg.multipleble.R;
import com.wg.multipleble.Util.ObjectPool;
import com.wg.multipleble.Util.UUIDList;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.ble.ConnectRequest;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class ScanFragment extends BaseFragment implements IconnectListenner {

    private List<BleDevice> deviceList = new ArrayList<>();
    private ScanAdpter scanAdpter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar state_scanning;
    private BluetoothAdapter bluetoothAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scan_fragment,container,false);
        initView(view);
        startScan();
        Log.d(getClass().getSimpleName(), "onCreateView: ");
        return view;
    }


    private void initView(View view){
        state_scanning = (ProgressBar)view.findViewById(R.id.state_scanning);
        recyclerView = (RecyclerView) view.findViewById(R.id.device_list);
        scanAdpter = new ScanAdpter(deviceList);
        scanAdpter.setIconnectListenner(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(scanAdpter);

        /**
         * ????????????
         */
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refresh_layout);
        // ????????????????????????????????????????????????????????????????????????????????????16??????????????????????????????int???????????????
        // ?????????????????????????????????????????????????????????
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        // ?????????????????????????????????
        //swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary, R.color.colorPrimaryDark);
        final Handler handler = new Handler();
       //???????????????SwipeRefreshLayout????????????????????????????????????????????????????????????
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // ??????????????????
                // ???????????????????????????????????????????????????????????????????????????????????????
                new Thread(){
                    @Override
                    public void run () {
                        super.run();
                        //????????????????????????
                        //???????????? ????????? ?????????????????? ???????????????
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // ??????????????????????????????????????????????????????????????????
                                swipeRefreshLayout.setRefreshing(false);
                                stopScan();
                                startScan();
                                deviceList.clear();
                                scanAdpter.notifyDataSetChanged();
                            }
                        }, 200);
                    }
                }.start();
            }
        });
    }

    /**
     * ????????????
     */
    public void startScan(){
         bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
         if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
             state_scanning.setVisibility(View.VISIBLE);
             final ScanSettings settings = new ScanSettings.Builder()
                     .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                     .setReportDelay(100)
                     .setUseHardwareBatchingIfSupported(false)
                     .build();
             final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
             scanner.startScan(null,settings,scanCallback);
         }else {
             @SuppressLint("MissingPermission")
             boolean enable = bluetoothAdapter.enable();
             if (enable){
                 startScan();
             }
         }
    }
    /**
     * ????????????
     */
    public void stopScan(){
        state_scanning.setVisibility(View.INVISIBLE);
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results){
                BleDevice bleDevice = new BleDevice(result);
                if (indexOf(bleDevice) == -1 && bleDevice.getName() != null && result.getScanRecord().getServiceUuids() != null){
                    deviceList.add(bleDevice);
                    scanAdpter.notifyItemRangeChanged(deviceList.size()-1,1);
                    //???RecyclerView?????????????????????
                    recyclerView.scrollToPosition(deviceList.size()-1);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.w("ScannerViewModel", "Scanning failed with code " + errorCode);
            if (errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                stopScan();
                startScan();
            }
        }
    };

    /**
     * ????????????
     */
    public int indexOf(BleDevice bleDevice){
        int i = 0;
        for (BleDevice device : deviceList){
            if (device.getAddress().equals(bleDevice.getAddress()))
                return i;
            i++;
        }
        return -1;
    }

    /**
     * ???????????????????????????
     * @param bleDevice
     */
    @Override
    public void DeviceConnect(BleDevice bleDevice,int position) {
            BleManager bleManager = new BleManager(getActivity(),bleDevice.getScanResult());
            ConnectRequest connectRequest = bleManager.connect(bleDevice.getDevice())
                    .retry(3,100)
                    .useAutoConnect(false)
                    .then(device -> {});
            connectRequest.enqueue();
            /**
             * ????????????????????????
             */
            bleManager.setIconnSuccessfulListenner(new IconnSuccessfulListenner() {
                @Override
                public void successfullCallback() {
                    Log.d(TAG, "????????????????????????");
                    deviceList.remove(position);
                    scanAdpter.notifyDataSetChanged();
                    DecoratorBleManager decoratorBleManager = new DecoratorBleManager(bleManager);
                    ObjectPool.addBleManager(bleDevice.getAddress(),decoratorBleManager); //?????????????????????????????????
                    MainActivity activity = (MainActivity) getActivity();
                    activity.twtBinder.addBleManager(decoratorBleManager);
                    activity.setViewPager(1);
                    activity.ByValue(bleDevice.getDevice(),bleManager,connectRequest);
                }
            });
    }

    @Override
    public void DeviceTaget(BluetoothDevice bluetoothDevice) {
        MainActivity activity = (MainActivity) getActivity();
        activity.setViewPager(1);
    }
}
