package app.cooka.cookapp.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.annotations.JsonAdapter;

import java.io.InputStream;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;

import app.cooka.cookapp.Settings;

@JsonAdapter(CategoryJsonAdapter.class)
public class Category extends Observable {

    public static final String LOGTAG = "COOKALOG";

    private final long categoryId;
    private Map<Long, String> name;
    private Map<Long, String> description;
    private long imageId;
    private String imageFileName;
    private Bitmap image;


    public Category(final long categoryId, String name, String description, long languageId) {
        this.categoryId = categoryId;
        this.name = new TreeMap<>();
        this.name.put(languageId, name);
        this.description = new TreeMap<>();
        this.description.put(languageId, description);
    }

    public long getCategoryId() {
        return categoryId;
    }

    public String getName(long languageId) {
        return name.containsKey(languageId) ? name.get(languageId) : null;
    }

    public void setName(String name, long languageId) {
        this.name.put(languageId, name);
        setChanged();
        notifyObservers();
    }

    public String getDescription(long languageId) {
        return description.containsKey(languageId) ? description.get(languageId) : null;
    }

    public void setDescription(String description, long languageId) {
        this.description.put(languageId, description);
        setChanged();
        notifyObservers();
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
        setChanged();
        notifyObservers();
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        if(this.imageFileName == null || this.imageFileName.compareTo(imageFileName) != 0) {
            this.imageFileName = imageFileName;
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

    public void setImage(Bitmap image) {
        this.imageId = 0;
        this.imageFileName = "";
        this.image = image;
        setChanged();
        notifyObservers();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        String imageUrl;
        Bitmap targetImage;
        Category category;

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

        protected void onPostExecute(Bitmap result) {
            if(result != null && category != null) {
                category.setImage(result);
                Log.d(LOGTAG, String.format("image bitmap set for category %d: %s",
                    category.categoryId, category.name.get(Settings.Factory.getInstance().getCurrentLanguageId())));
            }
        }
    }
}
