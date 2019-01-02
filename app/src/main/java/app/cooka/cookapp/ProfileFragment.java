package app.cooka.cookapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.FeedMessage;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.User;
import app.cooka.cookapp.view.FeedMessageRecyclerViewAdapter;
import app.cooka.cookapp.view.LoadingScreenView;
import app.cooka.cookapp.view.ProfileFeedViewAdapter;

import static com.facebook.FacebookSdk.getApplicationContext;

public class ProfileFragment extends Fragment {
    // Data for the Feed
    ArrayList<FeedMessage> feed = new ArrayList<FeedMessage>();

    RecyclerView rvwFeed;
    FeedMessageRecyclerViewAdapter feedListAdapter;
    LoadingScreenView loadingScreen;
    private LoginManager loginManager;
    private long userid;

    // Fragments for the Follower and Followee lists
    private FollowerFragment followerFragment;
    private FolloweeFragment followeeFragment;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginManager = LoginManager.Factory.getInstance(getApplicationContext());

        if(loginManager.getUserId() != 0L) {
            userid = loginManager.getUserId();
        }

        Log.d("COOKALOG", "onAttach " + String.valueOf(userid));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Set Arguments for followee and FollowerFragment
        Bundle bundle = new Bundle();
        bundle.putLong("userid", userid);

        // Initialisation of the Feed
        initFeed(userid);

        followerFragment = new FollowerFragment();
        followeeFragment = new FolloweeFragment();

        followerFragment.setArguments(bundle);
        followeeFragment.setArguments(bundle);

        // Loading Screen
        loadingScreen = new LoadingScreenView(getContext());
        loadingScreen = view.findViewById(R.id.loading_screen);
        loadingScreen.setVisible(true);

        // RecyclerView of the Feed
        rvwFeed = view.findViewById(R.id.rvwProfileFeed);
        feedListAdapter = new FeedMessageRecyclerViewAdapter(feed);
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
        final ImageView profileAvatar = view.findViewById(R.id.ivwProfilePicFollower);
        final CollapsingToolbarLayout collapsingToolbarLayout = view.findViewById(R.id.ctlProfileBar);

        // selectUser-Data to Display on Profile
        User.Factory.selectUser(getActivity(),userid, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                String nameText = null;
                String userNameText;
                String followerText;
                String followingText;

                // if user is not empty
                if (result != null){

                    // Make Follower and FolloweeCount Bold, Bigger and Green
                    if (result.getFolloweeCount() >= 10){
                        followerText = getString(R.string.profile_following_text, result.getFolloweeCount());

                        SpannableString spanFollowerText=  new SpannableString(followerText);
                        spanFollowerText.setSpan(new RelativeSizeSpan(1.3f), 0,2, 0); // set size
                        spanFollowerText.setSpan(new ForegroundColorSpan(Color.rgb(110,199,86)), 0, 2, 0);// set color
                        spanFollowerText.setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        profileFollowing.setText(spanFollowerText);
                    }
                    else {
                        followerText = getString(R.string.profile_following_text, result.getFolloweeCount());
                        SpannableString spanFollowerText=  new SpannableString(followerText);
                        spanFollowerText.setSpan(new RelativeSizeSpan(1.3f), 0,1, 0); // set size
                        spanFollowerText.setSpan(new ForegroundColorSpan(Color.rgb(110,199,86)), 0, 2, 0);// set color
                        spanFollowerText.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        profileFollowing.setText(spanFollowerText);
                    }

                    if (result.getFollowerCount() >= 10){
                        followingText = getString(R.string.profile_follower_text, result.getFollowerCount());
                        SpannableString spanFolloweeText=  new SpannableString(followingText);
                        spanFolloweeText.setSpan(new RelativeSizeSpan(1.3f), 0,2, 0); // set size
                        spanFolloweeText.setSpan(new ForegroundColorSpan(Color.rgb(110,199,86)), 0, 2, 0);// set color
                        spanFolloweeText.setSpan(new StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        profileFollower.setText(spanFolloweeText);
                    }
                    else {
                        followingText = getString(R.string.profile_follower_text, result.getFollowerCount());
                        SpannableString spanFolloweeText=  new SpannableString(followingText);
                        spanFolloweeText.setSpan(new RelativeSizeSpan(1.3f), 0,1, 0); // set size
                        spanFolloweeText.setSpan(new ForegroundColorSpan(Color.rgb(110,199,86)), 0, 2, 0);// set color
                        spanFolloweeText.setSpan(new StyleSpan(Typeface.BOLD), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        profileFollower.setText(spanFolloweeText);
                    }

                    // Check whether or not User set a FullName
                    if (result.getFirstName() != null && result.getLastName() == null){
                        nameText = getString(R.string.profile_name, (result.getFirstName()));
                    }
                    else if (result.getFirstName() == null && result.getLastName() != null){
                        nameText = getString(R.string.profile_name, (result.getLastName()));
                    }
                    else if (result.getFirstName() != null && result.getLastName() != null){
                        nameText = getString(R.string.profile_name, (result.getFirstName() + " " + result.getLastName()));
                    }
                    userNameText = getString(R.string.profile_username, (result.getUserName()));
                    profileFullname.setText(nameText);
                    profileUsername.setText(userNameText);
                    collapsingToolbarLayout.setTitle(nameText);

                    // If getProfileImageId() == 0 the user did not upload a profile picture
                    if (result.getProfileImageId() != 0){
                        // Load ProfileImage
                        GlideApp.with(profileAvatar.getContext())
                                .asBitmap()
                                .load("https://www.sebastianzander.de/cooka/img/" + result.getProfileImageFileName())
                                .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_default_profile_image_24dp)
                                .error(R.drawable.ic_default_profile_image_24dp)
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
    private void initFeed(long userId){
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
