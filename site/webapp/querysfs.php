<?php
include_once "lib/php/sfslib.php";
$sfs_host=$_POST["sfs_host"];
$sfs_port=$_POST["sfs_port"];
$item_path=$_POST["item_path"];

/*$sfs_host="jortiz81.homelinux.com";
$sfs_port=8081;
$item_path="/demo/inventory/computer";
*/


$sfsobj = new SFSConnection();
$sfsobj->setStreamFSInfo($sfs_host, $sfs_port);

$item_info = get("http://".$sfs_host.":".$sfs_port.$item_path);
$item_info_obj = json_decode($item_info, true);
$item_children = $item_info_obj["children"];
$elts = explode(" -> ", $item_children[0]);
$meter_path = $elts[1];

$mpath_info = get("http://".$sfs_host.":".$sfs_port.$meter_path);
$mpath_info_obj = json_decode($mpath_info, true);
$path_names = explode("/", $item_path);
$item_name = $path_names[count($path_names)-1];

if(!empty($mpath_info_obj["pubid"])){
	
	$tb=60*20;
	$query_ret = $sfsobj->tsNowRangeQueryTB($meter_path, $tb);
	$query_ret_obj = json_decode($query_ret, true);
	$tsquery_res = $query_ret_obj["ts_query_results"];

	$ret=array();
	for($i=0; $i<count($tsquery_res); $i++){
		$reading = $tsquery_res[$i]["value"];
		$ts = $tsquery_res[$i]["ts"];
		$datapoint = array($ts, $reading);
		array_push($ret, $datapoint);
	}
	$tsobj=array();
	$tsobj["item_name"]=$item_name;
	$tsobj["frame"]=$ret;
	echo json_encode($tsobj);
} else {
	$found=false;
	$parent_path=$meter_path;
	$uproot_meter="";
	while(!$found && strcmp($parent_path, "/") !=0){
		$pathElts = explode("/", $parent_path);
		if(count($pathElts)>=0){
			$parent_path="/";
			if(count($pathElts)>0){
				for($i=0; $i<count($pathElts)-1; $i++){
					$parent_path = $parent_path.$pathElts."/";
				}
			}
			$ppath_info = get("http://".$sfs_host.":".$sfs_port.$parent_path);
			$ppath_info_obj = json_decode($ppath_info, true);

			$ppath_children = $ppath_info_obj["children"];
			$ppath_children_obj = json_decode($ppath_children, true);
			if(count($ppath_children_obj)>0){
				$k=0;
				while($k<count($ppath_children_obj)){
					$this_child = $ppath_children_obj[$k];
					if(strpos($this_child, "->")){

						//check all symlinks

						$slink_array = explode(" -> ", $this_child);
						$p = $slink_array[1];


						//check if the child is a publisher

						$finfo = get("http://".$sfs_host.":".$sfs_port.$p);
						$finfo_obj = json_decode($finfo, true);
						if(!empty($finfo_obj["pubid"])){
							$found=true;
							$uproot_meter = $p;
							break;
						}
					}
					$k=$k+1;
				}
			}
		}
	}

	if($found){
		$tb=60*20;
		$query_ret = $sfsobj->tsNowRangeQueryTB($uproot_meter, $tb);
		$query_ret_obj = json_decode($query_ret, true);
		$tsquery_res = $query_ret_obj["ts_query_results"];
		$results = $tsquery_res["results"];

		$ret=array();
		for($i=0; $i<count($results); $i++){
			$reading = $results[$i]["Reading"];
			$ts = $results[$i]["timestamp"];
			$datapoint = array($ts, $reading);
			array_push($ret, $datapoint);
		}
		$tsobj=array();
		$tsobj["item_name"]=$item_name;
		$tsobj["frame"]=$ret;
		$tobj["uproot_path"]=$parent_path;
		echo json_encode($tsobj);
	}
}
?>
