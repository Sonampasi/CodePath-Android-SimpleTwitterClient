package com.codepath.apps.simpletweets.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.simpletweets.utils.EndlessRecyclerViewScrollListener;
import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.fragments.TweetComposeFragment;
import com.codepath.apps.simpletweets.fragments.TweetDetailFragment;
import com.codepath.apps.simpletweets.adapters.TweetsArrayAdapter;
import com.codepath.apps.simpletweets.utils.TwitterApplication;
import com.codepath.apps.simpletweets.utils.TwitterClient;
import com.codepath.apps.simpletweets.models.Tweet;
import com.codepath.apps.simpletweets.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    private RecyclerView rvTweets;
    private ArrayList<Tweet> tweets;
    private TweetsArrayAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    User myUserAccount;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showComposeDialog();
            }
        });

        rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();
        adapter = new TweetsArrayAdapter(this,tweets);
        rvTweets.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvTweets.setLayoutManager(linearLayoutManager);
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // load more previous tweets on scroll
                populateTimeline();
                return true;
            }
        };
        rvTweets.addOnScrollListener(scrollListener);

        // hook up listener for tweet tap to view tweet detail
        adapter.setOnItemClickListener(new TweetsArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                showTweetDetailDialog(position);
            }
        });

        client = TwitterApplication.getRestClient();
        // load the initial latest tweets
        populateTimeline();
        // get logged in Twitter account info as well
        getMyUserJson();

    }


    private void getMyUserInfo(JSONObject json) {
        myUserAccount = User.fromArray(json);
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data on Swipe-to-refresh
        // `client` here is an instance of Android Async HTTP
        long since_id;
        long max_id = 0;
        // get tweets newer than the current newest tweet
        Tweet newestDisplayedTweet = tweets.get(0);
        since_id = newestDisplayedTweet.getUid();
        client.getHomeTimeline(since_id, max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {
                // add them to the ArrayList, notify the adapter, scroll back to show the new tweets
                tweets.addAll(0, Tweet.fromJsonArray(json));
                adapter.notifyItemRangeInserted(0, json.length());
                scrollToTop();
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
            }
        });
    }

    public void scrollToTop() {
        // when you post a tweet or swipe-to-refresh, scroll back to display the new tweet(s)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager = (LinearLayoutManager) rvTweets.getLayoutManager();
        linearLayoutManager.scrollToPositionWithOffset(0, 0);
    }

    // bring up the dialogfragment for composing a new tweet
    private void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        // pass in the URL for the user's profile image
        //String profileImageUrl = myUserAccount.getProfileImageUrl();
        TweetComposeFragment tweetComposeFragment = TweetComposeFragment.newInstance(myUserAccount);
        tweetComposeFragment.show(fm, "fragment_compose");
    }

    // bring up the dialogfragment for showing a detailed view of a tweet
    private void showTweetDetailDialog(int position) {
        // pass in the user's profile image and the tweet
        FragmentManager fm = getSupportFragmentManager();
        TweetDetailFragment tweetDetailFragment = TweetDetailFragment.newInstance(tweets.get(position));
        tweetDetailFragment.show(fm, "fragment_tweet_detail");
    }

    private void populateTimeline() {
        final int previousTweetsLength = tweets.size();
        long max_id = 0;
        long since_id = 1;
        if (previousTweetsLength > 0) {
            max_id = tweets.get(previousTweetsLength - 1).getUid() + 1;
        }
        client.getHomeTimeline(since_id, max_id,new JsonHttpResponseHandler(){
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
                Log.d("Debug",jsonArray.toString());
                tweets.addAll(Tweet.fromJsonArray(jsonArray));
                adapter.notifyDataSetChanged();
            }

            // failure
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("Debug",errorResponse.toString());
            }
        });

    }

    private void getMyUserJson() {
        // get the logged-in user's user account info
        client.getMyUserInfo(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                getMyUserInfo(json);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
            }
        });
    }

    public void onTweetButtonClicked(String TweetText) {
        // when the user composes a new tweet and taps the Tweet button, post it
        client.postTweet(TweetText, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                // get the new tweet and add it to the ArrayList
                Tweet newTweet = Tweet.fromJson(json);
                tweets.add(0, newTweet);
                // notify the adapter
                adapter.notifyItemInserted(0);
                // scroll back to display the new tweet
                scrollToTop();
                // display a success Toast
                Toast toast = Toast.makeText(TimelineActivity.this, "Tweet posted!", Toast.LENGTH_SHORT);
                View view = toast.getView();
                view.setBackgroundColor(0xC055ACEE);
                TextView textView = (TextView) view.findViewById(android.R.id.message);
                textView.setTextColor(0xFFFFFFFF);
                toast.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("DEBUG", errorResponse.toString());
            }
        });
    }

}
