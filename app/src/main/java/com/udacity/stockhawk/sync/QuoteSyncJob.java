package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.IntDef;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.GlobalConfiguration;
import com.udacity.stockhawk.data.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
//import yahoofinance.Stock;
//import yahoofinance.YahooFinance;
//import yahoofinance.histquotes.HistoricalQuote;
//import yahoofinance.histquotes.Interval;
//import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.histquotes.Interval;


public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 1;
    // this is a debug flag remove to distribution

    //annotations of the resulting queries
    @Retention(RetentionPolicy.SOURCE)
    @IntDef( {
        ERROR_STATUS_OK,
        ERROR_STOCK_EMPTY,
        TOAST_ERROR_STOCK_NOT_EXIST,
        ERROR_NO_NETWORK,
        TOAST_ERROR_NO_CONECTIVITY,
        TOAST_STOCK_ADDED_NO_CONNECTIVITY
    })
    public  @interface locationErrorStatus{};

    public static final int ERROR_STATUS_OK = 0;
    public static final int ERROR_STOCK_EMPTY = 1;
    public static final int TOAST_ERROR_STOCK_NOT_EXIST = 2;
    public static final int ERROR_NO_NETWORK = 3;
    public static final int TOAST_ERROR_NO_CONECTIVITY = 4;
    public static final int TOAST_STOCK_ADDED_NO_CONNECTIVITY = 5;


    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");
        Interval [] theIntervals={Interval.DAILY, Interval.DAILY, Interval.MONTHLY,};//,Interval.DAILY};
        int [] calendar_from = {Calendar.WEEK_OF_MONTH,Calendar.MONTH,Calendar.YEAR, };//,Calendar.DAY_OF_WEEK};
        String [] graphOptionsValues = context.getResources().getStringArray(R.array.pref_graph_option_values);


        try {
            PrefUtils.setErrorStatus(context,ERROR_STATUS_OK);
            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();
            ArrayList<ContentValues> historicCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();


                if(isTheQuerySafe(quote)) {
                    float price = quote.getPrice().floatValue();
                    float change = quote.getChange().floatValue();
                    float percentChange = quote.getChangeInPercent().floatValue();
                    ContentValues quoteCV = new ContentValues();
                    quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                    quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                    quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                    quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                    quoteCVs.add(quoteCV);
                    ArrayList<ContentValues> historicSymbol;
                    if(GlobalConfiguration.ENABLE_HISTORIC) {
                        if (GlobalConfiguration.ENABLE_DUMMY_DATA) {
                            historicSymbol = getDummyHistoricData(context, stock);
                            historicCVs.addAll(historicSymbol);
                        } else {
                            historicSymbol = getHistoricFromStock(stock,
                                    graphOptionsValues,
                                    theIntervals,
                                    calendar_from);
                            historicCVs.addAll(historicSymbol);
                        }
                    }
                } else {
                    //The stock name does not exists:
                    PrefUtils.setErrorStatus(context,TOAST_ERROR_STOCK_NOT_EXIST);
                    PrefUtils.removeStock(context,quote.getSymbol());
                    // Toast.makeText(context,"the requested qquote edoes not exist",Toast.LENGTH_LONG);
                }
            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));
            if(GlobalConfiguration.ENABLE_HISTORIC) {
                context.getContentResolver()
                        .bulkInsert(
                                Contract.HistoricQuote.URI,
                                historicCVs.toArray(new ContentValues[historicCVs.size()]));
            }
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            PrefUtils.setErrorStatus(context,ERROR_NO_NETWORK);
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

         schedulePeriodic(context);
        syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {

           JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            scheduler.schedule(builder.build());
        }
    }
    private static boolean isTheQuerySafe(StockQuote quote) {
        if(quote.getPrice() != null &&
                quote.getChange() != null &&
                quote.getChangeInPercent()!= null ) {
            return true;
        }
        return false;
    }
    private static ArrayList<ContentValues> getHistoricFromStock(
                                                                 Stock stock,
                                                                 String [] graphOptionsValues,
                                                                 Interval[] theIntervals,
                                                                 int [] calendar_from) throws IOException {

        ArrayList<ContentValues> historicCVs = new ArrayList<>();
        for (int j = 0; j < theIntervals.length; j++) {
            ContentValues historicValue = new ContentValues();
            Calendar from = Calendar.getInstance();
            Calendar to = Calendar.getInstance();
            from.add(calendar_from[j], -1);

            List<HistoricalQuote> history = stock.getHistory(from, to, theIntervals[j]);
            StringBuilder historyBuilder = new StringBuilder();

            for (HistoricalQuote it : history) {
                historyBuilder.append(it.getDate().getTimeInMillis());
                historyBuilder.append(", ");
                historyBuilder.append(it.getClose());
                historyBuilder.append("\n");
            }
            historicValue.put(Contract.HistoricQuote.COLUMN_QUOTE_SYMBOL,
                    stock.getSymbol());
            historicValue.put(Contract.HistoricQuote.COLUMN_QUOTE_VIS_OPTION,
                    graphOptionsValues[j]);
            historicValue.put(Contract.HistoricQuote.COLUMN_HISTORIC,
                    historyBuilder.toString());
            historicCVs.add(historicValue);
        }
        return historicCVs;
    }
    private static ArrayList<ContentValues> getDummyHistoricData(
              Context context,
               Stock stock
                ) throws IOException {

        List<HistoricalQuote> history = new ArrayList<>();
        ArrayList<ContentValues> historicCVs = new ArrayList<>();
        InputStream is = context.getResources().openRawResource(R.raw.dummy_data);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        String jsonString = writer.toString();
        try {
            JSONObject json = new JSONObject(jsonString);;
            JSONObject query = json.getJSONObject("query");
            JSONObject results = query.getJSONObject("results");
            JSONArray quoteArray = results.getJSONArray("quote");
            for (int i = 0; i < quoteArray.length(); i++) {
                JSONObject quoteObject = quoteArray.getJSONObject(i);

                //Get calendar
                String dateString = quoteObject.getString("Date");
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                cal.setTime(sdf.parse(dateString));

                //get closign price
                BigDecimal closingPrice = new BigDecimal(quoteObject.getString("Close"));
                HistoricalQuote historicalQuote = new HistoricalQuote();
                historicalQuote.setDate(cal);
                historicalQuote.setClose(closingPrice);
                history.add(historicalQuote);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ContentValues historicValue = new ContentValues();
        StringBuilder historyBuilder = new StringBuilder();

        for (HistoricalQuote it : history) {
            historyBuilder.append(it.getDate().getTimeInMillis());//it.getCalendar().getTimeInMillis());
            historyBuilder.append(", ");
            historyBuilder.append(it.getClose());
            historyBuilder.append("\n");
        }
        historicValue.put(Contract.HistoricQuote.COLUMN_QUOTE_SYMBOL,
                stock.getSymbol());
        historicValue.put(Contract.HistoricQuote.COLUMN_QUOTE_VIS_OPTION,
                context.getString(R.string.preference_graph_none_value));
        historicValue.put(Contract.HistoricQuote.COLUMN_HISTORIC,
                historyBuilder.toString());
        historicCVs.add(historicValue);
        return historicCVs;
        //return history;
    }




}
