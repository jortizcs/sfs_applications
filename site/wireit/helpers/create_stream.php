<?php
  require_once('../header_text_plain.php');
  require_once('../sfslib.php');
  require_once('../curl_ops.php');
  require_once('../constants.php');
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo(CUR_HOST,8080);
  function create_stream($path,$stream){
    global $sfs;
    $reply=$sfs->mkrsrc($path,$stream,TYPE_GENERIC_PUBLISHER);
    return $reply;
  }
  function create_resource($path,$stream,$type){
    global $sfs;
    $reply=$sfs->mkrsrc($path,$stream, $type);
    return $reply;
  }
  if(isset($_POST['new_stream']) && isset($_POST['parent'])){
    $new_stream=$_POST['new_stream'];
    $parent = ($_POST['parent'] == "") ? "" : $_POST['parent'];
    $relative=$parent.$new_stream;
    //echo gettype($parent);
    //echo gettype($new_stream);
    if($sfs->exists($relative)){
      echo "stream exists";
    } else {
      echo $parent.$new_stream;
      echo $new_stream;
      $response=create_stream($parent,$new_stream);
      //die("creating");
      if($response){
        echo $response;
      }else{
        echo "error";
      }
    }
  } else {
    echo "error2";
    #$host  = $_SERVER['HTTP_HOST'];
    #$uri   = rtrim(dirname($_SERVER['PHP_SELF']), '/\\');
    //header("Location: http://$host/wireit");
    //exit;
  }
?>

