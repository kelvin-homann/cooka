<?php
    $resultCode = 0;
    $resultMessage = "";
    $numUserCollectionFollowsInserted = 0;

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

        if(!isset($_getpost['followCollectionId']) && (!isset($json) || strlen($json) == 0)) {
            $resultCode = 208000;
            $resultMessage = "either the parameter followCollectionId or a request body in form of an array of followCollectionIds needs to be specified";
            throw new Exception($resultMessage);
        }

        $followCollectionId = null;
        if(isset($_getpost['followCollectionId']))
            $followCollectionId = $_getpost['followCollectionId'];

        $followCollectionIds = json_decode($json, true);

        if(!isset($followCollectionId) && (!isset($followCollectionIds) || !is_array($followCollectionIds))) {
            $resultCode = 209000;
            $resultMessage = "the request does not contain a valid array of followCollectionIds";
            throw new Exception($resultMessage);
        }

        if(!isset($followCollectionId) && count($followCollectionIds) == 0) {
            $resultCode = 210000;
            $resultMessage = "the array of followCollectionIds does not contain any items";
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

        // add followCollectionId to the array of followCollectionIds
        if(isset($followCollectionIds) && is_array($followCollectionIds)) {
            if(!in_array($followCollectionId, $followCollectionIds)) {
                array_push($followCollectionIds, $followCollectionId);
            }
        }
        // or create a new array for followCollectionId alone
        else {
            $followCollectionIds = array();
            $followCollectionIds[] = $followCollectionId;
        }
    
        // insert a user-user follow relation
        $insertUserCollectionFollowsValuesSql = '';
        $insertUserCollectionFollowsParams = array();
        $p = 1;

        // iterate through followCollectionIds
        foreach($followCollectionIds as $user) {
            if(is_array($user) || !is_numeric($user)) continue;
            if(strlen($insertUserCollectionFollowsValuesSql) > 0) $insertUserCollectionFollowsValuesSql .= ', ';
            $insertUserCollectionFollowsValuesSql .= '(?, ?)';
            $insertUserCollectionFollowsParams[$p++] = array($foruserId, PDO::PARAM_INT);
            $insertUserCollectionFollowsParams[$p++] = array($user, PDO::PARAM_INT);
        }

        $insertUserCollectionFollowsSql = 'insert into UserCollectionFollows (userId, followCollectionId) values ' . $insertUserCollectionFollowsValuesSql;

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($insertUserCollectionFollowsSql, $insertUserCollectionFollowsParams);
            $sqlQueries[] = $query;
        }

        if(count($insertUserCollectionFollowsParams) > 0) {
            $insertUserCollectionFollowsStmt = $database->prepare($insertUserCollectionFollowsSql);
            foreach($insertUserCollectionFollowsParams as $index => $param)
                $insertUserCollectionFollowsStmt->bindValue($index, $param[0], $param[1]);
            $insertUserCollectionFollowsStmt->execute();
            $numInsertedUserCollectionFollows = $insertUserCollectionFollowsStmt->rowCount();

            if($numInsertedUserCollectionFollows == 0) {
                $resultCode = 212000;
                $resultMessage = "could not insert any user-collection follow relations into the database";
                throw new Exception($resultMessage);
            }
        }

        if($numInsertedUserCollectionFollows == 0 || count($insertUserCollectionFollowsParams) == 0) {
            $resultCode = 213000;
            $resultMessage = "the array of followCollectionIds does not contain any valid followCollectionId";
            throw new Exception($resultMessage);
        }

        $numUserCollectionFollowsInserted += $numInsertedUserCollectionFollows;
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
        'numUserCollectionFollowsInserted' => $numUserCollectionFollowsInserted,
    );

    // encode and return json
    echo json_encode($result, JSON_PRETTY_PRINT);
?>
