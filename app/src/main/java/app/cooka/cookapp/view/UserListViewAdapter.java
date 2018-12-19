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
import app.cooka.cookapp.model.User;

public class UserListViewAdapter extends BaseAdapter {

    private List<User> users = new ArrayList<>();

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return position < 0 || position >= users.size() ? null : users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position < 0 || position >= users.size() ? 0 : users.get(position).getUserId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = (convertView != null ? convertView : createView(parent));
        final UserListViewItem viewHolder = (UserListViewItem)view.getTag();
        viewHolder.setUser((User)getItem(position));
        return view;
    }

    public void setUsers(@Nullable List<User> users) {
        if(users == null)
            return;
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    private View createView(ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.user_list_view_item, parent, false);
        final UserListViewItem viewHolder = new UserListViewItem(view);
        view.setTag(viewHolder);
        return view;
    }

    public void add(User user) {
        users.add(user);
        notifyDataSetChanged();
    }

    private static class UserListViewItem {

        private TextView tvUserName;
        private ImageView ivwProfileImage;
        private TextView textLanguage;
        private TextView textStars;

        public UserListViewItem(View view) {
            tvUserName = view.findViewById(R.id.tvUserName);
            ivwProfileImage = view.findViewById(R.id.ivwProfileImage);
        }

        public void setUser(User user) {
            tvUserName.setText(user.getUserName());
            //ivwProfileImage.setImageBitmap
        }
    }
}
