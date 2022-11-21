package com.wg.multipleble.Util;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.wg.multipleble.Data.EchartsData;
import com.wg.multipleble.Data.UiEchartsData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 肌电信号波形图
 */
public class LineChartUtil {
    private LineChart lineChart; //图表
    private List<Entry> dataList = new ArrayList<>(); //存放X轴，Y轴数据
    private List<String> XLabel = new ArrayList<>(); //存放X轴标签
    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<List<Entry>> dataLists = new ArrayList<>();
    private List<ILineDataSet> lineDataSetList = new ArrayList<>();

    public LineChartUtil(LineChart lineChart) {
        this.lineChart = lineChart;
        Setting();
        SetXAxis();
        SetYAxis();
        initLineDataSet("方波图", Color.BLUE);
    }

    /**
     * 图表基础设置
     */
    private void Setting(){
        lineChart.setDoubleTapToZoomEnabled(false);
        // 不显示数据描述
        lineChart.getDescription().setEnabled(false);
        // 没有数据的时候，显示“暂无数据”
        lineChart.setNoDataText("暂无数据");
        //禁止x轴y轴同时进行缩放
        lineChart.setPinchZoom(false);
        //启用/禁用缩放图表上的两个轴。
        lineChart.setScaleEnabled(false);
        //设置为false以禁止通过在其上双击缩放图表。
        lineChart.setDrawGridBackground(false);
        //显示边界
        lineChart.setDrawBorders(true);
        lineChart.setBorderColor(Color.parseColor("#d5d5d5"));
        lineChart.getAxisRight().setEnabled(false);//关闭右侧Y轴
        lineChart.setTouchEnabled(false);


        //折线图例 标签 设置 这里不显示图例
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
    }

    private void SetXAxis(){
        //绘制X轴
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisMaximum(1000);
        xAxis.setAxisMinimum(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.parseColor("#d8d8d8"));
        //设置最后和第一个标签不超出x轴
        //xAxis.setAvoidFirstLastClipping(true);
//        设置线的宽度
        xAxis.setAxisLineWidth(1.0f);
        xAxis.setAxisLineColor(Color.parseColor("#d5d5d5"));
        xAxis.setLabelCount(5,true);
        xAxis.setDrawLabels(true);

//        xAxis.setValueFormatter(new ValueFormatter() {
//            private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SS", Locale.ENGLISH);
//            @Override
//            public String getFormattedValue(float value) {
//                long millis = TimeUnit.HOURS.toMillis((long) value);
//                return dateFormat.format(new Date(millis));
//            }
//        });
    }

    private void SetYAxis(){
        //绘制Y轴
        YAxis yAxis = lineChart.getAxisLeft();
//        yAxis.setAxisMaximum(30000);
//        yAxis.setAxisMinimum(-30000);
    }

    /**
     * 初始化一条折线
     */
    private void initLineDataSet(String name, int color) {
        //初始化存放八条线的容器
        for (int i = 0; i<9; i++){
            List<Entry> entryList = new ArrayList<>(); //每条线一个容器
            dataLists.add(entryList);
        }

        for (int i = 0;i<1000;i++){ //给每一条线添加1000个数据
            for (int j = 0; j < 9; j++){
                Entry entry = new Entry(i,0-j*2500000);
                List<Entry> entrys = dataLists.get(j);
                entrys.add(entry);
                dataLists.set(j,entrys);
            }
            XLabel.add(new SimpleDateFormat("HH:mm:ss:SS").format(new Date().getTime()));
        }

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(XLabel));

        for (int i = 0;i < 9; i++){
            lineDataSet = new LineDataSet(dataLists.get(i), name);
            lineDataSet.setLineWidth(1.5f);
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawValues(false);
            lineDataSet.setColor(Color.BLUE);
            lineDataSetList.add(lineDataSet);
        }
        //添加一个空的 LineData
        lineData = new LineData(lineDataSetList);
        lineChart.setData(lineData);

        lineChart.invalidate();

    }

    /**
     * 数据更新
     * @param uiEchartsData
     */
    public void UpdateData(List<UiEchartsData> uiEchartsData){
        List<UiEchartsData> datas = uiEchartsData; //获取一个包的数据,一个包五个点

        /**
         * 移除x轴、Y轴前面的数据
         */
        for (int i = 0; i<datas.get(0).getListPacket().size();i++){  //将八条线的前面一个包给它去除
            for (int j = 0;j < 9;j++){
                List<Entry> entries = dataLists.get(j);
                entries.remove(0);
                dataLists.set(j,entries);
            }
            XLabel.remove(0);
        }
        /**
         * 将数据往前移,相当于重新设置每一个点的x坐标
         */
        for (int i = 0;i < dataLists.size();i++){
            for (int j = 0; j <dataLists.get(i).size(); j++){
                Entry entry = dataLists.get(i).get(j);
                dataLists.get(i).set(j,new Entry(j,entry.getY()));
            }
        }
        /**
         * 往末尾添加数据
         */
        for (int i = 0; i<datas.get(0).getListPacket().size();i++){ //一共八个通道,每个通道五个点的数据
            for (int j = 0; j < 9; j++){
                EchartsData echartsData = datas.get(j).getListPacket().get(i);
                Entry entry = null;
                if (echartsData.isRecord()){
                    entry = new Entry(dataLists.get(j).size(),0);
                }else {
                    entry = new Entry(dataLists.get(j).size(),(echartsData.getDataPoint()-j*2500000));
                }
                List<Entry> entrys = dataLists.get(j);
                entrys.add(entry);
                dataLists.set(j,entrys);
            }
            //更新x轴标签的数据
            XLabel.add(datas.get(8).getListPacket().get(i).getTime());
        }

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(XLabel));

        for (int i =0;i<lineDataSetList.size();i++){
            LineDataSet lineDataSet1 = (LineDataSet) lineDataSetList.get(i);
            lineDataSet1.setValues(dataLists.get(i));
            lineDataSetList.set(i,lineDataSet1);
        }
        //Log.d(TAG, "UpdateData: "+lineData.getDataSetCount());
        lineData = new LineData(lineDataSetList);
        lineChart.setData(lineData);
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

}
