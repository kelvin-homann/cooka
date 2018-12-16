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

    $debug = false;
    $action = $_getpost['action'];

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
    case 'listRecipes':
        include($scriptDir . '/database_listrecipes.inc.php');
        break;
    case 'viewRecipe':
        include($scriptDir . '/database_viewrecipe.inc.php');
        break;
    case 'addRecipe':
        include($scriptDir . '/database_addrecipe.inc.php');
        break;

    case 'listCategories':
        include($scriptDir . '/database_listcategories.inc.php');
        break;
    case 'updateCategory':
        include($scriptDir . '/database_updatecategory.inc.php');
        break;
    
    case 'listTags':
        include($scriptDir . '/database_listtags.inc.php');
        break;
    
    case 'selectUser':
        include($scriptDir . '/database_selectuser.inc.php');
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

    case 'listUntranslatedUiStrings':
        include($scriptDir . '/database_listuntranslateduistrings.inc.php');
        break;
    case 'listReportedStrings':
        include($scriptDir . '/database_listreportedstrings.inc.php');
        break;

    default:
        returnError(1, 'unsupported action specified', 0, 0);
        exit();
    }
?>
