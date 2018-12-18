<?php
    // if(!isset($_getpost['userId'])) {
    //     returnError(3, "the parameter userId was not specified", 0, "");
    //     return;
    // }
    // if(!isset($_getpost['accessToken'])) {
    //     returnError(4, "the parameter accessToken was not specified", 0, "");
    //     return;
    // }

    $database = connect();
    if($database == null) return;

    $userId = null;
    $accessToken = null;
    $sortMode = 'title';
    $sortDirection = 'asc';
    $filterMode = null;

    if(isset($_getpost['userId']))
        $userId = $_getpost['userId'];
    if(isset($_getpost['accessToken']))
        $accessToken = $_getpost['accessToken'];

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
        $selectCategoriesSql = "select category.categoryId, category.parentCategoryId, " .
            "categoryNameString.originalValue as name, categoryDescriptionString.originalValue as description, " .
            "category.imageId, categoryImage.imageFileName, category.sortPrefix, category.browsable " .
            "from Categories category " .
            "left join Images categoryImage on category.imageId = categoryImage.imageId " .
            "left join Strings categoryNameString on category.nameStringId = categoryNameString.stringId " .
            "left join Strings categoryDescriptionString on category.descriptionStringId = categoryDescriptionString.stringId";

        // build params map
        $selectCategoriesParams = array(
            // 1 => array($sortField, PDO::PARAM_INT),
            // 3 => array($sortMode, PDO::PARAM_INT),
            // 3 => array($groupField, PDO::PARAM_INT),
        );

        // extend and log sql query
        if($logdb || $logfile || $logscreen) {
            $query = $selectCategoriesSql;//extendSqlQuery($selectCategoriesSql, $selectCategoriesParams);
            $sqlQueries[] = $query;
        }

        $selectCategoriesStmt = $database->prepare($selectCategoriesSql);
        // foreach($selectCategoriesParams as $index => $param)
        //     $selectCategoriesStmt->bindValue($index, $param[0], $param[1]);
        $selectCategoriesStmt->execute();
        $categoryRows = $selectCategoriesStmt->fetchAll(PDO::FETCH_ASSOC);

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
