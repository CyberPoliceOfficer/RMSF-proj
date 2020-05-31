<html>
<body>
 
<?php
        
include 'header.php';
    
$is_on = $_REQUEST['is_on']; 
    
$sql = "UPDATE Thresholds 
SET relay_on = ? WHERE serial_number = ? ";
    
$sth = $connection->prepare($sql);
    
if($sth == FALSE) {
    $info = $connection->errorInfo();
    echo("<p>Error: {$info[2]}</p>");
    exit();
}
	 
$sth->execute(array($is_on, $_REQUEST['serial']));
    
    
$connection = null;
    
?>

</body>
</html>