package app.cooka.cookapp.model;

public class RecipeStepIngredient {

    public static final String LOGTAG = "COOKALOG";

    // change states
    public static final int CHST_INGREDIENTNAME = 0x00000001;
    public static final int CHST_INGREDIENTDESCRIPTION = 0x00000002;
    public static final int CHST_INGREDIENTAMOUNT = 0x00000004;
    public static final int CHST_UNITTYPE = 0x00000008;
    public static final int CHST_CUSTOMUNIT = 0x00000010;
    public static final int CHST_FLAGS = 0x00000020;
    public static final int CHST_FORCE_UPDATE = 0xffffffff;

    // flags
    public static final int FLAG_HIDDEN = 0x00000001;
    public static final int FLAG_DELETED = 0x00000002;

    private int changeState = 0;
    private boolean committed = false;
    private int flags = 0;

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
        this.changeState = 0;

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
        this.changeState = 0;

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

    /**
     * Gets the change state of this recipe step instance that reflects what fields have changed
     * since the last synchronization. Basically an extended dirty flag.
     * @return the change state bit field
     */
    public int getChangeState() {
        return changeState;
    }

    public void resetChangeState() {
        changeState = 0;
    }

    public boolean getCommited() {
        return committed;
    }

    public void resetCommitted() {
        this.committed = false;
    }

    public void commit() {
        this.committed = true;
    }

    @SuppressWarnings("unused")
    public int getFlags() {
        return flags;
    }

    @SuppressWarnings("unused")
    public void setFlags(int flags) {
        this.flags = flags;
        changeState |= CHST_FLAGS;
    }

    @SuppressWarnings("unused")
    public void addFlags(int flags) {
        this.flags |= flags;
        changeState |= CHST_FLAGS;
    }

    @SuppressWarnings("unused")
    public void removeFlags(int flags) {
        this.flags ^= flags;
        changeState |= CHST_FLAGS;
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
        changeState |= CHST_INGREDIENTNAME;
    }

    public String getIngredientDescription() {
        return ingredientDescription;
    }

    public void setIngredientDescription(String ingredientDescription) {
        this.ingredientDescription = ingredientDescription;
        changeState |= CHST_INGREDIENTDESCRIPTION;
    }

    public float getIngredientAmount() {
        return ingredientAmount;
    }

    public void setIngredientAmount(float ingredientAmount) {
        this.ingredientAmount = ingredientAmount;
        changeState |= CHST_INGREDIENTAMOUNT;
    }

    public long getUnitTypeId() {
        return unitTypeId;
    }

    public void setUnitTypeId(long unitTypeId) {
        // re-reference the unit type instance
        this.unitTypeId = unitTypeId;
        changeState |= CHST_UNITTYPE;
    }

    public String getUnitTypeName() {
        return unitTypeName;
    }

    public void setUnitTypeName(String unitTypeName) {
        this.unitTypeName = unitTypeName;
        changeState |= CHST_UNITTYPE;
    }

    public String getUnitTypeAbbreviation() {
        return unitTypeAbbreviation;
    }

    public void setUnitTypeAbbreviation(String unitTypeAbbreviation) {
        this.unitTypeAbbreviation = unitTypeAbbreviation;
        changeState |= CHST_UNITTYPE;
    }

    public String getCustomUnit() {
        return customUnit;
    }

    public void setCustomUnit(String customUnit) {
        this.customUnit = customUnit;
        changeState |= CHST_CUSTOMUNIT;
    }
}
