package sfs.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import snaq.db.*;
import java.sql.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.net.URL;
import java.lang.StringBuffer;
import javax.sql.rowset.serial.*;

public class MySqlDriver {

	private static transient final Logger logger = Logger.getLogger(MySqlDriver.class.getPackage().getName());

    private static JSONParser parser = new JSONParser();

	private static String HOST = null;
	private static int PORT = -1;
	private static String LOGIN = null;
	private static String PW = null;

	private static String dbName = "jortiz";

	protected static ConnectionPool pool = null;
	private static int openConns =0;

    private static MySqlDriver mysqlDB=null;

	private static Hashtable<String, String> validSchemas = null;

    public static MySqlDriver getInstance(){
        if(mysqlDB ==null)
            setupMysqlDB();
        return mysqlDB;
    }

	private MySqlDriver(String host, int port) {
        
		HOST = host; PORT = port; LOGIN = null; PW = null;
		try {
			if(pool == null){
				String url = "jdbc:mysql://localhost/" + dbName + "?holdResultsOpenOverStatementClose=true";
				Driver driver = (Driver)Class.forName ("com.mysql.jdbc.Driver").newInstance ();
				DriverManager.registerDriver(driver);
				pool = new ConnectionPool("local", 0, 0, 0, 10, url, LOGIN, PW);
                pool.setCaching(true);
			} else {
				logger.info("Pool already created");
			}
		} catch (Exception e){
			logger.log(Level.WARNING, "", e);
		}
	}

	private MySqlDriver(String host, int port, String login, String pw, String dbname){
		super();
		HOST = host; PORT = port; LOGIN = login; PW = pw; dbName = dbname;
		logger.info("host: " + HOST + " port:" + PORT);

		try {
			if(pool == null){
				String url = "jdbc:mysql://localhost/" + dbName;
				Driver driver = (Driver)Class.forName ("com.mysql.jdbc.Driver").newInstance ();
				DriverManager.registerDriver(driver);
				pool = new ConnectionPool("local", 5, 10, 20, 0, url, LOGIN, PW);
			} else {
				logger.info("Pool already created");
			}
		} catch (Exception e){
			logger.log(Level.WARNING, "", e);
		}
	}

    private static void setupMysqlDB(){
        try{
            JSONParser parser = new JSONParser();
            String home=null;
            String dbConfigFile = "sfs/db/db_config/db_info.json";
            if((home=System.getenv().get("SFSHOME")) != null)
                dbConfigFile = home + "/sfs/db/db_config/db_info.json";
            logger.info("home: " + System.getenv().get("SFSHOME") + "; config: " + dbConfigFile);
            File configFile = new File(dbConfigFile);
            FileReader cFileReader = new FileReader(configFile);
            BufferedReader bufReader = new BufferedReader(cFileReader);
        
            StringBuffer strBuf = new StringBuffer();
            String line = null;
            while((line=bufReader.readLine())!=null)
                strBuf.append(line).append(" ");
            JSONObject configJsonObj = (JSONObject)parser.parse(strBuf.toString());
                                
            cFileReader.close();
            bufReader.close();
            String addr = (String)configJsonObj.get("address");
            int port = ((Long)configJsonObj.get("port")).intValue();
            String dbName = (String)configJsonObj.get("dbname");
            String username = (String)configJsonObj.get("login");
            String password = (String)configJsonObj.get("password");

            String parseStatus = "Addr: " + addr + "\nPort: " + port + "\nlogin: " + username + "\npw: " + password + " dbName: " + dbName;
            logger.info(parseStatus);
            if(!username.equalsIgnoreCase("")){
                mysqlDB = new MySqlDriver(addr, port, username, password, dbName);
            }
            else {
                mysqlDB = new MySqlDriver(addr, port);
            }
        } catch(Exception e){
            e.printStackTrace();
            System.out.println("FATAL ERROR: Error instantiating DBAbstraction Layer");
            System.exit(1);
        }
    }

	public static void main(String[] args){
		MySqlDriver  driver = new MySqlDriver("localhost", 3306, "root", "410soda", "jortiz");
	}

