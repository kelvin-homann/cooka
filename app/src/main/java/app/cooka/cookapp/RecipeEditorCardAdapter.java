package app.cooka.cookapp;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;

import app.cooka.cookapp.model.RecipeStep;
import app.cooka.cookapp.model.RecipeStepIngredient;
import app.cooka.cookapp.view.IngredientsView;

public class RecipeEditorCardAdapter extends PagerAdapter implements CardPagerAdapter {

    public static final float MAX_ELEVATION_FACTOR = 3f;

    private Context context;

    private ArrayList<RecipeStep> items;
    private ArrayList<View> views;
    private float baseCardElevation;

    public RecipeEditorCardAdapter(Context context) {
        items = new ArrayList<>();
        views = new ArrayList<>();
        this.context = context;
    }

    public void addItem() {
        RecipeStep step = RecipeStep.Factory.createRecipeStepDraft(getCount()+1, "", "", new ArrayList<RecipeStepIngredient>());
        items.add(step);
        views.add(null);
    }

    public void removeItem(int position) {
        items.remove(position);
        views.remove(position);
    }

    public View getViewAt(int position) {
        return views.get(position);
    }

    @Override
    public int getCount() {
        return views.size();
    }

    @Override
    public int getItemPosition(Object object) {
        for(int index = 0 ; index < getCount() ; index++){
            if((View)object == views.get(index)) {
                return index;
            }
        }
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.recipe_editor_card, container, false);
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
        String stepTitle = context.getString(R.string.step_title, position+1);
        step.setStepNumber(position+1);

        ((TextView)view.findViewById(R.id.card_step_title)).setText(stepTitle);
        ((EditText)view.findViewById(R.id.card_title)).setText(step.getStepTitle());
        ((EditText)view.findViewById(R.id.card_body)).setText(step.getStepDescription());
        //((IngredientsView)view.findViewById(R.id.ingredients_section_ingredients)).setIngredients(new String[]{"1", "300 g"}, new String[]{"Fisch", "Salat"});
    }

    public void syncSteps() {
        for(int i = 0; i < getCount(); i++) {
            String title = ((EditText)views.get(i).findViewById(R.id.card_title)).getText().toString();
            String body = ((EditText)views.get(i).findViewById(R.id.card_body)).getText().toString();

            items.get(i).setStepTitle(title);
            items.get(i).setStepDescription(body);
            items.get(i).setStepNumber(i+1);
        }
    }

    public float getBaseCardElevation() {
        return baseCardElevation;
    }
}
