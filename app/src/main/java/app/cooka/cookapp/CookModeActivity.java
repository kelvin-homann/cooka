package app.cooka.cookapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.rd.PageIndicatorView;
import com.rd.utils.DensityUtils;

public class CookModeActivity extends AppCompatActivity {

    //Tag used for debug logs
    private static final String LOG_TAG = "Cook Mode";

    //Main view group
    private View activityContentView;

    //View pager and page indicator
    private ViewPager cardViewPager;
    private CookModeCardAdapter cardAdapter;
    private CookModeCardTransformer cardTransformer;

    private PageIndicatorView pageIndicatorView;

    //Details Sheet and fab
    private MaterialButton detailsFab;
    private View detailsSheet;
    private BottomSheetBehavior detailsSheetBehavior;
    private View detailsSheetContentContainer;

    private static final int DETAILS_COLLAPSED_MARGIN = DensityUtils.dpToPx(32);
    private static final int DETAILS_EXPANDED_MARGIN = DensityUtils.dpToPx(0);

    private static final int FAB_OVERVIEW_WIDTH = DensityUtils.dpToPx(155);
    private static final int FAB_STEPS_WIDTH = DensityUtils.dpToPx(125);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cook_mode);

        //Main controls
        activityContentView = findViewById(R.id.fullscreen_content);

        //Card View Pager
        cardViewPager = findViewById(R.id.card_view_pager);

        //Adapter
        cardAdapter = new CookModeCardAdapter();
        cardAdapter.addItem("Fisch würzen");
        cardAdapter.addItem("Fisch braten");
        cardAdapter.addItem("Salat vorbereiten");
        cardAdapter.addItem("Gericht anrichten");
        cardAdapter.addItem("Fertig");

        //Setup view pager margin/padding

        //TODO: Remove hardcoded values
        int topPadding = DensityUtils.dpToPx(18);
        int bottomPadding = DensityUtils.dpToPx(26);
        int horizontalPadding = DensityUtils.dpToPx(14);
        int pageMargin = DensityUtils.dpToPx(10);

        cardViewPager.setAdapter(cardAdapter);
        cardViewPager.setOffscreenPageLimit(3);
        cardViewPager.setPageMargin(pageMargin);
        cardViewPager.setPadding(horizontalPadding, topPadding, horizontalPadding, bottomPadding);

        //Setup card transformer
        cardTransformer = new CookModeCardTransformer(cardViewPager, cardAdapter);
        cardTransformer.enableScaling(true);
        cardViewPager.setPageTransformer(false, cardTransformer);

        //Page Indicator
        pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setViewPager(cardViewPager);

        //Details Sheet
        detailsFab = findViewById(R.id.fab);
        detailsFab.setText(R.string.overview_fab_text);

        detailsSheet = findViewById(R.id.details_sheet);
        detailsSheetBehavior = BottomSheetBehavior.from(detailsSheet);
        detailsSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        detailsSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int state) {
                detailsSheet_onStateChanged(view, state);
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                detailsSheet_onSlide(view, v);
            }
        });

        detailsSheetContentContainer = findViewById(R.id.details_sheet_content);

        //Hide the default system ui for this activity
        hideSystemUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Hide the system ui again after the activity is resumed
        hideSystemUI();
    }

    private void hideSystemUI() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //activityControlsView.setVisibility(View.GONE);

        activityContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.KEEP_SCREEN_ON
                | View.SCREEN_STATE_ON);
    }

    private void showSystemUI() {
        // Show the system bar
        activityContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.KEEP_SCREEN_ON);
        //activityControlsView.setVisibility(View.VISIBLE);
    }

    public void fab_onClick(View view) {
        //Toggle the details sheet state
        int newState = (detailsSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ?
                BottomSheetBehavior.STATE_COLLAPSED :
                BottomSheetBehavior.STATE_EXPANDED);
        detailsSheetBehavior.setState(newState);
    }

    //TODO: Move method to utility class
    //NOTE: Not needed because a utility method is already included in the PageIndicatorView lib
    public static int convertDip2Pixels(Context context, int dip) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    //Set the details sheet background margin based on a value between 0 and 1
    //(used to sync the background margin with the details sheet state)
    private void interpolateDetailsSheetMargin(float value) {
        //Calculate new margin
        int newMargin = (int)((DETAILS_EXPANDED_MARGIN * value) + (DETAILS_COLLAPSED_MARGIN * (1-value)));

        //Set margin
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams)detailsSheetContentContainer.getLayoutParams();

        params.topMargin = newMargin;
        detailsSheetContentContainer.setLayoutParams(params);
    }

    //Set the details fab width/text based on a value between 0 and 1
    //(used to sync the fab with the details sheet state)
    private void interpolateDetailsFab(float value) {
        //Set text
        detailsFab.setText(value < 0.2f ? R.string.overview_fab_text : R.string.steps_fab_text);

        //Set new width
        int width = (int)(FAB_STEPS_WIDTH * value + FAB_OVERVIEW_WIDTH * (1 - value));
        detailsFab.setWidth(width);
    }

    //Called when the state of the details sheet is changed
    private void detailsSheet_onStateChanged(View view, int state) {

    }

    //Called when the details sheet slides (i.e. is moved in/out of view)
    private void detailsSheet_onSlide(View view, float v) {
        interpolateDetailsSheetMargin(v);
        interpolateDetailsFab(v);

        //if the user starts to collapse the sheet...
        if(v < 0.5f) {
            //Make the content of the details sheet scroll back to the beginning
            ((NestedScrollView)detailsSheetContentContainer).smoothScrollTo(0, 0);
        }
    }
}
