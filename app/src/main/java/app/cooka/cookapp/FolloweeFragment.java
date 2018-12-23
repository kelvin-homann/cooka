package app.cooka.cookapp;

import android.content.Context;
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

import app.cooka.cookapp.model.EFolloweeType;
import app.cooka.cookapp.model.Followee;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.view.FolloweeListViewAdapter;
import app.cooka.cookapp.view.LoadingScreenView;


public class FolloweeFragment extends Fragment {

    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> usernames = new ArrayList<String>();
    ArrayList<String> imgUrls = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initList();
        LoadingScreenView loadingScreen = new LoadingScreenView(getContext());
        View v = inflater.inflate(R.layout.fragment_followee, container, false);
        loadingScreen = v.findViewById(R.id.loading_screen);
        loadingScreen.setVisible(true);

        RecyclerView recyclerView = v.findViewById(R.id.lsvFollowee);
        FolloweeListViewAdapter adapter = new FolloweeListViewAdapter(usernames, imgUrls, names, getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final TextView followerNr = v.findViewById(R.id.tvwFolloweeNr);

        final LoadingScreenView finalLoadingScreen = loadingScreen;
        User.Factory.selectUser(getActivity(),25, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                if (result != null){
                    String followerNrText = getString(R.string.headertextnr_follower, result.getFolloweeCount());
                    followerNr.setText(followerNrText);
                    finalLoadingScreen.setVisible(false);
                }
            }
        });
        return v;
    }

    private void initList(){
        User.Factory.selectUserFollowees(getActivity(), 25, new IResultCallback<List<Followee>>() {
            @Override
            public void onSucceeded(List<Followee> result) {
                names.clear();
                usernames.clear();
                imgUrls.clear();
                for (int i = 0; i < result.size();i++){
                    if (result.get(i).getType() == EFolloweeType.user){
                        names.add(result.get(i).getDetail1() + " " + result.get(i).getDetail2());
                        usernames.add("@" + result.get(i).getDisplayName());
                        if(result.get(i).getImageId() == 0)
                            imgUrls.add("https://i.imgur.com/PHpDSVb.jpg"); // Placeholder Image
                        else
                            imgUrls.add("https://www.sebastianzander.de/cooka/img/" + result.get(i).getImageFileName());
                    }
                    else if (result.get(i).getType() == EFolloweeType.collection){
                        names.add("#" + result.get(i).getDisplayName());
                        usernames.add(null);
                        if(result.get(i).getImageId() == 0)
                            imgUrls.add("https://images.pexels.com/photos/461198/pexels-photo-461198.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260"); // Placeholder Image
                        else
                            imgUrls.add("https://www.sebastianzander.de/cooka/img/" + result.get(i).getImageFileName());
                    }
                    else if (result.get(i).getType() == EFolloweeType.tag){
                        names.add("#" + result.get(i).getDisplayName());
                        usernames.add(null);
                        if(result.get(i).getImageId() == 0)
                            imgUrls.add("https://images.pexels.com/photos/46239/salmon-dish-food-meal-46239.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260"); // Placeholder Image
                        else
                            imgUrls.add("https://www.sebastianzander.de/cooka/img/" + result.get(i).getImageFileName());
                    }
                }
            }
        });
    }
}
