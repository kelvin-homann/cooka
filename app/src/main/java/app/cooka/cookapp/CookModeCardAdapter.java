package app.cooka.cookapp;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

public class CookModeCardAdapter extends PagerAdapter {

    private ArrayList<String> items;
    private ArrayList<View> views;

    public CookModeCardAdapter() {
        items = new ArrayList<>();
        views = new ArrayList<>();
    }

    public void addItem(String title) {
        items.add(title);
        views.add(null);
    }

    public View getViewAt(int position) {
        return views.get(position);
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.cook_mode_card, container, false);
        container.addView(view);
        bind(items.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.card);

//        if (mBaseElevation == 0) {
//            mBaseElevation = cardView.getCardElevation();
//        }

//        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        views.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        views.set(position, null);
    }

    private void bind(String title, View view) {
        ((TextView)view.findViewById(R.id.card_title)).setText(title);
    }
}
