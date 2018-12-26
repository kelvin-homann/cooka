package app.cooka.cookapp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a cooking step from a recipe and that is an app model representation of
 * the corresponding database entity called RecipeStep. Used to serialize between app and database
 * and to cache its state without needing to repeatedly query the same unchanged object from the
 * database.
 */
public class RecipeStep {

    public static final String LOGTAG = "COOKALOG";

    private final long recipeStepId;
    private int stepNumber;
    private String stepTitle;
    private String stepDescription;

    private List<RecipeStepIngredient> recipeStepIngredients;

    private RecipeStep(final long recipeStepId, int stepNumber, String stepTitle, String
        stepDescription, List<RecipeStepIngredient> recipeStepIngredients)
    {
        this.recipeStepId = recipeStepId;
        this.stepNumber = stepNumber;
        this.stepTitle = stepTitle;
        this.stepDescription = stepDescription;
        this.recipeStepIngredients = recipeStepIngredients;
    }

    public static class Factory {

        /**
         * Creates a recipe step draft object for later insertion into the database.
         * @param stepNumber the step number within the order of recipe steps.
         * @param stepTitle a short title of the recipe step.
         * @param stepDescription the detailed description of the step or steps to take.
         * @param recipeStepIngredients a list of ingredients that are used in this recipe step.
         * @return a new {@linkplain RecipeStep} object; null if an error occurred.
         */
        public static RecipeStep createRecipeStepDraft(final int stepNumber, final String
            stepTitle, final String stepDescription, final List<RecipeStepIngredient>
            recipeStepIngredients)
        {
            return new RecipeStep(-1, stepNumber, stepTitle, stepDescription,
                recipeStepIngredients);
        }

        /**
         * Creates a recipe step object that is <b>not</b> linked to the database.
         * @param recipeStepId the identifier of the step.
         * @param stepNumber the step number within the order of recipe steps.
         * @param stepTitle a short title of the recipe step.
         * @param stepDescription the detailed description of the step or steps to take.
         * @param recipeStepIngredients a list of ingredients that are used in this recipe step.
         * @return a new {@linkplain RecipeStep} object; null if an error occurred.
         */
        public static RecipeStep createRecipeStep(final long recipeStepId, final int
            stepNumber, final String stepTitle, final String stepDescription,
            final List<RecipeStepIngredient> recipeStepIngredients)
        {
            return new RecipeStep(recipeStepId, stepNumber, stepTitle, stepDescription,
                recipeStepIngredients);
        }
    }

    public long getRecipeStepId() {
        return recipeStepId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        this.stepTitle = stepTitle;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }

    public List<RecipeStepIngredient> getRecipeStepIngredients() {
        return recipeStepIngredients;
    }

    public void setRecipeStepIngredients(List<RecipeStepIngredient> recipeStepIngredients) {
        this.recipeStepIngredients = recipeStepIngredients;
    }

    public void addRecipeStepIngredient(RecipeStepIngredient recipeStepIngredient) {
        if(recipeStepIngredient != null)
            recipeStepIngredients.add(recipeStepIngredient);
    }
}
