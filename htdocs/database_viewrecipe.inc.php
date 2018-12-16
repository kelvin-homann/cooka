<?php
    if(!isset($_getpost['recipeId'])) {
        returnError(3, "the parameter recipeId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['userId'])) {
        returnError(4, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['accessToken'])) {
        returnError(5, "the parameter accessToken was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $usedStringIds = array();

    $recipeId = $_getpost['recipeId'];
    $userId = $_getpost['userId'];
    $accessToken = $_getpost['accessToken'];
    $includeCategories = false;
    $includeTags = false;
    $includeSteps = false;
    $includeRatings = false;
    $recipe = null;
    $sqlqueries = array();

    if(isset($_getpost['includeCategories']) && $_getpost['includeCategories'] == 'true')
        $includeCategories = true;
    if(isset($_getpost['includeTags']) && $_getpost['includeTags'] == 'true')
        $includeTags = true;
    if(isset($_getpost['includeSteps']) && $_getpost['includeSteps'] == 'true')
        $includeSteps = true;
    if(isset($_getpost['includeRatings']) && $_getpost['includeRatings'] == 'true')
        $includeRatings = true;
    
    try {
        // select the actual recipe
        $selectRecipeSql = "select recipe.recipeId, titleString.originalValue as recipeTitle, descriptionString.originalValue as recipeDescription, " .
            "titleString.originalLanguageId, originalLanguageNameString.originalValue as originalLanguageName, " .
            "recipe.originalRecipeId, originalRecipeTitleString.originalValue as originalRecipeTitle, usr.userId as creatorId, " .
            "usr.userName as creatorName, recipe.mainImageId, mainImage.imageFileName as mainImageFileName, recipe.mainCategoryId, " .
            "categoryNameString.originalValue as mainCategoryName, recipe.publicationType, recipe.difficultyType, recipe.preparationTime, " .
            "recipe.viewedCount, recipe.cookedCount, recipe.pinnedCount, recipe.modifiedCount, recipe.variedCount, recipe.sharedCount, " .
            "recipe.createdDateTime, recipe.lastModifiedDateTime, recipe.lastCookedDateTime, recipe.rating " .
            "from Recipes recipe " .
            "left join Recipes originalRecipe on recipe.originalRecipeId = originalRecipe.recipeId " .
            "left join Users usr on recipe.creatorId = usr.userId " .
            "left join Categories category on recipe.mainCategoryId = category.categoryId " .
            "left join Strings titleString on recipe.titleStringId = titleString.stringId " .
            "left join Strings descriptionString on recipe.descriptionStringId = descriptionString.stringId " .
            "left join Strings originalRecipeTitleString on originalRecipe.titleStringId = originalRecipeTitleString.stringId " .
            "left join Strings categoryNameString on category.nameStringId = categoryNameString.stringId " .
            "left join Languages originalLanguage on originalLanguage.languageId = titleString.originalLanguageId " .
            "left join Strings originalLanguageNameString on originalLanguage.nameStringId = originalLanguageNameString.stringId " .
            "left join Images mainImage on mainImage.imageId = recipe.mainImageId " .
            "left join Logins login on login.userId = usr.userId " .
            "where recipe.recipeId = ? and (recipe.publicationType = 'public' or recipe.publicationType = 'unlisted' or " .
            "(recipe.publicationType = 'private' and login.accessToken = ?))";
        
        if($debug == true)
            $sqlqueries['selectRecipeSql'] = $selectRecipeSql;

        $selectRecipeStmt = $database->prepare($selectRecipeSql);
        $selectRecipeStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
        $selectRecipeStmt->bindValue(2, $accessToken, PDO::PARAM_STR);
        $selectRecipeStmt->execute();
        $recipeRows = $selectRecipeStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($recipeRows as $recipeRow) {
            $recipe = array(
                'recipeId' => isset($recipeRow['recipeId']) ? $recipeRow['recipeId'] : 0, 
                'recipeTitle' => isset($recipeRow['recipeTitle']) ? $recipeRow['recipeTitle'] : "", 
                'recipeDescription' => isset($recipeRow['recipeDescription']) ? $recipeRow['recipeDescription'] : "", 
                'originalLanguageId' => isset($recipeRow['originalLanguageId']) ? $recipeRow['originalLanguageId'] : 0, 
                'originalLanguageName' => isset($recipeRow['originalLanguageName']) ? $recipeRow['originalLanguageName'] : "", 
                'originalRecipeId' => isset($recipeRow['originalRecipeId']) ? $recipeRow['originalRecipeId'] : 0, 
                'originalRecipeTitle' => isset($recipeRow['originalRecipeTitle']) ? $recipeRow['originalRecipeTitle'] : "", 
                'creatorId' => isset($recipeRow['creatorId']) ? $recipeRow['creatorId'] : 0, 
                'creatorName' => isset($recipeRow['creatorName']) ? $recipeRow['creatorName'] : "", 
                'mainImageId' => isset($recipeRow['mainImageId']) ? $recipeRow['mainImageId'] : 0, 
                'mainImageFileName' => isset($recipeRow['mainImageFileName']) ? $recipeRow['mainImageFileName'] : "", 
                'mainCategoryId' => isset($recipeRow['mainCategoryId']) ? $recipeRow['mainCategoryId'] : 0, 
                'mainCategoryName' => isset($recipeRow['mainCategoryName']) ? $recipeRow['mainCategoryName'] : "", 
                'publicationType' => isset($recipeRow['publicationType']) ? $recipeRow['publicationType'] : "", 
                'difficultyType' => isset($recipeRow['difficultyType']) ? $recipeRow['difficultyType'] : "", 
                'preparationTime' => isset($recipeRow['preparationTime']) ? $recipeRow['preparationTime'] : 0, 
                'viewedCount' => isset($recipeRow['viewedCount']) ? $recipeRow['viewedCount'] : 0, 
                'cookedCount' => isset($recipeRow['cookedCount']) ? $recipeRow['cookedCount'] : 0, 
                'pinnedCount' => isset($recipeRow['pinnedCount']) ? $recipeRow['pinnedCount'] : 0, 
                'modifiedCount' => isset($recipeRow['modifiedCount']) ? $recipeRow['modifiedCount'] : 0, 
                'variedCount' => isset($recipeRow['variedCount']) ? $recipeRow['variedCount'] : 0, 
                'sharedCount' => isset($recipeRow['sharedCount']) ? $recipeRow['sharedCount'] : 0, 
                'rating' => isset($recipeRow['rating']) ? $recipeRow['rating'] : 0, 
                'createdDateTime' => isset($recipeRow['createdDateTime']) ? $recipeRow['createdDateTime'] : "", 
                'lastModifiedDateTime' => isset($recipeRow['lastModifiedDateTime']) ? $recipeRow['lastModifiedDateTime'] : "", 
                'lastCookedDateTime' => isset($recipeRow['lastCookedDateTime']) ? $recipeRow['lastCookedDateTime'] : ""
            );

            // query categories
            if($includeCategories == true)
            {
                $selectRecipeCategoriesSql = "select distinct category.categoryId, categoryNameString.originalValue as categoryName " .
                    "from Recipes recipe " .
                    "left join RecipeCategories recipeCategory on recipeCategory.recipeId = ? " .
                    "left join Categories category on category.categoryId = recipeCategory.categoryId " .
                    "left join Strings categoryNameString on categoryNameString.stringId = category.nameStringId";

                if($debug == true)
                    $sqlqueries['selectRecipeCategoriesSql'] = $selectRecipeCategoriesSql;

                $selectRecipeCategoriesStmt = $database->prepare($selectRecipeCategoriesSql);
                $selectRecipeCategoriesStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
                $selectRecipeCategoriesStmt->execute();
                $recipeCategoryRows = $selectRecipeCategoriesStmt->fetchAll(PDO::FETCH_ASSOC);
                $categories = array();

                foreach($recipeCategoryRows as $categoryRow) {
                    $category = array(
                        'categoryId' => isset($categoryRow['categoryId']) ? $categoryRow['categoryId'] : 0, 
                        'categoryName' => isset($categoryRow['categoryName']) ? $categoryRow['categoryName'] : ""
                    );
                    $categories[] = $category;
                }

                $recipe['categories'] = $categories;
            }

            // query tags
            if($includeTags == true)
            {
                $selectRecipeTagsSql = "select tag.tagId, tag.name as tagName " .
                    "from Tags tag " .
                    "left join RecipeTags recipeTag on recipeTag.tagId = tag.tagId " .
                    "left join Recipes recipe on recipeTag.recipeId = recipe.recipeId and recipeTag.recipeId = ?";

                if($debug == true)
                    $sqlqueries['selectRecipeTagsSql'] = $selectRecipeTagsSql;

                $selectRecipeTagsStmt = $database->prepare($selectRecipeTagsSql);
                $selectRecipeTagsStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
                $selectRecipeTagsStmt->execute();
                $recipeTagRows = $selectRecipeTagsStmt->fetchAll(PDO::FETCH_ASSOC);
                $tags = array();

                foreach($recipeTagRows as $tagRow) {
                    $tag = array(
                        'tagId' => isset($tagRow['tagId']) ? $tagRow['tagId'] : 0, 
                        'tagName' => isset($tagRow['tagName']) ? $tagRow['tagName'] : ""
                    );
                    $tags[] = $tag;
                }

                $recipe['tags'] = $tags;
            }

            // query cooking steps
            if($includeSteps == true)
            {
                $selectRecipeStepsSql = "select recipeStep.recipeStepId, recipeStep.stepNumber, stepTitleString.originalValue as stepTitle, " .
                    "stepDescriptionString.originalValue as stepDescription " .
                    "from RecipeSteps recipeStep " .
                    "left join Strings stepTitleString on stepTitleString.stringId = recipeStep.titleStringId " .
                    "left join Strings stepDescriptionString on stepDescriptionString.stringId = recipeStep.descriptionStringId " .
                    "where recipeStep.recipeId = ? " .
                    "order by recipeStep.stepNumber asc";

                if($debug == true)
                    $sqlqueries['selectRecipeStepsSql'] = $selectRecipeStepsSql;

                $selectRecipeStepsStmt = $database->prepare($selectRecipeStepsSql);
                $selectRecipeStepsStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
                $selectRecipeStepsStmt->execute();
                $recipeStepRows = $selectRecipeStepsStmt->fetchAll(PDO::FETCH_ASSOC);
                $steps = array();

                foreach($recipeStepRows as $stepRow) {
                    $step = array(
                        'recipeStepId' => isset($stepRow['recipeStepId']) ? $stepRow['recipeStepId'] : 0, 
                        'stepNumber' => isset($stepRow['stepNumber']) ? $stepRow['stepNumber'] : 0, 
                        'stepTitle' => isset($stepRow['stepTitle']) ? $stepRow['stepTitle'] : "", 
                        'stepDescription' => isset($stepRow['stepDescription']) ? $stepRow['stepDescription'] : ""
                    );

                    $recipeStepId = $stepRow['recipeStepId'];

                    // query step ingredients
                    $selectRecipeStepIngredientsSql = "select recipeStepIngredient.ingredientId, ingredientNameString.originalValue as ingredientName, " .
                        "ingredientDescriptionString.originalValue as ingredientDescription, recipeStepIngredient.amount as ingredientAmount, " .
                        "recipeStepIngredient.unitTypeId, unitTypeNameString.originalValue as unitTypeName, unitType.abbreviation as unitTypeAbbreviation, " . 
                        "recipeStepIngredient.customUnit " .
                        "from RecipeStepIngredients recipeStepIngredient " .
                        "left join Ingredients ingredient on ingredient.ingredientId = recipeStepIngredient.ingredientId " .
                        "left join Strings ingredientNameString on ingredientNameString.stringId = ingredient.nameStringId " .
                        "left join Strings ingredientDescriptionString on ingredientDescriptionString.stringId = ingredient.descriptionStringId " .
                        "left join UnitTypes unitType on unitType.unitTypeId = recipeStepIngredient.unitTypeId " .
                        "left join Strings unitTypeNameString on unitTypeNameString.stringId = unitType.nameStringId " .
                        "where recipeStepIngredient.recipeStepId = ? ";

                    if($debug == true)
                        $sqlqueries['selectRecipeStepIngredientsSql'] = $selectRecipeStepIngredientsSql;

                    $selectRecipeStepIngredientsStmt = $database->prepare($selectRecipeStepIngredientsSql);
                    $selectRecipeStepIngredientsStmt->bindValue(1, $recipeStepId, PDO::PARAM_INT);
                    $selectRecipeStepIngredientsStmt->execute();
                    $recipeStepIngredientRows = $selectRecipeStepIngredientsStmt->fetchAll(PDO::FETCH_ASSOC);
                    $stepIngredients = array();

                    foreach($recipeStepIngredientRows as $stepIngredientRow) {
                        $stepIngredient = array(
                            'ingredientId' => isset($stepIngredientRow['ingredientId']) ? $stepIngredientRow['ingredientId'] : 0, 
                            'ingredientName' => isset($stepIngredientRow['ingredientName']) ? $stepIngredientRow['ingredientName'] : "", 
                            'ingredientDescription' => isset($stepIngredientRow['ingredientDescription']) ? $stepIngredientRow['ingredientDescription'] : "", 
                            'ingredientAmount' => isset($stepIngredientRow['ingredientAmount']) ? $stepIngredientRow['ingredientAmount'] : 0, 
                            'unitTypeId' => isset($stepIngredientRow['unitTypeId']) ? $stepIngredientRow['unitTypeId'] : 0,
                            'unitTypeName' => isset($stepIngredientRow['unitTypeName']) ? $stepIngredientRow['unitTypeName'] : "",
                            'unitTypeAbbreviation' => isset($stepIngredientRow['unitTypeAbbreviation']) ? $stepIngredientRow['unitTypeAbbreviation'] : "",
                            'customUnit' => isset($stepIngredientRow['customUnit']) ? $stepIngredientRow['customUnit'] : ""
                        );

                        $stepIngredients[] = $stepIngredient;
                    }

                    $step['ingredients'] = $stepIngredients;
                    $steps[] = $step;
                }

                $recipe['steps'] = $steps;
            }

            break; // there should be only one recipe row for a given recipeId anyway
        }
    }
    catch(PDOException $e) {
        // rollback uncommited changes
        $array = array(
            'errcode' => 6,
            'pdo.code' => $e->getCode(), 
            'pdo.message' => $e->getMessage(), 
        );
        returnErrorArray($array);
        exit();
    }

    // only return data if there is a recipe to return
    if(isset($recipe))
    {
        // $recipes = array();
        // $recipes[] = $recipe;

        // // build return data
        // $data = array(
        //     'recipes' => $recipes,
        // );

        // if($debug == true)
        //     $data['sqlqueries'] = $sqlqueries;

        // encode and return json
        echo json_encode($recipe, JSON_PRETTY_PRINT);
    }
?>