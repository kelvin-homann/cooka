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
    public static String ingredientAmountToString(RecipeStepIngredient ingredient, float amount,
        boolean useFullUnitTypeName)
    {
        if(ingredient == null || ingredient.getIngredientAmount() <= 0) return "";

        String amountNum = "";

        if(amount > 0.124 && amount < 0.126)        // 1/8
            amountNum = "⅛";
        else if(amount > 0.162 && amount < 0.168)   // 1/6
            amountNum = "⅙";
        else if(amount > 0.198 && amount < 0.202)   // 1/5
            amountNum = "⅕";
        else if(amount > 0.248 && amount < 0.252)   // 1/4
            amountNum = "¼";
        else if(amount > 0.331 && amount < 0.335)   // 1/3
            amountNum = "⅓";
        else if(amount > 0.398 && amount < 0.402)   // 2/5
            amountNum = "⅖";
        else if(amount > 0.498 && amount < 0.502)   // 1/2
            amountNum = "½";
        else if(amount > 0.598 && amount < 0.602)   // 3/5
            amountNum = "⅗";
        else if(amount > 0.664 && amount < 0.668)   // 2/3
            amountNum = "⅔";
        else if(amount > 0.798 && amount < 0.802)   // 4/5
            amountNum = "⅘";
        else if(amount > 0.831 && amount < 0.835)   // 5/6
            amountNum = "⅚";
        else if(amount > 0.873 && amount < 0.877)   // 7/8
            amountNum = "⅞";
        else {
            DecimalFormat format = new DecimalFormat("#.##");
            amountNum = format.format(amount);
        }

        if(ingredient.getUnitTypeId() > 0) {
            return String.format(Locale.getDefault(), "%s %s", amountNum,
                useFullUnitTypeName ? ingredient.getUnitTypeName() : ingredient.getUnitTypeAbbreviation());
        }
        else if(ingredient.getCustomUnit() != null && ingredient.getCustomUnit().length() > 0) {
            return String.format(Locale.getDefault(), "%s %s", amountNum, ingredient.getCustomUnit());
        }
        else {
            return String.format(Locale.getDefault(), "%s", amountNum);
        }
    }

    public static String ingredientAmountToString(RecipeStepIngredient ingredient, float amount) {
        return ingredientAmountToString(ingredient, amount, false);
    }

    public static String ingredientAmountToString(RecipeStepIngredient ingredient) {
        return ingredientAmountToString(ingredient, ingredient.getIngredientAmount(), false);
    }

}
