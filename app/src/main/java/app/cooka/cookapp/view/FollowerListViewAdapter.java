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

import app.cooka.cookapp.R;
import de.hdodenhof.circleimageview.CircleImageView;

public class FollowerListViewAdapter extends RecyclerView.Adapter<FollowerListViewAdapter.ViewHolder> {

    // Data for Followers
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    public FollowerListViewAdapter(ArrayList<String> userNames, ArrayList<String> images, ArrayList<String> names, Context context){
        mUsernames = userNames;
        mImages = images;
        mNames = names;
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
        // Load Profile Picture
        Glide.with(mContext)
                .asBitmap()
                .load(mImages.get(i))
                .into(viewHolder.imageView);

        // Load Full Name
        viewHolder.name.setText(mNames.get(i));
        // Load Username
        viewHolder.userName.setText(mUsernames.get(i));

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
        return mNames.size();
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
