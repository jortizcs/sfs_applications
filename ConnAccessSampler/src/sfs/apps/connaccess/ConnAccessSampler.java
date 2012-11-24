package sfs.apps.connaccess;

import java.net.URL;
import java.util.*;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import sfs.apps.connaccess.GlobalConstants;
import sfs.apps.connaccess.ConnAccessSampler;
import sfs.apps.connaccess.WifiScanReceiver;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
import android.content.Intent;
import android.util.Log;
import android.net.wifi.WifiInfo;
import sfs.lib.*;
import android.widget.EditText;


public class ConnAccessSampler extends Activity {
	protected static WifiManager wifiMngr;
	protected static WifiInfo wifiInfo;
	protected static ConnectivityManager connMngr;
	protected static BroadcastReceiver wifiReceiver;
	protected static BroadcastReceiver screenEventReceiver;
	protected static boolean scanModeEnabled = false;
	protected static final int scanTime = 10; //(seconds)
	protected static Timer timer = null;
	protected static String currLocString =null;
	protected static String localMacAddress = null;
	private static boolean exp_paths_set = false;
	
	
	private static SharedPreferences bufferPref = null;
	private static ConcurrentHashMap<String, String> pubidHashMap = null;
	
	public static long serverRefTime = -1L;
	public static long localReftime = -1L;
	
	public static boolean isConnectedToSfs = false;
	
	private static final long samplePeriod = 10000L;
	private static int sampleCounter = 0;
	
	private static TextView statusText = null;
	protected static TextView debugText = null;
	public static String debugString = "";
	protected static EditText infoText=null;
	protected static SharedPreferences pref = null;
	
	public static enum ScanType {
		NONE, WIFI
	}
	
	public static ScanType t = ScanType.NONE; 
	
	private static SFSConnector sfs_server = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        if(bufferPref==null){
	        bufferPref = this.getSharedPreferences(GlobalConstants.BUFFER_DATA, 
					android.content.Context.MODE_PRIVATE);
        }
                
        String host= GlobalConstants.HOST.substring(7, GlobalConstants.HOST.lastIndexOf(":"));
        int port =Integer.parseInt(GlobalConstants.HOST.substring(GlobalConstants.HOST.lastIndexOf(":")+1, 
				GlobalConstants.HOST.length()));
        Log.i("ConnApp::" + ConnAccessSampler.class.toString(), host + ":" + port);
        sfs_server = new SFSConnector(host, port);
        
        //set text view
        statusText = (TextView)findViewById(R.id.stateText);
        
        //debug text view
        debugText = (TextView)findViewById(R.id.debugText);
        
        //load phone info
        pref = this.getSharedPreferences(GlobalConstants.PREFS, MODE_PRIVATE);
        GlobalConstants.PHONE_INFO=pref.getString("phone_info", "");
        
