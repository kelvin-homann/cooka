package app.cooka.cookapp.model;

import android.content.Context;
import android.util.Log;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A class that represents a collection and that is an app model representation of the corresponding
 * database entity called Collections. Used to serialize between app and database and to cache its
 * state without needing to repeatedly query the same unchanged object from the database.
 */
public class Collection {
    
    public static final String LOGTAG = "COOKALOG";
    
    private Collection() {
        
        // not yet implemented
    }
    
    public static class Factory {

        /**
         * Follows the collection identified by followCollectionId and calls the corresponding
         * callback methods if provided.
         * @param context the Android context to the run the method on.
         * @param followCollectionId the identifier of the collection to be followed.
         * @param followCollectionCallback the callback interface to be called on success or on
         *      failure (may be null).
         */
        public static void followCollection(final Context context, final long followCollectionId,
            final IFollowCollectionCallback followCollectionCallback)
        {
            DatabaseClient.Factory.getInstance(context)
                .followCollection(followCollectionId)
                .enqueue(new Callback<FollowCollectionResult>() {
                    @Override
                    public void onResponse(Call<FollowCollectionResult> call,
                        Response<FollowCollectionResult> response)
                    {
                        final FollowCollectionResult result = response.body();
                        if(result == null) {
                            Log.e(LOGTAG, "Collection.Factory.followCollection() failed on response");
                            if(followCollectionCallback != null)
                                followCollectionCallback.onFailed(null);
                            return;
                        }
                        if(result.resultCode != 0) {
                            Log.e(LOGTAG, String.format("Collection.Factory.followCollection() failed with code %d: %s",
                                result.resultCode, result.resultMessage));
                            if(followCollectionCallback != null)
                                followCollectionCallback.onFailed(result);
                            return;
                        }
                        if(followCollectionCallback != null)
                            followCollectionCallback.onSucceeded(result);
                    }

                    @Override
                    public void onFailure(Call<FollowCollectionResult> call, Throwable t) {
                        Log.e(LOGTAG, "Collection.Factory.followCollection() failed: the web service did not respond");
                        t.printStackTrace();
                        if(followCollectionCallback != null)
                                followCollectionCallback.onFailed(null);
                    }
                });
        }

        /**
         * Follows the collections identified by the ids given in the followCollectionIds list and
         * calls the corresponding callback methods if provided.
         * @param context the Android context to the run the method on.
         * @param followCollectionIds list of identifiers of the collections to be followed.
         * @param followCollectionCallback the callback interface to be called on success or on
         *      failure (may be null).
         */
        public static void followCollections(final Context context, final List<Long>
            followCollectionIds, final IFollowCollectionCallback followCollectionCallback)
        {
            DatabaseClient.Factory.getInstance(context)
                .followCollections(followCollectionIds)
                .enqueue(new Callback<FollowCollectionResult>() {
                    @Override
                    public void onResponse(Call<FollowCollectionResult> call,
                        Response<FollowCollectionResult> response)
                    {
                        final FollowCollectionResult result = response.body();
                        if(result == null) {
                            Log.e(LOGTAG, "Collection.Factory.followCollections() failed on response");
                            if(followCollectionCallback != null)
                                followCollectionCallback.onFailed(null);
                            return;
                        }
                        if(result.resultCode != 0) {
                            Log.e(LOGTAG, String.format("Collection.Factory.followCollections() failed with code %d: %s",
                                result.resultCode, result.resultMessage));
                            if(followCollectionCallback != null)
                                followCollectionCallback.onFailed(result);
                            return;
                        }
                        if(followCollectionCallback != null)
                            followCollectionCallback.onSucceeded(result);
                    }

                    @Override
                    public void onFailure(Call<FollowCollectionResult> call, Throwable t) {
                        Log.e(LOGTAG, "Collection.Factory.followCollections() failed: the web service did not respond");
                        t.printStackTrace();
                        if(followCollectionCallback != null)
                                followCollectionCallback.onFailed(null);
                    }
                });
        }
    }
}
