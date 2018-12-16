<?php
    if(!isset($_getpost['userAccessToken'])) {
        returnError(4, "the parameter userAccessToken was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $userAccessToken = $_getpost['userAccessToken'];
    $sortMode = 'title';
    $sortDirection = 'asc';
    $filterMode = null;

    $tags = array();
    $numTags = 0;

    if(isset($_getpost['sortMode']))
    {
        switch($_getpost['sortMode']) {
        case 'rating':
            $sortMode = 'rating';
            break;
        }
    }

    if(isset($_getpost['sortDirection']) && $_getpost['sortDirection'] == 'desc')
        $sortDirection = 'desc';
    
    try {
        // select the actual categories
        $selectTagStmt = $database->prepare(
            "select tag.tagId, tag.name as tagName " .
            "from Tags tag"
        );
        $selectTagStmt->execute();
        $tagRows = $selectTagStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($tagRows as $tagRow) {
            $tag = array(
                'tagId' => $tagRow['tagId'], 
                'tagName' => $tagRow['tagName'], 
            );
            
            $tags[] = $tag;
            $numTags++;
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

    // build return data
    $data = array(
        'result' => array(
            'numRecords' => $numTags
        ), 
        'tags' => $tags,
    );

    // encode and return json
    echo json_encode($data, JSON_PRETTY_PRINT);
?>
