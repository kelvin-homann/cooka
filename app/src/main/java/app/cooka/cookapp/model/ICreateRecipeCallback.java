package app.cooka.cookapp.model;

public interface ICreateRecipeCallback {

    void onSucceeded(CreateRecipeResult createRecipeResult, Recipe createdRecipe);
    void onFailed();
}
