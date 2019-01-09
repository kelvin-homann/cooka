package app.cooka.cookapp;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.warkiz.tickseekbar.TickSeekBar;

import app.cooka.cookapp.model.EDifficultyType;
import app.cooka.cookapp.model.EPublicationType;
import app.cooka.cookapp.model.Recipe;

public class SaveRecipeDialogFragment extends DialogFragment {

    private final static EDifficultyType[] DIFFICULTIES = {
            EDifficultyType.SIMPLE,
            EDifficultyType.MODERATE,
            EDifficultyType.DEMANDING};

    private Recipe recipe;
    private OnSubmitListener listener;

    private NestedScrollView mainScrollView;
    private FloatingActionButton fabSaveButton;
    private EditText etRecipeTitle;
    private EditText etRecipeDescription;
    private AppCompatSpinner spRecipeCategory;
    private SwitchCompat swRecipePublic;
    private SwitchCompat swRecipeUnlisted;
    private TickSeekBar tsbRecipeDifficulty;

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
//    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        // Use the Builder class for convenient dialog construction
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.FullScreenDialog);
//        builder.setTitle(R.string.save_recipe).
//                setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        layoutToRecipe();
//                        listener.onSubmit();
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
//
//        //Set custom dialog layout
//        final View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_save_recipe_dialog, null);
//        etRecipeTitle = view.findViewById(R.id.recipe_title);
//        etRecipeDescription = view.findViewById(R.id.recipe_description);
//        spRecipeCategory = view.findViewById(R.id.recipe_category);
//        swRecipePublic = view.findViewById(R.id.recipe_public_private);
//        tsbRecipeDifficulty = view.findViewById(R.id.recipe_difficulty);
//        swRecipePublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                TransitionManager.beginDelayedTransition((ViewGroup)view);
//                swRecipeUnlisted.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
//            }
//        });
//        swRecipeUnlisted = view.findViewById(R.id.recipe_unlisted);
//        builder.setView(view);
//
//        //Load current recipe into layout for editing
//        recipeToLayout();
//
//        // Create the AlertDialog object and return it
//        return builder.create();
//    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_save_recipe_dialog, container, false);

        //Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.save_recipe);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
        }
        setHasOptionsMenu(true);

        //Recipe controls
        mainScrollView = view.findViewById(R.id.main_scroll_view);
        fabSaveButton = view.findViewById(R.id.fab);
        fabSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit(true);
            }
        });
        etRecipeTitle = view.findViewById(R.id.recipe_title);
        etRecipeDescription = view.findViewById(R.id.recipe_description);
        etRecipeDescription.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mainScrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        spRecipeCategory = view.findViewById(R.id.recipe_category);
        swRecipePublic = view.findViewById(R.id.recipe_public_private);
        tsbRecipeDifficulty = view.findViewById(R.id.recipe_difficulty);
        swRecipePublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TransitionManager.beginDelayedTransition((ViewGroup)view);
                swRecipeUnlisted.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            }
        });
        swRecipeUnlisted = view.findViewById(R.id.recipe_unlisted);

        //Load current recipe into layout for editing
        recipeToLayout();

        //Return the created layout
        return view;
    }

    //Save the content of the dialog layout into the recipe
    private void layoutToRecipe() {
        recipe.setTitle(etRecipeTitle.getText().toString());
        recipe.setDescription(etRecipeDescription.getText().toString());
        recipe.setMainCategoryId(spRecipeCategory.getSelectedItemId() + 11);
        recipe.setDifficultyType(DIFFICULTIES[tsbRecipeDifficulty.getProgress()]);
        recipe.setPublicationType(resolvePublicationType());
    }

    //Load the content of the recipe into the dialog layout
    private void recipeToLayout() {
        etRecipeTitle.setText(recipe.getTitle());
        etRecipeDescription.setText(recipe.getDescription());
        spRecipeCategory.setSelection((int)recipe.getMainCategoryId() - 11);
        tsbRecipeDifficulty.setProgress(recipe.getDifficultyType().ordinal());
        swRecipePublic.setChecked(recipe.getPublicationType() != EPublicationType.PRIVATE);
        swRecipeUnlisted.setChecked(recipe.getPublicationType() == EPublicationType.UNLISTED);
        swRecipeUnlisted.setVisibility(swRecipePublic.isChecked() ? View.VISIBLE : View.INVISIBLE);
    }

    private EPublicationType resolvePublicationType() {
        return swRecipePublic.isChecked() ?
                (swRecipeUnlisted.isChecked() ? EPublicationType.UNLISTED : EPublicationType.PUBLIC)
                : EPublicationType.PRIVATE;
    }

    public void show(FragmentManager manager, Recipe recipe, OnSubmitListener listener){
        setRecipe(recipe);
        setOnSubmitListener(listener);
        show(manager, "SaveRecipeDialog");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        //getActivity().getMenuInflater().inflate(R.menu.menu_ak, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // handle close button click here
            exit(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exit(boolean save) {
        if(save) {
            layoutToRecipe();
            if(listener != null) listener.onSubmit();
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public void setOnSubmitListener(OnSubmitListener listener) {
        this.listener = listener;
    }

    public interface OnSubmitListener {
        void onSubmit();
    }
}
