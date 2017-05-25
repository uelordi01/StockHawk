package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by uelordi on 17/05/2017.
 */

public class StockWidgetService extends RemoteViewsService {
    private final String LOG_TAG = StockWidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockWidgetRemoteAdapter(this, intent);
    }

}
