package sfs.db.txactions;

import sfs.db.*;
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.zip.*;

import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import org.simpleframework.http.Query;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class SfsGlobalTransactionManager implements Container{

    private static final MySqlDriver mysqldb = MySqlDriver.getInstance();
    private static final JSONParser parser = new JSONParser();
    private static Logger logger = Logger.getLogger(SfsGlobalTransactionManager.class.getPackage().getName());
    private static String sfsHttpHost = null;
    private static int sfsHttpPort = -1;
    private static long sfs_inittime = -1L;
    private static long local_inittime = -1L;

    public SfsGlobalTransactionManager(String host, int port, String protocol) throws Exception{
        if(protocol.equalsIgnoreCase("http")){
            sfsHttpHost = host;
            sfsHttpPort = port;

            try {
                URL url = new URL("http://" + sfsHttpHost + ":" + sfsHttpPort + "/time");
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                //conn.setDoOutput(true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                                                conn.getInputStream()));
                StringBuffer responseBuf = new StringBuffer();
                String line = null;
                while((line=reader.readLine())!=null)
                    responseBuf.append(line);
                logger.info(sfsHttpHost+":"+sfsHttpPort+" response::" + responseBuf.toString());
                conn.disconnect();
                JSONObject now = (JSONObject)parser.parse(responseBuf.toString());
                sfs_inittime = ((Long)now.get("Now")).longValue();
                local_inittime = System.currentTimeMillis()/1000;
            } catch(Exception e){
                throw e;
            }
        }
    }

    public static void main(String[] args){
        try {
            SfsGlobalTransactionManager server = new SfsGlobalTransactionManager(
                                    "ec2-184-169-204-224.us-west-1.compute.amazonaws.com", 8080, "http");
            //http
            Connection connection = new SocketConnection((Container)server);
            SocketAddress address = new InetSocketAddress(4896);
            connection.connect(address);
            logger.info("Listening for connection on 4896");
        } catch(Exception e){
            logger.log(Level.WARNING, "", e);
        }
    }

    public void handle(Request request, Response response){
        try {
            String method = request.getMethod();
            if(method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST")){
                JSONObject contentObj = (JSONObject)parser.parse(request.getContent());
                String type = (contentObj.get("type")!=null)?((String)contentObj.get("type")):"";
                JSONArray log = (contentObj.get("ops")!=null)?(JSONArray)contentObj.get("ops"):null;
                logger.info("Handling::contentObj=" + contentObj.toString() + "; type=" + type + "; log=" +log);
                if(type.equals("log")){
                    //sort these by timestamp
                    System.out.println(log.toArray().getClass().getName());
                    Object[] log_array_o = log.toArray();
                    JSONObject[] log_array = Arrays.copyOf(log_array_o, log_array_o.length, JSONObject[].class);
                    Arrays.sort(log_array, new LogEntryComparator<org.json.simple.JSONObject>());
                    for(int i=0; i<log.size(); i++){
                        logger.info("FUNCTION_CALL::applyAttempt(request, response, log_array[i]=" + log_array[i].toString() + ")");
                        applyAttempt(request, response, log_array[i], false);
                    }
                   
                    sendResponse(request,response,200,null);
                    return;
                } else {
                    JSONObject entry = new JSONObject();
                    long ts = (sfs_inittime + (System.currentTimeMillis()/1000 - local_inittime));
                    entry.put("ts", ts);
                    entry.put("path", request.getPath().getPath());
                    entry.put("method", request.getMethod());
                    if(((String)contentObj.get("operation")).equalsIgnoreCase("create_resource"))
                        entry.put("type", "default");
                    else if(((String)contentObj.get("operation")).equalsIgnoreCase("create_generic_resource"))
                        entry.put("type", "stream");
                    else
                        entry.put("type", "symlink");
                    entry.put("data", contentObj);
                    applyAttempt(request, response, entry, true);
                }
            } else if( method.equalsIgnoreCase("DELETE")){
                JSONObject entry = new JSONObject();
                long ts = (sfs_inittime + (System.currentTimeMillis()/1000 - local_inittime));
                entry.put("ts", ts);
                entry.put("path", request.getPath().getPath());
                entry.put("method", request.getMethod());
                entry.put("type", getType(request.getPath().getPath()));
                applyAttempt(request, response, entry, true);
            } else {
                String urlStr = "http://" + sfsHttpHost + ":" + sfsHttpPort + request.getPath().getPath();
                logger.info(urlStr);
                Query query = request.getQuery();
                logger.info("query-> " + query);
                if(query!=null && !query.equals("")){
                    urlStr += "?";
                    Iterator<String> attrs = query.keySet().iterator();
                    while(attrs.hasNext()){
                        String val = attrs.next();
                        urlStr += val +"="+(String)query.get(val);
                        if(attrs.hasNext())
                            urlStr += "&";
                    }
                }
                URL url = new URL(urlStr);
                logger.info(urlStr);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod(method.toUpperCase());
                conn.setDoOutput(true);

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                                                conn.getInputStream()));
                StringBuffer responseBuf = new StringBuffer();
                String line = null;
                while((line=reader.readLine())!=null)
                    responseBuf.append(line);
                logger.info(sfsHttpHost+":"+sfsHttpPort+" response::" + responseBuf.toString());
                int statuscode = conn.getResponseCode();
                conn.disconnect();
                sendResponse(request, response, statuscode, responseBuf.toString());
            }
        } catch(Exception e){
            logger.log(Level.WARNING, "", e);
            sendResponse(request, response, 404, null);
        }
    }

    public void applyAttempt(Request request, Response response, JSONObject sfsop, 
            boolean forwardSfsReplyToClient){
        try {
            long ts = ((Long)sfsop.get("ts")).longValue();

            //future operations relative to this one
            //try to apply it in timestamp order
            //  -- check the log, if it's the latest one, apply it, if not, resolve the conflict
            JSONArray relFutureOps = mysqldb.getAllOpsAfter(ts);
            logger.info("futureOps:: " + relFutureOps.toString());
            
            if(relFutureOps.size()>0){
                rollback(relFutureOps);
                apply(request, response, sfsop, null, 
                        true /* writeLog*/, forwardSfsReplyToClient);
                replay(relFutureOps);
            } else {
                apply(request, response, sfsop,null, true /* writeLog*/, forwardSfsReplyToClient);
            }
        } catch(Exception e){
            logger.log(Level.WARNING, "", e);
        }
    }

    /**
     * 
     */
    public boolean apply(Request req, Response resp, JSONObject sfsOp, JSONObject negOp, boolean writeLog, 
            boolean forwardSfsReplyToClient){
        int responseCode = 404;
        HttpURLConnection conn=null;
        try {
            JSONObject serverOp = (negOp==null)?sfsOp:negOp;
            logger.info("serverOp::" + serverOp);
            String path = cleanPath((String)serverOp.get("path"));
            String method = (String)serverOp.get("method");
            JSONObject data = (JSONObject) serverOp.get("data");
            URL url = new URL("http://" + sfsHttpHost + ":" + sfsHttpPort + path);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod(method.toUpperCase());
            if(method.equalsIgnoreCase("put") || method.equalsIgnoreCase("post"))
                conn.setDoOutput(true);
            conn.connect();

            if(method.equalsIgnoreCase("put") || method.equalsIgnoreCase("post")){
                OutputStream os = conn.getOutputStream();
                os.write(data.toString().getBytes());
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                            conn.getInputStream()));
            StringBuffer response = new StringBuffer();
            String line = null;
            while((line=reader.readLine())!=null)
                response.append(line);
            logger.info(sfsHttpHost+":"+sfsHttpPort+" response::" + response.toString());
            
            responseCode = conn.getResponseCode();
            if(forwardSfsReplyToClient)
                sendResponse(req, resp, responseCode, response.toString());

            //log it if the operation was successful in StreamFS
            if(method.equalsIgnoreCase("delete") && responseCode==200){
                logger.info("method=delete; writeLog=" + writeLog);
                if(writeLog)
                    log(sfsOp);
                else
                    unlog(sfsOp);
                return true;
            } else if(method.equalsIgnoreCase("put") || method.equalsIgnoreCase("post")) {
                String sfsOpString = (String)data.get("operation");
                if(sfsOpString.startsWith("create_")&& responseCode==201){
                    //what type of resource is in there
                    if(writeLog)
                        log(sfsOp);
                    else
                        unlog(sfsOp);
                    return true;
                } else {
                    logger.warning("NOT_APPLIED::"+sfsOpString);
                    return false;
                }
            } else {
                logger.warning("NOT_APPLIED::method="+ method);
                return false;
            }
        } catch(Exception e){
            logger.log(Level.WARNING, "", e);
        } finally {
            if(conn!=null)
                conn.disconnect();
        }
        if(forwardSfsReplyToClient)
            sendResponse(req, resp, responseCode, null);
           
        return false;
    }


    public void rollback(JSONArray futureOps){
        //sort and apply the negative operation (negop) in reverse chronological order
        Object[] log_array_o = futureOps.toArray();
        JSONObject[] log_array = Arrays.copyOf(log_array_o, log_array_o.length, JSONObject[].class);
        Arrays.sort(log_array, new LogEntryComparator<org.json.simple.JSONObject>());
        for(int i=log_array.length-1; i>=0; --i){
            JSONObject entry = log_array[i];
            logger.info("ENTRY::"+entry);
            String method = (String)entry.get("method");
            JSONObject data=null;
            String sfsoperation =null;
            String type= (String)entry.get("type");
            if(!method.equalsIgnoreCase("delete")){
                data = (JSONObject)entry.get("data");
                sfsoperation = (String)data.get("operation");
                type= (String)entry.get("type");
            }
 
            //negoq entries
            long negop_ts = ((Long)entry.get("ts")).longValue();
            String negop_method = null;
            String negop_path = cleanPath((String)entry.get("path"));
            JSONObject negop_data = null;

            if(method.equalsIgnoreCase("put") || method.equalsIgnoreCase("post")){
                if(sfsoperation.equalsIgnoreCase("create_resource") ||
                    sfsoperation.equalsIgnoreCase("create_generic_resource") ||
                    sfsoperation.equalsIgnoreCase("create_symlink")){
                    negop_method = "DELETE";
                    String name = (String)data.get("resourceName");
                    if(name==null)
                        name = (String)data.get("name");
                    negop_path += "/" + name;
                    negop_path = cleanPath(negop_path);
                }
            } else if(method.equalsIgnoreCase("delete")){
                if(type.equals("default")){
                    negop_method = "PUT";
                    negop_data = new JSONObject();
                    negop_data.put("operation", "create_resource");
                    negop_data.put("resourceName", 
                            negop_path.substring(negop_path.lastIndexOf("/")+1, 
                                                 negop_path.length()));
                    negop_data.put("resourceType", "default");
                } else if(type.equals("stream")){
                    negop_method = "PUT";
                    negop_data = new JSONObject();
                    negop_data.put("operation", "create_generic_publisher");
                    negop_data.put("resourceName", 
                            negop_path.substring(negop_path.lastIndexOf("/")+1, 
                                                 negop_path.length()));
                } else if(type.equals("symlink")){
                    negop_method = "POST";
                    negop_data = new JSONObject();
                    negop_data.put("operation", "create_symlink");
                    negop_data.put("uri", cleanPath(getParent(negop_path)));
                    negop_data.put("name", 
                            negop_path.substring(negop_path.lastIndexOf("/")+1, 
                                                 negop_path.length()));
                }
            }

            //apply and remove from the log
            JSONObject negop = new JSONObject();
            negop.put("method", negop_method);
            negop.put("path", negop_path);
            negop.put("ts", negop_ts);
            if((negop_method.equalsIgnoreCase("put") || negop_method.equalsIgnoreCase("post")) &&
                negop_data!=null){
                negop.put("data", negop_data);
                logger.info("1\t" + negop.toString());
                System.exit(1);
                apply(null, null, entry, negop, false, false);
            } else if(negop_method.equalsIgnoreCase("delete")){
                logger.info("2\t" + negop.toString());
                apply(null, null, entry, negop, false, false);
            } else {
                logger.warning("could not rollback::" + entry.toString());
            }
        }
    }

    public void replay(JSONArray ops){
        if(ops!=null){
            for(int i=0; i<ops.size(); i++){
                JSONObject entry = (JSONObject)ops.get(i);
                System.out.println("REPLAY::entry=" + entry.toString());
                apply(null, null, entry, null/* negop */, 
                        true /* writeLog */,
                        false /* forward reply to client */);
            }
        }
    }

    private synchronized void log(JSONObject sfsop){
        mysqldb.addToLog(sfsop);
    }

    private synchronized void unlog(JSONObject sfsop){
        /*if(((String)sfsop.get("method")).equalsIgnoreCase("delete"))
            sfsop.put("method", "PUT");*/
        mysqldb.removeFromLog(sfsop);
    }

    private static String getParent(String path){
        logger.info("Getting parent of " + path);
        if(path==null || path.equals("/")){
            logger.info("Returning NULL::" + path + " has no parent");
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(path, "/");
        Vector<String> tokens = new Vector<String>();
        while(tokenizer.hasMoreElements())
            tokens.add(tokenizer.nextToken());
        StringBuffer parentPathBuffer = new StringBuffer();
        if(tokens.size()==1)
            parentPathBuffer.append("/");
        else 
            for(int i=0; i<tokens.size()-1; i++)
                parentPathBuffer.append("/").append(tokens.elementAt(i));
        logger.info("Parent_path=" + parentPathBuffer.toString());
        return parentPathBuffer.toString();
    }

    private static String cleanPath(String path){
        //clean up the path
        if(path == null)
            return path;
        if(path.equals("") || path.equals("/"))
            return path;

        if(!path.startsWith("/"))
            path = "/" + path;
        path = path.replaceAll("/+", "/");
        if(path.endsWith("/"))
            path = path.substring(0,path.length()-1);
        return path;
    }
   
    private class LogEntryComparator<E> implements Comparator<E>{
        public int compare (E e1, E e2){
            if(!(e1 instanceof JSONObject) && !(e2 instanceof JSONObject))
                throw new ClassCastException();
            JSONObject entry1 = (JSONObject) e1;
            JSONObject entry2 = (JSONObject) e2;
            if(entry1!=null && entry2 !=null){
                long ts1=0L;
                long ts2=0L;
                if(entry1.get("ts")!=null)
                    ts1 = ((Long)entry1.get("ts")).longValue();
                if(entry2.get("ts")!=null)
                    ts2 = ((Long)entry2.get("ts")).longValue();

                if(ts1<ts2)
                    return -1;
                else if(ts1==ts2)
                    return 0;
                else if(ts1>ts2)
                    return 1;
            } else {
                throw new NullPointerException();
            }
            
            return -2;
        }

        public boolean equals(E e1, E e2){
            try {
                if(this.compare(e1, e2)==0)
                    return true;
            } catch(Exception e){}
            return false;
        }
    }

    private String getType(String path){
        try {
            String linksTo = getLink(path);
            if(linksTo!=null){
                return "symlink";}
            else {
                String resp = get(path);
                if(resp!=null){
                    JSONObject respObj = (JSONObject)parser.parse(resp);
                    if(respObj.containsKey("pubid"))
                        return "stream";
                    return "default";
                }
            }
        } catch(Exception e){
            logger.log(Level.WARNING, "", e);
        }
        return null;
    }

    private static String get(String path){
        HttpURLConnection conn=null;
        try {
            path = cleanPath(path);
            if(path.equals("/"))
                return null;
            if(path !=null && !path.equals("/")) {
                URL url = new URL("http://" + sfsHttpHost + ":" + sfsHttpPort + path);
                conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                                                conn.getInputStream()));
                StringBuffer response = new StringBuffer();
                String line = null;
                while((line=reader.readLine())!=null)
                    response.append(line);
                logger.info(sfsHttpHost+":"+sfsHttpPort+" response::" + response.toString());
                JSONObject respObj = (JSONObject)parser.parse(response.toString());
                return respObj.toString();
            }
        } catch(Exception e) {
            logger.log(Level.WARNING, "", e);
        } finally{
            if(conn!=null)
                conn.disconnect();
        }
        return null;
    }
      
    private static String getLink(String path) {
        try {
            path = cleanPath(path);
            if(path.equals("/"))
                return null;
            String parent = null;
            if(path !=null && !path.equals("/")) {
                StringBuffer buf = new StringBuffer();
                StringTokenizer tokenizer = new StringTokenizer(path, "/");
                Vector<String> tokens = new Vector<String>();
                while(tokenizer.hasMoreElements())
                    tokens.add(tokenizer.nextToken());
                for(int i=0; i<tokens.size()-1; i++)
                    buf.append("/").append(tokens.get(i));
                parent=buf.toString();
                String child = tokens.lastElement();
               
                //GET parent 
                String parentResp = get(parent);
                if(parentResp!=null){
                    JSONObject respObj = (JSONObject)parser.parse(parentResp);
                    JSONArray children = (JSONArray)respObj.get("children");
                    for(int j=0; j<children.size(); j++) {
                        if(((String)children.get(j)).startsWith(child) && ((String)children.get(j)).contains("->")) { //not guaranteed to work
                            tokenizer = new StringTokenizer((String)children.get(j), " -> ");
                            tokenizer.nextToken();
                            return tokenizer.nextToken();
                        }
                    }
                }
            }
        } catch(Exception e) {
            logger.log(Level.WARNING, "", e);
        }
        return null;
    }

    public static void sendResponse(Request request, Response response, int code, String data){
        try {
            long time = System.currentTimeMillis()/1000;
            String enc = request.getValue("Accept-encoding");
            boolean gzipResp = false;
            if(enc!=null && enc.indexOf("gzip")>-1)
                gzipResp = true;
            response.set("Content-Type", "application/json");
            response.set("Server", "StreamFS/2.0 (Simple 4.0)");
            response.set("Connection", "close");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);
            response.setCode(code);
            PrintStream body = response.getPrintStream();
            if(data!=null && !gzipResp)
                body.println(data);
            else if(data!=null && gzipResp){
                response.set("Content-Encoding", "gzip");
                GZIPOutputStream gzipos = new GZIPOutputStream(body);
                gzipos.write(data.getBytes());
                gzipos.close();
            }
            body.close();
        } catch(Exception e){
            logger.log(Level.WARNING, "", e);
        }
    }
}
