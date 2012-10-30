<?php
  require_once('../sfslib.php');
  require_once('../curl_ops.php');
  const TYPE_GENERIC_PUBLISHER="genpub";
  const TYPE_DEVICE="device";
  const TYPE_DEVICES="devices";
  const TYPE_DEFAULT="default";
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo("ec2-184-169-204-224.us-west-1.compute.amazonaws.com",8080);

  function stop_process($path){
    global $sfs;
    if(substr($path,0,1) != "/"){
        $path = "/$path";
    }
    if($sfs->exists($path)){
      global $host, $port;	
      $url = "http://$host:$port$path";
      $response=delete($url);
      echo $response;
      if($response){
        echo "response";
      }else{
        //response is empty even though successful deletion
        echo "what";
      }
    } else {
      echo "path does not exist";
      return False;
    }
  }
  if (isset($_POST['stop'])){
    stop_process($_POST['stop']);
  }
?>

