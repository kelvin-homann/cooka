package app.cooka.cookapp.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.R;
import app.cooka.cookapp.Settings;

public class CategoryGridViewAdapter extends BaseAdapter {

    public static final String LOGTAG = "COOKALOG";

    private List<Category> categories = new ArrayList<>();

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return position < 0 || position >= categories.size() ? null : categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position < 0 || position >= categories.size() ? 0 : categories.get(position).getCategoryId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = (convertView != null ? convertView : createView(parent));
        final CategoryGridViewItem viewHolder = (CategoryGridViewItem)view.getTag();
        viewHolder.setCategory((Category)getItem(position));
        return view;
    }

    public void setCategories(@Nullable List<Category> categorys) {
        if(categorys == null)
            return;
        this.categories.clear();
        this.categories.addAll(categorys);
        notifyDataSetChanged();
    }

    private View createView(ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.category_grid_view_item, parent, false);
        final CategoryGridViewItem viewHolder = new CategoryGridViewItem(view);
        view.setTag(viewHolder);
        return view;
    }

    public void add(Category category) {
        categories.add(category);
        notifyDataSetChanged();
    }

    private static class CategoryGridViewItem {

        private TextView tvwCategoryName;
        private ImageView ivwCategoryImage;
        private ImageView ivwOverlay;

        public CategoryGridViewItem(View view) {
            tvwCategoryName = view.findViewById(R.id.tvwCategoryName);
            ivwCategoryImage = view.findViewById(R.id.ivwCategoryImage);
            ivwOverlay = view.findViewById(R.id.ivwOverlay);
        }

        public void setCategory(Category category) {
            tvwCategoryName.setText(category.getName(Settings.Factory.getInstance().getCurrentLanguageId()));

            if(category.getImage() != null) {
                ivwCategoryImage.setImageBitmap(category.getImage());
                //ivwOverlay.setAlpha(0.6f);
                ivwOverlay.setBackgroundColor(Color.WHITE);
            }
            else {
                ivwCategoryImage.setImageBitmap(null);
                //ivwOverlay.setAlpha(0.6f);
                ivwOverlay.setBackgroundColor(Color.rgb(0xe9, 0xef, 0xd5)); // 0xe9efd5
            }
        }
    }
}
