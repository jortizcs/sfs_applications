package mobile.SFS;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.widget.Toast;

public class TXM {
	public static final String FILE = "TXLog";
	private boolean connected_ = true;
	private Context context_;
	
	public TXM(Context context) {
		context_ = context;
		new Timer().scheduleAtFixedRate(new TXMInterrupt(), 1000, 1000);
	}
	
	/**
	 * Performs a StreamFS operation. If a network connection exists, sends the operation to StreamFS.
	 * Otherwise, writes the operation to a local log
	 */
	public void performOp(String op, String path, JSONObject data) {
		displayMsg(connected_+"");
		if(hasNetworkConnection()) {
			displayMsg("Connected to network");
		}
		else {
			try {
				connected_ = false;
				displayMsg("No network connection. Writing to log instead");
				FileOutputStream out = context_.openFileOutput(FILE, Context.MODE_APPEND);
				out.write("write to file test".getBytes());
				out.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Called when connection is reestablished. Sends the transactions stored in the log to the StreamFS server
	 * and flushes the log.
	 */
	public void flushLog() {
		try {
			FileInputStream in = context_.openFileInput(FILE);
			displayMsg("reading from log: " + in.read(new byte[in.available()]));
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private void displayMsg(String msg){
    	Toast.makeText(context_, msg,Toast.LENGTH_LONG).show();
    }
	
	private boolean hasNetworkConnection() {
		NetworkInfo ni = ((ConnectivityManager)context_.getSystemService(Activity.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		return ni != null && ni.isConnected();
	}
	
	private class TXMInterrupt extends TimerTask {
		public void run() {
			//displayMsg("here");
			if(!connected_ && hasNetworkConnection()) { //something has been written to the log, but connection now exists
				connected_ = true;
			//	flushLog();
			}
		}
	}
}
