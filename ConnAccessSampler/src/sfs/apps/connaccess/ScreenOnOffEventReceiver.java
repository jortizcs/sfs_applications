package sfs.apps.connaccess;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import sfs.lib.SFSConnector;
import sfs.lib.Util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ScreenOnOffEventReceiver extends BroadcastReceiver {
	private static String loc_path = null;
	private static SFSConnector sfs_server = null;
	
	private static SharedPreferences bufferPref = null;
	private static ConcurrentHashMap<String, String> pubidHashMap = null;
	private String stream_path = null;
	private static boolean exp_paths_set = false;
	
	public ScreenOnOffEventReceiver(String locationPath, SharedPreferences preferences){
		loc_path = Util.cleanPath(locationPath);
		stream_path = loc_path + "/screen_state/" + ConnAccessSampler.localMacAddress + "/onoff_stream";
		try {
	    	URL hostport = new URL(GlobalConstants.HOST);
	    	int port = hostport.getPort();
	    	if(port==-1)
	    		port=80;
	    	sfs_server = new SFSConnector(hostport.getHost(), hostport.getPort());
	    	
	    	if(bufferPref==null)
		        bufferPref = preferences;
	    	if(pubidHashMap==null)
	    		pubidHashMap = new ConcurrentHashMap<String,String>();
	    	
	    	setupReporting();
	    } catch(Exception e){
			Log.e("ConnApp::", "", e);
	    }
	}
	
	public void onReceive(Context c, Intent intent) {
		try {
			long now = System.currentTimeMillis()/1000;
	  		long ts = (now-ConnAccessSampler.localReftime)+ ConnAccessSampler.serverRefTime;
	  		JSONArray newStreamBuf = new JSONArray();
	  		JSONObject datapt = new JSONObject();
	  		datapt.put("ts", ts);
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
	    		datapt.put("value", 0);
				Log.i("ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "screen_off::"+ datapt.toString());
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				datapt.put("value", 1);
				Log.i("ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "screen_on::" + datapt.toString());
			}
			setupReporting();
			if(datapt.has("value")){
				//post it or buffer it
				String pubid=null;
				if(!pubidHashMap.containsKey(stream_path)){
					pubid = sfs_server.getPubId(stream_path);
					if(pubid!=null){
						pubidHashMap.put(stream_path, pubid);
						Log.i("ADD_EVENT::", "[path=" + stream_path + ", pubid=" + pubid + "]");
					}
				} else {
					pubid=pubidHashMap.get(stream_path);
				}
				
				Log.i("ConnApp::" + ScreenOnOffEventReceiver.class.toString(), stream_path + "::pubid=" + pubid);
				if(pubid!=null){
					 postIt(pubid, stream_path, newStreamBuf, datapt);
				} else{
					Log.i("MISSING_KEY_EVENT::", "[path=" + stream_path + ", pubid=" + pubid + "]");
					Log.i("MISSING_KEY_EVENT::", pubidHashMap.toString());
					Log.i("ERROR_EVENT::", "[path=" + stream_path + ", pubid=" + pubid + "]");
					//System.exit(1);
					bufferIt(datapt);
				}
			}
		} catch(Exception e){
			Log.e("ConnApp::", "", e);
		}

	}
	
	private void postIt(String pubid, String stream_path, JSONArray newStreamBuf, JSONObject datapt){
		Log.i("POST_IT_EVENT", "[pubid=" + pubid + ", stream_path=" + stream_path + ", newStreamBuf=" + 
				newStreamBuf.toString() + ", datapt=" + datapt.toString() + "]");
		//post all the rest that you couldn't post before
		SharedPreferences.Editor editor = bufferPref.edit();
		String thisStreamBufStr = bufferPref.getString(stream_path, null);
		try {
			if(thisStreamBufStr != null){
				Log.i("POST_IT_EVENT", "1");
				boolean postOk = true;
				JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
							
				for(int i=0; i<thisStreamBuf.length(); ++i){
					JSONObject thisDatapt = thisStreamBuf.getJSONObject(i);
					postOk = sfs_server.postStreamData(stream_path, pubid, thisDatapt.toString());
					if(postOk){
						Log.i("ConnApp::" + "ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "\n\tposted: " + thisDatapt.toString());
					} else {
						newStreamBuf.put(thisDatapt);
					}
				}
		
				//post the new data point
				postOk = sfs_server.postStreamData(stream_path,pubid, datapt.toString());
				if(postOk){
					Log.i("ConnApp::" + "ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "\n\tposted: " + datapt.toString());
				} else {
					newStreamBuf.put(datapt);
				}
			} else {  //not in local buffer, so just try to post it and create a local buffer if the server is down
				Log.i("POST_IT_EVENT", "2");
				boolean postOk=true;
				postOk = sfs_server.postStreamData(stream_path,pubid, datapt.toString());
				if(postOk){
				Log.i("ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "\n\tposted: " + datapt.toString());
				} else {
					newStreamBuf.put(datapt);
				}
			}
		
			Log.i("ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "newStreamBuf.length=" + newStreamBuf.length());
			//replace the buffer if necessary; otherwise remove it
			editor.remove(stream_path);
			editor.putString(stream_path, newStreamBuf.toString());
			Log.i("ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" + stream_path
														+", buffer=" + newStreamBuf.toString());
			editor.commit();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
  
	private void bufferIt(JSONObject datapt){
		
		try {
			Log.i("BUFFER_IT_EVENT", "[stream_path=" + stream_path + ", datapt=" + datapt.toString() + "]");
			Log.e(ScreenOnOffEventReceiver.class.toString(), "Could not get pubid for path " + 
													GlobalConstants.HOST + stream_path);
			SharedPreferences.Editor editor = bufferPref.edit();
			String thisStreamBufStr = bufferPref.getString(stream_path, null);
			if(thisStreamBufStr != null){
				JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
				thisStreamBuf.put(datapt);
				editor.remove(stream_path);
				editor.putString(stream_path, thisStreamBuf.toString());
				
				Log.i("ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
						+ stream_path +", buffer=" + thisStreamBuf.toString());
			} else {
				JSONArray buf = new JSONArray();
				buf.put(datapt);
				editor.putString(stream_path, buf.toString());
				Log.i("ConnApp::" + ScreenOnOffEventReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
						+ stream_path +", buffer=" + buf.toString());
			}
			editor.commit();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void setupReporting(){
		Log.i("FUNCTION_CALL_EVENT", "ScanSetTask.setupReporting() called!");
		try {
			if(ConnAccessSampler.isConnectedToSfs && !exp_paths_set){
				Log.i("SETUP_EVENT", "0");
				if(sfs_server.exists(loc_path)){
					Log.i("SETUP_EVENT", "1");
					if(!sfs_server.exists(loc_path+ "/screen_state")){
						sfs_server.mkrsrc(loc_path, "screen_state", "default");
						Log.i("SETUP_EVENT", "2");
					} else{
						Log.i("SETUP_EVENT", "2a");
					}
					
					//create folder for this phone in the screen_state folder
					if(sfs_server.exists(loc_path+ "/screen_state") && 
							!sfs_server.exists(loc_path + "/screen_state/" + ConnAccessSampler.localMacAddress)){
						Log.i("SETUP_EVENT", "3");
						sfs_server.mkrsrc(loc_path+"/screen_state", ConnAccessSampler.localMacAddress, "default");
						JSONObject props = new JSONObject();
	    				props.put("info", GlobalConstants.PHONE_INFO);
	    				sfs_server.overwriteProps(loc_path+"/screen_state/"+ ConnAccessSampler.localMacAddress, props.toString());
						if(sfs_server.exists(loc_path+"/screen_state/"+ ConnAccessSampler.localMacAddress)){
							exp_paths_set=true;
							Log.i("SETUP_EVENT", "4");
							sfs_server.mkrsrc(loc_path+"/screen_state/" + ConnAccessSampler.localMacAddress ,"onoff_stream", "stream");
						}
						
					}
					
					if(!sfs_server.exists(loc_path+"/screen_state/" + ConnAccessSampler.localMacAddress + "/onoff_stream")){
						sfs_server.mkrsrc(loc_path+"/screen_state/" + ConnAccessSampler.localMacAddress ,"onoff_stream", "stream");
						Log.i("SETUP_EVENT", "4a");
					}
				} else{
					sfs_server.mkrsrc(Util.getParent(loc_path), loc_path.substring(loc_path.lastIndexOf("/")+1,loc_path.length()), "default");
					//create screen_state folder
					Log.i("SETUP_EVENT", "5");
					if(!sfs_server.exists(loc_path+ "/screen_state")){
						sfs_server.mkrsrc(loc_path, "screen_state", "default");
						Log.i("SETUP_EVENT", "6");
					}
					
					//create folder for this phone in the screen_state folder
					if(sfs_server.exists(loc_path+ "/screen_state") && 
							!sfs_server.exists(loc_path + "/screen_state/" + ConnAccessSampler.localMacAddress)){
						Log.i("SETUP_EVENT", "7");
						sfs_server.mkrsrc(loc_path+"/screen_state", ConnAccessSampler.localMacAddress, "default");
						JSONObject props = new JSONObject();
	    				props.put("info", GlobalConstants.PHONE_INFO);
	    				sfs_server.overwriteProps(loc_path, props.toString());
					}
				}
			} else {
				Log.i("NO_SETUP_EVENT", "isConnectedToSfs=" + ConnAccessSampler.isConnectedToSfs);
			}
		} catch(Exception e){
			Log.e("SETUP_EVENT_ERROR", "", e);
		}
	}
}
