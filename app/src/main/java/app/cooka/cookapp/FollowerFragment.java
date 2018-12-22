package app.cooka.cookapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.model.Follower;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.view.FollowerListViewAdapter;
import app.cooka.cookapp.view.LoadingScreenView;


public class FollowerFragment extends Fragment {

    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> usernames = new ArrayList<String>();
    ArrayList<String> imgUrls = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoadingScreenView loadingScreen = new LoadingScreenView(getContext());
        View v = inflater.inflate(R.layout.fragment_follower, container, false);
        loadingScreen = v.findViewById(R.id.loading_screen);
        loadingScreen.setVisible(true);
        initList();

        RecyclerView recyclerView = v.findViewById(R.id.lsvFollower);
        FollowerListViewAdapter adapter = new FollowerListViewAdapter(usernames, imgUrls, names, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final TextView followerNr = (TextView) v.findViewById(R.id.tvwFollowerNr);

        final LoadingScreenView finalLoadingScreen = loadingScreen;
        User.Factory.selectUser(getActivity(),4, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                if (result != null){
                    String followerNrText = getString(R.string.headertextnr_follower, result.getFollowerCount());
                    followerNr.setText(followerNrText);
                    finalLoadingScreen.setVisible(false);
                }
            }
        });
        return v;
    }

    private void initList(){
        User.Factory.selectUserFollowers(getActivity(), 4, new IResultCallback<List<Follower>>() {
            @Override
            public void onSucceeded(List<Follower> result) {
                for (int i = 0; i < result.size();i++){
                    names.add(result.get(i).getFirstName());
                    usernames.add(result.get(i).getUserName());
                    imgUrls.add("https://www.sebastianzander.de/cooka/img/" + result.get(i).getProfileImageFileName());
                }
                Log.d("COOKALOG", String.valueOf(result.size()));
            }
        });
    }
}
