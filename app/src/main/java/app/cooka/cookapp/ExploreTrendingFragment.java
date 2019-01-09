package app.cooka.cookapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.Recipe;
import app.cooka.cookapp.view.RecipeFeedCardItemAdapter;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ExploreTrendingFragment extends Fragment {

    // Data for the Feed
    ArrayList<Recipe> recipesTrending = new ArrayList<Recipe>();

    private RecyclerView rvwRecipesTrending;
    private RecipeFeedCardItemAdapter recipeTrendingCardAdapter;
    private LoginManager loginManager;
    private View view;

    public ExploreTrendingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        loginManager = LoginManager.Factory.getInstance(getApplicationContext());

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_explore_trending, container, false);

        // Initialisation of the Feed
        initFeed();

        // RecyclerView of the Feed
        rvwRecipesTrending = view.findViewById(R.id.rvwTrending);
        recipeTrendingCardAdapter = new RecipeFeedCardItemAdapter(recipesTrending);
        rvwRecipesTrending.setAdapter(recipeTrendingCardAdapter);
        rvwRecipesTrending.setHasFixedSize(true);
        rvwRecipesTrending.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private void initFeed() {

        // get current user id
        final long userId = loginManager.getUserId();
        if(userId == 0L)
            return;

        //adding filter key
        List<String> filterKeys = new ArrayList<>();


        List<String> sortKeys = new ArrayList<>();
        sortKeys.add("trendRating:desc");

        Recipe.Factory.selectRecipes(getContext(), filterKeys, sortKeys, 0, 0, new IResultCallback<List<Recipe>>() {
            @Override
            public void onSucceeded(List<Recipe> result) {

                Log.d("COOKALOG", "feed initiated " + recipesTrending.size());

                recipesTrending.clear();
                Log.d("COOKALOG", "feed cleared " + recipesTrending.size());

                recipesTrending.addAll(result);
                Log.d("COOKALOG", "feed added " + recipesTrending.size());

                recipeTrendingCardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Throwable t) {

            }
        });
    }

}
