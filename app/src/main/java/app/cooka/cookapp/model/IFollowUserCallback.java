package app.cooka.cookapp.model;

public interface IFollowUserCallback {

    void onSucceeded(FollowUserResult followUserResult);
    void onFailed(FollowUserResult followUserResult);
}
