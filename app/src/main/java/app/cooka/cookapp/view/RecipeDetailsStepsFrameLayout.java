package app.cooka.cookapp.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import app.cooka.cookapp.R;
import app.cooka.cookapp.model.RecipeStep;
import app.cooka.cookapp.model.RecipeStepIngredient;
import app.cooka.cookapp.utils.RecipeUtils;

public class RecipeDetailsStepsFrameLayout extends FrameLayout {

    private TableLayout frameLayout;

    public RecipeDetailsStepsFrameLayout(Context context) {

        super(context);
        initializeView(context, null);
    }

    public RecipeDetailsStepsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs);
    }

    public RecipeDetailsStepsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context, attrs);
    }

    public RecipeDetailsStepsFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(context, attrs);
    }

    private void initializeView(Context context, @Nullable AttributeSet attrs) {

        ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.recipe_details_steps_framelayout, this);
        frameLayout = findViewById(R.id.recipe_details_steps_framelayout);
    }

    public void setSteps(List<RecipeStep> steps) {

        frameLayout.removeAllViews();
        for(RecipeStep step : steps)
            addStep(step);
    }

    public void clearSteps() {

        frameLayout.removeAllViews();
    }

    public void addStep(RecipeStep step) {

        View item = View.inflate(getContext(), R.layout.recipe_details_steps_item, null);

        TextView tvwStepNumber = item.findViewById(R.id.tvwStepNumber);
        TextView tvwStepDescription = item.findViewById(R.id.tvwStepDescription);

        tvwStepNumber.setText(getContext().getString(R.string.recipe_details_step_n,
            step.getStepNumber()));
        tvwStepDescription.setText(step.getStepDescription());

        frameLayout.addView(item);
    }
}
