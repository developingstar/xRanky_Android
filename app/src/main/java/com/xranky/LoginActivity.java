package com.xranky;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.xranky.db.SQLiteFunction;
import com.xranky.twitter.TwitterSupport;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Star on 6/12/2016.
 */
public class LoginActivity extends Activity{
    private TwitterLoginButton loginButton;
    private SQLiteFunction db;
    private TwitterSupport twitter;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        db = new SQLiteFunction(this, SQLiteFunction.WRITE);
        twitter = new TwitterSupport(this);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TwitterSupport.getTwitterKey(), TwitterSupport.getTwitterSecret());
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_login);
        loginButton = (TwitterLoginButton) findViewById(R.id.btnLogin);
        loginButton.setBackgroundResource(R.drawable.twitter_button);
        loginButton.setPadding(25,0,0,0);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                TwitterSupport.setSession(session);
                TwitterSupport.setTwitterToken(session.getAuthToken());
                if (!db.isLogin()){
                    db.login(session.getUserId());
                    db.deleteALL();
                }
                twitter.getTimelineTweets(null);
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }
            @Override
            public void failure(TwitterException exception) {
                Log.d("TwitterKit", "Login with Twitter failure", exception);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

}
