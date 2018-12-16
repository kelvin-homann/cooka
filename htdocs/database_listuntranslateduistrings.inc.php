<?php
    if(!isset($_getpost['languageId'])) {
        returnError(3, "the parameter languageId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['userId'])) {
        returnError(4, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['userAccessToken'])) {
        returnError(4, "the parameter userAccessToken was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $languageId = $_getpost['languageId'];
    $userId = $_getpost['userId'];
    $userAccessToken = $_getpost['userAccessToken'];
    $uiStrings = array();
    $numUiStrings = 0;
    $appliedUserRights = 0;
    $authorized = false;
    
    try {
        // check if user is authorized to perform this action
        $selectUserSql = "select userRights from Users where userId = ? and userAccessToken = ?";
        $selectUserStmt = $database->prepare($selectUserSql);
        $selectUserStmt->bindValue(1, $userId, PDO::PARAM_INT);
        $selectUserStmt->bindValue(2, $userAccessToken, PDO::PARAM_STR);
        $selectUserStmt->execute();
        $userRows = $selectUserStmt->fetchAll(PDO::FETCH_ASSOC);

        if(isset($userRows) && count($userRows) == 1) {
            $userRow = $userRows[0];
            $userRights = $userRow['userRights'];
            
            // get user rights bit masks
            $urbm = include($scriptDir . '/cooka/userrights.inc.php');

            if(($userRights & $urbm['view ui strings']) == $urbm['view ui strings']) {
                $authorized = true;
                $appliedUserRights = $userRights;
            }
        }

        if(!$authorized)
            throw new Exception("you are not authorized to access this resource", 6);

        // select untranslated ui strings
        $selectStringsSql = "select string.stringId, string.originalValue, string.originalLanguageId " .
            "from Strings string " .
            "left join TranslatedStrings translatedString on translatedString.stringId = string.stringId and translatedString.languageId = ? " .
            "where translatedString.languageId is null and string.originalLanguageId != ? and string.ui = 1";
        $selectStringsStmt = $database->prepare($selectStringsSql);
        $selectStringsStmt->bindValue(1, $languageId, PDO::PARAM_INT);
        $selectStringsStmt->bindValue(2, $languageId, PDO::PARAM_INT);
        $selectStringsStmt->execute();
        $stringsRows = $selectStringsStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($stringsRows as $stringRow) {
            $string = array(
                'stringId' => $stringRow['stringId'], 
                'originalValue' => $stringRow['originalValue'], 
                'originalLanguageId' => $stringRow['originalLanguageId'], 
            );

            $uiStrings[] = $string;
            $numUiStrings++;
        }

        // build return data
        $data = array(
            'result' => array(
                'numRecords' => $numUiStrings
            ), 
            'strings' => $uiStrings
        );

        // encode and return json
        echo json_encode($data, JSON_PRETTY_PRINT);
    }

    catch(PDOException $e) {
        // rollback uncommited changes
        $array = array(
            'errcode' => 5,
            'pdo.code' => $e->getCode(), 
            'pdo.message' => $e->getMessage(), 
        );
        returnErrorArray($array);
    }

    catch(Exception $e) {
        returnError($e->getCode(), $e->getMessage(), null, null);
    }
?>
