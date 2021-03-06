<!DOCTYPE HTML>
<html>
<head>
<script src="http://yui.yahooapis.com/3.6.0/build/yui/yui-min.js"></script>
<script src="./build/wireit-loader/wireit-loader.js"></script>
<script>YUI_config.groups.wireit.base = './build/';</script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js" type="text/javascript"></script>
<script src="./beautify.js" type="text/javascript"></script>
<script src="./js/sfsapp.js"></script>
<link rel="stylesheet" href="./css/sfsapp_style.css" type="text/css"/>
</head>
<body>
<div id="ui-message-bar" class="js-message-bar"></div>
<div>
<form method="post" action="./helpers/create_stream.php">
<label for="parent">Parent(Leave blank top level)</label>
<input id="parent" type="text" name="parent" placeholder=""></input>
<label for="new_stream">New Resource</label>
<input id="new_stream"type="text" name="new_stream" placeholder="Relative Path to stream"></input>
<button value="Create Resource" name="Create Resource">Create Resource</button>
</form>
<form method="post" action="./helpers/stop_process.php">
<label for="stop">Stop Stream</label>
<input name="stop" type="text" placeholder="Relative Path to stream">
</input>
<button value="Destroy Process" name="Destroy Process">Destroy Process</button>
</form>
<form id="update_proc_form" method="post" action="./helpers/update_process.php">
<label for="update_proc">Update Process</label>
<!--<input id="update_proc" name="update_proc" placeholder="proc name"></input>-->
<select name="update_proc">
<?php 
require_once('config.php');
require_once('sfslib.php');
require_once('constants.php');
$sfs = new SFSConnection();
$sfs->setStreamFSInfo(CUR_HOST,8080);

global $host, $port;

$url = "http://$host:$port/proc";
$proc_arr=json_decode(get($url),true);
foreach($proc_arr['children'] as $key=>$value){
?>
  <option><?php echo $value ?></option>
<?php
}
?>
</select>
<button id="btn-load-proc" value="Load Process">Load Process</button>
<div id="dynamic_update_proc_area"><!--loaded with js data--></div>
<button value="Update Process" name="Update Process">Update Process</button>
</form>
<form id="create_proc_form"style="position:relative;"method="post" action="./helpers/create_process.php">
<label for="create_proc">Create Process</label>
<input id="create_proc" name="create_proc" placeholder="proc name"></input>
<fieldset>
  <legend>Script</legend>
  <label for="winsize">winsize</label>
  <input id="winsize" name="winsize" placeholder="10"></input>
  <label for="materialize">materialize</label>
  <select id="materialize" name="materialize">
    <option value="true">True</option>
    <option value="false">False</option>
  <select>  
  <label for="timeout">Timeout(ms)</label>
  <input type="text" name="timeout" id="timeout" placeholder="20000"></input>
  </br>
  <label for="editor">Function</label>
  <div style="position:relative;width:100%; min-height:200px;">
  <div id="editor" class="ui-editor js-editor">function foo(buffer,state) {
      var outObj = new Object();
      outObj.tag = "processed";
      return outObj;
  }</div>
  </div>
      
</fieldset>
<button value="Create Process" name="Create Process">Create Process</button>
</form>
<form id = "create_subscription" method="Post" action="./helpers/create_subscription.php">
<label for="path"> Path </label>
<input name="path"placeholder=" /temp/stream1" id="path" type="text"></input>
<label for="target">Target</label>
<input name="target"placeholder="/temp/stream2" id="path" type="text"></input>
<button>Create Subscription</button>
</form>

</div>
<script src="https://d1n0x3qji82z53.cloudfront.net/src-min-noconflict/ace.js" type="text/javascript" charset="utf-8"></script>
<script>
    $(document).ready(function(){
      var editor = ace.edit("editor");
      editor.setTheme("ace/theme/monokai");
      editor.getSession().setMode("ace/mode/javascript");
    });
</script>

