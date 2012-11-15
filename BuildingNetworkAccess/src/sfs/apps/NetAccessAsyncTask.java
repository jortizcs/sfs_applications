package sfs.apps;

import sfs.lib.*;

import java.util.*;
import java.util.concurrent.*;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.os.*;

import org.json.JSONArray;
import org.json.JSONObject;


public class NetAccessAsyncTask extends AsyncTask<Void, Integer, Void> {
	public static String DEBUG_TAG = "NetAccessAsyncTask::";
	public ConnectivityManager connMgr = null;
	public Handler handler = null;
	private static StringBuffer strbuf = null;
	private static String line  =null;
	private static SFSConnector sfsConnector = null;
	
	public static final int runtime = 120;//seconds
	
	public NetAccessAsyncTask(ConnectivityManager c,Handler h){
		connMgr = c;
		handler = h;
		strbuf = new StringBuffer();
		sfsConnector = new SFSConnector("energylens.sfsprod.is4server.com", 8080);
	}
	
	public Void doInBackground(Void... params){
        NetworkInfo networkInfo = null; 
        boolean isWifiConn = false;
        boolean isMobileConn = false;
        int i=0;
        BuildingNetworkAccess.netAccessRunning=true;
        boolean serverUp= false;
		while(i<runtime && BuildingNetworkAccess.netAccessRunning){
			networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	        isWifiConn = networkInfo.isConnected();
	        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	        isMobileConn = networkInfo.isConnected();
	        long servertime = sfsConnector.getTime();
	        Log.d(DEBUG_TAG, "Wifi connected: " + isWifiConn);
	        Log.d(DEBUG_TAG, "Mobile connected: " + isMobileConn);
	        try {Thread.sleep(1000);i+=1;} catch(Exception e){}
	        strbuf.delete(0, strbuf.length());
	        strbuf.append(new Date().toString()).append("\t").
	        	append("Wifi? ").append(new Boolean(isWifiConn).toString()).append(", 3G/4G? ").
	        	append(new Boolean(isMobileConn).toString()).append(" ServerTime=");
	        if(servertime>0){
	        	BuildingNetworkAccess.serverTime = servertime;
	        	BuildingNetworkAccess.localTime = System.currentTimeMillis();
	        	serverUp=true;
	        	strbuf.append(new Date(servertime).toString());
	        }
	        else{
	        	strbuf.append(new Long(servertime).toString());
	        	serverUp=false;
	        }
	        strbuf.append("\n");
	        
	        line = strbuf.toString();
	        publishProgress(i);
	        Log.i(DEBUG_TAG, new Integer(i).toString());
	        
	        saveData(isWifiConn, isMobileConn, serverUp);
		}
		BuildingNetworkAccess.netAccessRunning=false;
		publishProgress(101);
        
		return null;
	}
	
	public static boolean isServerUp(){
		long servertime = sfsConnector.getTime();
		if(servertime>0)
			return true;
		return false;
	}

