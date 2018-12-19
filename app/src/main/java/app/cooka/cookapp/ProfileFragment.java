package app.cooka.cookapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
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

import org.w3c.dom.Text;

import app.cooka.cookapp.login.LoginManager;
import app.cooka.cookapp.model.IResultCallback;
import app.cooka.cookapp.model.ISelectCallback;
import app.cooka.cookapp.model.User;

public class ProfileFragment extends Fragment {

    private FollowerFragment followerFragment;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        followerFragment = new FollowerFragment();

        final TextView name = (TextView) view.findViewById(R.id.tvwName);
        final TextView userName = (TextView) view.findViewById(R.id.tvwUsername);
        final TextView followers = (TextView) view.findViewById(R.id.tvwFollower);
        final TextView following = (TextView) view.findViewById(R.id.tvwFollowing);
        final TextView followerNr = (TextView) view.findViewById(R.id.tvwFollowernr);
        final TextView followingNr = (TextView) view.findViewById(R.id.tvwFollowingnr);
        final ImageView profilePicture = (ImageView) view.findViewById(R.id.ivwProfilePicFollower);
        final ImageView addButton = (ImageView) view.findViewById(R.id.ivwAdd);

        LoginManager.Factory.getInstance(getActivity());

        User.Factory.selectUser(4, new IResultCallback<User>() {
            @Override
            public void onSucceeded(User result) {
                if (result != null){
                    String nameText = getString(R.string.profile_name, (result.getFirstName() + " " + result.getLastName()));
                    String userNameText = getString(R.string.profile_username, (result.getUserName()));
                    String followingNrText = getString(R.string.profile_following_nr, result.getFollowingCount());
                    String followerNrText = getString(R.string.profile_follower_nr, result.getFollowedCount());
                    name.setText(nameText);
                    userName.setText(userNameText);
                    followerNr.setText(followerNrText);
                    followingNr.setText(followingNrText);
                    if (result.getProfileImageId() != 0){
                        Glide.with(getContext())
                                .asBitmap()
                                .load("https://www.sebastianzander.de/cooka/img/" + result.getProfileImageFileName())
                                .into(profilePicture);
                    }
                }
            }
        });

        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getActivity(), "Add User", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getActivity(), "FollowerFragment", Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }
}
