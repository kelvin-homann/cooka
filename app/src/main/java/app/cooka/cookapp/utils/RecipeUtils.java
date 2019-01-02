package app.cooka.cookapp.utils;



import java.text.DecimalFormat;
import java.util.Locale;

import app.cooka.cookapp.model.RecipeStepIngredient;

public class RecipeUtils {

    /**
     * Converts a given ingredient object to a String that describes amount and unit type
     * of the given ingredient.
     * @param ingredient The ingredient that should be described
     * @param useFullUnitTypeName Whether or not the full name of the unit type should be used
     * @return Output string
     */
    public static String ingredientAmountToString(RecipeStepIngredient ingredient, float amount, boolean useFullUnitTypeName) {
        if(ingredient == null || ingredient.getIngredientAmount() <= 0) return "";

        DecimalFormat format = new DecimalFormat("#.##");
        String amountNum = format.format(amount);

        return String.format(Locale.getDefault(),
                "%s %s", amountNum,
                useFullUnitTypeName ? ingredient.getUnitTypeName() : ingredient.getUnitTypeAbbreviation());
    }

    public static String ingredientAmountToString(RecipeStepIngredient ingredient, float amount) {
        return ingredientAmountToString(ingredient, amount, false);
    }

    public static String ingredientAmountToString(RecipeStepIngredient ingredient) {
        return ingredientAmountToString(ingredient, ingredient.getIngredientAmount(), false);
    }

}
