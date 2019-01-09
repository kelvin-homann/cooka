package app.cooka.cookapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.Image;
import app.cooka.cookapp.model.Recipe;
import app.cooka.cookapp.model.RecipeStep;
import app.cooka.cookapp.model.RecipeStepIngredient;
import app.cooka.cookapp.view.FeedMessageRecyclerViewAdapter;
import app.cooka.cookapp.view.LoadingScreenView;
import app.cooka.cookapp.view.RecipeDetailsIngredientsFrameLayout;
import app.cooka.cookapp.view.RecipeDetailsStepsFrameLayout;

public class RecipeDetailsActivity extends AppCompatActivity {

    // call intent extra keys
    public static final String IEK_RECIPEID = "recipeId";

    private Recipe recipe;

    private LoadingScreenView lsvwRecipeDetailsLoadingScreen;
    private ProgressBar pbarLoadingMainImage;
    private TextView tvwCouldNotLoadImage;
    private TextView tvwImageMissing;
    private Toolbar tbarRecipeDetailsToolbar;
    private ImageView ivwMainImage;
    private ImageView ivwMockupIndicator;
    private NestedScrollView nsvwScrollView;
    private TextView tvwRecipeTitle;
    private TextView tvwCreatedBy;
    private TextView tvwCreatorName;
    private TextView tvwRecipeDescription;
    private TextView tvwDifficultyValue;
    private TextView tvwPreparationTimeValue;
    private RecipeDetailsIngredientsFrameLayout fltRecipeDetailsIngredients;
    private TextView tvwForNServings;
    private TextView tvwIngredientsMissing;
    private RecipeDetailsStepsFrameLayout fltRecipeDetailsSteps;
    private TextView tvwRecipeStepsMissing;
    private ImageView ivwRatingStars[];
    private FloatingActionButton fabCookRecipe;
    private MenuItem miShowCreatorProfile;
    private boolean userHasScrolled = false;

    private static Bitmap imageMissingPlaceholder;
    private static Drawable ratingStarEmpty;
    private static Drawable ratingStarHalf;
    private static Drawable ratingStarFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        lsvwRecipeDetailsLoadingScreen = findViewById(R.id.lsvwRecipeDetailsLoadingScreen);
        lsvwRecipeDetailsLoadingScreen.setVisible(true);
        ViewGroup.LayoutParams currentLayoutParams = lsvwRecipeDetailsLoadingScreen.getLayoutParams();
        currentLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lsvwRecipeDetailsLoadingScreen.setLayoutParams(currentLayoutParams);

        pbarLoadingMainImage = findViewById(R.id.pbarLoadingMainImage);
        pbarLoadingMainImage.setVisibility(View.GONE);
        tvwCouldNotLoadImage = findViewById(R.id.tvwCouldNotLoadImage);
        tvwCouldNotLoadImage.setVisibility(View.GONE);
        tvwImageMissing = findViewById(R.id.tvwImageMissing);
        tvwImageMissing.setVisibility(View.GONE);

