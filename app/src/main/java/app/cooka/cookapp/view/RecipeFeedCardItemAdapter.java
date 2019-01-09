package app.cooka.cookapp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import app.cooka.cookapp.R;
import app.cooka.cookapp.UserProfileActivity;
import app.cooka.cookapp.model.Recipe;

public class RecipeFeedCardItemAdapter extends
    RecyclerView.Adapter<RecipeFeedCardItemAdapter.ViewHolder> {
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        // each data item is just a string in this case
        public TextView tvwRecipeTitle;
        public TextView tvwCreatorUserName;
        public TextView tvwCookedCount;
        public TextView tvwPinnedCount;
        public ImageView ivwRecipeMainImage;
        public ImageView ivwContextMenuButton;
        public Context context;

        public ViewHolder(View view) {
            super(view);
            this.tvwRecipeTitle = view.findViewById(R.id.tvwCategoryName);
            this.tvwCreatorUserName = view.findViewById(R.id.tvwCreatorUserName);
            this.tvwCookedCount = view.findViewById(R.id.tvwCookedCount);
            this.tvwPinnedCount = view.findViewById(R.id.tvwPinnedCount);
            this.ivwRecipeMainImage = view.findViewById(R.id.ivwRecipeMainImage);
            this.ivwContextMenuButton = view.findViewById(R.id.ivwContextMenuButton);
            this.context = context;

            view.setOnCreateContextMenuListener(this);

        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {

        }
    }

    public static final int FMCM_VIEW_PERFORMER_PROFILE = 10000;
    public static final int FMCM_VIEW_FOLLOWEE_PROFILE = 11000;
    public static final int FMCM_VIEW_RECIPE = 10010;
    public static final int FMCM_PIN_RECIPE = 10011;
    public static final int FMCM_COOK_RECIPE_NOW = 10012;
    public static final int FMCM_BROWSE_COLLECTION = 12000;
    public static final int FMCM_BROWSE_TAG = 13000;

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
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int index) {

        final Recipe recipe = recipes.get(index);

        viewHolder.tvwRecipeTitle.setText(recipe.getTitle());

        viewHolder.tvwCreatorUserName.setText(String.format("@%s", recipe.getCreatorName()));
        viewHolder.tvwCookedCount.setText(String.format("%d cooked", recipe.getCookedCount()));
        viewHolder.tvwPinnedCount.setText(String.format("%d pinned", recipe.getPinnedCount()));

        if(recipe.getMainImageFileName().length() > 0) {

            Glide.with(viewHolder.ivwRecipeMainImage)
                .asBitmap()
                .load("https://www.sebastianzander.de/cooka/img/" + recipe.getMainImageFileName())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Bitmap> target, boolean isFirstResource) {
                        viewHolder.ivwRecipeMainImage.setVisibility(View.GONE);
                        viewHolder.tvwCookedCount.setVisibility(View.GONE);
                        viewHolder.tvwPinnedCount.setVisibility(View.GONE);

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap>
                        target, DataSource dataSource, boolean isFirstResource) {
                        viewHolder.ivwRecipeMainImage.setImageBitmap(resource);
                        viewHolder.ivwRecipeMainImage.setVisibility(View.VISIBLE);
                        viewHolder.tvwCookedCount.setVisibility(View.VISIBLE);
                        viewHolder.tvwPinnedCount.setVisibility(View.VISIBLE);
                        return true;
                    }
                })
                .submit();
            } else
            viewHolder.ivwRecipeMainImage.setVisibility(View.GONE);
            viewHolder.tvwCookedCount.setVisibility(View.GONE);
            viewHolder.tvwPinnedCount.setVisibility(View.GONE);


        // TODO: 09/01/2019 add view Recipe to OnClickListener

        // set on click listener for MainImage
        viewHolder.ivwRecipeMainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("ContextMenu", "Recipe");
            }
        });


        // set on click listener for ContextMenu
        viewHolder.ivwContextMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = view.getContext();
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, viewHolder.ivwContextMenuButton,
                    Gravity.TOP | Gravity.RIGHT);
                //inflating menu from xml resource
                popup.inflate(R.menu.feedmessage_context_menu);
                Menu menu = popup.getMenu();

                String viewPerformerProfileText = context.getString(R.string.view_profile_of_user, recipe.getCreatorName());
                menu.add(R.id.fmcm_context_group, FMCM_VIEW_PERFORMER_PROFILE, 0, viewPerformerProfileText)
                    .setIcon(R.drawable.ic_user_24dp);

                String viewRecipeText = context.getString(R.string.view_recipe);
                String pinRecipeText = context.getString(R.string.pin_recipe);
                String cookRecipeNowText = context.getString(R.string.cook_this_recipe_now);
                menu.add(R.id.fmcm_context_group, FMCM_VIEW_RECIPE, 0, viewRecipeText);
                menu.add(R.id.fmcm_context_group, FMCM_PIN_RECIPE, 0, pinRecipeText);
                menu.add(R.id.fmcm_context_group, FMCM_COOK_RECIPE_NOW, 0, cookRecipeNowText);


                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int itemId = item.getItemId();
                        switch(itemId) {
                            case FMCM_VIEW_PERFORMER_PROFILE:
                                Bundle bundleProfile = new Bundle();
                                bundleProfile.putLong("userid", recipe.getCreatorId());
                                Intent profileIntent = new Intent(context, UserProfileActivity.class);
                                profileIntent.putExtras(bundleProfile);
                                context.startActivity(profileIntent);
                                return true;
                                case FMCM_VIEW_RECIPE:
                                Log.d("ContextMenu", "Recipe");
                                return true;
                            case FMCM_PIN_RECIPE:
                                Log.d("ContextMenu", "Pin");
                            case FMCM_COOK_RECIPE_NOW:
                                Log.d("ContextMenu", "Cook");
                            case R.id.fmcm_report:
                                Log.d("ContextMenu", "Report");
                        }

                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }
}

