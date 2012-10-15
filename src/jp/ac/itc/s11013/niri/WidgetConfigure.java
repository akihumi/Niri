package jp.ac.itc.s11013.niri;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

public class WidgetConfigure extends Activity {

    public static final String POST_SWITCH = "toggle";
    public static final String POST_PER_NUMBER = "post_per_number";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private int every_num_post;
    private NiriTwitter twitter;
    private ToggleButton post_switch;
    private boolean post_flag;
    // 何回毎に投稿するかを入力してもらうダイアログ
    private AlertDialog num_alert;
    private View view;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        setContentView(R.layout.config_widget);
        twitter = new NiriTwitter(this);
        every_num_post = 10;
        // ダイアログ表示用のxmlからIDを取得するための変数
        inflater = LayoutInflater.from(this.getBaseContext());
        view = inflater.inflate(R.layout.alert, null);
        num_alert = new AlertDialog.Builder(this)
        .setTitle("10~100までの数値を入力")
        .setMessage("最小10,最大100です。")
        .setView(view)
        .setNegativeButton("OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText edit = (EditText) view
                        .findViewById(R.id.edit_alert);
                Button post_num = (Button) WidgetConfigure.this.findViewById(R.id.post_number);
                if(edit.getText().toString().equals("")){
                    edit.setText(R.string.ten);
                }
                if(Integer.parseInt(edit.getText().toString()) < 10){
                    every_num_post = 10;
                }else if(Integer.parseInt(edit.getText().toString()) > 100){
                    every_num_post = 100;
                }else{
                    every_num_post = Integer.parseInt(edit.getText().toString());
                }
                post_num.setText(String.valueOf(every_num_post));
            }
        }).create();
        num_alert.setCancelable(true);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras(); 
        if(extras != null){
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_CANCELED, resultValue);
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }  
    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getSharedPreferences(Niri.PREFS_NAME + appWidgetId, MODE_PRIVATE);
        post_flag = prefs.getBoolean(POST_SWITCH, false);
        post_switch =  (ToggleButton) findViewById(R.id.post_switch);
        post_switch.setChecked(post_flag);
    }
    // ボタンを押した時の処理
    public void clickButton(View v){
        switch(v.getId()){
            case R.id.ok:
                finishConfigure();
                break;
            case R.id.post_number:
                num_alert.show();
                break;
            case R.id.widget_login:
                twitter.executeOAuth();
                break;
        }

    }
    public void toggle(View v){
        if(post_switch.isChecked()){
            post_flag = true;
        }else{
            post_flag = false;
        }
        post_switch.setChecked(post_flag);
        SharedPreferences prefs =
                getSharedPreferences(Niri.PREFS_NAME + appWidgetId, MODE_PRIVATE);
        prefs.edit().putBoolean(POST_SWITCH, post_flag).commit();
    }
    
    private void finishConfigure(){
        final Context context = WidgetConfigure.this;
        Spinner spinner = (Spinner) findViewById(R.id.config_spinner);
        int sound_id = spinner.getSelectedItemPosition();
        
        saveSettings(context, appWidgetId, sound_id);
        SharedPreferences.Editor prefs = 
                context.getSharedPreferences(Niri.PREFS_NAME + appWidgetId, MODE_PRIVATE).edit();
        prefs.putInt(POST_PER_NUMBER, every_num_post);
        prefs.commit();

        Intent resultValue = new Intent();  
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);  
        finish();  
    }
    private void saveSettings(Context context, int appWidgetId, int sound_id){
        SharedPreferences.Editor prefs = 
                context.getSharedPreferences(Niri.PREFS_NAME + appWidgetId, MODE_PRIVATE).edit();
        prefs.putInt(Niri.SOUND_ID, sound_id);
        prefs.commit();
    }  

}
