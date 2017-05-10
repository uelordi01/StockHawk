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
import java.util.List;


public class QuoteHistoricActivity extends AppCompatActivity
                                implements LoaderManager.LoaderCallbacks<Cursor>{
    private String currentStockName = "";
    private static final int HISTORIC_LOADER = 2;
    TextView historicDebugResult;
    ChartHandler stockChart;
    FrameLayout mGraphRootLayout;
    String [] graphOptions;
    private static final int NUMBER_OF_OPTIONS = 3;
    //private static int mSelectionCounter = 0;

//    LineChart mChart;
    String mgraphSelectedOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_historic);
//        InitInterface();
        Intent parent_activity = getIntent();
        String stockNameKey = getString(R.string.pref_stocks_key);
        currentStockName = parent_activity.getStringExtra(stockNameKey);

        historicDebugResult = (TextView) findViewById(R.id.historic_result);
        mGraphRootLayout = (FrameLayout)findViewById(R.id.historic_root_layout);

        startLoader();
    }
//    private void InitInterface() {
//        Intent parent_activity = getIntent();
//
//        historicDebugResult = (TextView) findViewById(R.id.historic_result);
//        Thread background = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Stock tesla = YahooFinance.get("TSLA", true);
//                    Timber.d(tesla.getHistory().toString());
//                } catch(IOException e)
//                {
//                    Timber.e(e.getMessage());
//                }
//            }
//            // parent_activity.
//        });

       // background.start();
//    }

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
            if(index < 2) {
                 index++;
            } else {
                index = 0;
            }
                mgraphSelectedOption = graphOptions[index];
                item.setTitle(mgraphSelectedOption);
                PrefUtils.setCurrentQuotesChartOption(this,mgraphSelectedOption);
                Bundle b = new Bundle();
                b.putString(getString(R.string.preference_interval_key),currentStockName);
                startLoader();
            }
            //if(NUMBER_OF_OPTIONS )
            //setCurrentQuotesChartOption()
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
        List<String> result= new ArrayList<>();
        mgraphSelectedOption = PrefUtils.getCurrentQutoesChartOption(this);
        int selectionIndex = -1;
        if (data.getCount() != 0) {
           // error.setVisibility(View.GONE)
            for(int i=0;i<data.getCount();i++) {
                data.moveToPosition(i);
                int symbolColumn = data.getColumnIndex(Contract.HistoricQuote.COLUMN_HISTORIC);
                result.add(data.getString(symbolColumn));
                //historicDebugResult.setText(restult);
            }

        }
        selectionIndex=getOptionIndex(mgraphSelectedOption);
        stockChart = new ChartHandler(getApplicationContext(),currentStockName);
        stockChart.setData(result.get(selectionIndex));
        stockChart.showGraph(mGraphRootLayout);
       /*  int mFillColor = Color.argb(150, 51, 181, 229);
        mChart = (LineChart)findViewById(R.id.quote_chart);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setGridBackgroundColor(mFillColor);
        mChart.setDrawGridBackground(true);

        mChart.setDrawBorders(true);

        // no description text
        mChart.getDescription().setEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);



        // add data
        setData(100, 60);

        mChart.invalidate();*/

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
//    private void setData(int count, float range) {
//
//        ArrayList<Entry> values = new ArrayList<Entry>();
//
//        for (int i = 0; i < count; i++) {
//
//            float val = (float) (Math.random() * range) + 3;
//            values.add(new Entry(i, val, getResources().getDrawable(R.drawable.star)));
//        }
//
//        LineDataSet set1;
//
//        if (mChart.getData() != null &&
//                mChart.getData().getDataSetCount() > 0) {
//            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
//            set1.setValues(values);
//            mChart.getData().notifyDataChanged();
//            mChart.notifyDataSetChanged();
//        } else {
//            // create a dataset and give it a type
//            set1 = new LineDataSet(values, "DataSet 1");
//
//            //set1.setDrawIcons(false);
//
//            // set the line to be drawn like this "- - - - - -"
//            set1.enableDashedLine(10f, 5f, 0f);
//            set1.enableDashedHighlightLine(10f, 5f, 0f);
//            set1.setColor(Color.BLACK);
//            set1.setCircleColor(Color.BLACK);
//            set1.setLineWidth(1f);
//            set1.setCircleRadius(3f);
//            set1.setDrawCircleHole(false);
//            set1.setValueTextSize(9f);
//            set1.setDrawFilled(true);
//            set1.setFormLineWidth(1f);
//            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
//            set1.setFormSize(15.f);
//
//            if (Utils.getSDKInt() >= 18) {
//                // fill drawable only supported on api level 18 and above
//               // Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
//                //set1.setFillDrawable(drawable);
//            }
//            else {
//                set1.setFillColor(Color.BLACK);
//            }
//
//            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
//            dataSets.add(set1); // add the datasets
//
//            // create a data object with the datasets
//            LineData data = new LineData(dataSets);
//
//            // set data
//            mChart.setData(data);
//        }
//    }
public void startLoader()
{

    Bundle contentBundle = new Bundle();
    graphOptions=getResources().getStringArray(R.array.pref_interval_option_values);
    //String stockNameKey;
    //contentBundle.putString(stockNameKey,currentStockName);
    LoaderManager loaderManager = getSupportLoaderManager();
    Loader<String> historic_loader = loaderManager.getLoader(HISTORIC_LOADER);
    if ( historic_loader == null ) {
        getSupportLoaderManager().initLoader(HISTORIC_LOADER, null, this);
    } else {
        loaderManager.restartLoader(HISTORIC_LOADER, null, this);
    }
}
}
