package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

/**
 * Created by uelordi on 17/05/2017.
 */

public class StockWidgetService extends RemoteViewsService {
    private final String LOG_TAG = StockWidgetService.class.getSimpleName();
//    /private static final String[]
  // TODO IF THERE IS IMAGE IN YOUR WIDGET YOU SHOULD ADD THE CONTENT DESCRIPTION
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }
    class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        Context context;
        int appWidgetId;

        private Cursor data = null;
        public StockRemoteViewsFactory(Context context, Intent intent) {
            // TODO Auto-generated constructor stub
            this.context = context;
            this.appWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            // initialize objects
        }
        @Override
        public void onCreate() {
            // Nothing to do
        }

        @Override
        public void onDataSetChanged() {
            if (data != null) {
                data.close();
            }
            final long identityToken = Binder.clearCallingIdentity();
            data = getContentResolver().query(Contract.Quote.URI,
                                             Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                                             null,
                                             null,
                                             Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION ||
                    data == null || !data.moveToPosition(position)) {
                return null;
            }
            RemoteViews views = new RemoteViews(getPackageName(),
                    R.layout.widget_detail_item);
            int symbolColumn = data.getColumnIndex(Contract.HistoricQuote.COLUMN_HISTORIC);
            int priceColumn = data.getColumnIndex(Contract.Quote.COLUMN_PRICE);
            views.setTextViewText(R.id.tv_widget_stock_name,data.getString(symbolColumn));
            views.setTextViewText(R.id.tv_widget_stock_price,data.getString(priceColumn));
            final Intent fillInIntent = new Intent();
            fillInIntent.setData(Contract.Quote.URI);
            views.setOnClickFillInIntent(R.id.widget_stock_list, fillInIntent);
            return views;

        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(),
                        R.layout.widget_detail_item);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (data.moveToPosition(position))
                //todo there will be a problem if the value is integer.
                return data.getLong(0);
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}



