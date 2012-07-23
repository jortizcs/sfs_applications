package sfs.db.txactions;

import sfs.db.*;
import java.net.*;

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

    public SfsGlobalTransactionManager(String host, int port, String protocol){
        if(protocol.equalsIgnoreCase("http")){
            sfsHttpHost = host;
            sfsHttpPort = port;
        }
    }

    public static void main(String[] args){
        try {
            SfsGlobalTransactionManager server = new SfsGlobalTransactionManager("", -1, "http");
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
        String method = request.getMethod();
        if(method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST")){
            JSONObject contentObj = parser.parse(request.getContent());
            String type = contentObj.optString("type");
            JSONArray log = contentObj.optJSONArray("ops");
            if(type.equals("log")){
                /*for(int i=0; i<log.size(); i++){
                }*/
                //sort these by timestamp
                //try to apply it in timestamp order
                //  -- check the log, if it's the latest one, apply it, if not, resolve the conflict
            } else {
                forwardOpToSfs(method, request.getPath(), contentObj);
            }
        }
    }

    /**
     * handle create/delete [default|symlink] resources, update/overwrite properties
     */
    public void forwardOpToSfs(String method, String path, JSONObject data ){
    }

    public synchronized void log(JSONObject sfsop){
    }

    public void rollback(long timestamp){
    }

    public void replay(long timestamp){
    }

    /**
     * This is where policy gets enforced.
     */
    public synchronized void resolveConflict(JSONObject sfsop){
    }
    
}
