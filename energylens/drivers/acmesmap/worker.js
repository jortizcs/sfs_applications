var backgrounder = require('backgrounder');
var http = require('http');
var total_data = "";

//var sfshost = "http://ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080";
//var host = "ec2-184-169-204-224.us-west-1.compute.amazonaws.com";
//var port = 8080;
var sfshost = "http://energylens.sfsdev.is4server.com:8081";
var host = "energylens.sfsdev.is4server.com";
var port = 8080;
var rtpath = "/jorge";
var iteration = 0;

var acmesinfo=new Object();
var acmesinfo_star = null;
var smapreport = null;

var HTTP_REQUEST_TIMEOUT = 1000;
function sendit(infokey, smapdatapt){
    //var infokey = acmeid + "/" + acmestreamname;
    var mpath = rtpath + "/acme" + infokey + "?type=generic&pubid="+ acmesinfo[infokey];
    var options = {
                    "host":host,
                    "port":port,
                    "path":mpath,
                    "method":'POST'
                    };
    var req = http.request(options, function(res) {
                    if(res.statusCode!=200){
                        console.log("NON 200 status Code: " + res.statusCode);
                    } else {
                        console.log("POST http://" + options.host + ":" + options.port + options.path + 
                            "\tdata=" + JSON.stringify(data));
                    }
                    iteration +=1;
                    forwardit(function(m){});
                });
    req.headers['connection']='close';
    req.setTimeout(HTTP_REQUEST_TIMEOUT);
    req.on('error', function(e){
                        console.log('problem with request: ' + e.message);
                        //process.exit(1);
                    });
    //smap reading: [[-10000, 155331, 4221], [0, 155387, 4221]]
    //timestamp, value, seqno, we only want the value
    var data = {"value":smapdatapt.Readings[0][1]};
    req.write(JSON.stringify(data) + '\n');
    req.end();
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

function sfs_setprops(mpath, props){
    var options = {
                    "host":host,
                    "port":port,
                    "path":mpath,
                    "method":'PUT'
                    };
    var req = http.request(options, function(res) {});
    req.on('error', function(e){
                        console.log('problem with request: ' + e.message);
                    });
    var data = {
                "operation":"update_properties",
                "properties":props
                };
    req.write(JSON.stringify(data) + '\n');
    req.end();
}

function makestreamfile(path, callback){
    console.log("\tmakestreamfile.path=" + path);
    var pieces = path.split("/");
    var ppath = "";
    for(i=1; i<pieces.length-1; i++){
        ppath= ppath + "/" + pieces[i];
    }
    console.log("\tmakestreamfile.ppath=" + ppath);
    var fn = pieces[pieces.length-1];;
    
    var options = {
                    "host":host,
                    "port":port,
                    "path":ppath,
                    "method":'PUT'
                    };
    var req = http.request(options, function(res) {
                            console.log("statuCode="+ res.statusCode);
                            if(res.statusCode==201){
                                callback(true);
                            } else {
                                callback(false);
                            }
                        });
    req.on('error', function(e){
                        callback(false);
                        console.log('problem with request: ' + e.message);
                    });
    var data = {
                "operation":"create_generic_publisher",
                "resourceName":fn
                };

    console.log(JSON.stringify(data));
    req.write(JSON.stringify(data) + '\n');
    req.end();
}

function makedefaultfile(path, callback){
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
                        callback(false);
                        console.log('problem with request: ' + e.message);
                    });
    var data = {
                "operation":"create_resource",
                "resourceName":fn,
                "resourceType":"default"
                };
    req.write(JSON.stringify(data) + '\n');
    req.end();
}

//acmepath does not exist, neither does acmestreampath
function seq_nofile(infokey, acmepath, acmestreampath, data, callback){
    makedefaultfile(acmepath, 
        function(ok_){
            if(ok_){
                sfs_fileexists(acmestreampath,
                    function(v2){
                    if(v2!=null){
                        //it exists, get the pubid
                        sfs_getpubid(acmestreampath, 
                            function(pubid){
                                if(pubid!=null){
                                    acmesinfo[infokey]=pubid;
                                    console.log("acmesinfo[" + infokey + "]=" + pubid);
                                    callback({"status":"forwarded"});
                                } else {
                                    callback({"status":"failed_2"});
                                }
                            });
                    } else {
                        // create the stream
                        seq_nostream(infokey, acmepath, acmestreampath, data, callback);
                    }            
                });
            } else {
                callback({"status":"failed_3"});
            }
        });
}

//acmepath exists, but acmestreampath DOES NOT
function seq_nostream(infokey, acmepath, acmestreampath, data, callback){
    makestreamfile(acmestreampath, 
        function(ok){
            if(ok==true){
                sfs_getpubid(acmestreampath, 
                    function(pubid){
                        if(pubid!=null){
                            acmesinfo[infokey]=pubid;
                            sendit(infokey, data);
                            //iteration +=1;
                            //forwardit(function(m){});
                            
                            console.log("acmesinfo[" + infokey + "]=" + pubid);
                            callback({"status":"forwarded"});
                        } else {
                            callback({"status":"failed_4"});
                        }
                    });
                if(acmestreampath.indexOf("energy")>0){
                    sfs_setprops(acmestreampath,{"units":"mWh"});
                } else {
                    sfs_setprops(acmestreampath,{"units":"mW"});
                }
            } else {
                console.log("failed_5");
                callback({"status":"failed_5"});
            }
    });
}

