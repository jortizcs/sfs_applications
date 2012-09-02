<!DOCTYPE HTML>
<html>
<head>
<script src="http://yui.yahooapis.com/3.6.0/build/yui/yui-min.js"></script>
<script src="./build/wireit-loader/wireit-loader.js"></script>
<script>YUI_config.groups.wireit.base = './build/';</script>
<script>
  YUI().use('arrow-wire', 'container', function (Y) {
    var layerEl=Y.once('#layer');
    c1 = new Y.Container({
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
  $sfs = new SFSConnection();
  $sfs->setStreamFSInfo("ec2-184-169-204-224.us-west-1.compute.amazonaws.com",8080);
  echo $sfs->getSFSTime();
  echo 'ok';
  $json= get("ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080");
  echo gettype($json);
  $json=json_decode($json);
  var_dump($json);
  if(isset($_GET['graph'])){
    //Allow JS to talk to php with ajax via a get or post request
    echo 'getting graph';
  }
?>
</p>
<div style="position: relative; width: 300px; height: 200px;" id="layer"></div>
</body>
</html>
