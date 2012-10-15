
package jp.ac.itc.s11013.niri;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Niri extends Activity {

    public static final String SOUND_ID = "sound_id";
    public static final String COUNTER = "count";
    public static final String DAY = "day", YEAR = "year", MONTH = "month";
    public static final String PREFS_NAME = "jp.ac.itc.s11013.niri";
    private CharSequence[] list_items;
    private SoundNiri niri_sound;
    private int count, play_id;
    private ImageView sanketa, futaketa, hitoketa;
    private Drawable sanketadraw, futaketadraw, hitoketadraw;
    private TypedArray images, yellow_images, red_images, green_images;
    private NiriTwitter twitter;
    private int day, year, month;
    private Calendar cal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.niri);
        twitter = new NiriTwitter(this);
        niri_sound = new SoundNiri(Niri.this);
        niri_sound.load();

        SharedPreferences set = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        cal = Calendar.getInstance();
        year = set.getInt(YEAR, -1);
        month = set.getInt(MONTH, -1);
        day = set.getInt(DAY, -1);
        
        if(year == -1 && month == -1 && day == -1){
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
            saveNumber(new String[]{YEAR, MONTH, DAY}, year, month, day);
        }
        play_id = 0;
        list_items = getResources().getStringArray(R.array.widget_audio_list);
        green_images = getResources().obtainTypedArray(R.array.number);
        yellow_images = getResources().obtainTypedArray(R.array.yellow_number);
        red_images = getResources().obtainTypedArray(R.array.red_number);
        sanketa = (ImageView) findViewById(R.id.sanketa);
        futaketa = (ImageView) findViewById(R.id.futaketa);
        hitoketa = (ImageView) findViewById(R.id.hitoketa);
        drawCounter();
    }

    @Override
    protected void onStart() {
        niri_sound.load();
        SharedPreferences setting = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        play_id = setting.getInt(SOUND_ID, 0);
        count = setting.getInt(COUNTER, 0);
        
        if(!(year == cal.get(Calendar.YEAR) &&
                month == cal.get(Calendar.MONTH) &&
                day == cal.get(Calendar.DAY_OF_MONTH))){
            count = 0;
            saveNumber(COUNTER, count);
            saveNumber(new String[]{YEAR, MONTH, DAY},
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH));
        }
        drawCounter();
        super.onStart();
    }

    @Override
    protected void onRestart() {
        niri_sound.load();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        niri_sound.load();
        super.onResume();
    }

    @Override
    protected void onPause() {
        niri_sound.unload();
        super.onPause();
    }

    @Override
    protected void onStop() {
        niri_sound.unload();
        saveNumber(COUNTER, count);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        niri_sound.unload();
        saveNumber(COUNTER, count);
        super.onDestroy();
    }

    //    ボタン押した時の処理
    public void click(View v) {
        switch (v.getId()) {
            case R.id.niri_button:
                count++;
                // サウンドの再生
                niri_sound.play(play_id);
                // 再描画
                drawCounter();
                break;
            case R.id.tweetbutton:
                if(twitter.isAccessToken()){
                    String tweet = list_items[play_id].toString();
                    twitter.tweet(count, tweet);
                }else{
                // "つぶやく"を押すと確証画面へ
                    twitter.executeOAuth();
                }
                break;
            case R.id.resetbutton:
                AlertDialog.Builder ab = new AlertDialog.Builder(this);
                ab.setMessage(R.string.reset_message)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences setting = getSharedPreferences(PREFS_NAME,
                                        MODE_PRIVATE);
                                SharedPreferences.Editor editor = setting.edit();
                                editor.remove(COUNTER);
                                editor.commit();
                                count = 0;
                                drawCounter();
                                Toast.makeText(getApplicationContext(), R.string.toast_reset,
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                ab.show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.niri, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                OptionDialogListener listener = new OptionDialogListener();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a audio")
                        .setItems(list_items, listener)
                        .create().show();
                break;
            case R.id.account:
                twitter.reLogin();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //カウンタービューの再描画
    private void drawCounter() {
        drawNumber(count);
    }
    private void drawNumber(int num){
        int val = num;
        if(count > 999){
            val = 999;
        }
        changeNumberColor(val);
        sanketadraw = images.getDrawable((val / 100) % 10);
        futaketadraw = images.getDrawable((val / 10) % 10);
        hitoketadraw = images.getDrawable(val % 10);
        sanketa.setImageDrawable(sanketadraw);
        futaketa.setImageDrawable(futaketadraw);
        hitoketa.setImageDrawable(hitoketadraw);
    }
    private void changeNumberColor(int val){
        TypedArray tmp;
        if(val > 666){
            tmp = red_images;
        }else if(val > 333){
            tmp = yellow_images;
        }else{
            tmp = green_images;
        }
        images = tmp;
    }

    // 値の保存をする
    private void saveNumber(String name, int num) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, num);
        editor.commit();
    }
    private void saveNumber(String[] name, int year, int month,int day) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name[0], year);
        editor.putInt(name[1], month);
        editor.putInt(name[2], day);
        editor.commit();
    }


    // 音生を変える
    public class OptionDialogListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            saveNumber(SOUND_ID, which);
            play_id = which;
        }
    }
}