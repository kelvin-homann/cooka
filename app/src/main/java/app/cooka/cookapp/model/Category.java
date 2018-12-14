package app.cooka.cookapp.model;

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
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import app.cooka.cookapp.Settings;

/**
 * A class that represents a category and that is an app model representation of the corresponding
 * database entity called Categories. Used to serialize between app and database and to cache its
 * state without needing to repeatedly query the same unchanged object from the database.
 */
@JsonAdapter(Category.JsonAdapter.class)
public class Category extends Observable {

    public static final String LOGTAG = "COOKALOG";

    public static final int CHANGED_NAME = 1;
    public static final int CHANGED_PARENTCATEGORYID = 1 << 1;
    public static final int CHANGED_DESCRIPTION = 1 << 2;
    public static final int CHANGED_IMAGEID = 1 << 3;
    public static final int CHANGED_IMAGEFILENAME = 1 << 4;
    public static final int CHANGED_SORTPREFIX = 1 << 5;
    public static final int CHANGED_BROWSABLE = 1 << 6;
    public static final int CHANGED_FORCE_UPDATE = 0xffffffff;

    private int changeState = 0;

    private final long categoryId;
    private long parentCategoryId;
    private Map<Long, String> name;
    private Map<Long, String> description;
    private long imageId;
    private String imageFileName;
    private Bitmap image;
    private String sortPrefix;
    private boolean browsable;

    public Category(final long categoryId, long parentCategoryId, String name, String description,
        long languageId, long imageId, String imageFileName, String sortPrefix, boolean browsable)
    {
        this.categoryId = categoryId;
        this.parentCategoryId = parentCategoryId;
        this.name = new TreeMap<>();
        this.name.put(languageId, name);
        this.description = new TreeMap<>();
        this.description.put(languageId, description);
        this.imageId = imageId;
        this.imageFileName = imageFileName;
        this.sortPrefix = sortPrefix;
        this.browsable = browsable;
    }

    /**
     * Gets the change state of this category instance that reflects what fields have changed
     * since the last synchronization. Basically an extended dirty flag.
     * @return
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

    public long getCategoryId() {
        return categoryId;
    }

    public long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
        changeState |= CHANGED_PARENTCATEGORYID;
        setChanged();
        notifyObservers();
    }

    public String getName(long languageId) {
        return name.containsKey(languageId) ? name.get(languageId) : null;
    }

    public void setName(String name, long languageId) {
        this.name.put(languageId, name);
        changeState |= CHANGED_NAME;
        setChanged();
        notifyObservers();
    }

    public String getDescription(long languageId) {
        return description.containsKey(languageId) ? description.get(languageId) : null;
    }

    public void setDescription(String description, long languageId) {
        this.description.put(languageId, description);
        changeState |= CHANGED_DESCRIPTION;
        setChanged();
        notifyObservers();
    }

    public long getImageId() {
        return imageId;
    }

    private void setImageId(long imageId) {
        this.imageId = imageId;
        changeState |= CHANGED_IMAGEID;
        setChanged();
        notifyObservers();
    }

    public String getImageFileName() {
        return imageFileName;
    }

    /**
     * Sets a new image file name. First looks in the image cache if the image is already cached
     * and chooses the cached version if up-to-date. Requests the image cache to download the image
     * if uncached or outdated.
     * @param imageFileName the relative image file name as it resides in the default image folder
     */
    private void setImageFileName(String imageFileName) {

        if(this.imageFileName == null || this.imageFileName.compareTo(imageFileName) != 0) {
            this.imageFileName = imageFileName;
            changeState |= CHANGED_IMAGEFILENAME;

            if(imageFileName.length() > 0) {
                String imageUrl = "https://www.sebastianzander.de/cooka/img/" + imageFileName;
                Log.d(LOGTAG, String.format("downloading image from url %s for category %d: %s", imageUrl,
                    categoryId, name.get(Settings.Factory.getInstance().getCurrentLanguageId())));
                new DownloadImageTask(imageUrl, this).execute();
            }

            setChanged();
            notifyObservers();
        }
    }

    public Bitmap getImage() {
        return image;
    }

    /**
     * Sets the internal bitmap. Only used to set the actual bitmap after the image has been
     * requested. Is not used to change the actual image reference and does not cause database
     * synchronisation to happen.
     * @param image
     */
    private void setImage(Bitmap image) {
        this.image = image;
        setChanged();
        notifyObservers();
    }

    public String getSortPrefix() {
        return sortPrefix;
    }

    public void setSortPrefix(String sortPrefix) {
        this.sortPrefix = sortPrefix;
        changeState |= CHANGED_SORTPREFIX;
    }

    public boolean isBrowsable() {
        return browsable;
    }

    public void setBrowsable(boolean browsable) {
        this.browsable = browsable;
        changeState |= CHANGED_BROWSABLE;
    }

    /**
     * A simple asynchronous image download task.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        String imageUrl;
        Category category;

        /**
         * @param imageUrl the URL of the image to be downloaded
         * @param category the category to assign the resulting Bitmap to
         */
        public DownloadImageTask(String imageUrl, Category category) {
            this.imageUrl = imageUrl;
            this.category = category;
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
         * Assigns the downloaded image in form of a bitmap to the referenced category object.
         * @param bitmap the downloaded image returned by doInBackground()
         */
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null && category != null) {
                category.setImage(bitmap);
                Log.d(LOGTAG, String.format("image bitmap set for category %d: %s",
                    category.categoryId, category.name.get(Settings.Factory.getInstance().getCurrentLanguageId())));
            }
        }
    }

    /**
     * A JSON type adapter that is responsible for Category object serialization
     */
    public static class JsonAdapter extends TypeAdapter<Category> {

        private long languageId;

        public JsonAdapter(long languageId) {
            this.languageId = languageId;
        }

        @Override
        public void write(JsonWriter out, Category category) throws IOException {
            out.beginObject();

            out.name("categoryId");
            out.value(category.getCategoryId());

            out.name("parentCategoryId");
            out.value(category.getParentCategoryId());

            out.name("name");
            out.value(category.getName(languageId));

            out.name("description");
            out.value(category.getDescription(languageId));

            out.name("imageId");
            out.value(category.getImageId());

            out.name("imageFileName");
            out.value(category.getImageFileName());

            out.name("sortPrefix");
            out.value(category.getSortPrefix());

            out.name("browsable");
            out.value(category.isBrowsable() ? 1 : 0);

            out.endObject();
        }

        @Override
        public Category read(JsonReader in) throws IOException {
            in.beginObject();

            in.nextName();
            long categoryId = in.nextLong();

            in.nextName();
            long parentCategoryId = in.nextLong();

            in.nextName();
            String name = in.nextString();

            in.nextName();
            String description = in.nextString();

            in.nextName();
            long imageId = in.nextLong();

            in.nextName();
            String imageFileName = in.nextString();

            in.nextName();
            String sortPrefix = in.nextString();

            in.nextName();
            boolean browsable = in.nextInt() != 0;

            in.endObject();

            return new Category(categoryId, parentCategoryId, name, description, languageId,
                imageId, imageFileName, sortPrefix, browsable);
        }
    }
}
