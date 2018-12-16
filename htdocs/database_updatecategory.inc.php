<?php
    if(!isset($_getpost['userId'])) {
        returnError(3, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['accessToken'])) {
        returnError(4, "the parameter accessToken was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['categoryId'])) {
        returnError(5, "the parameter categoryId was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $userId = $_getpost['userId'];
    $accessToken = $_getpost['accessToken'];
    $categoryId = $_getpost['categoryId'];
    $parentCategoryId = null;
    $name = null;
    $description = null;
    $imageId = null;
    $imageFileName = null;
    $sortPrefix = null;
    $browsable = null;
    $stringFormat = "ascii";

    if(isset($_getpost['stringFormat'])) {
        if($_getpost['stringFormat'] == "ascii") $stringFormat = "ascii";
        else if($_getpost['stringFormat'] == "base64") $stringFormat = "base64";
    }
    
    try {
        // prepare the sql query string
        $p = 1;
        $params = array();
        $updateCategorySqlTables = "update Categories category ";
        $updateCategorySqlSets = "";
        
        if(isset($_getpost['parentCategoryId'])) {
            $parentCategoryId = trim($_getpost['parentCategoryId']);
            if($parentCategoryId > 0) {
                $updateCategorySqlSets .= "category.parentCategoryId = ?";
                $params[$p] = array($parentCategoryId, PDO::PARAM_INT);
                $p++;
            }
        }

        if(isset($_getpost['name'])) {
            $name = trim($_getpost['name']);
            if($stringFormat == "base64")
                $name = base64_decode($name);
            if(strlen($name) > 0) {
                $updateCategorySqlTables .= "left join Strings categoryNameString on categoryNameString.stringId = category.nameStringId ";
                if($p > 1) $updateCategorySqlSets .= ", ";
                $updateCategorySqlSets .= "categoryNameString.originalValue = ?";
                $params[$p] = array($name, PDO::PARAM_STR);
                $p++;
            }
        }

        if(isset($_getpost['description'])) {
            $description = trim($_getpost['description']);
            if($stringFormat == "base64")
                $description = base64_decode($description);
            if(strlen($description) > 0) {
                $updateCategorySqlTables .= "left join Strings categoryDescriptionString on categoryDescriptionString.stringId = category.descriptionStringId ";
                if($p > 1) $updateCategorySqlSets .= ", ";
                $updateCategorySqlSets .= "categoryDescriptionString.originalValue = ?";
                $params[$p] = array($description, PDO::PARAM_STR);
                $p++;
            }
        }

        if(isset($_getpost['imageId'])) {
            $imageId = trim($_getpost['imageId']);
            if($imageId > 0) {
                if($p > 1) $updateCategorySqlSets .= ", ";
                $updateCategorySqlSets .= "category.imageId = ?";
                $params[$p] = array($imageId, PDO::PARAM_INT);
                $p++;
            }
        }

        if(isset($_getpost['imageFileName'])) {
            $imageFileName = trim($_getpost['imageFileName']);
            if($stringFormat == "base64")
                $imageFileName = base64_decode($imageFileName);
            if(strlen($imageFileName) > 0) {
                $updateCategorySqlTables .= "left join Images categoryImage on categoryImage.imageId = category.imageId ";
                if($p > 1) $updateCategorySqlSets .= ", ";
                $updateCategorySqlSets .= "categoryImage.imageFileName = ?";
                $params[$p] = array($imageFileName, PDO::PARAM_STR);
                $p++;
            }
        }

        if(isset($_getpost['sortPrefix'])) {
            $sortPrefix = trim($_getpost['sortPrefix']);
            if($stringFormat == "base64")
                $sortPrefix = base64_decode($sortPrefix);
            if(strlen($sortPrefix) > 0) {
                if($p > 1) $updateCategorySqlSets .= ", ";
                $updateCategorySqlSets .= "category.sortPrefix = ?";
                $params[$p] = array($sortPrefix, PDO::PARAM_STR);
                $p++;
            }
        }

        if(isset($_getpost['browsable'])) {
            $browsable = trim($_getpost['browsable']);
            if($browsable == 0 || $browsable == 1) {
                if($p > 1) $updateCategorySqlSets .= ", ";
                $updateCategorySqlSets .= "category.browsable = ?";
                $params[$p] = array($browsable, PDO::PARAM_INT);
                $p++;
            }
        }
        
        // put the where clause and concatenate the whole string
        $updateCategorySqlWheres = "where category.categoryId = ?";
        $updateCategorySql = $updateCategorySqlTables . "set " . $updateCategorySqlSets . " " . $updateCategorySqlWheres;

        $substUpdateCategorySql = $updateCategorySql;
        $pos = 0;
        $p = 1;
        while(($pos = strpos($substUpdateCategorySql, "?")) !== false) {
            if($p == count($params) + 1)
                $value = $categoryId;
            else {
                $param = $params[$p];
                $value = $param[0];
                if($param[1] == PDO::PARAM_STR)
                    $value = "'{$value}'";
            }
            $valen = strlen($value);
            $substUpdateCategorySql = substr_replace($substUpdateCategorySql, $value, $pos, 1);
            if($p == count($params) + 1)
                break;
            $p++;
        }

        // log the final prepared sql query string
        file_put_contents('./log_' . date("Ynj") . '.log', date("H:i:s") . ': ' . $substUpdateCategorySql . PHP_EOL, FILE_APPEND);

        // prepare the sql statement and bind parameters
        $updateCategoryStmt = $database->prepare($updateCategorySql);
        foreach($params as $index => $param)
            $updateCategoryStmt->bindValue($index, $param[0], $param[1]);
        $updateCategoryStmt->bindValue(count($params) + 1, $categoryId, PDO::PARAM_INT);

        if($p == 1)
            throw new Exception("update statement did not receive any assignments", 7);

        // update the actual category
        $updateCategoryStmt->execute();
        $response = "";
    }
    catch(PDOException $e) {
        // rollback uncommited changes
        $array = array(
            'errcode' => 6,
            'pdo.code' => $e->getCode(), 
            'pdo.message' => $e->getMessage(), 
        );
        returnErrorArray($array);
        exit();
    }

    // encode and return json
    echo json_encode($response, JSON_PRETTY_PRINT);
?>
