package app.cooka.cookapp.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class CategoryJsonAdapter extends TypeAdapter<Category> {

    private long languageId;

    public CategoryJsonAdapter(long languageId) {
        this.languageId = languageId;
    }

    @Override
    public void write(JsonWriter out, Category category) throws IOException {
        out.beginObject();
        out.name("categoryId");
        out.value(category.getCategoryId());
        out.name("name");
        out.value(category.getName(languageId));
        out.name("description");
        out.value(category.getDescription(languageId));
        out.endObject();
    }

    @Override
    public Category read(JsonReader in) throws IOException {
        in.beginObject();
        in.nextName();
        long categoryId = in.nextLong();
        in.nextName();
        String name = in.nextString();
        in.nextName();
        String description = in.nextString();
        in.nextName();
        long imageId = in.nextLong();
        in.nextName();
        String imageFileName = in.nextString();
        in.endObject();
        Category newCategory = new Category(categoryId, name, description, languageId);
        newCategory.setImageId(imageId);
        newCategory.setImageFileName(imageFileName);
        return newCategory;
    }
}