	private void saveData(boolean isWifiConn, boolean isMobileConn,
			boolean serverUp) {
		SharedPreferences bufferPref = BuildingNetworkAccess.bufferPref;
		SharedPreferences.Editor editor = bufferPref.edit();
		
		ConcurrentHashMap<String, JSONArray> dataBuffs =new ConcurrentHashMap<String, JSONArray>();
		
		StringBuffer pathBuf= (new StringBuffer()).append(BuildingNetworkAccess.BASE).append("/").
			append(BuildingNetworkAccess.CURRENT_CONTEXT);
		String path = pathBuf.toString();
		long time_t = (System.currentTimeMillis() - BuildingNetworkAccess.localTime)+BuildingNetworkAccess.serverTime;
		
		//type
		String typePath = new StringBuffer().append(path).append("/type").toString();
		if(bufferPref.contains(typePath)){
			try{
			JSONArray array= new JSONArray(bufferPref.getString(typePath, new JSONArray().toString()));
			dataBuffs.put(typePath, array);
			} catch(Exception e){
				Log.e(this.DEBUG_TAG, "", e);
			}
			
		}
		if(dataBuffs.containsKey(typePath)&& BuildingNetworkAccess.serverTime>0){
			try {
				JSONObject newVal = null;
				if(isMobileConn)
					newVal = new JSONObject().put("value", 0);
				else if(isWifiConn)
					newVal = new JSONObject().put("value", 1);
				else
					newVal = new JSONObject().put("value",2);
				newVal.put("ts", time_t);
				dataBuffs.put(typePath, dataBuffs.get(typePath).put(newVal));
				editor.putString(typePath, dataBuffs.get(typePath).toString());
			} catch(Exception e){
				Log.e(this.DEBUG_TAG, "", e);
			}
		} else if(!dataBuffs.containsKey(typePath)&& BuildingNetworkAccess.serverTime>0){
			try {
				JSONArray buf = new JSONArray();
				JSONObject newVal = null;
				if(isMobileConn)
					newVal = new JSONObject().put("value", 0);
				else if(isWifiConn)
					newVal = new JSONObject().put("value", 1);
				else
					newVal = new JSONObject().put("value",2);
				newVal.put("ts", time_t);
				dataBuffs.put(typePath, buf.put(newVal));
				editor.putString(typePath, dataBuffs.get(typePath).toString());
			} catch(Exception e){
				Log.e(this.DEBUG_TAG, "", e);
			}
		}
		
		//netaccess
		String netaccessPath = new StringBuffer().append(path).append("/access").toString();
		if(bufferPref.contains(netaccessPath)){
			try {
				JSONArray array= new JSONArray(bufferPref.getString(netaccessPath, new JSONArray().toString()));
				dataBuffs.put(netaccessPath, array);
			} catch(Exception e){
				Log.e(this.DEBUG_TAG, "", e);
			}
		}
		if(dataBuffs.containsKey(netaccessPath)&& BuildingNetworkAccess.serverTime>0){
			try {
				JSONObject newVal = null;
				if(serverUp)
					newVal = new JSONObject().put("value", 1);
				else
					newVal = new JSONObject().put("value", 0);
				newVal.put("ts", time_t);
				dataBuffs.put(netaccessPath, dataBuffs.get(typePath).put(newVal));
				editor.putString(netaccessPath, dataBuffs.get(typePath).toString());
			} catch(Exception e){
				Log.e(this.DEBUG_TAG, "", e);
			}
		} else if(!dataBuffs.containsKey(netaccessPath)&& BuildingNetworkAccess.serverTime>0){
			try {
				JSONArray buf = new JSONArray();
				JSONObject newVal = null;
				if(serverUp)
					newVal = new JSONObject().put("value", 1);
				else
					newVal = new JSONObject().put("value", 0);
				newVal.put("ts", time_t);
				dataBuffs.put(netaccessPath, buf.put(newVal));
				editor.putString(netaccessPath, dataBuffs.get(typePath).toString());
			} catch(Exception e){
				Log.e(this.DEBUG_TAG, "", e);
			}
		}
		
		if(!BuildingNetworkAccess.CURRENT_CONTEXT.equals("all")){
			//location
			String locPath = new StringBuffer().append(path).append("/loc").toString();
			if(bufferPref.contains(locPath)){
				try {
					JSONArray array= new JSONArray(bufferPref.getString(locPath, new JSONArray().toString()));
					dataBuffs.put(locPath, array);
				} catch(Exception e){
					Log.e(this.DEBUG_TAG, "", e);
				}
			}
			if(dataBuffs.containsKey(locPath)&& BuildingNetworkAccess.serverTime>0){
				try {
					JSONObject newVal = new JSONObject().put("value", BuildingNetworkAccess.CURRENT_LOC_ID);
					newVal.put("ts", time_t);
		    		dataBuffs.put(locPath, dataBuffs.get(typePath).put(newVal));
		    		editor.putString(locPath, dataBuffs.get(typePath).toString());
		    	} catch(Exception e){
		    		Log.e(this.DEBUG_TAG, "", e);
		    	}
		    } else if(!dataBuffs.containsKey(locPath)&& BuildingNetworkAccess.serverTime>0){
		    	try {
		    		JSONArray buf = new JSONArray();
					JSONObject newVal = new JSONObject().put("value", BuildingNetworkAccess.CURRENT_LOC_ID);
					newVal.put("ts", time_t);
		    		dataBuffs.put(locPath, buf.put(newVal));
		    		editor.putString(locPath, dataBuffs.get(typePath).toString());
		    	} catch(Exception e){
		    		Log.e(this.DEBUG_TAG, "", e);
		    	}
		    }
		}
		
		editor.commit();
		JSONObject d = new JSONObject((Map)dataBuffs);
		Log.i(this.DEBUG_TAG, "YOYOYO::"+d.toString());
		
	}

	protected void onProgressUpdate(Integer... progress) {
		if(progress[0]<101){
			Log.i(DEBUG_TAG, new Integer(progress[0]).toString());
			Message m = new Message();
			Bundle b= m.getData();
			b.putString("progress_level", new Double((double)(progress[0])/runtime).toString());
			b.putString("progress_msg", line);
			m.setData(b);
			handler.sendMessage(m);
		} else {
			Message m = new Message();
			Bundle b = m.getData();
			b.putString("progress_level", "Done");
			m.setData(b);
			handler.sendMessage(m);
		}
	}
	
	protected void onPostExecute(Long result) {
        //showDialog("Downloaded " + result + " bytes");
    }
	
	
}
