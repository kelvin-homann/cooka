<?php
    if(!isset($_getpost['userId'])) {
        returnError(3, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['accessToken'])) {
        returnError(4, "the parameter accessToken was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $userId = $_getpost['userId'];
    $accessToken = $_getpost['accessToken'];

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
        $selectRecipesSql = "select recipe.recipeId, titleString.originalValue as recipeTitle, recipe.creatorId, " .
            "usr.userName as creatorName, recipe.mainImageId, mainImage.imageFileName as mainImageFileName, " .
            "recipe.mainCategoryId, categoryNameString.originalValue as mainCategoryName, recipe.difficultyType, " .
            "recipe.preparationTime, recipe.cookedCount, recipe.pinnedCount, recipe.rating " .
            "from Recipes recipe " .
            "left join Recipes originalRecipe on recipe.originalRecipeId = originalRecipe.recipeId " .
            "left join Users usr on recipe.creatorId = usr.userId " .
            "left join Categories category on recipe.mainCategoryId = category.categoryId " .
            "left join Strings titleString on recipe.titleStringId = titleString.stringId " .
            "left join Strings categoryNameString on category.nameStringId = categoryNameString.stringId " .
            "left join Images mainImage on mainImage.imageId = recipe.mainImageId " .
            "where recipe.publicationType = ? ";

        $p = 1;
        $selectRecipesParams = array();
        $selectRecipesParams[$p++] = array('public', PDO::PARAM_STR);

        /* FILTERING */

        if(isset($_getpost['filterKey'])) {
            $filterKey = $_getpost['filterKey'];
            if(!is_array($filterKey)) $filterKey = array($filterKey);
            $selectRecipesFilterSql = "";
            $selectRecipesFilterParams = array();
            $fp = $p;
            for($i = 0; $i < count($filterKey); $i++) {
                list($key, $value) = explode(':', $filterKey[$i]);
                switch($key) {
                case 'contains':
                    $value = '%' . trim($value) . '%';
                    $selectRecipesFilterSql .= "and (titleString.originalValue like ? or categoryNameString.originalValue like ?) ";
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_STR);
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_STR);
                    break;

                case 'creatorId':
                    if(isset($value) && is_numeric($value)) {
                        $selectRecipesFilterSql .= "and recipe.creatorId = ? ";
                        $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_INT);
                    }
                    break;

                case 'hasImage':
                    if($value == 'true') $selectRecipesFilterSql .= "and recipe.mainImageId is not null ";
                    else if($value == 'false') $selectRecipesFilterSql .= "and recipe.mainImageId is null ";
                    break;
                }
            }

            // save adding
            if(count($selectRecipesFilterParams) > 0) {
                $selectRecipesSql .= $selectRecipesFilterSql;
                $selectRecipesParams = $selectRecipesParams + $selectRecipesFilterParams;
                $p = $fp;
            }
        }

        /* ORDERING */

        if(isset($_getpost['sortKey'])) {
            $sortKey = $_getpost['sortKey'];
            $allowedSortKeys = array(
                'title' => 'recipeTitle', 
                'difficultyType' => 'difficultyType', 
                'preparationTime' => 'preparationTime', 
                'cookedCount' => 'cookedCount', 
                'pinnedCount' => 'pinnedCount', 
                'rating' => 'rating'
            );
            $selectRecipesOrderSql = "";
            if(is_array($sortKey)) {
                for($i = 0; $i < count($sortKey); $i++) {
                    list($k, $d) = explode(':', $sortKey[$i]);
                    if(!array_key_exists($k, $allowedSortKeys)) continue;
                    if(!isset($d) || $d != 'desc') $d = 'asc';
                    if($i > 0) $selectRecipesOrderSql .= ', ' . $allowedSortKeys[$k] . ' ' . $d;
                    else $selectRecipesOrderSql .= $allowedSortKeys[$k] . ' ' . $d;
                }
            }
            else {
                list($k, $d) = explode(':', $sortKey);
                if(array_key_exists($k, $allowedSortKeys)) {
                    if(!isset($d) || $d != 'desc') $d = 'asc';
                    $selectRecipesOrderSql .= $allowedSortKeys[$k] . ' ' . $d;
                }
            }

            // save adding
            if(strlen($selectRecipesOrderSql) > 0)
                $selectRecipesSql .= "order by " . $selectRecipesOrderSql;
        }

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = count($selectRecipesParams) > 0 ? extendSqlQuery($selectRecipesSql, $selectRecipesParams) : $selectRecipesSql;
            $sqlQueries[] = $query;

            // if($logscreen) {
            //     echo '### ' . $query . ' ###';
            //     foreach($selectRecipesParams as $key => $value)
            //         echo ' < ' . $key . ': ' . $value[0] . ' > ';
            // }
        }

        $selectRecipesStmt = $database->prepare($selectRecipesSql);
        foreach($selectRecipesParams as $index => $param)
            $selectRecipesStmt->bindValue($index, $param[0], $param[1]);
        $selectRecipesStmt->execute();
        $recipeRows = $selectRecipesStmt->fetchAll(PDO::FETCH_ASSOC);

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
