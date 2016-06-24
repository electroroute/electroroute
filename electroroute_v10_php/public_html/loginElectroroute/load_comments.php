<?php

//carga y se conecta a la base de datos
require("config.inc.php");

if (!empty($_POST)) {
	if (empty($_POST['sitename'])) {
        
        // creamos el JSON
        $response["success"] = 0;
        $response["message"] = "Por favor introduzca un nombre de sitio";
        
        die(json_encode($response));
    }
    
	
    $query= " SELECT * FROM coments where sitename = :sitename";
    
    //acutalizamos el :site_name
    $query_params = array(
        ':sitename' => $_POST['sitename']
    );
	$array = array();
    
    //ejecutamos la consulta
    try {
        // estas son las dos consultas que se van a hacer en la bse de datos
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        // solo para testing
        //die("Failed to run query: " . $ex->getMessage());
        
        $response["success"] = 0;
        $response["message"] = "Database Error1. Please Try Again!";
        die(json_encode($response));
    }


    
   //como sabemos que el usuario ya existe lo matamos
	while ($row = $stmt->fetch()){
		 if ($row) {
			array_push($array, array(
			"rating" => $row['rating'],
			"name" => $row['username'],
			"date" => $row['date'],
			"comm" => $row['message'],
			"title" => $row['title']));
			}
    }
		//echo json_encode($array);
		//echo json_encode($array_2);
    
    //si hemos llegado a este punto
    //es que el usuario se agregado satisfactoriamente
    $response["success"] = 1;
    $response["message"] =  $array;
    echo json_encode($response);   
} else {
?>
 <h1>Comments</h1> 
 <form action="load_comments.php" method="post"> 
     sitename:<br /> 
     <input type="text" name="sitename" value="" />      
     <input type="submit" value="Buscar" /> 
 </form>
 <?php
}

?>