        tbarRecipeDetailsToolbar = findViewById(R.id.tbarRecipeDetailsToolbar);
        setSupportActionBar(tbarRecipeDetailsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ivwMainImage = findViewById(R.id.ivwMainImage);
        ivwMockupIndicator = findViewById(R.id.ivwMockupIndicator);
        nsvwScrollView = findViewById(R.id.nsvwScrollView);
        tvwRecipeTitle = findViewById(R.id.tvwRecipeTitle);
        tvwCreatedBy = findViewById(R.id.tvwCreatedBy);
        tvwCreatorName = findViewById(R.id.tvwCreatorName);
        tvwRecipeDescription = findViewById(R.id.tvwRecipeDescription);
        tvwDifficultyValue = findViewById(R.id.tvwDifficultyValue);
        tvwPreparationTimeValue = findViewById(R.id.tvwPreparationTimeValue);
        fltRecipeDetailsIngredients = findViewById(R.id.fltRecipeDetailsIngredients);
        tvwForNServings = findViewById(R.id.tvwForNServings);
        tvwIngredientsMissing = findViewById(R.id.tvwIngredientsMissing);
        fltRecipeDetailsSteps = findViewById(R.id.fltRecipeDetailsSteps);
        tvwRecipeStepsMissing = findViewById(R.id.tvwRecipeStepsMissing);

        fabCookRecipe = findViewById(R.id.fabCookRecipe);
        fabCookRecipe.hide();

        nsvwScrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                userHasScrolled = true;
            }
        });

        ivwRatingStars = new ImageView[]{
            findViewById(R.id.ivwRatingStar1),
            findViewById(R.id.ivwRatingStar2),
            findViewById(R.id.ivwRatingStar3),
            findViewById(R.id.ivwRatingStar4),
            findViewById(R.id.ivwRatingStar5)
        };

        if(ratingStarEmpty == null)
            ratingStarEmpty = getDrawable(R.drawable.ic_rating_star_empty);
        if(ratingStarHalf == null)
            ratingStarHalf = getDrawable(R.drawable.ic_rating_star_half);
        if(ratingStarFull == null)
            ratingStarFull = getDrawable(R.drawable.ic_rating_star_full);

        Intent callIntent = getIntent();
        Bundle extras = callIntent.getExtras();

        long recipeIdToLoad = 11;

        if(extras != null) {
            long intentRecipeId = extras.getLong(IEK_RECIPEID, 0);
            if(intentRecipeId != 0)
                recipeIdToLoad = intentRecipeId;
        }

        // retrieve recipe
        Recipe.Factory.selectRecipe(this, recipeIdToLoad, -1, -1, -1, -1, 0, -1, 3,
            new IResultCallback<Recipe>() {
            @Override
            public void onSucceeded(Recipe result) {
                setRecipe(result);
            }

            @Override
            public void onFailed(Throwable t) {
                Toast.makeText(RecipeDetailsActivity.this,
                    "Oops, something didn't work there. Please try again in a couple of seconds",
                    Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.recipe_details_toolbar, menu);
        miShowCreatorProfile = findViewById(R.id.action_show_creator_profile);
        updateShowCreatorProfileMenuItem();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

        case android.R.id.home:
            onBackPressed();
            return true;

        case R.id.action_share:
            shareRecipe();
            break;

        case R.id.action_add_to_collection:
            addRecipeToCollection();
            break;

        case R.id.action_cook_now:
            cookRecipe();
            break;

        case R.id.action_show_creator_profile:
            showCreatorProfile();
            break;

        case R.id.action_add_image_to_recipe:
            addImageToRecipe();
            break;

        case R.id.action_find_similar_recipes:
            findSimilarRecipes();
            break;

        case R.id.action_report_recipe:
            reportRecipe();
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void startAndLoadRecipe(final Context context, final long recipeId) {

        Intent callIntent = new Intent(context, RecipeDetailsActivity.class);
        callIntent.putExtra(IEK_RECIPEID, recipeId);
        context.startActivity(callIntent);
    }

    private void setRecipe(final Recipe recipe) {

        this.recipe = recipe;
        if(recipe == null)
            return;

        tvwRecipeTitle.setText(recipe.getTitle());

        final String creatorName = recipe.getCreatorName();

        if(creatorName != null && !creatorName.equals("")) {
            tvwCreatedBy.setVisibility(View.VISIBLE);
            tvwCreatorName.setText("@" + recipe.getCreatorName());
            tvwCreatorName.setVisibility(View.VISIBLE);

            tvwCreatorName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCreatorProfile();
                }
            });

            updateShowCreatorProfileMenuItem();
        }
        else {
            tvwCreatedBy.setVisibility(View.GONE);
            tvwCreatorName.setText("");
            tvwCreatorName.setVisibility(View.GONE);
            updateShowCreatorProfileMenuItem();
        }

        String description = recipe.getDescription();
        if(description != null && !description.equals("")) {
            tvwRecipeDescription.setText(description);
            tvwRecipeDescription.setVisibility(View.VISIBLE);
        }
        else {
            tvwRecipeDescription.setVisibility(View.GONE);
        }

        // set rating stars
        float rating = recipe.getRating();
        int fullStars = (int)rating;

        for(int i = 0; i < 5; i++) {
            if(i < fullStars) {
                ivwRatingStars[i].setImageDrawable(ratingStarFull);
            }
            else if(i == fullStars) {
                float fraction = rating - fullStars;
                if(fraction < 0.33f)
                    ivwRatingStars[i].setImageDrawable(ratingStarEmpty);
                else if(fraction < 0.66f)
                    ivwRatingStars[i].setImageDrawable(ratingStarHalf);
                else
                    ivwRatingStars[i].setImageDrawable(ratingStarFull);
            }
            else {
                ivwRatingStars[i].setImageDrawable(ratingStarEmpty);
            }
        }

        switch(recipe.getDifficultyType()) {
        case SIMPLE:
            tvwDifficultyValue.setText(getString(R.string.recipe_details_difficulty_simple));
            break;
        case MODERATE:
            tvwDifficultyValue.setText(getString(R.string.recipe_details_difficulty_moderate));
            break;
        case DEMANDING:
            tvwDifficultyValue.setText(getString(R.string.recipe_details_difficulty_demanding));
            break;
        }

        tvwPreparationTimeValue.setText(getString(R.string.recipe_details_preparation_time_n_min,
            recipe.getPreparationTime()));

        String mainImageFileName = recipe.getMainImageFileName();
        if(mainImageFileName != null && !mainImageFileName.equals("")) {
            final String mainImageFileUrl = Image.IMAGE_BASE_URL + mainImageFileName;
            setMainImage(mainImageFileUrl);
        }
        else {
            setMainImagePlaceholder();
            tvwCouldNotLoadImage.setVisibility(View.GONE);
            tvwImageMissing.setVisibility(View.VISIBLE);
        }

        if((recipe.getFlags() & Recipe.FLAG_MOCKUP) == Recipe.FLAG_MOCKUP) {
            ivwMockupIndicator.setVisibility(View.VISIBLE);
        }
        else {
            ivwMockupIndicator.setVisibility(View.GONE);
        }

        List<RecipeStepIngredient> ingredients = recipe.getIngredients();
        if(ingredients.size() > 0) {
            fltRecipeDetailsIngredients.setIngredients(ingredients);
            fltRecipeDetailsIngredients.setVisibility(View.VISIBLE);
            // fixed servings number for now
            tvwForNServings.setText(getString(R.string.recipe_details_for_n_servings, 1));
            tvwForNServings.setVisibility(View.VISIBLE);
            tvwIngredientsMissing.setVisibility(View.GONE);
        }
        else {
            fltRecipeDetailsIngredients.clearIngredients();
            fltRecipeDetailsIngredients.setVisibility(View.INVISIBLE);
            tvwForNServings.setVisibility(View.GONE);
            tvwIngredientsMissing.setVisibility(View.VISIBLE);
        }

        List<RecipeStep> steps = recipe.getRecipeSteps();
        if(steps.size() > 0) {
            fltRecipeDetailsSteps.setSteps(steps);
            fltRecipeDetailsSteps.setVisibility(View.VISIBLE);
            tvwRecipeStepsMissing.setVisibility(View.GONE);
        }
        else {
            fltRecipeDetailsSteps.clearSteps();
            fltRecipeDetailsSteps.setVisibility(View.INVISIBLE);
            tvwRecipeStepsMissing.setVisibility(View.VISIBLE);
        }

        // set "cook recipe" floating access button
        fabCookRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cookRecipe();
            }
        });

        lsvwRecipeDetailsLoadingScreen.setVisible(false);
        fabCookRecipe.show();
    }

    private void setMainImage(final String mainImageFileUrl) {

        pbarLoadingMainImage.setVisibility(View.VISIBLE);
        tvwCouldNotLoadImage.setVisibility(View.GONE);
        tvwImageMissing.setVisibility(View.GONE);

        Glide.with(this)
            .asBitmap()
            .apply(new RequestOptions()
            .placeholder(FeedMessageRecyclerViewAdapter.ViewHolder.defaultProfileImage))
            .load(mainImageFileUrl)
            .listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                    Target<Bitmap> target, boolean isFirstResource)
                {
                    ivwMainImage.setImageDrawable(null);
                    pbarLoadingMainImage.setVisibility(View.GONE);
                    setMainImagePlaceholder();
                    tvwCouldNotLoadImage.setVisibility(View.VISIBLE);
                    tvwImageMissing.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap>
                    target, DataSource dataSource, boolean isFirstResource)
                {
                    ivwMainImage.setImageBitmap(resource);
                    pbarLoadingMainImage.setVisibility(View.GONE);
                    tvwCouldNotLoadImage.setVisibility(View.GONE);
                    // adjust toolbar colors if necessary
                    return true;
                }
            })
            .submit();
    }

    private void setMainImagePlaceholder() {

        ivwMainImage.setImageResource(R.mipmap.image_missing);
        pbarLoadingMainImage.setVisibility(View.GONE);
    }

    private void shareRecipe() {

        Toast.makeText(this, "Share recipe... not yet implemented", Toast.LENGTH_LONG).show();
    }

    private void addRecipeToCollection() {

        Toast.makeText(this, "Add recipe to collection... not yet implemented", Toast.LENGTH_LONG).show();
    }

    private void cookRecipe() {

        CookModeActivity.startAndLoadRecipe(RecipeDetailsActivity.this, recipe.getRecipeId());
    }

    private void addImageToRecipe() {

        Toast.makeText(this, "Add image to recipe... not yet implemented", Toast.LENGTH_LONG).show();
    }

    private void showCreatorProfile() {

        final String creatorName = recipe.getCreatorName();

        if(creatorName != null && !creatorName.equals("")) {
            if(recipe.getCreatorId() != 0) {
                Bundle bundleProfile = new Bundle();
                bundleProfile.putLong("userid", recipe.getCreatorId());
                Intent profileIntent = new Intent(RecipeDetailsActivity.this, UserProfileActivity.class);
                profileIntent.putExtras(bundleProfile);
                RecipeDetailsActivity.this.startActivity(profileIntent);
            }
            else {
                Toast.makeText(RecipeDetailsActivity.this,
                    getString(R.string.recipe_details_creator_profile_is_mockup,
                        creatorName), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void findSimilarRecipes() {

        Toast.makeText(this, "Find similar recipes... not yet implemented", Toast.LENGTH_LONG).show();
    }

    private void reportRecipe() {

        Toast.makeText(this, "Report recipe... not yet implemented", Toast.LENGTH_LONG).show();
    }

    private void updateShowCreatorProfileMenuItem() {

        if(miShowCreatorProfile == null)
            return;
        miShowCreatorProfile.setVisible(!(recipe == null || recipe.getCreatorId() == 0));
    }
}
