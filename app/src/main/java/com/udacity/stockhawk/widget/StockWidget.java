package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.DetailActivity;

import static com.udacity.stockhawk.sync.QuoteSyncJob.ACTION_DATA_UPDATED;

/**
 * Implementation of App Widget functionality.
 */
public class StockWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.title_widget_stockhawk);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
        views.setTextViewText(R.id.widget_title, widgetText);

        views.setRemoteAdapter(R.id.widget_list,
                new Intent(context, StockWidgetRemoteViewsService.class));

        Intent startMainActivityIntent = new Intent(context, DetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startMainActivityIntent, 0);
        views.setPendingIntentTemplate(R.id.widget_list, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_DATA_UPDATED.equals(intent.getAction())) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.stock_widget);
            views.setRemoteAdapter(R.id.widget_list,
                    new Intent(context, StockWidgetRemoteViewsService.class));
        }
    }
}

