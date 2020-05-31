<html>
<body>
 
<?php
    
include 'header.php';
    

$sql = "SELECT serial_number, start_time
FROM Alarms";
    
$sth = $connection->prepare($sql);
    
if($sth == FALSE) {
    $info = $connection->errorInfo();
    echo("<p>Error: {$info[2]}</p>");
    exit();
}
	 
$sth->execute();
    
$result = $sth->fetchAll();
    
foreach($result as $row)
{
    
        echo($row["serial_number"]);
        echo(" ");
        echo($row["start_time"]);
        echo("\n");
}
   
$connection = null;
    
?>

</body>
</html>