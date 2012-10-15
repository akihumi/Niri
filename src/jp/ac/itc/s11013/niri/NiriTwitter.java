package jp.ac.itc.s11013.niri;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class NiriTwitter {
    private Context context;

    public static RequestToken requestToken = null;
    public static OAuthAuthorization OAuth = null;
    public static final String CONSUMER_KEY = "dSWiw9q7X4DlVvKYg4OTMw";
    public static final String CONSUMER_SECRET = "n4p0fyWLJQdtswBe3ykLCo8bEP9ko71bXhQhZ34AcKk";
    public static final String CALLBACK_URL = "callback://NiriCallback";
    public static final String OAUTH_VERIFIER = "oauth_verifier";
    public static final String KEY_TOKEN = "key_token";
    public static final String KEY_TOKEN_SECRET = "key_token_secret";
    public static final String TWITTER_PREFS = "twitter_prefs";
    public static final String FLAG = "flag";
    public static final String HATENA_SOPURAN = "http://d.hatena.ne.jp/sopuran/";

    public NiriTwitter(Context context){
        this.context = context;
    }
    public void executeOAuth() {
        // twitter4jの設定を読み込む
        Configuration conf = ConfigurationContext.getInstance();
        // OAuth認証オブジェクト作成
        OAuth = new OAuthAuthorization(conf);
        // OAuth認証オブジェクトにCONSUMER_KEY、CONSUMER_SECRETを追加
        OAuth.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        PreTask pre = new PreTask();
        pre.execute();
    }
    // 記憶した設定を消す
    public void logoutOAuth(){
        SharedPreferences pref = context.getSharedPreferences(NiriTwitter.TWITTER_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(NiriTwitter.KEY_TOKEN);
        editor.remove(NiriTwitter.KEY_TOKEN_SECRET);
        editor.remove(NiriTwitter.FLAG);
        editor.commit();

    }
    public void reLogin(){
        logoutOAuth();
        executeOAuth();
    }
    public boolean isAccessToken(){
        SharedPreferences flag = context.getSharedPreferences(NiriTwitter.TWITTER_PREFS,
                Context.MODE_PRIVATE);
        return flag.getBoolean(FLAG, false);
    }
    // twitterにツイートを試みる成功するとtrueをかえす
    private void trytweet(final int num, final String tweet) throws TwitterException {
        if(!(isAccessToken())){
            return;
        }
        // 投稿する文字を生成
        StringBuilder message = new StringBuilder();
        message.append(context.getString(R.string.today)).append(num).
        append(context.getString(R.string.kai)).append(tweet)
        .append(context.getString(R.string.desu)).append(tweet);
        // アクセストークンの生成
        SharedPreferences pref = context.getSharedPreferences(NiriTwitter.TWITTER_PREFS, Context.MODE_PRIVATE);
        String token = pref.getString(KEY_TOKEN, null);
        String tokenSecret = pref.getString(KEY_TOKEN_SECRET, null);
        AccessToken accessToken = new AccessToken(token, tokenSecret);
        TwitterListener listener = new TwitterAdapter(){
            @Override
            public void updatedStatus(Status status) {
                System.out.println("Successfully updated the status to [" +
                         status.getText() + "].");
              }
            @Override
            public void onException(TwitterException ex, TwitterMethod method) {
                super.onException(ex, method);
                ex.printStackTrace();
            }
        };
//      非同期でつぶやく
        AsyncTwitterFactory factory = new AsyncTwitterFactory();
        AsyncTwitter twitter = factory.getInstance();
        twitter.addListener(listener);
//        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
        twitter.setOAuthAccessToken(accessToken);
        Log.v("tweet", "in!");
        twitter.updateStatus(message.toString());
    }
    public void tweet(int num, String tweet){
        Toast.makeText(context, R.string.tweet_post, Toast.LENGTH_SHORT).show();
        try{
            trytweet(num, tweet);
        }catch(TwitterException e){
            Toast.makeText(context, R.string.error_twitterupdate, Toast.LENGTH_SHORT).show();
        }
    }
    public class PreTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog waitDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            waitDialog = new ProgressDialog(context);
            waitDialog.setMessage(context.getString(R.string.loading));
            waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            waitDialog.show();
        }
    
        @Override
        protected String doInBackground(Void... arg0) {
            // アプリの認証オブジェクト作成
            String uri = "";
            try {
                requestToken = OAuth.getOAuthRequestToken(CALLBACK_URL);
                if(requestToken != null){
                    uri = requestToken.getAuthorizationURL();
                }
            } catch(TwitterException e) {
                Toast.makeText(context, R.string.error_requesttoken, Toast.LENGTH_SHORT).show();
            }
            return uri;
        }
    
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(waitDialog.isShowing()){
                waitDialog.dismiss();
            }
    
            Intent i = new Intent(context, NiriCallback.class);
            i.putExtra("uri", result);
            context.startActivity(i);
        }
    }
}
