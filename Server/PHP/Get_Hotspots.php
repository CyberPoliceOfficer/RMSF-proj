<html>
<body>
 
<?php
    
include 'header.php';
    

$sql = "SELECT hotspots
FROM measurements WHERE serial_number = ? ORDER BY timestamp_ DESC LIMIT 1";
    
$sth = $connection->prepare($sql);
    
if($sth == FALSE) {
    $info = $connection->errorInfo();
    echo("<p>Error: {$info[2]}</p>");
    exit();
}
	 
$sth->execute(array($_REQUEST['serial']));
    
$result = $sth->fetchAll();
    
foreach($result as $row)
{
    
        echo($row["hotspots"]);
        echo("<br>");
    
}
   
$connection = null;
    
?>

</body>
</html>