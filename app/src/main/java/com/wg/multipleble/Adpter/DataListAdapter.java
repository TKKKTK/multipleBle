package com.wg.multipleble.Adpter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wg.multipleble.Data.DataPacket;
import com.wg.multipleble.R;

import java.util.ArrayList;
import java.util.List;

public class DataListAdapter extends RecyclerView.Adapter<DataListAdapter.ViewHolder>{
   private List<DataPacket> dataPackets = new ArrayList<>();

    public DataListAdapter(List<DataPacket> dataPackets) {
        this.dataPackets = dataPackets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_data_item,parent,false);
        DataListAdapter.ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
         DataPacket dataPacket = dataPackets.get(position);
         holder.data_text.setText(dataPacket.getData().toString());
         holder.time_text.setText(dataPacket.getTime());
    }

    @Override
    public int getItemCount() {
        return dataPackets.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        TextView data_text;
        TextView time_text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            data_text = (TextView) itemView.findViewById(R.id.data_text);
            time_text = (TextView) itemView.findViewById(R.id.time_text);
        }
    }
}
