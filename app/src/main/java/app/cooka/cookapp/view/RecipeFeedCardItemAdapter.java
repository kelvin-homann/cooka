package app.cooka.cookapp.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.cooka.cookapp.R;
import app.cooka.cookapp.model.Recipe;

public class RecipeFeedCardItemAdapter extends
    RecyclerView.Adapter<RecipeFeedCardItemAdapter.ViewHolder>
{
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tvwRecipeTitle;
        public TextView tvwCreatorUserName;
        public TextView tvwCookedCount;
        public TextView tvwPinnedCount;
        public ImageView ivwRecipeMainImage;
        public ViewHolder(View view) {
            super(view);
            this.tvwRecipeTitle = view.findViewById(R.id.tvwCategoryName);
            this.tvwCreatorUserName = view.findViewById(R.id.tvwCreatorUserName);
            this.tvwCookedCount = view.findViewById(R.id.tvwCookedCount);
            this.tvwPinnedCount = view.findViewById(R.id.tvwPinnedCount);
            this.ivwRecipeMainImage = view.findViewById(R.id.ivwRecipeMainImage);
        }
    }

    private List<Recipe> recipes;

    public RecipeFeedCardItemAdapter(List<Recipe> recipes) {

        this.recipes = recipes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.test_recipe_list_view_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int index) {

        Recipe recipe = recipes.get(index);
        viewHolder.tvwRecipeTitle.setText(recipe.getTitle());
        viewHolder.tvwCreatorUserName.setText(String.format("@%s", recipe.getCreatorName()));
        viewHolder.tvwCookedCount.setText(String.format("%d cooked", recipe.getCookedCount()));
        viewHolder.tvwPinnedCount.setText(String.format("%d pinned", recipe.getPinnedCount()));
        viewHolder.ivwRecipeMainImage.setImageBitmap(recipe.getMainImage());
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }
}
