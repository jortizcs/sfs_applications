<?php
$dbhost="localhost";
$login="root";
$pw="410soda";

$dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
mysql_select_db("mobile", $dbconn);
function getSFSHostPort($conn, $deployment){
	$dep = "DEMO";
	if(strcasecmp($deployment, "NOKIA_BERKELEY")==0)
		$dep = "NOKIA_BERKELEY";
	$query = "select * from config WHERE `deployment` = \"".$dep."\"";
	$res =mysql_query($query, $conn);
	if($res){
		$row = mysql_fetch_array($res);
		mysql_free_result($res);
		return $row;
	}
	return false;
}

function setSFSHostPort($h, $p, $d){
	global $dbconn;
	
	$v = getSFSHostPort($dbconn, $d);
	$dep = "DEMO";
	if(strcasecmp($d, "NOKIA_BERKELEY")==0)
		$dep = "NOKIA_BERKELEY";
	$query="";
	if($v==false){	
		$query = sprintf("insert into config (sfs_host, sfs_port, deployment) values(\"%s\", %d, \"%s\")", $h, $p, $dep);
	} else {
		$query = sprintf("update config set sfs_host=\"%s\", sfs_port=%d where deployment=\"%s\"", $h, $p, $dep);
	}
	//echo "query=".$query;
	$val=mysql_query($query);
	return $val;
}

$dat = file_get_contents('php://input');
//print_r($_POST);
//echo "this: ".$_POST["host"];
if(!empty($_POST["host"]) && !empty($_POST["port"]) && !empty($_POST["deployment"])){
	setSFSHostPort($_POST["host"], $_POST["port"], $_POST["deployment"]);
}
mysql_close($dbconn);
?>
