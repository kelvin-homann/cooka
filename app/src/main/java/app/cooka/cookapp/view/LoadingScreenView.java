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

    public static final int MIN_LOADING_TIME = 1000;

    private ProgressBar progressBar;
    private boolean visible;
    private View[] hiddenViews;
    private boolean hidable = true;
    private boolean wasHidden = false;
    private OnHideListener onHideListener;

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private Runnable makeHidableRunnable = new Runnable() {
        @Override
        public void run() {
            hidable = true;
            if(wasHidden) hide();
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
        if(!hidable && !visible) {
            wasHidden = true;
            return;
        }

        this.visible = visible;
        this.setVisibility(visible ? VISIBLE : INVISIBLE);

        if(visible) {
            this.bringToFront();
            wasHidden = false;
        }

        //Unhide hidden views
        if(!visible && hiddenViews != null) {
            for(int i = 0; i < hiddenViews.length; i++){
                hiddenViews[i].setVisibility(VISIBLE);
            }
        }

        if(!visible && onHideListener != null) onHideListener.onHide();
    }

    public boolean isVisible() {
        return visible;
    }

    public void hide() {
        setVisible(false);
    }

    //Different variations of show method
    public void show() {
        show((OnHideListener) null);
    }

    public void show(OnHideListener listener) {
        setOnHideListener(listener);
        setVisible(true);
    }

    public void show(View... viewsToHide) {
        show(null, viewsToHide);
    }

    public void show(OnHideListener listener, View... viewsToHide) {
        //Hide extra views
        for (int i = 0; i < viewsToHide.length; i++) {
            viewsToHide[i].setVisibility(INVISIBLE);
        }
        hiddenViews = viewsToHide;
        show(listener);
    }

    public void showFor(long milliseconds, View... viewsToHide) {
        showFor(milliseconds, null, viewsToHide);
    }

    public void showFor(long milliseconds, OnHideListener listener, View... viewsToHide) {
        show(listener, viewsToHide);
        Handler handler = new Handler();
        handler.postDelayed(hideRunnable, milliseconds);
    }

    public void showForAtLeast(long milliseconds, View... viewsToHide) {
        showForAtLeast(milliseconds, null, viewsToHide);
    }

    public void showForAtLeast(long milliseconds, OnHideListener listener, View... viewsToHide) {
        hidable = false;
        show(listener, viewsToHide);
        Handler handler = new Handler();
        handler.postDelayed(makeHidableRunnable, milliseconds);
    }

    //Hide listener
    public void setOnHideListener(OnHideListener onHideListener) {
        this.onHideListener = onHideListener;
    }

    public interface OnHideListener {
        void onHide();
    }

}
