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
    $tags = $_getpost['tags'];
    $recipeId = null;

    $othertags = array(
        'nudeln', 
        'pasta', 
        'italienisch', 
        'tomaten'
    );

    // echo base64_encode(serialize($othertags));
    // exit();

    if(isset($_getpost['tags'])) {
        $tags_array = @unserialize(base64_decode($_getpost['tags']));

        if($tags_array) {
            echo 'unserialized tags:<br />';
            foreach($tags_array as $tag) {
                echo '<li>#' . $tag . '</li>';
            }
        }
    }

    exit();
    
    try {
        // insert the actual recipe
        $insertRecipeSql = "";

        // build params map
        $insertRecipeParams = array(
            1 => array($userId, PDO::PARAM_INT),
            2 => array($accessToken, PDO::PARAM_STR),
        );

        // ...
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

    // build return data

    $data = array(
        'recipe' => $recipe,
    );

    // encode and return json

    $jsonstring = json_encode($data, JSON_PRETTY_PRINT);

    header('Content-Type: application/json');
    echo $jsonstring;
?>
