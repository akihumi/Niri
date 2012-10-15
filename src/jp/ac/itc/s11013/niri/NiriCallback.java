package jp.ac.itc.s11013.niri;

import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class NiriCallback extends Activity {
    private static ProgressDialog waitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.callback);
        waitDialog = new ProgressDialog(this);
        waitDialog.setMessage(this.getString(R.string.webloading));
        waitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        waitDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        WebView webview = (WebView) findViewById(R.id.webView1);
        webview.setWebViewClient(new CostomWebClient());
        webview.loadUrl(this.getIntent().getExtras().getString("uri"));
    }

    public class CostomWebClient extends WebViewClient{

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(!(url.indexOf("denied=") == -1)){
                view.loadUrl(NiriTwitter.HATENA_SOPURAN);
            }else if(url != null && url.startsWith(NiriTwitter.CALLBACK_URL)){
                // URLパラメータを分解する。
                String[] urlParameters = url.split("\\?")[1].split("&");
//                String oauthToken = "";
                String verifier = "";
                
//                // oauth_tokenをURLパラメータから切り出す。
//                if(urlParameters[0].startsWith("oauth_token")){
//                    oauthToken = urlParameters[0].split("=")[1];
//                }else if(urlParameters[1].startsWith("oauth_token")){
//                    oauthToken = urlParameters[1].split("=")[1];
//                }
//                
                // oauth_verifierをURLパラメータから切り出す。
                if(urlParameters[0].startsWith("oauth_verifier")){
                    verifier = urlParameters[0].split("=")[1];
                }else if(urlParameters[1].startsWith("oauth_verifier")){
                    verifier = urlParameters[1].split("=")[1];
                }
                AccessToken accessToken = null;
                try {
                    // AccessTokeオブジェクトを取得
                    accessToken = NiriTwitter.OAuth.getOAuthAccessToken(NiriTwitter.requestToken,
                            verifier);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                SharedPreferences pref = getSharedPreferences(NiriTwitter.TWITTER_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(NiriTwitter.KEY_TOKEN, accessToken.getToken());
                editor.putString(NiriTwitter.KEY_TOKEN_SECRET,
                        accessToken.getTokenSecret());
                editor.putBoolean(NiriTwitter.FLAG, true);
                editor.commit();
                view.loadUrl("file:///android_asset/load.html");
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(waitDialog.isShowing()){
            waitDialog.dismiss();
            }
            if(url != null && url.startsWith(NiriTwitter.CALLBACK_URL)){
                finish();
            }
        }
    }
//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        }
//    }
}