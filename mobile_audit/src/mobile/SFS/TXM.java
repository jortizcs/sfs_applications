package mobile.SFS;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class TXM {
	public static final String FILE = "TXLog";
	public static final String LOG_PATH = "";
	private static final boolean DEBUG = true;
	
	private static TXM txm;
	
	private long localInitTime_, serverInitTime_;
	private boolean connected_ = true;
	private Context context_;
	
	public static TXM getTXM() {
		return txm;
	}
	
	public static TXM initTXM(Context context) {
		try {
			return txm = new TXM(context);
		}
		catch(Exception e) {
			return null;
		}
	}
	
	private TXM(Context context) throws Exception {
		context_ = context;
		localInitTime_ = System.currentTimeMillis();
		serverInitTime_ = Long.valueOf(new JSONObject(CurlOpsReal.get(GlobalConstants.HOST + "/time")).getString("Now"));
		displayMsg("local: " + localInitTime_ + " server: " + serverInitTime_);
		new Timer().scheduleAtFixedRate(new TXMInterrupt(), 1000, 1000);
	}
	
	/**
	 * Performs a StreamFS operation. If a network connection exists, sends the operation to StreamFS.
	 * Otherwise, writes the operation to a local log
	 */
	public String performOp(String op, String path, JSONObject data) {
		displayMsg("op: " + op + ", path: " + path + ", data: " + data.toString());
		SfsCache cache = SfsCache.getInstance();
		
		try {
			if(hasNetworkConnection()) {
				displayMsg("Connected to network");
				
				if(op.equals("PUT"))
					return CurlOpsReal.put(data.toString(), path);
				if(op.equals("POST"))
					return CurlOpsReal.post(data.toString(), path);
				if(op.equals("GET"))
					return CurlOpsReal.get(path);
				if(op.equals("DELETE"))
					return CurlOpsReal.delete(path);
			}
			else {
				connected_ = false;
				displayMsg("No network connection. Writing to log instead");
				
				FileOutputStream out = context_.openFileOutput(FILE, Context.MODE_APPEND);
				JSONObject json = new JSONObject();
				json.put("op", op);
				json.put("path", path);
				json.put("data", data);
				json.put("ts", serverInitTime_ + System.currentTimeMillis() - localInitTime_);
				json.put("type", cache.getEntry(path).has("links_to") ? "symlink" : cache.getEntry(path).getString("type"));
				out.write(json.toString().getBytes());
				out.close();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			displayMsg("error");
		}
		
		return cache.performOp(op, path, data).toString();
	}
	
	/**
	 * Called when connection is reestablished. Sends the transactions stored in the log to the StreamFS server
	 * and flushes the log.
	 */
	public void flushLog() {
		try {
			FileInputStream in = context_.openFileInput(FILE);
			int nextChar;
			String logEntry = "";
			JSONArray arr = new JSONArray();
			
			while((nextChar = in.read()) != -1) {
				if(nextChar != 10)
					logEntry += (char)nextChar;
				else {
					try {
						arr.put(new JSONObject(logEntry));
						logEntry = "";
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
			
			JSONObject log = new JSONObject();
			log.put("type", "log");
			log.put("data", arr);
			CurlOpsReal.post(log.toString(), LOG_PATH); //send log to server
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void displayMsg(String msg) {
		if(DEBUG) {
			Toast.makeText(context_, msg, Toast.LENGTH_LONG).show();
			System.out.println(msg);
		}
    }
	
	private boolean hasNetworkConnection() {
		NetworkInfo ni = ((ConnectivityManager)context_.getSystemService(Activity.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}
	
	private class TXMInterrupt extends TimerTask {
		public void run() {
			if(!connected_ && hasNetworkConnection()) { //something has been written to the log, but connection now exists
				connected_ = true;
				flushLog();
			}
		}
	}
}
