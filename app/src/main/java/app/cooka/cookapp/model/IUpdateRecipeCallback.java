package app.cooka.cookapp.model;

public interface IUpdateRecipeCallback {

    void onSucceeded(UpdateRecipeResult updateRecipeResult, Recipe updatedRecipe);
    void onFailed(UpdateRecipeResult updateRecipeResult);
}
