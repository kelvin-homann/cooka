package app.cooka.cookapp.model;

/**
 * The type of feed message describing what particular action a certain user has performed
 */
public enum EFeedMessageType {

    followedUser,
    followedTag,
    followedCollection,
    createdRecipe,
    modifiedRecipe,
    cookedRecipe,
    createdCollection,
    addedRecipeToCollection,
    addedImageToRecipe,
}
