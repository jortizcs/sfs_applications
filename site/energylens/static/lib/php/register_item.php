<?php
include_once("sfslib.php");

$item_name =$_POST["item_name"];
$acme_id=$_POST["acme_id"];
$outlet_id=$_POST["outlet_id"];
//$pushes_own=$_POST["pushes"];
//$is_smap_pub=$_POST["is_smap_pub"];
//$smap_url=$_POST["smap_url"];
$qrc=$_POST["qrc"];
$sfs_host=$_POST["sfs_host"];
$sfs_port=$_POST["sfs_port"];


$errors=array();
if(empty($item_name)){
	array_push($errors, "missing item name");
}

if(empty($acme_id) && empty($outlet_id) && strcmp($pushes_own, "false")){
	array_push($errors, "no way of getting data from item; missing acme, missing outlet, missing internal data production");
}

if(count($errors)>0){
	echo json_encode($errors);
} else {

	//qrc -> item [-> [acme|outlet]]
	if(strcmp($pushes, "true")==0){
		//make item publisher (smap | generic)
		$status=array();
		$status["status"]="fail";
		echo json_encode($status);
	} else {
		$sfsconn = new SFSConnection();
		$sfsconn->setStreamFSInfo($sfs_host, $sfs_port);
		//connect to acme or outlet

		$set_props = false;
		$sfsconn->mkrsrc("/demo/inventory/", $item_name, "default");
		$exists=$sfsconn->exists("/demo/inventory/".$item_name);
		if(!empty($acme_id) && $exists ){
			$val=get("http://".$sfs_host.":".$sfs_port."/demo/inventory/".$acme_id);
			if(!empty($val)){
				$sfsconn->mksymlink("/demo/inventory/".$item_name, "/demo/inventory/".$acme_id, $acme_id);
				$set_props=true;
			}
		} elseif(!empty($outlet_id) && $exists ){
			$val=get("http://".$sfs_host.":".$sfs_port."/demo/inventory/".$outlet_id);
			if(!empty($val)){
				$sfsconn->mksymlink("/demo/inventory/".$item_name, "/demo/inventory/".$outlet_id, $outlet_id);
				$set_props=true;
			}
		}

		if($set_props){
			//set the properties of the newly added item (services object)
			$service_obj = array();
			$service_obj["historical"]="http://is4server.com/services.php?item_path="."/demo/inventory/".$item_name;
			//$service_ojbj["control"]="";
			$sfsconn->overwriteProps("/demo/inventory/".$item_name, $service_obj);
			$sfsconn->mksymlink("/demo/qrc/".$qrc, "/demo/inventory/".$item_name, $item_name);
		}

		$status=array();
		$status["status"]="success";
		echo json_encode($status);
	}

}

?>
