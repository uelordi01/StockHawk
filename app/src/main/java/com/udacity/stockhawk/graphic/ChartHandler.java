package com.udacity.stockhawk.graphic;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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


        String[] rows = parseString.split("\n");
        String []labels = new String[rows.length];
        for(int i = 0;i<rows.length;i++) {
            String []row = rows[i].split(",");
            values.add(new Entry(i,Float.parseFloat(row[1])));
            labels[i] = row[0];
        }
        LineDataSet set1;
        set1 = new LineDataSet(values,mChartName);
        set1.enableDashedLine(10f, 5f, 0f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setHighLightColor(Color.RED);
        set1.setValueTextColor(Color.BLUE);
        set1.setHighlightEnabled(true); // allow highlighting for DataSet
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.RED);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);
        setLabels(labels);


        // set this to false to disable the drawing of highlight indicator (lines)
        set1.setDrawHighlightIndicators(true);

        set1.setCircleColor(Color.BLUE);
        LineData ln =new LineData(set1);
        mChart.setData(ln);
    }
    public void showGraph(ViewGroup parentView) {
        parentView.addView(mChart);
        mChart.invalidate();
    }

    private static String getDateFromTimestamp(@NotNull long timestamp) {
       /* Timestamp dateTimestamp = new Timestamp(timestamp);
        Date date = new Date(dateTimestamp.getTime());*/
        //return date.toString();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        //return cal.get(Calendar.YEAR);
        return String.valueOf(cal.get(Calendar.YEAR));
    }
    private void setLabels(String [] labels) {
        final String []date_labels = labels;
        if(labels.length>0) {
            IAxisValueFormatter formatter = new IAxisValueFormatter() {

                @Override
                public String getFormattedValue(float value, AxisBase axis) {
                    return getDateFromTimestamp(Long.parseLong(date_labels[(int) value]));
                }
            };
            XAxis xAxis = mChart.getXAxis();
            xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
            xAxis.setValueFormatter(formatter);
        }
    }
}
