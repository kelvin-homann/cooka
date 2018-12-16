package app.cooka.cookapp.login;

import app.cooka.cookapp.model.InvalidateLoginResult;

public interface ILogoutCallback {

    void onSucceeded(InvalidateLoginResult result);
    void onFailed(int errorCode, String errorMessage, Throwable t);
}
