package app.cooka.cookapp.model;

import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class Image {

    public static final String LOGTAG = "COOKALOG";
    public static final String IMAGE_BASE_URL = "https://www.sebastianzander.de/cooka/img/";

    private final long imageId;
    private long creatorId;
    private String creatorName;
    private long modifierId;
    private String modifierName;
    private String name;
    private String imageFileName;
    private Date createdDateTime;
    private Date lastModifiedDateTime;
    private float rating;

    private Image(final long imageId, long creatorId, String creatorName, long modifierId,
        String modifierName, String name, String imageFileName, String createdDateTime, String
        lastModifiedDateTime, float rating)
    {
        this.imageId = imageId;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.modifierId = modifierId;
        this.modifierName = modifierName;
        this.name = name;
        this.imageFileName = imageFileName;

        try {
            if(createdDateTime.length() > 0)
                this.createdDateTime = DatabaseClient.databaseDateFormat.parse(createdDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse createdDateTime while creating image %d: %s. Set to null instead.", imageId, name));
        }

        try {
            if(lastModifiedDateTime.length() > 0)
                this.lastModifiedDateTime = DatabaseClient.databaseDateFormat.parse(lastModifiedDateTime);
        }
        catch(ParseException e) {
            e.printStackTrace();
            Log.e(LOGTAG, String.format("could not parse lastModifiedDateTime while creating image %d: %s. Set to null instead.", imageId, name));
        }

        this.rating = rating;
    }

    /**
     * A image factory class that does image creation, selection and database serialization.
     */
    public static class Factory {

        public static Image createImage(final long imageId, long creatorId, String creatorName,
            long modifierId, String modifierName, String name, String imageFileName, String
            createdDateTime, String lastModifiedDateTime, float rating)
        {
            return new Image(imageId, creatorId, creatorName, modifierId, modifierName, name,
                imageFileName, createdDateTime, lastModifiedDateTime, rating);
        }
    }

    public long getImageId() {
        return imageId;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public long getModifierId() {
        return modifierId;
    }

    public void setModifierId(long modifierId) {
        this.modifierId = modifierId;
    }

    public String getModifierName() {
        return modifierName;
    }

    public void setModifierName(String modifierName) {
        this.modifierName = modifierName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getImageFileUrl() {
        return IMAGE_BASE_URL + imageFileName;
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    /**
     * A JSON type adapter that is responsible for Recipe object serialization
     */
    public static class JsonAdapter extends TypeAdapter<Image> {

        @Override
        public void write(JsonWriter out, Image image) throws IOException {}

        @Override
        public Image read(JsonReader in) throws IOException {

            in.beginObject();

            in.nextName();
            final long imageId = in.nextLong();

            in.endObject();

            return null;
        }
    }
}
