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
        int whole = (int)amount;
        float fraction = amount % 1;

        // if amount has fractions
        if(fraction != 0) {

            if(whole > 0)
                amountNum = String.format("%d", whole);

            if(fraction > 0.124 && fraction < 0.126)        // 1/8
                amountNum += "⅛";
            else if(fraction > 0.162 && fraction < 0.168)   // 1/6
                amountNum += "⅙";
            else if(fraction > 0.198 && fraction < 0.202)   // 1/5
                amountNum += "⅕";
            else if(fraction > 0.248 && fraction < 0.252)   // 1/4
                amountNum += "¼";
            else if(fraction > 0.331 && fraction < 0.335)   // 1/3
                amountNum += "⅓";
            else if(fraction > 0.398 && fraction < 0.402)   // 2/5
                amountNum += "⅖";
            else if(fraction > 0.498 && fraction < 0.502)   // 1/2
                amountNum += "½";
            else if(fraction > 0.598 && fraction < 0.602)   // 3/5
                amountNum += "⅗";
            else if(fraction > 0.664 && fraction < 0.668)   // 2/3
                amountNum += "⅔";
            else if(fraction > 0.798 && fraction < 0.802)   // 4/5
                amountNum += "⅘";
            else if(fraction > 0.831 && fraction < 0.835)   // 5/6
                amountNum += "⅚";
            else if(fraction > 0.873 && fraction < 0.877)   // 7/8
                amountNum += "⅞";
            else {
                DecimalFormat format = new DecimalFormat("#.##");
                amountNum = format.format(amount);
            }
        }
        else {
            amountNum = String.format("%d", (int)amount);
        }

        if(ingredient.getUnitTypeId() > 0) {
            return String.format(Locale.getDefault(), "%s %s", amountNum,
                useFullUnitTypeName || ingredient.getUnitTypeAbbreviation().length() == 0 ?
                    ingredient.getUnitTypeName() : ingredient.getUnitTypeAbbreviation());
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
