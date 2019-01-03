package app.cooka.cookapp.model;

import android.content.Context;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        
        /**
         * Follows the tag identified by followTagId and calls the corresponding callback methods
         * if provided.
         * @param context the Android context to the run the method on.
         * @param followTagId the identifier of the tag to be followed.
         * @param followTagCallback the callback interface to be called on success or on failure
         *      (may be null).
         */
        public static void followTag(final Context context, final long followTagId, final
            IFollowTagCallback followTagCallback)
        {
            DatabaseClient.Factory.getInstance(context)
                .followTag(followTagId)
                .enqueue(new Callback<FollowTagResult>() {
                    @Override
                    public void onResponse(Call<FollowTagResult> call, Response<FollowTagResult>
                        response)
                    {
                        final FollowTagResult result = response.body();
                        if(result == null) {
                            Log.e(LOGTAG, "Tag.Factory.followTag() failed on response");
                            if(followTagCallback != null)
                                followTagCallback.onFailed(null);
                            return;
                        }
                        if(result.resultCode != 0) {
                            Log.e(LOGTAG, String.format("Tag.Factory.followTag() failed with code %d: %s",
                                result.resultCode, result.resultMessage));
                            if(followTagCallback != null)
                                followTagCallback.onFailed(result);
                            return;
                        }
                        if(followTagCallback != null)
                            followTagCallback.onSucceeded(result);
                    }

                    @Override
                    public void onFailure(Call<FollowTagResult> call, Throwable t) {
                        Log.e(LOGTAG, "Tag.Factory.followTag() failed: the web service did not respond");
                        t.printStackTrace();
                        if(followTagCallback != null)
                                followTagCallback.onFailed(null);
                    }
                });
        }

        /**
         * Follows the tags identified by the ids given in the followTagIds list and calls the
         * corresponding callback methods if provided.
         * @param context the Android context to the run the method on.
         * @param followTagIds list of identifiers of the tags to be followed.
         * @param followTagCallback the callback interface to be called on success or on failure
         *      (may be null).
         */
        public static void followTags(final Context context, final List<Long> followTagIds, final
            IFollowTagCallback followTagCallback)
        {
            DatabaseClient.Factory.getInstance(context)
                .followTags(followTagIds)
                .enqueue(new Callback<FollowTagResult>() {
                    @Override
                    public void onResponse(Call<FollowTagResult> call, Response<FollowTagResult>
                        response)
                    {
                        final FollowTagResult result = response.body();
                        if(result == null) {
                            Log.e(LOGTAG, "Tag.Factory.followTags() failed on response");
                            if(followTagCallback != null)
                                followTagCallback.onFailed(null);
                            return;
                        }
                        if(result.resultCode != 0) {
                            Log.e(LOGTAG, String.format("Tag.Factory.followTags() failed with code %d: %s",
                                result.resultCode, result.resultMessage));
                            if(followTagCallback != null)
                                followTagCallback.onFailed(result);
                            return;
                        }
                        if(followTagCallback != null)
                            followTagCallback.onSucceeded(result);
                    }

                    @Override
                    public void onFailure(Call<FollowTagResult> call, Throwable t) {
                        Log.e(LOGTAG, "Tag.Factory.followTags() failed: the web service did not respond");
                        t.printStackTrace();
                        if(followTagCallback != null)
                                followTagCallback.onFailed(null);
                    }
                });
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
