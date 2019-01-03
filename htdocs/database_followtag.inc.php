<?php
    $resultCode = 0;
    $resultMessage = "";
    $numUserTagFollowsInserted = 0;

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

        if(!isset($_getpost['followTagId']) && (!isset($json) || strlen($json) == 0)) {
            $resultCode = 208000;
            $resultMessage = "either the parameter followTagId or a request body in form of an array of followTagIds needs to be specified";
            throw new Exception($resultMessage);
        }

        $followTagId = null;
        if(isset($_getpost['followTagId']))
            $followTagId = $_getpost['followTagId'];

        $followTagIds = json_decode($json, true);

        if(!isset($followTagId) && (!isset($followTagIds) || !is_array($followTagIds))) {
            $resultCode = 209000;
            $resultMessage = "the request does not contain a valid array of followTagIds";
            throw new Exception($resultMessage);
        }

        if(!isset($followTagId) && count($followTagIds) == 0) {
            $resultCode = 210000;
            $resultMessage = "the array of followTagIds does not contain any items";
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

        // add followTagId to the array of followTagIds
        if(isset($followTagIds) && is_array($followTagIds)) {
            if(!in_array($followTagId, $followTagIds)) {
                array_push($followTagIds, $followTagId);
            }
        }
        // or create a new array for followTagId alone
        else {
            $followTagIds = array();
            $followTagIds[] = $followTagId;
        }
    
        // insert a user-user follow relation
        $insertUserTagFollowsValuesSql = '';
        $insertUserTagFollowsParams = array();
        $p = 1;

        // iterate through followTagIds
        foreach($followTagIds as $user) {
            if(is_array($user) || !is_numeric($user)) continue;
            if(strlen($insertUserTagFollowsValuesSql) > 0) $insertUserTagFollowsValuesSql .= ', ';
            $insertUserTagFollowsValuesSql .= '(?, ?)';
            $insertUserTagFollowsParams[$p++] = array($foruserId, PDO::PARAM_INT);
            $insertUserTagFollowsParams[$p++] = array($user, PDO::PARAM_INT);
        }

        $insertUserTagFollowsSql = 'insert into UserTagFollows (userId, followTagId) values ' . $insertUserTagFollowsValuesSql;

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($insertUserTagFollowsSql, $insertUserTagFollowsParams);
            $sqlQueries[] = $query;
        }

        if(count($insertUserTagFollowsParams) > 0) {
            $insertUserTagFollowsStmt = $database->prepare($insertUserTagFollowsSql);
            foreach($insertUserTagFollowsParams as $index => $param)
                $insertUserTagFollowsStmt->bindValue($index, $param[0], $param[1]);
            $insertUserTagFollowsStmt->execute();
            $numInsertedUserTagFollows = $insertUserTagFollowsStmt->rowCount();

            if($numInsertedUserTagFollows == 0) {
                $resultCode = 212000;
                $resultMessage = "could not insert any user-tag follow relations into the database";
                throw new Exception($resultMessage);
            }
        }

        if($numInsertedUserTagFollows == 0 || count($insertUserTagFollowsParams) == 0) {
            $resultCode = 213000;
            $resultMessage = "the array of followTagIds does not contain any valid followTagId";
            throw new Exception($resultMessage);
        }

        $numUserTagFollowsInserted += $numInsertedUserTagFollows;
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
        'numUserTagFollowsInserted' => $numUserTagFollowsInserted,
    );

    // encode and return json
    echo json_encode($result, JSON_PRETTY_PRINT);
?>
