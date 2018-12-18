<?php
    if(!isset($_getpost['userName'])) {
        returnError(3, "the parameter userName was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['accessToken'])) {
        returnError(4, "the parameter accessToken was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['emailAddress']) && !isset($_getpost['linkedProfileUserId'])) {
        returnError(5, "either the parameter emailAddress or linkedProfileUserId has to be specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $userName = $_getpost['userName'];
    $firstName = null;
    $lastName = null;
    $emailAddress = null;
    $hashedPassword = null;
    $salt = null;
    $accessToken = $_getpost['accessToken'];
    $linkedProfileType = 'unlinked';
    $linkedProfileUserId = null;
    $userRights = 1;
    $deviceId = null;

    if(isset($_getpost['firstName']))
        $firstName = $_getpost['firstName'];
    if(isset($_getpost['lastName']))
        $lastName = $_getpost['lastName'];
    if(isset($_getpost['emailAddress']))
        $emailAddress = $_getpost['emailAddress'];
    if(isset($_getpost['hashedPassword']))
        $hashedPassword = $_getpost['hashedPassword'];
    if(isset($_getpost['salt']))
        $salt = $_getpost['salt'];
    if(isset($_getpost['linkedProfileType']))
        $linkedProfileType = $_getpost['linkedProfileType'];
    if(isset($_getpost['linkedProfileUserId']))
        $linkedProfileUserId = $_getpost['linkedProfileUserId'];
    if(isset($_getpost['userRights']))
        $userRights = $_getpost['userRights'];
    if(isset($_getpost['deviceId']))
        $deviceId = $_getpost['deviceId'];

    $mainCollectionId = null;
    $insertedUserId = null;
    $result = array();
    
    try {
        // start transaction block
        $database->beginTransaction();

        // insert new main collection
        $insertCollectionStmt = $database->prepare("insert into Collections (publicationType) values ('public')");
        $insertCollectionStmt->execute();
        $mainCollectionId = $database->lastInsertId();

        // insert actual user
        $insertUserSql = "insert into Users (userName, firstName, lastName, " .
            "emailAddress, hashedPassword, salt, linkedProfileType, linkedProfileUserId, profileImageId, " .
            "mainCollectionId, userRights, joinedDateTime, lastActiveDateTime) " .
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now())";

        // build params map
        $insertUserParams = array(
            1 => array($userName, PDO::PARAM_STR),
            2 => array($firstName, PDO::PARAM_STR),
            3 => array($lastName, PDO::PARAM_STR),
            4 => array($emailAddress, PDO::PARAM_STR),
            5 => array($hashedPassword, PDO::PARAM_STR),
            6 => array($salt, PDO::PARAM_STR),
            7 => array($linkedProfileType, PDO::PARAM_INT),
            8 => array($linkedProfileUserId, PDO::PARAM_STR),
            9 => array($profileImageId, PDO::PARAM_INT),
            10 => array($mainCollectionId, PDO::PARAM_INT),
            11 => array($userRights, PDO::PARAM_INT),
        );

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($insertUserSql, $insertUserParams);
            $sqlQueries[] = $query;
        }

        $insertUserStmt = $database->prepare($insertUserSql);
        foreach($insertUserParams as $index => $param)
            $insertUserStmt->bindValue($index, $param[0], $param[1]);
        $insertUserStmt->execute();
        $insertedUserId = $database->lastInsertId();

        // update collection and set owner id
        $updateCollectionSql = "update Collections set ownerId = ? where collectionId = ?";

        // build params map
        $updateCollectionParams = array(
            1 => array($insertedUserId, PDO::PARAM_INT),
            2 => array($mainCollectionId, PDO::PARAM_INT),
        };

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($updateCollectionSql, $updateCollectionParams);
            $sqlQueries[] = $query;
        }

        $updateCollectionStmt = $database->prepare($updateCollectionSql);
        foreach($updateCollectionParams as $index => $param)
            $updateCollectionStmt->bindValue($index, $param[0], $param[1]);
        $updateCollectionStmt->execute();
        $updateCollectionRowCount = $updateCollectionStmt->rowCount();

        // insert a new login
        $insertLoginSql = "insert into Logins (userId, accessToken, deviceId, refreshes, " .
            "loggedInDateTime, lastRefreshedDateTime, validUntilDateTime, expiresAtDateTime) " .
            "values (?, ?, ?, 0, now(), null, date_add(now(), interval 30 day), date_add(now(), interval 4 month))";

        // build params map
        $insertLoginParams = array(
            1 => array($insertedUserId, PDO::PARAM_INT),
            2 => array($accessToken, PDO::PARAM_STR),
            3 => array($deviceId, PDO::PARAM_STR),
        );

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($insertLoginSql, $insertLoginParams);
            $sqlQueries[] = $query;
        }

        $insertLoginStmt = $database->prepare($insertLoginSql);
        foreach($insertLoginParams as $index => $param)
            $insertLoginStmt->bindValue($index, $param[0], $param[1]);
        $insertLoginStmt->execute();
        $insertedLoginId = $database->lastInsertId();

        // build return json
        $result = array(
            'userId' => isset($insertedUserId) ? $insertedUserId : 0,
            'loginId' => isset($insertedLoginId) ? $insertedLoginId : 0,
            'mainCollectionId' => isset($mainCollectionId) ? $mainCollectionId : 0,
            'profileImageId' => isset($profileImageId) ? $profileImageId : 0,
        );
    }
    catch(PDOException $e) {
        // rollback uncommited changes
        $database->rollBack();
        $array = array(
            'errcode' => 6,
            'pdo.code' => $e->getCode(), 
            'pdo.message' => $e->getMessage()
        );
        returnErrorArray($array);
        exit();
    }
    finally {
        // commit changes to database and end transaction block
        $database->commit();
    }

    // encode and return json
    echo json_encode($result, JSON_PRETTY_PRINT);
?>
