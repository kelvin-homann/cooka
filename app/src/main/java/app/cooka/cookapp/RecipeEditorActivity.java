package app.cooka.cookapp;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rd.PageIndicatorView;
import com.rd.utils.DensityUtils;

import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.Recipe;
import app.cooka.cookapp.model.RecipeStep;
import app.cooka.cookapp.model.RecipeStepIngredient;
import app.cooka.cookapp.view.IngredientsView;
import app.cooka.cookapp.view.LoadingScreenView;

public class RecipeEditorActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private LoadingScreenView loadingScreen;

    //The recipe that is currently beeing edited
    private Recipe recipe;

    //View pager and page indicator
    private ViewPager cardViewPager;
    private RecipeEditorCardAdapter cardAdapter;
    private CookModeCardTransformer cardTransformer;

    private PageIndicatorView pageIndicatorView;

    private DialogInterface.OnClickListener deleteStepDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    deleteCurrentStep();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_editor);

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Card View Pager
        cardViewPager = findViewById(R.id.card_view_pager);

        //Adapter
        cardAdapter = new RecipeEditorCardAdapter(this);
        cardAdapter.setDeleteIngredientListener(new IngredientsView.OnDeleteIngredientListener() {
            @Override
            public void onDelete(int index) {
                onDeleteIngredient(index);
            }
        });
        cardAdapter.addItem();

        //Setup view pager margin/padding

        //TODO: Remove hardcoded values
        int topPadding = DensityUtils.dpToPx(18);
        int bottomPadding = DensityUtils.dpToPx(26);
        int horizontalPadding = DensityUtils.dpToPx(14);
        int pageMargin = DensityUtils.dpToPx(10);

        cardViewPager.setAdapter(cardAdapter);
        cardViewPager.setOffscreenPageLimit(3);
        cardViewPager.setPageMargin(pageMargin);
        cardViewPager.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding);

        //Setup card transformer
        cardTransformer = new CookModeCardTransformer(cardViewPager, cardAdapter);
        cardTransformer.enableScaling(true);
        cardViewPager.setPageTransformer(false, cardTransformer);

        //Page Indicator
        pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(cardViewPager);

        //Loading screen
        loadingScreen = findViewById(R.id.loading_screen);
        loadingScreen.hide();

        loadRecipe(11);
    }

    public void fab_onClick(View view) {
        cardAdapter.addItem();
        cardAdapter.notifyDataSetChanged();
        cardViewPager.setCurrentItem(cardAdapter.getCount()-1, true);
    }

    public void currentCardDelete_onClick(View view) {
        showDeleteStepDialog();
    }

    public void addIngredient_onClick(View view) {
        IngredientDialogFragment dialog = new IngredientDialogFragment();
        dialog.show(getSupportFragmentManager(), false, new IngredientDialogFragment.OnSubmitListener() {
            @Override
            public void onSubmit(RecipeStepIngredient ingredient) {
                onAddIngredient(ingredient);
            }
        });

    }

    private void onAddIngredient(RecipeStepIngredient ingredient) {
        getCurrentStep().getRecipeStepIngredients().add(ingredient);
        cardAdapter.refreshIngredients(cardViewPager.getCurrentItem());
    }

    private void onDeleteIngredient(int index) {
        Log.d("Recipe Editor", "Attempting to delete item at index " + index);
        getCurrentStep().getRecipeStepIngredients().remove(index);
        cardAdapter.refreshIngredients(cardViewPager.getCurrentItem());
    }

    private void showDeleteStepDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Dialog);
        builder.setMessage(R.string.delete_step_warning_message).
                setPositiveButton(R.string.yes, deleteStepDialogListener).
                setNegativeButton(R.string.no, deleteStepDialogListener).
                show();
    }

    private void deleteCurrentStep() {
        int itemToRemove = cardViewPager.getCurrentItem();

        cardAdapter.syncSteps();
        cardViewPager.setAdapter(null);
        cardAdapter.removeItem(itemToRemove);

        //Adapter needs to be reinitialised with new list of views
        cardViewPager.setAdapter(cardAdapter);

        cardAdapter.notifyDataSetChanged();
        cardViewPager.setCurrentItem(itemToRemove >= cardAdapter.getCount() ? cardAdapter.getCount()-1 : itemToRemove);
    }

    public void loadRecipe(long id) {
        //Show loading screen before loading
        loadingScreen.show();

        Recipe.Factory.selectRecipe(this, id, new IResultCallback<Recipe>() {
            @Override
            public void onSucceeded(Recipe result) {
                loadRecipe(result);
            }
        });
    }

    public void loadRecipe(Recipe recipe) {
        this.recipe = recipe;

        cardViewPager.setAdapter(null);
        cardAdapter.clear();

        for(RecipeStep step : recipe.getRecipeSteps()) {
            cardAdapter.addItem(step);
        }

        cardViewPager.setAdapter(cardAdapter);

        loadingScreen.hide();
    }

    private void saveRecipe() {
        //Create save recipe dialog
//        SaveRecipeDialogFragment dialog = new SaveRecipeDialogFragment();
//        dialog.show(getSupportFragmentManager(), recipe, new SaveRecipeDialogFragment.OnSubmitListener() {
//            @Override
//            public void onSubmit() {
//
//            }
//        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        SaveRecipeDialogFragment saveFragment = new SaveRecipeDialogFragment();
        saveFragment.setRecipe(recipe);
        saveFragment.setOnSubmitListener(new SaveRecipeDialogFragment.OnSubmitListener() {
            @Override
            public void onSubmit() {

            }
        });
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, saveFragment).addToBackStack(null).commit();
    }

    private RecipeStep getCurrentStep() {
        return recipe.getRecipeSteps().get(cardViewPager.getCurrentItem());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipe_editor_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveRecipe();
                return true;
        }
        return false;
    }
}
