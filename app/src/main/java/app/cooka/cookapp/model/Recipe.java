package app.cooka.cookapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * A class that represents a recipe and that is an app model representation of the corresponding
 * database entity called Recipes. Used to serialize between app and database and to cache its
 * state without needing to repeatedly query the same unchanged object from the database.
 */
@JsonAdapter(Recipe.JsonAdapter.class)
public class Recipe extends java.util.Observable {

    public static final String LOGTAG = "COOKALOG";

    // change states
    public static final int CHST_TITLE = 0x00000001;
    public static final int CHST_DESCRIPTION = 0x00000002;
    public static final int CHST_CREATOR = 0x00000004;
    public static final int CHST_MAINIMAGE = 0x00000008;
    public static final int CHST_MAINCATEGORY = 0x00000010;
    public static final int CHST_CATEGORIES = 0x00000020;
    public static final int CHST_TAGS = 0x00000040;
    public static final int CHST_PUBLICATIONTYPE = 0x00000080;
    public static final int CHST_DIFFICULTYTYPE = 0x00000100;
    public static final int CHST_PREPARATIONTIME = 0x00000200;
    public static final int CHST_RECIPESTEPS = 0x00000400;
    public static final int CHST_FLAGS = 0x00000800;
    public static final int CHST_FORCE_UPDATE = 0xffffffff;

    // flags
    public static final int FLAG_MOCKUP = 0x00000001;
    public static final int FLAG_UNCHANGED_COPY = 0x00000002;
    public static final int FLAG_HIGHLIGHTED = 0x00000004;
    public static final int FLAG_HIDDEN = 0x00000008;
    public static final int FLAG_DRAFT = 0x00000010;
    public static final int FLAG_DELETED = 0x00000020;

    private int changeState = 0;
    private boolean committed = false;
    private int flags = 0;

    private long recipeId;
    private long languageId;
    private String title;
    private String description;
    private long originalRecipeId;
    private String originalRecipeTitle;
    private long creatorId;
    private String creatorName;
    private long mainImageId;
    private String mainImageFileName;
    private Bitmap mainImage;
    private long mainCategoryId;
    private String mainCategoryName;

    private EPublicationType publicationType;
    private EDifficultyType difficultyType;
    private int preparationTime;

    private int viewedCount;
    private int cookedCount;
    private int pinnedCount;
    private int modifiedCount;
    private int variedCount;
    private int sharedCount;

    private float rating;

    private Date createdDateTime;
    private Date lastModifiedDateTime;
    private Date lastCookedDateTime;

    private List<Category> categories;
    private int numCategoriesRequested = -1;
    private List<Tag> tags;
    private int numTagsRequested = -1;
    private List<RecipeStepIngredient> ingredients;
    private int numIngredientsRequested = -1;
    private List<RecipeStep> recipeSteps;
    private int numRecipeStepsRequested = -1;
//    private List<Rating> recipeRatings;
//    private int numRecipeRatingsRequested = 0;
    private List<Image> recipeImages;
    private int numRecipeImagesRequested = 0;
    private List<Recipe> similarRecipes;
    private int numSimilarRecipesRequested = 0;

    private Recipe(final long recipeId, long languageId, String title, String description) {

        this.recipeId = recipeId;
        this.languageId = languageId;
        this.title = title;
        this.description = description;
    }

