<html>
<body>
 
<?php
    
include 'header.php';
    
$email = $_REQUEST["email"];
$password = $_REQUEST["password"];
    
$execInfo = array($email, $password);
    
$sql = "SELECT is_admin 
FROM Users
WHERE email = ?
AND password_ = ?";
    
$sth = $connection->prepare($sql);
    
if($sth == FALSE) {
    $info = $connection->errorInfo();
    echo("<p>Error: {$info[2]}</p>");
    exit();
}
	 
$sth->execute($execInfo);
    
$result = $sth->fetchAll();
    
$nresults = count($result);

    
if($nresults > 0) {
    foreach($result as $row)
    { 
	    echo($row["is_admin"]);
        echo("<br>");
        
    }
    
}
    
else {
    echo("-1");
    echo("<br>");
}
  
$connection = null;
    
?>

    
</body>
</html>