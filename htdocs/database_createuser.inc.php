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
    $linkedProfileType = null;
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
        $insertUserStmt = $database->prepare("insert into Users (userName, firstName, lastName, " .
            "emailAddress, hashedPassword, salt, linkedProfileType, linkedProfileUserId, profileImageId, " .
            "mainCollectionId, userRights, joinedDateTime, lastActiveDateTime) " .
            "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), now())");
        $insertUserStmt->bindValue(1, $userName, PDO::PARAM_STR);
        $insertUserStmt->bindValue(2, $firstName, PDO::PARAM_STR);
        $insertUserStmt->bindValue(3, $lastName, PDO::PARAM_STR);
        $insertUserStmt->bindValue(4, $emailAddress, PDO::PARAM_STR);
        $insertUserStmt->bindValue(5, $hashedPassword, PDO::PARAM_STR);
        $insertUserStmt->bindValue(6, $salt, PDO::PARAM_STR);
        $insertUserStmt->bindValue(7, $linkedProfileType, PDO::PARAM_INT);
        $insertUserStmt->bindValue(8, $linkedProfileUserId, PDO::PARAM_STR);
        $insertUserStmt->bindValue(9, $profileImageId, PDO::PARAM_INT);
        $insertUserStmt->bindValue(10, $mainCollectionId, PDO::PARAM_INT);
        $insertUserStmt->bindValue(11, $userRights, PDO::PARAM_INT);
        $insertUserStmt->execute();
        $insertedUserId = $database->lastInsertId();

        // update collection and set owner id
        $updateCollectionStmt = $database->prepare("update Collections set ownerId = ? where collectionId = ?");
        $updateCollectionStmt->bindValue(1, $insertedUserId, PDO::PARAM_INT);
        $updateCollectionStmt->bindValue(2, $mainCollectionId, PDO::PARAM_INT);
        $updateCollectionStmt->execute();
        $updateCollectionRowCount = $updateCollectionStmt->rowCount();

        // insert a new login
        $insertLoginStmt = $database->prepare("insert into Logins (userId, accessToken, deviceId, refreshes, " .
            "loggedInDateTime, lastRefreshedDateTime, validUntilDateTime, expiresAtDateTime) " .
            "values (?, ?, ?, 0, now(), null, date_add(now(), interval 30 day), date_add(now(), interval 4 month))");
        $insertLoginStmt->bindValue(1, $insertedUserId, PDO::PARAM_INT);
        $insertLoginStmt->bindValue(2, $accessToken, PDO::PARAM_STR);
        $insertLoginStmt->bindValue(3, $deviceId, PDO::PARAM_STR);
        $insertLoginStmt->execute();
        $insertedLoginId = $database->lastInsertId();

        // build return json
        $result['userId'] = isset($insertedUserId) ? $insertedUserId : 0;
        $result['loginId'] = isset($insertedLoginId) ? $insertedLoginId : 0;
        $result['mainCollectionId'] = isset($mainCollectionId) ? $mainCollectionId : 0;
        $result['profileImageId'] = isset($profileImageId) ? $profileImageId : 0;

        // log the final prepared sql query string
        //file_put_contents('./log_' . date("Ynj") . '.log', date("H:i:s") . ': ' . $substInsertUserSql . PHP_EOL, FILE_APPEND);
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
