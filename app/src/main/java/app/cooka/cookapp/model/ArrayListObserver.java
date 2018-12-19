package app.cooka.cookapp.model;

import android.support.v7.widget.RecyclerView;
import android.widget.BaseAdapter;

import java.util.Observable;
import java.util.Observer;

/**
 * A class that when subscribed to observable ArrayList elements notifies the ArrayList attached
 * BaseAdapter of changes in any of the elements in that ArrayList. Can be used to reflect changes
 * in individual list elements that does not cause the ArrayList to change, then notify the UI and
 * force an update of any attached view.
 */
public class ArrayListObserver implements Observer {

    private BaseAdapter baseAdapter;
    private RecyclerView.Adapter recyclerAdapter;

    /**
     * @param baseAdapter the BaseAdapter that is attached to the observed ArrayList
     */
    public ArrayListObserver(BaseAdapter baseAdapter, RecyclerView.Adapter recyclerAdapter) {
        this.baseAdapter = baseAdapter;
        this.recyclerAdapter = recyclerAdapter;
    }

    @Override
    public void update(Observable observable, Object arg) {
        if(baseAdapter != null)
            this.baseAdapter.notifyDataSetChanged();
        if(recyclerAdapter != null)
            this.recyclerAdapter.notifyDataSetChanged();
    }
}
