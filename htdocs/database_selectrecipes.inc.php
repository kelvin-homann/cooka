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

    $mockup_usernames = null;
    
    try {
        // select the actual recipes
        $selectRecipesSql = 
            "select recipeId, languageId, title, creatorId, creatorName, mainImageId, mainImageFileName, " .
            "mainCategoryId, mainCategoryName, difficultyType, preparationTime, cookedCount, pinnedCount, " .
            "variedCount, rating, fairRating, createdDateTime, lastCookedDateTime, flags, score, " .
            "sum(score) as trendRating from (" .

            "select recipe.recipeId, titleString.originalLanguageId as languageId, " .
            "titleString.originalValue as title, recipe.creatorId, " .
            "usr.userName as creatorName, recipe.mainImageId, mainImage.imageFileName as mainImageFileName, " .
            "recipe.mainCategoryId, categoryNameString.originalValue as mainCategoryName, recipe.difficultyType, " .
            "recipe.preparationTime, recipe.cookedCount, recipe.pinnedCount, recipe.variedCount, recipe.rating, " .
            "floor(recipe.rating * 2) / 2 as fairRating, recipe.createdDateTime, recipe.lastCookedDateTime, " .
            "recipe.flags, cooking.recipeCookingId, " .
            "(case when cooking.cookedDateTime is not null then 28 - least(datediff(now(), cooking.cookedDateTime), 28) else 1.0 end) as score " .
            "from Recipes recipe " .
            "left join Recipes originalRecipe on recipe.originalRecipeId = originalRecipe.recipeId " .
            "left join Users usr on recipe.creatorId = usr.userId " .
            "left join Categories category on recipe.mainCategoryId = category.categoryId " .
            "left join Strings titleString on recipe.titleStringId = titleString.stringId " .
            "left join Strings descriptionString on recipe.descriptionStringId = descriptionString.stringId " .
            "left join Strings categoryNameString on category.nameStringId = categoryNameString.stringId " .
            "left join Images mainImage on mainImage.imageId = recipe.mainImageId " .
            "left join RecipeCookings cooking on cooking.recipeId = recipe.recipeId ";

        $p = 1;
        $selectRecipesParams = array();

        if(isset($_getpost['filterKey'])) {
            $filterKey = $_getpost['filterKey'];
            if(!is_array($filterKey)) $filterKey = array($filterKey);
        }

        /* FILTERING: JOINS */

        if(isset($_getpost['filterKey'])) {
            $selectRecipesJoinsSql = "";
            //$selectRecipesFilterParams = array();
            $fp = $p;
            for($i = 0; $i < count($filterKey); $i++) {
                list($key, $value) = explode(':', $filterKey[$i]);
                $value = trim($value);

                if($key == 'tag') {
                    $selectRecipesFilterSql .= "left join RecipeTags rt on rt.recipeId = recipe.recipeId " .
                        "left join Tags tag on tag.tagId = rt.tagId ";
                }

                else if($key == 'category') {
                    $selectRecipesFilterSql .= "left join RecipeCategories rc on rc.recipeId = recipe.recipeId " .
                        "left join Categories category on category.categoryId = rc.categoryId ";
                }
            }

            // save adding
            if(strlen($selectRecipesJoinsSql) > 0) {
                $selectRecipesSql .= $selectRecipesJoinsSql;
                //$selectRecipesParams = $selectRecipesParams + $selectRecipesFilterParams;
                $p = $fp;
            }
        }

        /* FILTERING: WHERES */

        $selectRecipesSql .= "where recipe.publicationType = 'public' ";
        //$selectRecipesParams[$p++] = array('public', PDO::PARAM_STR);

        if(isset($_getpost['filterKey'])) {
            $selectRecipesFilterSql = "";
            $selectRecipesFilterParams = array();
            $fp = $p;
            for($i = 0; $i < count($filterKey); $i++) {
                list($key, $value) = explode(':', $filterKey[$i]);
                $value = trim($value);

                if($key == 'contains') {
                    $value = '%' . $value . '%';
                    $selectRecipesFilterSql .= "and (titleString.originalValue like ? or descriptionString.originalValue like ? or categoryNameString.originalValue like ?) ";
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_STR);
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_STR);
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_STR);
                }

                else if($key == 'creatorId' && isset($value) && is_numeric($value)) {
                    $selectRecipesFilterSql .= "and recipe.creatorId = ? ";
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_INT);
                }

                else if($key == 'difficulty' && isset($value)) {
                    switch($value) {
                    case 'simple':
                        $selectRecipesFilterSql .= "and recipe.difficultyType = 'simple' ";
                        break;
                    case 'moderate':
                        $selectRecipesFilterSql .= "and recipe.difficultyType = 'moderate' ";
                        break;
                    case 'demanding':
                        $selectRecipesFilterSql .= "and recipe.difficultyType = 'demanding' ";
                        break;
                    }
                }

                else if($key == 'category' && isset($value) && is_numeric($value)) {
                    $selectRecipesFilterSql .= "and recipe.mainCategoryId = ? ";
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_INT);
                }

                else if($key == 'category' && isset($value)) {
                    $selectRecipesFilterSql .= "and categoryNameString.originalValue like ? ";
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_STR);
                }

                else if($key == 'hasImage' && isset($value)) {
                    if($value == 'true' || $value == true) $selectRecipesFilterSql .= "and recipe.mainImageId is not null ";
                    else if($value == 'false' || $value == false) $selectRecipesFilterSql .= "and recipe.mainImageId is null ";
                }

                else if($key == 'wasVaried' && isset($value)) {
                    if($value == 'true' || $value == true) $selectRecipesFilterSql .= "and recipe.variedCount > 0 ";
                    else if($value == 'false' || $value == false) $selectRecipesFilterSql .= "and recipe.variedCount = 0 ";
                }

                else if($key == 'flags' && isset($value) && is_numeric($value)) {
                    $selectRecipesFilterSql .= "and recipe.flags = ? ";
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_INT);
                }

                else if($key == 'hasFlags' && isset($value) && is_numeric($value)) {
                    $selectRecipesFilterSql .= "and recipe.flags & ? != 0 ";
                    $selectRecipesFilterParams[$fp++] = array($value, PDO::PARAM_INT);
                }
            }

            // save adding
            if(strlen($selectRecipesFilterSql) > 0) {
                $selectRecipesSql .= $selectRecipesFilterSql;
                $selectRecipesParams = $selectRecipesParams + $selectRecipesFilterParams;
                $p = $fp;
            }
        }

        // close outer select
        $selectRecipesSql .= ') as RatedRecipes group by recipeId ';

        /* ORDERING */

        if(isset($_getpost['sortKey'])) {
            $sortKey = $_getpost['sortKey'];
            $allowedSortKeys = array(
                'title' => 'recipeTitle', 
                'difficulty' => 'difficultyType', 
                'preparationTime' => 'preparationTime', 
                'cookedCount' => 'cookedCount', 
                'pinnedCount' => 'pinnedCount', 
                'rating' => 'rating',
                'fairRating' => 'fairRating', 
                'trendRating' => 'trendRating', 
                'created' => 'createdDateTime',
                'lastCooked' => 'lastCookedDateTime'
            );
            $selectRecipesOrderSql = "";
            if(is_array($sortKey)) {
                for($i = 0; $i < count($sortKey); $i++) {
                    list($k, $d) = explode(':', $sortKey[$i]);
                    if(!array_key_exists($k, $allowedSortKeys)) continue;
                    if(!isset($d) || $d != 'desc') $d = 'asc';
                    if($i > 0) $selectRecipesOrderSql .= ', ' . $allowedSortKeys[$k] . ' ' . $d;
                    else $selectRecipesOrderSql .= $allowedSortKeys[$k] . ' ' . $d;
                    if($k == 'fairRating') $selectRecipesOrderSql .= ', rand()';
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
        }

        $selectRecipesStmt = $database->prepare($selectRecipesSql);
        foreach($selectRecipesParams as $index => $param)
            $selectRecipesStmt->bindValue($index, $param[0], $param[1]);
        $selectRecipesStmt->execute();
        $recipeRows = $selectRecipesStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($recipeRows as $recipeRow) {

            $creatorName = "";
            $recipeId = $recipeRow['recipeId'];
            $flags = isset($recipeRow['flags']) ? $recipeRow['flags'] : 0;

            if($flags & 1 == 1 && (!isset($recipeRow['creatorId']) || $recipeRow['creatorId'] == 0)) {
                if(!isset($mockup_usernames)) {
                    $mockup_usernames = include($scriptDir . '/mockup_usernames.inc.php');
                }
                $index = $recipeId % count($mockup_usernames);
                $creatorName = $mockup_usernames[$index];
            }
            else if(isset($recipeRow['creatorName'])) {
                $creatorName = $recipeRow['creatorName'];
            }

            $recipe = array(
                'recipeId' => isset($recipeId) ? $recipeId : 0, 
                'languageId' => isset($recipeRow['languageId']) ? $recipeRow['languageId'] : 0, 
                'title' => isset($recipeRow['title']) ? $recipeRow['title'] : "", 
                'creatorId' => isset($recipeRow['creatorId']) ? $recipeRow['creatorId'] : 0, 
                'creatorName' => $creatorName, 
                'mainImageId' => isset($recipeRow['mainImageId']) ? $recipeRow['mainImageId'] : 0, 
                'mainImageFileName' => isset($recipeRow['mainImageFileName']) ? $recipeRow['mainImageFileName'] : "", 
                'mainCategoryId' => isset($recipeRow['mainCategoryId']) ? $recipeRow['mainCategoryId'] : 0, 
                'mainCategoryName' => isset($recipeRow['mainCategoryName']) ? $recipeRow['mainCategoryName'] : "", 
                'difficultyType' => isset($recipeRow['difficultyType']) ? $recipeRow['difficultyType'] : "moderate", 
                'preparationTime' => isset($recipeRow['preparationTime']) ? $recipeRow['preparationTime'] : 0, 
                'cookedCount' => isset($recipeRow['cookedCount']) ? $recipeRow['cookedCount'] : 0, 
                'pinnedCount' => isset($recipeRow['pinnedCount']) ? $recipeRow['pinnedCount'] : 0, 
                'rating' => isset($recipeRow['rating']) ? $recipeRow['rating'] : 0, 
              //'trendRating' => isset($recipeRow['trendRating']) ? doubleval($recipeRow['trendRating']) : 0, 
                'flags' => $flags
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

    // encode and return json
    echo json_encode($recipes, JSON_PRETTY_PRINT);
?>