<?php
  //keep a reference to sub object
  //encapsulate host and port
  function isSourcePath($path=null,$sfs_obj=null){
    global $host,$port;
    $home_path="http://".$host.":".$port;
    $subs=get("http://".$host.":".$port."/sub");  
    $subs=json_decode($subs,true); 
    foreach($subs["children"] as $index=>$child_id){
      $child=get($home_path."/sub/".$child_id);
      $child=json_decode($child,true);
      if(isset($child["sourcePath"])){
      //var_dump($child["sourcePath"]);
        if($path == $child["sourcePath"]){
          return true;
        }
      } else if (isset($child["sourcePaths"])){
        foreach($child["sourcePaths"] as $key=>$value){
          if ($path == $value){
            return true;
          }
        }
      }
    }
  }
  function mapAllSourcePaths($fn){
    global $host,$port;
    $home_path="http://".$host.":".$port;
    $subs=get("http://".$host.":".$port."/sub");  
    $subs=json_decode($subs,true); 

    foreach($subs["children"] as $index=>$child_id){
      $child=get($home_path."/sub/".$child_id);
      $child=json_decode($child,true);
      if(isset($child["sourcePath"])){
            $fn($child["sourcePath"]);
      } else if (isset($child["sourcePaths"])){
        foreach($child["sourcePaths"] as $key=>$value){
            $fn($path);
        }
      }
    }
  }
  function makeHtmlGraph($src_path){
    $str = <<<EOD
      <div class="sfs_sub_container" title="$src_path">Source Path:$src_path</div>
EOD;
    echo $str;
  }
  function getAllStreams($type='json'){
    global $host,$port;
    $json=array();
    $home_path="http://".$host.":".$port;
    $subs=get("http://".$host.":".$port."/sub");  
    $subs=json_decode($subs,true); 
    $json["paths"]=array();

    foreach($subs["children"] as $index=>$child_id){
      $child=get($home_path."/sub/".$child_id);
      $child=json_decode($child,true);
      if(isset($child["sourcePath"])){
            $json["paths"][]=stripslashes($child["sourcePath"]);
      } else if (isset($child["sourcePaths"])){
        foreach($child["sourcePaths"] as $key=>$value){
            $json["paths"][]=stripslashes($value);
        }
      }
    }
    //figure out how to json_encode without slashes
    return json_encode($json,JSON_FORCE_OBJECT);
  }
  function getAllSubscriptions(){
    global $host,$port;
    $json=array();
    $home_path="http://".$host.":".$port;
    $subs=get("http://".$host.":".$port."/sub");  
    $subs=json_decode($subs,true); 
    return json_encode($subs,JSON_FORCE_OBJECT);
  }
  //funcitons to use ajax to talk to
  // getCurrentSubscriptionForStream();
  // removeSubscriptonFromStream 
  // addSubscriptionToStream;
  //edn
  function generateAllProcesses(){
  }
  function generateAllGraphs(){
    mapAllSourcePaths(makeHtmlGraph);
  }
  function createSubscription(){

  }
  function createOutputStream(){
  }
  //initialization
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo("ec2-184-169-204-224.us-west-1.compute.amazonaws.com",8080);
  $json= get("ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080");
  #read all subs to build nodes and destination
  //$json=get("energylens.sfsprod.is4server.com:8080/sub/8-18289");
  /*$node=json_decode($json, true);
  //var_dump($node);
  $srcArr=$node["sourcePaths"];
  $dest=$node["destination"];
  //insert rescursive defn to build other nodes
  //var_dump($srcArr);
  foreach($srcArr as $index=>$value){
    if($srcArr[$index]){
      //echo $srcArr[$index];
    }
  }
  if(isset($_GET['graph'])){
    echo getAllStreams(); 
  }*/
?>

<!--<div style="position: relative; width: 300px; height: 200px;" id="layer"></div>-->
<!--<script>
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
    //Subscription containers
    var allSubContainers=Y.all('.sfs_sub_container');
    //Graphs all processes
    a= allSubContainers;
    /*a.each(function(e){
      new Y.Container({
        render: layerEl,
        footerContent:'footerContent',
        bodyContent:'blah',
        headerContent: 'Stream Data',
        width :200,
        height:100,
        xy:[Math.random()*window.innerWidth, Math.random()*window.innerHeight]
      });
    });
    */
    //graph all stream
    //end callback
    });
</script>-->
</body>
</html>
