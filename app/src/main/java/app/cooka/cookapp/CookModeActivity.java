package app.cooka.cookapp;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.rd.PageIndicatorView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CookModeActivity extends AppCompatActivity {

    private View activityContentView;
    private View activityControlsView;
    private PageIndicatorView pageIndicatorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cook_mode);

        activityControlsView = findViewById(R.id.fullscreen_content_controls);
        activityContentView = findViewById(R.id.fullscreen_content);

        pageIndicatorView = findViewById(R.id.pageIndicatorView);
        pageIndicatorView.setCount(5);
        pageIndicatorView.setSelection(0);

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
}
