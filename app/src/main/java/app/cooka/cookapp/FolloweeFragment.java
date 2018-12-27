package app.cooka.cookapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.model.Followee;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.view.FolloweeListViewAdapter;
import app.cooka.cookapp.view.LoadingScreenView;

public class FolloweeFragment extends Fragment {
    // Data for the Followees
    ArrayList<Followee> followees = new ArrayList<>();

    RecyclerView rvwFollowee;
    FolloweeListViewAdapter followeeListViewAdapter;
    LoadingScreenView loadingScreen;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followee, container, false);
        // Initialisation of the FolloweeList
        initList(25);

        // LoadingScreen
        loadingScreen = new LoadingScreenView(getContext());
        loadingScreen = view.findViewById(R.id.loading_screen);
        loadingScreen.setVisible(true);

        // Init RecyclerView to Display Followee List
        rvwFollowee = view.findViewById(R.id.lsvFollowee);
        followeeListViewAdapter = new FolloweeListViewAdapter(/*usernames, imgUrls, names,*/followees, getContext());
        rvwFollowee.setAdapter(followeeListViewAdapter);
        rvwFollowee.setHasFixedSize(true);
        rvwFollowee.setLayoutManager(new LinearLayoutManager(getActivity()));

        // ViewHolder for the Followee count
        final TextView followeeNr = view.findViewById(R.id.tvwFolloweeNr);

        // Get followeenr
        User.Factory.selectUser(getActivity(),25, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                if (result != null){
                    String followerNrText = getString(R.string.headertextnr_follower, result.getFolloweeCount());
                    followeeNr.setText(followerNrText);
                    loadingScreen.setVisible(false);
                }
            }
        });
        return view;
    }

    /**
     * Fills the ArrayLists needed to Display the FolloweeList
     * Loops through the returned ArrayList and adds the Data from
     * the Object to the correct ArrayLists in this class
     * @param userid userId to set the User
     *
     */
    private void initList(int userid){

        User.Factory.selectUserFollowees(getActivity(), userid, new IResultCallback<List<Followee>>() {
            @Override
            public void onSucceeded(List<Followee> result) {
                followees.clear();

                followees.addAll(result);

                followeeListViewAdapter.notifyDataSetChanged();
            }

        });
    }
}
