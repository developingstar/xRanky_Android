package com.xranky.twitter;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by Star on 6/15/2016.
 */
public class XRankyTwitterClient extends TwitterApiClient {

    public XRankyTwitterClient(TwitterSession session) {
        super(session);
    }

    public SearchTweets searchTweets(){
        return getService(SearchTweets.class);
    }

    public FollowersList getFollowersList() {
        return getService(FollowersList.class);
    }

    public FriendsList getFriendsList() {
        return getService(FriendsList.class);
    }

    public ReplyList getReplyList(){
        return getService(ReplyList.class);
    }

    public SearchLookup userLookup(){
        return getService(SearchLookup.class);
    }

    public Follow setFollow() {
        return getService(Follow.class);
    }

    /*interface used for Auth Api call for CreateFriendship*/

    public interface SearchTweets{
        @GET("/1.1/search/tweets.json")
        void show(@Query("q") String query, @Query("count") int count, @Query("until") String until, Callback<Tweet> cb);
    }

    public interface FollowersList {
        @GET("/1.1/followers/list.json")
        void show(@Query("user_id") long user_id, @Query("cursor") String cursor, @Query("count") int count, Callback<Response> cb);
    }

    public interface FriendsList {
        @GET("/1.1/friends/list.json")
        void show(@Query("user_id") long user_id, @Query("cursor") String cursor, @Query("count") int count, Callback<Response> cb);
    }

    public interface ReplyList {
        @GET("/1.1/search/tweets.json")
        void show(@Query("q") String query, Callback<Response> cb);
    }

    public interface Follow {
        @POST("/1.1/friendships/create.json")
        void create(@Query("user_id") String user_id, @Query("follow") boolean follow, Callback<User> cb);
    }

    public interface SearchLookup{
        @GET("/1.1/users/lookup.json")
        void show(@Query("user_id") long id, @Query("include_entities") boolean entities, Callback<Response> cb);
    }
}