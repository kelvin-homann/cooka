package app.cooka.cookapp.view;

import android.content.Context;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import app.cooka.cookapp.GlideApp;
import app.cooka.cookapp.R;
import app.cooka.cookapp.model.EFeedMessageType;
import app.cooka.cookapp.model.FeedMessage;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Data for Feed
    private ArrayList<FeedMessage> mFeed;
    private Context mContext;

    public ProfileFeedAdapter(ArrayList<FeedMessage> feed, Context context){
        mFeed = feed;
        mContext = context;
    }

    /**
     * Returns day difference between Feed Entry creation and current date
     *
     * @param date performedDateTime of Feed Entry
     * @return days between today and feed entry creation
     */
    private String getDate(Date date){
        int days = 0;
        LocalDate today= null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            today = LocalDate.now();
            LocalDate date2 = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            days = Period.between(date2, today).getDays();
        }
        else {
            return "";
        }

        if (days <= 0)
            return "0 days ago";
        else if (days == 1)
            return String.valueOf(days) + " day ago";
        else
            return String.valueOf(days) + " days ago";
    }

    @Override
    public int getItemViewType(int position) {
        if (mFeed.get(position).getType() == EFeedMessageType.followedUser)
            return 0;
        else if (mFeed.get(position).getType() == EFeedMessageType.followedTag)
            return 0;
        else if (mFeed.get(position).getType() == EFeedMessageType.followedCollection)
            return 0;
        else if (mFeed.get(position).getType() == EFeedMessageType.addedImageToRecipe)
            return 2;
        else if (mFeed.get(position).getType() == EFeedMessageType.createdCollection)
            return 2;
        else if (mFeed.get(position).getType() == EFeedMessageType.createdRecipe)
            return 2;
        else if (mFeed.get(position).getType() == EFeedMessageType.cookedRecipe)
            return 2;
        else if (mFeed.get(position).getType() == EFeedMessageType.modifiedRecipe)
            return 0;
        else if (mFeed.get(position).getType() == EFeedMessageType.addedRecipeToCollection)
            return 0;
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        switch (i){
            case 0: // Followed User
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_listview_feed, viewGroup, false);
                return new ViewHolder0(view);
            case 2: // Followed Tag
                View view2 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_listview_feed_big, viewGroup, false);
                return new ViewHolder2(view2);
            default:
                View view_default = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_listview_feed, viewGroup, false);
                return new ViewHolder0(view_default);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        switch (viewHolder.getItemViewType()) {
            case 0:
                ViewHolder0 viewHolder1 = (ViewHolder0)viewHolder;
                GlideApp.with(mContext)
                        .asBitmap()
                        .load("https://www.sebastianzander.de/cooka/img/" + mFeed.get(i).getObject1ImageFileName())
                        .placeholder(R.drawable.ic_default_profile_image_24dp)
                        .error(R.drawable.ic_default_profile_image_24dp)
                        .into(viewHolder1.imageView);

                if (mFeed.get(i).getType() == EFeedMessageType.followedTag)
                    viewHolder1.name.setText("#" + mFeed.get(i).getObject1Name());
                else
                    viewHolder1.name.setText(mFeed.get(i).getObject1Name());
                viewHolder1.userName.setText(null);
                viewHolder1.message.setText(mFeed.get(i).getMessage());
                viewHolder1.timestamp.setText(getDate(mFeed.get(i).getPerformedDateTime()));

                viewHolder1.parentLayout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Toast.makeText(mContext, "Click", Toast.LENGTH_LONG).show();
                    }
                });
                break;

            case 2:
                ViewHolder2 viewHolder2 = (ViewHolder2)viewHolder;
                if (!mFeed.get(i).getObject1ImageFileName().equals("")){
                    GlideApp.with(mContext)
                            .asBitmap()
                            .load("https://www.sebastianzander.de/cooka/img/" + mFeed.get(i).getUserImageFileName())
                            .placeholder(R.drawable.ic_default_profile_image_24dp)
                            .error(R.drawable.ic_default_profile_image_24dp)
                            .into(viewHolder2.imageView);
                }

                GlideApp.with(mContext)
                        .asBitmap()
                        .load("https://www.sebastianzander.de/cooka/img/" + mFeed.get(i).getObject1ImageFileName())
                        .centerCrop()
                        .placeholder(R.drawable.ic_default_profile_image_24dp)
                        .error(R.drawable.ic_default_profile_image_24dp)
                        .into(viewHolder2.bigPicture);

                viewHolder2.name.setText(mFeed.get(i).getUserName());
                viewHolder2.userName.setText(mFeed.get(i).getObject1Name());
                viewHolder2.message.setText(mFeed.get(i).getMessage());
                viewHolder2.timestamp.setText(getDate(mFeed.get(i).getPerformedDateTime()));

                viewHolder2.parentLayout.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        Toast.makeText(mContext, "Click", Toast.LENGTH_LONG).show();
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mFeed.size();
    }

    class ViewHolder0 extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView name;
        TextView userName;
        TextView message;
        TextView timestamp;
        ConstraintLayout parentLayout;
        ViewHolder0(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.ivwCustomFeedAvatar);
            name = itemView.findViewById(R.id.tvwFeedNames);
            userName = itemView.findViewById(R.id.tvwFeedUsername);
            message = itemView.findViewById(R.id.tvwFeedMessage);
            timestamp = itemView.findViewById(R.id.tvwFeedTime);
            parentLayout = itemView.findViewById(R.id.customListViewFeed);
        }
    }

    class ViewHolder2 extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        ImageView bigPicture;
        TextView name;
        TextView userName;
        TextView message;
        TextView timestamp;
        ConstraintLayout parentLayout;
        ViewHolder2(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.ivwCustomFeedAvatarBig);
            name = itemView.findViewById(R.id.tvwFeedNamesBig);
            userName = itemView.findViewById(R.id.tvwFeedUsernameBig);
            message = itemView.findViewById(R.id.tvwFeedMessageBig);
            bigPicture = itemView.findViewById(R.id.ivwFeedBigPicture);
            timestamp = itemView.findViewById(R.id.tvwFeedTimeBig);
            parentLayout = itemView.findViewById(R.id.customListViewFeedBig);
        }
    }
}