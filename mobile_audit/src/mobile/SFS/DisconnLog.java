package mobile.SFS;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TimerTask;
import java.util.Timer;

import android.util.Log;

public class DisconnLog extends TimerTask{
	private static DisconnLog disconnLog = null;
	private static ConcurrentHashMap<Long, JSONArray> table = null;
	private static ConcurrentLinkedQueue<JSONObject> queue = null;
	
	private DisconnLog(){
		table = new ConcurrentHashMap<Long, JSONArray>();
		queue = new ConcurrentLinkedQueue<JSONObject>();
	}
	
	public DisconnLog getInstance(){
		if(disconnLog==null)
			disconnLog = new DisconnLog();
		return disconnLog;
	}
	
	public synchronized void addLogEntry(String path, String httpMethod, JSONObject obj){
		long tlong = new Long(System.currentTimeMillis());
		JSONArray ops = null;
		if(table.containsKey(tlong)){
			ops = table.get(tlong);
			try {
				JSONObject operation = new JSONObject();
				operation.put("path", path);
				operation.put("method", httpMethod);
				operation.put("value", obj);
				ops.put(operation);
				table.replace(tlong, ops);
			} catch(Exception e){
				Log.e(DisconnLog.class.toString(), e.getStackTrace().toString());
			}
		}
		return;
	}
	
	public long getOldestTsEntry(){
		Set<Long> keys = table.keySet();
		ArrayList<Long> keysAL = new ArrayList<Long>(keys);
		if(keysAL.size()>0){
			long[] keysArray = new long[keysAL.size()];
			Arrays.sort(keysArray);
			return keysArray[0];
		} 
		return 0L;
	}
	
	public void push(JSONObject q){
		queue.offer(q);
	}
	
	public void run(){
		
	}
	
}
