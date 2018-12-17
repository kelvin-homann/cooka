<?php
    if(!isset($_getpost['userId']) && !isset($_getpost['userName']) && !isset($_getpost['emailAddress'])) {
        returnError(3, "either the parameter userId, userName or emailAddress has to be specified", 0, "");
        return;
    }
    if(!isset($_getpost['hashedPassword'])) {
        returnError(4, "the parameter hashedPassword was not specified", 0, "");
        return;
    }
    // if(!isset($_getpost['accessToken'])) {
    //     returnError(4, "the parameter accessToken was not specified", 0, "");
    //     return;
    // }

    $database = connect();
    if($database == null) return;

    $userId = 0;
    $userName = "";
    $emailAddress = "";
    $hashedPassword = $_getpost['hashedPassword'];
    $accessToken = null;
    $deviceId = null;

    if(isset($_getpost['accessToken']))
        $accessToken = $_getpost['accessToken'];
    if(isset($_getpost['deviceId']))
        $deviceId = $_getpost['deviceId'];

    $fn = null;
    $id = null;
    $pt = null;
    
    if(isset($_getpost['userId']) && strlen(trim($userId = $_getpost['userId'])) > 0) {
        $id = $userId;
        $fn = 'userId';
        $pt = PDO::PARAM_INT;
    }
    else if(isset($_getpost['userName']) && strlen(trim($userName = $_getpost['userName'])) > 0) {
        $id = $userName;
        $fn = 'userName';
        $pt = PDO::PARAM_STR;
    }
    else if(isset($_getpost['emailAddress']) && strlen(trim($emailAddress = $_getpost['emailAddress'])) > 0) {
        $id = $emailAddress;
        $fn = 'emailAddress';
        $pt = PDO::PARAM_STR;
    }
    
    try {
        // authenticate the user
        $authenticateUserSql = "select userId, userName, firstName, lastName, emailAddress, userRights " .
            "from Users where ${fn} = ? and hashedPassword = ?";
        $authenticateUserStmt = $database->prepare($authenticateUserSql);
        $authenticateUserStmt->bindValue(1, $id, $pt);
        $authenticateUserStmt->bindValue(2, $hashedPassword, PDO::PARAM_STR);
        $authenticateUserStmt->execute();
        $userRows = $authenticateUserStmt->fetchAll(PDO::FETCH_ASSOC);
        $numAuthenticatedUser = $authenticateUserStmt->rowCount();

        $resultCode = 0;
        $insertedLoginId = 0;
        $userRights = 1;

        if(isset($numAuthenticatedUser) && $numAuthenticatedUser == 1)
            $resultCode = 1;

        // insert a new login
        if(count($userRows) == 1) {
            $userRow = $userRows[0];
            $userId = $userRow['userId'];
            $userName = $userRow['userName'];
            $firstName = $userRow['firstName'];
            $lastName = $userRow['lastName'];
            $emailAddress = $userRow['emailAddress'];
            $userRights = $userRow['userRights'];

            if(isset($accessToken)) {
                // delete any old login on the same device
                if(isset($deviceId)) {
                    $deleteLoginsSql = "delete from Logins where userId = ? and deviceId = ?";
                    $deleteLoginsStmt = $database->prepare($deleteLoginsSql);
                    $deleteLoginsStmt->bindValue(1, $userId, PDO::PARAM_INT);
                    $deleteLoginsStmt->bindValue(2, $deviceId, PDO::PARAM_STR);
                    $deleteLoginsStmt->execute();
                    $numDeletedLogins = $deleteLoginsStmt->rowCount();
                }

                // insert the actual login
                $insertLoginStmt = $database->prepare("insert into Logins (userId, accessToken, deviceId, loggedInDateTime, " .
                    "validUntilDateTime, expiresAtDateTime) " .
                    "values (?, ?, ?, now(), date_add(now(), interval 30 day), date_add(now(), interval 4 month))");
                $insertLoginStmt->bindValue(1, $userId, PDO::PARAM_INT);
                $insertLoginStmt->bindValue(2, $accessToken, PDO::PARAM_STR);
                $insertLoginStmt->bindValue(3, $deviceId, PDO::PARAM_STR);
                $insertLoginStmt->execute();
                $insertedLoginId = $database->lastInsertId();

                // update user's last active date time
                $updateUserSql = "update Users set lastActiveDateTime = now() where userId = ?";
                $updateUserStmt = $database->prepare($updateUserSql);
                $updateUserStmt->bindValue(1, $userId, PDO::PARAM_INT);
                $updateUserStmt->execute();
                $numUpdatedUsers = $updateUserStmt->rowCount();
            }
        }

        $result = array(
            'result' => isset($resultCode) ? $resultCode : 0,
            'userId' => isset($userId) ? $userId : 0,
            'userName' => isset($userName) ? $userName : "",
            'firstName' => isset($firstName) ? $firstName : "",
            'lastName' => isset($lastName) ? $lastName : "",
            'emailAddress' => isset($emailAddress) ? $emailAddress : "",
            'userRights' => isset($userRights) ? $userRights : 0,
            'loginId' => isset($insertedLoginId) ? $insertedLoginId : 0
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
