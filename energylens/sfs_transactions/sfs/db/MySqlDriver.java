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

	private Connection openConnLocal(){
		Connection conn = null;
		try {
			conn =  pool.getConnection(10000);
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


	private Connection openConn() {
		Connection conn = null;
		try {
			/*String url = "jdbc:mysql://" + HOST + "/" + dbName;
			Class.forName ("com.mysql.jdbc.Driver").newInstance ();
			conn = DriverManager.getConnection (url, LOGIN, PW);*/

			/*JDCConnectionDriver driver = new JDCConnectionDriver("com.mysql.jdbc.Driver", url, LOGIN, PW);
			conn = driver.connect(url, null);*/

			conn =  pool.getConnection(1000);
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

    public void addToLog(JSONObject op){
    }

    public void removeFromLog(JSONObject op){
    }

    public void removeAllGreaterThan(long timestamp){
    }

    public void emptyLog(){
    }

    public JSONArray getAllOpsAfter(long timestamp){
        return null;
    }
}
