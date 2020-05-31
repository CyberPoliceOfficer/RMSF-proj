
<html>
<body>
 
<?php
    
include 'header.php';
    
    
$sql = "SELECT serial_number, relay_on, fan_threshold, relay_threshold
FROM thresholds WHERE serial_number = ? ";
    
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
    
        echo($row["serial_number"]);
        echo(" ");
	    echo($row["relay_on"]);
        echo(" ");
	    echo($row["fan_threshold"]);
        echo(" ");
        echo($row["relay_threshold"]);
        echo("<br>");
}
    
    
$connection = null;
    
?>

</body>
</html>