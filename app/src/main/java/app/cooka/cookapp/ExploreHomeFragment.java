package app.cooka.cookapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.FeedMessage;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.view.FeedMessageRecyclerViewAdapter;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ExploreHomeFragment extends android.support.v4.app.Fragment {

    // Data for the Feed
    ArrayList<FeedMessage> feed = new ArrayList<FeedMessage>();

    RecyclerView rvwFeed;
    FeedMessageRecyclerViewAdapter feedListAdapter;
    private LoginManager loginManager;

    public ExploreHomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        loginManager = LoginManager.Factory.getInstance(getApplicationContext());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d("ExploreHomeFragment", "onCreateView: inflating fragment layout");
        View view = inflater.inflate(R.layout.fragment_explore_home, container, false);

        // Initialisation of the Feed
        initFeed();

        // RecyclerView of the Feed
        rvwFeed = view.findViewById(R.id.rvwHome);
        feedListAdapter = new FeedMessageRecyclerViewAdapter(feed);
        rvwFeed.setAdapter(feedListAdapter);
        rvwFeed.setHasFixedSize(true);
        rvwFeed.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initFeed();
    }

    /**
     * Fills the ArrayLists needed to Display the Feed
     * Loops through the returned ArrayList and adds the Data from
     * the Object to the correct ArrayLists in this class
     */
    private void initFeed() {

        // get current user id
        final long userId = loginManager.getUserId();
        if(userId == 0L)
            return;

        FeedMessage.Factory.selectFeedMessages(getContext(), userId, FeedMessage.ST_ALL,
            false, new IResultCallback<List<FeedMessage>>() {
            @Override
            public void onSucceeded(List<FeedMessage> result) {

                feed.clear();
                feed.addAll(result);
                feedListAdapter.notifyDataSetChanged();
            }

                @Override
                public void onFailed(Throwable t) {

                }
            });

    }
}


