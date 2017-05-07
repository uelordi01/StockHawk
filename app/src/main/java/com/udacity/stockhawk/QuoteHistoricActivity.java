package com.udacity.stockhawk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class QuoteHistoricActivity extends AppCompatActivity {
    private String currentStockName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote_historic);

    }
    private void InitInterface() {
        Intent parent_activity = getIntent();
        currentStockName=parent_activity.getStringExtra(getString(R.string.pref_stocks_key));
    }
}
