package app.cooka.cookapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.Follower;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.view.FollowerListViewAdapter;
import app.cooka.cookapp.view.LoadingScreenView;

import static com.facebook.FacebookSdk.getApplicationContext;

public class FollowerFragment extends Fragment {
    // Data for the Followers
    ArrayList<Follower> followers = new ArrayList<>();

    RecyclerView rvwFollower;
    FollowerListViewAdapter followerListViewAdapter;
    LoadingScreenView loadingScreen;
    private LoginManager loginManager;
    private long userid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("COOKALOG", "FollowerFragment onCreate Arguments" + String.valueOf(getArguments()));

        if (getArguments() != null) {
            userid = getArguments().getLong("userid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follower, container, false);
        // Initialisation of the FollowerList
        initList(userid);

        // Init LoadingScreen
        loadingScreen = new LoadingScreenView(getContext());
        loadingScreen = view.findViewById(R.id.loading_screen);
        loadingScreen.setVisible(true);

        // Init RecyclerView to Display Follower List
        rvwFollower = view.findViewById(R.id.lsvFollower);
        followerListViewAdapter = new FollowerListViewAdapter(followers, getContext());
        rvwFollower.setAdapter(followerListViewAdapter);
        rvwFollower.setLayoutManager(new LinearLayoutManager(getActivity()));

        // ViewHolder for the Follower count
        final TextView followerNr = (TextView) view.findViewById(R.id.tvwFollowerNr);

        // Get followernr
        User.Factory.selectUser(getActivity(),userid, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                if (result != null){
                    String followerNrText = getString(R.string.headertextnr_follower, result.getFollowerCount());
                    followerNr.setText(followerNrText);

                    // Disable LoadingScreen once data is loaded
                    loadingScreen.setVisible(false);
                }
            }
        });
        return view;
    }


    /**
     * Fills the ArrayLists needed to Display the FollowerList
     * Loops through the returned ArrayList and adds the Data from
     * the Object to the correct ArrayLists in this class
     *
     * @param userid userId to set the User
     *
     */
    private void initList(long userid){
        User.Factory.selectUserFollowers(getActivity(), userid, new IResultCallback<List<Follower>>() {
            @Override
            public void onSucceeded(List<Follower> result) {
                followers.clear();

                followers.addAll(result);

                followerListViewAdapter.notifyDataSetChanged();
            }
        });
    }
}
