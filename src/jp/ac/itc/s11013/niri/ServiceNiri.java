
package jp.ac.itc.s11013.niri;

import java.util.Calendar;	

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class ServiceNiri extends Service {

    private SoundNiri niri_sound;
    private NiriTwitter twitter;
    private int post_per_num;
    private boolean flag;
    private CharSequence[] list_items;
    private int sound_id;
    private int count;
    private Calendar cal;
    private int year, month, day;

    @Override
    public void onCreate() {
        super.onCreate();
        niri_sound = new SoundNiri(ServiceNiri.this);
        niri_sound.load();
        twitter = new NiriTwitter(this);
        list_items = getResources().getStringArray(R.array.widget_audio_list);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        int appWidgetId =
                intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

        SharedPreferences pref = getSharedPreferences(
                Niri.PREFS_NAME + appWidgetId, android.content.Context.MODE_PRIVATE);
        cal = Calendar.getInstance();
        year = pref.getInt(Niri.YEAR, -1);
        month = pref.getInt(Niri.MONTH, -1);
        day = pref.getInt(Niri.DAY, -1);

        if (year == -1 && month == -1 && day == -1) {
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            savedate(pref, year, month, day);
        }

        if (!(year == cal.get(Calendar.YEAR) &&
                month == cal.get(Calendar.MONTH) && day == cal.get(Calendar.DAY_OF_MONTH))) {
            count = 0;
            pref.edit().putInt(Niri.COUNTER, count).commit();
            savedate(pref, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
        }

        flag = pref.getBoolean(WidgetConfigure.POST_SWITCH, false);
        post_per_num = pref.getInt(WidgetConfigure.POST_PER_NUMBER, 10);
        sound_id = pref.getInt(Niri.SOUND_ID, -1);
        count = pref.getInt(Niri.COUNTER, 0);

        if (!(sound_id == -1)) {
            // widgetが押された時の設定
            if (NiriWidget.VIEW_CLICK_ACTION.equals(intent.getAction())) {
                // soundがloadされているか
                niri_sound.play(sound_id);
                count++;
                if (flag && count % post_per_num == 0) {
                    twitter.tweet(count, list_items[sound_id].toString());
                }
            }
            pref.edit().putInt(Niri.COUNTER, count).commit();
        }

    }

    @Override
    public void onDestroy() {
        niri_sound.unload();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void savedate(SharedPreferences pref, int y, int m, int d) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Niri.YEAR, y);
        editor.putInt(Niri.MONTH, m);
        editor.putInt(Niri.DAY, d);
        editor.commit();
    }
}
