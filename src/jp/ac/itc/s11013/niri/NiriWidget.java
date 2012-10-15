
package jp.ac.itc.s11013.niri;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.RemoteViews;

public class NiriWidget extends AppWidgetProvider {

    public static final String VIEW_CLICK_ACTION = "VIEW_CLICK_ACTION";
    private RemoteViews remote_views;
    private PendingIntent pending_intent;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences pref = context.getSharedPreferences(Niri.PREFS_NAME
                    + appWidgetId, android.content.Context.MODE_PRIVATE);
            Editor e = pref.edit();
            e.clear().commit();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId : appWidgetIds) {

            remote_views = new RemoteViews(context.getPackageName(),
                    R.layout.widget_layout);

            Intent intent = new Intent();
            intent.setAction(NiriWidget.VIEW_CLICK_ACTION);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            pending_intent = PendingIntent.getService(
                    context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remote_views.setOnClickPendingIntent(R.id.widget_imageView, pending_intent);
            //          サービスの起動
            context.startService(intent);

            
            // AppWidgetの画面更新
            appWidgetManager.updateAppWidget(appWidgetId, remote_views);

        }
    }

}
