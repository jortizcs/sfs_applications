package sfs.db.txactions;

import sfs.db.*;
import java.net.*;
import java.util.*;

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
        try {
        String method = request.getMethod();
        if(method.equalsIgnoreCase("PUT") || method.equalsIgnoreCase("POST")){
            JSONObject contentObj = (JSONObject)parser.parse(request.getContent());
            String type = (contentObj.get("type")!=null)?((String)contentObj.get("type")):"";
            JSONArray log = (contentObj.get("ops")!=null)?(JSONArray)contentObj.get("ops"):null;
            if(type.equals("log")){
                
                //sort these by timestamp
                JSONObject[] log_array = (JSONObject[])log.toArray();
                Arrays.sort(log_array, new LogEntryComparator<org.json.simple.JSONObject>());
                for(int i=0; i<log.size(); i++)
                    applyAttempt(log_array[i]);    
                
                
            } else {
                forwardOpToSfs(method, request.getPath().getPath(), contentObj);
            }
        }
        } catch(Exception e){
            logger.log(Level.WARNING, "", e);
        }
    }

    /**
     * handle create/delete [default|symlink] resources, update/overwrite properties
     */
    public void forwardOpToSfs(String method, String path, JSONObject data ){
    }

    public void applyAttempt(JSONObject sfsop){
        long ts = ((Long)sfsop.get("ts")).longValue();
        JSONArray getAllOpsAfter(ts);
        //try to apply it in timestamp order
        //  -- check the log, if it's the latest one, apply it, if not, resolve the conflict
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
}
