package app.cooka.cookapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class FollowerFragment extends Fragment {

    String[] Names = {"Jackson Pot", "Hans Down", "Fleece Marigold", "Norman Gordon", "Hilary Ouse", "Spruce Springclean", "Jackson Pot", "Hans Down", "Fleece Marigold", "Norman Gordon", "Hilary Ouse", "Spruce Springclean"};
    String[] Usernames = {"JPot", "hDown68", "GoldieF", "gNorm12", "HilaryOOO", "BruceSprinkler", "JPot", "hDown68", "GoldieF", "gNorm12", "HilaryOOO", "BruceSprinkler"};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_follower, container, false);
        ListView listView = (ListView)v.findViewById(R.id.lsvFollower);

        CustomAdapter customAdapter = new CustomAdapter();

        listView.setAdapter(customAdapter);

        return v;
    }

    class CustomAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return Names.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Redo with RecyclerView
            convertView = getLayoutInflater().inflate(R.layout.custom_listview_follow, null);

            TextView textView_name = (TextView)convertView.findViewById(R.id.tvwNameFollower);
            TextView textView_username = (TextView)convertView.findViewById(R.id.tvwUsernameFollower);
            textView_name.setText(Names[position]);
            String text = getString(R.string.text_username_customlvw, Usernames[position]);
            textView_username.setText(text);

            return convertView;
        }
    }
}
