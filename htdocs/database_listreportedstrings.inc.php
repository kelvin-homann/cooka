<?php
    if(!isset($_getpost['accessToken'])) {
        returnError(4, "the parameter accessToken was not specified", 0, "");
        return;
    }

    $database = connect();
    if($database == null) return;

    $accessToken = $_getpost['accessToken'];
    $uiStrings = null;
    $strings = array();
    $numStrings = 0;

    if(isset($_getpost['uiStrings']))
        $uiStrings = $_getpost['uiStrings'];
    
    try {
        // select untranslated ui strings
        $selectStringsStmt = $database->prepare(
            "select string.stringId, string.originalValue, string.originalLanguageId, translatedString.value as translatedValue, " .
            "translatedString.translationSource, translatedString.languageId as translationLanguageId, translatedString.incorrectTranslationState, " .
            "translatedString.incorrectTranslationReporterId, reporter.userName as incorrectTranslationReporterName, " .
            "reporter.emailAddress as incorrectTranslationReporterEmailAddress, translatedString.incorrectTranslationReportedDateTime " .
            "from Strings string " .
            "left join TranslatedStrings translatedString on translatedString.stringId = string.stringId " .
            "left join Users reporter on reporter.userId = translatedString.incorrectTranslationReporterId " .
            "where translatedString.incorrectTranslationState > 0" . (isset($uiStrings) ? " and string.uiString = ?" : "")
        );

        $arg = 1;
        if(isset($uiStrings))
            $selectStringsStmt->bindValue($arg++, $uiStrings, PDO::PARAM_INT);

        $selectStringsStmt->execute();
        $stringsRows = $selectStringsStmt->fetchAll(PDO::FETCH_ASSOC);

        foreach($stringsRows as $stringRow) {
            $string = array(
                'stringId' => $stringRow['stringId'], 
                'originalValue' => $stringRow['originalValue'], 
                'originalLanguageId' => $stringRow['originalLanguageId'], 
                'translatedValue' => $stringRow['translatedValue'], 
                'translationSource' => $stringRow['translationSource'], 
                'translationLanguageId' => $stringRow['translationLanguageId'], 
                'incorrectTranslationState' => $stringRow['incorrectTranslationState'], 
                'incorrectTranslationReporterId' => $stringRow['incorrectTranslationReporterId'], 
                'incorrectTranslationReporterName' => $stringRow['incorrectTranslationReporterName'], 
                'incorrectTranslationReporterEmailAddress' => $stringRow['incorrectTranslationReporterEmailAddress'], 
                'incorrectTranslationReportedDateTime' => $stringRow['incorrectTranslationReportedDateTime'], 
            );

            //$sourceLanguage = $stringRow['originalLanguageId'];
            //if(!in_array($sourceLanguage, $sourceLanguages))
            //    $sourceLanguages[] = $sourceLanguage;

            $strings[] = $string;
            $numStrings++;
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
            'numRecords' => $numStrings
        ), 
        'strings' => $strings
    );

    // encode and return json
    echo json_encode($data, JSON_PRETTY_PRINT);
?>
