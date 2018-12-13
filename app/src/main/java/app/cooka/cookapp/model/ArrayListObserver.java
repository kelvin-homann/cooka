package app.cooka.cookapp.model;

import android.widget.BaseAdapter;

import java.util.Observable;
import java.util.Observer;

public class ArrayListObserver implements Observer {

    private BaseAdapter baseAdapter;

    public ArrayListObserver(BaseAdapter baseAdapter) {
        this.baseAdapter = baseAdapter;
    }

    @Override
    public void update(Observable observable, Object arg) {
        this.baseAdapter.notifyDataSetChanged();
    }
}
