package com.xranky.twitter;

/**
 * Created by Star on 6/12/2016.
 */

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import com.xranky.db.SQLiteFunction;
import com.xranky.model.AppState;
import com.xranky.model.FeedItemModel;
import com.xranky.model.Ranking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import retrofit.client.Response;

public class TwitterSupport {
    private static final String TWITTER_KEY = "sMgG1ajdqw5S7UjK9tuAnIvSZ";
    private static final String TWITTER_SECRET = "6L1kPdLNOuQYMWQNlvpSXFWLO5jhl6rUH1mOCqch7XZnzuEzNb";
    private static TwitterAuthToken twitterToken = null;
    private static TwitterSession session = null;
    private static JSONObject myFriends = null;
    public static int  request_count = 0;
    public static boolean isStart = false;
    private SQLiteFunction db = null;
    private Context context = null;


    public TwitterSupport(Context context) {
        this.context = context;
        db = new SQLiteFunction(context, SQLiteFunction.WRITE);

    }

    public void searchTweets(){
        XRankyTwitterClient apiClient = new XRankyTwitterClient(session);
        apiClient.searchTweets().show("asd", 150,"2016-1-1", new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> tweet) {
                Log.e("Success","Search" + tweet.data);
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    public void getTimelineTweets(Long maxID){
        if (!db.checkRequest()){
            request_count = 0;
            if (isStart)
                db.transfer();
            if (!isStart)
                Toast.makeText(context, "Too Many Request!", Toast.LENGTH_SHORT).show();
            if (isStart) isStart = false;
            return;
        }
        if (!isStart) isStart = true;
        request_count ++;
        Toast.makeText(context,"Updating the ranking...",Toast.LENGTH_SHORT).show();
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        twitterApiClient.getStatusesService().homeTimeline(200,null,maxID,null,null,null,null, new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> listResult) {
                int indexFlag = 0;
                List<Tweet> tweets = listResult.data;
                FeedItemModel[] models = new FeedItemModel[tweets.size()];
                for(int i = 0; i < tweets.size();i++){
                    try {
                        Date date = new Date(tweets.get(i).createdAt);
                        if(Ranking.isDate24H(date.getTime())) {
                            indexFlag ++;
                            models[i] = new FeedItemModel();
                            models[i].id = tweets.get(i).id;
                            models[i].date = "" + date.getTime();

                            Tweet tweet;
                            if (tweets.get(i).retweetedStatus != null) {
                                tweet = tweets.get(i).retweetedStatus;
                                models[i].reUser = tweets.get(i).user.name;
                                models[i].retweeted = 1;
                            }else{
                                tweet = tweets.get(i);
                                models[i].reUser = null;
                                models[i].retweeted = 0;
                            }
                            models[i].userid = tweet.user.id;
                            models[i].name = tweet.user.name;
                            models[i].screenname = tweet.user.screenName;
                            models[i].profilepic = tweet.user.profileImageUrl;
                            models[i].status = tweet.text;
                            models[i].favorites = tweet.favoriteCount;
                            models[i].followers = tweet.user.followersCount;
                            models[i].replies = 0;
                            models[i].retweets = tweet.retweetCount;
                            models[i].isFollow = 1; //isFollowing(models[i].userid);

                            List<MediaEntity> media = tweets.get(i).entities.media;
                            if (media != null){
                                models[i].videoUrl = media.get(0).mediaUrlHttps;
                            }else{
                                models[i].videoUrl = "null";
                            }
                        }
                    }catch (Exception e){
                        Toast.makeText(context, "Failed to fetch data!.", Toast.LENGTH_SHORT).show();
                    }
                }
                db.saveFeedItemModel(models);
                AppState app = new AppState();
                app.updatedTime = new Date().getTime();
                app.requestCount = request_count;
                db.updateAppState(app);
                if (indexFlag == tweets.size()){
                    getTimelineTweets(models[indexFlag - 1].id);
                }else{
                    db.transfer();
                    isStart = false;
                }
//                EventBus.getDefault().post(SQliteHelp.home);
                setReplies(models);
            }
            @Override
            public void failure(TwitterException e) {
                Log.d("ERROR","Load Failed" + e );
            }
        });
    }

    public void setReplies(final FeedItemModel[] models){
        if (models.length == 0) return;
        XRankyTwitterClient apiClient = new XRankyTwitterClient(session);
        for (int i = 0; i < models.length; i ++) {
            final int finalI = i;
            if(models[i] == null) break;
            String name = models[i].name;
            apiClient.getReplyList().show("to:" + name, new Callback<Response>() {
                @Override
                public void success(Result<Response> result) {
                    BufferedReader reader = null;
                    StringBuilder sb = new StringBuilder();
                    try {
                        reader = new BufferedReader(new InputStreamReader(result.response.getBody().in()));
                        String line;
                        try {
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String response = sb.toString();
                    try {
                        JSONObject obj = new JSONObject(response);
                        JSONArray objArr = obj.getJSONArray("statuses");
                        db.updateReplies(models[finalI],objArr.length());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void failure(TwitterException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void getFollowers(long userId){
        XRankyTwitterClient apiClient = new XRankyTwitterClient(session);
        apiClient.getFollowersList().show(userId, "-1", 200, new Callback<Response>() {
            @Override
            public void success(Result<Response> result) {
                BufferedReader reader = null;
                StringBuilder sb = new StringBuilder();
                try {
                    reader = new BufferedReader(new InputStreamReader(result.response.getBody().in()));
                    String line;

                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String response = sb.toString();
                try {
                    JSONObject obj = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    public void getFriends() {
        XRankyTwitterClient apiClient = new XRankyTwitterClient(session);
        apiClient.getFriendsList().show(session.getUserId(), "-1", 200, new Callback<Response>() {
            @Override
            public void success(Result<Response> result) {
                BufferedReader reader = null;
                StringBuilder sb = new StringBuilder();
                try {
                    reader = new BufferedReader(new InputStreamReader(result.response.getBody().in()));
                    String line;

                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String response = sb.toString();
                try {
                    myFriends = new JSONObject(response);
                    //getTimelineTweets(null);
                    //searchTweets();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }

    public int isFollowing (long userId){
        JSONArray frinds = null;
        try {
            frinds = myFriends.getJSONArray("users");
        }catch (JSONException e){
            e.printStackTrace();
            return 0;
        }

        for (int i = 0; i < frinds.length(); i ++){
            try {
                Object id = frinds.getJSONObject(i).get("id");
                if (Long.valueOf(id.toString()).equals(userId)){
                    return 1;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        }
        return 0;
    }

    public void follow (final String userId){
        XRankyTwitterClient apiClient = new XRankyTwitterClient(session);
        apiClient.setFollow().create(userId, true, new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                db.updateIsFollow(userId);
                Toast.makeText(context, "Successed following!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(context, "Error following", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static String getTwitterKey() {
        return TWITTER_KEY;
    }

    public static String getTwitterSecret() {
        return TWITTER_SECRET;
    }

    public static TwitterAuthToken getTwitterToken() {
        return twitterToken;
    }

    public static void setTwitterToken(TwitterAuthToken token){
        twitterToken = token;
    }

    public static TwitterSession getSession() {
        return session;
    }

    public static void setSession(TwitterSession session) {
        TwitterSupport.session = session;
    }

    public static JSONObject getMyFriends() {
        return myFriends;
    }
}
