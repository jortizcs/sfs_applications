<!DOCTYPE HTML>
<html>
<head>
<script src="http://yui.yahooapis.com/3.6.0/build/yui/yui-min.js"></script>
<script src="./build/wireit-loader/wireit-loader.js"></script>
<script>YUI_config.groups.wireit.base = './build/';</script>
<script>
  YUI().use('arrow-wire', 'container', function (Y) {
    var layerEl=Y.once('#layer');
    new Y.Container({
    children: [
      /*terminal points*/
      //{ align: {points:["tl", "tl"]} },
      { align: {points:["tl", "bc"]} }
    ],
    render: layerEl,
    xy: [200,100],
    width: 200,
    height: 100,
    headerContent: 'headerContent',
    bodyContent: 'bodyContent',
    footerContent: 'footerContent'
    }); 
    /*arrow graphics*/
    var mygraphic = new Y.Graphic({render: "#layer"});
    var wire = mygraphic.addShape({
    type: Y.ArrowWire,
    stroke: {
    weight: 4,
    color: "rgb(173,216,230)" 
    },
    src: {
    getXY: function() {
    return [30,60];
      }
    },
    tgt: {
      getXY: function() {
      return [250,150];
      }
     }
    });

    });
</script>
</head>
<body>
<p>
<?php
  require_once("sfslib.php");
  //keep a reference to sub object
  //encapsulate host and port
  function isSourcePath($path=null,$sfs_obj=null){
    global $host,$port;
    $home_path="http://".$host.":".$port;
    $subs=get("http://".$host.":".$port."/sub");  
    $subs=json_decode($subs,true); 
    foreach($subs["children"] as $index=>$child_id){
      echo $child_id;
      $child=get($home_path."/sub/".$child_id);
      $child=json_decode($child,true);
      if(isset($child["sourcePath"])){
      var_dump($child["sourcePath"]);
      } else if (isset($child["sourcePaths"])){
      var_dump($child["sourcePaths"]);
      }
    }
  }
  function createSubscription(){

  }
  function createOutputStream(){
  }
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo("ec2-184-169-204-224.us-west-1.compute.amazonaws.com",8080);
  echo $sfs->getSFSTime();
  echo $host;
  echo $port;
  isSourcePath();
  $json= get("ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080");
  #read all subs to build nodes and destination
  $json=get("energylens.sfsprod.is4server.com:8080/sub/8-18289");
  $node=json_decode($json, true);
  var_dump($node);
  $srcArr=$node["sourcePaths"];
  $dest=$node["destination"];
  //insert rescursive defn to build other nodes
  var_dump($srcArr);
  foreach($srcArr as $index=>$value){
    echo $srcArr[$index];
  }
  if(isset($_GET['graph'])){
    //Allow JS to talk to php with ajax via a get or post request
    echo 'getting graph';
  }
?>
</p>
<div style="position: relative; width: 300px; height: 200px;" id="layer"></div>
</body>
</html>
