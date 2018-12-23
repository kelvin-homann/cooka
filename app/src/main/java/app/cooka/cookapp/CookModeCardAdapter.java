package app.cooka.cookapp;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import app.cooka.cookapp.view.IngredientsView;

public class CookModeCardAdapter extends PagerAdapter {

    public static final float MAX_ELEVATION_FACTOR = 3f;

    private ArrayList<String> items;
    private ArrayList<View> views;
    private float baseCardElevation;

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
        bind(items.get(position), view, position);
        CardView cardView = (CardView) view.findViewById(R.id.card);

        if (baseCardElevation == 0) {
            baseCardElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(baseCardElevation * MAX_ELEVATION_FACTOR);
        views.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        views.set(position, null);
    }

    private void bind(String title, View view, int position) {
        ((TextView)view.findViewById(R.id.card_title)).setText(title);
        ((TextView)view.findViewById(R.id.card_step_title)).setText("Schritt " + (position+1));
        ((IngredientsView)view.findViewById(R.id.ingredients_section_ingredients)).setIngredients(new String[]{"1", "300 g"}, new String[]{"Fisch", "Salat"});
    }

    public float getBaseCardElevation() {
        return baseCardElevation;
    }
}
