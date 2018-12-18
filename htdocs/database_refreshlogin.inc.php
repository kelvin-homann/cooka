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
    
    try {
        // delete all invalid or expired logins
        $deleteLoginsSql = "delete from Logins where validUntilDateTime < now() or expiresAtDateTime < now()";
        $deleteLoginsStmt = $database->prepare($deleteLoginsSql);
        $deleteLoginsStmt->execute();
        $numDeletedLogins = $deleteLoginsStmt->rowCount();

        // update the actual login
        $updateLoginSql = "update Logins set refreshes = refreshes + 1, lastRefreshedDateTime = now(), " .
            "validUntilDateTime = date_add(now(), interval 30 day) where userId = ? and accessToken = ?";

        // build params map
        $updateLoginParams = array(
            1 => array($userId, PDO::PARAM_INT),
            2 => array($accessToken, PDO::PARAM_STR),
        );

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($updateLoginSql, $updateLoginParams);
            $sqlQueries[] = $query;
        }

        $updateLoginStmt = $database->prepare($updateLoginSql);
        foreach($updateLoginParams as $index => $param)
            $updateLoginStmt->bindValue($index, $param[0], $param[1]);
        $updateLoginStmt->execute();
        $numUpdatedLogins = $updateLoginStmt->rowCount();

        // update user's last active date time
        $updateUserSql = "update Users set lastActiveDateTime = now() where userId = ?";
        $updateUserStmt = $database->prepare($updateUserSql);
        $updateUserStmt->bindValue(1, $userId, PDO::PARAM_INT);
        $updateUserStmt->execute();
        $numUpdatedUsers = $updateUserStmt->rowCount();

        $result = array(
            'result' => isset($numUpdatedLogins) && $numUpdatedLogins > 0 ? 1 : 0,
            'numUpdatedLogins' => isset($numUpdatedLogins) ? $numUpdatedLogins : 0,
            'numDeletedLogins' => isset($numDeletedLogins) ? $numDeletedLogins : 0,
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

    if(isset($result))
    {
        // encode and return json
        echo json_encode($result, JSON_PRETTY_PRINT);
    }
?>
