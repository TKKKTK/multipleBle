package com.wg.multipleble.Adpter;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wg.multipleble.Interface.IOnSelectDeviceItem;
import com.wg.multipleble.R;

import java.util.List;

public class DeviceConnectAdapter extends RecyclerView.Adapter<DeviceConnectAdapter.ViewHolder>{

    private List<BluetoothDevice> deviceList;
    private int mPosition = 0; //记录点击的位置
    private IOnSelectDeviceItem iOnSelectDeviceItem;

    public DeviceConnectAdapter(List<BluetoothDevice> deviceList){
          this.deviceList = deviceList;
    }

    public void setiOnSelectDeviceItem(IOnSelectDeviceItem iOnSelectDeviceItem) {
        this.iOnSelectDeviceItem = iOnSelectDeviceItem;
    }

    @NonNull
    @Override
    public DeviceConnectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_inform_item,parent,false);
        DeviceConnectAdapter.ViewHolder holder = new DeviceConnectAdapter.ViewHolder(view);
        return holder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull DeviceConnectAdapter.ViewHolder holder, int position) {
         int index = position;
          BluetoothDevice bluetoothDevice = deviceList.get(position);
          holder.device_name.setText(bluetoothDevice.getName());
          holder.device_address.setText(bluetoothDevice.getAddress());
          //默认设置选中第一个
          if (mPosition != position){
              holder.selectView.setVisibility(View.INVISIBLE);
          }else if (mPosition == position){
              holder.selectView.setVisibility(View.VISIBLE);
          }

          holder.itemView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                    mPosition = index;  //记录点击的位置
                    notifyDataSetChanged(); //更新数据
                    iOnSelectDeviceItem.selectDeviceItem(mPosition);
              }
          });
          holder.disconnect.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Log.d(TAG, "onClick: 点击断开连接!!");
                  iOnSelectDeviceItem.closeDeviceConnect(index);
              }
          });
    }

    /**
     * 更新选中的位置
     * @param position
     */
    public void setSelectItem(int position){
        this.mPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView device_name;
        TextView device_address;
        ImageView disconnect;
        View itemView;
        View selectView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            device_name = (TextView) itemView.findViewById(R.id.device_name);
            device_address = (TextView) itemView.findViewById(R.id.device_address);
            disconnect = (ImageView) itemView.findViewById(R.id.disconnect);
            selectView = (View) itemView.findViewById(R.id.selected);
            this.itemView = itemView;
        }
    }
}
