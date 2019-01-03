<?php
    $resultCode = 0;
    $resultMessage = "";
    $recipeIdToReturn = 0;
    
    try {
        if(!isset($_getpost['userId'])) {
            $resultCode = 203000;
            $resultMessage = "the parameter userId was not specified";
            throw new Exception($resultMessage);
        }
        if(!isset($_getpost['accessToken'])) {
            $resultCode = 204000;
            $resultMessage = "the parameter accessToken was not specified";
            throw new Exception($resultMessage);
        }
        
        $database = connect();
        if($database == null) {
            $resultCode = 205000;
            $resultMessage = "the database connection could not be established";
            throw new Exception($resultMessage);
        }

        $userId = $_getpost['userId'];
        $accessToken = $_getpost['accessToken'];
        $languageId = 1031;
        $ignoreDuplicate = 0;
        $json = file_get_contents('php://input');
        $recipe = json_decode($json, true);
        $rollbackOnCategoryErrors = true;
        $rollbackOnTagErrors = true;

        if(isset($_getpost['languageId']) && is_numeric($_getpost['languageId']))
            $languageId = $_getpost['languageId'];
        if(isset($_getpost['ignoreDuplicate']) && boolval($_getpost['ignoreDuplicate']) == true)
            $ignoreDuplicate = 1;
        else
            $ignoreDuplicate = 0;

        if(!isset($json)) {
            $resultCode = 206000;
            $resultMessage = "the request does not contain a valid json body";
            throw new Exception($resultMessage);
        }

        file_put_contents('./json/createRecipe_' . date("Ynj") . '.json', $json);

        if(!isset($recipe)) {
            $resultCode = 207000;
            $resultMessage = "the request does not contain a valid recipe object";
            throw new Exception($resultMessage);
        }
    
        // start transaction block
        $database->beginTransaction();

        if(isset($recipe['languageId']) && is_numeric($recipe['languageId']))
            $languageId = $recipe['languageId'];
        else {
            $resultCode = 208000;
            $resultMessage = "the key languageId was not specified within the submitted recipe";
            throw new Exception($resultMessage);
        }

        $creatorId = $userId;
        if(isset($recipe['creatorId']) && is_numeric($recipe['creatorId']))
            $creatorId = $recipe['creatorId'];
        else {
            $resultCode = 209000;
            $resultMessage = "the key creatorId was not specified within the submitted recipe";
            throw new Exception($resultMessage);
        }

        // prepare string values sql string
        $numRecipeStringValues = 0;
        $insertRecipeStringsValuesSql = '';
        $insertRecipeStringsParams = array();

        // check for individual submitted key value pairs
        if(isset($recipe['title'])) {
            $insertRecipeStringsValuesSql = "(:rt_ov, :rt_ol)"; // rt = recipe title; ov = original value; ol = original language id
            $insertRecipeStringsParams[':rt_ov'] = array($recipe['title'], PDO::PARAM_STR);
            $insertRecipeStringsParams[':rt_ol'] = array($languageId, PDO::PARAM_INT);
            $numRecipeStringValues++;
        }
        else {
            $resultCode = 210000;
            $resultMessage = "the key title was not specified within the submitted recipe";
            throw new Exception($resultMessage);
        }

        if(isset($recipe['description'])) {
            if(strlen($insertRecipeStringsValuesSql) > 0) $insertRecipeStringsValuesSql .= ', ';
            $insertRecipeStringsValuesSql .= "(:rd_ov, :rd_ol)"; // rd = recipe description
            $insertRecipeStringsParams[':rd_ov'] = array($recipe['description'], PDO::PARAM_STR);
            $insertRecipeStringsParams[':rd_ol'] = array($languageId, PDO::PARAM_INT);
            $numRecipeStringValues++;
        }

        // check for duplicate recipe with same title and by same creator
        if($ignoreDuplicate == 0) {
            $existsRecipeSql = "select 1 from Recipes recipe " .
                "inner join Strings titleString on titleString.stringId = recipe.titleStringId and titleString.originalValue = :ts " .
                "where recipe.creatorId = :cid;";
            $existsRecipeParams = array(
                ':ts' => array($recipe['title'], PDO::PARAM_STR),
                ':cid' => array($creatorId, PDO::PARAM_INT)
            );

            // extend and log sql query
            if($logdb || $logfile || $logscreen) {
                $query = extendSqlQuery($insertMainImageSql, $insertMainImageParams);
                $sqlQueries[] = $query;
            }

            $existsRecipeStmt = $database->prepare($existsRecipeSql);
            foreach($existsRecipeParams as $index => $param)
                $existsRecipeStmt->bindValue($index, $param[0], $param[1]);
            $existsRecipeStmt->execute();
            $existsRecipeRows = $existsRecipeStmt->fetchAll(PDO::FETCH_ASSOC);

            // if there already exists a recipe with the same title and by the same creator
            if(count($existsRecipeRows) == 1) {
                $resultCode = 211000;
                $resultMessage = "a recipe with the title \"" . $recipe['title'] . "\" by creatorId $creatorId does already exist";
                throw new Exception($resultMessage);
            }
        }

        // insert recipe strings
        $insertRecipeStringsSql = 'insert into Strings (originalValue, originalLanguageId) values ' . $insertRecipeStringsValuesSql;
        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($insertRecipeStringsSql, $insertRecipeStringsParams);
            $sqlQueries[] = $query;
        }
        
        $insertRecipeStringsStmt = $database->prepare($insertRecipeStringsSql);
        foreach($insertRecipeStringsParams as $index => $param)
            $insertRecipeStringsStmt->bindValue($index, $param[0], $param[1]);
        $insertRecipeStringsStmt->execute();
        $insertRecipeStringsId = $database->lastInsertId();
        $numAffectedRows = $insertRecipeStringsStmt->rowCount();

        if($numRecipeStringValues != $numAffectedRows) {
            $resultCode = 212000;
            $resultMessage = "the number of inserted recipe strings ($numAffectedRows) is not equal to the number of provided strings ($numRecipeStringValues)";
            throw new Exception($resultMessage);
        }

        if(!isset($insertRecipeStringsId) || $insertRecipeStringsId == 0) {
            $resultCode = 213000;
            $resultMessage = "could not insert recipe strings into the database";
            throw new Exception($resultMessage);
        }

        $ido = 0;

        // add inserted string ids to the recipe map
        $recipe['titleStringId'] = $insertRecipeStringsId + $ido++;
        if(isset($recipe['description']))
            $recipe['descriptionStringId'] = $insertRecipeStringsId + $ido++;

        // insert recipe main image
        if(isset($recipe['mainImageFileName']) && strlen($recipe['mainImageFileName']) > 0) {

            $insertMainImageSql = 'insert into Images (imageFileName, creatorId) values (:ifn, :cid)';
            $insertMainImageParams = array(
                ':ifn' => array($recipe['mainImageFileName'], PDO::PARAM_STR),
                ':cid' => array($creatorId, PDO::PARAM_INT),
            );

            // extend and log sql query
            if($logdb || $logfile || $logscreen) {
                $query = extendSqlQuery($insertMainImageSql, $insertMainImageParams);
                $sqlQueries[] = $query;
            }

            $insertMainImageStmt = $database->prepare($insertMainImageSql);
            foreach($insertMainImageParams as $index => $param)
                $insertMainImageStmt->bindValue($index, $param[0], $param[1]);
            $insertMainImageStmt->execute();
            $insertMainImageId = $database->lastInsertId();
            $numAffectedRows = $insertMainImageStmt->rowCount();

            if($numAffectedRows == 0) {
                $resultCode = 214000;
                $resultMessage = "could not insert main image with file name \"" . $recipe['mainImageFileName'] . "\" into the database";
                throw new Exception($resultMessage);
            }

            $recipe['mainImageId'] = $insertMainImageId;
        }

        // build dynamic recipe insert sql string
        // insert recipe
        $insertRecipeParams = array();

        // fixed columns and values
        $insertRecipeColumnsSql = 'titleStringId, creatorId';
        $insertRecipeValuesSql = ':tsid, :cid';
        $insertRecipeParams[':tsid'] = array($recipe['titleStringId'], PDO::PARAM_INT);
        $insertRecipeParams[':cid'] = array($recipe['creatorId'], PDO::PARAM_INT);

        // dynamic columns and values
        if(isset($recipe['descriptionStringId']) && $recipe['descriptionStringId'] != 0) {
            $insertRecipeColumnsSql .= ', descriptionStringId';
            $insertRecipeValuesSql .= ', :dsid';
            $insertRecipeParams[':dsid'] = array($recipe['descriptionStringId'], PDO::PARAM_INT);
        }
        if(isset($recipe['originalRecipeId']) && $recipe['originalRecipeId'] != 0) {
            $insertRecipeColumnsSql .= ', originalRecipeId';
            $insertRecipeValuesSql .= ', :orid';
            $insertRecipeParams[':orid'] = array($recipe['originalRecipeId'], PDO::PARAM_INT);
        }
        if(isset($recipe['originalCreatorId']) && $recipe['originalCreatorId'] != 0) {
            $insertRecipeColumnsSql .= ', originalCreatorId';
            $insertRecipeValuesSql .= ', :ocid';
            $insertRecipeParams[':ocid'] = array($recipe['originalCreatorId'], PDO::PARAM_INT);
        }
        if(isset($recipe['mainCategoryId']) && $recipe['mainCategoryId'] != 0) {
            $insertRecipeColumnsSql .= ', mainCategoryId';
            $insertRecipeValuesSql .= ', :mcatid';
            $insertRecipeParams[':mcatid'] = array($recipe['mainCategoryId'], PDO::PARAM_INT);
        }
        if(isset($recipe['mainImageId']) && $recipe['mainImageId'] != 0) {
            $insertRecipeColumnsSql .= ', mainImageId';
            $insertRecipeValuesSql .= ', :mimgid';
            $insertRecipeParams[':mimgid'] = array($recipe['mainImageId'], PDO::PARAM_INT);
        }
        if(isset($recipe['publicationType']) && strlen($recipe['publicationType']) > 0) {
            $insertRecipeColumnsSql .= ', publicationType';
            $insertRecipeValuesSql .= ', :publt';
            $insertRecipeParams[':publt'] = array($recipe['publicationType'], PDO::PARAM_STR);
        }
        if(isset($recipe['difficultyType']) && strlen($recipe['difficultyType']) > 0) {
            $insertRecipeColumnsSql .= ', difficultyType';
            $insertRecipeValuesSql .= ', :difft';
            $insertRecipeParams[':difft'] = array($recipe['difficultyType'], PDO::PARAM_STR);
        }
        if(isset($recipe['preparationTime']) && $recipe['preparationTime'] != 0) {
            $insertRecipeColumnsSql .= ', preparationTime';
            $insertRecipeValuesSql .= ', :prept';
            $insertRecipeParams[':prept'] = array($recipe['preparationTime'], PDO::PARAM_INT);
        }
        if(isset($recipe['flags']) && $recipe['flags'] != 0) {
            $flags = $recipe['flags'] ^ 0x00000020; // remove deleted-flag if set
            if($flags != 0) {
                $insertRecipeColumnsSql .= ', flags';
                $insertRecipeValuesSql .= ', :flags';
                $insertRecipeParams[':flags'] = array($flags, PDO::PARAM_INT);
            }
        }

        $insertRecipeSql = "insert into Recipes ($insertRecipeColumnsSql) values ($insertRecipeValuesSql)";
        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($insertRecipeSql, $insertRecipeParams);
            $sqlQueries[] = $query;
        }

        $insertRecipeStmt = $database->prepare($insertRecipeSql);
        foreach($insertRecipeParams as $index => $param)
            $insertRecipeStmt->bindValue($index, $param[0], $param[1]);
        $insertRecipeStmt->execute();
        $insertRecipeId = $database->lastInsertId();
        $numAffectedRows = $insertRecipeStmt->rowCount();

        if($numAffectedRows == 0) {
            $resultCode = 215000;
            $resultMessage = "could not insert recipe \"" . $recipe['title'] . "\" into the database";
            throw new Exception($resultMessage);
        }

        // add inserted recipe id to the recipe map
        $recipe['recipeId'] = $insertRecipeId;

        // if categories are provided
        if(isset($recipe['categories']) && is_array($recipe['categories'])) {

            $categories = $recipe['categories'];

            // insert recipe category relations
            $insertRecipeCategoriesValuesSql = '';
            $insertRecipeCategoriesParams = array();
            $i_category = 1;
            $numRecipeCategoriesToInsert = 0;

            foreach($categories as $category) {
                if(isset($category['categoryId']) && $category['categoryId'] > 0) {
                    if(strlen($insertRecipeCategoriesValuesSql) > 0) $insertRecipeCategoriesValuesSql .= ', ';
                    $insertRecipeCategoriesValuesSql .= "(:c${i_category}_rid, :c${i_category}_cid)";
                    $insertRecipeCategoriesParams[":c${i_category}_rid"] = array($insertRecipeId, PDO::PARAM_INT);
                    $insertRecipeCategoriesParams[":c${i_category}_cid"] = array($category['categoryId'], PDO::PARAM_INT);
                    $i_category++;
                    $numRecipeCategoriesToInsert++;
                }
            }

            if(strlen($insertRecipeCategoriesValuesSql) > 0) {
                $insertRecipeCategoriesSql = 'insert into RecipeCategories (recipeId, categoryId) values ' . $insertRecipeCategoriesValuesSql;
                // extend and log sql query
                if($logdb || $logfile || $logscreen) {
                    $query = extendSqlQuery($insertRecipeCategoriesSql, $insertRecipeCategoriesParams);
                    $sqlQueries[] = $query;
                }

                $insertRecipeCategoriesStmt = $database->prepare($insertRecipeCategoriesSql);
                foreach($insertRecipeCategoriesParams as $index => $param)
                    $insertRecipeCategoriesStmt->bindValue($index, $param[0], $param[1]);
                $insertRecipeCategoriesStmt->execute();
                $numAffectedRows = $insertRecipeCategoriesStmt->rowCount();

                if($numRecipeCategoriesToInsert != $numAffectedRows && $rollbackOnCategoryErrors == true) {
                    $resultCode = 216000;
                    $resultMessage = "the number of inserted recipe category relations ($numAffectedRows) is not equal to the number of provided recipe category relations ($numRecipeCategoriesToInsert)";
                    throw new Exception($resultMessage);
                }
            }

        } // if categories

        // if tags are provided
        if(isset($recipe['tags']) && is_array($recipe['tags'])) {

            $tags = $recipe['tags'];
            // iterate through tags and create new or update existing tags
            foreach($tags as $tag) {

                // if no tag id was set but a tag name was set, insert a new tag and return the new tag id
                if((!isset($tag['tagId']) || $tag['tagId'] == 0) && isset($tag['name']) && strlen($tag['name']) > 0) {
                    $insertTagSql = 'insert into Tags (name, lastActiveDateTime) values (:tn, now()) " .
                        "on duplicate key update lastActiveDateTime = now()';
                    $insertTagParams = array(
                        ':tn' => array($tag['name'], PDO::PARAM_STR)
                    );

                    // extend and log sql query
                    if($logdb || $logfile || $logscreen) {
                        $query = extendSqlQuery($insertTagSql, $insertTagParams);
                        $sqlQueries[] = $query;
                    }

                    $insertTagStmt = $database->prepare($insertTagSql);
                    foreach($insertTagParams as $index => $param)
                        $insertTagStmt->bindValue($index, $param[0], $param[1]);
                    $insertTagStmt->execute();
                    $insertTagId = $database->lastInsertId();
                    $numAffectedRows = $insertTagStmt->rowCount();

                    if($numAffectedRows == 0 && $rollbackOnTagErrors == true) {
                        $resultCode = 217000;
                        $resultMessage = "could not insert tag with name \"" . $tag['name'] . "\" into database";
                        throw new Exception($resultMessage);
                    }

                    // if an new tag was inserted and insertTagId is set
                    if(isset($insertTagId) && $insertTagId > 0)
                        $tag['tagId'] = $insertTagId;

                    // else: if an existing tag was updated and insertTagId is unset, select the tag id
                    else {
                        $selectTagSql = 'select tagId from Tags where name = :tn limit 1';
                        $selectTagParams = array(
                            ':tn' => array($tag['name'], PDO::PARAM_STR)
                        );

                        // extend and log sql query
                        if($logdb || $logfile || $logscreen) {
                            $query = extendSqlQuery($selectTagSql, $selectTagParams);
                            $sqlQueries[] = $query;
                        }

                        $selectTagStmt = $database->prepare($selectTagSql);
                        foreach($selectTagParams as $index => $param)
                            $selectTagStmt->bindValue($index, $param[0], $param[1]);
                        $selectTagStmt->execute();
                        $selectTagRows = $selectTagStmt->fetchAll(PDO::FETCH_ASSOC);

                        if(count($selectTagRows) == 0 && $rollbackOnTagErrors == true) {
                            $resultCode = 218000;
                            $resultMessage = "could not select tag with name \"" . $tag['name'] . "\" from database";
                            throw new Exception($resultMessage);
                        }
                    }
                }
                // if no tag id and no tag name was set, return an error
                else if((!isset($tag['tagId']) || $tag['tagId'] == 0) && (!isset($tag['name']) || strlen($tag['name']) == 0) && $rollbackOnTagErrors) {
                    $resultCode = 219000;
                    $resultMessage = "the tag name key is not present within the submitted recipe but is required when no tagId was specified";
                    throw new Exception($resultMessage);
                }

            } // foreach tag (create tag)

            // insert recipe tag relations
            $insertRecipeTagsValuesSql = '';
            $insertRecipeTagsParams = array();
            $i_tag = 1;
            $numRecipeTagsToInsert = 0;

            foreach($tags as $tag) {
                if(isset($tag['tagId']) && $tag['tagId'] > 0) {
                    if(strlen($insertRecipeTagsValuesSql) > 0) $insertRecipeTagsValuesSql .= ', ';
                    $insertRecipeTagsValuesSql .= "(:t${i_tag}_rid, :t${i_tag}_tid)";
                    $insertRecipeTagsParams[":t${i_tag}_rid"] = array($insertRecipeId, PDO::PARAM_INT);
                    $insertRecipeTagsParams[":t${i_tag}_tid"] = array($tag['tagId'], PDO::PARAM_INT);
                    $i_tag++;
                    $numRecipeTagsToInsert++;
                }
            }

            if(strlen($insertRecipeTagsValuesSql) > 0) {
                $insertRecipeTagsSql = 'insert into RecipeTags (recipeId, tagId) values ' . $insertRecipeTagsValuesSql;
                // extend and log sql query
                if($logdb || $logfile || $logscreen) {
                    $query = extendSqlQuery($insertRecipeTagsSql, $insertRecipeTagsParams);
                    $sqlQueries[] = $query;
                }

                $insertRecipeTagsStmt = $database->prepare($insertRecipeTagsSql);
                foreach($insertRecipeTagsParams as $index => $param)
                    $insertRecipeTagsStmt->bindValue($index, $param[0], $param[1]);
                $insertRecipeTagsStmt->execute();
                $numAffectedRows = $insertRecipeTagsStmt->rowCount();

                if($numRecipeTagsToInsert != $numAffectedRows && $rollbackOnTagErrors == true) {
                    $resultCode = 220000;
                    $resultMessage = "the number of inserted recipe tag relations ($numAffectedRows) is not equal to the number of provided recipe tag relations ($numRecipeTagsToInsert)";
                    throw new Exception($resultMessage);
                }
            }

        } // if tags

        // if recipe steps are provided
        if(isset($recipe['recipeSteps']) && is_array($recipe['recipeSteps'])) {
            
            $recipeSteps = $recipe['recipeSteps'];
            // iterate through recipe steps
            foreach($recipeSteps as $recipeStep) {

                $numRecipeStepStringValues = 0;
                $stepNumber = $recipeStep['stepNumber'];
                $insertRecipeStepStringsValuesSql = '';
                $insertRecipeStepStringsParams = array();

                if(isset($recipeStep['stepDescription'])) {
                    $insertRecipeStepStringsValuesSql = "(:rs${stepNumber}_d_ov, :rs${stepNumber}_d_ol)";
                    $insertRecipeStepStringsParams[":rs${stepNumber}_d_ov"] = array($recipeStep['stepDescription'], PDO::PARAM_STR);
                    $insertRecipeStepStringsParams[":rs${stepNumber}_d_ol"] = array($languageId, PDO::PARAM_INT);
                    $numRecipeStepStringValues++;
                }
                else {
                    $resultCode = 221000 + $stepNumber;
                    $resultMessage = "the step description key for recipe step number $stepNumber is not present within the submitted recipe";
                    throw new Exception($resultMessage);
                }
                if(isset($recipeStep['stepTitle'])) {
                    if(strlen($insertRecipeStepStringsValuesSql) > 0) $insertRecipeStepStringsValuesSql .= ', ';
                    $insertRecipeStepStringsValuesSql .= "(:rs${stepNumber}_t_ov, :rs${stepNumber}_t_ol)";
                    $insertRecipeStepStringsParams[":rs${stepNumber}_t_ov"] = array($recipeStep['stepTitle'], PDO::PARAM_STR);
                    $insertRecipeStepStringsParams[":rs${stepNumber}_t_ol"] = array($languageId, PDO::PARAM_INT);
                    $numRecipeStepStringValues++;
                }

                // insert recipe step strings
                $insertRecipeStepStringsSql = 'insert into Strings (originalValue, originalLanguageId) values ' . $insertRecipeStepStringsValuesSql;
                // extend and log sql query
                if($logdb || $logfile || $logscreen) {
                    $query = extendSqlQuery($insertRecipeStepStringsSql, $insertRecipeStepStringsParams);
                    $sqlQueries[] = $query;
                }
                
                $insertRecipeStepStringsStmt = $database->prepare($insertRecipeStepStringsSql);
                foreach($insertRecipeStepStringsParams as $index => $param)
                    $insertRecipeStepStringsStmt->bindValue($index, $param[0], $param[1]);
                $insertRecipeStepStringsStmt->execute();
                $insertRecipeStepStringsId = $database->lastInsertId();
                $numAffectedRows = $insertRecipeStepStringsStmt->rowCount();

                if($numRecipeStepStringValues != $numAffectedRows) {
                    $resultCode = 222000 + $stepNumber;
                    $resultMessage = "the number of inserted recipe step strings ($numAffectedRows) is not equal to the number of provided strings ($numRecipeStepStringValues)";
                    throw new Exception($resultMessage);
                }

                $ido = 0;

                // add inserted string ids to the recipe map
                $recipeStep['stepDescriptionStringId'] = $insertRecipeStepStringsId + $ido++;
                if(isset($recipeStep['stepTitle']))
                    $recipeStep['stepTitleStringId'] = $insertRecipeStepStringsId + $ido++;

                // build dynamic recipe step insert sql string
                // insert recipe step
                $insertRecipeStepParams = array();

                // fixed columns and values
                $insertRecipeStepColumnsSql = 'recipeId, stepNumber, descriptionStringId';
                $insertRecipeStepValuesSql = ':rid, :sn, :dsid';
                $insertRecipeStepParams[':rid'] = array($insertRecipeId, PDO::PARAM_INT);
                $insertRecipeStepParams[':sn'] = array($recipeStep['stepNumber'], PDO::PARAM_INT);
                $insertRecipeStepParams[':dsid'] = array($recipeStep['stepDescriptionStringId'], PDO::PARAM_INT);

                // dynamic columns and values
                if(isset($recipeStep['stepTitleStringId'])) {
                    $insertRecipeStepColumnsSql .= ', titleStringId';
                    $insertRecipeStepValuesSql .= ', :tsid';
                    $insertRecipeStepParams[':tsid'] = array($recipeStep['stepTitleStringId'], PDO::PARAM_INT);
                }

                $insertRecipeStepSql = "insert into RecipeSteps ($insertRecipeStepColumnsSql) values ($insertRecipeStepValuesSql)";
                // extend and log sql query
                if($logdb || $logfile || $logscreen) {
                    $query = extendSqlQuery($insertRecipeStepSql, $insertRecipeStepParams);
                    $sqlQueries[] = $query;
                }

                $insertRecipeStepStmt = $database->prepare($insertRecipeStepSql);
                foreach($insertRecipeStepParams as $index => $param)
                    $insertRecipeStepStmt->bindValue($index, $param[0], $param[1]);
                $insertRecipeStepStmt->execute();
                $insertRecipeStepId = $database->lastInsertId();
                $numAffectedRows = $insertRecipeStepStmt->rowCount();
    
                if($numAffectedRows == 0) {
                    $resultCode = 223000 + $stepNumber;
                    $resultMessage = "could not insert recipe step with number $stepNumber into the database";
                    throw new Exception($resultMessage);
                }
    
                // add inserted recipe step id to the recipe map
                $recipeStep['recipeStepId'] = $insertRecipeStepId;

                // if recipe step ingredients are provided
                if(isset($recipeStep['recipeStepIngredients']) && is_array($recipeStep['recipeStepIngredients'])) {
                    
                    $recipeStepIngredients = $recipeStep['recipeStepIngredients'];
                    $i = 1;

                    // iterate through recipe steps
                    foreach($recipeStepIngredients as $recipeStepIngredient) {
                        
                        // only insert ingredient strings if there is no ingredient id set
                        if(!isset($recipeStepIngredient['ingredientId']) || $recipeStepIngredient['ingredientId'] == 0) {

                            $numIngredientStringValues = 0;
                            $insertIngredientStringsValuesSql = '';
                            $insertIngredientStringsParams = array();

                            if(isset($recipeStepIngredient['ingredientName'])) {
                                $insertIngredientStringsValuesSql = "(:rs${stepNumber}_i${i}_n_ov, :rs${stepNumber}_i${i}_n_ol)";
                                $insertIngredientStringsParams[":rs${stepNumber}_i${i}_n_ov"] = array($recipeStepIngredient['ingredientName'], PDO::PARAM_STR);
                                $insertIngredientStringsParams[":rs${stepNumber}_i${i}_n_ol"] = array($languageId, PDO::PARAM_INT);
                                $numIngredientStringValues++;
                            }
                            else {
                                $resultCode = 224000 + $stepNumber;
                                $resultMessage = "an ingredient name key in recipe step number $stepNumber is not present within the submitted recipe";
                                throw new Exception($resultMessage);
                            }

                            if(isset($recipeStepIngredient['ingredientDescription'])) {
                                if(strlen($insertIngredientStringsValuesSql) > 0) $insertIngredientStringsValuesSql .= ', ';
                                $insertIngredientStringsValuesSql .=  "(:rs${stepNumber}_i${i}_d_ov, :rs${stepNumber}_i${i}_d_ol)";
                                $insertIngredientStringsParams[":rs${stepNumber}_i${i}_d_ov"] = array($recipeStepIngredient['stepDescription'], PDO::PARAM_STR);
                                $insertIngredientStringsParams[":rs${stepNumber}_i${i}_d_ol"] = array($languageId, PDO::PARAM_INT);
                                $numIngredientStringValues++;
                            }

                            if($numIngredientStringValues > 0) {
                                $insertIngredientStringsSql = 'insert into Strings (originalValue, originalLanguageId) values ' . $insertIngredientStringsValuesSql;
                                // extend and log sql query
                                if($logdb || $logfile || $logscreen) {
                                    $query = extendSqlQuery($insertIngredientStringsSql, $insertIngredientStringsParams);
                                    $sqlQueries[] = $query;
                                }
                                
                                $insertIngredientStringsStmt = $database->prepare($insertIngredientStringsSql);
                                foreach($insertIngredientStringsParams as $index => $param)
                                    $insertIngredientStringsStmt->bindValue($index, $param[0], $param[1]);
                                $insertIngredientStringsStmt->execute();
                                $insertIngredientStringsId = $database->lastInsertId();
                                $numAffectedRows = $insertIngredientStringsStmt->rowCount();

                                if($numIngredientStringValues != $numAffectedRows) {
                                    $resultCode = 225000 + $stepNumber;
                                    $resultMessage = "the number of inserted ingredient strings ($numAffectedRows) is not equal to the number of provided strings ($numIngredientStringValues)";
                                    throw new Exception($resultMessage);
                                }

                                $ido = 0;

                                // add inserted string ids to the recipe map
                                $recipeStepIngredient['ingredientNameStringId'] = $insertIngredientStringsId + $ido++;
                                if(isset($recipeStepIngredient['ingredientDescription']))
                                    $recipeStepIngredient['ingredientDescriptionStringId'] = $insertIngredientStringsId + $ido++;

                                $insertIngredientParams = array();

                                $insertIngredientColumnsSql = 'nameStringId';
                                if(isset($recipeStepIngredient['ingredientDescription']))
                                    $insertIngredientColumnsSql .= ', descriptionStringId';

                                $insertIngredientValuesSql = ':nsid';
                                $insertIngredientParams[':nsid'] = array($recipeStepIngredient['ingredientNameStringId'], PDO::PARAM_INT);
                                if(isset($recipeStepIngredient['ingredientDescription'])) {
                                    $insertIngredientValuesSql .= ', :dsid';
                                    $insertIngredientParams[':dsid'] = array($recipeStepIngredient['ingredientDescriptionStringId'], PDO::PARAM_INT);
                                }

                                $insertIngredientSql = "insert into Ingredients ($insertIngredientColumnsSql) values ($insertIngredientValuesSql)";
                                // extend and log sql query
                                if($logdb || $logfile || $logscreen) {
                                    $query = extendSqlQuery($insertIngredientSql, $insertIngredientParams);
                                    $sqlQueries[] = $query;
                                }

                                $insertIngredientStmt = $database->prepare($insertIngredientSql);
                                foreach($insertIngredientParams as $index => $param)
                                    $insertIngredientStmt->bindValue($index, $param[0], $param[1]);
                                $insertIngredientStmt->execute();
                                $insertIngredientId = $database->lastInsertId();
                                $numAffectedRows = $insertIngredientStmt->rowCount();

                                if($numAffectedRows == 0) {
                                    $resultCode = 226000 + $stepNumber;
                                    $resultMessage = "could not insert ingredient \"" . $recipeStepIngredient['ingredientName'] . "\" into the database";
                                    throw new Exception($resultMessage);
                                }

                                // add inserted ingredient id to the recipe map
                                $recipeStepIngredient['ingredientId'] = $insertIngredientId;
                            }
                        }

                        // if there is no unit type id or unit type name set, abort with an error
                        // if((!isset($recipeStepIngredient['unitTypeId']) || $recipeStepIngredient['unitTypeId'] == 0) && 
                        //     (!isset($recipeStepIngredient['unitTypeName']) || strlen($recipeStepIngredient['unitTypeName']) == 0))
                        // {
                        //     $resultCode = 20000 + $stepNumber;
                        //     $resultMessage = "no unit type specified in recipe step " . $stepNumber;
                        //     throw new Exception($resultMessage);
                        // }
                        // if there is no unit type id set but a unit type name set, insert new unit type string and unit type
                        if((!isset($recipeStepIngredient['unitTypeId']) || $recipeStepIngredient['unitTypeId'] == 0) && 
                            isset($recipeStepIngredient['unitTypeName']) && strlen($recipeStepIngredient['unitTypeName']) > 0)
                        {
                            $insertUnitTypeStringsValuesSql = '';
                            $insertUnitTypeStringsParams = array();

                            $insertUnitTypeStringsValuesSql = "(:rs${stepNumber}_u${i}_n_ov, :rs${stepNumber}_u${i}_n_ol)";
                            $insertUnitTypeStringsParams[":rs${stepNumber}_u${i}_n_ov"] = array($recipeStepIngredient['unitTypeName'], PDO::PARAM_STR);
                            $insertUnitTypeStringsParams[":rs${stepNumber}_u${i}_n_ol"] = array($languageId, PDO::PARAM_INT);

                            $insertUnitTypeStringsSql = 'insert into Strings (originalValue, originalLanguageId) values ' . $insertUnitTypeStringsValuesSql;
                            // extend and log sql query
                            if($logdb || $logfile || $logscreen) {
                                $query = extendSqlQuery($insertUnitTypeStringsSql, $insertUnitTypeStringsParams);
                                $sqlQueries[] = $query;
                            }
                            
                            $insertUnitTypeStringsStmt = $database->prepare($insertUnitTypeStringsSql);
                            foreach($insertUnitTypeStringsParams as $index => $param)
                                $insertUnitTypeStringsStmt->bindValue($index, $param[0], $param[1]);
                            $insertUnitTypeStringsStmt->execute();
                            $insertUnitTypeStringsId = $database->lastInsertId();
                            $numAffectedRows = $insertUnitTypeStringsStmt->rowCount();

                            if($numAffectedRows == 0) {
                                $resultCode = 227000 + $stepNumber;
                                $resultMessage = "could not insert unit type name string \"" . $recipeStepIngredient['unitTypeName'] . "\" into the database";
                                throw new Exception($resultMessage);
                            }

                            // add inserted string ids to the recipe map
                            $recipeStepIngredient['unitTypeNameStringId'] = $insertUnitTypeStringsId;

                            // insert unit type
                            $insertUnitTypeParams = array();

                            $insertUnitTypeColumnsSql = 'nameStringId';
                            if(isset($recipeStepIngredient['unitTypeAbbreviation']))
                                $insertUnitTypeColumnsSql .= ', abbreviation';
                            if(isset($recipeStepIngredient['unitTypeClass']))
                                $insertUnitTypeColumnsSql .= ', unitClass';

                            $insertUnitTypeValuesSql = ':utnid';
                            $insertUnitTypeParams[':utnid'] = array($recipeStepIngredient['unitTypeNameStringId'], PDO::PARAM_INT);
                            if(isset($recipeStepIngredient['unitTypeAbbreviation'])) {
                                $insertUnitTypeValuesSql .= ', :uta';
                                $insertUnitTypeParams[':uta'] = array($recipeStepIngredient['unitTypeAbbreviation'], PDO::PARAM_STR);
                            }
                            if(isset($recipeStepIngredient['unitTypeClass'])) {
                                $insertUnitTypeValuesSql .= ', :utc';
                                $insertUnitTypeParams[':utc'] = array($recipeStepIngredient['unitTypeClass'], PDO::PARAM_INT);
                            }

                            $insertUnitTypeSql = "insert into UnitTypes ($insertUnitTypeColumnsSql) values ($insertUnitTypeValuesSql)";
                            // extend and log sql query
                            if($logdb || $logfile || $logscreen) {
                                $query = extendSqlQuery($insertUnitTypeSql, $insertUnitTypeParams);
                                $sqlQueries[] = $query;
                            }

                            $insertUnitTypeStmt = $database->prepare($insertUnitTypeSql);
                            foreach($insertUnitTypeParams as $index => $param)
                                $insertUnitTypeStmt->bindValue($index, $param[0], $param[1]);
                            $insertUnitTypeStmt->execute();
                            $insertUnitTypeId = $database->lastInsertId();
                            $numAffectedRows = $insertUnitTypeStmt->rowCount();

                            if($numAffectedRows == 0) {
                                $resultCode = 228000 + $stepNumber;
                                $resultMessage = "could not insert unit type \"" . $recipeStepIngredient['unitTypeName'] . "\" into the database";
                                throw new Exception($resultMessage);
                            }
                            
                            // add inserted ingredient id to the recipe map
                            $recipeStepIngredient['unitTypeId'] = $insertUnitTypeId;
                        }
                        // if there is no unit type id and no unit type name set but the unit type abbreviation, look for the unit type and return its id
                        else if((!isset($recipeStepIngredient['unitTypeId']) || $recipeStepIngredient['unitTypeId'] == 0) && 
                            isset($recipeStepIngredient['unitTypeAbbreviation']) && strlen($recipeStepIngredient['unitTypeAbbreviation']) > 0)
                        {
                            $selectUnitTypeSql = 'select unitTypeId from UnitTypes where abbreviation = :uta';
                            $selectUnitTypeParams = array(
                                ':uta' => array($recipeStepIngredient['unitTypeAbbreviation'], PDO::PARAM_STR)
                            );

                            // extend and log sql query
                            if($logdb || $logfile || $logscreen) {
                                $query = extendSqlQuery($selectUnitTypeSql, $selectUnitTypeParams);
                                $sqlQueries[] = $query;
                            }

                            $selectUnitTypeStmt = $database->prepare($selectUnitTypeSql);
                            foreach($selectUnitTypeParams as $index => $param)
                                $selectUnitTypeStmt->bindValue($index, $param[0], $param[1]);
                            $selectUnitTypeStmt->execute();
                            $selectUnitTypeRows = $selectUnitTypeStmt->fetchAll(PDO::FETCH_ASSOC);

                            if(count($selectUnitTypeRows) == 0) {
                                $resultCode = 229000 + $stepNumber;
                                $resultMessage = "could not find unit type with abbreviation \"" . $recipeStepIngredient['unitTypeAbbreviation'] . "\" in the database";
                                throw new Exception($resultMessage);
                            }
                            
                            // add selected ingredient id to the recipe map
                            $recipeStepIngredient['unitTypeId'] = $selectUnitTypeRows[0]['unitTypeId'];
                        }

                        // build dynamic recipe step ingredient insert sql string
                        // insert recipe step ingredient
                        $insertRecipeStepIngredientParams = array();

                        // fixed columns and values
                        $insertRecipeStepIngredientColumnsSql = 'recipeStepId, ingredientId';
                        $insertRecipeStepIngredientValuesSql = ':rsid, :iid';
                        $insertRecipeStepIngredientParams[':rsid'] = array($recipeStep['recipeStepId'], PDO::PARAM_INT);
                        $insertRecipeStepIngredientParams[':iid'] = array($recipeStepIngredient['ingredientId'], PDO::PARAM_INT);

                        // dynamic columns and values
                        if(isset($recipeStepIngredient['ingredientAmount'])) {
                            $insertRecipeStepIngredientColumnsSql .= ', amount';
                            $insertRecipeStepIngredientValuesSql .= ', :amnt';
                            $insertRecipeStepIngredientParams[':amnt'] = array(round($recipeStepIngredient['ingredientAmount'], 6), PDO::PARAM_STR);
                        }
                        if(isset($recipeStepIngredient['unitTypeId']) && $recipeStepIngredient['unitTypeId'] != 0) {
                            $insertRecipeStepIngredientColumnsSql .= ', unitTypeId';
                            $insertRecipeStepIngredientValuesSql .= ', :utid';
                            $insertRecipeStepIngredientParams[':utid'] = array($recipeStepIngredient['unitTypeId'], PDO::PARAM_INT);
                        }
                        if(isset($recipeStepIngredient['customUnit']) && strlen($recipeStepIngredient['customUnit']) > 0) {
                            $insertRecipeStepIngredientColumnsSql .= ', customUnit';
                            $insertRecipeStepIngredientValuesSql .= ', :cstm';
                            $insertRecipeStepIngredientParams[':cstm'] = array($recipeStepIngredient['customUnit'], PDO::PARAM_STR);
                        }

                        $insertRecipeStepIngredientSql = "insert into RecipeStepIngredients ($insertRecipeStepIngredientColumnsSql) values ($insertRecipeStepIngredientValuesSql)";
                        // extend and log sql query
                        if($logdb || $logfile || $logscreen) {
                            $query = extendSqlQuery($insertRecipeStepIngredientSql, $insertRecipeStepIngredientParams);
                            $sqlQueries[] = $query;
                        }

                        $insertRecipeStepIngredientStmt = $database->prepare($insertRecipeStepIngredientSql);
                        foreach($insertRecipeStepIngredientParams as $index => $param)
                            $insertRecipeStepIngredientStmt->bindValue($index, $param[0], $param[1]);
                        $insertRecipeStepIngredientStmt->execute();
                        $numAffectedRows = $insertRecipeStepIngredientStmt->rowCount();
            
                        if($numAffectedRows == 0) {
                            $resultCode = 230000 + $stepNumber;
                            $resultMessage = "could not insert recipe step ingredient with ingredient id $insertIngredientId into the database";
                            throw new Exception($resultMessage);
                        }

                        $i++;

                    } // foreach recipeStepIngredient

                } // if recipeStepIngredients

            } // foreach recipeStep

        } // if recipeSteps

        // commit changes to database and end transaction block
        $database->commit();

        // only return a valid recipe id if the database commit succeeded
        $recipeIdToReturn = $insertRecipeId;
    }
    catch(PDOException $e) {
        // rollback uncommited changes
        if($database->inTransaction())
            $database->rollBack();
        $resultCode = 300000;
        $resultMessage = 'database error: ' . $e->getCode() . ': ' . $e->getMessage();
    }
    catch(Exception $e) {
        // rollback uncommited changes
        if($database != null && $database->inTransaction())
            $database->rollBack();
    }

    $result = array(
        'resultCode' => $resultCode,
        'resultMessage' => $resultMessage,
        'recipeId' => $recipeIdToReturn,
    );

    if($resultCode > 0)
        $sqlQueries[] = $resultCode . ': ' . $resultMessage;

    // encode and return json
    echo json_encode($result, JSON_PRETTY_PRINT);
?>
