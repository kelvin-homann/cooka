<?php
    if(!isset($_getpost['userId'])) {
        returnError(3, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['accessToken'])) {
        returnError(4, "the parameter accessToken was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['followUserId'])) {
        returnError(5, "the parameter followUserId was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $userId = $_getpost['userId'];
    $accessToken = $_getpost['accessToken'];
    $followUserId = $_getpost['followUserId'];
    $followers = array();
    $sqlqueries = array();
    
    try {
        // select the follower users
        $selectFollowersSql = "select user.userId, user.userName, " .
            "user.firstName, user.lastName, user.profileImageId, " .
            "profileImage.imageFileName as profileImageFileName, user.followerCount, user.followeeCount, user.verifiedState " .
            "from Users user " .
            "left join Images profileImage on user.profileImageId = profileImage.imageId " .
            "left join UserUserFollows uuf on uuf.userId = user.userId " .
            "where uuf.followUserId = ?";

        if($debug == true)
            $sqlqueries['selectUserSql'] = $selectFollowersSql;

        $selectFollowersStmt = $database->prepare($selectFollowersSql);
        $selectFollowersStmt->bindValue(1, $followUserId, PDO::PARAM_INT);
        $selectFollowersStmt->execute();
        $followerRows = $selectFollowersStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($followerRows as $followerRow) {
            $follower = array(
                'userId' => isset($followerRow['userId']) ? $followerRow['userId'] : 0, 
                'userName' => isset($followerRow['userName']) ? $followerRow['userName'] : "", 
                'firstName' => isset($followerRow['firstName']) ? $followerRow['firstName'] : "", 
                'lastName' => isset($followerRow['lastName']) ? $followerRow['lastName'] : "", 
                'profileImageId' => isset($followerRow['profileImageId']) ? $followerRow['profileImageId'] : 0, 
                'profileImageFileName' => isset($followerRow['profileImageFileName']) ? $followerRow['profileImageFileName'] : "", 
                'followerCount' => isset($followerRow['followerCount']) ? $followerRow['followerCount'] : 0, 
                'followeeCount' => isset($followerRow['followeeCount']) ? $followerRow['followeeCount'] : 0, 
                'verifiedState' => isset($followerRow['verifiedState']) ? $followerRow['verifiedState'] : 0, 
            );

            $followers[] = $follower;
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

    echo json_encode($followers, JSON_PRETTY_PRINT);
?>
