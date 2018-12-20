package app.cooka.cookapp.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.*;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import app.cooka.cookapp.R;

public class LoadingScreenView extends FrameLayout {

    private ProgressBar progressBar;
    private boolean visible;
    private View[] hiddenViews;

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    //Constructors
    public LoadingScreenView(Context context) {
        super(context);
        initializeView(context);
    }

    public LoadingScreenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context);
    }

    public LoadingScreenView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(context);
    }

    public LoadingScreenView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeView(context);
    }

    //Used to initialize the view with its components
    private void initializeView(Context context) {
        ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.loading_screen_view, this);

        //TODO: Add default visibility attribute
        visible = true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        progressBar = findViewById(R.id.progress_bar);
        setVisible(visible);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        this.setVisibility(visible ? VISIBLE : INVISIBLE);
        if(visible) this.bringToFront();

        //Unhide hidden views
        if(!visible && hiddenViews != null) {
            for(int i = 0; i < hiddenViews.length; i++){
                hiddenViews[i].setVisibility(VISIBLE);
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void show() {
        setVisible(true);
    }

    public void show(View... viewsToHide) {
        //Hide extra views
        for (int i = 0; i < viewsToHide.length; i++) {
            viewsToHide[i].setVisibility(INVISIBLE);
        }
        hiddenViews = viewsToHide;
        show();
    }

    public void hide() {
        setVisible(false);
    }

    public void showFor(long milliseconds) {
        show();
        Handler handler = new Handler();
        handler.postDelayed(hideRunnable, milliseconds);
    }

    public void showFor(long milliseconds, View... viewsToHide) {
        show(viewsToHide);
        Handler handler = new Handler();
        handler.postDelayed(hideRunnable, milliseconds);
    }
}
