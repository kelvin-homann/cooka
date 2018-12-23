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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import app.cooka.cookapp.R;

public class IngredientsView extends FrameLayout {

    private TableLayout table;
    private boolean amountIsBold = false;

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

            if(!showDivider) table.setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        }
    }

    public void setIngredients(String[] amounts, String[] names) {
        if(amounts.length != names.length) {
            Log.e("IngredientsView", "Amounts and Names must be of the same length.");
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < amounts.length; i++) {
            View row = inflater.inflate(R.layout.ingredients_row, table, false);

            TextView amount = (TextView)row.findViewById(R.id.ingredient_amount);
            amount.setText(amounts[i]);
            amount.setTypeface(amount.getTypeface(), amountIsBold ? Typeface.BOLD : Typeface.NORMAL);
            Log.d("IngredientsView", "Amount is bold: " + amountIsBold);

            ((TextView)row.findViewById(R.id.ingredient_name)).setText(names[i]);
            table.addView(row);
        }
    }
}
