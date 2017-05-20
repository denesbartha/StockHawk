package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.mock.MockUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    private static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 1;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {

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

            while (iterator.hasNext()) {
                String symbol = iterator.next();


                Stock stock = quotes.get(symbol);
                // if the specified stock does not exists => delete it from the database
                if (null == stock.getName())
                {
                    PrefUtils.removeStock(context, symbol);
                    continue;
                }
                StockQuote quote = stock.getQuote();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();

                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                List<HistoricalQuote> history = null;
                try {
                    history = stock.getHistory(from, to, Interval.WEEKLY);
                } catch (FileNotFoundException exception) {
//                    continue;
                    // Note for reviewer:
                    // Due to the problems with Yahoo API we have commented the line above
                    // and included this one to fetch the history from MockUtils
                    // This should be enough as to develop and review while the API is down
                    history = MockUtils.getHistory();
                }

                StringBuilder historyBuilder = new StringBuilder();

                for (HistoricalQuote it : history) {
                    historyBuilder.append(it.getDate().getTimeInMillis());
                    historyBuilder.append(", ");
                    historyBuilder.append(it.getClose());
                    historyBuilder.append("\n");
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                quoteCVs.add(quoteCV);

            }

//            // DEBUG
//            ContentValues quoteCV = new ContentValues();
//            quoteCV.put(Contract.Quote.COLUMN_SYMBOL, "MSFT");
//            quoteCV.put(Contract.Quote.COLUMN_PRICE, 105.34);
//            quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, -303.3);
//            quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, -2034);
//            quoteCV.put(Contract.Quote.COLUMN_HISTORY, "1432209876584, 1000\n 1432814676584, 1001\n 1433419476584, 1000\n 1434024276584, 999\n 1434629076584, 999\n 1435233876584, 998\n 1435838676584, 999\n 1436443476584, 998\n 1437048276584, 999\n 1437653076584, 998\n 1438257876584, 997\n 1438862676584, 996\n 1439467476584, 996\n 1440072276584, 995\n 1440677076584, 995\n 1441281876584, 996\n 1441886676584, 996\n 1442491476584, 995\n 1443096276584, 996\n 1443701076584, 996\n 1444305876584, 995\n 1444910676584, 995\n 1445515476584, 996\n 1446120276584, 995\n 1446725076584, 995\n 1447329876584, 994\n 1447934676584, 993\n 1448539476584, 994\n 1449144276584, 995\n 1449749076584, 995\n 1450353876584, 996\n 1450958676584, 995\n 1451563476584, 994\n 1452168276584, 994\n 1452773076584, 995\n 1453377876584, 994\n 1453982676584, 994\n 1454587476584, 993\n 1455192276584, 992\n 1455797076584, 992\n 1456401876584, 992\n 1457006676584, 992\n 1457611476584, 991\n 1458216276584, 990\n 1458821076584, 989\n 1459425876584, 988\n 1460030676584, 988\n 1460635476584, 987\n 1461240276584, 988\n 1461845076584, 988\n 1462449876584, 987\n 1463054676584, 986\n 1463659476584, 985\n 1464264276584, 986\n 1464869076584, 985\n 1465473876584, 986\n 1466078676584, 985\n 1466683476584, 985\n 1467288276584, 984\n 1467893076584, 984\n 1468497876584, 985\n 1469102676584, 985\n 1469707476584, 984\n 1470312276584, 984\n 1470917076584, 984\n 1471521876584, 983\n 1472126676584, 982\n 1472731476584, 983\n 1473336276584, 983\n 1473941076584, 982\n 1474545876584, 981\n 1475150676584, 981\n 1475755476584, 980\n 1476360276584, 980\n 1476965076584, 979\n 1477569876584, 978\n 1478174676584, 978\n 1478779476584, 978\n 1479384276584, 977\n 1479989076584, 976\n 1480593876584, 975\n 1481198676584, 974\n 1481803476584, 974\n 1482408276584, 975\n 1483013076584, 976\n 1483617876584, 977\n 1484222676584, 978\n 1484827476584, 978\n 1485432276584, 977\n 1486037076584, 978\n 1486641876584, 978\n 1487246676584, 978\n 1487851476584, 978\n 1488456276584, 977\n 1489061076584, 978\n 1489665876584, 979\n 1490270676584, 978\n 1490875476584, 977\n 1491480276584, 977\n 1492085076584, 976\n 1492689876584, 976\n 1493294676584, 975\n 1493899476584, 975\n 1494504276584, 974\n 1495109076584, 975\n");
//            quoteCVs.add(quoteCV);

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

            updateWidgets(context);

        } catch (IOException exception) {
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

    private static void updateWidgets(Context context) {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }


}
