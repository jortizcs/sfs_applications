<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd"> 
<html> 

 <head> 
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"> 
    <title>Historical data service</title> 
    <link href="layout.css" rel="stylesheet" type="text/css"> 
    <!--[if lte IE 8]><script language="javascript" type="text/javascript" src="../excanvas.min.js"></script><![endif]--> 
    <script language="javascript" type="text/javascript" src="lib/jquery-1.6.1.min.js"></script> 
    <script language="javascript" type="text/javascript" src="lib/flot/jquery.flot.min.js"></script> 
 </head> 
    <body>
	<table>
		<tr><td>
			<table border="0">
			<tr><td><a href="">week</a></td></tr>
			<tr><td><a href="">day</a></td></tr>
			<tr><td><a href="">hour</a></td></tr>
			<tr><td><a href="">minute</a></td></tr>
			<tr><td><a href="">live</a></td></tr>
			</table>
		</td>
		<td>
			<img src="watts_label.png" width="25" height="70" />
		</td>
		<td>
			<label for="placeholder" id="iname"></label>
    			<div id="placeholder" style="width:600px;height:300px;"></div> 
		</td>
		<!--<td align="right">
			<label id="e_frac">X %</label> of total energy<br>
		</td> -->
		</tr>
	</table>

<script type="text/javascript"> 

//calibration constants
var coeffs= new Array();
coeffs['311']=20.45;
coeffs['361']=24.99;
coeffs['387']=22.42;
coeffs['352']=26.45;
coeffs['312']=22.91;
coeffs['351']=22.63;
coeffs['359']=25.46;
coeffs['326']=21.71;
coeffs['325']=25.76;
coeffs['354']=21.75;
coeffs['318']=22.67;
coeffs['385']=24.49;
coeffs['336']=21.52;
coeffs['323']=23.15;
coeffs['349']=23.65;

coeffs['123']=1;

function qsfs_callback(data){
	//r = eval('(' + data + ')');
	var r = JSON.parse(data);
	series = r.frame;
	series2=[];
	for(i=0; i<series.length; ++i){
		var tval = (series[i][1])/coeffs[r.item_name];
		series2.push([ (series[i][0]-25200)*1000, series[i][1] ]);
	}
	options = new Object();
	options.xaxis = new Object();
	options.xaxis.mode="time";
	options.xaxis.timeformat="%h:%M:%S %p";
	document.getElementById("iname").innerHTML="<center><h3>"+r.item_name+"</h3></center>";
	$.plot($("#placeholder"), [ series2 ], options);
}

$(function () {
	var request = new Object();
	request.sfs_host=document.getElementById("sfs_host").value;
	request.sfs_port=document.getElementById("sfs_port").value;
	request.item_path=document.getElementById("path").value;
	jQuery.post("querysfs.php", request, qsfs_callback);

});
</script>
<?php
$sfs_host=$_GET["sfs_host"];
$sfs_port=$_GET["sfs_port"];
$sfs_itempath = $_GET["item_path"];
echo "<input type=\"hidden\" id=\"sfs_host\" value=\"".$sfs_host."\">";
echo "<input type=\"hidden\" id=\"sfs_port\" value=".$sfs_port.">";
echo "<input type=\"hidden\" id=\"path\" value=\"".$sfs_itempath."\">";
?>

</body> 
</html> 

