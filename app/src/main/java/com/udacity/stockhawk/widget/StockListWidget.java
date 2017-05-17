package com.udacity.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.udacity.stockhawk.R;

/**
 * Created by uelordi on 17/05/2017.
 */

public class StockListWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Intent intent = new Intent(context,WidgetUpdateIntentService.class);
        intent.putExtra(context.getString(R.string.widget_id_array_key),appWidgetIds);
        context.startService(intent);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("om.udacity.stockhawk.ACTION_DATA_UPDATED")) {
            context.startService(new Intent(context,WidgetUpdateIntentService.class));
        }
    }
}
