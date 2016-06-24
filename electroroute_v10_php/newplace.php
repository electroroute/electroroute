<?php
/*
siempre tener en cuenta "config.inc.php" 
*/
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
    //preguntamos si el ussuario y la contraseña esta vacia
    //sino muere
    if (empty($_POST['sitename']) || empty($_POST['direction']) || empty($_POST['longitude']) || empty($_POST['latitude']) || empty($_POST['site_type'])) {
        
        // creamos el JSON
        $response["success"] = 0;
        $response["message"] = "Por favor entre el usuario y el direction";
        
        die(json_encode($response));
    }
    
    //si no hemos muerto (die), nos fijamos si exist en la base de datos
    $query        = " SELECT 1 FROM places WHERE sitename = :site_name";
    
    //acutalizamos el :site_name
    $query_params = array(
        ':site_name' => $_POST['sitename']
    );
    
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
    
    //buscamos la información
    //como sabemos que el usuario ya existe lo matamos
    $row = $stmt->fetch();
    if ($row) {
        // Solo para testing
        //die("This sitename is already in use");
        
        $response["success"] = 0;
        $response["message"] = "Lo siento el usuario ya existe";
        die(json_encode($response));
    }

    //Si llegamos a este punto, es porque el usuario no existe
    //y lo insertamos (agregamos)
    $query = "INSERT INTO places ( sitename, direction, longitude, latitude, description, site_type, imagen) VALUES ( :site_name, :direction, :lon, :lat , :description, :site_type, :imagen) ";
    
    //actualizamos los token
    $query_params = array(
        ':site_name' => $_POST['sitename'],
        ':direction' => $_POST['direction'],		
		':lon' => $_POST['longitude'],
        ':lat' => $_POST['latitude'],
        ':description' => $_POST['description'],
		':site_type' => $_POST['site_type'],
		':imagen' => $_POST['imagen']

    );
    
    //ejecutamos la query y creamos el usuario
    try {
        $stmt   = $db->prepare($query);
        $result = $stmt->execute($query_params);
    }
    catch (PDOException $ex) {
        // solo para testing
        //die("Failed to run query: " . $ex->getMessage());
        
        $response["success"] = 0;
        $response["message"] = "Error base de datos2. Porfavor vuelve a intentarlo";
        die(json_encode($response));
    }
    
    //si hemos llegado a este punto
    //es que el usuario se agregado satisfactoriamente
    $response["success"] = 1;
    $response["message"] = "El sitio se ha agregado correctamente";
    echo json_encode($response);
    
    //para cas php tu puedes simpelmente redireccionar o morir
    //header("Location: login.php"); 
    //die("Redirecting to login.php");
    
    
} else {
?>
 <h1>Register</h1> 
 <form action="newplace.php" method="post"> 
     sitename:<br /> 
     <input type="text" name="sitename" value="" /> 
     <br /><br /> 
     direction:<br /> 
     <input type="text" name="direction" value="" /> 
     <br /><br /> 
     longitude:<br /> 
     <input type="double" name="longitude" value="" />
     <br /><br /> 
     latitude:<br /> 
     <input type="double" name="latitude" value="" /> 
     <br /><br /> 
     description:<br /> 
     <input type="text" name="description" value="" />   
     <br /><br /> 
	 type:<br /> 
     <input type="text" name="site_type" value="" />   
     <br /><br /> 
	 imagen:<br /> 
     <input type="text" name="imagen" value="" />   
     <br /><br /> 
     <input type="submit" value="Register New Site" /> 
 </form>
 <?php
}

?>