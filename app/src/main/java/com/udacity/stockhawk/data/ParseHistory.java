package com.udacity.stockhawk.data;

import com.github.mikephil.charting.data.Entry;
import com.google.common.base.Splitter;

import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class ParseHistory {
    /***
     * Parses the given history string into proper numeric <key, value> pairs.
     */
    public static List<Entry> parseHistory(final String history) {
        List<Entry> lst = new ArrayList<>();
        try {
            int i = 0;
            for (String e : Splitter.on("\n").split(history)) {
                String[] stock = e.split(",");
//                float time = Long.parseLong(stock[0]) / 1000000.0f;
                float price = Float.parseFloat(stock[1]);
                lst.add(new Entry(i++, price));
            }
        } catch (Exception e) {
            Timber.d(e.getMessage());
        }
        return lst;
    }
}
