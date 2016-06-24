<?php

//carga y se conecta a la base de datos
require("config.inc.php");
    $query= " SELECT * FROM places";
    
    //acutalizamos el :site_name
    $query_params = null;
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
		"name" => $row['sitename'],
		"long" =>  (double)$row['longitude'],
		"lat" => (double)$row['latitude'],
		"dir" => $row['direction'],
		"desc" => $row['description'],
		"type" => $row['site_type'],
		"rat" => $row['rating'],
		"cont" => $row['contador']));

	}
    }
		//echo json_encode($array);
		//echo json_encode($array_2);
    
    //si hemos llegado a este punto
    //es que el usuario se agregado satisfactoriamente
    $response["success"] = 1;
    $response["message"] =  $array;
    echo json_encode($response)
    
    //para cas php tu puedes simpelmente redireccionar o morir
    //header("Location: login.php"); 
    //die("Redirecting to login.php");
	
	?>