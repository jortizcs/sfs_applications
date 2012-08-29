package sfs.apps.connaccess;

import java.net.URL;
import java.util.List;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import sfs.lib.Util;
import sfs.lib.SFSConnector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

//import java.util.regex.Pattern;

public class WifiScanReceiver extends BroadcastReceiver {
  //private static final String TAG = "WiFiScanReceiver";
	private static String loc_path = null;
	private static SFSConnector sfsconn = null;
	private static SharedPreferences bufferPref = null;
	private static ConcurrentHashMap<String, String> pubidHashMap = null;
	
	public static boolean isReporting= false;

	public WifiScanReceiver(ConnAccessSampler wifiSensorCtrl, String locationPath, SharedPreferences dataBufPref) {
	    super();
	    if(pubidHashMap==null){
		    loc_path = Util.cleanPath(locationPath);
		    bufferPref = dataBufPref;
		    pubidHashMap = new ConcurrentHashMap<String, String>();
	    }
	    try {
	    	URL hostport = new URL(GlobalConstants.HOST);
	    	int port = hostport.getPort();
	    	if(port==-1)
	    		port=80;
	    	sfsconn = new SFSConnector(hostport.getHost(), hostport.getPort());
	    	setupReporting();
	    } catch(Exception e){
	    	e.printStackTrace();
	    }
  }
  
