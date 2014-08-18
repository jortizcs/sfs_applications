<html>
  <head>
    <title>Energy Lens</title>
    <link href="./style.css" rel="stylesheet" type="text/css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" text="javascript/css"></script>
  </head>

  <body>
  <aside id="masthead_bg"></aside>
  <div id="wrap" class="clearfix">
    <header id="masthead">
      <nav>
        <ul>
          <li> <a href="#">Home</a></li>
          <li>/ <a href="#">About</a></li>
          <li>/ <a href="#">Grapher</a></li>
          <li>/ <a href="#">Download</a></li>
          <li>| <a href="#"> Login</a></li>
        </ul>
      </nav>
      <img style="margin-top:20px;" src="./img/EL.png" alt="logo" title="logo"/>
    </header>

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

#generate the QR codes
for($i=0; $i<$max; $i++){
	$v = uniqid();
	//$thisurl="http://is4server.com/mobile/mobile.php?qrc=".$v;
	$thisurl="http://streamfs.cs.berkeley.edu/mobile/mobile.php?qrc=".$v;
	if(strcasecmp($dep, "nokia")==0)
		$thisurl="http://streamfs.cs.berkeley.edu/mobile/nokia/mobile.php?qrc=".$v;
	$t_tinyurl=get("http://tinyurl.com/api-create.php?url=".$thisurl);
	array_push($urls,$t_tinyurl);
	//echo "this_url=".$thisurl."\ttinyurl=".$t_tinyurl."\n\n";
	QRcode::png($t_tinyurl, "qrcs/".$i.".png"); // creates code image and outputs it into /qrcs
}


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

echo "<a href=\"qrcgen_print.php?num=".$max."\" target=\"_blank\">Print-friendly version</a>"
?>

</body>
</html>
