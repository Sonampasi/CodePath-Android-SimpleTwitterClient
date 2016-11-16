package com.codepath.apps.simpletweets.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.simpletweets.R;
import com.codepath.apps.simpletweets.models.Tweet;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TweetDetailFragment extends android.support.v4.app.DialogFragment {

    private Tweet tweet;

    public TweetDetailFragment() {
        // Required empty public constructor
    }

    public static TweetDetailFragment newInstance(Tweet tweet) {
        // get the tweet to display and the logged-in user's profile image URL
        TweetDetailFragment fragment = new TweetDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("tweet", tweet);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tweet_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Get fields from view
        ImageView ivClose = (ImageView) view.findViewById(R.id.ivClose);
        ImageView ivProfileImage = (ImageView) view.findViewById(R.id.ivProfileImage);
        TextView tvUserName = (TextView) view.findViewById(R.id.tvUserName);
        TextView tvScreenName = (TextView) view.findViewById(R.id.tvScreenName);
        final TextView tvTweetBody = (TextView) view.findViewById(R.id.tvBody);
        TextView tvTimeStamp = (TextView) view.findViewById(R.id.tvCreatedAt);
        // Fetch arguments from bundle

        // and display the tweet
        tweet = (Tweet) getArguments().getSerializable("tweet");
        Picasso.with(view.getContext()).load(tweet.getUser().getProfileImageUrl()).fit().centerCrop().into(ivProfileImage);
        tvUserName.setText(tweet.getUser().getName());
        tvScreenName.setText("@" + tweet.getUser().getScreenName());
        tvTweetBody.setText(tweet.getBody());
        String tweetTimeStamp = useTweetDetailDateFormat(tweet.getCreatedAt());
        tvTimeStamp.setText(tweetTimeStamp);

        // the X in the top-left corner closes the fragment
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }

    // display the tweet's timestamp using the format that the Twitter app uses when you
    // tap a tweet to bring up a detailed view
    public String useTweetDetailDateFormat(String rawJsonDate) {
        Date date;
        long dateMillis = 0;
        date = new Date(dateMillis);
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            dateMillis = sf.parse(rawJsonDate).getTime();
            date = new Date(dateMillis);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String twitterDetailDateFormat = "h:mm a • d MMM yy";
        SimpleDateFormat stdf = new SimpleDateFormat(twitterDetailDateFormat, Locale.ENGLISH);
        stdf.setLenient(true);

        return stdf.format(date);
    }
}