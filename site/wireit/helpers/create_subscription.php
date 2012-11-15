<?php
require_once('../header_text_plain.php');
require_once('../config.php');
require_once('../constants.php');
require_once('../curl_ops.php');
require_once('../sfslib.php');
const PROC_NAME="create_proc";
const OPERATION="operation";
const WINSIZE="winsize";
const MATERIALIZE="materialize";
const FUNC="func";
const TIMEOUT="timeout";
const TARGET="target";
const PATH="path";
/*Put this in sfs lib*/
$sfs = new SFSConnection();
$sfs->setStreamFSInfo(CUR_HOST,8080);

function pad_path($path){
    if(substr($path,0,1) != "/"){
        $path = "/$path";
    }
    return $path;
  }

function create_subscription($arr){
  /*to do make sure make sure both paths exist*/
  $request=array();
  $request['path']=pad_path($arr[PATH]);//$arr[OPERATION];
  $request['target']=($arr[TARGET]);
  $path = "/sub";#$arr[PROC_NAME];
  global $host, $port;	
  $url = "http://".$host.":".$port.$path;
  echo "request: ".json_encode($request);
  if(count($request)>0){
    $reply = post(json_encode($request), $url);
    return $reply;
  }
  return 0;
}
function isSubscriptionCreation($request){
    //script is populated by forntend
    return isset($request[TARGET]) && isset($request[PATH]); 
}
  switch($_SERVER['REQUEST_METHOD']){
      case "GET":
      $request=&$_GET;
        break;
      case "POST":
      $request=&$_POST;
        break;
  }
  if(isSubscriptionCreation($request)){
    echo create_subscription($request);
  } else {
    echo json_encode(array("response"=>"invalid"));
  }
  //var_dump($_POST);
  //var_dump($request);
  foreach($_POST as $k=>$v){
     unset($_POST[$v]);
  }
  unset($request);
  unset($_POST);
?>
