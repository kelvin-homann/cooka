package app.cooka.cookapp.model;

/**
 * A class that represents an ingredient and that is an app model representation of the
 * corresponding database entity called Ingredients. Used to serialize between app and database and
 * to cache its state without needing to repeatedly query the same unchanged object from the
 * database.
 */
public class Ingredient {

    public static final String LOGTAG = "COOKALOG";

    private final long ingredientId;
    private long nameStringId = 0;
    private String name;
    private long descriptionStringId = 0;
    private String description;

    public Ingredient(final long ingredientId, long nameStringId, long descriptionStringId) {

        this.ingredientId = ingredientId;
        this.nameStringId = nameStringId;
        this.descriptionStringId = descriptionStringId;

        // todo: do we need to query and cache the strings here?
        // query name from String.Factory?
        // query description from String.Factory?
    }

    public long getIngredientId() {
        return ingredientId;
    }

    public long getNameStringId() {
        return nameStringId;
    }

    public void setNameStringId(long nameStringId) {
        // re-reference the string instance
        this.nameStringId = nameStringId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDescriptionStringId() {
        return descriptionStringId;
    }

    public void setDescriptionStringId(long descriptionStringId) {
        // re-reference the string instance
        this.descriptionStringId = descriptionStringId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
