<?php
    if(!isset($_getpost['loginId']) && !isset($_getpost['userName']) && !isset($_getpost['emailAddress'])) {
        returnError(3, "either the parameter loginId, userName or emailAddress has to be specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $loginId = "";
    $userName = "";
    $emailAddress = "";
    $pullSalt = null;

    if(isset($_getpost['loginId']))
        $loginId = $_getpost['loginId'];
    if(isset($_getpost['userName']))
        $userName = $_getpost['userName'];
    if(isset($_getpost['emailAddress']))
        $emailAddress = $_getpost['emailAddress'];
    if(isset($_getpost['pullSalt']) && trim($_getpost['pullSalt']) == 'true')
        $pullSalt = true;
    
    try {
        // select the actual user
        $existsUserSql = "select userId, userName, emailAddress, salt from Users where ";
        if(isset($_getpost['loginId'])) $existsUserSql .= "userName = ? or emailAddress = ?";
        else if(isset($_getpost['userName'])) $existsUserSql .= "userName = ?";
        else $existsUserSql .= "emailAddress = ?";

        // build params map
        $existsUserParams = array();
        if(isset($_getpost['loginId'])) {
            $existsUserParams[1] = array($loginId, PDO::PARAM_STR);
            $existsUserParams[2] = array($loginId, PDO::PARAM_STR);
        }
        else if(isset($_getpost['userName'])) 
            $existsUserParams[1] = array($userName, PDO::PARAM_STR);
        else 
            $existsUserParams[1] = array($emailAddress, PDO::PARAM_STR);

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = extendSqlQuery($existsUserSql, $existsUserParams);
            $sqlQueries[] = $query;
        }
            
        $existsUserStmt = $database->prepare($existsUserSql);
        foreach($existsUserParams as $index => $param)
            $existsUserStmt->bindValue($index, $param[0], $param[1]);
        $existsUserStmt->execute();
        $userRows = $existsUserStmt->fetchAll(PDO::FETCH_ASSOC);
        $numExistsUser = $existsUserStmt->rowCount();

        $resultCode = 0;
        if(count($userRows) == 1) {
            $userRow = $userRows[0];
            if(strcasecmp($userRow['userName'], $userName) == 0 || strcasecmp($userRow['userName'], $loginId) == 0)
                $resultCode |= 0x01;
            if(strcasecmp($userRow['emailAddress'], $emailAddress) == 0 || strcasecmp($userRow['emailAddress'], $loginId) == 0)
                $resultCode |= 0x02;
        }

        // build return json
        $result = array(
            'result' => $resultCode,
        );

        if($pullSalt && count($userRows) == 1) {
            $userRow = $userRows[0];
            $result['userId'] = $userRow['userId'];
            $result['salt'] = $userRow['salt'];
        }
        else {
            $result['userId'] = 0;
            $result['salt'] = "";
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

    if(isset($result))
    {
        // encode and return json
        echo json_encode($result, JSON_PRETTY_PRINT);
    }
?>
