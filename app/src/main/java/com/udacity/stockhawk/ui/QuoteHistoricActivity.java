package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.graphic.ChartHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class QuoteHistoricActivity extends AppCompatActivity
                                implements LoaderManager.LoaderCallbacks<Cursor>{
    private String currentStockName = "";
    private static final int HISTORIC_LOADER = 2;
    TextView historicDebugResult;
    ChartHandler stockChart;
    FrameLayout mGraphRootLayout;
    String [] graphOptions;
    String [] graphOptionsLabels;
    private static final int NUMBER_OF_OPTIONS = 3;
    //private static int mSelectionCounter = 0;

//    LineChart mChart;
    String mgraphSelectedOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_historic);
        Intent parent_activity = getIntent();
        String stockNameKey = getString(R.string.pref_stocks_key);
        currentStockName = parent_activity.getStringExtra(stockNameKey);
        mGraphRootLayout = (FrameLayout)findViewById(R.id.historic_root_layout);

        startLoader();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.quotes_historic_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //setTitle("holakease");
            int index = getOptionIndex(PrefUtils.getCurrentQutoesChartOption(this));
            if(index < graphOptions.length-1) {
                 index++;
            } else {
                index = 0;
            }
                mgraphSelectedOption = graphOptions[index];
                item.setTitle(graphOptionsLabels[index]);
                PrefUtils.setCurrentQuotesChartOption(this,mgraphSelectedOption);
                startLoader();
            }
        return super.onOptionsItemSelected(item);

    }
    public int getOptionIndex(String option) {
       for(int i=0;i<graphOptions.length;i++) {
           if(option.equals(graphOptions[i])){
               return i;
           }
       }
       return 0;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // String    symbol = args.getString(getString(R.string.pref_stocks_key));
        //return null;
        return new CursorLoader(this,
                Contract.HistoricQuote.makeUriForQuotes(currentStockName),
                Contract.HistoricQuote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        HashMap<String,String> result = new HashMap<>();

        mgraphSelectedOption = PrefUtils.getCurrentQutoesChartOption(this);
        int selectionIndex = -1;
        if (data.getCount() != 0) {
            for(int i=0;i<data.getCount();i++) {
                data.moveToPosition(i);
                int symbolColumn = data.getColumnIndex(Contract.HistoricQuote.COLUMN_HISTORIC);
                int intervalType = data.getColumnIndex(Contract.HistoricQuote.COLUMN_QUOTE_INTERVAL);
                result.put(data.getString(intervalType),data.getString(symbolColumn));
            }
        }
        //selectionIndex=getOptionIndex(mgraphSelectedOption);
        if(selectionIndex < result.size()) {
            stockChart = new ChartHandler(getApplicationContext(),currentStockName);
            stockChart.setData(result.get(mgraphSelectedOption), mgraphSelectedOption);
            stockChart.showGraph(mGraphRootLayout);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    public void startLoader() {
        graphOptions  = getResources().getStringArray(R.array.pref_interval_option_values);
        graphOptionsLabels = getResources().getStringArray(R.array.pref_interval_option_labels);
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> historic_loader = loaderManager.getLoader(HISTORIC_LOADER);
        if ( historic_loader == null ) {
            getSupportLoaderManager().initLoader(HISTORIC_LOADER, null, this);
        } else {
            loaderManager.restartLoader(HISTORIC_LOADER, null, this);
        }
    }
}
