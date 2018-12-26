<?php
    if(!isset($_getpost['userId'])) {
        returnError(3, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['accessToken'])) {
        returnError(4, "the parameter accessToken was not specified", 0, "");
        return;
    }
    
    $database = connect();
    if($database == null) return;

    $userId = $_getpost['userId'];
    $accessToken = $_getpost['accessToken'];
    $json = file_get_contents('php://input');
    $recipe = json_decode($json, true);

    if(!isset($json)) {
        returnError(5, "the request did not contain a valid json body", 0, "");
        return;
    }
    
    file_put_contents('./json/createRecipe_' . date("Ynj") . '.json', $json . PHP_EOL);

    if(!isset($recipe)) {
        returnError(6, "the request did not contain a valid recipe object", 0, "");
        return;
    }

    // echo json_encode($result, JSON_PRETTY_PRINT);
    // return;
    
    try {
        // start transaction block
        $database->beginTransaction();

        // insert the actual recipe
        $insertRecipeSql = "insert into Recipes (";

        // iterate over recipe array and check what keys are set

        // build params map
        $insertRecipeParams = array(
            1 => array($userId, PDO::PARAM_INT),
            2 => array($accessToken, PDO::PARAM_STR),
        );

        $insertedRecipeId = 0;

        $result = array(
            'result' => 1,
            'recipeId' => $insertedRecipeId,
        );
    }
    catch(PDOException $e) {
        // rollback uncommited changes
        $database->rollBack();
        $array = array(
            'errcode' => 7,
            'pdo.code' => $e->getCode(), 
            'pdo.message' => $e->getMessage(), 
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
