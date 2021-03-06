package app.cooka.cookapp.model;

public interface IResultCallback<T> {

    void onSucceeded(T result);
    void onFailed(Throwable t);
}
