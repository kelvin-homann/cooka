<?php
    $resultCode = 0;
    $resultMessage = "";
    $numUserUserFollowsInserted = 0;

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
        $userRights = authenticateUser($database, $userId, $accessToken);

        if($userRights < 1) {
            $resultCode = 206000;
            $resultMessage = "the user could not be authenticated by the provided access token";
            throw new Exception($resultMessage);
        }
        // only preparatory
        else if($userRights == -3) {
            $resultCode = 207000;
            $resultMessage = "the user is not authorized to perform the requested action";
            throw new Exception($resultMessage);
        }

        $json = file_get_contents('php://input');

        if(!isset($_getpost['followUserId']) && (!isset($json) || strlen($json) == 0)) {
            $resultCode = 208000;
            $resultMessage = "either the parameter followUserId or a request body in form of an array of followUserIds needs to be specified";
            throw new Exception($resultMessage);
        }

        $followUserId = null;
        if(isset($_getpost['followUserId']))
            $followUserId = $_getpost['followUserId'];

        $followUserIds = json_decode($json, true);

        if(!isset($followUserId) && (!isset($followUserIds) || !is_array($followUserIds))) {
            $resultCode = 209000;
            $resultMessage = "the request does not contain a valid array of followUserIds";
            throw new Exception($resultMessage);
        }

        if(!isset($followUserId) && count($followUserIds) == 0) {
            $resultCode = 210000;
            $resultMessage = "the array of followUserIds does not contain any items";
            throw new Exception($resultMessage);
        }

        $foruserId = null;
        $followers = array();
        $sqlqueries = array();

        if(isset($_getpost['foruserId'])) {

            $surrogateToAnyOtherUserRight = $userRightsMap['act as surrogate to any other user'];
            $surrogateToPrincipalUserRight = $userRightsMap['act as surrogate to principal user'];
            $surrogateUserRights = $surrogateToAnyOtherUserRight | $surrogateToPrincipalUserRight;

            // check if the user identified by userId is allowed to act as a surrgate to any other user, 
            // that is userId is most likely an administrator or a moderator
            if(($userRights & $surrogateUserRights) == 0) {
                $resultCode = 211000;
                $resultMessage = "the user is not allowed to act as a surrogate to any other user";
                throw new Exception($resultMessage);
            }
            
            // check if the user is allowed to act as a surrgate to the user identified by foruserId
            // todo: implement surrogate pattern in the database

            $foruserId = $_getpost['foruserId'];
        }
        // set foruserId to the authenticated user identified by userId
        else {
            $foruserId = $userId;
        }

        // add followUserId to the array of followUserIds
        if(isset($followUserIds) && is_array($followUserIds)) {
            if(!in_array($followUserId, $followUserIds)) {
                array_push($followUserIds, $followUserId);
            }
        }
        // or create a new array for followUserId alone
        else {
            $followUserIds = array();
            $followUserIds[] = $followUserId;
        }
    
        // insert a user-user follow relation
        $insertUserUserFollowsValuesSql = '';
        $insertUserUserFollowsParams = array();
        $p = 1;

        // iterate through followUserIds
        foreach($followUserIds as $user) {
            if(is_array($user) || !is_numeric($user)) continue;
            if($user == $foruserId) continue; // can't follow yourself!
            if(strlen($insertUserUserFollowsValuesSql) > 0) $insertUserUserFollowsValuesSql .= ', ';
            $insertUserUserFollowsValuesSql .= '(?, ?)';
            $insertUserUserFollowsParams[$p++] = array($foruserId, PDO::PARAM_INT);
            $insertUserUserFollowsParams[$p++] = array($user, PDO::PARAM_INT);
        }

        $insertUserUserFollowsSql = 'insert into UserUserFollows (userId, followUserId) values ' . $insertUserUserFollowsValuesSql;

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($insertUserUserFollowsSql, $insertUserUserFollowsParams);
            $sqlQueries[] = $query;
        }

        if(count($insertUserUserFollowsParams) > 0) {
            $insertUserUserFollowsStmt = $database->prepare($insertUserUserFollowsSql);
            foreach($insertUserUserFollowsParams as $index => $param)
                $insertUserUserFollowsStmt->bindValue($index, $param[0], $param[1]);
            $insertUserUserFollowsStmt->execute();
            $numInsertedUserUserFollows = $insertUserUserFollowsStmt->rowCount();

            if($numInsertedUserUserFollows == 0) {
                $resultCode = 212000;
                $resultMessage = "could not insert any user-user follow relations into the database";
                throw new Exception($resultMessage);
            }
        }

        if($numInsertedUserUserFollows == 0 || count($insertUserUserFollowsParams) == 0) {
            $resultCode = 213000;
            $resultMessage = "the array of followUserIds does not contain any valid followUserId";
            throw new Exception($resultMessage);
        }

        $numUserUserFollowsInserted += $numInsertedUserUserFollows;
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
        'numUserUserFollowsInserted' => $numUserUserFollowsInserted,
    );

    // encode and return json
    echo json_encode($result, JSON_PRETTY_PRINT);
?>
