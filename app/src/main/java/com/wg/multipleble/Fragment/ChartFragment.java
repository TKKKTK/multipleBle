package com.wg.multipleble.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.wg.multipleble.Data.UiEchartsData;
import com.wg.multipleble.R;
import com.wg.multipleble.Util.LineChartUtil;

public class ChartFragment extends Fragment {

    private LineChart lineChart;
    private LineChartUtil lineChartUtil;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chart_fragment,container,false);
        initView(view);
        return view;
    }

    private void initView(View view){
         lineChart = view.findViewById(R.id.chart_data);
         lineChartUtil = new LineChartUtil(lineChart);
    }

    public void dataUpdate(UiEchartsData uiEchartsData){
         //lineChartUtil.UpdateData(uiEchartsData);
    }


}
