<?php

require("config.inc.php");

if (!empty($_POST)) {

    if (empty($_POST['sitename']) || empty($_POST['rating']) || empty($_POST['contador'])) {        
        $response["success"] = 0;
        $response["message"] = "Please Enter All Fields";
        die(json_encode($response));
    }


    //si no hemos muerto (die), nos fijamos si exist en la base de datos
    $query        = " SELECT 1 FROM places WHERE sitename = :site_name";
    
    //acutalizamos el :site_name
    $query_params = array(
        ':site_name' => $_POST['sitename']
		
    );

    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error1. Please Try Again!";
        die(json_encode($response));
    }

    $query = "UPDATE places SET rating = :rating, contador = :contador WHERE sitename = :site_name";

    $query_params = array(
        ':site_name' => $_POST['sitename'],
	':rating' => $_POST['rating'],
	':contador' => $_POST['contador']
    );

    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        $response["success"] = 0;
        $response["message"] = "Database Error2. Please Try Again!";
        die(json_encode($response));
    }

    $response["success"] = 1;
    $response["message"] = "Rating actualizado!";
    echo json_encode($response);  

} else {
?>
<h1>Rating</h1> 
 <form action="rating.php" method="post"> 
     sitename:<br /> 
     <input type="text" name="sitename" value="" /> 
     <br /><br /> 
     rating:<br /> 
     <input type="float" name="rating" value="" /> 
     <br /><br /> 
	 contador:<br /> 
     <input type="number" name="contador" value="" /> 
     <br /><br /> 

<input type="submit" value="create" /> 
 </form>
 <?php
}

?>