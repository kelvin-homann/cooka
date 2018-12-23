<?php
    function extendSqlQuery($sql, $params) {

        if(!isset($sql) || !isset($params) || count($params) == 0)
            return $sql;

        $extendedSql = $sql;

        $pos = 0;
        $i = 1;

        // unnamed parameters designated with a question mark
        while(($pos = strpos($extendedSql, "?")) !== false) {
            if($i >= count($params))
                break;

            $param = $params[$i];
            $value = $param[0];
            if(!isset($param[0]) || $param[0] == null)
                $value = 'null';
            else if($param[1] == PDO::PARAM_STR)
                $value = "'{$value}'";

            $valen = strlen($value);
            $extendedSql = substr_replace($extendedSql, $value, $pos, 1);
            if($i == count($params) + 1)
                break;
            $i++;
        }

        // named parameters introduced by a semicolon
        foreach($params as $key => $param) {
            while(($pos = strpos($extendedSql, $key, $pos)) !== false) {
                $value = $param[0];
                if(!isset($param[0]) || $param[0] == null)
                    $value = 'null';
                else if($param[1] == PDO::PARAM_STR)
                    $value = "'{$value}'";

                $keylen = strlen($key);
                $insert = $key . '=' . $value;
                $extendedSql = substr_replace($extendedSql, $insert, $pos, $keylen);
                $pos += strlen($insert);
            }
        }

        return $extendedSql;
    }


    function logSqlQueryToDatabase($database, $originatorId, $accessToken, $sql) {

        global $logdb;
        global $action;

        if(!isset($database) || !isset($sql) || $logdb == false)
            return false;

        if(isset($originatorId) && $originatorId == '')
            $originatorId = null;
        if(isset($accessToken) && $accessToken == '')
            $accessToken = null;

        $insertLoggedQueryParams = array(
            1 => array($originatorId, PDO::PARAM_INT),
            2 => array($originatorId, PDO::PARAM_INT),
            3 => array($accessToken, PDO::PARAM_STR),
            4 => array($accessToken, PDO::PARAM_STR),
            5 => array($action, PDO::PARAM_STR),
            6 => array($sql, PDO::PARAM_STR), 
            7 => array($_SERVER['REQUEST_URI'], PDO::PARAM_STR),
            8 => array($_SERVER['REQUEST_METHOD'], PDO::PARAM_STR),
            9 => array($_SERVER['HTTP_USER_AGENT'], PDO::PARAM_STR),
        );

        $insertLoggedQuerySql = "insert into LoggedQueries (originatorId, loginId, accessToken, action, query, requestUri, requestMethod, userAgent) " .
            "values (?, (select loginId from Logins where userId = ? and accessToken = ?), ?, ?, ?, ?, ?, ?)";

        $insertLoggedQueryStmt = $database->prepare($insertLoggedQuerySql);
        foreach($insertLoggedQueryParams as $index => $param)
            $insertLoggedQueryStmt->bindValue($index, $param[0], $param[1]);
        $insertLoggedQueryStmt->execute();
        $numInsertedLoggedQueries = $insertLoggedQueryStmt->rowCount();

        return ($numInsertedLoggedQueries != 0);
    }
?>
