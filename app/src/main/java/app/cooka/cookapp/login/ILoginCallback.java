package app.cooka.cookapp.login;

import app.cooka.cookapp.model.AuthenticateUserResult;

public interface ILoginCallback {

    void onSucceeded(AuthenticateUserResult result);
    void onFailed(int errorCode, String errorMessage, Throwable t);
}
