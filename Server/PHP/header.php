<?php

$host = "db.ist.utl.pt";
$user = "ist187028";
$pass = "syss3789";
$dsn = "pgsql:host=$host;dbname=$user";
try
{
  $connection = new PDO($dsn, $user, $pass);
}
catch(PDOException $exception)
{
  echo("<p>Error: ");
  echo($exception->getMessage());
  echo("</p>");
  exit();
}
 ?>
