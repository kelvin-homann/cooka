package app.cooka.cookapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.dynamic.OnDelegateCreatedListener;

import java.util.Locale;

import app.cooka.cookapp.R;
import app.cooka.cookapp.model.RecipeStepIngredient;

public class IngredientsView extends FrameLayout {

    private TableLayout table;
    private boolean amountIsBold = false;
    private int minimumItemCount = 0;
    private boolean editorMode = false;

    //Constructors
    public IngredientsView(@NonNull Context context) {
        super(context);
        initializeView(context, null);
    }

    public IngredientsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeView(context, attrs);
    }

    public IngredientsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context, attrs);
    }

    public IngredientsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView(context, attrs);
    }

    //Setup the view
    private void initializeView(Context context, @Nullable AttributeSet attrs) {
        ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
                inflate(R.layout.ingredients_view, this);

        table = findViewById(R.id.table_layout);

        //Process attributes
        if(attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.IngredientsView);
            boolean showDivider = array.getBoolean(R.styleable.IngredientsView_showDivider, true);
            amountIsBold = array.getBoolean(R.styleable.IngredientsView_amountIsBold, true);
            minimumItemCount = array.getInt(R.styleable.IngredientsView_minimumItemCount, 0);
            editorMode = array.getBoolean(R.styleable.IngredientsView_editorMode, false);

            if(!showDivider) table.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        }
    }

    public void setIngredients(String[] amounts, String[] names) {
        if(amounts.length != names.length) {
            Log.e("IngredientsView", "Amounts and Names must be of the same length.");
            return;
        }

        //LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < Math.max(amounts.length, minimumItemCount); i++) {
            if(i >= amounts.length) {
                addIngredient("", "");
                continue;
            }
            addIngredient(amounts[i], names[i]);
        }
    }

    //Add ingredient by passing amount and name as string
    public void addIngredient(String amount, String name) {
        View row = View.inflate(getContext(), getRowLayout(), null);

        TextView amountView = (TextView)row.findViewById(R.id.ingredient_amount);
        amountView.setText(amount);
        amountView.setTypeface(amountView.getTypeface(), amountIsBold ? Typeface.BOLD : Typeface.NORMAL);

        ((TextView)row.findViewById(R.id.ingredient_name)).setText(name);
        table.addView(row);
    }

    //Add ingredient by passing amount and name as string as well as adding an OnDelete listener
    public void addIngredient(String amount, String name, final OnDeleteIngredientListener listener) {
        addIngredient(amount, name);
        if(!editorMode) return;
        final int index = table.getChildCount()-1;

        ImageView button = table.getChildAt(index).findViewById(R.id.delete_button);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(index);
            }
        });
    }

    //Add ingredient by passing RecipeStepIngredient object
    public void addIngredient(RecipeStepIngredient ingredient) {
        String amount = String.format(Locale.getDefault(), "%f %s", ingredient.getIngredientAmount(),
                ingredient.getUnitTypeAbbreviation());
        String name = ingredient.getIngredientName();

        addIngredient(amount, name);
    }

    //Add ingredient by passing RecipeStepIngredient object as well as adding an OnDelete listener
    public void addIngredient(RecipeStepIngredient ingredient, final OnDeleteIngredientListener listener) {
        String amount = String.format(Locale.getDefault(), "%f %s", ingredient.getIngredientAmount(),
                ingredient.getUnitTypeAbbreviation());
        String name = ingredient.getIngredientName();

        addIngredient(amount, name, listener);
    }

    private int getRowLayout() {
        return editorMode ? R.layout.ingredients_row_editor : R.layout.ingredients_row;
    }

    public interface OnDeleteIngredientListener {
        void onDelete(int index);
    }
}
