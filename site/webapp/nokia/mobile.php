<html>
<head>
<script type="text/javascript" src="lib/jquery-1.6.1.min.js"> </script>
</head>
<script type="text/javascript">
function mobileRet(data){
	window.location.reload();
}

function registerRet(data){
	window.location.reload();
}

function detach(){
	var input_data = new Object();
	input_data.qrc=document.getElementById("qrc").value;
	input_data.sfs_host=document.getElementById("sfs_host").value;
	input_data.sfs_port=document.getElementById("sfs_port").value;
	jQuery.post("lib/php/detach.php", input_data, registerRet);
}

function registerItem(qrc, host, port){
	var input_data= new Object();
	input_data.item_name=document.getElementById("item_name").value;
	input_data.acme_id=document.getElementById("acme_id").value;
	input_data.outlet_id=document.getElementById("outlet_id").value;
	//input_data.pushes_own=document.getElementById("data_push").value;
	input_data.qrc=document.getElementById("qrc").value;
	input_data.sfs_host=document.getElementById("sfs_host").value;
	input_data.sfs_port=document.getElementById("sfs_port").value;

	jQuery.post("lib/php/register_item.php", input_data, registerRet);
}

function reregisterItem(qrc, host, port){
	var input_data= new Object();
	input_data.item_name=document.getElementById("item_name_e").value;
	input_data.acme_id=document.getElementById("acme_id_e").value;
	input_data.outlet_id=document.getElementById("outlet_id_e").value;
	//input_data.pushes_own=document.getElementById("data_push").value;
	input_data.qrc=document.getElementById("qrc").value;
	input_data.sfs_host=document.getElementById("sfs_host").value;
	input_data.sfs_port=document.getElementById("sfs_port").value;
	input_data.edit=true;

	jQuery.post("lib/php/register_item.php", input_data, registerRet);
}

function clickFunc(){
	var v1=document.getElementById("host").value;
	var v2 = document.getElementById("port").value;
	var data = new Object();
	data.host=v1;
	data.port=v2;
	data.deployment = "NOKIA_BERKELEY";
	jQuery.post("lib/php/mobileLib.php", data, mobileRet);
}

function show(id){
	alert("show called!");
	if(id==1){
		document.getElementById("register").style.visibility=false;
		document.getElementById("services").style.visibility=true;
	} else if(id==2){
		document.getElementById("register").style.visibility=true;
		document.getElementById("services").style.visibility=false;
	}
}
</script>
<?php
include_once("lib/php/mobileLib.php");
include_once("lib/php/sfslib.php");

$homedir = "/buildings/nokia_berkeley";
$qrc = $_GET["qrc"];
$dbhost="localhost";
$login="root";
$pw="410soda";
$dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
mysql_select_db("mobile", $dbconn);
$url = getSFSHostPort($dbconn, "NOKIA_BERKELEY");
mysql_close($dbconn);

function isbound($path){
	global $url;
	//$respobj=get("http://".$url["sfs_host"].":".$url["sfs_port"]$homedir."/qrc/".$qrc);
	$respobj=get("http://".$url["sfs_host"].":".$url["sfs_port"].$path);
	#echo "checking:"."http://".$url["sfs_host"].":".$url["sfs_port"].$path."\n";
	if(!empty($respobj)){
		$respobj_json = json_decode($respobj, true);
		$children = $respobj_json["children"];
		if(count($children)>0){
			#echo "YES!";
			return true;	
		}
	}
	#echo "NO!";
	return false;
}

if(empty($qrc)){
	echo "<body>";
		if($url==false){
		echo "SFS Host: <input type=\"text\" id=\"host\" />";
		echo "SFS Port: <input type=\"text\" id=\"port\" /><br />";
		echo "<button type=\"button\" onclick=\"clickFunc()\">Set</button>";
	} else {
		
		echo "<h3> Current Setting </h3>";
		echo "SFS Host: ".$url["sfs_host"]."<br />";
		echo "SFS Port: ".$url["sfs_port"]."<br />";

		echo "<br><h3>Change settings </h3>";
		echo "Input SFS Host: <input type=\"text\" id=\"host\" />";
		echo "Input SFS Port: <input type=\"text\" id=\"port\" /><br />";
		echo "<button type=\"button\" onclick=\"clickFunc()\">Set</button>";
	}
	
} else {
	echo "<input type=\"hidden\" id=\"qrc\" value=\"".$qrc."\">";

} 

