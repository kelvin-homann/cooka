package app.cooka.cookapp;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Used Guide: https://droidmentor.com/create-onboard-screens-using-viewpager/
 */

public class TutorialActivity extends AppCompatActivity {
    private LinearLayout pagerIndicator;
    private int dotsCount;
    private ImageView[] dots;
    private ViewPager onboardPager;
    private TutorialAdapter mAdapter;
    private Button btnGetStarted;
    int previous_pos = 0;

    ArrayList<TutorialItem> tutorialItems = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        btnGetStarted = findViewById(R.id.btnTutorialGetStarted);
        onboardPager = findViewById(R.id.vpIntroduction);
        pagerIndicator = findViewById(R.id.vpTutorialDots);

        loadData();

        mAdapter = new TutorialAdapter(this, tutorialItems);
        onboardPager.setAdapter(mAdapter);
        onboardPager.setCurrentItem(0);
        onboardPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                // Change the current position intimation

                for (int i = 0; i < dotsCount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(TutorialActivity.this, R.drawable.non_selected_item_dot));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(TutorialActivity.this, R.drawable.selected_item_dot));


                int pos=position+1;

                if(pos==dotsCount&&previous_pos==(dotsCount-1))
                    show_animation();
                else if(pos==(dotsCount-1)&&previous_pos==dotsCount)
                    hide_animation();

                previous_pos=pos;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TutorialActivity.this,"Start MainActivity",Toast.LENGTH_LONG).show();
            }
        });

        setUiPageViewController();

    }

    // Load data into the viewpager

    public void loadData()
    {
        int[] header = {R.string.tut_header1, R.string.tut_header2, R.string.tut_header3};
        int[] desc = {R.string.tut_desc1, R.string.tut_desc2, R.string.tut_desc3};
        int[] imageId = {R.mipmap.tutorial_background_feed, R.mipmap.tutorial_background_profile, R.mipmap.tutorial_background_cookmode};

        for(int i = 0;i < imageId.length; i++)
        {
            TutorialItem item = new TutorialItem();
            item.setImage(imageId[i]);
            item.setTitle(getResources().getString(header[i]));
            item.setDescription(getResources().getString(desc[i]));

            tutorialItems.add(item);
        }
    }

    // Button bottomUp animation

    public void show_animation()
    {
        Animation show = AnimationUtils.loadAnimation(this, R.anim.slide_up_anim);

        btnGetStarted.startAnimation(show);

        show.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                btnGetStarted.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                btnGetStarted.clearAnimation();

            }

        });


    }

    // Button Topdown animation
    public void hide_animation()
    {
        Animation hide = AnimationUtils.loadAnimation(this, R.anim.slide_down_anim);

        btnGetStarted.startAnimation(hide);

        hide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                btnGetStarted.clearAnimation();
                btnGetStarted.setVisibility(View.GONE);

            }

        });


    }

    private void setUiPageViewController() {
        dotsCount = mAdapter.getCount();
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(TutorialActivity.this, R.drawable.non_selected_item_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(6, 0, 6, 0);

            pagerIndicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(TutorialActivity.this, R.drawable.selected_item_dot));
    }


}