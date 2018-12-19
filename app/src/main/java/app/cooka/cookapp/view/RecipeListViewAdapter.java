package app.cooka.cookapp.view;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.R;
import app.cooka.cookapp.Settings;
import app.cooka.cookapp.model.Recipe;

public class RecipeListViewAdapter extends BaseAdapter {
    
    private List<Recipe> recipes = new ArrayList<>();

    @Override
    public int getCount() {
        return recipes.size();
    }

    @Override
    public Object getItem(int position) {
        return position < 0 || position >= recipes.size() ? null : recipes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position < 0 || position >= recipes.size() ? 0 : recipes.get(position).getRecipeId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = (convertView != null ? convertView : createView(parent));
        final RecipeListViewItem viewHolder = (RecipeListViewItem)view.getTag();
        viewHolder.setRecipe((Recipe)getItem(position));
        return view;
    }

    public void setRecipes(@Nullable List<Recipe> recipes) {
        if(recipes == null)
            return;
        this.recipes.clear();
        this.recipes.addAll(recipes);
        notifyDataSetChanged();
    }

    private View createView(ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.category_list_view_item, parent, false);
        final RecipeListViewItem viewHolder = new RecipeListViewItem(view);
        view.setTag(viewHolder);
        return view;
    }

    public void add(Recipe recipe) {
        recipes.add(recipe);
        notifyDataSetChanged();
    }

    private static class RecipeListViewItem {

        private TextView tvRecipeTitle;

        public RecipeListViewItem(View view) {
            tvRecipeTitle = view.findViewById(R.id.tvwCategoryName);
        }

        public void setRecipe(Recipe recipe) {
            tvRecipeTitle.setText(recipe.getTitle());
        }
    }
}
