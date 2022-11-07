package com.wg.multipleble.Fragment;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.wg.multipleble.Adpter.DataFragmentAdapter;
import com.wg.multipleble.Adpter.DeviceConnectAdapter;
import com.wg.multipleble.Interface.IOnSelectDeviceItem;
import com.wg.multipleble.Manager.BleManager;
import com.wg.multipleble.Manager.DecoratorBleManager;
import com.wg.multipleble.R;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.ble.ConnectRequest;

public class DeviceFragment extends BaseFragment implements IOnSelectDeviceItem {
    private RecyclerView recyclerView;
    private DeviceConnectAdapter deviceConnectAdapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private ViewPager viewPager;
    private List<Fragment> dataFragments = new ArrayList<>();
    private DataFragmentAdapter dataFragmentAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_fragment,container,false);
        initView(view);
        setEvent();
        Log.d(getClass().getSimpleName(), "onCreateView: ");
        return view;
    }

    private void initView(View view){
        /**
         * 已经连接的设备列表
         */
        recyclerView = (RecyclerView) view.findViewById(R.id.device_inform_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        deviceConnectAdapter = new DeviceConnectAdapter(deviceList);
        deviceConnectAdapter.setiOnSelectDeviceItem(this);
        recyclerView.setAdapter(deviceConnectAdapter);

        /**
         * 数据接收的碎片列表
         */
         viewPager = (ViewPager) view.findViewById(R.id.data_vp);
         dataFragmentAdapter = new DataFragmentAdapter(getActivity().getSupportFragmentManager(),dataFragments);
         viewPager.setAdapter(dataFragmentAdapter);

    }

    public void  setEvent(){
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
              deviceConnectAdapter.setSelectItem(position);
              recyclerView.scrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    public void addDevice(BluetoothDevice device, BleManager bleManager, ConnectRequest connectRequest){
        DecoratorBleManager decoratorBleManager = new DecoratorBleManager(bleManager);
        DataFragment dataFragment = new DataFragment(decoratorBleManager,device);
        dataFragments.add(dataFragment);
        viewPager.setAdapter(dataFragmentAdapter);
        deviceList.add(device);
        deviceConnectAdapter.notifyItemRangeChanged(deviceList.size()-1,1);
        //将RecyclerView定位到最后一行
        recyclerView.scrollToPosition(deviceList.size()-1);
        deviceConnectAdapter.setSelectItem(deviceList.size()-1);
        viewPager.setCurrentItem(deviceList.size()-1);
    }

    /**
     * 连接列表中被选中的设备
     * @param position
     */
    @Override
    public void selectDeviceItem(int position) {
        viewPager.setCurrentItem(position);
    }

    @Override
    public void closeDeviceConnect(int position) {
        deviceList.remove(position);
        deviceConnectAdapter.notifyDataSetChanged();
        DataFragment dataFragment = (DataFragment) dataFragmentAdapter.getItem(position);
        //Log.d(TAG, "closeDeviceConnect: "+dataFragment.bluetoothDevice.getAddress());
        dataFragment.disconnect();
        dataFragment.onDestroy();
        dataFragment.onDetach();
        dataFragments.remove(position);
        dataFragmentAdapter.setFragmentList(dataFragments);
        dataFragmentAdapter.notifyDataSetChanged();
        viewPager.setAdapter(dataFragmentAdapter);
    }
}
