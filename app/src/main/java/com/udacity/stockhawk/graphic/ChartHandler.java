package com.udacity.stockhawk.graphic;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by uelordi on 08/05/2017.
 */

public class ChartHandler {
    private static ChartHandler instance = null;

//    public ChartHandler getInstance() {
//        if(instance == null) {
//            instance = new ChartHandler();
//        }
//        return instance;
//    }
    LineChart mChart;
    String mChartName;
    public ChartHandler(Context context, String chartName) {
        mChart = new LineChart(context);
        mChartName = chartName;
        configureChartLayout();
    }
    public void setData(String parseString) {
        addData(parseString);
    }
    private void configureChartLayout(){
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                                            ViewGroup.LayoutParams.MATCH_PARENT);
        mChart.setLayoutParams(params);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
        // set the line to be drawn like this "- - - - - -"

    }
    private   void addData(String parseString) {
        ArrayList<Entry> values = new ArrayList<Entry>();
        ArrayList<String> labels = new ArrayList<String>();

        String[] rows = parseString.split("\n");
        for(int i = 0;i<rows.length;i++) {
            String []row = rows[i].split(",");
            values.add(new Entry(i,Float.parseFloat(row[1])));
        }
        LineDataSet set1;
        set1 = new LineDataSet(values,mChartName);
        set1.enableDashedLine(10f, 5f, 0f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setHighLightColor(Color.RED);
        set1.setValueTextColor(Color.BLUE);


        set1.setCircleColor(Color.BLUE);
        LineData ln =new LineData(set1);
        mChart.setData(ln);
    }
    public void showGraph(ViewGroup parentView) {
        parentView.addView(mChart);
        mChart.invalidate();
    }

    private static String getDateFromTimestamp(@NotNull long timestamp) {
        Timestamp dateTimestamp = new Timestamp(timestamp);
        Date date = new Date(dateTimestamp.getTime());
        return date.toString();
    }
}
