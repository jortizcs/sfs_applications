<?php
define('SFS_PROD','energylens.sfsprod.is4server.com');
define('TEST_SITE','ec2-184-169-204-224.us-west-1.compute.amazonaws.com');
if(False){
  define('CUR_HOST',SFS_PROD);
} else {
  define('CUR_HOST',TEST_SITE);
}
const TYPE_GENERIC_PUBLISHER="genpub";
const TYPE_DEVICE="device";
const TYPE_DEVICES="devices";
const TYPE_DEFAULT="default";
const TYPE_PROC="save_proc";
const TYPE_UPDATE="overwrite_properties";

?>
