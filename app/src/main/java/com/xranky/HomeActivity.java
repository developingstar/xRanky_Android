package com.xranky;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.xranky.customView.MyRecyclerAdapter;
import com.xranky.db.SQLiteFunction;
import com.xranky.eventBus.AddToFriend;
import com.xranky.eventBus.AddToPages;
import com.xranky.model.FeedItem;
import com.xranky.twitter.TwitterSupport;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * Created by Star on 6/13/2016.
 */

public class HomeActivity extends AppCompatActivity {

    private ArrayList<FeedItem> feedsList;
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter adapter;
    private ProgressBar progressBar;
    SwipeRefreshLayout swipe;
    TwitterSupport twitter;
    SQLiteFunction db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        twitter  = new TwitterSupport(this);

        db = new SQLiteFunction(this,SQLiteFunction.WRITE);
        // Initialize recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = (ProgressBar) findViewById(R.id.progressBar1);

        new AsyncHttpTask().execute();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                0,  ItemTouchHelper.RIGHT) {

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                 int swipeDir) {
                // Remove swiped item from list and notify the RecyclerView
                long userid = feedsList.get(viewHolder.getAdapterPosition()).userid;
                db.updateRef(userid, 2);
                Log.e("onSwiped", viewHolder.getAdapterPosition() + "");
                Toast.makeText(getApplicationContext(), "added to Pages", Toast.LENGTH_SHORT).show();
                for(int i=feedsList.size()-1;i> -1;i--){
                    if(feedsList.get(i).userid==userid){
                        feedsList.remove(i);
                    }
                }
                // mRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                EventBus.getDefault().post(new AddToPages());
            }

            @Override
            public boolean onMove(RecyclerView arg0, ViewHolder arg1,
                                  ViewHolder arg2) {
                // TODO Auto-generated method stub
                return false;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                simpleItemTouchCallback);

        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                new AsyncHttpTask().execute();
            }
        });
    }

    @Subscribe
    public void onEvent(String event){
        new AsyncHttpTask().execute();
    }

    @Subscribe
    public void onEvent(AddToFriend event){
        new AsyncHttpTask().execute();
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            setProgressBarIndeterminateVisibility(true);
            twitter.getTimelineTweets(null);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            try {
                feedsList = db.getFeeds(MainActivity.main);
                Log.e("feedM", ""+feedsList.size());
                result = 1;
            } catch (Exception e) {
                Log.e("MainAc", ""+e.toString());
            }
            return result; // "Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI
            progressBar.setVisibility(View.GONE);
            swipe.setRefreshing(false);
            if (swipe.isActivated()) {
                adapter.notifyDataSetChanged();
            } else {
                if (result == 1) {
                    adapter = new MyRecyclerAdapter(HomeActivity.this,feedsList,twitter,db);
                    mRecyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(HomeActivity.this, "No feeds available!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
