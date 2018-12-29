package app.cooka.cookapp.model;

public class CreateRecipeResult {

    /**
     * <p><b>Possible result codes and messages:</b></p>
     * <p>xxx is a placeholder for the relevant recipe step number for which the error occurred.</p>
     * <ul>
     * <li><b>000000:</b>&nbsp;&nbsp; success: the recipe was created successfully</li>
     * <li><b>101000:</b>&nbsp;&nbsp; the given recipe object is not a valid recipe draft object</li>
     * <li><b>102000:</b>&nbsp;&nbsp; the given recipe object does not have a title set</li>
     * <li><b>103000:</b>&nbsp;&nbsp; the given recipe object does not contain any recipe steps</li>
     * <li><b>110000:</b>&nbsp;&nbsp; call could not be created; please verify that the user id in the login information store is set</li>
     * <li><b>202000:</b>&nbsp;&nbsp; failed to connect to database</li>
     * <li><b>203000:</b>&nbsp;&nbsp; the parameter userId was not specified</li>
     * <li><b>204000:</b>&nbsp;&nbsp; the parameter accessToken was not specified</li>
     * <li><b>205000:</b>&nbsp;&nbsp; the database connection could not be established</li>
     * <li><b>206000:</b>&nbsp;&nbsp; the request did not contain a valid json body</li>
     * <li><b>207000:</b>&nbsp;&nbsp; the request did not contain a valid recipe object</li>
     * <li><b>208000:</b>&nbsp;&nbsp; the key languageId was not specified within the submitted recipe</li>
     * <li><b>209000:</b>&nbsp;&nbsp; the key creatorId was not specified within the submitted recipe</li>
     * <li><b>210000:</b>&nbsp;&nbsp; the key title was not specified within the submitted recipe</li>
     * <li><b>211000:</b>&nbsp;&nbsp; a recipe with the given title by the given creator does already exist</li>
     * <li><b>212000:</b>&nbsp;&nbsp; the number of inserted recipe strings is not equal to the number of provided strings</li>
     * <li><b>213000:</b>&nbsp;&nbsp; could not insert recipe strings into the database</li>
     * <li><b>215000:</b>&nbsp;&nbsp; could not insert recipe into the database</li>
     * <li><b>216000:</b>&nbsp;&nbsp; the number of inserted recipe category relations is not equal to the number of provided recipe category relations</li>
     * <li><b>217000:</b>&nbsp;&nbsp; could not insert tag with the given name into database</li>
     * <li><b>218000:</b>&nbsp;&nbsp; could not select tag with the given name from database</li>
     * <li><b>219000:</b>&nbsp;&nbsp; the tag name key is not present within the submitted recipe but is required when no tagId was specified</li>
     * <li><b>220000:</b>&nbsp;&nbsp; the number of inserted recipe tag relations is not equal to the number of provided recipe tag relations</li>
     * <li><b>223xxx:</b>&nbsp;&nbsp; could not insert recipe step into the database</li>
     * <li><b>224xxx:</b>&nbsp;&nbsp; an ingredient name key in recipe step is not present within the submitted recipe</li>
     * <li><b>225xxx:</b>&nbsp;&nbsp; the number of inserted ingredient strings is not equal to the number of provided strings</li>
     * <li><b>226xxx:</b>&nbsp;&nbsp; could not insert ingredient into the database</li>
     * <li><b>227xxx:</b>&nbsp;&nbsp; could not insert unit type name string into the database</li>
     * <li><b>228xxx:</b>&nbsp;&nbsp; could not insert unit type into the database</li>
     * <li><b>229xxx:</b>&nbsp;&nbsp; could not find unit type with abbreviation in the database</li>
     * <li><b>230xxx:</b>&nbsp;&nbsp; could not insert recipe step ingredient into the database</li>
     * <li><b>300000:</b>&nbsp;&nbsp; database error message followed by database implementation specific error code and message</li>
     * </ul>
     */
    public int resultCode;

    /**
     * See {@linkplain CreateRecipeResult#resultCode} for details regarding possible result codes and their corresponding messages.
     */
    public String resultMessage;

    /**
     * The recipe identifier set when there was no error and {@linkplain CreateRecipeResult#resultCode} is 0
     */
    public long recipeId = 0;
}
