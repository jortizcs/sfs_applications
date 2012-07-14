var backgrounder = require('backgrounder');
var http = require('http');


var workers = new Array();

function handle(req, resp){
    if(req.method=='POST'){
        req.on('data', function(chunk){
                var requestObj = JSON.parse(chunk);
                if(requestObj.op == 'start' && 
                    typeof requestObj.path != 'undefined' && 
                    typeof workers[requestObj.path] == 'undefined'){

                    var worker = backgrounder.spawn(__dirname + "/fake_sensor_worker.js");
                    worker.send({"op": "start", "path":requestObj.path});
                    workers[requestObj.path] = worker;
                    resp.writeHead(200, {'Content-Type': 'text/json'});
                    resp.end(JSON.stringify(
                        {"msg":"started virtual stream to " + requestObj.path}));
                } else if (requestObj.op=="stop" &&
                            typeof requestObj.path != "undefined"){
                    if(typeof workers[requestObj.path] != "undefined"){
                        workers[requestObj.path].terminate();
                        delete workers[requestObj.path];
                        resp.writeHead(200, {'Content-Type': 'text/json'});
                        resp.end(JSON.stringify(
                            {"msg":"killed virtual stream to " + requestObj.path}));
                    } else {
                        resp.writeHead(200, {'Content-Type': 'text/json'});
                        resp.end(JSON.stringify(
                            {"msg":"could not find virtual stream to " + requestObj.path}));
                    }
                } else if(requestObj.op == 'change_sfshost'){
                    for(i=0; i<workers.length; i++){
                        workers[i].send({"op":"change_sfshost", 
                                        "host":requestObj.host,
                                        "port":requestObj.port});
                        resp.writeHead(200, {'Content-Type': 'text/json'});
                        resp.end(JSON.stringify(
                            {"msg":"host changed to http://" + requestObj.host + ":" + requestObj.port}));
                    }
                }
                else {
                    console.log("not ok");
                    resp.writeHead(200, {'Content-Type': 'text/json'});
                        resp.end(JSON.stringify(
                            {"msg":"huh? i have no idea what you want me to do."}));
                }
            });
    }
}

var server = http.createServer(handle);
server.listen(1338, "127.0.0.1");
console.log("starting server 127.0.0.1:1338");
