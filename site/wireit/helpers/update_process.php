<?php
  require_once('../sfslib.php');
  require_once('../curl_ops.php');
  require_once('../constants.php');
  const TYPE_GENERIC_PUBLISHER="genpub";
  const TYPE_DEVICE="device";
  const TYPE_DEVICES="devices";
  const TYPE_DEFAULT="default";
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo(CUR_HOST,8080);
  function loadPropsToForm($path){
    global $sfs;
    global $host, $port;
    if(substr($path,0,1) != "/"){
        $path = "/$path";
    }
    $url = "http://$host:$port$path";
    $response=get($url);
    $response_array=json_decode(get($url),true);
    var_dump($response_array);
    if(count($response_array) > 0){
      echo '<form method="post">';
    }
    echo "$path:<textarea name=\"$path\">$response</textarea></br>";
    foreach($response_array as $prop=>$value){
      if(strlen($value) > 10){
        echo "$prop:<textarea name=\"$prop\">$value</textarea></br>";
      } else {
        echo "$prop:<input type=\"text\" name=\"$prop\" value=\"$value\"></input></br>";
      }
    }

    if(count($response_array) > 0){
      echo '<input type="submit" name="Update" value="update"></submit">';
      echo '</form>';
    }

    if($response_array){
      return $response_array;
    }else{
      echo "error";
    }
  } /*end defintion of loadPropsToForm*/
  if(isset($_POST['update_proc'])){ 
      $proc=$_POST['update_proc'];
      if(substr($proc,0,1) != "/"){
        $proc= "/proc/$proc";
      }
      if($sfs->exists($proc)){
        loadPropsToForm($proc);
      } else {
        echo "process not found";
      }
   } else {
     echo "unexpected error";
   }
?>

