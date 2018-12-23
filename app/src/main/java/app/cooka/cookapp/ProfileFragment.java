package app.cooka.cookapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.module.AppGlideModule;


import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.ISelectCallback;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.view.LoadingScreenView;

public class ProfileFragment extends Fragment {

    private FollowerFragment followerFragment;
    private FolloweeFragment followeeFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        followerFragment = new FollowerFragment();
        followeeFragment = new FolloweeFragment();


        LoadingScreenView loadingScreen = new LoadingScreenView(getContext());
        //loading Screen
        loadingScreen = view.findViewById(R.id.loading_screen);
        loadingScreen.setVisible(true);

        final TextView name = view.findViewById(R.id.tvwName);
        final TextView userName = view.findViewById(R.id.tvwUsername);
        final TextView followers = view.findViewById(R.id.tvwFollower);
        final TextView following = view.findViewById(R.id.tvwFollowing);
        final TextView followerNr = view.findViewById(R.id.tvwFollowernr);
        final TextView followingNr = view.findViewById(R.id.tvwFollowingnr);
        final ImageView profilePicture = view.findViewById(R.id.ivwProfilePicFollower);
        final CollapsingToolbarLayout collapsingToolbarLayout = view.findViewById(R.id.ctlProfileBar);

        LoginManager.Factory.getInstance(getActivity());

        final LoadingScreenView finalLoadingScreen = loadingScreen;
        User.Factory.selectUser(getActivity(),25, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                if (result != null){
                    String nameText = getString(R.string.profile_name, (result.getFirstName() + " " + result.getLastName()));
                    String userNameText = getString(R.string.profile_username, (result.getUserName()));
                    String followingNrText = getString(R.string.profile_following_nr, result.getFollowerCount());
                    String followerNrText = getString(R.string.profile_follower_nr, result.getFolloweeCount());
                    name.setText(nameText);
                    userName.setText(userNameText);
                    followerNr.setText(followerNrText);
                    followingNr.setText(followingNrText);
                    collapsingToolbarLayout.setTitle(nameText);

                    if (result.getProfileImageId() != 0){
                        finalLoadingScreen.setVisible(false);
                        GlideApp.with(getContext())
                                .asBitmap()
                                .load("https://www.sebastianzander.de/cooka/img/" + result.getProfileImageFileName())
                                .apply(new RequestOptions()
                                .placeholder(R.drawable.default_avatar)
                                .error(R.drawable.default_avatar)
                                .centerCrop())
                                .into(profilePicture);
                    }
                }
            }
        });

        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Starts the FollowerFragment
                FragmentTransaction transaction = null;
                if (getFragmentManager() != null) {
                    transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_frame_profile, followerFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = null;
                if (getFragmentManager() != null) {
                    transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_frame_profile, followeeFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }
        });
        return view;
    }

    private void initFeed(){

    }
}
