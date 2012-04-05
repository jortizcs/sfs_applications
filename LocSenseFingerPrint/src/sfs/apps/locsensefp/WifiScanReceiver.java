package sfs.apps.locsensefp;

import java.net.URL;
import java.util.List;
import java.util.Date;
import java.util.HashMap;

import sfs.lib.Util;
import sfs.lib.SFSConnector;

/*import java.net.URL;
 import java.util.Vector;
 
 import sfs.lib.CurlOps;
 
 import org.json.JSONObject;*/
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
	private static HashMap<String, String> pubidHashMap = null;
	
	public WifiScanReceiver(LocSenseFingerPrint wifiSensorCtrl, String locationPath, SharedPreferences dataBufPref) {
	    super();
	    loc_path = locationPath;
	    bufferPref = dataBufPref;
	    pubidHashMap = new HashMap<String, String>();
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
	
	public static void changeLocation(String locationPath){
		//boolean b = Pattern.matches("(/+.)++/*", loc_path);
		loc_path = Util.cleanPath(locationPath);
		Log.i(WifiScanReceiver.class.toString(), "Changing location=" + loc_path);
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
			long ts = (new Date()).getTime()/1000;
			if(LocSenseFingerPrint.scanModeEnabled && LocSenseFingerPrint.t == LocSenseFingerPrint.ScanType.WIFI){
				Log.i(WifiScanReceiver.class.toString(), "Scan Mode enabled; Recording");
				List<ScanResult> results = LocSenseFingerPrint.wifiMngr.getScanResults();
				for (ScanResult result : results) {
					try {
						JSONArray newStreamBuf = new JSONArray();
						JSONObject datapt = new JSONObject();
						datapt.put("ts", ts);
						datapt.put("value", result.level);
						Log.i(WifiScanReceiver.class.toString(), "checking: " + 
							  GlobalConstants.HOST + loc_path);
						String bssid = result.SSID + "__" + result.BSSID.replaceAll(":", "_");
						if(loc_path != null && sfsconn.exists(loc_path)){
							String stream_path = loc_path + "wifi/" + bssid;
							Log.i(WifiScanReceiver.class.toString(), "stream_path=" + stream_path);
							if(!sfsconn.exists(stream_path)){
								if(!sfsconn.exists(loc_path + "wifi"))
									sfsconn.mkrsrc(loc_path, "wifi", "default");
								sfsconn.mkrsrc(loc_path + "wifi/", bssid, "genpub");
								JSONObject propsObj = new JSONObject();
								propsObj.put("units", "dBm");
								sfsconn.updateProps(loc_path + "wifi/" + bssid, propsObj.toString());
							}
							
							String pubid;
							if(!pubidHashMap.containsKey(loc_path + "wifi/" + bssid)){
								pubid = sfsconn.getPubId(loc_path + "wifi/" + bssid);
								if(pubid!=null)
									pubidHashMap.put(loc_path + "wifi/" + bssid, pubid);
							}
							else
								pubid = pubidHashMap.get(loc_path + "wifi/" + bssid);
							
							Log.i(WifiScanReceiver.class.toString(), loc_path + "wifi/" + bssid +"::pubid=" + pubid);
							if(pubid!=null){
								postIt(bssid, pubid, newStreamBuf, datapt);
							} else{
								bufferIt(bssid, datapt);
							}
						} else {
							bufferIt(bssid, datapt);
						}
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void postIt(String bssid, String pubid, JSONArray newStreamBuf, JSONObject datapt){
		//post all the rest that you couldn't post before
		SharedPreferences.Editor editor = bufferPref.edit();
		String thisStreamBufStr = bufferPref.getString(loc_path + "wifi/" + bssid, null);
		try {
			
			
			if(thisStreamBufStr != null){
				boolean postOk = true;
				JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
				
				for(int i=0; i<thisStreamBuf.length(); ++i){
					JSONObject thisDatapt = thisStreamBuf.getJSONObject(i);
					postOk = sfsconn.putStreamData(loc_path + "wifi/" + 
												   bssid,pubid, thisDatapt.toString());
					if(postOk){
						Log.i(WifiScanReceiver.class.toString(), "\n\tposted: " + thisDatapt.toString());
					} else {
						newStreamBuf.put(thisDatapt);
					}
				}
				
				//post the new data point
				postOk = sfsconn.putStreamData(loc_path + "wifi/" + 
											   bssid,pubid, datapt.toString());
				if(postOk){
					Log.i(WifiScanReceiver.class.toString(), "\n\tposted: " + datapt.toString());
				} else {
					newStreamBuf.put(datapt);
				}
				
			} else {  //not in local buffer, so just try to post it and create a local buffer if the server is down
				boolean postOk=true;
				postOk = sfsconn.putStreamData(loc_path + "wifi/" + 
											   bssid,pubid, datapt.toString());
				if(postOk){
					Log.i(WifiScanReceiver.class.toString(), "\n\tposted: " + datapt.toString());
				} else {
					newStreamBuf.put(datapt);
				}
			}
			
			Log.i(WifiScanReceiver.class.toString(), "newStreamBuf.length=" + newStreamBuf.length());
			//replace the buffer if necessary; otherwise remove it
			editor.remove(loc_path);
			//if(newStreamBuf.length()>0){
			editor.putString(loc_path + "wifi/" + bssid, newStreamBuf.toString());
			Log.i(WifiScanReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
				  + loc_path + "wifi/" + bssid +", buffer=" + newStreamBuf.toString());
			//}
			editor.commit();
			
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void bufferIt(String bssid, JSONObject datapt){
		try {
			Log.e(WifiScanReceiver.class.toString(), "Could not get pubid for path " + 
				  GlobalConstants.HOST + loc_path + "wifi/" + bssid);
			SharedPreferences.Editor editor = bufferPref.edit();
			String thisStreamBufStr = bufferPref.getString(loc_path + "wifi/" + bssid , null);
			if(thisStreamBufStr != null){
				JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
				thisStreamBuf.put(datapt);
				editor.remove(loc_path + "wifi/" + bssid);
				editor.putString(loc_path + "wifi/" + bssid, thisStreamBuf.toString());
				
				Log.i(WifiScanReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
					  + loc_path + "wifi/" + bssid+", buffer=" + thisStreamBuf.toString());
			} else {
				JSONArray buf = new JSONArray();
				buf.put(datapt);
				editor.putString(loc_path + "wifi/" + bssid, buf.toString());
				Log.i(WifiScanReceiver.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
					  + loc_path + "wifi/" + bssid +", buffer=" + buf.toString());
			}
			editor.commit();
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
}
