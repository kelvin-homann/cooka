package app.cooka.cookapp.model;

public interface IFollowTagCallback {

    void onSucceeded(FollowTagResult followTagResult);
    void onFailed(FollowTagResult followTagResult);
}
