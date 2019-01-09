package app.cooka.cookapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

public class CookbookRecipesFragment extends Fragment {

    // Data for the Feed
    ArrayList<Recipe> recipesCookbook = new ArrayList<Recipe>();

    private RecyclerView rvwRecipesCookbook;
    private RecipeFeedCardItemAdapter recipesCookbookCardAdapter;
    private LoginManager loginManager;
    private View view;
    private FloatingActionButton fabCreateRecipe;

    public CookbookRecipesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        loginManager = LoginManager.Factory.getInstance(getApplicationContext());

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_cookbook_recipes, container, false);
        fabCreateRecipe = view.findViewById(R.id.fabCreateRecipe);
        fabCreateRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecipeEditorActivity.startAndCreateNew(getContext());
            }
        });

        // Initialisation of the Feed
        initFeed();

        // RecyclerView of the Feed
        rvwRecipesCookbook = view.findViewById(R.id.rvwRecipes);
        recipesCookbookCardAdapter = new RecipeFeedCardItemAdapter(recipesCookbook);
        rvwRecipesCookbook.setAdapter(recipesCookbookCardAdapter);
        rvwRecipesCookbook.setHasFixedSize(true);
        rvwRecipesCookbook.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private void initFeed() {

        // get current user id
        final long userId = loginManager.getUserId();
        if(userId == 0L)
            return;

        //adding filter key
        List<String> filterKeys = new ArrayList<>();
        filterKeys.add(String.format("creatorId:%d",loginManager.getUserId()));

        //adding sort key
        List<String> sortKeys = new ArrayList<>();
        sortKeys.add("");

        Recipe.Factory.selectRecipes(getContext(), filterKeys, null, 0, 0, new IResultCallback<List<Recipe>>() {
            @Override
            public void onSucceeded(List<Recipe> result) {

                Log.d("COOKALOG", "feed inti " + recipesCookbook.size());
                recipesCookbook.clear();

                Log.d("COOKALOG", "feed cleared " + recipesCookbook.size());
                recipesCookbook.addAll(result);

                Log.d("COOKALOG", "feed added " + recipesCookbook.size());
                recipesCookbookCardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Throwable t) {

            }
        });
    }

}
