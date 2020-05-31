<html>
<body>
 
<?php
    
include 'header.php';
    
    
$sql = "SELECT EXTRACT(DAY FROM timestamp_) as day, MAX(temperature) as temp
FROM Measurements
WHERE EXTRACT(MONTH FROM CURRENT_DATE) = EXTRACT(MONTH FROM timestamp_)
AND serial_number = ?
GROUP BY EXTRACT(DAY FROM timestamp_)";
    
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
    echo($row["day"]);
    echo(" ");
    echo($row["temp"]);
    echo("<br>");
}
    

$connection = null;
    
?>

</body>
</html>