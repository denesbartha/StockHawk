package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.ParseHistory;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {
    @BindView(R.id.detailed_symbol)
    TextView detailedSymbol;

    @BindView(R.id.detailed_price)
    TextView detailedPrice;

    @BindView(R.id.detailed_price_change)
    TextView detailedPriceChange;

    @BindView(R.id.detailed_percentage_change)
    TextView detailedPercentageChange;

    @BindView(R.id.detailed_history)
    LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Intent parentIntent = getIntent();
        if (parentIntent != null && parentIntent.hasExtra(Contract.Quote.COLUMN_SYMBOL)
                && parentIntent.hasExtra(Contract.Quote.COLUMN_PRICE)
                && parentIntent.hasExtra(Contract.Quote.COLUMN_ABSOLUTE_CHANGE)
                && parentIntent.hasExtra(Contract.Quote.COLUMN_PERCENTAGE_CHANGE)
                && parentIntent.hasExtra(Contract.Quote.COLUMN_HISTORY)) {

            final String symbol = parentIntent.getStringExtra(Contract.Quote.COLUMN_SYMBOL);
            final String price = parentIntent.getStringExtra(Contract.Quote.COLUMN_PRICE);
            final String absoluteChange =
                    parentIntent.getStringExtra(Contract.Quote.COLUMN_ABSOLUTE_CHANGE) + "$";
            final String percentageChange = parentIntent.getStringExtra(
                    Contract.Quote.COLUMN_PERCENTAGE_CHANGE) + "%";
            final String history = parentIntent.getStringExtra(Contract.Quote.COLUMN_HISTORY);

            if (Float.parseFloat(parentIntent.getStringExtra(Contract.Quote.COLUMN_ABSOLUTE_CHANGE))
                    > 0) {
                detailedPriceChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                detailedPercentageChange.setBackgroundResource(
                        R.drawable.percent_change_pill_green);
            } else {
                detailedPriceChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                detailedPercentageChange.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            detailedSymbol.setText(symbol);
            detailedPrice.setText(price);
            detailedPriceChange.setText(absoluteChange);
            detailedPercentageChange.setText(percentageChange);

            LineDataSet dataSet = new LineDataSet(ParseHistory.parseHistory(history),
                    "Change in $");
            dataSet.setDrawFilled(true);
            lineChart.setData(new LineData(dataSet));
            lineChart.invalidate();
            lineChart.setContentDescription(getString(R.string.linear_chart));
        }
    }
}
