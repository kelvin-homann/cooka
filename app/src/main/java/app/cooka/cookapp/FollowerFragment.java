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
    // Data for the Followers
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> usernames = new ArrayList<String>();
    ArrayList<String> imgUrls = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_follower, container, false);
        // Initialisation of the FollowerList
        initListDummy();
//        initList(25);

        // Init LoadingScreen
        LoadingScreenView loadingScreen = new LoadingScreenView(getContext());
        loadingScreen = v.findViewById(R.id.loading_screen);
        loadingScreen.setVisible(true);

        // Init RecyclerView to Display Follower List
        RecyclerView recyclerView = v.findViewById(R.id.lsvFollower);
        FollowerListViewAdapter adapter = new FollowerListViewAdapter(usernames, imgUrls, names, getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // ViewHolder for the Follower count
        final TextView followerNr = (TextView) v.findViewById(R.id.tvwFollowerNr);

        // new initalisation of the LoadingScreenView to be used in @Override Method
        final LoadingScreenView finalLoadingScreen = loadingScreen;

        // Get followernr
        User.Factory.selectUser(getActivity(),25, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                if (result != null){
                    String followerNrText = getString(R.string.headertextnr_follower, result.getFollowerCount());
                    followerNr.setText(followerNrText);

                    // Disable LoadingScreen once data is loaded
                    finalLoadingScreen.setVisible(false);
                }
            }
        });
        return v;
    }

    private void initListDummy(){
        names.clear();
        usernames.clear();
        imgUrls.clear();
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
        imgUrls.add("https://i.imgur.com/WLlkPsF.jpg");
        names.add("Kalle");
        usernames.add("@Kalle");
    }

    /**
     * Fills the ArrayLists needed to Display the FollowerList
     * Loops through the returned ArrayList and adds the Data from
     * the Object to the correct ArrayLists in this class
     *
     * @param userid userId to set the User
     *
     */
    private void initList(int userid){
        User.Factory.selectUserFollowers(getActivity(), userid, new IResultCallback<List<Follower>>() {
            @Override
            public void onSucceeded(List<Follower> result) {
                // TODO refine to differ between cases
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
