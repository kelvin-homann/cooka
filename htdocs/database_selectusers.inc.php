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

    $usedStringIds = array();

    $userId = $_getpost['userId'];
    $accessToken = $_getpost['accessToken'];
    $followersOfUserId = null;
    $followeesOfUserId = null;
    $users = array();
    $sqlqueries = array();

    if(isset($_getpost['followersOfUserId']))
        $followersOfUserId = $_getpost['followersOfUserId'];
    if(isset($_getpost['followeesOfUserId']))
        $followeesOfUserId = $_getpost['followeesOfUserId'];
    
    try {
        // select the actual users
        $selectUsersSql = "select user.userId, user.userName, user.firstName, user.lastName, user.profileImageId, " .
            "profileImage.imageFileName as profileImageFileName, user.followerCount, user.followeeCount, " .
            "user.verifiedState " .
            "from Users user " .
            "left join Images profileImage on user.profileImageId = profileImage.imageId";

        // build params map
        $selectUsersParams = array(
            // 1 => array($sortField, PDO::PARAM_INT),
            // 2 => array($sortMode, PDO::PARAM_INT),
            // 3 => array($groupField, PDO::PARAM_INT),
        );

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($selectUsersSql, $selectUsersParams);
            $sqlQueries[] = $query;
        }
        
        $selectUsersStmt = $database->prepare($selectUsersSql);
        // foreach($selectUsersParams as $index => $param)
        //     $selectUsersStmt->bindValue($index, $param[0], $param[1]);
        $selectUsersStmt->execute();
        $userRows = $selectUsersStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($userRows as $userRow) {
            $user = array(
                'userId' => isset($userRow['userId']) ? $userRow['userId'] : 0, 
                'userName' => isset($userRow['userName']) ? $userRow['userName'] : "", 
                'firstName' => isset($userRow['firstName']) ? $userRow['firstName'] : "", 
                'lastName' => isset($userRow['lastName']) ? $userRow['lastName'] : "", 
                'profileImageId' => isset($userRow['profileImageId']) ? $userRow['profileImageId'] : 0, 
                'profileImageFileName' => isset($userRow['profileImageFileName']) ? $userRow['profileImageFileName'] : "", 
                'followerCount' => isset($userRow['followerCount']) ? $userRow['followerCount'] : 0, 
                'followeeCount' => isset($userRow['followeeCount']) ? $userRow['followeeCount'] : 0, 
                'verifiedState' => isset($userRow['verifiedState']) ? $userRow['verifiedState'] : 0, 
            );

            $users[] = $user;
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

    echo json_encode($users, JSON_PRETTY_PRINT);
?>