    private Recipe(final long recipeId, long languageId, String title, String description,
        long originalRecipeId, String originalRecipeTitle, long creatorId, String creatorName,
        long mainImageId, String mainImageFileName, long mainCategoryId, String mainCategoryName,
        String publicationType, String difficultyType, int preparationTime, int viewedCount,
        int cookedCount, int pinnedCount, int modifiedCount, int variedCount, int sharedCount,
        float rating, String createdDateTime, String lastModifiedDateTime,
        String lastCookedDateTime)
    {
        this.recipeId = recipeId;
        this.languageId = languageId;
        this.title = title;
        this.description = description;
        this.originalRecipeId = originalRecipeId;
        this.originalRecipeTitle = originalRecipeTitle;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.mainImageId = mainImageId;
        this.mainImageFileName = mainImageFileName;
        this.mainCategoryId = mainCategoryId;
        this.mainCategoryName = mainCategoryName;

        try {
            if(publicationType.length() > 0)
                this.publicationType = EPublicationType.valueOf(publicationType.toUpperCase());
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse publicationType while creating recipe %d: %s. Set to null instead.", recipeId, title));
        }

        try {
            if(difficultyType.length() > 0)
                this.difficultyType = EDifficultyType.valueOf(difficultyType.toUpperCase());
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse difficultyType while creating recipe %d: %s. Set to null instead.", recipeId, title));
        }

        this.preparationTime = preparationTime;

        this.viewedCount = viewedCount;
        this.cookedCount = cookedCount;
        this.pinnedCount = pinnedCount;
        this.modifiedCount = modifiedCount;
        this.variedCount = variedCount;
        this.sharedCount = sharedCount;

        this.rating = rating;

        try {
            if(createdDateTime.length() > 0)
                this.createdDateTime = DatabaseClient.databaseDateFormat.parse(createdDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse createdDateTime while creating recipe %d: %s. Set to null instead.", recipeId, title));
        }

        try {
            if(lastModifiedDateTime.length() > 0)
                this.lastModifiedDateTime = DatabaseClient.databaseDateFormat.parse(lastModifiedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastModifiedDateTime while creating recipe %d: %s. Set to null instead.", recipeId, title));
        }

        try {
            if(lastCookedDateTime.length() > 0)
                this.lastCookedDateTime = DatabaseClient.databaseDateFormat.parse(lastCookedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastCookedDateTime while creating recipe %d: %s. Set to null instead.", recipeId, title));
        }
    }

    private Recipe(final long recipeId, long languageId, String title, long creatorId,
        String creatorName, long mainImageId, String mainImageFileName, long mainCategoryId,
        String mainCategoryName, String difficultyType, int preparationTime, int cookedCount,
        int pinnedCount, float rating)
    {
        this.recipeId = recipeId;
        this.languageId = languageId;
        this.title = title;
        this.description = "";
        this.originalRecipeId = 0;
        this.originalRecipeTitle = "";
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.mainImageId = mainImageId;
        this.mainImageFileName = mainImageFileName;
        this.mainCategoryId = mainCategoryId;
        this.mainCategoryName = mainCategoryName;

        this.publicationType = EPublicationType.PUBLIC;

        try {
            if(difficultyType.length() > 0)
                this.difficultyType = EDifficultyType.valueOf(difficultyType.toUpperCase());
        }
        catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse difficultyType while creating recipe %d: %s. Set to null instead.", recipeId, title));
        }

        this.preparationTime = preparationTime;

        this.viewedCount = 0;
        this.cookedCount = cookedCount;
        this.pinnedCount = pinnedCount;
        this.modifiedCount = 0;
        this.variedCount = 0;
        this.sharedCount = 0;

        this.rating = rating;
    }

    // only used for recipe draft creation by the Factory
    private Recipe(long languageId) {

        this.recipeId = -1;
        this.languageId = languageId;
        this.changeState = CHST_FORCE_UPDATE;
        this.committed = true;
    }

    /**
     * A recipe factory class that does recipe creation, selection and database serialization.
     */
    public static class Factory {

        /**
         * Creates a recipe draft and returns an empty recipe object to be filled with data by
         * during the recipe creation process within the recipe editor.
         * The filled object will then have to be passed to the
         * {@linkplain Recipe.Factory#submitRecipe(Context, Recipe, boolean, ICreateRecipeCallback)}
         * method to be stored into the database.
         * @return an empty recipe object for manual data fill.
         */
        public static Recipe createRecipeDraft(final long languageId) {

            return new Recipe(languageId);
        }

        /**
         * Submits a previously created recipe draft in form of a recipe object for insertion into
         * the database. Does only work with recipe objects created by the
         * {@linkplain Factory#createRecipeDraft(long languageId)} method. The recipe draft object
         * needs to have a title set and needs to contain at least one recipe step before it can be
         * submitted.
         * @param context the Android context to run this method in.
         * @param recipeDraft the recipe object to be inserted into the database.
         * @param ignoreDuplicate whether or not to ignore duplicate recipes when there is already
         *      a recipe with the exact same title and description (default: false).
         * @param createRecipeCallback the create recipe callback that will be called when the
         *      insertion succeeded and that will be given the transformed recipe object that was
         *      a draft instance previously.
         */
        public static void submitRecipe(final Context context, final Recipe recipeDraft,
            boolean ignoreDuplicate, final ICreateRecipeCallback createRecipeCallback)
        {
            if(createRecipeCallback == null) {
                throw new NullPointerException("create recipe callback is null");
            }
            if(recipeDraft == null) {
                throw new NullPointerException("recipe draft object is null");
            }
            if(recipeDraft.recipeId != -1) {
                CreateRecipeResult createRecipeResult = new CreateRecipeResult();
                createRecipeResult.resultCode = 101000;
                createRecipeResult.resultMessage = "the given recipe object is not a valid recipe draft object";
                createRecipeCallback.onFailed(createRecipeResult);
                return;
            }
            if(recipeDraft.title == null || recipeDraft.title.length() == 0) {
                CreateRecipeResult createRecipeResult = new CreateRecipeResult();
                createRecipeResult.resultCode = 102000;
                createRecipeResult.resultMessage = "the given recipe object does not have a title set";
                createRecipeCallback.onFailed(createRecipeResult);
                return;
            }
            if(recipeDraft.recipeSteps == null || recipeDraft.recipeSteps.size() == 0) {
                CreateRecipeResult createRecipeResult = new CreateRecipeResult();
                createRecipeResult.resultCode = 103000;
                createRecipeResult.resultMessage = "the given recipe object does not contain any recipe steps";
                createRecipeCallback.onFailed(createRecipeResult);
                return;
            }

            Call<CreateRecipeResult> call = DatabaseClient.Factory
                .getInstance(context)
                .createRecipe(recipeDraft, ignoreDuplicate);

            if(call == null) {
                CreateRecipeResult createRecipeResult = new CreateRecipeResult();
                createRecipeResult.resultCode = 110000;
                createRecipeResult.resultMessage = "call could not be created; please verify that the user id in the login information store is set";
                createRecipeCallback.onFailed(createRecipeResult);
                return;
            }

            call.enqueue(new Callback<CreateRecipeResult>() {
                @Override
                public void onResponse(Call<CreateRecipeResult> call, Response<CreateRecipeResult> response) {
                    CreateRecipeResult createRecipeResult = response.body();
                    if(createRecipeResult != null && createRecipeResult.resultCode == 0) {
                        recipeDraft.recipeId = createRecipeResult.recipeId;
                        recipeDraft.resetChangeState();
                        recipeDraft.resetCommitted();
                        // todo: register new recipe at the update observer
                        createRecipeCallback.onSucceeded(createRecipeResult, recipeDraft);
                    }
                    else {
                        createRecipeCallback.onFailed(createRecipeResult);
                    }
                }

                @Override
                public void onFailure(Call<CreateRecipeResult> call, Throwable t) {
                    Log.e(LOGTAG, "Recipe.Factory.submitRecipe() failed");
                    Log.e(LOGTAG, t.getMessage());
                    createRecipeCallback.onFailed(null);
                }
            });
        }

        /**
         * Updates and synchronizes an existing recipe with its database counterpart.
         * @param context the Android context to run this method in.
         * @param recipe the recipe object to be updated in the database.
         * @param updateRecipeCallback the create recipe callback that will be called when the
         *      insertion succeeded and that will be given the transformed recipe object that was
         *      a draft instance previously.
         */
        public static void updateRecipe(final Context context, final Recipe recipe,
            final IUpdateRecipeCallback updateRecipeCallback)
        {
            if(updateRecipeCallback == null) {
                throw new NullPointerException("update recipe callback is null");
            }
            if(recipe == null) {
                throw new NullPointerException("recipe object is null");
            }
            if(recipe.recipeId == -1) {
                UpdateRecipeResult updateRecipeResult = new UpdateRecipeResult();
                updateRecipeResult.resultCode = 101000;
                updateRecipeResult.resultMessage = "the given recipe object is a recipe draft object and needs database insertion first";
                updateRecipeCallback.onFailed(updateRecipeResult);
                return;
            }

            Call<UpdateRecipeResult> call = DatabaseClient.Factory
                .getInstance(context)
                .updateRecipe(recipe);

            if(call == null) {
                UpdateRecipeResult updateRecipeResult = new UpdateRecipeResult();
                updateRecipeResult.resultCode = 110000;
                updateRecipeResult.resultMessage = "call could not be created; please verify that the user id in the login information store is set";
                updateRecipeCallback.onFailed(updateRecipeResult);
                return;
            }

            call.enqueue(new Callback<UpdateRecipeResult>() {
                @Override
                public void onResponse(Call<UpdateRecipeResult> call, Response<UpdateRecipeResult> response) {
                    UpdateRecipeResult updateRecipeResult = response.body();
                    if(updateRecipeResult != null && updateRecipeResult.resultCode == 0) {
                        recipe.resetChangeState();
                        recipe.resetCommitted();
                        updateRecipeCallback.onSucceeded(updateRecipeResult, recipe);
                    }
                    else {
                        updateRecipeCallback.onFailed(updateRecipeResult);
                    }
                }

                @Override
                public void onFailure(Call<UpdateRecipeResult> call, Throwable t) {
                    Log.e(LOGTAG, "Recipe.Factory.updateRecipe() failed");
                    Log.e(LOGTAG, t.getMessage());
                    updateRecipeCallback.onFailed(null);
                }
            });
        }

        /**
         * Selects a recipe from the database given its recipe identifier.
         * @param context the Android context to run this method in.
         * @param recipeId the identifier of the recipe to be selected.
         * @param selectRecipeCallback the select callback that will be called when the
         *      selection succeeded and that will be given the recipe object.
         * @return a subscription object to the select request; null if an error occurred.
         */
        public static Subscription selectRecipe(final Context context, final long recipeId,
            final IResultCallback<Recipe> selectRecipeCallback)
        {
            if(selectRecipeCallback == null) {
                throw new NullPointerException("select recipe callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectRecipe(recipeId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Recipe>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Recipe.Factory.selectRecipes() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(Recipe recipe) {
                        // todo: register recipe at the update observer
                        selectRecipeCallback.onSucceeded(recipe);
                    }
                });
        }

        /**
         * Selects a recipe from the database given its recipe identifier.
         * @param context the Android context to run this method in.
         * @param recipeId the identifier of the recipe to be selected.
         * @param numCategoriesRequested the maximum number of categories associated with this
         *      recipe to be fetched with the recipe.
         * @param numTagsRequested the maximum number of tags that this recipe is tagged with to be
         *      fetched with the recipe.
         * @param numRecipeStepsRequested the maximum number of recipe steps to be fetched with the
         *      recipe.
         * @param numRecipeRatingsRequested the maximum number of recipe ratings to be fetched with
         *      the recipe.
         * @param selectRecipeCallback the select callback that will be called when the
         *      selection succeeded and that will be given the recipe object.
         * @return a subscription object to the select request; null if an error occurred.
         */
        public static Subscription selectRecipe(final Context context, final long recipeId,
            final int numCategoriesRequested, final int numTagsRequested,
            final int numRecipeStepsRequested, final int numRecipeRatingsRequested,
            final IResultCallback<Recipe> selectRecipeCallback)
        {
            if(selectRecipeCallback == null) {
                throw new NullPointerException("select recipe callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectRecipe(recipeId, numCategoriesRequested, numTagsRequested, 0,
                    numRecipeStepsRequested, numRecipeRatingsRequested, 0, 0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Recipe>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Recipe.Factory.selectRecipes() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(Recipe recipe) {
                        // todo: register recipe at the update observer
                        selectRecipeCallback.onSucceeded(recipe);
                    }
                });
        }

        /**
         * Selects a recipe from the database given its recipe identifier.
         * @param context the Android context to run this method in.
         * @param recipeId the identifier of the recipe to be selected.
         * @param numCategoriesRequested the maximum number of categories associated with this
         *      recipe to be fetched with the recipe.
         * @param numTagsRequested the maximum number of tags that this recipe is tagged with to be
         *      fetched with the recipe.
         * @param numIngredientsRequested the maximum number of ingredients to be fetched with the
         *      recipe.
         * @param numRecipeStepsRequested the maximum number of recipe steps to be fetched with the
         *      recipe.
         * @param numRecipeRatingsRequested the maximum number of recipe ratings to be fetched with
         *      the recipe.
         * @param numRecipeImagesRequested the maximum number of additional recipe images to be
         *      fetched with the recipe.
         * @param numSimilarRecipesRequested the maximum number of similar recipes to be fetched
         *      with the recipe.
         * @param selectRecipeCallback the select callback that will be called when the
         *      selection succeeded and that will be given the recipe object.
         * @return a subscription object to the select request; null if an error occurred.
         */
        public static Subscription selectRecipe(final Context context, final long recipeId,
            final int numCategoriesRequested, final int numTagsRequested, final int
            numIngredientsRequested, final int numRecipeStepsRequested, final int
            numRecipeRatingsRequested, final int numRecipeImagesRequested, final int
            numSimilarRecipesRequested, final IResultCallback<Recipe> selectRecipeCallback)
        {
            if(selectRecipeCallback == null) {
                throw new NullPointerException("select recipe callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectRecipe(recipeId, numCategoriesRequested, numTagsRequested,
                    numIngredientsRequested, numRecipeStepsRequested, numRecipeRatingsRequested,
                    numRecipeImagesRequested, numSimilarRecipesRequested)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Recipe>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Recipe.Factory.selectRecipe() failed");
                        Log.e(LOGTAG, e.getMessage());
                    }
                    @Override public void onNext(Recipe recipe) {
                        // todo: register recipe at the update observer
                        selectRecipeCallback.onSucceeded(recipe);
                    }
                });
        }

