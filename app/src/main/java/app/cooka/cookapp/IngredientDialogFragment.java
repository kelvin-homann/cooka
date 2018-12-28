package app.cooka.cookapp;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import java.util.Locale;

import app.cooka.cookapp.model.RecipeStepIngredient;

public class IngredientDialogFragment extends DialogFragment {

    private boolean edit;
    private RecipeStepIngredient ingredient;
    private OnSubmitListener listener;

    private EditText etIngredientName;
    private EditText etIngredientAmount;
    private EditText etIngredientUnit;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Dialog);
        builder.setTitle(edit ? R.string.edit_ingredient : R.string.add_ingredient).
                setPositiveButton(edit ? R.string.ok : R.string.add, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        layoutToIngredient();
                        listener.onSubmit(ingredient);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        //Set custom dialog layout
        View view = getActivity().getLayoutInflater().inflate(R.layout.ingredient_dialog_fragment, null);
        etIngredientName = view.findViewById(R.id.ingredient_name);
        etIngredientAmount = view.findViewById(R.id.ingredient_amount);
        etIngredientUnit = view.findViewById(R.id.ingredient_unit);
        builder.setView(view);

        //Load current ingredient into layout for editing
        if(edit) ingredientToLayout();

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void ingredientToLayout() {
        if(ingredient == null) return;

        etIngredientName.setText(ingredient.getIngredientName());
        etIngredientAmount.setText(String.format(Locale.getDefault(), "%f", ingredient.getIngredientAmount()));
        etIngredientUnit.setText(ingredient.getUnitTypeAbbreviation());
    }

    private void layoutToIngredient() {
        if(ingredient == null) {
            ingredient = RecipeStepIngredient.Factory.
                    createRecipeStepIngredientDraft(null, null, 0, null, null, null);
        }

        ingredient.setIngredientName(etIngredientName.getText().toString());

        float amount = 0f;
        String a = etIngredientAmount.getText().toString();
        if(a != null && !a.isEmpty()) amount = Float.parseFloat(a);

        ingredient.setIngredientAmount(amount);
        ingredient.setUnitTypeAbbreviation(etIngredientUnit.getText().toString());
    }

    public void show(FragmentManager manager, boolean edit, OnSubmitListener listener) {
        this.edit = edit;
        this.listener = listener;
        show(manager, "IngredientDialog");
    }

    public RecipeStepIngredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(RecipeStepIngredient ingredient) {
        this.ingredient = ingredient;
    }

    public interface OnSubmitListener {
        void onSubmit(RecipeStepIngredient ingredient);
    }
}
