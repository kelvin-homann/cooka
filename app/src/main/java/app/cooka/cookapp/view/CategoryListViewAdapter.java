package app.cooka.cookapp.view;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.R;
import app.cooka.cookapp.Settings;
import app.cooka.cookapp.model.Category;

public class CategoryListViewAdapter extends BaseAdapter {

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
        final CategoryListViewItem viewHolder = (CategoryListViewItem)view.getTag();
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
        final View view = inflater.inflate(R.layout.category_list_view_item, parent, false);
        final CategoryListViewItem viewHolder = new CategoryListViewItem(view);
        view.setTag(viewHolder);
        return view;
    }

    public void add(Category category) {
        categories.add(category);
        notifyDataSetChanged();
    }

    private static class CategoryListViewItem {

        private TextView tvCategoryName;

        public CategoryListViewItem(View view) {
            //tvCategoryName = view.findViewById(R.id.tvCategoryName);
        }

        public void setCategory(Category category) {
            //tvCategoryName.setText(category.getName(Settings.Factory.getInstance().getCurrentLanguageId()));
        }
    }
}
