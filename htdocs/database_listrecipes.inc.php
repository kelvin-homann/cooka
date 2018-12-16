<?php
    if(!isset($_getpost['userId'])) {
        returnError(3, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['userAccessToken'])) {
        returnError(4, "the parameter userAccessToken was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $userId = $_getpost['userId'];
    $userAccessToken = $_getpost['userAccessToken'];
    $sortMode = 'title';
    $sortDirection = 'asc';
    $filterMode = null;

    $recipes = array();
    $numRecipes = 0;

    if(isset($_getpost['sortMode']))
    {
        switch($_getpost['sortMode']) {
        case 'rating':
            $sortMode = 'rating';
            break;
        }
    }

    if(isset($_getpost['sortDirection']) && $_getpost['sortDirection'] == 'desc')
        $sortDirection = 'desc';
    
    try {
        // select the actual recipes
        $selectRecipeStmt = $database->prepare(
            "select recipe.recipeId, titleString.originalValue as recipeTitle, usr.userId as creatorId, usr.userName as creatorName, " .
            "recipe.mainImageId, mainImage.imageFileName as mainImageFileName, recipe.mainCategoryId, categoryNameString.originalValue as mainCategoryName, " .
            "recipe.difficultyType, recipe.preparationTime, recipe.cookedCount, recipe.pinnedCount, recipe.rating " .
            "from Recipes recipe " .
            "left join Recipes originalRecipe on recipe.originalRecipeId = originalRecipe.recipeId " .
            "left join Users usr on recipe.creatorId = usr.userId " .
            "left join Categories category on recipe.mainCategoryId = category.categoryId " .
            "left join Strings titleString on recipe.titleStringId = titleString.stringId " .
            "left join Strings categoryNameString on category.nameStringId = categoryNameString.stringId " .
            "left join Images mainImage on mainImage.imageId = recipe.mainImageId " .
            "where recipe.publicationType = 'public'"
        );
        $selectRecipeStmt->execute();
        $recipeRows = $selectRecipeStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($recipeRows as $recipeRow) {
            $recipe = array(
                'recipeId' => isset($recipeRow['recipeId']) ? $recipeRow['recipeId'] : 0, 
                'recipeTitle' => isset($recipeRow['recipeTitle']) ? $recipeRow['recipeTitle'] : "", 
                'creatorId' => isset($recipeRow['creatorId']) ? $recipeRow['creatorId'] : 0, 
                'creatorName' => isset($recipeRow['creatorName']) ? $recipeRow['creatorName'] : "", 
                'mainImageId' => isset($recipeRow['mainImageId']) ? $recipeRow['mainImageId'] : 0, 
                'mainImageFileName' => isset($recipeRow['mainImageFileName']) ? $recipeRow['mainImageFileName'] : "", 
                'mainCategoryId' => isset($recipeRow['mainCategoryId']) ? $recipeRow['mainCategoryId'] : 0, 
                'mainCategoryName' => isset($recipeRow['mainCategoryName']) ? $recipeRow['mainCategoryName'] : "", 
                'difficultyType' => isset($recipeRow['difficultyType']) ? $recipeRow['difficultyType'] : "", 
                'preparationTime' => isset($recipeRow['preparationTime']) ? $recipeRow['preparationTime'] : "", 
                'cookedCount' => isset($recipeRow['cookedCount']) ? $recipeRow['cookedCount'] : 0, 
                'pinnedCount' => isset($recipeRow['pinnedCount']) ? $recipeRow['pinnedCount'] : 0, 
                'rating' => isset($recipeRow['rating']) ? $recipeRow['rating'] : 0, 
            );

            $recipes[] = $recipe;
            $numRecipes++;
        }
    }
    catch(PDOException $e) {
        // rollback uncommited changes
        $array = array(
            'errcode' => 5,
            'pdo.code' => $e->getCode(), 
            'pdo.message' => $e->getMessage(), 
        );
        returnErrorArray($array);
        exit();
    }

    // build return data
    // $data = array(
    //     'result' => array(
    //         'numRecords' => $numRecipes
    //     ), 
    //     'recipes' => $recipes,
    // );

    // encode and return json
    echo json_encode($recipes, JSON_PRETTY_PRINT);
?>
