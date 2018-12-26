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
import java.util.ArrayList;
import app.cooka.cookapp.GlideApp;
import app.cooka.cookapp.R;
import de.hdodenhof.circleimageview.CircleImageView;



public class FolloweeListViewAdapter extends RecyclerView.Adapter<FolloweeListViewAdapter.ViewHolder>{

    // Data for Followees
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private Context mContext;

    public FolloweeListViewAdapter(ArrayList<String> userNames, ArrayList<String> images, ArrayList<String> names, Context context){
        mUsernames = userNames;
        mImages = images;
        mNames = names;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_listview_followees, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        // Loads the ProfilePicture
        GlideApp.with(mContext)
                .asBitmap()
                .load(mImages.get(i))
                .placeholder(R.drawable.ic_account_circle_24dp)
                .error(R.drawable.ic_account_circle_24dp)
                .into(viewHolder.imageView);

        // Set full name
        viewHolder.name.setText(mNames.get(i));
        // Set username
        viewHolder.userName.setText(mUsernames.get(i));

        viewHolder.parentLayout.setOnClickListener(new View.OnClickListener(){
            // TODO redirect to Profile
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
            imageView = itemView.findViewById(R.id.ivwFolloweeCustomListProfileAvatar);
            name = itemView.findViewById(R.id.tvwNameFollowee);
            userName = itemView.findViewById(R.id.tvwUsernameFollowee);
            parentLayout = itemView.findViewById(R.id.customListViewFollowee);
        }
    }
}