$host=$url["sfs_host"];
$port=$url["sfs_port"];
//echo "host=".$host."<br>port=".$port."<br>";
if(!empty($host) && !empty($port)){
	echo "<input type=\"hidden\" id=\"sfs_host\" value=\"".$host."\">";
	echo "<input type=\"hidden\" id=\"sfs_port\" value=\"".$port."\">";
	$sfsconn = new SFSConnection();
	$sfsconn->setStreamFSInfo($host, $port);
	$qrc_exists = $sfsconn->exists($homedir."/qrc/".$qrc);
	$qrc_bound = isbound($homedir."/qrc/".$qrc);

	//echo "qrc_bound=".$qrc_bound."<br>qrc_exists=".$qrc_exists."<br>";

	if(empty($qrc)){
		echo "<div id=register style=\"display:none\">";
	} elseif($qrc_exists && !$qrc_bound){
		echo "<div id=register>";
	} elseif(!empty($qrc) && !$qrc_exists){
		$sfsconn->mkrsrc($homedir."/qrc/", $qrc, "default");
		if($sfsconn->exists($homedir."/qrc/".$qrc)){
			echo "<div id=register>";
		} else {
			echo "Could not create resource for ".$qrc."<br>";
			echo "<div id=register style=\"display:none\">";
		}
	} elseif($qrc_exists && $qrc_bound){
		$qrc_children = $sfsconn->getChildren($homedir."/qrc/".$qrc);
		$slink=$qrc_children[0];
		$slink_a =explode(" -> ", $slink);
		$iname = $slink_a[0];
		$iname_children = $sfsconn->getChildren($slink_a[1]);

		if($iname_children==false || count($iname_children)==0){
			echo "<div id=reregister>";
			echo "<h3>Register item</h3>";
			echo "<h4>How is the data produced?</h4>";
			echo "Acme id:<input type=text id=\"acme_id_e\" \><br>";
			echo "Outlet id:<input type=text id=\"outlet_id_e\" \><br>";
			echo "<button type=\"button\" onclick=\"reregisterItem()\">Re-attach</button>";
			echo "<input type=\"hidden\" id=\"item_name_e\" value=\"".$iname."\">";
			echo "</div>";
			echo "<div id=services style=\"display:none\">";
		}
		echo "<div id=register style=\"display:none\">";
	} else {
		echo "<div id=register style=\"display:none\">";
	}
} else {
	echo "<div id=register style=\"display:none\">";
}
?>
<h3>Register item</h3>
Name:<input type=text id="item_name" \><br>
Description:<input type=text id="itemdesc" \><br><br>

<h4>How is the data produced?</h4>
Acme id:<input type=text id="acme_id" \><br>
Outlet id:<input type=text id="outlet_id" \><br>
<!--Pushes own data? <input type=checkbox id="data_push"><br>-->
<button type="button" onclick="registerItem()">Register</button>

</div>

<?php
//////////////////
//SHOW SERVICES///
//////////////////


$dbhost="localhost";
$login="root";
$pw="410soda";
$dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
mysql_select_db("mobile", $dbconn);
$url = getSFSHostPort($dbconn, "NOKIA_BERKELEY");
mysql_close($dbconn);


$sfsobj = new SFSConnection();
$sfsobj->setStreamFSInfo($url["sfs_host"], $url["sfs_port"]);

$qrc=$_GET["qrc"];

if(empty($qrc)){
	echo "<div id=services style=\"display:none\">";
} elseif(isbound($homedir."/qrc/".$qrc)){
	#echo "It is bound!\n";
	//crawl the links and get the service links associated with device
	$respobj=get("http://".$url["sfs_host"].":".$url["sfs_port"].$homedir."/qrc/".$qrc);
	#echo "respobj=".$respobj;
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
							$control_link = $tprops["control"];
							echo "<a href=\"".$hist_link."&sfs_host=".$url["sfs_host"]."&sfs_port=".$url["sfs_port"]."\">historical</a><br><br>";
							if(!empty($control_link))
								echo "<a href=\"".$control_link."\">control</a><br>";
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
?>
</div>
</body>
</html>
