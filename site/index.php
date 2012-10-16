<?php include_once ("sfslib.php") ?>
<!DOCTYPE html>
<html>
<head>
<title>Energy Lens WireIt Graph</title>
<script src="http://yui.yahooapis.com/3.6.0/build/yui/yui-min.js"></script>
<script src="./build/wireit-loader/wireit-loader.js"></script>
<script>YUI_config.groups.wireit.base = './build/';</script>
<script>
YUI().use('arrow-wire', function(Y) {
  
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
  $sfs= new SFSConnection();
  $sfs->setStreamFSInfo("ec2-184-169-204-224.us-west-1.compute.amazonaws.com",8080);
  echo $sfs->getSFSTime();
  echo htmlentities("&;");
?>
</p>
<div style="position: relative; width: 300px; height: 200px;" id="layer"></div>
</body>
</html>
