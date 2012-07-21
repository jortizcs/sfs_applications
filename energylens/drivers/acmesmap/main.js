var backgrounder = require('backgrounder');
var http = require('http');

var workers = new Array();
var total_data="";

var host = "ec2-184-169-204-224.us-west-1.compute.amazonaws.com";
var port = 8080;
var rtpath = "/jorge";
var sfshost = "http://" + host + ":" + port;

var acmesinfo = new Object();

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
                            worker.send({"op":"start","smapreport":requestObj, "acmesinfo":acmesinfo});
                            worker.on("message", 
                                function(msg){
                                    addToAcmesInfo(msg.acmesinfo);
                                    console.log("terminating worker");
                                    worker.terminate();
                                });
                            resp.writeHead(200, {'Content-Type': 'text/json'});
                            resp.end("{\"status\":\"success\"}");
                            return;
                        }
                    }
                    console.log("not ok");
                    /*resp.writeHead(200, {'Content-Type': 'text/json'});
                    resp.end(JSON.stringify(
                        {"msg":"huh? i have no idea what you want me to do."}));*/
                }
            }
        } catch(e){
            console.log(e);
        }
        resp.writeHead(200, {'Content-Type': 'text/json'});
        resp.end("{\"status\":\"success\"}");
    });
}

var server = http.createServer(handle);
server.listen(1339, host);
console.log("starting server " + host + ":1339");


