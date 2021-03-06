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
    $ofuserId = $userId;
    $selectedTypes = 65519;//65535 ^ 16;
    $onlyOwnMessages = false;
    $conflateRelatedMessages = true;
    $startDate = null;

    if(isset($_getpost['ofuserId']))
        $ofuserId = $_getpost['ofuserId'];
    if(isset($_getpost['selectedTypes']) && $_getpost['selectedTypes'] > 0)
        $selectedTypes = $_getpost['selectedTypes'];
    if(isset($_getpost['onlyOwnMessages']) && $_getpost['onlyOwnMessages'] == 'true')
        $onlyOwnMessages = true;
    if(isset($_getpost['conflateRelatedMessages']) && $_getpost['conflateRelatedMessages'] == 'false')
        $conflateRelatedMessages = false;
    if(isset($_getpost['startDate'])) {
        $inputDate = $_getpost['startDate'];
        $dateTimePattern = "/([0-9]{4})([0-9]{2})([0-9]{2})([0-9]{0,2})([0-9]{0,2})([0-9]{0,2})/";
        $matches = array();
        if(preg_match($dateTimePattern, $inputDate, $matches) == 1) {
            $parsedDate = sprintf("%04d-%02d-%02d %02d:%02d:%02d", $matches[1], $matches[2], $matches[3], 
                isset($matches[4]) ? $matches[4] : 23, isset($matches[5]) ? $matches[5] : 59, isset($matches[6]) ? $matches[6] : 59);
            $startDate = $parsedDate;
        }
    }

    $snacks = array();
    $sqlQueries = array();
    
    try {
        // select snacks for the feed
        $partSelectSql = array();
        $partSelectParams = array();

        /************************************************************************************************************************************************************************************ */
        // part select followed users
        $partSelectFollowedUsersSql = 
            "select distinct 'followedUser' as 'type', concat(user.userName, ' is now following @', followUser.userName) as message, " .
            "uuf.userId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "uuf.followUserId as object1Id, followUser.userName as object1Name, followUserImage.imageFileName as object1ImageFileName, " .
            "null as object2Id, uuf.followedDateTime as performedDateTime " .
            "from UserUserFollows uuf " .
            "left join Users user on user.userId = uuf.userId ";

        if(isset($ofuserId)) {
            $partSelectFollowedUsersSql .= "left join UserUserFollows uuf2 on (uuf2.followUserId = uuf.userId and uuf2.userId = :fu_uid_1) "; // join to receive followed users of followees
            $partSelectParams[1][':fu_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }

        $partSelectFollowedUsersSql .= 
            "left join Users followUser on followUser.userId = uuf.followUserId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "left join Images followUserImage on followUserImage.imageId = followUser.profileImageId " . 
            "where uuf.followedDateTime is not null and uuf.followUserId != :fu_uid_2 "; // make sure to hide messages saying someone is now following ofuserId
        $partSelectParams[1][':fu_uid_2'] = array($ofuserId, PDO::PARAM_INT);

        if(isset($startDate)) {
            $partSelectFollowedUsersSql .= "and uuf.followedDateTime < :fu_sd ";
            $partSelectParams[1][':fu_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectFollowedUsersSql .= "and (uuf.userId = :fu_uid_3 "; // where ofuserId is the performer
            $partSelectParams[1][':fu_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectFollowedUsersSql .= "or uuf2.userId = :fu_uid_4"; // where a followee of ofuserId is the performer
                $partSelectParams[1][':fu_uid_4'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectFollowedUsersSql .= ") ";
        }

        $partSelectSql[1] = $partSelectFollowedUsersSql;

        /************************************************************************************************************************************************************************************ */
        // part select followed tags
        $partSelectFollowedTagsSql = 
            "select 'followedTag' as 'type', concat(user.userName, ' is now following #', followTag.name) as message, " .
            "utf.userId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "utf.followTagId as object1Id, followTag.name as object1Name, (" .
                "select tagImage.imageFileName " .
                "from TagDetails tagDetail " .
                "left join Images tagImage on tagImage.imageId = tagDetail.lastImageId " .
                "where tagDetail.tagId = utf.followTagId " .
                "limit 1" .
            ") as object1ImageFileName, null as object2Id, " .
            "utf.followedDateTime as performedDateTime " .
            "from UserTagFollows utf ";

        if(isset($ofuserId)) {
            $partSelectFollowedTagsSql .= "left join UserUserFollows uuf on (uuf.followUserId = utf.userId and uuf.userId = :ft_uid_1) "; // join to receive followed tags of followees
            $partSelectParams[2][':ft_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }

        $partSelectFollowedTagsSql .= 
            "left join Users user on user.userId = utf.userId " .
            "left join Tags followTag on followTag.tagId = utf.followTagId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "where utf.followedDateTime is not null ";

        if(isset($startDate)) {
            $partSelectFollowedTagsSql .= "and utf.followedDateTime < :ft_sd ";
            $partSelectParams[2][':ft_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectFollowedTagsSql .= "and (utf.userId = :ft_uid_2 "; // where ofuserId is the performer
            $partSelectParams[2][':ft_uid_2'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectFollowedTagsSql .= "or uuf.userId = :ft_uid_3"; // where a followee of ofuserId is the performer
                $partSelectParams[2][':ft_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectFollowedTagsSql .= ") ";
        }

        $partSelectSql[2] = $partSelectFollowedTagsSql;

        /************************************************************************************************************************************************************************************ */
        // part select followed collections
        $partSelectFollowedCollectionsSql = 
            "select 'followedCollection' as 'type', concat(user.userName, ' is now following ', collectionTitle.originalValue) as message, " .
            "ucf.userId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "ucf.followCollectionId as object1Id, collectionTitle.originalValue as object1Name, null as object1ImageFileName, " .
            "null as object2Id, ucf.followedDateTime as performedDateTime " .
            "from UserCollectionFollows ucf ";

        if(isset($ofuserId)) {
            $partSelectFollowedCollectionsSql .= "left join UserUserFollows uuf on (uuf.followUserId = ucf.userId and uuf.userId = :fc_uid_1) "; // join to receive followed collections of followees
            $partSelectParams[4][':fc_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }

        $partSelectFollowedCollectionsSql .= 
            "left join Users user on user.userId = ucf.userId " .
            "left join Collections followCollection on ucf.followCollectionId = followCollection.collectionId " .
            "left join Strings collectionTitle on followCollection.titleStringId = collectionTitle.stringId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "where ucf.followedDateTime is not null ";

        if(isset($startDate)) {
            $partSelectFollowedCollectionsSql .= "and ucf.followedDateTime < :fc_sd ";
            $partSelectParams[4][':fc_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectFollowedCollectionsSql .= "and (ucf.userId = :fc_uid_2 "; // where ofuserId is the performer
            $partSelectParams[4][':fc_uid_2'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectFollowedCollectionsSql .= "or uuf.userId = :fc_uid_3"; // where a followee of ofuserId is the performer
                $partSelectParams[4][':fc_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectFollowedCollectionsSql .= ") ";
        }

        $partSelectSql[4] = $partSelectFollowedCollectionsSql;

        /************************************************************************************************************************************************************************************ */
        // part select created recipes
        // $partSelectCreatedRecipesSql = 
        //     "select 'createdRecipe' as 'type', concat(user.userName, ' created recipe ', recipeTitle.originalValue) as message, " .
        //     "recipe.creatorId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
        //     "recipe.recipeId as object1Id, recipeTitle.originalValue as object1Name, (" .
        //         "select image.imageFileName " .
        //         "from Images image " .
        //         "left join RecipeImages recipeImage on recipeImage.imageId = image.imageId " .
        //         "left join Users user on user.userId = image.creatorId " .
        //         "where recipeImage.recipeId = recipe.recipeId and (image.creatorId = user.userId or image.creatorId != user.userId or image.creatorId is null) " . // prefer the user's image if he added one
        //         "order by image.rating desc, rand() limit 1" . // second order random if multiple best ratings
        //     ") as object1ImageFileName, null as object2Id, recipe.createdDateTime as performedDateTime " .
        //     "from Recipes recipe ";

        $partSelectCreatedRecipesSql = 
            "select 'createdRecipe' as 'type', concat(user.userName, ' created recipe ', recipeTitle.originalValue) as message, " .
            "recipe.creatorId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "recipe.recipeId as object1Id, recipeTitle.originalValue as object1Name, mainImage.imageFileName as object1ImageFileName, " .
            "null as object2Id, recipe.createdDateTime as performedDateTime " .
            "from Recipes recipe ";

        if(isset($ofuserId)) {
            $partSelectCreatedRecipesSql .= "left join UserUserFollows uuf on (uuf.followUserId = recipe.creatorId and uuf.userId = :ar_uid_1) "; // join to receive created recipes of followees
            $partSelectParams[8][':ar_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }
        
        $partSelectCreatedRecipesSql .= 
            "left join Strings recipeTitle on recipeTitle.stringId = recipe.titleStringId " .
            "left join Users user on user.userId = recipe.creatorId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "left join Images mainImage on mainImage.imageId = recipe.mainImageId " .
            "where recipe.createdDateTime is not null ";

        if(isset($startDate)) {
            $partSelectCreatedRecipesSql .= "and recipe.createdDateTime < :ar_sd ";
            $partSelectParams[8][':ar_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectCreatedRecipesSql .= "and (recipe.creatorId = :ar_uid_2 "; // where ofuserId is the performer
            $partSelectParams[8][':ar_uid_2'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectCreatedRecipesSql .= "or uuf.userId = :ar_uid_3"; // where a followee of ofuserId is the performer
                $partSelectParams[8][':ar_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectCreatedRecipesSql .= ") ";
        }

        $partSelectSql[8] = $partSelectCreatedRecipesSql;

        /************************************************************************************************************************************************************************************ */
        // part select modified recipes
        $partSelectModifiedRecipesSql = 
            "select 'modifiedRecipe' as 'type', concat(user.userName, ' modified recipe ', recipeTitle.originalValue) as message, " .
            "recipe.modifierId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "recipe.recipeId as object1Id, recipeTitle.originalValue as object1Name, null as object1ImageFileName, " .
            "null as object2Id, recipe.lastModifiedDateTime as performedDateTime " .
            "from Recipes recipe ";

        if(isset($ofuserId)) {
            $partSelectModifiedRecipesSql .= "left join UserUserFollows uuf on (uuf.followUserId = recipe.modifierId and uuf.userId = :mr_uid_1) "; // join to receive modified recipes of followees
            $partSelectParams[16][':mr_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }

        $partSelectModifiedRecipesSql .= 
            "left join Strings recipeTitle on recipeTitle.stringId = recipe.titleStringId " .
            "left join Users user on user.userId = recipe.modifierId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "where recipe.lastModifiedDateTime is not null ";

        if(isset($startDate)) {
            $partSelectModifiedRecipesSql .= "and recipe.lastModifiedDateTime < :mr_sd ";
            $partSelectParams[16][':mr_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectModifiedRecipesSql .= "and (recipe.modifierId = :mr_uid_2 "; // where ofuserId is the performer
            $partSelectParams[16][':mr_uid_2'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectModifiedRecipesSql .= "or uuf.userId = :mr_uid_3"; // where a followee of ofuserId is the performer
                $partSelectParams[16][':mr_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectModifiedRecipesSql .= ") ";
        }

        $partSelectSql[16] = $partSelectModifiedRecipesSql;

        /************************************************************************************************************************************************************************************ */
        // part select cooked recipes
        $partSelectCookedRecipesSql = 
            "select 'cookedRecipe' as 'type', concat(user.userName, ' cooked recipe ', recipeTitle.originalValue) as message, " .
            "cooking.cookId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "cooking.recipeId as object1Id, recipeTitle.originalValue as object1Name, (" .
                "select image.imageFileName " .
                "from Recipes imagedRecipe " .
                "left join RecipeImages recipeImage on recipeImage.recipeId = imagedRecipe.recipeId " .
                "left join Images image on image.imageId = recipeImage.imageId " .
                "where recipeImage.recipeId = cooking.recipeId and (image.creatorId = user.userId or image.creatorId != user.userId or image.creatorId is null) " . // prefer the user's image if he added one
                "order by image.rating desc, rand() limit 1" . // second order random if multiple best ratings
            ") as object1ImageFileName, null as object2Id, " .
            "cooking.cookedDateTime as performedDateTime " .
            "from RecipeCookings cooking ";

        if(isset($ofuserId)) {
            $partSelectCookedRecipesSql .= "left join UserUserFollows uuf on (uuf.followUserId = cooking.cookId and uuf.userId = :cr_uid_1) "; // join to receive cooked recipes of followees
            $partSelectParams[32][':cr_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }

        $partSelectCookedRecipesSql .= 
            "left join Users user on user.userId = cooking.cookId " .
            "left join Recipes recipe on recipe.recipeId = cooking.recipeId " .
            "left join Strings recipeTitle on recipeTitle.stringId = recipe.titleStringId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "where cooking.cookedDateTime is not null ";

        if(isset($startDate)) {
            $partSelectCookedRecipesSql .= "and cooking.cookedDateTime < :cr_sd ";
            $partSelectParams[32][':cr_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectCookedRecipesSql .= "and (cooking.cookId = :cr_uid_2 "; // where ofuserId is the performer
            $partSelectParams[32][':cr_uid_2'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectCookedRecipesSql .= "or uuf.userId = :cr_uid_3"; // where a followee of ofuserId is the performer
                $partSelectParams[32][':cr_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectCookedRecipesSql .= ") ";
        }

        $partSelectSql[32] = $partSelectCookedRecipesSql;

        /************************************************************************************************************************************************************************************ */
        // part select created collections
        $partSelectCreatedCollectionsSql = 
            "select 'createdCollection' as 'type', concat(user.userName, ' created collection ', collectionTitle.originalValue) as message, " .
            "collection.creatorId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "collection.collectionId as object1Id, collectionTitle.originalValue as object1Name, null as object1ImageFileName, " .
            "null as object2Id, collection.createdDateTime as performedDateTime " .
            "from Collections collection ";

        if(isset($ofuserId)) {
            $partSelectCreatedCollectionsSql .= "left join UserUserFollows uuf on (uuf.followUserId = collection.creatorId and uuf.userId = :cc_uid_1) "; // join to receive created collections of followees
            $partSelectParams[64][':cc_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }
        
        $partSelectCreatedCollectionsSql .= 
            "left join Strings collectionTitle on collectionTitle.stringId = collection.titleStringId " .
            "left join Users user on user.userId = collection.creatorId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "where collection.createdDateTime is not null and collection.titleStringId is not null and user.mainCollectionId != collection.collectionId ";

        if(isset($startDate)) {
            $partSelectCreatedCollectionsSql .= "and collection.createdDateTime < :cc_sd ";
            $partSelectParams[64][':cc_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectCreatedCollectionsSql .= "and (collection.creatorId = :cc_uid_2 "; // where ofuserId is the performer
            $partSelectParams[64][':cc_uid_2'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectCreatedCollectionsSql .= "or uuf.userId = :cc_uid_3"; // where a followee of ofuserId is the performer
                $partSelectParams[64][':cc_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectCreatedCollectionsSql .= ") ";
        }

        $partSelectSql[64] = $partSelectCreatedCollectionsSql;

        /************************************************************************************************************************************************************************************ */
        // part select added recipes to collections
        $partSelectAddedRecipesToCollectionSql = 
            "select 'addedRecipeToCollection' as 'type', concat(user.userName, ' added a recipe to his collection') as message, " .
            "col.creatorId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "cr.recipeId as object1Id, collectionTitle.originalValue as object1Name, (" .
                "select image.imageFileName " .
                "from Images image " .
                "left join RecipeImages recipeImage on recipeImage.imageId = image.imageId " .
                "left join Users user on user.userId = image.creatorId " .
                "where recipeImage.recipeId = cr.recipeId and (image.creatorId = user.userId or image.creatorId != user.userId or image.creatorId is null) " . // prefer the user's image if he added one
                "order by image.rating desc, rand() limit 1" . // second order random if multiple best ratings
            ") as object1ImageFileName, cr.collectionId as object2Id, cr.addedDateTime as performedDateTime " .
            "from CollectionRecipes cr " .
            "left join Collections col on col.collectionId = cr.collectionId ";

        if(isset($ofuserId)) {
            $partSelectAddedRecipesToCollectionSql .= "left join UserUserFollows uuf on (uuf.followUserId = col.creatorId and uuf.userId = :arc_uid_1) "; // join to receive added recipes to collections of followees
            $partSelectParams[128][':arc_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }

        $partSelectAddedRecipesToCollectionSql .= 
            "left join Recipes recipe on recipe.recipeId = cr.recipeId " .
            "left join Strings collectionTitle on collectionTitle.stringId = col.titleStringId " .
            "left join Users user on user.userId = col.creatorId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "where cr.addedDateTime is not null ";

        if(isset($startDate)) {
            $partSelectAddedRecipesToCollectionSql .= "and cr.addedDateTime < :arc_sd ";
            $partSelectParams[128][':arc_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectAddedRecipesToCollectionSql .= "and (col.creatorId = :arc_uid_2 "; // where ofuserId is the performer
            $partSelectParams[128][':arc_uid_2'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectAddedRecipesToCollectionSql .= "or uuf.userId = :arc_uid_3"; // where a followee of ofuserId is the performer
                $partSelectParams[128][':arc_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectAddedRecipesToCollectionSql .= ") ";
        }

        $partSelectSql[128] = $partSelectAddedRecipesToCollectionSql;

        /************************************************************************************************************************************************************************************ */
        // part select added images to recipes
        $partSelectAddedImagesToRecipesSql = 
            "select 'addedImageToRecipe' as 'type', concat(user.userName, ' added an image to recipe ', recipeTitle.originalValue) as message, " .
            "img.creatorId as userId, user.userName as userName, userImage.imageFileName as userImageFileName, " .
            "ri.imageId as object1Id, recipeTitle.originalValue as object1Name, img.imageFileName as object1ImageFileName, ri.recipeId as object2Id, " .
            "ri.addedDateTime as performedDateTime " .
            "from RecipeImages ri " .
            "left join Images img on ri.imageId = img.imageId ";

        if(isset($ofuserId)) {
            $partSelectAddedImagesToRecipesSql .= "left join UserUserFollows uuf on (uuf.followUserId = img.creatorId and uuf.userId = :air_uid_1) "; // join to receive added images to recipes of followees
            $partSelectParams[256][':air_uid_1'] = array($ofuserId, PDO::PARAM_INT);
        }

        $partSelectAddedImagesToRecipesSql .= 
            "left join Recipes recipe on recipe.recipeId = ri.recipeId " .
            "left join Strings recipeTitle on recipeTitle.stringId = recipe.titleStringId " .
            "left join Users user on user.userId = img.creatorId " .
            "left join Images userImage on userImage.imageId = user.profileImageId " .
            "where ri.addedDateTime is not null ";

        if(isset($startDate)) {
            $partSelectAddedImagesToRecipesSql .= "and ri.addedDateTime < :air_sd ";
            $partSelectParams[256][':air_sd'] = array($startDate, PDO::PARAM_STR);
        }

        if(isset($ofuserId)) {
            $partSelectAddedImagesToRecipesSql .= "and (img.creatorId = :air_uid_2 "; // where ofuserId is the performer
            $partSelectParams[256][':air_uid_2'] = array($ofuserId, PDO::PARAM_INT);
            if(!$onlyOwnMessages) {
                $partSelectAddedImagesToRecipesSql .= "or uuf.userId = :air_uid_3"; // where a followee of ofuserId is the performer
                $partSelectParams[256][':air_uid_3'] = array($ofuserId, PDO::PARAM_INT);
            }
            $partSelectAddedImagesToRecipesSql .= ") ";
        }

        $partSelectSql[256] = $partSelectAddedImagesToRecipesSql;

        /************************************************************************************************************************************************************************************ */

        $selectFeedMessagesSql = "";
        $selectFeedMessagesParams = array();

        if($conflateRelatedMessages == true) {
            $selectFeedMessagesSql .= "select type, count(*) as cnt, message, userId, userName, userImageFileName, " .
                "group_concat(object1Id order by performedDateTime desc separator '|') as object1Id, " .
                "group_concat(object1Name order by performedDateTime desc separator '|') as object1Name, " .
                "group_concat(object1ImageFileName order by performedDateTime desc separator '|') as object1ImageFileName, object2Id, " .
                "group_concat(performedDateTime order by performedDateTime desc separator '|') as performedDateTime from (";
        }

        $numUnions = 0;
            
        // concatenate requested part select queries
        foreach($partSelectSql as $key => $value) {
            if(($selectedTypes & $key) != 0) {
                if($numUnions > 0) $selectFeedMessagesSql .= "union ";
                $selectFeedMessagesSql .= $value;
                if(is_array($partSelectParams[$key]) && count($partSelectParams[$key]) > 0)
                    $selectFeedMessagesParams = $selectFeedMessagesParams + $partSelectParams[$key];
                $numUnions++;
            }
        }

        if($conflateRelatedMessages == true) {
            $selectFeedMessagesSql .= ") as FeedMessages " .
                "group by type, userId, date(performedDateTime), hour(performedDateTime) ";
        }

        // order union results
        $selectFeedMessagesSql .= "order by date(performedDateTime) desc, hour(performedDateTime) desc, rand();";

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($selectFeedMessagesSql, $selectFeedMessagesParams);
            $sqlQueries[] = $query;

            if($logscreen) {
                echo $query . ' ################# ';
                foreach($selectFeedMessagesParams as $index => $param)
                    echo $index . ' = ' . $param[0] . ' | ';
            }
        }

        //$time_t0 = microtime(true);
        
        $selectFeedMessagesStmt = $database->prepare($selectFeedMessagesSql);
        foreach($selectFeedMessagesParams as $index => $param)
            $selectFeedMessagesStmt->bindParam($index, $param[0], $param[1]);
        $selectFeedMessagesStmt->execute();
        $feedRows = $selectFeedMessagesStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($feedRows as $feedRow) {
            $snack = array(
                'type' => isset($feedRow['type']) ? $feedRow['type'] : "unknown", 
                'count' => $conflateRelatedMessages == true && isset($feedRow['cnt']) ? $feedRow['cnt'] : 1,
                'message' => isset($feedRow['message']) ? $feedRow['message'] : "", 
                'userId' => isset($feedRow['userId']) ? $feedRow['userId'] : 0, 
                'userName' => isset($feedRow['userName']) ? $feedRow['userName'] : 0, 
                'userImageFileName' => isset($feedRow['userImageFileName']) ? $feedRow['userImageFileName'] : "", 
                'object1Id' => isset($feedRow['object1Id']) ? $feedRow['object1Id'] : 0, 
                'object1Name' => isset($feedRow['object1Name']) ? $feedRow['object1Name'] : 0, 
                'object1ImageFileName' => isset($feedRow['object1ImageFileName']) ? $feedRow['object1ImageFileName'] : "", 
                'object2Id' => isset($feedRow['object2Id']) ? $feedRow['object2Id'] : 0, 
                'performedDateTime' => isset($feedRow['performedDateTime']) ? $feedRow['performedDateTime'] : "", 
            );

            $snacks[] = $snack;
        }

        //$time_te = microtime(true);
        //$executionTime = $time_te - $time_t0;

        //echo sprintf("the actual query took %.3f ms", $executionTime);
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

    if($prettyPrint == true)
        echo json_encode($snacks, JSON_PRETTY_PRINT);
    else
        echo json_encode($snacks, JSON_UNESCAPED_UNICODE);
?>
