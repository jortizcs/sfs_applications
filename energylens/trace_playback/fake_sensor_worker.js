var http = require('http');
var fs = require('fs');
var backgrounder = require("backgrounder");

var sfshost = "http://ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080";
var host = "ec2-184-169-204-224.us-west-1.compute.amazonaws.com";
var port = 8080;
var mpath = null;
var pubid = null;

var started=false;

//data point index
var dpidx = 0;
var dpsz = 100;
var dpbuf = null;

function readit(){
    fs.readFile('todai_desktop_power_trace.json', 'ascii',
        function(err, data){
            if(err){
                console.error("could not open file: %s", err);
                process.exit(1);
            }
            var dataobj = JSON.parse(data);
            trace_data = dataobj.ts_query_results;
            if(dpidx+dpsz < trace_data.length){
                dpbuf = trace_data.slice(dpidx, dpidx+dpsz);
            } else {
                dpbuf = trace_data.slice(dpidx, trace_data.length);
                for(i=0; i<dpsz-(trace_data.length-dpidx); i++){
                    dpbuf.push(trace_data[i]);
                }
            }
            //dpidx = dpidx+dpsz % trace_data.length;
            sendit();
        });
}

function sendit(){
    var options = {
                    "host":host,
                    "port":port,
                    "path":mpath + "?type=generic&pubid="+pubid,
                    "method":'POST'
                    };
    var req = http.request(options, function(res) {});
    req.on('error', function(e){
                        console.log('problem with request: ' + e.message);
                    });
    var datapt = dpbuf[dpidx];
    var data = {"value":datapt.value};
    req.write(JSON.stringify(data) + '\n');
    req.end();

    console.log("POST http://" + options.host + ":" + options.port + options.path + "\tdata=" + JSON.stringify(data));

    //schedule the next one
    dpidx = dpidx+1;
    var diff=-1;
    if(dpidx<dpidx+dpsz){
        diff = dpbuf[dpidx].ts-datapt.ts;
        console.log("Scheduling next sent in " + diff + " seconds");
        if(diff>0){
            setTimeout(sendit, diff*1000);
        }
    } else {
        diff = datapt.ts-dpbuf[dpidx-2].ts;
        if(diff>0){
            setTimeout(readit, diff*1000);
        }
    }
}

function sfs_fileexists(path, callback){
    http.get(sfshost + path, function(res){
            if(res.statusCode != 404){
                var res = {"path":path, "exists":true};
                callback(res);
            } else {
                callback(null);
            }
        }).on('error', 
            function(e){
                callback(null);
            });
}

function sfs_getpubid(path, callback){
    http.get(sfshost + path, function(res){
            res.on('data', function(chunk){
                var resObj = JSON.parse(chunk);
                if(typeof resObj.pubid != "undefined"){
                    callback(resObj.pubid);
                    pubid = resObj.pubid;
                } else {
                    callback(null);
                }
            });
        }).on('error', 
            function(e){
                callback(null);
            });
}

function makestreamfile(path, callback){
    var pieces = path.split("/");
    var ppath = "";
    for(i=0; i<pieces.length-1; i++){
        ppath="/" + pieces[i];
    }
    var fn = pieces[pieces.length-1];;
    
    var options = {
                    "host":host,
                    "port":port,
                    "path":ppath,
                    "method":'PUT'
                    };
    var req = http.request(options, function(res) {
                            if(res.statusCode==201){
                                callback(true);
                            } else {
                                callback(false);
                            }
                        });
    req.on('error', function(e){
                        console.log('problem with request: ' + e.message);
                    });
    var data = {
                "operation":"create_generic_publisher",
                "resourceName":fn
                };
    req.write(JSON.stringify(data) + '\n');
    req.end();
}

function init(path){
    console.log("Initing path=" + path);
    mpath = path;
    sfs_fileexists(path, function(res){
                    if(res ==null){
                        makestreamfile(path, function(res2){
                            if(res2==true){
                                console.log("created " + path + " successfully");
                                sfs_getpubid(path, function(res3){
                                        if(res3 !=null){
                                            readit();
                                        }
                                    });
                            }
                        });
                    } else {
                        sfs_getpubid(path, function(res){
                                        if(res !=null){
                                            readit();
                                        }
                                    });
                    }
                });
}

process.on('message', 
    function(message, callback){
        if(message.op == "start" && !started){
            started = true;
            init(message.path);
        } else if(message.op == "change_sfshost"){
            if( typeof message.sfshost != "undefined"){
                host = message.host;
                port = message.port;
                sfshost = "http://" + host + ":" + port;
            }
        } else {
            console.log("not cool");
        }
    });
        


