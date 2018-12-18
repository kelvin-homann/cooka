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
        // insert a user-user follow relation
        $insertUserUserFollowSql = "insert into UserUserFollows (userId, followUserId) values (?, ?)";

        // build params map
        $insertUserUserFollowParams = array(
            1 => array($userId, PDO::PARAM_INT),
            2 => array($followUserId, PDO::PARAM_INT),
        );

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($insertUserUserFollowSql, $insertUserUserFollowParams);
            $sqlQueries[] = $query;
        }

        $insertUserUserFollowStmt = $database->prepare($insertUserUserFollowSql);
        foreach($insertUserUserFollowParams as $index => $param)
            $insertUserUserFollowStmt->bindValue($index, $param[0], $param[1]);
        $insertUserUserFollowStmt->execute();
        $numInsertedUserUserFollows = $insertUserUserFollowStmt->rowCount();

        // build return json
        $result = array(
            'result' => isset($numInsertedUserUserFollows) && $numInsertedUserUserFollows == 1 ? 1 : 0,
        );
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

    echo json_encode($result, JSON_PRETTY_PRINT);
?>