//acmepath exists, but acmestreampath MAY NOT
function seq_filenostream(infokey, acmepath, acmestreampath, data, callback){
    sfs_fileexists(acmestreampath,
        function(v2){
            if(v2!=null){
                //it exists, get the pubid
                sfs_getpubid(acmestreampath, 
                    function(pubid){
                        if(pubid!=null){
                            acmesinfo[infokey]=pubid;
                            //sendit(infokey, data);
                            //iteration +=1;
                            forwardit(function(m){});
                            
                            /*if(acmestreampath.indexOf("energy")>0){
                                sfs_setprops(acmestreampath,{"units":"mWh"});
                            } else {
                                sfs_setprops(acmestreampath,{"units":"mW"});
                            }*/
                            
                            console.log("acmesinfo[" + infokey + "]=" + pubid);
                            callback({"status":"forwarded"});
                        } else {
                            callback({"status":"failed_1"});
                        }
                    });
            } else {
                // create the stream
                seq_nostream(infokey, acmepath, acmestreampath, data, callback);            
            }            
        });
}

//takes a smapreport and creates the necessary channels
//in streamfs; this is a sample smap report:
//{"/fitpc_acmes/8a0/true_power": {"uuid": "f0083ced-89c4-5fcc-ab54-898217b8126a", "Readings": [[-10000, 20060, 4321], [0, 20109, 4321]]}, "/fitpc_acmes/8a0/true_energy": {"uuid": "61ec26e3-badd-5d3c-8a1f-e614503c7106", "Readings": [[-10000, 166488, 4321], [0, 166543, 4321]]}, "/fitpc_acmes/8a0/apparent_power": {"uuid": "ef5f2399-1184-50e8-a51e-9d71d84b1ccf", "Readings": [[-10000, 31441, 4321], [0, 31536, 4321]]}}
function forwardit(callback){
    var keys = Object.keys(smapreport);
    if(iteration<keys.length){
        console.log("iteration: " + (iteration+1) + " of " + keys.length);
        var i = iteration;
        //    /fitpc_acmes/8a0/true_power; the first empty string is counted
        var pieces = String(keys[i]).replace(/^\s+|\s+$/g, '').split('/');
        var acmeid = parseInt(pieces[2],16);
        var streamname = pieces[3];
        console.log("acmeid=" + acmeid + ", stream_name=" + streamname);

        var infokey = acmeid + "/" + streamname;
        var infokey2 = rtpath + "/acme" + infokey;
        var infokey3 = infokey2 + "/";

        //lookup the pubid from the star lookup
        if(typeof acmesinfo[infokey] == "undefined" && 
            (typeof acmesinfo_star[infokey2] != "undefined" ||
            typeof acmesinfo_star[infokey3] != "undefined")){
            if(typeof acmesinfo_star[infokey3] != "undefined"){
                acmesinfo[infokey] = acmesinfo_star[infokey3].pubid;
            } else {
                acmesinfo[infokey] = acmesinfo_star[infokey2].pubid;
            }
        }


        if(typeof acmesinfo[infokey] == "undefined"){
            
            var acmepath = rtpath + "/acme" + acmeid;
            var acmestreampath = rtpath + "/acme" + infokey;

            //check the acme folder exists, if not, make it
            sfs_fileexists(acmepath, 
                function(v){
                    //check if stream exists, if not, make it
                    if(v !=null){
                        seq_filenostream(infokey, acmepath, acmestreampath, 
                                            smapreport[keys[i]], function(m){
                                            });
                        console.log("CALL::seq_filenostream(infokey="+infokey+", acmepath=" + 
                                        acmepath + ", acmestreampath=" + acmestreampath +
                                        ", data=" + JSON.stringify(smapreport[keys[i]]) + ")")
                    } else {
                        seq_nofile(infokey, acmepath, acmestreampath, 
                                    smapreport[keys[i]], function(m){});
                        console.log("CALL::seq_nofile(infokey="+infokey+", acmepath=" + 
                                        acmepath + ", acmestreampath=" + acmestreampath +
                                        ", data=" + JSON.stringify(smapreport[keys[i]]) + ");");
                    }
                });
        } else {

            console.log("acmesinfo[" + infokey+"]=" + acmesinfo[infokey]);
            sendit(infokey, smapreport[keys[i]]);
            //iteration +=1;
            //forwardit(function(m){});
        }
    } else {
        callback({"status":"done"});
    }
}

process.on('message', 
    function(message, callback){
        if(message.op == "start"){
            started = true;
            acmesinfo = message.acmesinfo;
            acmesinfo_star = message.acmesinfo_star;
            smapreport = message.smapreport;
            forwardit(function(status_){
                    console.log("forwardit.status=" + JSON.stringify(status_));
                    console.log("acmesinfo=" + JSON.stringify(acmesinfo));
                    process.send({"acmesinfo":acmesinfo});
                });
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
  
