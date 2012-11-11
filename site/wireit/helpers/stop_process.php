<?php
  require_once('../sfslib.php');
  require_once('../curl_ops.php');
  require_once('../constants.php');
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo(CUR_HOST,8080);

  function stop_process($path){
    global $sfs;
    if(substr($path,0,1) != "/"){
        $path = "/$path";
    }
    global $host, $port;	
    $url = "http://$host:$port$path";
    echo $url;
    if($sfs->exists($path)){
      $response=$sfs->destroyResource($path);
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

