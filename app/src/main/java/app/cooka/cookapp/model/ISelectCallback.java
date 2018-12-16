package app.cooka.cookapp.model;

import java.util.List;

public interface ISelectCallback<T> {

    void onSucceeded(T selectedItem);
}
