<?php
    if(!isset($_getpost['userId'])) {
        returnError(3, "the parameter userId was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['accessToken'])) {
        returnError(4, "the parameter accessToken was not specified", 0, "");
        return;
    }
    if(!isset($_getpost['ofuserId'])) {
        returnError(5, "the parameter ofuserId was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $userId = $_getpost['userId'];
    $accessToken = $_getpost['accessToken'];
    $ofuserId = $_getpost['ofuserId'];
    $followees = array();
    $sqlqueries = array();
    
    try {
        // part select the followed users
        $selectFolloweesSql = "select 'user' as type, user.userId as id, user.userName as displayName, " .
            "user.firstName as detail1, user.lastName as detail2, user.profileImageId as imageId, " .
            "profileImage.imageFileName as imageFileName, user.followerCount, user.followeeCount, user.verifiedState, " .
            "user.lastActiveDateTime as lastActiveDateTime " .
            "from Users user " .
            "left join Images profileImage on user.profileImageId = profileImage.imageId " .
            "left join UserUserFollows uuf on uuf.followUserId = user.userId " .
            "where uuf.userId = ? " .
            "union ";

        // part select the followed tags
        $selectFolloweesSql .= "select 'tag', tag.tagId as id, tag.name as displayName, " .
            "null as detail1, null as detail2, null as imageId, " .
            "null as imageFileName, tag.followerCount, null as followeeCount, null as verifiedState, " .
            "tag.lastActiveDateTime as lastActiveDateTime " .
            "from Tags tag " .
            "left join UserTagFollows utf on utf.followTagId = tag.tagId " .
            "where utf.userId = ? " .
            "union ";

        // part select the followed collections
        $selectFolloweesSql .= "select 'collection', collection.collectionId as id, collectionNameString.originalValue as displayName, " .
            "owner.userName as detail1, " .
            "(select count(*) from CollectionRecipes cr where cr.collectionId = collection.collectionId) as detail2, " .
            "null as imageId, null as imageFileName, collection.followerCount, null as followeeCount, null as verifiedState, " .
            "collection.lastActiveDateTime as lastActiveDateTime " .
            "from Collections collection " .
            "left join Users owner on owner.userId = collection.ownerId " .
            "left join Strings collectionNameString on collectionNameString.stringId = collection.nameStringId " .
            "left join UserCollectionFollows ucf on ucf.followCollectionId = collection.collectionId " .
            "where ucf.userId = ? ";

        // order union results
        $selectFolloweesSql .= "order by lastActiveDateTime desc";
        
        if($debug == true)
            $sqlqueries['selectUserSql'] = $selectFolloweesSql;

        $selectFolloweesStmt = $database->prepare($selectFolloweesSql);
        $selectFolloweesStmt->bindValue(1, $ofuserId, PDO::PARAM_INT);
        $selectFolloweesStmt->bindValue(2, $ofuserId, PDO::PARAM_INT);
        $selectFolloweesStmt->bindValue(3, $ofuserId, PDO::PARAM_INT);
        $selectFolloweesStmt->execute();
        $followeeRows = $selectFolloweesStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($followeeRows as $followeeRow) {
            $followee = array(
                'type' => isset($followeeRow['type']) ? $followeeRow['type'] : "", 
                'id' => isset($followeeRow['id']) ? $followeeRow['id'] : "", 
                'displayName' => isset($followeeRow['displayName']) ? $followeeRow['displayName'] : "", 
                'detail1' => isset($followeeRow['detail1']) ? $followeeRow['detail1'] : "", 
                'detail2' => isset($followeeRow['detail2']) ? $followeeRow['detail2'] : "", 
                'imageId' => isset($followeeRow['imageId']) ? $followeeRow['imageId'] : 0, 
                'imageFileName' => isset($followeeRow['imageFileName']) ? $followeeRow['imageFileName'] : "", 
                'followerCount' => isset($followeeRow['followerCount']) ? $followeeRow['followerCount'] : 0, 
                'followeeCount' => isset($followeeRow['followeeCount']) ? $followeeRow['followeeCount'] : 0, 
                'verifiedState' => isset($followeeRow['verifiedState']) ? $followeeRow['verifiedState'] : 0, 
            );

            $followees[] = $followee;
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

    echo json_encode($followees, JSON_PRETTY_PRINT);
?>
