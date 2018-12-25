package app.cooka.cookapp.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import app.cooka.cookapp.R;
import app.cooka.cookapp.model.EFeedMessageType;
import app.cooka.cookapp.model.FeedMessage;
import app.cooka.cookapp.utils.TimeUtils;

public class FeedMessageRecyclerViewAdapter extends
    RecyclerView.Adapter<FeedMessageRecyclerViewAdapter.ViewHolder>
{
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        // each data item is just a string in this case
        public static Drawable defaultProfileImage;
        public ImageView ivwUserProfileImage;
        public ImageView ivwContextMenuButton;
        public TextView tvwUserName;
        public TextView tvwUserDescription;
        public ImageView ivwImage;
        public TextView tvwMessageHeading;
        public TextView tvwMessageDateTime;
        public Context context;
        public ViewHolder(View view, Context context) {
            super(view);
            this.ivwUserProfileImage = view.findViewById(R.id.ivwUserProfileImage);
            this.ivwContextMenuButton = view.findViewById(R.id.ivwContextMenuButton);
            this.tvwUserName = view.findViewById(R.id.tvwUserName);
            this.tvwUserDescription = view.findViewById(R.id.tvwUserDescription);
            this.ivwImage = view.findViewById(R.id.ivwImage);
            this.tvwMessageHeading = view.findViewById(R.id.tvwMessageHeading);
            this.tvwMessageDateTime = view.findViewById(R.id.tvwMessageDateTime);
            this.context = context;

            if(defaultProfileImage == null && context != null) {
                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    defaultProfileImage = context.getDrawable(R.drawable.ic_default_profile_image_24dp);
                else {
                    Resources resources = context.getResources();
                    defaultProfileImage = resources.getDrawable(R.drawable.ic_default_profile_image_24dp);
                }
            }

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo
            menuInfo)
        {
            //.inflate(R.menu.color_menu, menu);
        }
    }

    public static final SimpleDateFormat feedMessageListDateFormat = new SimpleDateFormat("d MMMM yyyy, hh:mma");

    public static final int FMCM_VIEW_PERFORMER_PROFILE = 10000;
    public static final int FMCM_VIEW_FOLLOWEE_PROFILE = 10001;
    public static final int FMCM_VIEW_RECIPE = 10010;
    public static final int FMCM_PIN_RECIPE = 10011;
    public static final int FMCM_COOK_RECIPE_NOW = 10012;
    public static final int FMCM_BROWSE_COLLECTION = 10020;
    public static final int FMCM_BROWSE_TAG = 10021;

    private static String[] userTitles = {
        "cookie monster",
        "captain ice cream",
        "pizza inhaler",
        "old spice",
        "pastafari",
        "salad spinner",
        "star chef",
        "potato peeler"
    };

    private List<FeedMessage> feedMessages;

    public FeedMessageRecyclerViewAdapter(List<FeedMessage> feedMessages) {

        this.feedMessages = feedMessages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.test_feedmessage_list_view_item, parent, false);
        ViewHolder vh = new ViewHolder(v, parent.getContext());
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int index) {

        final FeedMessage feedMessage = feedMessages.get(index);
        final String userProfileImageFileName = feedMessage.getUserImageFileName();
        final String imageFileName = feedMessage.getObject1ImageFileName();

//        Pattern p = Pattern.compile("([A-Z]{1})");
//        Matcher m = p.matcher(feedMessage.getType().toString());
//        final String feedMessageTypeReadable = m.replaceAll(" $1").toLowerCase();

        // set the user name
        viewHolder.tvwUserName.setText(feedMessage.getUserName());

        // set user description, title, rank or location
        int userTitlesIndex = (int)(feedMessage.getUserId() % userTitles.length);
        final String userTitle = userTitles[userTitlesIndex];
        viewHolder.tvwUserDescription.setText(userTitle);

        final EFeedMessageType messageType = feedMessage.getType();

        // set message heading text view
        String message;
        Resources resources = viewHolder.context.getResources();
        if(resources != null) {
            switch(messageType) {
            case followedUser:
                message = resources.getString(R.string.is_now_following_user, feedMessage.getObject1Name());
                break;
            case followedTag:
                message = resources.getString(R.string.is_now_following_tag, feedMessage.getObject1Name());
                break;
            case followedCollection:
                message = resources.getString(R.string.is_now_following_collection, feedMessage.getObject1Name());
                break;
            case createdRecipe:
                message = resources.getString(R.string.created_recipe, feedMessage.getObject1Name());
                break;
            case modifiedRecipe:
                message = resources.getString(R.string.modified_recipe, feedMessage.getObject1Name());
                break;
            case cookedRecipe:
                message = resources.getString(R.string.cooked_recipe, feedMessage.getObject1Name());
                break;
            case createdCollection:
                message = resources.getString(R.string.created_collection, feedMessage.getObject1Name());
                break;
            case addedRecipeToCollection:
                message = resources.getString(R.string.added_recipe_to_collection, feedMessage.getObject1Name());
                break;
            case addedImageToRecipe:
                message = resources.getString(R.string.added_image_to_recipe, feedMessage.getObject1Name());
                break;
            // this should not happen
            default:
                message = feedMessage.getMessage();
                break;
            }
        }
        else message = feedMessage.getMessage();
        viewHolder.tvwMessageHeading.setText(message);

        // set time ago text view
        final String timeAgo = TimeUtils.getTimeAgoString(viewHolder.context, feedMessage.getPerformedDateTime());
        if(timeAgo != null && timeAgo.length() > 0)
            viewHolder.tvwMessageDateTime.setText(timeAgo);
        else
            viewHolder.tvwMessageDateTime.setText(feedMessageListDateFormat.format(feedMessage.getPerformedDateTime()));

        // if there is a valid non-mockup profile image file name provided
        if(userProfileImageFileName != null && userProfileImageFileName.length() > 0) {

            String userProfileImageFileUrl;
            if(!userProfileImageFileName.equalsIgnoreCase("[pravatar]"))
                userProfileImageFileUrl = "https://www.sebastianzander.de/cooka/img/" + userProfileImageFileName;
            else
                userProfileImageFileUrl = "http://i.pravatar.cc/300?u=" + feedMessage.getUserName() + "@user.cooka.app";

            Glide.with(viewHolder.context)
                .asBitmap()
                .apply(new RequestOptions()
                .placeholder(ViewHolder.defaultProfileImage))
                .load(userProfileImageFileUrl)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                        Target<Bitmap> target, boolean isFirstResource)
                    {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap>
                        target, DataSource dataSource, boolean isFirstResource)
                    {
                        viewHolder.ivwUserProfileImage.setImageBitmap(resource);
                        viewHolder.ivwUserProfileImage.setVisibility(View.VISIBLE);
                        return true;
                    }
                })
                .submit();
        }
        // if there is no profile image set to the user use the default profile image
        else if(ViewHolder.defaultProfileImage != null) {
            viewHolder.ivwUserProfileImage.setImageDrawable(ViewHolder.defaultProfileImage);
            viewHolder.ivwUserProfileImage.setVisibility(View.VISIBLE);
        }
        // this should practically not happen
        else {
            // assigns the user a more or less unique color to be used in the user profile view
            viewHolder.ivwUserProfileImage.setImageDrawable(null);
            viewHolder.ivwUserProfileImage.setBackgroundColor(feedMessage.getUserName().hashCode());
            viewHolder.ivwUserProfileImage.setVisibility(View.VISIBLE);
        }

        // set the context dependent image
        if(imageFileName != null && imageFileName.length() > 0) {
            final String imageFileUrl = "https://www.sebastianzander.de/cooka/img/" + imageFileName;
            Glide.with(viewHolder.context)
                .asBitmap()
                .load(imageFileUrl)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                        Target<Bitmap> target, boolean isFirstResource)
                    {
                        viewHolder.ivwImage.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap>
                        target, DataSource dataSource, boolean isFirstResource)
                    {
                        viewHolder.ivwImage.setImageBitmap(resource);
                        viewHolder.ivwImage.setVisibility(View.VISIBLE);
                        return true;
                    }
                })
                .submit();
        }
        // if there is no context dependent image hide the image entirely
        else
        {
            viewHolder.ivwImage.setVisibility(View.GONE);
        }

        // set on click listener
        viewHolder.ivwContextMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = view.getContext();
                //creating a popup menu
                PopupMenu popup = new PopupMenu(context, viewHolder.ivwContextMenuButton,
                    Gravity.TOP | Gravity.RIGHT);
                //inflating menu from xml resource
                popup.inflate(R.menu.feedmessage_context_menu);
                Menu menu = popup.getMenu();

                String viewPerformerProfileText = context.getString(R.string.view_profile_of_user, feedMessage.getUserName());
                menu.add(R.id.fmcm_context_group, FMCM_VIEW_PERFORMER_PROFILE, 0, viewPerformerProfileText)
                    .setIcon(R.drawable.ic_user_24dp);

                // add message type dependent menu items
                switch(messageType) {
                case followedUser:
                    String viewFolloweeProfileText = context.getString(R.string.view_profile_of_user, feedMessage.getObject1Name());
                    menu.add(R.id.fmcm_context_group, FMCM_VIEW_FOLLOWEE_PROFILE, 0, viewFolloweeProfileText)
                        .setIcon(R.drawable.ic_user_24dp);
                    break;
                case followedTag:
                    String browseTagText = context.getString(R.string.browse_tag_tag, feedMessage.getObject1Name());
                    menu.add(R.id.fmcm_context_group, FMCM_BROWSE_TAG, 0, browseTagText);
                    break;
                case followedCollection:
                case createdCollection:
                case addedRecipeToCollection:
                    String browseCollectionText = context.getString(R.string.browse_collection_name,
                        feedMessage.getObject1Name());
                    menu.add(R.id.fmcm_context_group, FMCM_BROWSE_COLLECTION, 0, browseCollectionText);
                    break;
                case createdRecipe:
                case modifiedRecipe:
                case cookedRecipe:
                case addedImageToRecipe:
                    String viewRecipeText = context.getString(R.string.view_recipe);
                    String pinRecipeText = context.getString(R.string.pin_recipe);
                    String cookRecipeNowText = context.getString(R.string.cook_this_recipe_now);
                    menu.add(R.id.fmcm_context_group, FMCM_VIEW_RECIPE, 0, viewRecipeText);
                    menu.add(R.id.fmcm_context_group, FMCM_PIN_RECIPE, 0, pinRecipeText);
                    menu.add(R.id.fmcm_context_group, FMCM_COOK_RECIPE_NOW, 0, cookRecipeNowText);
                    break;
                }

                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()) {
                        case FMCM_VIEW_PERFORMER_PROFILE:
                            Toast.makeText(viewHolder.context, String.format("view profile of %s",
                                feedMessage.getUserName()), Toast.LENGTH_LONG).show();
                            return true;
                        case FMCM_VIEW_FOLLOWEE_PROFILE:
                            Toast.makeText(viewHolder.context, String.format("view profile of %s",
                                feedMessage.getObject1Name()), Toast.LENGTH_LONG).show();
                            return true;
                        case FMCM_VIEW_RECIPE:
                            Toast.makeText(viewHolder.context, String.format("view recipe %s",
                                feedMessage.getObject1Name()), Toast.LENGTH_LONG).show();
                            return true;
                        case FMCM_PIN_RECIPE:
                            Toast.makeText(viewHolder.context, String.format("pin recipe %s",
                                feedMessage.getObject1Name()), Toast.LENGTH_LONG).show();
                            return true;
                        case FMCM_COOK_RECIPE_NOW:
                            Toast.makeText(viewHolder.context, String.format("cook recipe %s now",
                                feedMessage.getObject1Name()), Toast.LENGTH_LONG).show();
                            return true;
                        case FMCM_BROWSE_COLLECTION:
                            Toast.makeText(viewHolder.context, String.format("browse %s",
                                feedMessage.getObject1Name()), Toast.LENGTH_LONG).show();
                            return true;
                        case FMCM_BROWSE_TAG:
                            Toast.makeText(viewHolder.context, String.format("browse tag %s",
                                feedMessage.getObject1Name()), Toast.LENGTH_LONG).show();
                            return true;
                        case R.id.fmcm_report:
                            Toast.makeText(viewHolder.context, "report feed message", Toast.LENGTH_LONG).show();
                            return true;
                        default:
                            return false;
                        }
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return feedMessages.size();
    }
}
