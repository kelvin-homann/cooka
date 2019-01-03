package app.cooka.cookapp.model;

public interface IFollowCollectionCallback {

    void onSucceeded(FollowCollectionResult followCollectionResult);
    void onFailed(FollowCollectionResult followCollectionResult);
}
