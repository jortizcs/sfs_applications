<?php
require_once('../curl_ops.php');
require_once('../constants.php');
/*Put this in sfs lib*/
function create_process(){
  $request=array();
  $request['operaton']=TYPE_PROC;
  $request['name']=$name;
  $request['script']=array();
  global $host, $port;	
  $url = "http://".$host.":".$port.$path;
  if(count($request)>0){
    $reply = put(json_encode($request), $url);
    return $reply;
  }
  return 0;
}
?>
