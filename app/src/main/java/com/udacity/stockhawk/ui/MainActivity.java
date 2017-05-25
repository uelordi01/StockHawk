package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.tv_error)
    TextView errorView;
    private StockAdapter adapter;
    private String mAddedSymbol="";
    private Parcelable mListState;
    @Override
    public void onClick(String symbol) {

        Timber.d("Symbol clicked: %s", symbol);
        Intent historicAct = new Intent(this, QuoteHistoricActivity.class);
        historicAct.putExtra(getString(R.string.pref_stocks_key),symbol);
        startActivity(historicAct);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListState = stockRecyclerView.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(getString(R.string.
                        stock_list_view_index),mListState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            mListState = savedInstanceState.
                    getParcelable(getString(R.string.stock_list_view_index));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        adapter = new StockAdapter(this, this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        if(savedInstanceState != null ) {

        }
        //onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(stockRecyclerView);


    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(this);
        if (!networkUp() && adapter.getItemCount() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            PrefUtils.setErrorStatus(this, QuoteSyncJob.ERROR_NO_NETWORK);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            PrefUtils.setErrorStatus(this, QuoteSyncJob.TOAST_ERROR_NO_CONECTIVITY);
        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            PrefUtils.setErrorStatus(this, QuoteSyncJob.ERROR_STOCK_EMPTY);
        } else {
            PrefUtils.setErrorStatus(this,QuoteSyncJob.ERROR_STATUS_OK);
        }
        updateView();
    }
    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {
            mAddedSymbol = symbol;
            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                PrefUtils.setErrorStatus(this,QuoteSyncJob.TOAST_ERROR_NO_CONECTIVITY);
            }
        }
        updateView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);

       /* if (data.getCount() != 0) {
            errorView.setVisibility(View.GONE);
        }*/

        adapter.setCursor(data);

        if (!networkUp() && adapter.getItemCount() == 0) {
            PrefUtils.setErrorStatus(this, QuoteSyncJob.ERROR_NO_NETWORK);
        } else if (!networkUp()) {
            PrefUtils.setErrorStatus(this, QuoteSyncJob.TOAST_ERROR_NO_CONECTIVITY);
        } else if (PrefUtils.getStocks(this).size() == 0) {
            PrefUtils.setErrorStatus(this, QuoteSyncJob.ERROR_STOCK_EMPTY);
        }
        updateView();
        stockRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void updateView(){
        String message = "";
        boolean showToast = false;
        @QuoteSyncJob.locationErrorStatus int errorType = PrefUtils.getErrorStatus(this);
        switch (errorType) {
            case QuoteSyncJob.ERROR_STATUS_OK: {
                message = getString(R.string.error_status_ok);
                break;
            }
            case QuoteSyncJob.ERROR_STOCK_EMPTY:{
                message = getString(R.string.error_no_stocks);
                break;
            }
            case QuoteSyncJob.TOAST_ERROR_STOCK_NOT_EXIST: {
                message = getString(R.string.toast_error_stock_not_found, mAddedSymbol);
                showToast = true;
                break;
            }
            case QuoteSyncJob.ERROR_NO_NETWORK: {
                message = getString(R.string.error_no_network);
                break;
            }
            case QuoteSyncJob.TOAST_ERROR_NO_CONECTIVITY:{
                message = getString(R.string.toast_no_connectivity);
                showToast = true;
                break;
            }
            case QuoteSyncJob.TOAST_STOCK_ADDED_NO_CONNECTIVITY:{
                message = getString(R.string.toast_stock_added_no_connectivity, mAddedSymbol);
                showToast = true;
                break;
            }
            default: { //here is not errorType.
                message = "";
                break;
            }
        }
        errorView.setText(message);
        if(errorType == QuoteSyncJob.ERROR_STATUS_OK
                || errorType == QuoteSyncJob.TOAST_ERROR_NO_CONECTIVITY
                || errorType == QuoteSyncJob.TOAST_ERROR_STOCK_NOT_EXIST
                || errorType == QuoteSyncJob.TOAST_STOCK_ADDED_NO_CONNECTIVITY) {
            errorView.setVisibility(View.GONE);
        } else {
            errorView.setVisibility(View.VISIBLE);
        }
        if (showToast) {
            Toast.makeText(this, message,Toast.LENGTH_LONG).show();
        }

    }
}
