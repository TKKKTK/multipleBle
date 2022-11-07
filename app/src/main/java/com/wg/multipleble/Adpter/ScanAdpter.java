package com.wg.multipleble.Adpter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wg.multipleble.Data.BleDevice;
import com.wg.multipleble.Interface.IconnectListenner;
import com.wg.multipleble.Manager.BleManager;
import com.wg.multipleble.R;
import com.wg.multipleble.Util.UUIDList;

import java.util.List;

public class ScanAdpter extends RecyclerView.Adapter<ScanAdpter.ViewHolder> {

    private List<BleDevice> deviceList;
    private IconnectListenner iconnectListenner;
    public ScanAdpter(List<BleDevice> deviceList) {
        this.deviceList = deviceList;
    }

    public void setIconnectListenner(IconnectListenner listenner){
        this.iconnectListenner = listenner;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list,parent,false);
        ScanAdpter.ViewHolder holder = new ScanAdpter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        BleDevice bleDevice = deviceList.get(position);
        holder.device_name.setText(bleDevice.getName());
        holder.device_address.setText(bleDevice.getAddress());
        holder.device_rssi.setText(bleDevice.getRssi()+"dbm");
        holder.connect_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iconnectListenner.DeviceConnect(bleDevice,position);

            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView device_name;
        TextView device_address;
        TextView device_rssi;
        Button connect_device;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            device_name = (TextView) itemView.findViewById(R.id.device_name);
            device_address = (TextView) itemView.findViewById(R.id.device_address);
            device_rssi = (TextView) itemView.findViewById(R.id.device_rssi);
            connect_device = (Button) itemView.findViewById(R.id.connect_device);
        }
    }
}
