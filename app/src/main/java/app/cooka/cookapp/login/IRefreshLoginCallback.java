package app.cooka.cookapp.login;

import app.cooka.cookapp.model.InvalidateLoginResult;
import app.cooka.cookapp.model.RefreshLoginResult;

public interface IRefreshLoginCallback {

    void onLoginRefreshed(RefreshLoginResult result);
    void onLoginInvalidated();
    void onFailed(int errorCode, String errorMessage, Throwable t);
}
