package app.cooka.cookapp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import app.cooka.cookapp.R;
import app.cooka.cookapp.model.RecipeStepIngredient;
import app.cooka.cookapp.utils.RecipeUtils;

public class RecipeDetailsIngredientsFrameLayout extends FrameLayout {

    private TableLayout frameLayout;

    public RecipeDetailsIngredientsFrameLayout(Context context) {

        super(context);
        initializeView(context, null);
    }

    public RecipeDetailsIngredientsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs);
    }

    public RecipeDetailsIngredientsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context, attrs);
    }

    public RecipeDetailsIngredientsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(context, attrs);
    }

    private void initializeView(Context context, @Nullable AttributeSet attrs) {

        ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.recipe_details_ingredients_framelayout, this);
        frameLayout = findViewById(R.id.recipe_details_ingredients_framelayout);
    }

    public void setIngredients(List<RecipeStepIngredient> ingredients) {

        frameLayout.removeAllViews();
        for(RecipeStepIngredient ingredient : ingredients)
            addIngredient(ingredient);
    }

    public void clearIngredients() {

        frameLayout.removeAllViews();
    }

    public void addIngredient(RecipeStepIngredient ingredient) {

        View item = View.inflate(getContext(), R.layout.recipe_details_ingredients_item, null);

        final String ingredientAmount = RecipeUtils.ingredientAmountToString(ingredient,
            ingredient.getIngredientAmount(), false);
        final String ingredientName = ingredient.getIngredientName() != null &&
            ingredient.getIngredientName().length() > 0 ? ingredient.getIngredientName() :
            ingredient.getCustomUnit();

        TextView tvwIngredientAmount = item.findViewById(R.id.tvwStepNumber);
        TextView tvwIngredientName = item.findViewById(R.id.tvwIngredientName);

        tvwIngredientAmount.setText(ingredientAmount);
        tvwIngredientName.setText(ingredientName);

        frameLayout.addView(item);
    }
}
