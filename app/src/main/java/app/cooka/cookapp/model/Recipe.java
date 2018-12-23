package app.cooka.cookapp.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

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

    public static final int CHANGED_TITLE = 1;
    public static final int CHANGED_DESCRIPTION = 1 << 1;
    public static final int CHANGED_MAINIMAGE = 1 << 2;
    public static final int CHANGED_MAINCATEGORY = 1 << 3;
    public static final int CHANGED_CATEGORIE = 1 << 4;
    public static final int CHANGED_TAGS = 1 << 5;
    public static final int CHANGED_PUBLICATIONTYPE = 1 << 6;
    public static final int CHANGED_DIFFICULTYTYPE = 1 << 7;
    public static final int CHANGED_PREPARATIONTIME = 1 << 8;
    public static final int CHANGED_STEPS = 1 << 9;
    public static final int CHANGED_FORCE_UPDATE = 0xffffffff;

    private int changeState = 0;
    private boolean committed = false;

    private final long recipeId;
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

    private Recipe(final long recipeId, String title, String description) {

        this.recipeId = recipeId;
        this.title = title;
        this.description = description;
    }

    private Recipe(final long recipeId, String title, String description, long originalRecipeId,
        String originalRecipeTitle, long creatorId, String creatorName, long mainImageId,
        String mainImageFileName, long mainCategoryId, String mainCategoryName,
        String publicationType, String difficultyType, int preparationTime,
        int viewedCount, int cookedCount, int pinnedCount, int modifiedCount, int variedCount,
        int sharedCount, float rating, String createdDateTime, String lastModifiedDateTime,
        String lastCookedDateTime)
    {
        this.recipeId = recipeId;
        this.title = title;
        this.description = description;
        this.originalRecipeId = originalRecipeId;
        this.originalRecipeTitle = originalRecipeTitle;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.mainImageId = mainImageId;

        setMainImageFileName(mainImageFileName);

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

    private Recipe(final long recipeId, String title, long creatorId, String creatorName,
        long mainImageId, String mainImageFileName, long mainCategoryId, String mainCategoryName,
        String difficultyType, int preparationTime, int cookedCount, int pinnedCount, float rating)
    {
        this.recipeId = recipeId;
        this.title = title;
        this.description = "";
        this.originalRecipeId = 0;
        this.originalRecipeTitle = "";
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.mainImageId = mainImageId;

        setMainImageFileName(mainImageFileName);

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

    /**
     * A recipe factory class that does recipe creation, selection and database serialization.
     */
    public static class Factory {

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
                    }
                    @Override public void onNext(List<Recipe> recipes) {
                        // todo: register all users at the update observer
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

    public void resetChangeState() {
        changeState = 0;
    }

    public void setForceUpdate() {
        changeState = CHANGED_FORCE_UPDATE;
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

    public long getRecipeId() {
        return recipeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        changeState |= CHANGED_TITLE;
        setChanged();
        notifyObservers();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        changeState |= CHANGED_DESCRIPTION;
        setChanged();
        notifyObservers();
    }

    public long getOriginalRecipeId() {
        return originalRecipeId;
    }

    public String getOriginalRecipeTitle() {
        return originalRecipeTitle;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public long getMainImageId() {
        return mainImageId;
    }

    public void setMainImageId(long mainImageId) {
        this.mainImageId = mainImageId;
        changeState |= CHANGED_MAINIMAGE;
        setChanged();
        notifyObservers();
    }

    public String getMainImageFileName() {
        return mainImageFileName;
    }

    private void setMainImageFileName(String mainImageFileName) {

        if(this.mainImageFileName == null || this.mainImageFileName.compareTo(mainImageFileName) != 0) {
            this.mainImageFileName = mainImageFileName;
            changeState |= CHANGED_MAINIMAGE;

            if(mainImageFileName.length() > 0) {
                String imageUrl = "https://www.sebastianzander.de/cooka/img/" + mainImageFileName;
                new DownloadImageTask(imageUrl, this).execute();
            }

            setChanged();
            notifyObservers();
        }
    }

    public Bitmap getMainImage() {
        return mainImage;
    }

    public void setMainImage(Bitmap mainImage) {
        this.mainImage = mainImage;
        setChanged();
        notifyObservers();
    }

    public long getMainCategoryId() {
        return mainCategoryId;
    }

    public void setMainCategoryId(long mainCategoryId) {
        this.mainCategoryId = mainCategoryId;
    }

    public String getMainCategoryName() {
        return mainCategoryName;
    }

    public void setMainCategoryName(String mainCategoryName) {
        this.mainCategoryName = mainCategoryName;
    }

    public EPublicationType getPublicationType() {
        return publicationType;
    }

    public void setPublicationType(EPublicationType publicationType) {
        this.publicationType = publicationType;
    }

    public EDifficultyType getDifficultyType() {
        return difficultyType;
    }

    public void setDifficultyType(EDifficultyType difficultyType) {
        this.difficultyType = difficultyType;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public int getViewedCount() {
        return viewedCount;
    }

    public void setViewedCount(int viewedCount) {
        this.viewedCount = viewedCount;
    }

    public int getCookedCount() {
        return cookedCount;
    }

    public void setCookedCount(int cookedCount) {
        this.cookedCount = cookedCount;
    }

    public int getPinnedCount() {
        return pinnedCount;
    }

    public void setPinnedCount(int pinnedCount) {
        this.pinnedCount = pinnedCount;
    }

    public int getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(int modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    public int getVariedCount() {
        return variedCount;
    }

    public void setVariedCount(int variedCount) {
        this.variedCount = variedCount;
    }

    public int getSharedCount() {
        return sharedCount;
    }

    public void setSharedCount(int sharedCount) {
        this.sharedCount = sharedCount;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Date getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(Date createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Date getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(Date lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public Date getLastCookedDateTime() {
        return lastCookedDateTime;
    }

    public void setLastCookedDateTime(Date lastCookedDateTime) {
        this.lastCookedDateTime = lastCookedDateTime;
    }

    /**
     * A simple asynchronous image download task.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
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
        public void write(JsonWriter out, Recipe recipe) throws IOException {}

        @Override
        public Recipe read(JsonReader in) throws IOException {
            in.beginObject();

            in.nextName();
            final long recipeId = in.nextLong();

            in.nextName();
            String title = in.nextString();

            in.nextName();
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

            in.endObject();

            return new Recipe(recipeId, title, description, originalRecipeId, originalRecipeTitle,
                creatorId, creatorName, mainImageId, mainImageFileName, mainCategoryId,
                mainCategoryName, publicationType, difficultyType, preparationTime, viewedCount,
                cookedCount, pinnedCount, modifiedCount, variedCount, sharedCount, rating,
                createdDateTime, lastModifiedDateTime, lastCookedDateTime);
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

            return new Recipe(recipeId, title, creatorId, creatorName, mainImageId,
                mainImageFileName, mainCategoryId, mainCategoryName, difficultyType,
                preparationTime, cookedCount, pinnedCount, rating);
        }
    }
}
