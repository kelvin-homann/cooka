package app.cooka.cookapp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import app.cooka.cookapp.GlideApp;
import app.cooka.cookapp.R;
import app.cooka.cookapp.model.Followee;
import app.cooka.cookapp.model.Follower;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerListViewAdapter extends RecyclerView.Adapter<FollowerListViewAdapter.ViewHolder> {
    // Data for Followers
    private ArrayList<Follower> mFollowers;
    private Context mContext;

    public FollowerListViewAdapter(ArrayList<Follower> followers, Context context){
        mFollowers = followers;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_listview_follower, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if (mFollowers.get(i).getProfileImageId() != 0){
            GlideApp.with(mContext)
                    .asBitmap()
                    .load("https://www.sebastianzander.de/cooka/img/" + mFollowers.get(i).getProfileImageFileName())
                    .placeholder(R.drawable.ic_default_profile_image_24dp)
                    .error(R.drawable.ic_default_profile_image_24dp)
                    .into(viewHolder.imageView);
        }

        // Set full name
        viewHolder.name.setText(mFollowers.get(i).getFirstName() + " " + mFollowers.get(i).getLastName()); // TODO Replace with getFullName
        // Set username
        viewHolder.userName.setText("@" + mFollowers.get(i).getUserName());

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener(){
            // TODO redirect to user profile
            @Override
            public void onClick(View view){
                Toast.makeText(mContext, "Click", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFollowers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imageView;
        TextView name;
        TextView userName;
        ConstraintLayout parentLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivwProfilePicFollower);
            name = itemView.findViewById(R.id.tvwNameFollower);
            userName = itemView.findViewById(R.id.tvwUsernameFollower);
            parentLayout = itemView.findViewById(R.id.customListViewFollower);
        }
    }
}