	private Connection openConn() {
		Connection conn = null;
		try {
			conn =  pool.getConnection(10000);
			logger.finer("Free_count: " + pool.getFreeCount());
			logger.info ("Database connection established");

			if(conn != null) {
				openConns += 1;
				logger.info("Open: conn_count=" + openConns);
			}
		} catch (Exception e){
			logger.log(Level.SEVERE, "Cannot connect to database server", e);
		}

		return conn;
	}

	private void closeConn(Connection conn){
		try {
			if (conn != null && !conn.isClosed()){
				conn.close ();
				logger.finer("Free_count_close: " + pool.getFreeCount());
				logger.info("Database connection terminated");

				openConns -= 1;
				logger.info("Close: conn_count=" + openConns);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while closing database connection", e);
		}
	}

    /**  LOGGING FUNCTIONS **/

    public void addToLog(JSONObject op){
        Connection conn=null;
        try {
            conn = openConn();
            String method = (String)op.get("op");
            String path = (String)op.get("path");
            String sfsop = ((JSONObject)op.get("data")).toString();
            long ts = ((Long)op.get("ts")).longValue();
            String query = "Insert into `sfs_txlog` (`method`, `path`, `sfsop`, `timestamp`) values(?, ?, ? ?)";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, method);
            ps.setString(2, path);
            ps.setString(3, sfsop);
            ps.setLong(4, ts);
            ps.executeUpdate();
        } catch(Exception e){
            logger.log(Level.WARNING, "",e);
        } finally{
            closeConn(conn);
        }
    }

    public void removeFromLog(JSONObject op){
        Connection conn=null;
        try {
            conn = openConn();
            String method = (String)op.get("op");
            String path = (String)op.get("path");
            String sfsop = ((JSONObject)op.get("data")).toString();
            long ts = ((Long)op.get("ts")).longValue();
            int id = (op.get("id")!=null)?((Integer)op.get("id")).intValue():-1;
            String query =null;
            if(id==-1)
                query = "Remove from `sfs_txlog` where `method`=? and `path`=? and `sfsop`=?";
            else 
                query = "Remove from `sfs_txlog` where `id`=?";

            PreparedStatement ps = conn.prepareStatement(query);
            if(id==-1){
                ps.setString(1, method);
                ps.setString(2, path);
                ps.setString(3, sfsop);
                ps.setLong(4, ts);
            } else {
                ps.setInt(1, id);     
            }
            ps.executeUpdate();
        } catch(Exception e){
            logger.log(Level.WARNING, "",e);
        } finally{
            closeConn(conn);
        }
    }

    public void removeAllGreaterThan(long timestamp){
        Connection conn=null;
        try {
            conn = openConn();
            String query = "Remove from `sfs_txlog` where `ts`>?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setLong(1, timestamp);
            ps.executeUpdate();
        } catch(Exception e){
            logger.log(Level.WARNING, "",e);
        } finally{
            closeConn(conn);
        }
    }

    public void emptyLog(){
        Connection conn=null;
        try {
            conn = openConn();
            String query = "truncate `sfs_txlog`";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.executeUpdate();
        } catch(Exception e){
            logger.log(Level.WARNING, "",e);
        } finally{
            closeConn(conn);
        }
    }

    public JSONArray getAllOpsAfter(long timestamp){
        Connection conn=null;
        JSONArray ops = new JSONArray();
        try {
            conn = openConn();
            String query = "select `id`,`method`, `path`, `sfsop`, `timestamp` from `sfs_txlog` where `ts`>?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setLong(1, timestamp);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                try {
                    JSONObject entry = new JSONObject();
                    entry.put("id", rs.getInt("id"));
                    entry.put("ts", rs.getLong("timestamp"));
                    entry.put("op", rs.getString("method"));
                    JSONObject sfsops = (JSONObject)parser.parse(rs.getString("sfsop"));
                    entry.put("data",sfsops);
                    ops.add(entry);
                } catch(Exception e){
                    logger.log(Level.WARNING, "", e);
                }
            }
        } catch(Exception e){
            logger.log(Level.WARNING, "",e);
            return null;
        } finally{
            closeConn(conn);
        }
        return ops;
    }
}
