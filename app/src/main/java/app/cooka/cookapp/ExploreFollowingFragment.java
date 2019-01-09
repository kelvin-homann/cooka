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

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.Recipe;
import app.cooka.cookapp.view.RecipeFeedCardItemAdapter;

public class ExploreFollowingFragment extends android.support.v4.app.Fragment {

    // Data for the Feed
    ArrayList<Recipe> recipesFollowing = new ArrayList<Recipe>();

    RecyclerView rvwRecipesFollowing;
    RecipeFeedCardItemAdapter recipeFollowingCardAdapter;


    public ExploreFollowingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore_following, container, false);

        // Initialisation of the Feed
        initFeed();

        // RecyclerView of the Feed
        rvwRecipesFollowing = view.findViewById(R.id.rvwFollowing);
        recipeFollowingCardAdapter = new RecipeFeedCardItemAdapter(recipesFollowing);
        rvwRecipesFollowing.setAdapter(recipeFollowingCardAdapter);
        rvwRecipesFollowing.setHasFixedSize(true);
        rvwRecipesFollowing.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private void initFeed(){

        //adding filter key
        List<String> filterKeys = new ArrayList<>();
        filterKeys.add("");

        //adding sort key
        List<String> sortKeys = new ArrayList<>();
        sortKeys.add("trendRating:desc");

        Recipe.Factory.selectFeedRecipes(getContext(), new IResultCallback<List<Recipe>>() {
            @Override
            public void onSucceeded(List<Recipe> result) {

                Log.d("COOKALOG", "feed inti Following " + recipesFollowing.size());

                recipesFollowing.clear();
                Log.d("COOKALOG", "feed cleared Following" + recipesFollowing.size());

                recipesFollowing.addAll(result);
                Log.d("COOKALOG", "feed added Folowing" + recipesFollowing.size());

                recipeFollowingCardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Throwable t) {

            }
        });
    }

}

