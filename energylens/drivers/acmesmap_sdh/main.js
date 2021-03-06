var backgrounder = require('backgrounder');
var http = require('http');
var fs = require('fs');

var total_data="";

var host = "ec2-184-169-204-224.us-west-1.compute.amazonaws.com";
var port = 8080;
var rtpath = "/dev";
var sfshost = "http://" + host + ":" + port;

var acmesinfo = new Object();
var acmesinfo_star = null;

var worker_stack = new Array();

function check_connection(){
    http.get(sfshost, function(res){
            if(res.statusCode != 404){
               //process.kill(1);
                console.log("streamfs server is not up\n");
            }
        }).on('error', 
            function(e){
                //process.kill(1);
                console.log("streamfs server is not up\n");
            });
}

function addToAcmesInfo(obj){
    var keys = Object.keys(obj);
    for(i=0; i<keys.length; i++){
        acmesinfo[keys[i]] = obj[keys[i]];
    }
}

function handle(req, resp){
    req.setEncoding('utf8');
    
    req.on('data', function(chunk) {
        //console.log("Receive_Event::" + chunk);
        total_data += chunk;
    });
    
    req.on('end', function(){
        try {
            var requestObj = JSON.parse(total_data);
            total_data = "";
            if(req.method=='POST'){
                if(typeof requestObj.op == "undefined" && requestObj.op == 'change_sfshost'){
                        host=requestObj.host,
                        port=requestObj.port;
                        rtpath= requestObj.root_path;
                        sfshost = "http://" + host + ":" + port;

                        resp.writeHead(200, {'Content-Type': 'text/json'});
                        resp.end(JSON.stringify(
                            {"msg":"host changed to http://" + 
                                requestObj.host + ":" + requestObj.port + 
                                "\trootPath=" + rtpath}));
                        return;
                }
                else{ //it must be coming from the smap/acme deployment
                    var keys = Object.keys(requestObj);
                    if(keys.length>0){
                        var thiskey = JSON.stringify(keys[0]);
                        if(thiskey.indexOf("/fitpc_acmes")>0){
                            var smapreport = requestObj;
                            console.log(JSON.stringify(requestObj) + "\n");
                            var worker = backgrounder.spawn(__dirname + "/worker.js");
                            worker_stack.push(worker);
                            worker.send({"op":"start","smapreport":requestObj, 
                                        "acmesinfo":acmesinfo,
                                        "acmesinfo_star":acmesinfo_star});
                            worker.on("message", 
                                function(msg){
                                    addToAcmesInfo(msg.acmesinfo);
                                    console.log("terminating worker");
                                    worker.terminate();
                                });
                            setTimeout(terminateNextWorker, 2000);
                            resp.writeHead(200, {'Content-Type': 'text/json'});
                            resp.end("{\"status\":\"success\"}");
                            return;
                        }
                    }
                    console.log("not ok");
                }
            }
        } catch(e){
            console.log(e);
        }
        resp.writeHead(200, {'Content-Type': 'text/json'});
        resp.end("{\"status\":\"success\"}");
    });
}

/*http.get("http://" + host + ":" + port + rtpath + "/*", 
    function(res){
        res.on('data', function(chunk){
            var resObj = JSON.parse(chunk);
            var keys = Object.keys(resObj);
            for(i=0; i<keys.length; i++){
                if(keys[i].indexOf("acme")<0){
                    for(j=0; j<resObj[keys[i]].children.length; j++){
                        var name = resObj[keys[i]].children[j];
                        var id = name.slice(4);
                    }
                }
            }
        });
    }).on('error', 
        function(e){
            console.log(e);
            process.exit(1);
        });
*/

/*var server = http.createServer(handle);
server.listen(1340, host);
console.log("starting server " + host + ":1340");

setInterval(check_connection, 1000*75);*/

var server=null;
var timechunks="";
var servertime = -1;
var respStr = "";
var driver_worker = backgrounder.spawn(__dirname + "/driver_sdh.js");
var terminated = false;

function initwload(){
    console.log("FETCHING: http://" + host + ":" + port + rtpath + "/*");
    http.get("http://" + host + ":" + port + rtpath + "/*", 
        function(res){
            res.on('data', function(chunk){
                    respStr += chunk;
                    if(!terminated){
                        driver_worker.terminate();
                        terminated = true;
                    }
                });
            res.on('end', function(){
                acmesinfo_star = JSON.parse(respStr);
                writeit(acmesinfo_star);
                startserver(); 
            });
        }).on('error', 
            function(e){
                console.log(e);
                //process.exit(1);
            });
}

function writeit(devinfo_){
    var data = {"ts":servertime, "devinfo":devinfo_};
    fs.writeFileSync("./devs.json", JSON.stringify(data), 'utf8');
}

function initwoload(){
    if(!terminated){
        driver_worker.terminate();
        terminated = true;
        console.log("Terminated driver.js\n");
    }
    console.log("LOADING devinfo from file\n");
    var devinfo_ = fs.readFileSync("./devs.json", 'utf8');
    var devinfoObj = JSON.parse(devinfo_);
    acmesinfo_star = devinfoObj.devinfo;
    startserver();
    
}

function startserver(){
    server = http.createServer(handle);
    server.listen(1340, host);
    console.log("starting server " + host + ":1340");
    //setInterval(check_connection, 1000*75)
}

function settime(){
    http.get("http://" + host + ":" + port + "/time",
        function(res){
            res.on('data', function(chunk){
                timechunks += chunk;
            });

            res.on('end', function(){
                console.log("time=" + timechunks);
                servertime = JSON.parse(timechunks).Now;
            }); 
        }    
    );
}

if(fs.existsSync("./devs.json")){
    var devinfo = JSON.parse(fs.readFileSync("./devs.json", 'utf8'));
    http.get("http://" + host + ":" + port + "/time",
        function(res){
            res.on('data', function(chunk){
                timechunks += chunk;
            });

            res.on('end', function(){
                console.log("time=" + timechunks);
                servertime = JSON.parse(timechunks).Now;
                if(servertime-devinfo.ts>604800/*3600*/){
                    initwload();
                } else {
                    initwoload();
                }
            }); 
        }    
    );
} else {
    settime();
    initwload();
}

function terminateNextWorker(){
    if(worker_stack.length>0){
        worker_stack.shift().terminate();
        console.log("Terminated worker thread; num_workers=" + worker_stack.length);
    }
}


