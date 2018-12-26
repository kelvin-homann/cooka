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
    $numCategoriesRequested = -1;
    $numTagsRequested = -1;
    $numRecipeStepsRequested = -1;
    $numRecipeRatingsRequested = 0;
    $recipe = null;
    $sqlqueries = array();

    if(isset($_getpost['numCategoriesRequested']) && is_numeric($_getpost['numCategoriesRequested']))
        $numCategoriesRequested = $_getpost['numCategoriesRequested'];
    if(isset($_getpost['numTagsRequested']) && is_numeric($_getpost['numTagsRequested']))
        $numTagsRequested = $_getpost['numTagsRequested'];
    if(isset($_getpost['numRecipeStepsRequested']) && is_numeric($_getpost['numRecipeStepsRequested']))
        $numRecipeStepsRequested = $_getpost['numRecipeStepsRequested'];
    if(isset($_getpost['numRecipeRatingsRequested']) && is_numeric($_getpost['numRecipeRatingsRequested']))
        $numRecipeRatingsRequested = $_getpost['numRecipeRatingsRequested'];
    
    try {
        // select the actual recipe
        $selectRecipeSql = "select recipe.recipeId, titleString.originalLanguageId as languageId, " .
            "titleString.originalValue as title, descriptionString.originalValue as description, " .
            // "titleString.originalLanguageId as languageId, languageNameString.originalValue as languageName, " .
            "recipe.originalRecipeId, originalRecipeTitleString.originalValue as originalTitle, usr.userId as creatorId, " .
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
            // "left join Languages language on language.languageId = titleString.originalLanguageId " .
            // "left join Strings languageNameString on language.nameStringId = languageNameString.stringId " .
            "left join Images mainImage on mainImage.imageId = recipe.mainImageId " .
            "left join Logins login on login.userId = usr.userId " .
            "where recipe.recipeId = ? and (recipe.publicationType = 'public' or recipe.publicationType = 'unlisted' or " .
            "(recipe.publicationType = 'private' and login.accessToken = ?))";
        
        // build params map
        $selectRecipeParams = array(
            1 => array($recipeId, PDO::PARAM_INT),
            2 => array($accessToken, PDO::PARAM_STR),
        );

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($selectRecipeSql, $selectRecipeParams);
            $sqlQueries[] = $query;
        }

        $selectRecipeStmt = $database->prepare($selectRecipeSql);
        foreach($selectRecipeParams as $index => $param)
            $selectRecipeStmt->bindValue($index, $param[0], $param[1]);
        $selectRecipeStmt->execute();
        $recipeRows = $selectRecipeStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($recipeRows as $recipeRow) {
            $recipe = array(
                'recipeId' => isset($recipeRow['recipeId']) ? $recipeRow['recipeId'] : 0, 
                'languageId' => isset($recipeRow['languageId']) ? $recipeRow['languageId'] : 0, 
                'title' => isset($recipeRow['title']) ? $recipeRow['title'] : "", 
                'description' => isset($recipeRow['description']) ? $recipeRow['description'] : "", 
                'originalRecipeId' => isset($recipeRow['originalRecipeId']) ? $recipeRow['originalRecipeId'] : 0, 
                'originalTitle' => isset($recipeRow['originalTitle']) ? $recipeRow['originalTitle'] : "", 
                // 'languageId' => isset($recipeRow['languageId']) ? $recipeRow['languageId'] : 0, 
                // 'languageName' => isset($recipeRow['languageName']) ? $recipeRow['languageName'] : "", 
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
            if($numCategoriesRequested != 0)
            {
                $selectRecipeCategoriesSql = "select distinct category.categoryId, categoryNameString.originalValue as name " .
                    "from Recipes recipe " .
                    "left join RecipeCategories recipeCategory on recipeCategory.recipeId = ? " .
                    "left join Categories category on category.categoryId = recipeCategory.categoryId " .
                    "left join Strings categoryNameString on categoryNameString.stringId = category.nameStringId " .
                    "order by recipeCategory.categorizedDateTime desc";

                if($numCategoriesRequested > 0)
                    $selectRecipeCategoriesSql .= " limit ?";

                if($debug == true)
                    $sqlqueries['selectRecipeCategoriesSql'] = $selectRecipeCategoriesSql;

                $selectRecipeCategoriesStmt = $database->prepare($selectRecipeCategoriesSql);
                $selectRecipeCategoriesStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
                if($numCategoriesRequested > 0)
                    $selectRecipeCategoriesStmt->bindValue(2, $numCategoriesRequested, PDO::PARAM_INT);
                $selectRecipeCategoriesStmt->execute();
                $recipeCategoryRows = $selectRecipeCategoriesStmt->fetchAll(PDO::FETCH_ASSOC);
                $categories = array();

                foreach($recipeCategoryRows as $categoryRow) {
                    $category = array(
                        'categoryId' => isset($categoryRow['categoryId']) ? $categoryRow['categoryId'] : 0, 
                        'name' => isset($categoryRow['name']) ? $categoryRow['name'] : ""
                    );
                    $categories[] = $category;
                }

                $recipe['categories'] = $categories;
            }
            else
                $recipe['categories'] = array();

            $recipe['numCategoriesRequested'] = $numCategoriesRequested;

            // query tags
            if($numTagsRequested != 0)
            {
                $selectRecipeTagsSql = "select tag.tagId, tag.name " .
                    "from Tags tag " .
                    "right join RecipeTags recipeTag on recipeTag.tagId = tag.tagId and recipeTag.recipeId = ? " .
                    "left join Recipes recipe on recipeTag.recipeId = recipe.recipeId " .
                    "order by recipeTag.taggedDateTime desc";

                if($numTagsRequested > 0)
                    $selectRecipeTagsSql .= " limit ?";

                if($debug == true)
                    $sqlqueries['selectRecipeTagsSql'] = $selectRecipeTagsSql;

                $selectRecipeTagsStmt = $database->prepare($selectRecipeTagsSql);
                $selectRecipeTagsStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
                if($numTagsRequested > 0)
                    $selectRecipeTagsStmt->bindValue(2, $numTagsRequested, PDO::PARAM_INT);
                $selectRecipeTagsStmt->execute();
                $recipeTagRows = $selectRecipeTagsStmt->fetchAll(PDO::FETCH_ASSOC);
                $tags = array();

                foreach($recipeTagRows as $tagRow) {
                    $tag = array(
                        'tagId' => isset($tagRow['tagId']) ? $tagRow['tagId'] : 0, 
                        'name' => isset($tagRow['name']) ? $tagRow['name'] : ""
                    );
                    $tags[] = $tag;
                }

                $recipe['tags'] = $tags;
            }
            else
                $recipe['tags'] = array();

            $recipe['numTagsRequested'] = $numTagsRequested;

            // query cooking steps
            if($numRecipeStepsRequested != 0)
            {
                $selectRecipeStepsSql = "select recipeStep.recipeStepId, recipeStep.stepNumber, stepTitleString.originalValue as stepTitle, " .
                    "stepDescriptionString.originalValue as stepDescription " .
                    "from RecipeSteps recipeStep " .
                    "left join Strings stepTitleString on stepTitleString.stringId = recipeStep.titleStringId " .
                    "left join Strings stepDescriptionString on stepDescriptionString.stringId = recipeStep.descriptionStringId " .
                    "where recipeStep.recipeId = ? " .
                    "order by recipeStep.stepNumber asc";

                if($numRecipeStepsRequested > 0)
                    $selectRecipeStepsSql .= " limit ?";

                if($debug == true)
                    $sqlqueries['selectRecipeStepsSql'] = $selectRecipeStepsSql;

                $selectRecipeStepsStmt = $database->prepare($selectRecipeStepsSql);
                $selectRecipeStepsStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
                if($numRecipeStepsRequested > 0)
                    $selectRecipeStepsStmt->bindValue(2, $numRecipeStepsRequested, PDO::PARAM_INT);
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

                    $step['recipeStepIngredients'] = $stepIngredients;
                    $steps[] = $step;
                }

                $recipe['recipeSteps'] = $steps;
            }
            else
                $recipe['recipeSteps'] = array();

            $recipe['numRecipeStepsRequested'] = $numRecipeStepsRequested;

            // query ratings
            if($numRatingsRequested != 0) {
            }

            $recipe['recipeRatings'] = array();
            $recipe['numRecipeRatingsRequested'] = $numRecipeRatingsRequested;

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
