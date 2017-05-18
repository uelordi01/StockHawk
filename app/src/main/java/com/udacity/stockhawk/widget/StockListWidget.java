package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by uelordi on 17/05/2017.
 */

public class StockListWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetID : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

            Intent returnIntent = new Intent(context, MainActivity.class);
            //TODO CALL TO THE REMOTEVIEW_SERVICE

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, returnIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_stock_list, pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                setRemoteAdapter(context, views);
            } else {
                setRemoteAdapterV11(context, views);
            }
            appWidgetManager.updateAppWidget(appWidgetID, views);
        }

//        Intent intent = new Intent(context,WidgetUpdateIntentService.class);
//        intent.putExtra(AppWidgetManager.ACTION_APPWIDGET_UPDATE,appWidgetIds);
//
//
////        context.startService(intent);
//        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        if(intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {
//            RemoteViews views = new RemoteViews(context.getPackageName(),
//                    R.layout.widget_layout);
//            setRemoteAdapter(context,views);
        String action =  intent.getAction();
        if(intent.getAction().equals(QuoteSyncJob.ACTION_DATA_UPDATED)) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, getClass()));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_stock_list);
        }
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(R.id.widget_stock_list,
                new Intent(context, StockWidgetService.class));
    }
//
//    /**
//     * Sets the remote adapter used to fill in the list items
//     *
//     * @param views RemoteViews to set the RemoteAdapter
//     */
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
        views.setRemoteAdapter(0, R.id.widget_stock_list,
                new Intent(context, StockWidgetService.class));
    }
}
