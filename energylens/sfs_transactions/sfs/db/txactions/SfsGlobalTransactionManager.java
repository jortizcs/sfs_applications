package sfs.db.txactions;

import java.net.*;

import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import org.simpleframework.http.Query;

import java.util.logging.Logger;
import java.util.logging.Level;

public class SfsGlobalTransactionManager implements Container{

    private static Logger logger = Logger.getLogger(SfsGlobalTransactionManager.class.getPackage().getName());
    private static String sfsHttpHost = null;
    private static String sfsHttpPort = -1;

    public SfsGlobalTransactionManager(){
    }

    public static void main(String[] args){
        try {
            SfsGlobalTransactionManager server = new SfsGlobalTransactionManager();
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
    }

    public void forwardOpToSfs(JSONObject sfsop){
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
