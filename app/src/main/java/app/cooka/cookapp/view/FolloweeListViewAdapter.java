package app.cooka.cookapp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import app.cooka.cookapp.GlideApp;
import app.cooka.cookapp.ProfileActivity;
import app.cooka.cookapp.R;
import app.cooka.cookapp.UserProfileActivity;
import app.cooka.cookapp.model.Collection;
import app.cooka.cookapp.model.EFolloweeType;
import app.cooka.cookapp.model.FollowCollectionResult;
import app.cooka.cookapp.model.FollowTagResult;
import app.cooka.cookapp.model.FollowUserResult;
import app.cooka.cookapp.model.Followee;
import app.cooka.cookapp.model.IFollowCollectionCallback;
import app.cooka.cookapp.model.IFollowTagCallback;
import app.cooka.cookapp.model.IFollowUserCallback;
import app.cooka.cookapp.model.Tag;
import app.cooka.cookapp.model.User;
import de.hdodenhof.circleimageview.CircleImageView;



public class FolloweeListViewAdapter extends RecyclerView.Adapter<FolloweeListViewAdapter.ViewHolder>{
    // Data for Followees
    private ArrayList<Followee> mFollowees;
    private Context mContext;
    private Button followButton;

    public FolloweeListViewAdapter(ArrayList<Followee> followees , Context context){
        mFollowees = followees;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_listview_followees, viewGroup, false);
        followButton = view.findViewById(R.id.btnFollowFollowee);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        if (mFollowees.get(i).getType() == EFolloweeType.user){
            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User.Factory.followUser(mContext, mFollowees.get(i).getId(), new IFollowUserCallback() {
                        @Override
                        public void onSucceeded(FollowUserResult followUserResult) {
                            followButton.setText(R.string.profile_followed);
                        }

                        @Override
                        public void onFailed(FollowUserResult followUserResult) {

                        }
                    });
                }
            });

            // Loads the ProfilePicture
            if (mFollowees.get(i).getImageId() != 0){
                GlideApp.with(mContext)
                        .asBitmap()
                        .load("https://www.sebastianzander.de/cooka/img/" + mFollowees.get(i).getImageFileName())
                        .placeholder(R.drawable.ic_default_profile_image_24dp)
                        .error(R.drawable.ic_default_profile_image_24dp)
                        .into(viewHolder.imageView);
            }

            // Set full name
            viewHolder.name.setText(mFollowees.get(i).getDetail1() + " " + mFollowees.get(i).getDetail2());
            // Set username
            viewHolder.userName.setText("@" + mFollowees.get(i).getDisplayName());

            viewHolder.parentLayout.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Bundle b = new Bundle();
                    b.putLong("userid", mFollowees.get(i).getId());
                    Intent intent = new Intent(mContext, UserProfileActivity.class);
                    intent.putExtras(b); //Put your id to your next Intent
                    mContext.startActivity(intent);
                }
            });
        }
        else if (mFollowees.get(i).getType() == EFolloweeType.collection){

            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Collection.Factory.followCollection(mContext, mFollowees.get(i).getId(), new IFollowCollectionCallback() {
                        @Override
                        public void onSucceeded(FollowCollectionResult followCollectionResult) {
                            followButton.setText(R.string.profile_followed);
                        }

                        @Override
                        public void onFailed(FollowCollectionResult followCollectionResult) {

                        }
                    });

                }
            });

            String nameText = null;
            // Loads the ProfilePicture
            if (mFollowees.get(i).getImageId() != 0){
                GlideApp.with(mContext)
                        .asBitmap()
                        .load("https://www.sebastianzander.de/cooka/img/" + mFollowees.get(i).getImageFileName())
                        .placeholder(R.drawable.ic_default_profile_image_24dp)
                        .error(R.drawable.ic_default_profile_image_24dp)
                        .into(viewHolder.imageView);
            }

            // Set full name
            if (mFollowees.get(i).getDetail1() != null && mFollowees.get(i).getDetail2() == null){
                nameText = mFollowees.get(i).getDetail1();
            }
            else if (mFollowees.get(i).getDetail1() == null && mFollowees.get(i).getDetail2() != null){
                nameText = mFollowees.get(i).getDetail2();
            }
            else if (mFollowees.get(i).getDetail1() != null && mFollowees.get(i).getDetail2() != null){
                nameText = mFollowees.get(i).getDetail1() + " " + mFollowees.get(i).getDetail2();
            }

            viewHolder.name.setText(nameText);
            // Set username
            viewHolder.userName.setText(null);
        }
        else if (mFollowees.get(i).getType() == EFolloweeType.tag){

            Tag.Factory.followTag(mContext, mFollowees.get(i).getId(), new IFollowTagCallback() {
                @Override
                public void onSucceeded(FollowTagResult followTagResult) {
                    followButton.setText(R.string.profile_followed);
                }

                @Override
                public void onFailed(FollowTagResult followTagResult) {

                }
            });

            // Loads the ProfilePicture
            if (mFollowees.get(i).getImageId() != 0){
                GlideApp.with(mContext)
                        .asBitmap()
                        .load("https://www.sebastianzander.de/cooka/img/" + mFollowees.get(i).getImageFileName())
                        .placeholder(R.drawable.ic_default_profile_image_24dp)
                        .error(R.drawable.ic_default_profile_image_24dp)
                        .into(viewHolder.imageView);
            }
            // Set full name
            viewHolder.name.setText("#" + mFollowees.get(i).getDisplayName());
            // Set username
            viewHolder.userName.setText(null);
        }

    }

    @Override
    public int getItemCount() {
        return mFollowees.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imageView;
        TextView name;
        TextView userName;
        ConstraintLayout parentLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivwFolloweeCustomListProfileAvatar);
            name = itemView.findViewById(R.id.tvwNameFollowee);
            userName = itemView.findViewById(R.id.tvwUsernameFollowee);
            parentLayout = itemView.findViewById(R.id.customListViewFollowee);
        }
    }
}
