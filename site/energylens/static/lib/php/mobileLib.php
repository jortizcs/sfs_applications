<?php
$dbhost="localhost";
$login="root";
$pw="410soda";

$dbconn = mysql_connect($dbhost, $login, $pw) or die ('Could not connect to db');
mysql_select_db("mobile", $dbconn);
function getSFSHostPort($conn){
	$query = "select sfs_host, sfs_port from config";
	$res =mysql_query($query, $conn);
	if($res){
		$row = mysql_fetch_array($res);
		mysql_free_result($res);
		return $row;
	}
	return false;
}

function setSFSHostPort($h, $p){
	global $dbconn;
	$v = getSFSHostPort($dbconn);
	$query="";
	if($v==false){	
		$query = sprintf("insert into config (sfs_host, sfs_port) values(\"%s\", %d)", $h, $p);
	} else {
		$query = sprintf("update config set sfs_host=\"%s\", sfs_port=%d where id=1", $h, $p);
	}
	//echo "query=".$query;
	$val=mysql_query($query);
	return $val;
}

$dat = file_get_contents('php://input');
//print_r($_POST);
//echo "this: ".$_POST["host"];
if(!empty($_POST["host"]) && !empty($_POST["port"])){
	setSFSHostPort($_POST["host"], $_POST["port"]);
}
mysql_close($dbconn);
?>
