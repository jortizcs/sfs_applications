<?php
include_once "sfslib.php";
$sfs_host = $_POST["sfs_host"];
$sfs_port = $_POST["sfs_port"];
$qrc = $_POST["qrc"];

/*$sfs_host="jortiz81.homelinux.com";
$sfs_port=8081;
$qrc="4961c150-6df5-4a8b-909c-f7851ac6c5b1";*/
$sfsconn = new SFSConnection();

if(!empty($sfs_host) && !empty($sfs_port) && !empty($qrc)){
	$sfsconn->setStreamFSInfo($sfs_host, $sfs_port);
	$qrc_children = $sfsconn->getChildren("/demo/qrc/".$qrc);
	$slink=$qrc_children[0];
	$slink_a =explode(" -> ", $slink);
	$iname = $slink_a[0];
	$iname_children = $sfsconn->getChildren($slink_a[1]);

	if(count($iname_children)>0){
		$this_child = $iname_children[0];
		$p = explode(" -> ", $this_child);
		if($sfsconn->exists($slink_a[1]."/".$p[0])){
			delete("http://".$sfs_host.":".$sfs_port.$slink_a[1]."/".$p[0]);
			//echo "delete http://".$sfs_host.":".$sfs_port.$slink_a[1]."/".$p[0];
		} elseif($sfsconn->exists($slink_a[1].$p[0])){
			delete("http://".$sfs_host.":".$sfs_port.$slink_a[1].$p[0]);
			//echo "delete http://".$sfs_host.":".$sfs_port.$slink_a[1].$p[0];
		}
	}
}

?>
