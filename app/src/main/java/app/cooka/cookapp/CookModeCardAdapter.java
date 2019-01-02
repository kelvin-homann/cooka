package app.cooka.cookapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ContentFrameLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

import app.cooka.cookapp.model.RecipeStep;
import app.cooka.cookapp.view.IngredientsView;

public class CookModeCardAdapter extends PagerAdapter implements CardPagerAdapter {

    public static final float MAX_ELEVATION_FACTOR = 3f;

    private ArrayList<RecipeStep> items;
    private ArrayList<View> views;
    private float baseCardElevation;
    private Context context;

    public CookModeCardAdapter(Context context) {
        items = new ArrayList<>();
        views = new ArrayList<>();
        this.context = context;
    }

    public void addItem(RecipeStep step) {
        items.add(step);
        views.add(null);
    }

    public void clear() {
        items.clear();
        views.clear();
    }

    public View getViewAt(int position) {
        if(getCount() <= position) return null;
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

    private void bind(RecipeStep step, View view, int position) {
        ((TextView)view.findViewById(R.id.card_title)).setText(step.getStepTitle());
        ((TextView)view.findViewById(R.id.card_step_title)).setText(context.getString(R.string.step_title, step.getStepNumber()));
        ((TextView)view.findViewById(R.id.card_body)).setText(step.getStepDescription());
        ((IngredientsView)view.findViewById(R.id.ingredients_section_ingredients)).setIngredients(step.getRecipeStepIngredients());
    }

    public float getBaseCardElevation() {
        return baseCardElevation;
    }
}
