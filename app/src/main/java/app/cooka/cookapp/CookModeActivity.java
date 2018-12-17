package app.cooka.cookapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.rd.PageIndicatorView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CookModeActivity extends AppCompatActivity {

    private View activityContentView;
    private View activityControlsView;

    private ViewPager cardViewPager;
    private CookModeCardAdapter cardAdapter;
    private PageIndicatorView pageIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cook_mode);

        //Main controls
        activityControlsView = findViewById(R.id.fullscreen_content_controls);
        activityContentView = findViewById(R.id.fullscreen_content);

        //Card View Pager
        cardViewPager = findViewById(R.id.card_view_pager);

        //Adapter
        cardAdapter = new CookModeCardAdapter();
        cardAdapter.addItem("SCHRITT 1");
        cardAdapter.addItem("SCHRITT 2");
        cardAdapter.addItem("SCHRITT 3");
        cardAdapter.addItem("SCHRITT 4");
        cardAdapter.addItem("SCHRITT 5");

        int margin = convertDip2Pixels(this, 8);
        cardViewPager.setAdapter(cardAdapter);
        cardViewPager.setOffscreenPageLimit(3);
        cardViewPager.setPageMargin(margin);
        cardViewPager.setPadding(margin, margin * 2, margin, margin * 2);

        //Page Indicator
        pageIndicatorView = findViewById(R.id.pageIndicatorView);
//        pageIndicatorView.setCount(cardAdapter.getCount());
        pageIndicatorView.setViewPager(cardViewPager);

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
                | View.KEEP_SCREEN_ON);
    }

    private void showSystemUI() {
        // Show the system bar
        activityContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.KEEP_SCREEN_ON);
        //activityControlsView.setVisibility(View.VISIBLE);
    }

    //TODO: Move method to utillity class
    public static int convertDip2Pixels(Context context, int dip) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }
}
