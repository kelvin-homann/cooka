package app.cooka.cookapp;

import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.rd.PageIndicatorView;
import com.rd.utils.DensityUtils;

import app.cooka.cookapp.model.Recipe;
import app.cooka.cookapp.model.RecipeStep;

public class RecipeEditorActivity extends AppCompatActivity {

    private Toolbar toolbar;

    //View pager and page indicator
    private ViewPager cardViewPager;
    private RecipeEditorCardAdapter cardAdapter;
    private CookModeCardTransformer cardTransformer;

    private PageIndicatorView pageIndicatorView;

    private DialogInterface.OnClickListener deleteStepDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    deleteCurrentStep();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

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
        cardAdapter = new RecipeEditorCardAdapter();
        cardAdapter.addItem("Fisch würzen");
        cardAdapter.addItem("Fisch würzen");

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

    public void fab_onClick(View view) {
        cardAdapter.addItem("");
        cardAdapter.notifyDataSetChanged();
        cardViewPager.setCurrentItem(cardAdapter.getCount()-1, true);
    }

    public void currentCardDelete_onClick(View view) {
        showDeleteStepDialog();
    }

    private void showDeleteStepDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Dialog);
        builder.setMessage(R.string.delete_step_warning_message).
                setPositiveButton(R.string.yes, deleteStepDialogListener).
                setNegativeButton(R.string.no, deleteStepDialogListener).
                show();
    }

    private void deleteCurrentStep() {
        int itemToRemove = cardViewPager.getCurrentItem();

        cardViewPager.setAdapter(null);
        cardAdapter.removeItem(itemToRemove);

        //Adapter needs to be reinitialised with new list of views
        cardViewPager.setAdapter(cardAdapter);

        cardAdapter.notifyDataSetChanged();
        cardViewPager.setCurrentItem(itemToRemove >= cardAdapter.getCount() ? cardAdapter.getCount()-1 : itemToRemove);
    }
}
