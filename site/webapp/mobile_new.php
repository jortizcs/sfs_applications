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
            <li class="active"><a href="../energylens/">Home</a></li>
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

include_once("lib2/php/mobileLib.php");
include_once("lib2/php/sfslib.php");

# get the streamfs instance for this deployment
$qrc = $_GET["qrc"];
$dbhost="localhost";
$login="root";
$pw="410soda";
$dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
mysql_select_db("mobile", $dbconn);
$url = getSFSHostPort($dbconn, "DEMO");
mysql_close($dbconn);

if($url == false){
    echo "Not deployment identifier specified<br>";
} else {
    $host=$url["sfs_host"];
    $port=$url["sfs_port"];
    $homedir=$url["homedir"];

    $dbhost="localhost";
    $login="root";
    $pw="410soda";
    $dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
    mysql_select_db("mobile", $dbconn);
    $url = getSFSHostPort($dbconn, "DEMO");
    mysql_close($dbconn);

    $sfsobj = new SFSConnection();
    $sfsobj->setStreamFSInfo($url["sfs_host"], $url["sfs_port"]);

    $qrc=$_GET["qrc"];
    if(empty($qrc)){
        echo "Item Unregistered<br>";
        echo "Want to register your item?  Download the <a href=\"../energylens\">Android-based Mobile Auditing Application</a><br>";
    } elseif(isbound($homedir."/qrc/".$qrc)){
        
        //crawl the links and get the service links associated with device
        $respobj=get("http://".$url["sfs_host"].":".$url["sfs_port"].$homedir."/qrc/".$qrc);
        if(!empty($respobj)){
            $respobj_json = json_decode($respobj, true);
            $children = $respobj_json["children"];
            if(count($children)>0){
                for($i=0; $i<count($children); $i++){
                    $child = $children[$i];
                    if(strpos($child, "->")){
                        $symlink_a = explode(" -> ", $child);
                        if(count($symlink_a)==2){
                            $target=$symlink_a[1];
                            $target_respobj = get("http://".$url["sfs_host"].":".$url["sfs_port"].$target);
                            
                            if(!empty($target_respobj)){
                                if($i==0)
                                    echo "<div id=services><h3>Available Services</h3>";
                                $target_json = json_decode($target_respobj, true);
                                $tprops = $target_json["properties"];
                                $hist_link = $tprops["historical"];
                                $personal_link = $tprops["personal"];
                                $control_link = $tprops["control"];
                                echo "<a href=\"".$hist_link."&sfs_host=".$url["sfs_host"]."&sfs_port=".$url["sfs_port"]."\">energy scan</a><br>";
                                if(!empty($control_link))
                                    echo "<a href=\"".$control_link."\">control</a><br>";
                                if(!empty($personal_link))
                                    echo "<a href=\"".$personal_link."\">personal energy</a><br><br>";
                                echo "<button type=\"button\" onclick=\"detach()\">Detach from meter</button>";
                            } else {
                                echo "<div id=services style=\"display:none\">";
                            }
                        }
                    }

                }
            } else {
                echo "<div id=services style=\"display:none\">";
            }
        }
    } else {
        echo "<div id=services style=\"display:none\">";
    }
}
?>

</body>
</html>