	public static void changeLocation(String locationPath){
		loc_path = Util.cleanPath(locationPath);
		Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "Changing location=" + loc_path);
	}
  
  public static void changeSFSHostPort(){
	  try {
	    	URL hostport = new URL(GlobalConstants.HOST);
	    	int port = hostport.getPort();
	    	if(port==-1)
	    		port=80;
	    	sfsconn = new SFSConnector(hostport.getHost(), hostport.getPort());
	    } catch(Exception e){
	    	e.printStackTrace();
	    }
  }
  
  int scanCounter = 0;

  @Override
  public void onReceive(Context c, Intent intent) {
  	try {
  		long now = (new Date()).getTime()/1000;
  		long ts = (now-ConnAccessSampler.localReftime)+ ConnAccessSampler.serverRefTime;
  		if(ConnAccessSampler.scanModeEnabled){
			Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "Scan Mode enabled; Recording");
		    List<ScanResult> results = ConnAccessSampler.wifiMngr.getScanResults();
		    isReporting = true;
		    for (ScanResult result : results) {
		    	try {
		    		JSONArray newStreamBuf = new JSONArray();
		    		JSONObject datapt = new JSONObject();
		    		datapt.put("ts", ts);
		    		datapt.put("value", result.level);
		    		Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "checking: " + 
		    				GlobalConstants.HOST + loc_path);
		    		String bssid = result.BSSID.replaceAll(":", "_");
		    		Log.i("ConnApp::" + "ConnApp::" + WifiScanReceiver.class.toString(), bssid + "::" + datapt.toString());
		    		String stream_path = loc_path + "/wifi/" + ConnAccessSampler.localMacAddress + "/" + bssid;
		    		Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "stream_path=" + stream_path);
		    		
		    		
		    		if(sfsconn.exists(stream_path)){
		    			String pubid=null;
		    			if(!pubidHashMap.containsKey(stream_path)){
		    				pubid = sfsconn.getPubId(stream_path);
		    				if(pubid!=null){
		    					pubidHashMap.put(stream_path, pubid);
		    					Log.i("ADD_EVENT::", "[path=" + stream_path + ", pubid=" + pubid + "]");
		    				}
		    			} else {
		    				pubid=pubidHashMap.get(stream_path);
		    			}
		    			
		    			Log.i("ConnApp::" + WifiScanReceiver.class.toString(), stream_path + "::pubid=" + pubid);
		    			if(pubid!=null){
		    				 postIt(bssid, pubid, stream_path, newStreamBuf, datapt);
		    			} else{
		    				Log.i("MISSING_KEY_EVENT::", "[path=" + stream_path + ", pubid=" + pubid + "]");
		    				Log.i("MISSING_KEY_EVENT::", pubidHashMap.toString());
		    				Log.i("ERROR_EVENT::", "[path=" + stream_path + ", pubid=" + pubid + "]");
		    				//System.exit(1);
		    				bufferIt(bssid, datapt);
		    			}
		    		} else {
		    			Log.i("ConnApp::", "Create resource::[parent=" + Util.getParent(stream_path) + 
		    					",filename=" + bssid + ",type=default]");

						//create create a stream for this access point in the result set in the folder for this phone
						sfsconn.mkrsrc(Util.getParent(stream_path), bssid,  "stream");
						Log.i("ConnApp::","Creating stream file" + stream_path);
						JSONObject propsObj = new JSONObject();
	    				propsObj.put("units", "dBm");
	    				sfsconn.overwriteProps(stream_path, propsObj.toString());
						
						if(sfsconn.exists(stream_path)){
							String pubid=null;
			    			if(!pubidHashMap.containsKey(stream_path)){
			    				pubid = sfsconn.getPubId(stream_path);
			    				if(pubid!=null){
			    					pubidHashMap.put(stream_path, pubid);
			    					postIt(bssid, pubid, stream_path, newStreamBuf, datapt);
			    				}
			    			}
							
						} else {
							bufferIt(bssid, datapt);
							setupReporting();
						}
		    		}
		    	} catch(Exception e){
		    		e.printStackTrace();
		    	}
		    }
		}
  	} catch(Exception e){
  		e.printStackTrace();
  	} finally {
  		isReporting=false;
  	}
  }
  
	private void postIt(String bssid, String pubid, String stream_path, JSONArray newStreamBuf, JSONObject datapt){
		//post all the rest that you couldn't post before
		SharedPreferences.Editor editor = bufferPref.edit();
		String thisStreamBufStr = bufferPref.getString(stream_path, null);
		try {
			if(thisStreamBufStr != null){
				boolean postOk = true;
				JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
							
				for(int i=0; i<thisStreamBuf.length(); ++i){
					JSONObject thisDatapt = thisStreamBuf.getJSONObject(i);
					postOk = sfsconn.postStreamData(stream_path, pubid, thisDatapt.toString());
					if(postOk){
						Log.i("ConnApp::" + "ConnApp::" + WifiScanReceiver.class.toString(), "\n\tposted: " + thisDatapt.toString());
						ConnAccessSampler.debugString+= thisDatapt.toString() + "->" + stream_path + "\n";
					} else {
						newStreamBuf.put(thisDatapt);
						ConnAccessSampler.debugString+= thisDatapt.toString() + "->" + stream_path + "SAVED\n";
					}
				}
		
				//post the new data point
				postOk = sfsconn.postStreamData(stream_path,pubid, datapt.toString());
				if(postOk){
					Log.i("ConnApp::" + "ConnApp::" + WifiScanReceiver.class.toString(), "\n\tposted: " + datapt.toString());
					ConnAccessSampler.debugString+= datapt.toString() + "->" + stream_path + "\n";
				} else {
					newStreamBuf.put(datapt);
					ConnAccessSampler.debugString+= datapt.toString() + "->" + stream_path + "SAVED\n";
				}
			} else {  //not in local buffer, so just try to post it and create a local buffer if the server is down
				boolean postOk=true;
				postOk = sfsconn.postStreamData(stream_path,pubid, datapt.toString());
				if(postOk){
				Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "\n\tposted: " + datapt.toString());
				} else {
					newStreamBuf.put(datapt);
				}
			}
		
			Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "newStreamBuf.length=" + newStreamBuf.length());
			//replace the buffer if necessary; otherwise remove it
			editor.remove(stream_path);
			editor.putString(stream_path, newStreamBuf.toString());
			Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" + stream_path
														+", buffer=" + newStreamBuf.toString());
			editor.commit();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
  
	private void bufferIt(String bssid, JSONObject datapt){
		try {
			String stream_path = loc_path + "/wifi/" + ConnAccessSampler.localMacAddress + "/" +  bssid;
			Log.e(WifiScanReceiver.class.toString(), "Could not get pubid for path " + 
													GlobalConstants.HOST + stream_path);
			SharedPreferences.Editor editor = bufferPref.edit();
			String thisStreamBufStr = bufferPref.getString(stream_path, null);
			if(thisStreamBufStr != null){
				JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
				thisStreamBuf.put(datapt);
				editor.remove(stream_path);
				editor.putString(stream_path, thisStreamBuf.toString());
				ConnAccessSampler.debugString+= datapt.toString() + "->" + stream_path + "SAVED \n";
				
				Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
						+ stream_path +", buffer=" + thisStreamBuf.toString());
			} else {
				JSONArray buf = new JSONArray();
				buf.put(datapt);
				editor.putString(stream_path, buf.toString());
				Log.i("ConnApp::" + WifiScanReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
						+ stream_path +", buffer=" + buf.toString());
				ConnAccessSampler.debugString+= datapt.toString() + "->" + stream_path + "SAVED \n";
			}
			editor.commit();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void setupReporting(){
		try {
			if(ConnAccessSampler.isConnectedToSfs){
				if(sfsconn.exists(loc_path)){
					//create wifi folder
					if(!sfsconn.exists(loc_path+ "/wifi")){
						sfsconn.mkrsrc(loc_path, "wifi", "default");
					}
					
					//create folder for this phone in the wifi folder
					if(sfsconn.exists(loc_path+ "/wifi") && !sfsconn.exists(loc_path + "/wifi/" + ConnAccessSampler.localMacAddress)){
						sfsconn.mkrsrc(loc_path+"/wifi", ConnAccessSampler.localMacAddress, "default");
						JSONObject props = new JSONObject();
	    				props.put("info", GlobalConstants.PHONE_INFO);
						sfsconn.overwriteProps(loc_path + "/wifi/" + ConnAccessSampler.localMacAddress, props.toString());
					}
				} else{
					sfsconn.mkrsrc(Util.getParent(loc_path), loc_path.substring(loc_path.lastIndexOf("/")+1,loc_path.length()), "default");
					//create wifi folder
					if(!sfsconn.exists(loc_path+ "/wifi")){
						sfsconn.mkrsrc(loc_path, "wifi", "default");
					}
					
					//create folder for this phone in the wifi folder
					if(sfsconn.exists(loc_path+ "/wifi") && !sfsconn.exists(loc_path + "/wifi/" + ConnAccessSampler.localMacAddress)){
						sfsconn.mkrsrc(loc_path+"/wifi", ConnAccessSampler.localMacAddress, "default");
						JSONObject props = new JSONObject();
	    				props.put("info", GlobalConstants.PHONE_INFO);
						sfsconn.overwriteProps(loc_path + "/wifi/" + ConnAccessSampler.localMacAddress, props.toString());
					}
				}
			}
		} catch(Exception e){
			Log.e("ConnApp::", "", e);
		}
	}
}