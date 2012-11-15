<?php
require_once('../header_text_plain.php');
require_once('../curl_ops.php');
require_once('../constants.php');
require_once('../sfslib.php');
const PROC_NAME="create_proc";
const OPERATION="operation";
const WINSIZE="winsize";
const MATERIALIZE="materialize";
const FUNC="func";
const TIMEOUT="timeout";
/*Put this in sfs lib*/
$sfs = new SFSConnection();
$sfs->setStreamFSInfo(CUR_HOST,8080);

function create_process($arr){
  $request=array();
  $request['operation']=TYPE_PROC;//$arr[OPERATION];
  $request['name']=$arr[PROC_NAME];
  $request['script']=array();
  $request['script']['winsize']=$arr[WINSIZE];
  $request['script']['func']=$arr[FUNC];
  $request['script']['timeout']=$arr[TIMEOUT];
  $request['script']['materialize']=$arr[MATERIALIZE];
  $path = "/proc";#$arr[PROC_NAME];
  global $host, $port;	
  $url = "http://".$host.":".$port.$path;
  //echo $url;
  //echo "request: ".json_encode($request);
  if(count($request)>0){
    $reply = post(json_encode($request), $url);
    return json_($reply,true);
  }
  return 0;
}
function isProcessCreation($request){
    //script is populated by forntend
    return isset($request[PROC_NAME]) && isset($request[MATERIALIZE]) && isset($request[WINSIZE]) && isset($request[FUNC]) && isset($request[TIMEOUT]);
}
  $request;
  switch($_SERVER['REQUEST_METHOD']){
      case "GET":
      $request=&$_GET;
        break;
      case "POST":
      $request=&$_POST;
        break;
  }
  if(isProcessCreation($request)){
    //create_process($request);
    //var_dump($request);
    $results = create_process($request);
    var_dump($results);
    return;
    if((bool)empty($results) || $results == "\"\""){
      echo "yes";
    }
    if((count($results) == 0) || (bool)empty($results) || $results  == "")  { 
      echo json_encode(array("status"=>"success"));
    } else {
      echo json_encode($results);
    }
  } else {
    echo json_encode(array("status"=>"fail", "errors"=>"make sure all fields are filled"));
  }
  //var_dump($_POST);
  //var_dump($request);
  foreach($_POST as $k=>$v){
     unset($_POST[$v]);
  }
  unset($request);
  unset($_POST);
  //var_dump($_POST);
  //echo "request";
  //var_dump($_request);
  
?>
