<?php
    if(!isset($_getpost['userId'])) {
        returnError(3, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['userAccessToken'])) {
        returnError(4, "the parameter userAccessToken was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['selectUserId'])) {
        returnError(3, "the parameter selectUserId was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $usedStringIds = array();

    $userId = $_getpost['userId'];
    $userAccessToken = $_getpost['userAccessToken'];
    $selectUserId = $_getpost['selectUserId'];
    $includeCategories = false;
    $includeTags = false;
    $includeSteps = false;
    $includeRatings = false;
    $users = array();
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
        // select the actual user
        $selectUserSql = "select user.userId, user.userName, user.firstName, user.lastName, user.emailAddress, user.confirmedEmailAddress, " .
            "user.profileImageId, profileImage.imageFileName as profileImageFileName, user.joinedDateTime, user.lastActiveDateTime, " .
            "user.lastRecipeCreatedDateTime, user.lastCollectionEditedDateTime, user.lastCookModeUsedDateTime, user.viewedCount, " .
            "user.followedCount, user.followingCount, user.verifiedState, user.userRights, " .
            "user.linkedProfileType, user.linkedProfileUserId " .
            "from Users user " .
            "left join Images profileImage on user.profileImageid = profileImage.imageid " .
            "where user.userId = ?";
        
        if($debug == true)
            $sqlqueries['selectUserSql'] = $selectUserSql;

        $selectUserStmt = $database->prepare($selectUserSql);
        $selectUserStmt->bindValue(1, $selectUserId, PDO::PARAM_INT);
        //$selectUserStmt->bindValue(2, $userAccessToken, PDO::PARAM_INT);
        $selectUserStmt->execute();
        $userRows = $selectUserStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($userRows as $userRow) {
            $user = array(
                'userId' => isset($userRow['userId']) ? $userRow['userId'] : 0, 
                'userName' => isset($userRow['userName']) ? $userRow['userName'] : "", 
                'firstName' => isset($userRow['firstName']) ? $userRow['firstName'] : "", 
                'lastName' => isset($userRow['lastName']) ? $userRow['lastName'] : "", 
                'emailAddress' => isset($userRow['emailAddress']) ? $userRow['emailAddress'] : "", 
                'confirmedEmailAddress' => isset($userRow['confirmedEmailAddress']) ? $userRow['confirmedEmailAddress'] : 0, 
                'linkedProfileType' => isset($userRow['linkedProfileType']) ? $userRow['linkedProfileType'] : "", 
                'linkedProfileUserId' => isset($userRow['linkedProfileUserId']) ? $userRow['linkedProfileUserId'] : "", 
                'profileImageId' => isset($userRow['profileImageId']) ? $userRow['profileImageId'] : 0, 
                'profileImageFileName' => isset($userRow['profileImageFileName']) ? $userRow['profileImageFileName'] : "", 
                'joinedDateTime' => isset($userRow['joinedDateTime']) ? $userRow['joinedDateTime'] : "", 
                'lastActiveDateTime' => isset($userRow['lastActiveDateTime']) ? $userRow['lastActiveDateTime'] : "", 
                'lastRecipeCreatedDateTime' => isset($userRow['lastRecipeCreatedDateTime']) ? $userRow['lastRecipeCreatedDateTime'] : "", 
                'lastCollectionEditedDateTime' => isset($userRow['lastCollectionEditedDateTime']) ? $userRow['lastCollectionEditedDateTime'] : "", 
                'lastCookModeUsedDateTime' => isset($userRow['lastCookModeUsedDateTime']) ? $userRow['lastCookModeUsedDateTime'] : "", 
                'viewedCount' => isset($userRow['viewedCount']) ? $userRow['viewedCount'] : 0, 
                'followedCount' => isset($userRow['followedCount']) ? $userRow['followedCount'] : 0, 
                'followingCount' => isset($userRow['followingCount']) ? $userRow['followingCount'] : 0, 
                'verifiedState' => isset($userRow['verifiedState']) ? $userRow['verifiedState'] : 0, 
                'userRights' => isset($userRow['userRights']) ? $userRow['userRights'] : 0
            );

            $users[] = $user;
            break; // there should be only one recipe row for a given recipeId anyway
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

    echo json_encode($user, JSON_PRETTY_PRINT);
?>
