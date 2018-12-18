<?php
    //error_reporting(0);
    date_default_timezone_set("Europe/Berlin");
    header('Content-Type: application/json');

    list($scriptPath) = get_included_files();
    $scriptPathInfo = pathinfo(parse_url($scriptPath)['path']);
    $scriptDir = $scriptPathInfo['dirname'];

    $usingssl = false;
    if(!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] != 'off') {
        $usingssl = true;
    }
    
    //  for debugging purposes
    $_getpost = $_GET;
    //$_getpost = $_POST;

    if(!isset($_getpost['action']))
        exit();

    include($scriptDir . '/functions.inc.php');

    $action = $_getpost['action'];
    $debug = false;
    $allowLogfile = true;
    $allowLogdb = true;
    $logfile = true;
    $logdb = true;
    $logscreen = false;

    if(isset($_getpost['logfile']) && trim($_getpost['logfile']) == 'true')
        $logfile = true && $allowLogfile;
    else if(isset($_getpost['logfile']) && trim($_getpost['logfile']) == 'false')
        $logfile = false;

    if(isset($_getpost['logdb']) && trim($_getpost['logdb']) == 'true')
        $logdb = true && $allowLogdb;
    else if(isset($_getpost['logdb']) && trim($_getpost['logdb']) == 'false')
        $logdb = false;

    if(isset($_getpost['sql']) && trim($_getpost['sql']) == 'true')
        $logscreen = true;

    /**
     * connects to database
     */
    function connect()
    {
        global $scriptDir;
        $config = include($scriptDir . '/config.inc.php');
        $database = null;

        try {
            $database = new PDO($config['dsn'], $config['dbuser'], $config['dbpass'], $config['pdo_options']);
        }
        catch(PDOException $e) {
            returnError(2, 'failed to connect to database', 0, '');
            return null;
        }

        return $database;
    }

    /**
     * returns json formatted array
     */
    function returnErrorArray($array)
    {
        $data = array(
            'error' => $array
        );
        
        // encode and return json
        echo json_encode($data, JSON_PRETTY_PRINT);
    }

    /**
     * returns json formatted error information
     */
    function returnError($errcode, $message, $mysqlerrno, $mysqlerrmsg)
    {
        global $action;

        $data = array(
            'error' => array(
                'errcode' => $errcode, 
                'message' => $message, 
                'action' => $action, 
                'mysqlerrno' => $mysqlerrno, 
                'mysqlerrmsg' => $mysqlerrmsg
            )
        );
        
        // encode and return json
        echo json_encode($data, JSON_PRETTY_PRINT);
    }

    // switch through all allowed actions
    switch($action) {

    /* RECIPE ACTIONS */

    case 'selectRecipe':
        include($scriptDir . '/database_selectrecipe.inc.php');
        break;
    case 'selectRecipes':
        include($scriptDir . '/database_selectrecipes.inc.php');
        break;
    case 'createRecipe':
        include($scriptDir . '/database_createrecipe.inc.php');
        break;

    /* CATEGORY ACTIONS */

    case 'selectCategories':
        include($scriptDir . '/database_selectcategories.inc.php');
        break;
    case 'updateCategory':
        include($scriptDir . '/database_updatecategory.inc.php');
        break;

    /* COLLECTION ACTIONS */

    case 'selectCollectionFollowers':
        include($scriptDir . '/database_selectcollectionfollowers.inc.php');
        break;

    /* TAG ACTIONS */
    
    case 'selectTags':
        include($scriptDir . '/database_selecttags.inc.php');
        break;
    case 'selectTagFollowers':
        include($scriptDir . '/database_selecttagfollowers.inc.php');
        break;

    /* USER ACTIONS */
    
    case 'selectUser':
        include($scriptDir . '/database_selectuser.inc.php');
        break;
    case 'selectUsers':
        include($scriptDir . '/database_selectusers.inc.php');
        break;
    case 'createUser':
        include($scriptDir . '/database_createuser.inc.php');
        break;
    case 'existsUser':
        include($scriptDir . '/database_existsuser.inc.php');
        break;
    case 'authenticateUser':
        include($scriptDir . '/database_authenticateuser.inc.php');
        break;
    case 'refreshLogin':
        include($scriptDir . '/database_refreshlogin.inc.php');
        break;
    case 'invalidateLogin':
        include($scriptDir . '/database_invalidatelogin.inc.php');
        break;
    case 'selectUserFollowers':
        include($scriptDir . '/database_selectuserfollowers.inc.php');
        break;
    case 'selectUserFollowees':
        include($scriptDir . '/database_selectuserfollowees.inc.php');
        break;
    case 'followUser':
        include($scriptDir . '/database_followuser.inc.php');
        break;

    /* STRING ACTIONS */

    case 'selectUntranslatedUiStrings':
        include($scriptDir . '/database_selectuntranslateduistrings.inc.php');
        break;
    case 'selectReportedStrings':
        include($scriptDir . '/database_selectreportedstrings.inc.php');
        break;

    default:
        returnError(1, 'unsupported action specified', 0, 0);
        exit();
    }

    // do logging if enabled

    if($logfile && isset($sqlQueries) && count($sqlQueries) > 0) {
        foreach($sqlQueries as $query) {
            $prefix = strlen($action) > 0 ? $action : 'unknown';
            file_put_contents('./log/' . $prefix . '_' . date("Ynj") . '.log', 
                date("Y-n-j H:i:s") . ';' . $userId . ';' . $accessToken. ';' . $query . ';' . $_SERVER['REQUEST_URI'] . ';' . 
                $_SERVER['REQUEST_METHOD'] . ';' . $_SERVER['HTTP_USER_AGENT'] . PHP_EOL, FILE_APPEND);
        }
    }

    if($logdb && isset($sqlQueries) && count($sqlQueries) > 0)
        foreach($sqlQueries as $query)
            logSqlQueryToDatabase($database, $userId, $accessToken, $query);

    if($logscreen && count($sqlQueries) > 0)
        echo json_encode($sqlQueries, JSON_PRETTY_PRINT);
?>
