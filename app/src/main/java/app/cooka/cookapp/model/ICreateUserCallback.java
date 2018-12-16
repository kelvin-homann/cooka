package app.cooka.cookapp.model;

public interface ICreateUserCallback {

    void onSucceeded(CreateUserResult createUserResult, User createdUser);
    void onFailed();
}
