package app.cooka.cookapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.User;


public class FollowerFragment extends Fragment {

    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> usernames = new ArrayList<String>();
    ArrayList<String> imgUrls = new ArrayList<String>();
    TextView followerNr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initList();
        View v = inflater.inflate(R.layout.fragment_follower, container, false);
        RecyclerView recyclerView = v.findViewById(R.id.lsvFollower);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(usernames, imgUrls, names, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final TextView followerNr = (TextView) v.findViewById(R.id.tvwFollowerNr);

        User.Factory.selectUser(4, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                if (result != null){
                    String followerNrText = getString(R.string.headertextnr_follower, result.getFollowedCount());
                    followerNr.setText(followerNrText);
                }
            }
        });
        return v;
    }

    private void initList(){
        imgUrls.add("https://images.pexels.com/photos/846741/pexels-photo-846741.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260");
        imgUrls.add("https://images.pexels.com/photos/1239291/pexels-photo-1239291.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260");
        imgUrls.add("https://images.pexels.com/photos/733872/pexels-photo-733872.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260");
        imgUrls.add("https://images.pexels.com/photos/415829/pexels-photo-415829.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260");

        names.add("Jürgen");
        names.add("Petra");
        names.add("Peter");
        names.add("Hans");

        usernames.add("Homann");
        usernames.add("Test53");
        usernames.add("lalalla");
        usernames.add("lolhaha");
    }
}
