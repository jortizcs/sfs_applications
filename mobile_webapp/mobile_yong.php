<?php
include_once("lib2/php/sfslib.php");
$qrc = $_GET["qrc"];
$HOST = "ec2-184-169-204-224.us-west-1.compute.amazonaws.com";
$GRAPHER_HOST = "http://ec2-204-236-167-113.us-west-1.compute.amazonaws.com/grapher/development";
$PORT = 8080;
$inventory_path = "/buildings/SDH/inventory";

$sfsconn = new SFSConnection();
$sfsconn->setStreamFSInfo($HOST, $PORT);
$qrc_exist = $sfsconn->exists("/buildings/SDH/qrc/".$qrc);
if(!$qrc_exist){
	echo "not exist";
}
else{
	$children = $sfsconn->getChildren("/buildings/SDH/qrc/".$qrc);
	$in_inventory = false;
	$item = "";
	for($i=0;$i<count($children);$i++){
		if(strpos($children[$i],$inventory_path)){
			$item = strtok($children[$i]," -> ");
			$in_inventory = true;
		}
	}
	if($in_inventory){
		$children = $sfsconn->getChildren("/buildings/SDH/qrc/".$qrc."/".$item);
		//echo "inven";
		if($children){
			$graph_path = strtok($children[0]," -> ");
			$graph_path = strtok(" -> ");
			//echo $graph_path;
			$info = get("http://".$HOST.":".$PORT."/buildings/SDH/qrc/".$qrc."/".$item);
			$info_obj = json_decode($info,true);
			$ts = $info_obj['properties']['bindattach_ts'];
			if($ts){
				$url = $GRAPHER_HOST.$graph_path."true_power/?start=".$ts;
				//echo $url;
				header( 'Location: '.$url );
			}else{
				echo "no property 'bindattach_ts'\n";
			}
		}
		else{
			echo "no children under "."/buildings/SDH/qrc/".$qrc."/".$item;
		}
	}else{
		echo "qrc is not in /buildings/SDH/qrc/";
	}
}
?>