<?php
  require_once('../header_text_plain.php');
  require_once('../sfslib.php');
  require_once('../curl_ops.php');
  require_once('../constants.php');
  require_once('../config.php');
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo(CUR_HOST,8080);
  function return_json($path){
    global $sfs;
    global $host, $port;
    if(substr($path,0,1) != "/"){
        $path = "/$path";
    }
    $url = "http://$host:$port$path";
    $response=get($url);
    //echo $response."<br><br><br>";
    //var_dump($response);
    //echo "<br>";
    //return json_encode(json_decode($response,true));
    return (json_encode($response));

  }
  function update_process($request){
    /* only updates scritp right now */
    if(!isset($request["func"])){
      return json_encode(array("status"=>"fail", "errors"=> array("script update not provided")));
    }
    global $sfs, $host, $port;
    $req = array();
    $req["operation"] = TYPE_UPDATE;
    $req["script"] = array();
    $req["script"]["func"]=$req["func"];
    $url  =  "$host:$post/proc/".$req["update_proc"];
    $results = post($url,$req);
    return json_encode($results);
  }
  function loadPropsToForm($path){
    global $sfs;
    global $host, $port;
    if(substr($path,0,1) != "/"){
        $path = "/$path";
    }
    $url = "http://$host:$port$path";
    $response=get($url);
    $response_array=json_decode(get($url),true);
    //var_dump($response_array);
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
      //die($proc);
      if($sfs->exists($proc)){
        if (isset($_POST['func'])){
          $request['path']=$proc;
          $request['func']=$_POST['func'];
          echo update_process($request);
        } else {
          echo return_json($proc);
        }
      } else {
        echo json_encode(array("status"=>"fail", "errors"=>array("$proc not found")));
      }
   } 
?>

