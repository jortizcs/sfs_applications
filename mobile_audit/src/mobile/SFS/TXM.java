package mobile.SFS;

import org.json.JSONObject;

public interface TXM {
	
	/**
	 * Performs a StreamFS operation. If a network connection exists, sends the operation to StreamFS.
	 * Otherwise, writes the operation to a local log
	 */
	public JSONObject performOp(String op, String path, JSONObject data);
	
	/**
	 * Called when connection is reestablished. Sends the transactions stored in the log to the StreamFS server
	 * and flushes the log.
	 */
	public void flushLog();
}
