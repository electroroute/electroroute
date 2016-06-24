<?php
/*
siempre tener en cuenta "config.inc.php" 
*/
require("config.inc.php");

//if posted data is not empty
if (!empty($_POST)) {
    //preguntamos si el ussuario y la contraseña esta vacia
    //sino muere
    if (empty($_POST['sitename']) || empty($_POST['comment']) || empty($_POST['title']) || empty($_POST['date']) || empty($_POST['user'])) {
        
        // creamos el JSON
        $response["success"] = 0;
        $response["message"] = "Por favor introduzca todos los datos";
        
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
    
    
    //Si llegamos a este punto, es porque el usuario no existe
    //y lo insertamos (agregamos)
    $query = "INSERT INTO coments ( sitename, message, date, username, rating, title) VALUES ( :site_name, :comment, :date, :user , :rating, :title) ";
    
    //actualizamos los token
    $query_params = array(
        ':site_name' => $_POST['sitename'],
        ':comment' => $_POST['comment'],		
		':date' => $_POST['date'],
        ':user' => $_POST['user'],
        ':rating' => $_POST['rating'],
		':title' => $_POST['title']

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
 <h1>Comentarios y puntuaciones</h1> 
 <form action="rating_comments.php" method="post"> 
     sitename:<br /> 
     <input type="text" name="sitename" value="" /> 
     <br /><br /> 
     title:<br /> 
     <input type="text" name="title" value="" /> 
     <br /><br /> 
     comentario:<br /> 
     <input type="text" name="comment" value="" />   
     <br /><br /> 
	 date:<br /> 
     <input type="text" name="date" value="" />   
     <br /><br /> 
	 usuario:<br /> 
     <input type="text" name="user" value="" />   
     <br /><br /> 
     rating:<br /> 
     <input type="float" name="rating" value="" /> 
     <br /><br />  
     <input type="submit" value="Register New Site" /> 
 </form>
 <?php
}

?>