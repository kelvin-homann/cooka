package app.cooka.cookapp.model;

public interface ICreateCallback<T> {

    void onSucceeded(T createdItem);
}
