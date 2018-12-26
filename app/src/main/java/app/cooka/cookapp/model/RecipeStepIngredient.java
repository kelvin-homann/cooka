package app.cooka.cookapp.model;

public class RecipeStepIngredient {

    public static final String LOGTAG = "COOKALOG";

    private long ingredientId;
    private String ingredientName;
    private String ingredientDescription;
    private float ingredientAmount;
    private long unitTypeId;
    private String unitTypeName;
    private String unitTypeAbbreviation;
    private String customUnit;

    private RecipeStepIngredient(long ingredientId, float ingredientAmount, long unitTypeId) {

        this.ingredientId = ingredientId;
        this.ingredientAmount = ingredientAmount;
        this.unitTypeId = unitTypeId;

        /* IMPLEMENTATION NOTICE */
        // we are not going to create and reference Ingredient and UnitType instances for now
    }

    private RecipeStepIngredient(long ingredientId, String ingredientName, String
        ingredientDescription, float ingredientAmount, long unitTypeId, String unitTypeName,
        String unitTypeAbbreviation, String customUnit)
    {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.ingredientDescription = ingredientDescription;
        this.ingredientAmount = ingredientAmount;
        this.unitTypeId = unitTypeId;
        this.unitTypeName = unitTypeName;
        this.unitTypeAbbreviation = unitTypeAbbreviation;
        this.customUnit = customUnit;

        /* IMPLEMENTATION NOTICE */
        // we are not going to create and reference Ingredient and UnitType instances for now
    }

    public static class Factory {

        /**
         * Creates a new recipe step ingredient draft for later insertion into the database.
         * @param ingredientId the identifier of the ingredient to be used.
         * @param ingredientAmount the amount of the ingredient to be used.
         * @param unitTypeId the type of unit the amount is specified in.
         * @param customUnit a custom unit string if the unit is not a standard unit stored in the
         *      database.
         * @return a {@linkplain RecipeStepIngredient} object; null if an error occurred.
         */
        public static RecipeStepIngredient createRecipeStepIngredientDraft(final long ingredientId,
            final float ingredientAmount, final long unitTypeId, final String customUnit)
        {
            return new RecipeStepIngredient(ingredientId, null, null, ingredientAmount, unitTypeId,
                null, null, customUnit);
        }

        /**
         * Creates a new recipe step ingredient draft for later insertion into the database.
         * @param ingredientName the name of the ingredient to be used.
         * @param ingredientDescription a description of the ingredient (rarely used).
         * @param ingredientAmount the amount of the ingredient to be used.
         * @param unitTypeName the name of the unit the amount is specified in.
         * @param unitTypeAbbreviation the abbreviation of the unit the amount is specified in.
         * @param customUnit a custom unit string if the unit is not a standard unit stored in the
         *      database.
         * @return a {@linkplain RecipeStepIngredient} object; null if an error occurred.
         */
        public static RecipeStepIngredient createRecipeStepIngredientDraft(final String
            ingredientName, final String ingredientDescription, final float ingredientAmount,
            final String unitTypeName, final String unitTypeAbbreviation, final String customUnit)
        {
            return new RecipeStepIngredient(0, ingredientName, ingredientDescription,
                ingredientAmount, 0, unitTypeName, unitTypeAbbreviation, customUnit);
        }

        /**
         * Creates a new recipe step ingredient object that is <b>not</b> linked to the database.
         * @param ingredientId the identifier of the ingredient to be used.
         * @param ingredientName the name of the ingredient to be used.
         * @param ingredientDescription a description of the ingredient (rarely used).
         * @param ingredientAmount the amount of the ingredient to be used.
         * @param unitTypeId the type of unit the amount is specified in.
         * @param unitTypeName the name of the unit the amount is specified in.
         * @param unitTypeAbbreviation the abbreviation of the unit the amount is specified in.
         * @param customUnit a custom unit string if the unit is not a standard unit stored in the
         *      database.
         * @return a {@linkplain RecipeStepIngredient} object; null if an error occurred.
         */
        public static RecipeStepIngredient createRecipeStepIngredient(final long ingredientId,
            final String ingredientName, final String ingredientDescription, final float
            ingredientAmount, final long unitTypeId, final String unitTypeName, final String
            unitTypeAbbreviation, final String customUnit)
        {
            return new RecipeStepIngredient(ingredientId, ingredientName, ingredientDescription,
                ingredientAmount, unitTypeId, unitTypeName, unitTypeAbbreviation, customUnit);
        }
    }

    public long getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(long ingredientId) {
        // re-reference the ingredient instance
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getIngredientDescription() {
        return ingredientDescription;
    }

    public void setIngredientDescription(String ingredientDescription) {
        this.ingredientDescription = ingredientDescription;
    }

    public float getIngredientAmount() {
        return ingredientAmount;
    }

    public void setIngredientAmount(float ingredientAmount) {
        this.ingredientAmount = ingredientAmount;
    }

    public long getUnitTypeId() {
        return unitTypeId;
    }

    public void setUnitTypeId(long unitTypeId) {
        // re-reference the unit type instance
        this.unitTypeId = unitTypeId;
    }

    public String getUnitTypeName() {
        return unitTypeName;
    }

    public void setUnitTypeName(String unitTypeName) {
        this.unitTypeName = unitTypeName;
    }

    public String getUnitTypeAbbreviation() {
        return unitTypeAbbreviation;
    }

    public void setUnitTypeAbbreviation(String unitTypeAbbreviation) {
        this.unitTypeAbbreviation = unitTypeAbbreviation;
    }

    public String getCustomUnit() {
        return customUnit;
    }

    public void setCustomUnit(String customUnit) {
        this.customUnit = customUnit;
    }
}
