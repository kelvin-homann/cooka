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
    $sortMode = 'title';
    $sortDirection = 'asc';
    $filterMode = null;

    $categories = array();
    $numCategories = 0;

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
        $selectCategoryStmt = $database->prepare(
            "select category.categoryId, category.parentCategoryId, " .
            "categoryNameString.originalValue as name, categoryDescriptionString.originalValue as description, " .
            "category.imageId, categoryImage.imageFileName, category.sortPrefix, category.browsable " .
            "from Categories category " .
            "left join Images categoryImage on category.imageId = categoryImage.imageId " .
            "left join Strings categoryNameString on category.nameStringId = categoryNameString.stringId " .
            "left join Strings categoryDescriptionString on category.descriptionStringId = categoryDescriptionString.stringId"
        );
        $selectCategoryStmt->execute();
        $categoryRows = $selectCategoryStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($categoryRows as $categoryRow) {
            $category = array(
                'categoryId' => isset($categoryRow['categoryId']) ? $categoryRow['categoryId'] : 0, 
                'parentCategoryId' => isset($categoryRow['parentCategoryId']) ? $categoryRow['parentCategoryId'] : 0, 
                'name' => isset($categoryRow['name']) ? $categoryRow['name'] : "", 
                'description' => isset($categoryRow['description']) ? $categoryRow['description'] : "", 
                'imageId' => isset($categoryRow['imageId']) ? $categoryRow['imageId'] : 0, 
                'imageFileName' => isset($categoryRow['imageFileName']) ? $categoryRow['imageFileName'] : "", 
                'sortPrefix' => isset($categoryRow['sortPrefix']) ? $categoryRow['sortPrefix'] : "", 
                'browsable' => isset($categoryRow['browsable']) ? $categoryRow['browsable'] : "", 
            );
            
            $categories[] = $category;
            $numCategories++;
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
    // $data = array(
    //     'result' => array(
    //         'numRecords' => $numCategories
    //     ), 
    //     'categories' => $categories,
    // );

    // encode and return json
    echo json_encode($categories, JSON_PRETTY_PRINT);
?>