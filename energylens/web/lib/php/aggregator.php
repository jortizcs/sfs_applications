<?php
include_once("../sfslib/php/curl_ops.php");

$dbhost = "localhost";
$user = "root";
//$pw = "pangia_root";
$pw = "root";

$agg_subs = array();

$conn = mysql_connect($dbhost, $user, $pw) or die ('Error connecting to mysql');
mysql_select_db("statistics", $conn);
$all_scopes = array("year", "month", "week", "day", "hour", "min", "realtime");


function calc_upper($lower_timestamp, $scope){
	switch($scope){
		case "year":
			return $lower_timestamp+31556926;
		case "month":
			return $lower_timestamp+2629743;
		case "week":
			return $lower_timestamp+604800;
		case "day":
			return $lower_timestamp+86400;
		case "hour":
			return $lower_timestamp+3600;
		case "min":
			return $lower_timestamp+60;
		default:
			return $lower_timestamp;
	}
}

function prefix_range_exists($prefix, $timestamp, $scope){

	global $conn;
	if(strcmp($scope, "realtime")!=0){
		$query = sprintf("select value from `agg_stats` where path='%s' and scope='%s' and start_time<=%d and end_time>=%d limit 1", 
			mysql_real_escape_string($prefix), 
			mysql_real_escape_string($scope),
			$timestamp, 
			$timestamp);
		//echo "prefix_range_exists_query=".$query."\n";
		$result = mysql_query($query, $conn);
		//echo "result=".$result."\n";
		if($result){
			//echo "count = ".mysql_num_rows($result)."\n";
			if(mysql_num_rows($result)>0){
				$row = mysql_fetch_assoc($result);
				return $row["value"];
			}
		}
		return -1;
	} else {
		$query = sprintf("select value from `agg_stats` where path='%s' and scope='%s'", 
			mysql_real_escape_string($prefix), 
			"realtime");
		$result = mysql_query($query, $conn);
		if($result){
			if(mysql_num_rows($result)>0){
				$row = mysql_fetch_assoc($result);
				return $row["value"];
			}
		}
		return -1;
	}
}

function update_agg($fullpath, $prefix, $timestamp, $value, $scope){

	global $conn;
	$current_val="";
	if(strcmp($scope, "realtime")==0){
		$current_val = prefix_range_exists($fullpath, $timestamp, $scope);
	} else {
		$current_val = prefix_range_exists($prefix, $timestamp, $scope);
	}


	if(strcmp($scope, "realtime")!=0){
		if($current_val  >=0){
			$new_value = $value + $current_val;
			$query = sprintf("update `agg_stats` set value=%d where path='%s' and start_time<=%d and end_time>=%d and scope='%s'",
				$new_value,
				mysql_real_escape_string($prefix),
				$timestamp,
				$timestamp,
				mysql_real_escape_string($scope));
			$result = mysql_query($query, $conn);
			//echo "result=".$result."\n";
			if($result){
				//echo "count = ".mysql_affected_rows()."\n";
				if(mysql_affected_rows()>0){
					return true;
				}
			}
			return false;
		} else {
			$upper_ts = calc_upper($timestamp, $scope);
			$insert_query = sprintf("insert into `agg_stats` (path, start_time, end_time, scope, value, units) values ('%s',%d,%d,'%s',%d,'%s')",
				mysql_real_escape_string($prefix),
				$timestamp,
				$upper_ts,
				mysql_real_escape_string($scope),
				$value,
				"W"
			);
			//echo "insert_query=".$insert_query."\n";
			$result = mysql_query($insert_query, $conn);
			//echo "result=".$result."\n";
			if($result){
				//echo "count = ".mysql_affected_rows()."\n";
				if(mysql_affected_rows()>0){
					//echo "scope=$scope\n";
					return true;
				}
			}
			//echo mysql_error()."\n";
			return false;
		}
	} else {
		//either insert or update the current real-time value
		if($current_val >=0 ){
			$query = sprintf("update `agg_stats` set value=%d, start_time=%d, end_time=%d where path='%s' and scope='%s'",
				$value,
				$timestamp,
				$timestamp,
				mysql_real_escape_string($fullpath),
				"realtime");
			$result = mysql_query($query, $conn);
			if($result){
				if(mysql_affected_rows()>0){
					return true;
				}
			}
			return false;
		} else {
			$insert_query = sprintf("insert into `agg_stats` (path, start_time, end_time, scope, value, units) values ('%s',%d,%d,'%s',%d,'%s')",
				mysql_real_escape_string($fullpath),
				$timestamp,
				$timestamp,
				"realtime",
				$value,
				"W"
			);
			$result = mysql_query($insert_query, $conn);
			if($result){
				if(mysql_affected_rows()>0){
					//echo "scope=realtime\n";
					return true;
				}
			}
			return false;
		}
	}
	
}

function get_agg($prefix, $scope, $start_time, $end_time){
	global $conn;
	$query  = sprintf("select value, units, end_time as timestamp from `agg_stats` where path='%s' and scope='%s' and (start_time>=%d and end_time<=%d)",
			mysql_real_escape_string($prefix),
			mysql_real_escape_string($scope),
			$start_time,
			$end_time);
	//echo "get_agg_query=".$query."\n";
	$result = mysql_query($query, $conn);
	//echo "result=".$result."\n";
	if($result){
		//echo "count = ".mysql_num_rows($result)."\n";
		if(mysql_num_rows($result)>0){
			$data_ret=array();
			$all_rows=array();
			while($row = mysql_fetch_assoc($result)){
				array_push($all_rows, $row);
			}
			$data_ret["data"]=$all_rows;
			echo json_encode($data_ret);
		}
	}
}

$req_method = $_SERVER['REQUEST_METHOD'];
if(strcmp($req_method, 'GET')==0) {
	//echo "get called<br>";
	$prefix = $_REQUEST["prefix"];
	$scope = $_REQUEST["scope"];
	$stime = $_REQUEST["starttime"];
	$etime = $_REQUEST["endtime"];
	//echo "prefix=".$prefix."<br>scope=".$scope."<br>start_time=".$stime."<br>end_time=".$etime."<br>";
	get_agg($prefix, $scope, $stime, $etime);
} elseif (strcmp($req_method, 'POST')==0) {
	//echo "post called<br>";
	$data = file_get_contents('php://input');
	//echo "data=".$data."\n\n";
	if(!empty($data)){
		$data_json = json_decode($data, true);
		//print_r($data_json);
		$d_sfspath = $data_json["is4_uri"];
		$d_timestamp = $data_json["timestamp"];
		$d_value = $data_json["Reading"];
		$d_units = $data_json["units"];

		//convert all unit power readings to W
		if(!empty($d_units) && strcmp($d_units, "KW")){
			$d_value = $d_value*1000;
		}
		
		$toks = explode("/", $d_sfspath);
		$tprefix = "";
		$tpath="";
		for ($i=1; $i<count($toks); $i++){
			$tpath = $tpath."/".$toks[$i];
			if(strcmp($tpath, $d_sfspath)!=0){
				$tprefix = $tpath."/*";
				//echo "prefix=".$tprefix."\n";

				//print_r($all_scopes);
				//place in appropriate bin
				for($j=0; $j<count($all_scopes); $j++){
					//echo $all_scopes[$j]."\n";
					update_agg($d_sfspath, $tprefix, $d_timestamp, $d_value, $all_scopes[$j]);
				}
			}
		}
	}
}

mysql_close($conn);
?>
