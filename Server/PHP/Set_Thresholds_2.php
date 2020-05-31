
<html>
<body>
 
<?php
    
include 'header.php';
    
$t2 = $_REQUEST["t2"];

$sql = " UPDATE Thresholds 
SET relay_threshold = ? WHERE serial_number = ? ";
    
$sth = $connection->prepare($sql);
    
if($sth == FALSE) {
    $info = $connection->errorInfo();
    echo("<p>Error: {$info[2]}</p>");
    exit();
}
	 
$sth->execute(array($t2, $_REQUEST['serial']));
    
$connection = null;
    
?>

</body>
</html>