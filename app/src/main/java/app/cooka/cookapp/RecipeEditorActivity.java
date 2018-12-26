package app.cooka.cookapp;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.rd.PageIndicatorView;
import com.rd.utils.DensityUtils;

public class RecipeEditorActivity extends AppCompatActivity {

    private Toolbar toolbar;

    //View pager and page indicator
    private ViewPager cardViewPager;
    private CookModeCardAdapter cardAdapter;
    private CookModeCardTransformer cardTransformer;

    private PageIndicatorView pageIndicatorView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_editor);

        //Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    }
}
