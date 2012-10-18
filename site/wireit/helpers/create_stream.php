<?php
  require_once('../sfslib.php');
  require_once('../curl_ops.php');
  const TYPE_GENERIC_PUBLISHER="genpub";
  const TYPE_DEVICE="device";
  const TYPE_DEVICES="devices";
  const TYPE_DEFAULT="default";
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo("ec2-184-169-204-224.us-west-1.compute.amazonaws.com",8080);
  echo $sfs->getSFSTime();
  function create_stream($path,$stream){
    global $sfs;
    $sfs->mkrsrc($path,$stream,TYPE_GENERIC_PUBLISHER);
  }
  function create_resource($path,$stream,$type){
    global $sfs;
    $sfs->mkrsrc($path,$stream, $type);
  }
?>