        //set up the scan button
        Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		if(GlobalConstants.PHONE_INFO.length()>0){
	        		scanModeEnabled=true;
	        		statusText.setText("ENABLED");
	        		startScanning(v);
        		}
        	}
        });
        
        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		scanModeEnabled=false;
        		statusText.setText("DISABLED");
        		//timer.cancel();
        	}
        });
        
        Button quitButton = (Button) findViewById(R.id.quitButton);
        quitButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		scanModeEnabled=false;
        		statusText.setText("QUIT");
        		//timer.cancel();
        		System.exit(0);
        	}
        });
        
        infoText = (EditText)findViewById(R.id.EditText01);
        Button setInfoButton = (Button) findViewById(R.id.setButton);
        if(GlobalConstants.PHONE_INFO.length()>0){
        	infoText.setText(GlobalConstants.PHONE_INFO);
        }
        setInfoButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		GlobalConstants.PHONE_INFO = infoText.getText().toString();
        		SharedPreferences.Editor editor = pref.edit();
        		editor.putString("phone_info", GlobalConstants.PHONE_INFO);
        		editor.commit();
        	}
        });
    }
    
    public void startScanning(View v){
    	
    	// INITIALIZE RECEIVER
        //set up the receivers
    	if(wifiReceiver==null){
	    	wifiReceiver = new WifiScanReceiver(this, GlobalConstants.EXP_RT_PATH, bufferPref);
	    	registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	}
    	
    	if(wifiMngr ==null){
	    	// Setup WiFi
	    	wifiMngr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    	wifiInfo = wifiMngr.getConnectionInfo();
	    	localMacAddress = wifiInfo.getMacAddress().replaceAll(":", "_");
    	}
    	
    	if(screenEventReceiver==null){
    		screenEventReceiver = new ScreenOnOffEventReceiver(GlobalConstants.EXP_RT_PATH, bufferPref);
    		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
    		filter.addAction(Intent.ACTION_SCREEN_OFF);
	    	registerReceiver(screenEventReceiver, filter);
    	}
    	v.postDelayed(new ScanSetTask(GlobalConstants.EXP_RT_PATH, v), samplePeriod);
    	/*t = ScanType.WIFI;
    	timer = new Timer();
    	timer.scheduleAtFixedRate(new ScanSetTask(GlobalConstants.EXP_RT_PATH), 0L, samplePeriod);*/
    }
    
    protected class ScanSetTask extends TimerTask{
    	
    	private String loc_path = null;
    	private SFSConnector sfsconn = null;
    	
    	private String stream_path = null;
    	private View view = null;
    	
    	
    	public ScanSetTask(String locationPath, View v){
    		loc_path = Util.cleanPath(locationPath);
    		view = v;
    		try {
    			loc_path = Util.cleanPath(locationPath);
			    stream_path = loc_path + "/conn_state/" + ConnAccessSampler.localMacAddress + "/conn_stream";
    			if(pubidHashMap==null){
    			    pubidHashMap = new ConcurrentHashMap<String, String>();
    		    }
    			sfsconn=sfs_server;
    	    	setupBulkReporting(new JSONArray(), false);
    	    } catch(Exception e){
    			Log.e("ERROR_EVENT::", "", e);
    	    }
    	}
    	
    	public void run(){
    		Log.i("ConnApp::", "RUNNING_BACKGROUND_TASK");
    		if(ConnAccessSampler.scanModeEnabled){
	    		getSFSTime();
	    		if(!WifiScanReceiver.isReporting && serverRefTime>0 && ConnAccessSampler.sampleCounter==6){
	    			ConnAccessSampler.sampleCounter=0;
	    			wifiMngr.startScan();
	    		} else {
	    			ConnAccessSampler.sampleCounter+=1;
	    		}
	    		debugText.setText(debugString);
	    		debugString="";
    		}
    		view.postDelayed(new ScanSetTask(GlobalConstants.EXP_RT_PATH, view), samplePeriod);
		}
		
		public void getSFSTime(){
			try {
				if(sfs_server!=null){
					setupReporting();
					long t = sfs_server.getTime();
					long now = System.currentTimeMillis();
					JSONArray newStreamBuf = new JSONArray();
					if(t>0){
						isConnectedToSfs=true;
						ConnAccessSampler.localReftime = now;
						serverRefTime = t;
						long ts = (now-ConnAccessSampler.localReftime)+ ConnAccessSampler.serverRefTime;
				  		JSONObject datapt = new JSONObject();
				  		datapt.put("ts", ts);
				  		datapt.put("value",1);
						Log.i("ConnApp::" + ScanSetTask.class.toString(), "Time_set=" + serverRefTime + "; value=" + datapt.toString());
						
						//post it or buffer it
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
		    			
		    			Log.i("ConnApp::" + ScanSetTask.class.toString(), stream_path + "::pubid=" + pubid);
		    			if(pubid!=null){
		    				 postIt(pubid, stream_path, newStreamBuf, datapt);
		    			} else{
		    				Log.i("MISSING_KEY_EVENT::", "[path=" + stream_path + ", pubid=" + pubid + "]");
		    				Log.i("MISSING_KEY_EVENT::", pubidHashMap.toString());
		    				Log.i("ERROR_EVENT::", "[path=" + stream_path + ", pubid=" + pubid + "]");
		    				//System.exit(1);
		    				bufferIt(datapt);
		    			}
					} else {
						isConnectedToSfs=false;
						long ts = (now-ConnAccessSampler.localReftime)+ ConnAccessSampler.serverRefTime;
				  		JSONObject datapt = new JSONObject();
				  		datapt.put("ts", ts);
				  		datapt.put("value",0);
						Log.i("ConnApp::" + ScanSetTask.class.toString(), "No connection to SFS"+ "; value=" + datapt.toString());
						bufferIt(datapt);
					}
				} else {
					Log.i("ERROR_EVENT", "sfs_server is null!");
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
						postOk = sfsconn.postStreamData(stream_path, pubid, thisDatapt.toString());
						if(postOk){
							Log.i("ConnApp::" + "ConnApp::" + ScanSetTask.class.toString(), "\n\tposted: " + thisDatapt.toString());
						} else {
							newStreamBuf.put(thisDatapt);
						}
					}
			
					//post the new data point
					postOk = sfsconn.postStreamData(stream_path,pubid, datapt.toString());
					if(postOk){
						Log.i("ConnApp::" + "ConnApp::" + ScanSetTask.class.toString(), "\n\tposted: " + datapt.toString());
					} else {
						newStreamBuf.put(datapt);
					}
				} else {  //not in local buffer, so just try to post it and create a local buffer if the server is down
					Log.i("POST_IT_EVENT", "2");
					boolean postOk=true;
					postOk = sfsconn.postStreamData(stream_path,pubid, datapt.toString());
					if(postOk){
					Log.i("ConnApp::" + ScanSetTask.class.toString(), "\n\tposted: " + datapt.toString());
					} else {
						newStreamBuf.put(datapt);
					}
				}
			
				Log.i("ConnApp::" + ScanSetTask.class.toString(), "newStreamBuf.length=" + newStreamBuf.length());
				//replace the buffer if necessary; otherwise remove it
				editor.remove(stream_path);
				editor.putString(stream_path, newStreamBuf.toString());
				Log.i("ConnApp::" + ScanSetTask.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" + stream_path
															+", buffer=" + newStreamBuf.toString());
				editor.commit();
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	  
		private void bufferIt(JSONObject datapt){
			
			try {
				String stream_path = loc_path + "/conn_state/" + ConnAccessSampler.localMacAddress + "/conn_stream";
				Log.i("BUFFER_IT_EVENT", "[stream_path=" + stream_path + ", datapt=" + datapt.toString() + "]");
				Log.e(ScanSetTask.class.toString(), "Could not get pubid for path " + 
														GlobalConstants.HOST + stream_path);
				SharedPreferences.Editor editor = bufferPref.edit();
				String thisStreamBufStr = bufferPref.getString(stream_path, null);
				if(thisStreamBufStr != null){
					JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
					thisStreamBuf.put(datapt);
					editor.remove(stream_path);
					editor.putString(stream_path, thisStreamBuf.toString());
					
					Log.i("ConnApp::" + ScanSetTask.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
							+ stream_path +", buffer=" + thisStreamBuf.toString());
				} else {
					JSONArray buf = new JSONArray();
					buf.put(datapt);
					editor.putString(stream_path, buf.toString());
					Log.i("ConnApp::" + ScanSetTask.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
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
						//create conn_state folder
						if(!sfs_server.exists(loc_path+ "/conn_state")){
							sfs_server.mkrsrc(loc_path, "conn_state", "default");
							Log.i("SETUP_EVENT", "2");
						} else{
							Log.i("SETUP_EVENT", "2a");
						}
						
						//create folder for this phone in the conn_state folder
						if(sfs_server.exists(loc_path+ "/conn_state") && 
								!sfs_server.exists(loc_path + "/conn_state/" + ConnAccessSampler.localMacAddress)){
							Log.i("SETUP_EVENT", "3");
							sfs_server.mkrsrc(loc_path+"/conn_state", ConnAccessSampler.localMacAddress, "default");
							JSONObject props = new JSONObject();
		    				props.put("info", GlobalConstants.PHONE_INFO);
		    				sfs_server.overwriteProps(loc_path+"/conn_state/"+ ConnAccessSampler.localMacAddress, props.toString());
							if(sfs_server.exists(loc_path+"/conn_state/"+ ConnAccessSampler.localMacAddress)){
								Log.i("SETUP_EVENT", "4");
								sfs_server.mkrsrc(loc_path+"/conn_state/" + ConnAccessSampler.localMacAddress ,"conn_stream", "stream");
							}
							
						}
						
						if(!sfs_server.exists(loc_path+"/conn_state/" + ConnAccessSampler.localMacAddress + "/conn_stream")){
							sfs_server.mkrsrc(loc_path+"/conn_state/" + ConnAccessSampler.localMacAddress ,"conn_stream", "stream");
							Log.i("SETUP_EVENT", "4a");
						} else {
							exp_paths_set=true;
						}
						
					} else{
						sfs_server.mkrsrc(Util.getParent(loc_path), loc_path.substring(loc_path.lastIndexOf("/")+1,loc_path.length()), "default");
						//create conn_state folder
						Log.i("SETUP_EVENT", "5");
						if(!sfs_server.exists(loc_path+ "/conn_state")){
							sfs_server.mkrsrc(loc_path, "conn_state", "default");
							Log.i("SETUP_EVENT", "6");
						}
						
						//create folder for this phone in the conn_state folder
						boolean e=sfs_server.exists(loc_path + "/conn_state/" + ConnAccessSampler.localMacAddress);
						if(sfs_server.exists(loc_path+ "/conn_state") && 
								!e){
							Log.i("SETUP_EVENT", "7");
							sfs_server.mkrsrc(loc_path+"/conn_state", ConnAccessSampler.localMacAddress, "default");
							JSONObject props = new JSONObject();
		    				props.put("info", GlobalConstants.PHONE_INFO);
		    				sfs_server.overwriteProps(loc_path+"/conn_state/"+ConnAccessSampler.localMacAddress, props.toString());
						} else if(e){
							exp_paths_set=true;
						}
					}
				} else {
					Log.i("NO_SETUP_EVENT", "isConnectedToSfs=" + ConnAccessSampler.isConnectedToSfs + "; paths_set=" + exp_paths_set);
				}
			} catch(Exception e){
				Log.e("SETUP_EVENT_ERROR", "", e);
			}
		}
		
		private void setupBulkReporting(JSONArray list, boolean override){
			try {
				if(ConnAccessSampler.isConnectedToSfs && (!exp_paths_set || override)){
					JSONObject props = new JSONObject();
					props.put("info", GlobalConstants.PHONE_INFO);
					
					JSONObject spec = new JSONObject();
					spec.put("path", loc_path + "/conn_state/" + ConnAccessSampler.localMacAddress);
					spec.put("type", "default");
					spec.put("properties", props);
					
					JSONObject spec2 = new JSONObject();
					spec2.put("path", loc_path+"/conn_state/" + ConnAccessSampler.localMacAddress + "/conn_stream");
					spec2.put("type", "stream");
					
					list.put(spec);
					list.put(spec2);
					
					JSONObject responseObj = sfs_server.bulkResourceCreate("/", list);
					Log.i("ConnApp::","responseObj="+responseObj.toString());
					if(responseObj!=null){
						for(int i=0; i<list.length(); i++){
							String testPath = Util.cleanPath(list.getString(i));
							JSONObject thisResp = null;
							try {
								if(responseObj.has(testPath))
									thisResp = responseObj.getJSONObject(testPath);
								else if(responseObj.has(testPath+"/"))
									thisResp = responseObj.getJSONObject(testPath+"/");
								
								if(thisResp!=null && thisResp.has("PubId")){
									String pubid = thisResp.getString("PubId");
									pubidHashMap.put(testPath, pubid);
								}
							} catch(Exception handleError){
								Log.e("SETUP_EVENT_ERROR", "", handleError);
							}
						}
						exp_paths_set = true;
					}
					
					
				}
			} catch(Exception e){
				Log.e("ConnApp::", "", e);
			}
		}
		
    }
    
    
    
}