package app.cooka.cookapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.cooka.cookapp.model.FeedMessage;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.view.LoadingScreenView;
import app.cooka.cookapp.view.ProfileFeedViewAdapter;

public class ProfileFragment extends Fragment {
    // Data for the Feed
    ArrayList<FeedMessage> feed = new ArrayList<FeedMessage>();

    // Fragments for the Follower and Followee lists
    private FollowerFragment followerFragment;
    private FolloweeFragment followeeFragment;

    RecyclerView rvwFeed;
    ProfileFeedViewAdapter feedListAdapter;
    LoadingScreenView loadingScreen;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // Initialisation of the Feed
        initFeed(25);

        followerFragment = new FollowerFragment();
        followeeFragment = new FolloweeFragment();

        // Loading Screen
        loadingScreen = new LoadingScreenView(getContext());
        loadingScreen = view.findViewById(R.id.loading_screen);
        loadingScreen.setVisible(true);

        // RecyclerView of the Feed
        rvwFeed = view.findViewById(R.id.rvwProfileFeed);
        feedListAdapter = new ProfileFeedViewAdapter(feed, getContext());
        rvwFeed.setAdapter(feedListAdapter);
        rvwFeed.setHasFixedSize(true);
        rvwFeed.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Setting divider between Feed items
        DividerItemDecoration itemDecorator = new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.feed_divider)));
        rvwFeed.addItemDecoration(itemDecorator);

        // ViewHolders for the Profile
        final TextView profileFullname = view.findViewById(R.id.tvwName);
        final TextView profileUsername = view.findViewById(R.id.tvwUsername);
        final TextView profileFollower = view.findViewById(R.id.tvwFollower);
        final TextView profileFollowing = view.findViewById(R.id.tvwFollowing);
        final TextView profileFollowerNr = view.findViewById(R.id.tvwFollowernr);
        final TextView profileFollowingNr = view.findViewById(R.id.tvwFollowingnr);
        final ImageView profileAvatar = view.findViewById(R.id.ivwProfilePicFollower);
        final CollapsingToolbarLayout collapsingToolbarLayout = view.findViewById(R.id.ctlProfileBar);

        // selectUser-Data to Display on Profile
        User.Factory.selectUser(getActivity(),25, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                // if user is not empty
                if (result != null){
                    // TODO replace with getFullName()
                    String nameText = getString(R.string.profile_name, (result.getFirstName() + " " + result.getLastName()));
                    String userNameText = getString(R.string.profile_username, (result.getUserName()));
                    String followingNrText = getString(R.string.profile_following_nr, result.getFollowerCount());
                    String followerNrText = getString(R.string.profile_follower_nr, result.getFolloweeCount());
                    profileFullname.setText(nameText);
                    profileUsername.setText(userNameText);
                    profileFollowerNr.setText(followerNrText);
                    profileFollowingNr.setText(followingNrText);
                    collapsingToolbarLayout.setTitle(nameText);

                    // If getProfileImageId() == 0 the user did not upload a profile picture
                    if (result.getProfileImageId() != 0){
                        // Load ProfileImage
                        GlideApp.with(profileAvatar.getContext())
                                .asBitmap()
                                .load("https://www.sebastianzander.de/cooka/img/" + result.getProfileImageFileName())
                                .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_account_circle_24dp)
                                .error(R.drawable.ic_account_circle_24dp)
                                .centerCrop())
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        // Disable LoadingScreen once Profile Picture is loaded
                                        loadingScreen.setVisible(false);
                                        return false;
                                    }
                                })
                                .into(profileAvatar);
                    }
                }
            }
        });

        profileFollower.setOnClickListener(new View.OnClickListener() {
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

        profileFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Starts the Followee Fragment
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

    /**
     * Fills the ArrayLists needed to Display the Feed
     * Loops through the returned ArrayList and adds the Data from
     * the Object to the correct ArrayLists in this class
     * @param userId userId to set the User
     *
     */
    private void initFeed(int userId){
        FeedMessage.Factory.selectFeedMessages(getContext(), userId, FeedMessage.ST_ALL, true, new IResultCallback<List<FeedMessage>>() {
            @Override
            public void onSucceeded(List<FeedMessage> result) {
                feed.clear();

                feed.addAll(result);

                feedListAdapter.notifyDataSetChanged();
            }
        });
    }
}
