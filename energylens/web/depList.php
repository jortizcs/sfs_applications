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
    //print_r($_POST);
    if(!empty($_POST['dep_name']) && !empty($_POST['delete'])){
            //delete information from the database
            $dbhost="localhost";
            $login="root";
            $pw="410soda";
            $dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
            mysql_select_db("mobile", $dbconn);
            $query = "delete from config where `deployment`=\"".$_POST['dep_name']."\"";
            $rsrc = mysql_query($query, $dbconn);
            mysql_close($dbconn);

            //delete files
            unlink("config/".$_POST['dep_name'].".json");
            unlink("config/".$_POST['dep_name'].".png");
    }
    elseif(!empty($_POST['deployment'])){
        $name = $_POST['deployment'];
        $fn = "config/".$name.".json";

        //register it.  Save the information to a file.
        if(!file_exists($fn)){
            file_put_contents($fn, json_encode($_POST));

            //create a tinyurl for it.
            $url = "http://is4server.com/energylens/".$fn;
            $tinyurl = get("http://tinyurl.com/api-create.php?url=".$url);
            
            //generate an img for that tinyurl and save it in config.
            QRcode::png($tinyurl, "config/".$name.".png");

            $urlinfo = parse_url($_POST["host"]);
            //print_r($urlinfo);
            $host = $urlinfo["host"];
            $port = $urlinfo["port"];
            if(empty($port))
                $port = 8080;

            //put information in the database
            $dbhost="localhost";
            $login="root";
            $pw="410soda";
            $dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
            mysql_select_db("mobile", $dbconn);
            $query = "insert into config (`sfs_host`, `sfs_port`, `homedir`, `deployment`, `configurl`) values (\"".$host."\", \"".$port."\", \"".$_POST["deployment"]."\",\"".$_POST["deployment"]."\", \"".$url."\")";
            $rsrc = mysql_query($query, $dbconn);
            mysql_close($dbconn);
        } else {
            echo "<script type=\"text/javascript\">";
            echo "alert('what')";
            echo "</script>";
        }
    }
?>

<script type="text/javascript">
function setInfo(){
    var item= document.getElementById("selections");
    var v = item.options[item.selectedIndex].value;
    var imgobj = document.getElementById("qrcode_img");
    if(v !== 'none' )
        imgobj.src = "config/"+v+".png";
    else
        imgobj.src="config/none.png";
}
</script>

<script type="text/javascript">
setInfo();
</script>
<center>
<table>
    <tr>
        <td>
            <select id="selections" onchange="setInfo()">
            <?php
                $dbhost="localhost";
                $login="root";
                $pw="410soda";
                $dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
                mysql_select_db("mobile", $dbconn);
                $query = "select deployment from config";
                $rsrc = mysql_query($query, $dbconn);
                if($rsrc){
                    echo "<option value=\"none\">Choose deployment below ...</option>";
                    while ($row = mysql_fetch_assoc($rsrc))
                        echo "<option value=\"".$row['deployment']."\">".$row['deployment']."</option>";
                }
                mysql_close($dbconn);
            ?>
            </select>
        </td>
    </tr>
    <tr>
        <td>
        <img width="380" height="250" id="qrcode_img" src="" />
        </td>
    </tr>
</table>

<table>
    <tr>
        <td>
        <form action="depRegistration.php" method="get">
        <input type="submit" value="Register a new deployment" />
        </form>
        <!--<form>
        <button>Delete this deployment</button>
        </form>-->
        </td>
    </tr>
</table>

</body>
</html>
