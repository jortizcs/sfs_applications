<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Energy Lens Application</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="Jorge Ortiz" content="">

    <!-- Le styles -->
    <link href="../bootstrap/docs/assets/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
        padding-top: 1px;
        padding-bottom: 1px;
      }
    </style>
    <link href="../bootstrap/docs/assets/css/bootstrap-responsive.css" rel="stylesheet">

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Le fav and touch icons -->
    <link rel="shortcut icon" href="images/favicon.ico">
    <link rel="apple-touch-icon" href="images/apple-touch-icon.png">
    <link rel="apple-touch-icon" sizes="72x72" href="images/apple-touch-icon-72x72.png">
    <link rel="apple-touch-icon" sizes="114x114" href="images/apple-touch-icon-114x114.png">
  </head>

  <body>

    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
	  <a class="brand" href="http://streamfs.cs.berkeley.edu">StreamFS Apps</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li class="active"><a href="index.php">Home</a></li>
              <li><a href="http://www.eecs.berkeley.edu/~jortiz">About</a></li>
              <li><a href="mailto:jortiz@cs.berkeley.edu">Contact</a></li>
		
            </ul>
            <ul class="nav pull-right nav-pills">
      	    <li><a class="btn btn-primary btn-large">CalNet Login</a></li>
      	    </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
<br><br><br>

<?php
include_once "../lib/php/curl_ops.php";
include_once "../phpqrcode/phpqrcode.php";

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
	QRcode::png($t_tinyurl, "../qrcs/".$i.".png"); // creates code image and outputs it directly into browser
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
		//echo "<td><img src=qrcs/".$img.".png"."><br><center>".$img."</center></td>";
		echo "<td><center><img src=../qrcs/".$img.".png"."></center><center><b>".$u."</b></center></td>";
	}
	echo "</tr>";
	$i=$i+4;
}
echo "</table>";
echo "</center>";

?>

</body>
</html>
