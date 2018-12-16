<?php
    if(!isset($_getpost['userAccessToken'])) {
        returnError(4, "the parameter userAccessToken was not specified", 0, "");
        return;
    }
    
    $database = connect();
    if($database == null) return;

    $userAccessToken = $_getpost['userAccessToken'];
    $tags = $_getpost['tags'];
    $recipeId = null;

    $othertags = array(
        'nudeln', 
        'pasta', 
        'italienisch', 
        'tomaten'
    );

    // echo base64_encode(serialize($othertags));
    // exit();

    if(isset($_getpost['tags'])) {
        $tags_array = @unserialize(base64_decode($_getpost['tags']));

        if($tags_array) {
            echo 'unserialized tags:<br />';
            foreach($tags_array as $tag) {
                echo '<li>#' . $tag . '</li>';
            }
        }
    }

    exit();
    
    try {
        // select the actual recipe
        $selectRecipeStmt = $database->prepare("select * from RecipesDetailed where recipeId = ?");
        $selectRecipeStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
        $selectRecipeStmt->execute();
        $recipeRows = $selectRecipeStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($recipeRows as $recipe) {
            $recipe_1 = array(
                'recipeId' => $recipe['recipeId'], 
                'recipeTitle' => $recipe['recipeTitle'], 
                'recipeDescription' => $recipe['recipeDescription'], 
                'originalRecipeId' => $recipe['originalRecipeId'], 
                'originalRecipeTitle' => $recipe['originalRecipeTitle'], 
                'userId' => $recipe['userId'], 
                'creatorName' => $recipe['creatorName'], 
                'mainCategoryId' => $recipe['mainCategoryId'], 
                'mainCategoryName' => $recipe['mainCategoryName'], 
            );

            // query categories
            $categories = array();

            $recipe_2 = array(
                'categories' => $categories, 
                'publicationType' => $recipe['publicationType'], 
                'difficultyType' => $recipe['difficultyType'], 
                'preparationTime' => $recipe['preparationTime'], 
                'viewedCount' => $recipe['viewedCount'], 
                'cookedCount' => $recipe['cookedCount'], 
                'pinnedCount' => $recipe['pinnedCount'], 
                'modifiedCount' => $recipe['modifiedCount'], 
                'variedCount' => $recipe['variedCount'], 
                'sharedCount' => $recipe['sharedCount'], 
                'createdDateTime' => $recipe['createdDateTime'], 
                'lastModifiedDateTime' => $recipe['lastModifiedDateTime'], 
                'lastCookedDateTime' => $recipe['lastCookedDateTime']
            );

            // query tags
            $selectRecipeTagsStmt = $database->prepare("select tag.tagId, tag.name from Tags tag left join RecipeTags recipeTag on recipeTag.tagId = tag.tagId left join Recipes recipe on recipeTag.recipeId = recipe.recipeId and recipeTag.recipeId = ?");
            $selectRecipeTagsStmt->bindValue(1, $recipeId, PDO::PARAM_INT);
            $selectRecipeTagsStmt->execute();
            $recipeTagRows = $selectRecipeTagsStmt->fetchAll(PDO::FETCH_ASSOC);

            $tags = array();

            foreach($recipeTagRows as $tag) {
                array_push($tags, '#' . $tag['name']);
            }

            $recipe_3 = array(
                'tags' => $tags
            );

            $recipe = array_merge($recipe_1, $recipe_2, $recipe_3);

            break; // there should be only one row anyway
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

    $data = array(
        'recipe' => $recipe,
    );

    // encode and return json

    $jsonstring = json_encode($data, JSON_PRETTY_PRINT);

    header('Content-Type: application/json');
    echo $jsonstring;
?>
