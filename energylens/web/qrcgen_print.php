<html>
  <head>
    <title>Energy Lens</title>
    <link href="./style.css" rel="stylesheet" type="text/css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" text="javascript/css"></script>
  </head>

  <body>
  
<?php
include_once "lib/php/curl_ops.php";
include_once "lib/phpqrcode/phpqrcode.php";
#error_reporting(0);
error_reporting(E_ERROR | E_WARNING | E_PARSE);

$max=$_GET["num"];
$dep=$_GET["dep"];
$urls = array();
if(empty($max))
	$max=1000;

# output the QR Codes images in a grid where each row has 4 columns
echo "<table border=1 cellspacing=\"10\" cellpadding=\"10\">";
$val = $max/4;
#for($i=0; $i<$max; $i+10){
$i=0;
echo "<center>";
while($i<$max){
	echo "<tr>";
	for($j=0; $j<4; $j++){
		$img=$i+$j;
		$u = $urls[$img];
		echo "<td><center><img src=qrcs/".$img.".png"."></center><center><b>".$u."</b></center></td>";
	}
	echo "</tr>";
	$i=$i+4;
}
echo "</table>";
echo "</center>";

?>

</body>
</html>
