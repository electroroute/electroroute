<?php 

require("config.inc.php");
if (!empty($_GET)) {
 
 $sitename = $_GET['sitename'];
     //si no hemos muerto (die), nos fijamos si exist en la base de datos
    $query = "select imagen from places where sitename = :sitename";
    
    //acutalizamos el :user
    $query_params = array(
        ':sitename' => $_GET['sitename']
    );
	
	 //ejecutamos la consulta
    try {
        // estas son las dos consultas que se van a hacer en la bse de datos
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
         //solo para testing
        die("Failed to run query: " . $ex->getMessage());
        
        $response["success"] = 0;
        $response["message"] = "Database Error1. Please Try Again!";
        die(json_encode($response));
    }
	$row = $stmt->fetch();
        echo base64_decode($row['imagen']);

 
} else {
?>
 <h1>Register</h1> 
 <form action="get_image.php" method="get"> 
     Sitename:<br /> 
     <input type="text" name="sitename" value="" /> 
   
     <input type="submit" value="get" /> 
 </form>
 <?php
}

?>