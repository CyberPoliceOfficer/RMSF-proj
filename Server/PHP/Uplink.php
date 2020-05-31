<html>
<body>
 
<?php

include 'header.php';
    

$sql = "INSERT INTO Measurements VALUES (?, now(), ?, ?, ?);";
$sql2 = "INSERT INTO Alarms VALUES (?, now());";

$sth = $connection->prepare($sql);
$sth2 = $connection->prepare($sql2);
    
if($sth == NULL || $sth2 == NULL) {
    $info = $connection->errorInfo();
    echo("<p>Error: {$info[2]}</p>");
    exit();
}
    
    
$raw = file_get_contents("php://input");
$decoded = json_decode($raw);
    
$serial = $decoded->hardware_serial;
$payload = bin2hex(base64_decode($decoded->payload_raw));

if(!empty($payload)) {
    $temp = hexdec($payload[0].$payload[1]);

    $rpm = hexdec($payload[2].$payload[3])*8000/255;
    
    if( hexdec($payload[4].$payload[5]) != 0) {
        $sth2->execute(array($serial));
    }
    
    $x1 = hexdec($payload[6].$payload[7]);
    $y1 = hexdec($payload[8].$payload[9]);
    $r1 = hexdec($payload[10].$payload[11]);
    
    $x2 = hexdec($payload[12].$payload[13]);
    $y2 = hexdec($payload[14].$payload[15]);
    $r2 = hexdec($payload[16].$payload[17]);
    
    $x3 = hexdec($payload[18].$payload[19]);
    $y3 = hexdec($payload[20].$payload[21]);
    $r3 = hexdec($payload[22].$payload[23]);

    $clusters = array($x1, $y1, $r1, $x2, $y2, $r2, $x3, $y3, $r3);
    $cls = to_pg_array($clusters);
    
    $sth->execute(array($serial, $cls, $temp, $rpm));
}
    

    
   
$connection = null;

function to_pg_array($set) {
    settype($set, 'array'); // can be called with a scalar or array
    $result = array();
    foreach ($set as $t) {
        if (is_array($t)) {
            $result[] = to_pg_array($t);
        } else {
            $t = str_replace('"', '\\"', $t); // escape double quote
            if (! is_numeric($t)) // quote only non-numeric values
                $t = '"' . $t . '"';
            $result[] = $t;
        }
    }
    return '{' . implode(",", $result) . '}'; // format
}
     
?>

</body>
</html>