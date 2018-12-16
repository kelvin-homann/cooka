package app.cooka.cookapp.login;

import app.cooka.cookapp.model.CreateUserResult;
import app.cooka.cookapp.model.User;

public interface ICreateAccountCallback {

    void onSucceeded(CreateUserResult result, User createdUser);
    void onFailed(int errorCode, String errorMessage, Throwable t);
}