        /**
         * Select recipes from the database filtered by the given filter keys and sorted by the
         * given sort keys (see example).
         *
         * @param context the Android context to run this method in.
         * @param filterKeys a list of strings representing "key:value" pairs that will be combined
         *      to filter your recipe results. The following keys and their values are possible:
         *      <li> contains:{text} (text: search string) </li>
         *      <li> creatorId:{integer} (integer: user id) </li>
         *      <li> difficulty:{simple|moderate|demanding} </li>
         *      <li> category:{text|integer} (text: search string; integer: category id) </li>
         *      <li> hasImage:{true|false} </li>
         *      <li> wasVaried:{true|false} </li>
         *      <li> flags:{integer} (exact combination of flags) </li>
         *      <li> hasFlags:{integer} (contains given flags) </li>
         * @param sortKeys a list of strings representing "key:value" pairs that will be combined
         *      to build different levels of order in the recipe results. Value is either "asc"
         *      or "desc". The following sort keys are possible:
         *      <li> title </li>
         *      <li> difficulty </li>
         *      <li> preparationTime </li>
         *      <li> cookedCount </li>
         *      <li> pinnedCount </li>
         *      <li> rating </li>
         *      <li> fairRating </li>
         *      <li> trendRating </li>
         *      <li> created </li>
         *      <li> lastCooked </li>
         * @param limit the maximum number of recipes to fetch from the database
         * @param offset the offset within the result set to start fetching the recipes from (i.e.
         *      limit 10 and offset 11 selects recipes 11 to 20)
         * @param selectRecipesCallback the select callback that will be called when the selection
         *      succeeded and that will be given the recipe object.
         * @return a subscription object to the select request; null if an error occurred.
         */
        public static Subscription selectRecipes(final Context context, final List<String>
            filterKeys, final List<String> sortKeys, final long limit, final long offset,
            final IResultCallback<List<Recipe>> selectRecipesCallback)
        {
            if(selectRecipesCallback == null) {
                throw new NullPointerException("select recipes callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectRecipes(filterKeys, sortKeys, limit, offset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Recipe>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Recipe.Factory.selectRecipes() failed");
                        Log.e(LOGTAG, e.getMessage());
                        selectRecipesCallback.onFailed(e);
                    }
                    @Override public void onNext(List<Recipe> recipes) {
                        // todo: register all recipes at the update observer
                        selectRecipesCallback.onSucceeded(recipes);
                    }
                });
        }

        public static Subscription selectFeedRecipes(final Context context,
            final IResultCallback<List<Recipe>> selectRecipesCallback)
        {
            if(selectRecipesCallback == null) {
                throw new NullPointerException("select recipes callback is null");
            }
            return DatabaseClient.Factory.getInstance(context)
                .selectFeedRecipes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Recipe>>() {
                    @Override public void onCompleted() {}
                    @Override public void onError(Throwable e) {
                        Log.e(LOGTAG, "Recipe.Factory.selectFeedRecipes() failed");
                        Log.e(LOGTAG, e.getMessage());
                        selectRecipesCallback.onFailed(e);
                    }
                    @Override public void onNext(List<Recipe> recipes) {
                        // todo: register all recipes at the update observer
                        selectRecipesCallback.onSucceeded(recipes);
                    }
                });
        }
    }

    /**
     * Gets the change state of this recipe instance that reflects what fields have changed
     * since the last synchronization. Basically an extended dirty flag.
     * @return the change state bit field
     */
    public long getChangeState() {
        return changeState;
    }

    /**
     * Reset the change state of this recipe as well as its recipe steps and recipe step ingredients
     * to denote that this recipe instance is up-to-date and synchronized with its database
     * counterpart. This method should be called after a recipe has been successfully submitted to,
     * created or updated in the database.
     */
    public void resetChangeState() {
        changeState = 0;
        if(recipeSteps != null) for(RecipeStep recipeStep : recipeSteps) {
            recipeStep.resetChangeState();
        }
    }

    public void setForceUpdate() {
        changeState = CHST_FORCE_UPDATE;
    }

    public boolean getCommited() {
        return committed;
    }

    public void resetCommitted() {
        this.committed = false;
    }

    public void commit() {
        this.committed = true;
    }

    @SuppressWarnings("unused")
    public int getFlags() {
        return flags;
    }

    @SuppressWarnings("unused")
    public void setFlags(int flags) {
        this.flags = flags;
        changeState |= CHST_FLAGS;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public void addFlags(int flags) {
        this.flags |= flags;
        changeState |= CHST_FLAGS;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public void removeFlags(int flags) {
        this.flags ^= flags;
        changeState |= CHST_FLAGS;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public long getRecipeId() {
        return recipeId;
    }

    @SuppressWarnings("unused")
    public String getTitle() {
        return title;
    }

    @SuppressWarnings("unused")
    public void setTitle(String title) {
        this.title = title;
        changeState |= CHST_TITLE;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unused")
    public void setDescription(String description) {
        this.description = description;
        changeState |= CHST_DESCRIPTION;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public long getOriginalRecipeId() {
        return originalRecipeId;
    }

    @SuppressWarnings("unused")
    public void setOriginalRecipeId(long originalRecipeId) {
        this.originalRecipeId = originalRecipeId;
    }

    @SuppressWarnings("unused")
    public String getOriginalRecipeTitle() {
        return originalRecipeTitle;
    }

    @SuppressWarnings("unused")
    public long getCreatorId() {
        return creatorId;
    }

    @SuppressWarnings("unused")
    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
        changeState |= CHST_CREATOR;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public String getCreatorName() {
        return creatorName;
    }

    @SuppressWarnings("unused")
    public long getMainImageId() {
        return mainImageId;
    }

    @SuppressWarnings("unused")
    public void setMainImageId(long mainImageId) {
        this.mainImageId = mainImageId;
        changeState |= CHST_MAINIMAGE;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public String getMainImageFileName() {
        return mainImageFileName;
    }

    @SuppressWarnings("unused")
    public void setMainImageFileName(String mainImageFileName) {
        this.mainImageFileName = mainImageFileName;
        changeState |= CHST_MAINIMAGE;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    private void downloadMainImageFileName(String mainImageFileName) {

        if(this.mainImageFileName == null || this.mainImageFileName.compareTo(mainImageFileName) != 0) {
            this.mainImageFileName = mainImageFileName;
            changeState |= CHST_MAINIMAGE;

            if(mainImageFileName.length() > 0) {
                String imageUrl = "https://www.sebastianzander.de/cooka/img/" + mainImageFileName;
                new DownloadImageTask(imageUrl, this).execute();
            }

            setChanged();
            notifyObservers();
        }
    }

    @SuppressWarnings("unused")
    public Bitmap getMainImage() {
        return mainImage;
    }

    @SuppressWarnings("unused")
    public void setMainImage(Bitmap mainImage) {
        this.mainImage = mainImage;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public long getMainCategoryId() {
        return mainCategoryId;
    }

    @SuppressWarnings("unused")
    public void setMainCategoryId(long mainCategoryId) {
        this.mainCategoryId = mainCategoryId;
        changeState |= CHST_MAINCATEGORY;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public String getMainCategoryName() {
        return mainCategoryName;
    }

    @SuppressWarnings("unused")
    public void setMainCategoryName(String mainCategoryName) {
        this.mainCategoryName = mainCategoryName;
        changeState |= CHST_MAINCATEGORY;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public EPublicationType getPublicationType() {
        return publicationType;
    }

    @SuppressWarnings("unused")
    public void setPublicationType(EPublicationType publicationType) {
        this.publicationType = publicationType;
        changeState |= CHST_PUBLICATIONTYPE;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public EDifficultyType getDifficultyType() {
        return difficultyType;
    }

    @SuppressWarnings("unused")
    public void setDifficultyType(EDifficultyType difficultyType) {
        this.difficultyType = difficultyType;
        changeState |= CHST_DIFFICULTYTYPE;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public int getPreparationTime() {
        return preparationTime;
    }

    @SuppressWarnings("unused")
    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
        changeState |= CHST_PREPARATIONTIME;
        setChanged();
        notifyObservers();
    }

    @SuppressWarnings("unused")
    public int getViewedCount() {
        return viewedCount;
    }

    @SuppressWarnings("unused")
    public void setViewedCount(int viewedCount) {
        this.viewedCount = viewedCount;
    }

    @SuppressWarnings("unused")
    public int getCookedCount() {
        return cookedCount;
    }

    @SuppressWarnings("unused")
    public void setCookedCount(int cookedCount) {
        this.cookedCount = cookedCount;
    }

    @SuppressWarnings("unused")
    public int getPinnedCount() {
        return pinnedCount;
    }

    @SuppressWarnings("unused")
    public void setPinnedCount(int pinnedCount) {
        this.pinnedCount = pinnedCount;
    }

    @SuppressWarnings("unused")
    public int getModifiedCount() {
        return modifiedCount;
    }

    @SuppressWarnings("unused")
    public void setModifiedCount(int modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    @SuppressWarnings("unused")
    public int getVariedCount() {
        return variedCount;
    }

    @SuppressWarnings("unused")
    public void setVariedCount(int variedCount) {
        this.variedCount = variedCount;
    }

    @SuppressWarnings("unused")
    public int getSharedCount() {
        return sharedCount;
    }

    @SuppressWarnings("unused")
    public void setSharedCount(int sharedCount) {
        this.sharedCount = sharedCount;
    }

    @SuppressWarnings("unused")
    public float getRating() {
        return rating;
    }

    @SuppressWarnings("unused")
    public void setRating(float rating) {
        this.rating = rating;
    }

    @SuppressWarnings("unused")
    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    @SuppressWarnings("unused")
    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    @SuppressWarnings("unused")
    public Date getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    @SuppressWarnings("unused")
    public void setLastModifiedDateTime(Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    @SuppressWarnings("unused")
    public Date getLastCookedDateTime() {
        return lastCookedDateTime;
    }

    @SuppressWarnings("unused")
    public void setLastCookedDateTime(Date lastCookedDateTime) {
        this.lastCookedDateTime = lastCookedDateTime;
    }

    @SuppressWarnings("unused")
    public List<Category> getCategories() {
        return categories;
    }

    @SuppressWarnings("unused")
    public void setCategories(List<Category> categories) {
        this.categories = categories;
        changeState |= CHST_CATEGORIES;
    }

    @SuppressWarnings("unused")
    public int getNumCategoriesRequested() {
        return numCategoriesRequested;
    }

    @SuppressWarnings("unused")
    public List<Tag> getTags() {
        return tags;
    }

    @SuppressWarnings("unused")
    public void setTags(List<Tag> tags) {
        this.tags = tags;
        changeState |= CHST_TAGS;
    }

    @SuppressWarnings("unused")
    public int getNumTagsRequested() {
        return numTagsRequested;
    }

    @SuppressWarnings("unused")
    public List<RecipeStepIngredient> getIngredients() {
        return ingredients;
    }

    @SuppressWarnings("unused")
    public void setIngredients(List<RecipeStepIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    @SuppressWarnings("unused")
    public int getNumIngredientsRequested() {
        return numIngredientsRequested;
    }

    @SuppressWarnings("unused")
    public List<RecipeStep> getRecipeSteps() {
        return recipeSteps;
    }

    @SuppressWarnings("unused")
    public void setRecipeSteps(List<RecipeStep> recipeSteps) {
        this.recipeSteps = recipeSteps;
        changeState |= CHST_RECIPESTEPS;
    }

    @SuppressWarnings("unused")
    public int getNumRecipeStepsRequested() {
        return numRecipeStepsRequested;
    }

    @SuppressWarnings("unused")
    public List<Image> getRecipeImages() {
        return recipeImages;
    }

    @SuppressWarnings("unused")
    public void setRecipeImages(List<Image> recipeImages) {
        this.recipeImages = recipeImages;
    }

    @SuppressWarnings("unused")
    public int getNumRecipeImagesRequested() {
        return numRecipeImagesRequested;
    }

    @SuppressWarnings("unused")
    public List<Recipe> getSimilarRecipes() {
        return similarRecipes;
    }

    @SuppressWarnings("unused")
    public void setSimilarRecipes(List<Recipe> similarRecipes) {
        this.similarRecipes = similarRecipes;
    }

    @SuppressWarnings("unused")
    public int getNumSimilarRecipesRequested() {
        return numSimilarRecipesRequested;
    }

    /**
     * A simple asynchronous image download task.
     */
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        String imageUrl;
        Recipe recipe;

        /**
         * @param imageUrl the URL of the image to be downloaded
         * @param recipe the recipe to assign the resulting main image Bitmap to
         */
        public DownloadImageTask(String imageUrl, Recipe recipe) {
            this.imageUrl = imageUrl;
            this.recipe = recipe;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            try {
                InputStream in = new java.net.URL(imageUrl).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            }
            catch(Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bitmap;
        }

        /**
         * Assigns the downloaded image in form of a bitmap to the referenced recipe object.
         * @param bitmap the downloaded image returned by doInBackground()
         */
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null && recipe != null) {
                recipe.setMainImage(bitmap);
            }
        }
    }

    /**
     * A JSON type adapter that is responsible for Recipe object serialization
     */
    public static class JsonAdapter extends TypeAdapter<Recipe> {

        @Override
        public void write(JsonWriter out, Recipe recipe) throws IOException {
            out.beginObject();

            out.name("recipeId");
            out.value(recipe.recipeId);

            out.name("languageId");
            out.value(recipe.languageId);

            if((recipe.changeState & Recipe.CHST_TITLE) == Recipe.CHST_TITLE) {
                out.name("title");
                out.value(recipe.title);
            }

            if((recipe.changeState & Recipe.CHST_DESCRIPTION) == Recipe.CHST_DESCRIPTION) {
                out.name("description");
                out.value(recipe.description != null ? recipe.description : "");
            }

            out.name("originalRecipeId");
            out.value(recipe.originalRecipeId);

            out.name("originalRecipeTitle");
            out.value(recipe.originalRecipeTitle);

            out.name("creatorId");
            out.value(recipe.creatorId);

            out.name("creatorName");
            out.value(recipe.creatorName);

            if((recipe.changeState & Recipe.CHST_MAINIMAGE) == Recipe.CHST_MAINIMAGE) {
                out.name("mainImageId");
                out.value(recipe.mainImageId);
                out.name("mainImageFileName");
                out.value(recipe.mainImageFileName != null ? recipe.mainImageFileName : "");
            }

            if((recipe.changeState & Recipe.CHST_MAINCATEGORY) == Recipe.CHST_MAINCATEGORY) {
                out.name("mainCategoryId");
                out.value(recipe.mainCategoryId);
                out.name("mainCategoryName");
                out.value(recipe.mainCategoryName != null ? recipe.mainCategoryName : "");
            }

            if((recipe.changeState & Recipe.CHST_PUBLICATIONTYPE) == Recipe.CHST_PUBLICATIONTYPE) {
                out.name("publicationType");
                out.value(recipe.publicationType.toString().toLowerCase());
            }

            if((recipe.changeState & Recipe.CHST_DIFFICULTYTYPE) == Recipe.CHST_DIFFICULTYTYPE) {
                out.name("difficultyType");
                out.value(recipe.difficultyType.toString().toLowerCase());
            }

            if((recipe.changeState & Recipe.CHST_PREPARATIONTIME) == Recipe.CHST_PREPARATIONTIME) {
                out.name("preparationTime");
                out.value(recipe.preparationTime);
            }

            if((recipe.changeState & Recipe.CHST_FLAGS) == Recipe.CHST_FLAGS) {
                out.name("flags");
                out.value(recipe.flags);
            }

            /*if(recipe.viewedCount > 0) {
                out.name("viewedCount");
                out.value(recipe.viewedCount);
            }

            if(recipe.cookedCount > 0) {
                out.name("cookedCount");
                out.value(recipe.cookedCount);
            }

            if(recipe.pinnedCount > 0) {
                out.name("pinnedCount");
                out.value(recipe.pinnedCount);
            }

            if(recipe.modifiedCount > 0) {
                out.name("modifiedCount");
                out.value(recipe.modifiedCount);
            }

            if(recipe.variedCount > 0) {
                out.name("variedCount");
                out.value(recipe.variedCount);
            }

            if(recipe.sharedCount > 0) {
                out.name("sharedCount");
                out.value(recipe.sharedCount);
            }

            if(recipe.rating != 0f) {
                out.name("rating");
                out.value(recipe.rating);
            }

            if(recipe.createdDateTime != null) {
                out.name("createdDateTime");
                out.value(DatabaseClient.databaseDateFormat.format(recipe.createdDateTime));
            }

            if(recipe.lastModifiedDateTime != null) {
                out.name("lastModifiedDateTime");
                out.value(DatabaseClient.databaseDateFormat.format(recipe.lastModifiedDateTime));
            }

            if(recipe.lastCookedDateTime != null) {
                out.name("lastCookedDateTime");
                out.value(DatabaseClient.databaseDateFormat.format(recipe.lastCookedDateTime));
            }*/

            if((recipe.changeState & Recipe.CHST_CATEGORIES) == Recipe.CHST_CATEGORIES) {
                out.name("categories");
                out.beginArray();
                if(recipe.categories != null) for(Category category : recipe.categories) {
                    out.beginObject();
                    out.name("categoryId");
                    out.value(category.getCategoryId());
                    out.name("name");
                    out.value(category.getName());
                    out.endObject();
                }
                out.endArray();
            }

            if((recipe.changeState & Recipe.CHST_TAGS) == Recipe.CHST_TAGS) {
                out.name("tags");
                out.beginArray();
                if(recipe.tags != null) for(Tag tag : recipe.tags) {
                    out.beginObject();
                    out.name("tagId");
                    out.value(tag.getTagId());
                    out.name("name");
                    out.value(tag.getName());
                    out.endObject();
                }
                out.endArray();
            }

            out.name("recipeSteps");
            out.beginArray();
            if(recipe.recipeSteps != null) for(RecipeStep recipeStep : recipe.recipeSteps) {

                int recipeStepChangeState = recipeStep.getChangeState();
                if(recipeStepChangeState == 0)
                    continue;

                out.beginObject();
                out.name("recipeStepId");
                out.value(recipeStep.getRecipeStepId());

                if((recipeStepChangeState & RecipeStep.CHST_FLAGS) == RecipeStep.CHST_FLAGS) {
                    out.name("flags");
                    out.value(recipeStep.getFlags());

                    // if the recipe step was deleted stop the object here and continue with the next
                    // because any other recipe step changes do not make sense from here on
                    if((recipeStep.getFlags() & RecipeStep.FLAG_DELETED) == RecipeStep.FLAG_DELETED) {
                        out.endObject();
                        continue;
                    }
                }

                if((recipeStepChangeState & RecipeStep.CHST_STEPNUMBER) == RecipeStep.CHST_STEPNUMBER) {
                    out.name("stepNumber");
                    out.value(recipeStep.getStepNumber());

                    // if the order of recipe steps has changed
                    if(recipeStep.getOriginalStepNumber() != -1) {
                        out.name("originalStepNumber");
                        out.value(recipeStep.getOriginalStepNumber());
                    }
                }

                if((recipeStepChangeState & RecipeStep.CHST_TITLE) == RecipeStep.CHST_TITLE) {
                    out.name("stepTitle");
                    out.value(recipeStep.getStepTitle());
                }

                if((recipeStepChangeState & RecipeStep.CHST_DESCRIPTION) == RecipeStep.CHST_DESCRIPTION) {
                    out.name("stepDescription");
                    out.value(recipeStep.getStepDescription());
                }

                out.name("recipeStepIngredients");
                out.beginArray();
                List<RecipeStepIngredient> recipeStepIngredients = recipeStep.getRecipeStepIngredients();
                if(recipeStepIngredients != null)
                    for(RecipeStepIngredient recipeStepIngredient : recipeStepIngredients) {

                        int recipeStepIngredientChangeState = recipeStepIngredient.getChangeState();
                        if(recipeStepIngredientChangeState == 0)
                            continue;

                        out.beginObject();
                        out.name("ingredientId");
                        out.value(recipeStepIngredient.getIngredientId());

                        if((recipeStepChangeState & RecipeStepIngredient.CHST_FLAGS) == RecipeStepIngredient.CHST_FLAGS) {
                            out.name("flags");
                            out.value(recipeStepIngredient.getFlags());

                            // if the recipe step ingredient was deleted stop the object here and continue with the next
                            // because any other recipe step ingredient changes do not make sense from here on
                            if((recipeStepIngredient.getFlags() & RecipeStepIngredient.FLAG_DELETED) == RecipeStepIngredient.FLAG_DELETED) {
                                out.endObject();
                                continue;
                            }
                        }

                        if((recipeStepChangeState & RecipeStepIngredient.CHST_INGREDIENTNAME) == RecipeStepIngredient.CHST_INGREDIENTNAME) {
                            out.name("ingredientName");
                            out.value(recipeStepIngredient.getIngredientName());
                        }

                        if((recipeStepChangeState & RecipeStepIngredient.CHST_INGREDIENTDESCRIPTION) == RecipeStepIngredient.CHST_INGREDIENTDESCRIPTION) {
                            out.name("ingredientDescription");
                            out.value(recipeStepIngredient.getIngredientDescription());
                        }

                        if((recipeStepChangeState & RecipeStepIngredient.CHST_INGREDIENTAMOUNT) == RecipeStepIngredient.CHST_INGREDIENTAMOUNT) {
                            out.name("ingredientAmount");
                            out.value(recipeStepIngredient.getIngredientAmount());
                        }

                        if((recipeStepChangeState & RecipeStepIngredient.CHST_UNITTYPE) == RecipeStepIngredient.CHST_UNITTYPE) {
                            out.name("unitTypeId");
                            out.value(recipeStepIngredient.getUnitTypeId());
                            out.name("unitTypeName");
                            out.value(recipeStepIngredient.getUnitTypeName());
                            out.name("unitTypeAbbreviation");
                            out.value(recipeStepIngredient.getUnitTypeAbbreviation());
                        }

                        if((recipeStepChangeState & RecipeStepIngredient.CHST_CUSTOMUNIT) == RecipeStepIngredient.CHST_CUSTOMUNIT) {
                            out.name("customUnit");
                            out.value(recipeStepIngredient.getCustomUnit());
                        }

                        out.endObject(); // recipeStepIngredient
                    }
                out.endArray(); // recipeStepIngredients

                out.endObject(); // recipeStep
            }
            out.endArray(); // recipeSteps

            out.endObject(); // recipe
        }

        @Override
        public Recipe read(JsonReader in) throws IOException {
            in.beginObject();

            in.nextName();
            final long recipeId = in.nextLong();

            in.nextName();
            final long languageId = in.nextLong();

            in.nextName();
            String title = in.nextString();

            String nextName = in.nextName();
            // differentiate between recipe detail and recipe list output

            // if the input json represents a detailed recipe item
            if(nextName.equalsIgnoreCase("description")) {

                String description = in.nextString();

                in.nextName();
                long originalRecipeId = in.nextLong();

                in.nextName();
                String originalRecipeTitle = in.nextString();

                in.nextName();
                long creatorId = in.nextLong();

                in.nextName();
                String creatorName = in.nextString();

                in.nextName();
                long mainImageId = in.nextLong();

                in.nextName();
                String mainImageFileName = in.nextString();

                in.nextName();
                long mainCategoryId = in.nextLong();

                in.nextName();
                String mainCategoryName = in.nextString();

                in.nextName();
                String publicationType = in.nextString();

                in.nextName();
                String difficultyType = in.nextString();

                in.nextName();
                int preparationTime = in.nextInt();

                in.nextName();
                int viewedCount = in.nextInt();

                in.nextName();
                int cookedCount = in.nextInt();

                in.nextName();
                int pinnedCount = in.nextInt();

                in.nextName();
                int modifiedCount = in.nextInt();

                in.nextName();
                int variedCount = in.nextInt();

                in.nextName();
                int sharedCount = in.nextInt();

                in.nextName();
                float rating = (float)in.nextDouble();

                in.nextName();
                String createdDateTime = in.nextString();

                in.nextName();
                String lastModifiedDateTime = in.nextString();

                in.nextName();
                String lastCookedDateTime = in.nextString();

                in.nextName();
                int flags = in.nextInt();

                in.nextName();
                int numCategoriesRequested = in.nextInt();

                // categories
                List<Category> categories = null;
                if(numCategoriesRequested != 0) {
                    in.nextName();
                    in.beginArray();
                    categories = new ArrayList<>();
                    while(in.hasNext()) {
                        JsonToken categoryToken = in.peek();
                        if(categoryToken == JsonToken.END_ARRAY)
                            break;

                        in.beginObject();
                        in.nextName();
                        final long categoryId = in.nextLong();
                        in.nextName();
                        String categoryName = in.nextString();
                        in.endObject();

                        categories.add(new Category(categoryId, languageId, categoryName));
                    }
                    in.endArray();
                }

                in.nextName();
                int numTagsRequested = in.nextInt();

                // tags
                List<Tag> tags = null;
                if(numTagsRequested != 0) {
                    in.nextName();
                    in.beginArray();
                    tags = new ArrayList<>();
                    while(in.hasNext()) {
                        JsonToken tagToken = in.peek();
                        if(tagToken == JsonToken.END_ARRAY)
                            break;

                        in.beginObject();
                        in.nextName();
                        final long tagId = in.nextLong();
                        in.nextName();
                        String tagName = in.nextString();
                        in.endObject();

                        tags.add(Tag.Factory.createTag(tagId, tagName));
                    }
                    in.endArray();
                }

                in.nextName();
                int numIngredientsRequested = in.nextInt();

                // ingredients
                List<RecipeStepIngredient> ingredients = null;
                if(numIngredientsRequested != 0) {
                    in.nextName();
                    in.beginArray();
                    ingredients = new ArrayList<>();
                    while(in.hasNext()) {
                        JsonToken rsiToken = in.peek();
                        if(rsiToken == JsonToken.END_ARRAY)
                            break;

                        in.beginObject();
                        in.nextName();
                        final long ingredientId = in.nextLong();
                        in.nextName();
                        String ingredientName = in.nextString();
                        in.nextName();
                        String ingredientDescription = in.nextString();
                        in.nextName();
                        float ingredientAmount = (float)in.nextDouble();
                        in.nextName();
                        final long unitTypeId = in.nextLong();
                        in.nextName();
                        String unitTypeName = in.nextString();
                        in.nextName();
                        String unitTypeAbbreviation = in.nextString();
                        in.nextName();
                        String customUnit = in.nextString();
                        in.endObject();

                        ingredients.add(RecipeStepIngredient.Factory
                            .createRecipeStepIngredient(ingredientId, ingredientName,
                                ingredientDescription, ingredientAmount, unitTypeId, unitTypeName,
                                unitTypeAbbreviation, customUnit));
                    }
                    in.endArray();
                }

                in.nextName();
                int numRecipeStepsRequested = in.nextInt();

                // steps
                List<RecipeStep> recipeSteps = null;
                if(numRecipeStepsRequested != 0) {
                    in.nextName();
                    in.beginArray();
                    recipeSteps = new ArrayList<>();
                    while(in.hasNext()) {
                        JsonToken rsToken = in.peek();
                        if(rsToken == JsonToken.END_ARRAY)
                            break;

                        in.beginObject();
                        in.nextName();
                        final long recipeStepId = in.nextLong();
                        in.nextName();
                        int stepNumber = in.nextInt();
                        in.nextName();
                        String stepTitle = in.nextString();
                        in.nextName();
                        String stepDescription = in.nextString();

                        // ingredients
                        in.nextName();
                        in.beginArray();
                        List<RecipeStepIngredient> recipeStepIngredients = new ArrayList<>();
                        while(in.hasNext()) {
                            JsonToken rsiToken = in.peek();
                            if(rsiToken == JsonToken.END_ARRAY)
                                break;

                            in.beginObject();
                            in.nextName();
                            final long ingredientId = in.nextLong();
                            in.nextName();
                            String ingredientName = in.nextString();
                            in.nextName();
                            String ingredientDescription = in.nextString();
                            in.nextName();
                            float ingredientAmount = (float)in.nextDouble();
                            in.nextName();
                            final long unitTypeId = in.nextLong();
                            in.nextName();
                            String unitTypeName = in.nextString();
                            in.nextName();
                            String unitTypeAbbreviation = in.nextString();
                            in.nextName();
                            String customUnit = in.nextString();
                            in.endObject();

                            recipeStepIngredients.add(RecipeStepIngredient.Factory
                                .createRecipeStepIngredient(ingredientId, ingredientName,
                                    ingredientDescription, ingredientAmount, unitTypeId, unitTypeName,
                                    unitTypeAbbreviation, customUnit));
                        }
                        in.endArray();
                        in.endObject();

                        RecipeStep newRecipeStep = RecipeStep.Factory.createRecipeStep(recipeStepId, stepNumber, stepTitle,
                            stepDescription, recipeStepIngredients);

                        recipeSteps.add(newRecipeStep);
                    }
                    in.endArray();
                }

                in.nextName();
                int numRecipeRatingsRequested = in.nextInt();

                // ratings (not yet implemented)
                if(numRecipeRatingsRequested != 0) {
                    in.nextName();
                    in.beginArray();
                    //List<Rating> ratings = new ArrayList<>();
                    while(in.hasNext()) {
                        JsonToken categoryToken = in.peek();
                        if(categoryToken == JsonToken.END_ARRAY)
                            break;
                    }
                    in.endArray();
                }

                in.nextName();
                int numRecipeImagesRequested = in.nextInt();

                // recipe images
                List<Image> recipeImages = null;
                if(numRecipeImagesRequested != 0) {
                    in.nextName();
                    in.beginArray();
                    recipeImages = new ArrayList<>();
                    while(in.hasNext()) {
                        JsonToken tagToken = in.peek();
                        if(tagToken == JsonToken.END_ARRAY)
                            break;

                        in.beginObject();
                        in.nextName();
                        final long imageId = in.nextLong();
                        in.nextName();
                        final long imageCreatorId = in.nextLong();
                        in.nextName();
                        String imageCreatorName = in.nextString();
                        in.nextName();
                        final long imageModifierId = in.nextLong();
                        in.nextName();
                        String imageModifierName = in.nextString();
                        in.nextName();
                        String imageName = in.nextString();
                        in.nextName();
                        String imageFileName = in.nextString();
                        in.nextName();
                        String imageCreatedDateTime = in.nextString();
                        in.nextName();
                        String imageLastModifiedDateTime = in.nextString();
                        in.nextName();
                        float imageRating = (float)in.nextDouble();
                        in.endObject();

                        Image newImage = Image.Factory.createImage(imageId, imageCreatorId,
                            imageCreatorName, imageModifierId, imageModifierName, imageName,
                            imageFileName, imageCreatedDateTime, imageLastModifiedDateTime,
                            imageRating);

                        recipeImages.add(newImage);
                    }
                    in.endArray();
                }

                in.nextName();
                int numSimilarRecipesRequested = in.nextInt();

                // recipe images
                List<Recipe> similarRecipes = null;
                if(numSimilarRecipesRequested != 0) {
                    in.nextName();
                    in.beginArray();
                    similarRecipes = new ArrayList<>();
                    while(in.hasNext()) {
                        JsonToken tagToken = in.peek();
                        if(tagToken == JsonToken.END_ARRAY)
                            break;
                    }
                    in.endArray();
                }

                in.endObject();

                Recipe newRecipe = new Recipe(recipeId, languageId, title, description, originalRecipeId,
                    originalRecipeTitle, creatorId, creatorName, mainImageId, mainImageFileName,
                    mainCategoryId, mainCategoryName, publicationType, difficultyType, preparationTime,
                    viewedCount, cookedCount, pinnedCount, modifiedCount, variedCount, sharedCount,
                    rating, createdDateTime, lastModifiedDateTime, lastCookedDateTime);

                newRecipe.categories = categories;
                newRecipe.numCategoriesRequested = numCategoriesRequested;

                newRecipe.tags = tags;
                newRecipe.numTagsRequested = numTagsRequested;

                newRecipe.ingredients = ingredients;
                newRecipe.numIngredientsRequested = numIngredientsRequested;

                newRecipe.recipeSteps = recipeSteps;
                newRecipe.numRecipeStepsRequested = numRecipeStepsRequested;

                newRecipe.recipeImages = recipeImages;
                newRecipe.numRecipeImagesRequested = numRecipeImagesRequested;

                newRecipe.similarRecipes = similarRecipes;
                newRecipe.numSimilarRecipesRequested = numSimilarRecipesRequested;

                newRecipe.setFlags(flags);

                return newRecipe;
            }
            // if the input json string represents a list recipe item
            else {
                long creatorId = in.nextLong();

                in.nextName();
                String creatorName = in.nextString();

                in.nextName();
                long mainImageId = in.nextLong();

                in.nextName();
                String mainImageFileName = in.nextString();

                in.nextName();
                long mainCategoryId = in.nextLong();

                in.nextName();
                String mainCategoryName = in.nextString();

                in.nextName();
                String difficultyType = in.nextString();

                in.nextName();
                int preparationTime = in.nextInt();

                in.nextName();
                int cookedCount = in.nextInt();

                in.nextName();
                int pinnedCount = in.nextInt();

                in.nextName();
                float rating = (float)in.nextDouble();

                in.nextName();
                int flags = in.nextInt();

                in.endObject();

                Recipe newRecipe = new Recipe(recipeId, languageId, title, creatorId, creatorName, mainImageId,
                    mainImageFileName, mainCategoryId, mainCategoryName, difficultyType,
                    preparationTime, cookedCount, pinnedCount, rating);

                newRecipe.setFlags(flags);

                return newRecipe;
            }
        }
    }

    /**
     * A JSON type adapter that is responsible for Recipe object serialization provided in compact
     * format.
     */
    public static class CompactFormatAdapter extends TypeAdapter<Recipe> {

        @Override
        public void write(JsonWriter out, Recipe recipe) throws IOException {}

        @Override
        public Recipe read(JsonReader in) throws IOException {
            in.beginObject();

            in.nextName();
            final long recipeId = in.nextLong();

            in.nextName();
            final long languageId = in.nextLong();

            in.nextName();
            String title = in.nextString();

            in.nextName();
            long creatorId = in.nextLong();

            in.nextName();
            String creatorName = in.nextString();

            in.nextName();
            long mainImageId = in.nextLong();

            in.nextName();
            String mainImageFileName = in.nextString();

            in.nextName();
            long mainCategoryId = in.nextLong();

            in.nextName();
            String mainCategoryName = in.nextString();

            in.nextName();
            String difficultyType = in.nextString();

            in.nextName();
            int preparationTime = in.nextInt();

            in.nextName();
            int cookedCount = in.nextInt();

            in.nextName();
            int pinnedCount = in.nextInt();

            in.nextName();
            float rating = (float)in.nextDouble();

            in.endObject();

            return new Recipe(recipeId, languageId, title, creatorId, creatorName, mainImageId,
                mainImageFileName, mainCategoryId, mainCategoryName, difficultyType,
                preparationTime, cookedCount, pinnedCount, rating);
        }
    }
}
