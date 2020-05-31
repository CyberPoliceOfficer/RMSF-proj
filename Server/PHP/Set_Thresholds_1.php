<html>
<body>
 
<?php
    
include 'header.php';
    
$t1 = $_REQUEST["t1"];

$sql = " UPDATE Thresholds 
SET fan_threshold = ? WHERE serial_number = ? ";
    
$sth = $connection->prepare($sql);
    
if($sth == FALSE) {
    $info = $connection->errorInfo();
    echo("<p>Error: {$info[2]}</p>");
    exit();
}
	 
$sth->execute(array($t1, $_REQUEST['serial']));
    
$connection = null;
    
?>

</body>
</html>