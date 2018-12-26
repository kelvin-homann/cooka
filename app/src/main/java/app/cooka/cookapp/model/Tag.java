package app.cooka.cookapp.model;

/**
 * A class that represents a tag and that is an app model representation of the corresponding
 * database entity called Tags. Used to serialize between app and database and to cache its
 * state without needing to repeatedly query the same unchanged object from the database.
 */
public class Tag {

    public static final String LOGTAG = "COOKALOG";

    private final long tagId;
    private String name;

    private Tag(final long tagId, String name) {

        this.tagId = tagId;
        this.name = name;
    }

    public static class Factory {

        /**
         * Creates a new {@linkplain Tag} object that is not linked to the database.
         * @param tagId the tag identifier.
         * @param name the name of the tag.
         * @return a new {@linkplain Tag} object; null if an error occurred.
         */
        public static Tag createTag(final long tagId, final String name) {

            return new Tag(tagId, name);
        }
    }

    public long getTagId() {
        return tagId;
    }

    public String getName() {
        return name;
    }

    /**
     * Builds and returns a new {@linkplain Tag} object from only a tag identifier. This is used
     * when creating lists of tags like they are used in a recipe and that shall be linked in the
     * database later.
     * @param tagId the tag identifier of the tag to be linked in the database later.
     * @return a new {@linkplain Tag} object; null if an error occurred.
     */
    public static Tag fromTagId(final long tagId) {
        return new Tag(tagId, null);
    }
}
