package com.blackspider.parseserversdkdemo.ui.signup;
/*
 *  ****************************************************************************
 *  * Created by : Arhan Ashik on 7/9/2018 at 3:13 PM.
 *  * Email : ashik.pstu.cse@gmail.com
 *  *
 *  * Last edited by : Arhan Ashik on 7/9/2018.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.blackspider.parseserversdkdemo.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class GetTwitterTokenTask extends AsyncTask<String, Void, String> {

    private LoginActivity activity;
    private String oauthURL, verifier;
    private Dialog dialog;
    private WebView webView;
    ProgressDialog progressBar;

    // Twitter variables
    private static Twitter twitter;
    private static RequestToken requestToken;
    private static AccessToken accessToken;

    public GetTwitterTokenTask(LoginActivity activity) {
        this.activity = activity;
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(activity.getString(R.string.twitter_consumer_key), activity.getString(R.string.twitter_consumer_secret));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //showing a progress dialog
        progressBar = new ProgressDialog(activity);
        progressBar.setMessage("Connecting...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setCancelable(false);
        progressBar.show();
    }

    @Override
    protected String doInBackground(String... args) {

        try {
            requestToken = twitter.getOAuthRequestToken();
            oauthURL = requestToken.getAuthorizationURL();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        return oauthURL;
    }

    @Override
    protected void onPostExecute(String oauthUrl) {
        if (oauthUrl != null) {
            dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            dialog.setContentView(R.layout.auth_dialog);
            webView = (WebView) dialog.findViewById(R.id.webv);
            //webView.getSettings().setJavaScriptEnabled(true);
            webView.loadUrl(oauthUrl);

            webView.setWebViewClient(new WebViewClient() {
                boolean authComplete = false;

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (url.contains("oauth_verifier") && authComplete == false) {
                        authComplete = true;
                        Log.e("AsyncTask", url);
                        Uri uri = Uri.parse(url);
                        verifier = uri.getQueryParameter("oauth_verifier");

                        dialog.dismiss();

                        //revoke access token asynctask
                        new AccessTokenGet().execute();
                    } else if (url.contains("denied")) {
                        dialog.dismiss();
                        Toast.makeText(activity, "Permission is Denied", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.show();
            dialog.setCancelable(true);
            progressBar.dismiss(); //dismiss progress dialog when task finished.
        } else {
            Toast.makeText(activity, "Network Error!", Toast.LENGTH_SHORT).show();
        }
    }

    private class AccessTokenGet extends AsyncTask<String, String, User> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(activity);
            progressBar.setMessage("Fetching Data ...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setCancelable(false);
            progressBar.show();
        }

        @Override
        protected User doInBackground(String... args) {
            User user = null;
            try {
                accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                user = twitter.showUser(accessToken.getUserId());
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return user;
        }

        @Override
        protected void onPostExecute(User response) {
            if (response == null) {
                Log.e("AsyncTask", "null user");
            } else {
                //call back data to UI here
                activity.callBackDataFromAsyncTask(response);
            }
            progressBar.dismiss();
        }
    }
}
