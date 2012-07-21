var http = require('http');

function handlePost(req, res){
    res.writeHead(200, {'Content-Type': 'text/json'});
    res.end("{\"status\":\"success\"}");
}

var server= http.createServer(function(req,res){
    req.setEncoding('utf8');

    console.log(req.headers);

    req.on('data', function(chunk) {
        console.log("Receive_Event::" + chunk);
    });

    req.on('end', function() {
        console.log('on end');
        console.log("Bytes received: " + req.socket.bytesRead);
        if(req.method=='POST'){
            handlePost(req,res);
        } else{
            res.writeHead(200, {'Content-Type': 'text/plain'});
            res.end();
        }
    });
});

server.listen(1339, "ec2-184-169-204-224.us-west-1.compute.amazonaws.com");
console.log('Server running at http://ec2-184-169-204-224.us-west-1.compute.amazonaws.com:1339/');
