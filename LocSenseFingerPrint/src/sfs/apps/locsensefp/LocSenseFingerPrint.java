package sfs.apps.locsensefp;

import java.net.URL;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

import sfs.lib.CurlOps;
import sfs.lib.Util;
import sfs.lib.SFSConnector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Handler;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.os.Message;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;

public class LocSenseFingerPrint extends Activity implements OnItemSelectedListener, OnDismissListener {
	
	private static final String DEFAULT_SPACE = GlobalConstants.SPACESHOME;
	private static boolean settingGlobals = false;
	protected static WifiManager wifiMngr;
	protected static BroadcastReceiver receiver;
	protected static boolean scanModeEnabled = false;
	protected static final int scanTime = 10; //(seconds)
	private static int countDownCnter = LocSenseFingerPrint.scanTime;
	protected static Timer timer = null;
	protected static AmbientNoiseRecorder noiseRecorder;
	protected static String currLocString =null;
	
	private static ProgressDialog wifiProgDialog;
	private static ProgressDialog lightProgDialog;
	private static ProgressDialog soundProgDialog;
	private static boolean wifiDialogIsActive=false;
	private static boolean lightDialogIsActive = false;
	private static boolean soundDialogIsActive = false;
	
	private static SharedPreferences bufferPref = null;
	private static HashMap<String, String> pubidHashMap = null;
	
	public static enum ScanType {
		NONE, WIFI, LIGHT, SOUND
	}
	
	public static ScanType t = ScanType.NONE; 
	
	//Lets get it started here!
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setPreviousGlobals();
        try {
	        //set the buffer file name for this server
			URL u = new URL(GlobalConstants.HOST);
			int port =80;
			if(u.getPort()>0)
				port = u.getPort();
			GlobalConstants.BUFFER_DATA = u.getHost() + "_" + port + "_buffer";
        } catch (Exception e){
        	e.printStackTrace();
        }
        pubidHashMap = new HashMap<String, String>();
        bufferPref = this.getSharedPreferences(GlobalConstants.BUFFER_DATA, 
				android.content.Context.MODE_PRIVATE);
        
        //set it
        TextView currLoc = (TextView) findViewById(R.id.currLoc);
        currLocString = (currLoc.getText().equals("Current location") ? DEFAULT_SPACE : "Current location");
        currLoc.setText(currLocString);
        
        //set up the change location button
        Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        		intent.putExtra("curr_loc", currLocString);
        		startActivityForResult(intent, 0);
			}
		});
        
        //set up the scan button
        Button scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		//do something when the scan button is clicked
        		//scan wifi, scan light, scan sound
        		//scanWifi();
        		startScanning();
        	}
        });
        
        //set the type to scan wifi first
        t=ScanType.LIGHT;
        
        //populate the listview options
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem, 
        		new String[] {"Scan Deployment Info QR-Code","Deployment Info", "Clear Saved Data"}));
        
        listView.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		Intent intent = null;
        		
				switch(arg2) {
					case 0: //intent = new Intent(MobileSFS.this, ViewServices.class); break;
						intent = new Intent("com.google.zxing.client.android.SCAN");
		        		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		        		intent.putExtra("curr_loc", currLocString);
		        		settingGlobals = true;
		        		startActivityForResult(intent, 0);
		        		break;
					case 1:
						if(checkGlobals()){
							StringBuffer msgBuf = new StringBuffer("Name=").append(GlobalConstants.NAME);
							msgBuf.append("\nHost=").append(GlobalConstants.HOST);
							msgBuf.append("\nRoot=").append(GlobalConstants.ROOT);
							msgBuf.append("\nhome=").append(GlobalConstants.HOMEPATH);
					    	msgBuf.append("\nqrc=").append(GlobalConstants.QRCHOME);
					    	msgBuf.append("\ntaxonomy=").append(GlobalConstants.TAXHOME);
							msgBuf.append("\nspaces=").append(GlobalConstants.SPACESHOME);
							msgBuf.append("\ninventory=").append(GlobalConstants.INVHOME);
							displayMsg(msgBuf.toString());
				    	}
						break;
					case 2:
						SharedPreferences.Editor editor = bufferPref.edit();
						editor.clear();
						editor.commit();
						Context context = getApplicationContext();
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(context, "Cleared data!", duration);
						toast.show();
						break;
				}
				
				//intent.putExtra("curr_loc", currLocString);
				//startActivity(intent);
			}
        });
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {		
		
		String urlstr = intent.getStringExtra("SCAN_RESULT");//getIntent().getStringExtra("url");
	    
	    if(settingGlobals){
			Log.i(LocSenseFingerPrint.class.getName(), "setglobals:"+ settingGlobals + "\tstr=" + urlstr);
			try{
	    		String res = CurlOps.getConfigObjStrFromUrl(urlstr);
	    		Log.i(LocSenseFingerPrint.class.getName(), "setglobals.res:"+ res);
				JSONObject resObj = new JSONObject(res);
				Log.i(LocSenseFingerPrint.class.getName(), "setglobals.resObj:"+ resObj.toString());
				//fetch the config information from this URL
				if(resObj.has("host") && resObj.has("root") && resObj.has("homepath") &&
						resObj.has("qrchome") && resObj.has("taxhome") && resObj.has("spaceshome") && 
						resObj.has("invhome")){
					GlobalConstants.NAME = resObj.getString("deployment");
					GlobalConstants.HOST = resObj.getString("host");
					GlobalConstants.ROOT = resObj.getString("root");
					GlobalConstants.HOMEPATH = resObj.getString("homepath");
					GlobalConstants.QRCHOME = resObj.getString("qrchome");
					GlobalConstants.TAXHOME = resObj.getString("taxhome");
					GlobalConstants.SPACESHOME = resObj.getString("spaceshome");
					GlobalConstants.INVHOME = resObj.getString("invhome");
					
					//set the shared preferences object values for storage later on
					Log.i(LocSenseFingerPrint.class.toString(), "Setting preferences");
					SharedPreferences pref = this.getSharedPreferences(GlobalConstants.PREFS, MODE_PRIVATE);
					SharedPreferences.Editor editor = pref.edit();
					editor.putString("name", GlobalConstants.NAME);
					editor.putString("host", GlobalConstants.HOST);
					editor.putString("root", GlobalConstants.ROOT);
					editor.putString("homepath", GlobalConstants.HOMEPATH);
					editor.putString("qrchome", GlobalConstants.QRCHOME);
					editor.putString("taxhome", GlobalConstants.TAXHOME);
					editor.putString("spaceshome", GlobalConstants.SPACESHOME);
					editor.putString("invhome", GlobalConstants.INVHOME);
					editor.commit();
					
					//set the buffer file name for this server
					URL u = new URL(GlobalConstants.HOST);
					int port =80;
					if(u.getPort()>0)
						port = u.getPort();
					GlobalConstants.BUFFER_DATA = u.getHost() + "_" + port + "_buffer";
					Log.i(LocSenseFingerPrint.class.toString(), "Set BUFFER_DATA name to " + GlobalConstants.BUFFER_DATA);
					
					//make sure the wifi receiver knows where to put the new wifi data
					WifiScanReceiver.changeSFSHostPort();
					
					Toast.makeText(this, "Set new configuration",Toast.LENGTH_LONG).show();
				} else {
					Log.i(LocSenseFingerPrint.class.getName(), "setglobals::couldn't set them!");
				}
			} catch(Exception e){
				e.printStackTrace();
			}
			settingGlobals = false;
			return;
		} else {
			try {
				String newLoc = Util.getUriFromQrc(CurlOps.getQrcFromUrl(intent.getStringExtra("SCAN_RESULT")));
				TextView currLoc = (TextView) findViewById(R.id.currLoc);
		        currLoc.setText(newLoc);
		        currLocString = newLoc;
		        WifiScanReceiver.changeLocation(newLoc);
			}
			catch(Exception e) {
				e.printStackTrace();
				//returnIntent_.putExtra("curr_loc", "Unknown");
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, "Unknown QR code", duration);
				toast.show();
			}
			
		}
	}
	
	public void setPreviousGlobals(){
    	SharedPreferences pref = this.getSharedPreferences(GlobalConstants.PREFS, MODE_PRIVATE);
    	if(checkGlobals()){
    		GlobalConstants.NAME = pref.getString("name", "");
	    	GlobalConstants.HOST = pref.getString("host", "");
			GlobalConstants.ROOT = pref.getString("root", "");
			GlobalConstants.HOMEPATH = pref.getString("homepath", "");
			GlobalConstants.QRCHOME = pref.getString("qrchome", "");
			GlobalConstants.TAXHOME = pref.getString("taxhome", "");
			GlobalConstants.SPACESHOME = pref.getString("spaceshome", "");
			GlobalConstants.INVHOME = pref.getString("invhome", "");
    	}
    }
    
    public boolean checkGlobals(){
    	SharedPreferences pref = this.getSharedPreferences(GlobalConstants.PREFS, MODE_PRIVATE);
    	String testStr = pref.getString("host", "");
    	if(testStr.equals("")){
    		Toast.makeText(this, "Deployment Information Not Set;  Go to " +
    				"http://is4server.com/energylens to set it.",
					 Toast.LENGTH_LONG).show();
    		return false;
    	}
    	return true;
    }
    
    public void displayMsg(String msg){
    	Toast.makeText(this, msg,Toast.LENGTH_LONG).show();
    }
    
    public void startScanning(){
    	LocSenseFingerPrint.scanModeEnabled = true;
    	lightDialogIsActive = true;
    	showDialog(2);
    	
    	t = ScanType.LIGHT;
    	timer = new Timer();
		timer.scheduleAtFixedRate(new ScanSetTask(handler), 100L, 1000L);
	    setupLightSensing();
    }
    
    public void wifiSetup(){
    	if(wifiMngr ==null)
	    	// Setup WiFi
	    	wifiMngr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	
    	//set up the receiver
    	if(receiver==null){
    		SharedPreferences bufferPref = this.getSharedPreferences(GlobalConstants.BUFFER_DATA, 
					android.content.Context.MODE_PRIVATE);
	    	receiver = new WifiScanReceiver(this, currLocString, bufferPref);
	    	registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    	}
    }
    
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	String msgType = msg.getData().getString("msgtype");
        	if(msgType.equals("countdown")){
	            // Get the current value of the variable total from the message data
	            // and update the progress bar.
        		//Log.i("OSMsgHandler", "msg==countdown");
	            int total = scanTime - msg.getData().getInt("counter");
	            
	            if(t==ScanType.WIFI)
	            	wifiProgDialog.setProgress(total);
	            else if(t==ScanType.LIGHT)
	            	lightProgDialog.setProgress(total);
	            else if(t==ScanType.SOUND)
	            	soundProgDialog.setProgress(total);
	            
	            if (total <= 0 && t==ScanType.WIFI && wifiDialogIsActive){
	                dismissDialog(1);
	                t=ScanType.NONE;
	            } else if(total<=0 && t==ScanType.LIGHT && lightDialogIsActive){
	            	dismissDialog(2);
	            } else if(total<=0 && t==ScanType.SOUND){
	            	AmbientNoiseRecorder.stopRecording();
	            	if(soundDialogIsActive)
	            		dismissDialog(3);
	            }
        	} else if(msgType.equals("sched_wifi")){
        		scanWifi();
        	} else if(msgType.equals("sched_sound")){
        		scanSound();
        	}
        }
    };
    
    public void scanWifi(){
    	wifiSetup();
    	t = ScanType.WIFI;
    	
    	//start scanning wifi
    	wifiMngr.startScan();
    	timer = new Timer();
    	timer.scheduleAtFixedRate(new ScanSetTask(handler), 0L, 1000L);
    	wifiDialogIsActive = true;
    	showDialog(1);
    }
    
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    }

    public void onNothingSelected(AdapterView parent) {
      // Do nothing.
    }

    public void scanSound(){
    	noiseRecorder = new AmbientNoiseRecorder(bufferPref);
    	LocSenseFingerPrint.scanModeEnabled = true;
    	t = ScanType.SOUND;
    	timer = new Timer();
		timer.scheduleAtFixedRate(new ScanSetTask(handler), 100L, 1000L);
		soundDialogIsActive = true;
	    showDialog(3);
	    noiseRecorder.start();
    }
    
    protected class ScanSetTask extends TimerTask{
    	
    	Handler mHandler;
    	
    	public ScanSetTask(Handler handler){
    		mHandler = handler;
    		synchronized(this){
    			LocSenseFingerPrint.countDownCnter = LocSenseFingerPrint.scanTime;
    			LocSenseFingerPrint.scanModeEnabled = true;
    			Log.i(ScanSetTask.class.toString(), "ScanSetTask started");
    		}
    	}
    	
    	public void run(){
    		if(countDownCnter==0){
	    		synchronized(this){
	    			LocSenseFingerPrint.scanModeEnabled = false;
	    			LocSenseFingerPrint.countDownCnter = LocSenseFingerPrint.scanTime;
	    			if(receiver !=null)
	    				unregisterReceiver(receiver);
	        		receiver = null;
	        		Log.i(ScanSetTask.class.toString(), "DONE");
	        		timer.cancel();
	        		timer = null;
	        		timer = new Timer();
	        		
	        		//now scan the light sensor
	    			if(t == ScanType.LIGHT){
	    				Message msg = mHandler.obtainMessage();
	    	            Bundle b = new Bundle();
	    	            b.putString("msgtype", "countdown");
	    	            b.putInt("counter", countDownCnter);
	    	            msg.setData(b);
	    	            mHandler.sendMessage(msg);
	    	            
	    	            msg = mHandler.obtainMessage();
	    	            b = new Bundle();
	    	            b.putString("msgtype", "sched_wifi");
	    	            msg.setData(b);
	    	            mHandler.sendMessage(msg);
	    	            
	    			    return;
	    			} else if(t == ScanType.WIFI){
	    				Message msg = mHandler.obtainMessage();
	    	            Bundle b = new Bundle();
	    	            b.putString("msgtype", "countdown");
	    	            b.putInt("counter", countDownCnter);
	    	            msg.setData(b);
	    	            mHandler.sendMessage(msg);
	    	            
	    	            msg = mHandler.obtainMessage();
	    	            b = new Bundle();
	    	            b.putString("msgtype", "sched_sound");
	    	            msg.setData(b);
	    	            mHandler.sendMessage(msg);
	    	            
	    			    return;
	    			}
	    		}
    		} else {
    			
    			countDownCnter--;
    			String msg;
    			if(LocSenseFingerPrint.countDownCnter==0)
    	    		msg = "DONE SCANNING!";
    	    	else
    	    		msg = "Time remaining=" + LocSenseFingerPrint.countDownCnter;
    			Log.i(ScanSetTask.class.toString(), msg);
    		}

    		Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("msgtype", "countdown");
            b.putInt("counter", countDownCnter);
            msg.setData(b);
            mHandler.sendMessage(msg);
    	}
    }
    
    protected Dialog onCreateDialog(int id) {
        switch(id) {
	        /*case 0:                      // Spinner
	            progDialog = new ProgressDialog(this);
	            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            progDialog.setMessage("Scanning...");
	            return progDialog;*/
	        case 1:                      // Horizontal
	        	wifiProgDialog = new ProgressDialog(this);
	            wifiProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	            wifiProgDialog.setMax(10);
	            wifiProgDialog.setMessage("Scanning Wifi...");
	            wifiDialogIsActive = true;
	        	return wifiProgDialog;
	        case 2:                      // Horizontal
	        	lightProgDialog = new ProgressDialog(this);
	        	lightProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        	lightProgDialog.setMax(10);
	        	lightProgDialog.setMessage("Recording light...");
	        	lightDialogIsActive = true;
	        	return lightProgDialog;
	        case 3:                      // Horizontal
	        	soundProgDialog = new ProgressDialog(this);
	        	soundProgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	        	soundProgDialog.setMax(10);
	        	soundProgDialog.setMessage("Recording sound...");
	        	soundDialogIsActive = true;
	        	return soundProgDialog;
	        default:
	            return null;
        }
    }
    
    public void onDismiss (DialogInterface dialog){
    	if(t==ScanType.LIGHT){
    		lightDialogIsActive = false;
    	}
    	else if(t==ScanType.SOUND){
    		soundDialogIsActive = false;
    	}
    	else if(t==ScanType.WIFI){
    		wifiDialogIsActive=false;
    	}
    }
        
    public void setupLightSensing(){
    	//light sensor setup
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	    if (lightSensor == null){
	    	Toast.makeText(LocSenseFingerPrint.this,"No Light Sensor! quit-",Toast.LENGTH_LONG).show();
	    }else{
	    	//float max =  lightSensor.getMaximumRange();             
	    	sensorManager.registerListener(lightSensorEventListener, lightSensor, 
	    			SensorManager.SENSOR_DELAY_FASTEST);
     
	    }
    }
    
    
    SensorEventListener lightSensorEventListener = new SensorEventListener(){
		
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		 // TODO Auto-generated method stub
		}
			 
		public void onSensorChanged(SensorEvent event) {
			SFSConnector sfsconn = null;
			try {
		    	URL hostport = new URL(GlobalConstants.HOST);
		    	int port = hostport.getPort();
		    	if(port==-1)
		    		port=80;
		    	sfsconn = new SFSConnector(hostport.getHost(), hostport.getPort());
		    } catch(Exception e){
		    	e.printStackTrace();
		    }
			// TODO Auto-generated method stub
			if(event.sensor.getType()==Sensor.TYPE_LIGHT &&
					LocSenseFingerPrint.scanModeEnabled && t==ScanType.LIGHT){
				
				float currentReading = event.values[0];
				Log.i(LocSenseFingerPrint.class.toString(), "currentReading=" + currentReading);
				JSONObject datapt = new JSONObject();
				long ts = new Date().getTime()/1000;
				try {
		    		datapt.put("ts", ts);
		    		datapt.put("value", (double)currentReading);
				} catch (Exception e){
					e.printStackTrace();
				}
	    		
	    		String loc_path = Util.cleanPath(currLocString);
	    		
	    		Log.i(LocSenseFingerPrint.class.toString(), "checking: " + 
	    				GlobalConstants.HOST + loc_path);
	    		if(loc_path != null && sfsconn.exists(loc_path)){
	    			String bssid = "reading";
	    			String stream_path = loc_path + "qr_light/" + bssid;
	    			Log.i(LocSenseFingerPrint.class.toString(), "stream_path=" + stream_path);
	    			if(!sfsconn.exists(stream_path)){
	    				if(!sfsconn.exists(loc_path + "qr_light"))
	    					sfsconn.mkrsrc(loc_path, "qr_light", "default");
	    				sfsconn.mkrsrc(loc_path + "qr_light/", bssid, "genpub");
	    				JSONObject propsObj = new JSONObject();
	    				try {
	    					propsObj.put("units", "lumen");
	    				}catch (Exception e){
	    					e.printStackTrace();
	    				}
	    				sfsconn.updateProps(loc_path + "qr_light/" + bssid, propsObj.toString());
	    			}
	    			String pubid;
	    			if(!pubidHashMap.containsKey(loc_path + "qr_light/" + bssid)){
	    				pubid = sfsconn.getPubId(loc_path + "qr_light/" + bssid);
	    				if(pubid!=null)
	    					pubidHashMap.put(loc_path + "qr_light/" + bssid, pubid);
	    			}
	    			else
	    				pubid = pubidHashMap.get(loc_path + "qr_light/" + bssid);
	    			
	    			JSONArray newStreamBuf = new JSONArray();
	    			SharedPreferences.Editor editor = bufferPref.edit();
    				if(pubid!=null){
    					//post all the rest that you couldn't post before
    					
						String thisStreamBufStr = bufferPref.getString(loc_path + "qr_light/" + bssid, null);
						if(thisStreamBufStr != null){
							Log.i(LocSenseFingerPrint.class.toString(), "Draining buffer for " + loc_path);
							try {
								boolean postOk = true;
								JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
								for(int i=0; i<thisStreamBuf.length(); ++i){
									JSONObject thisDatapt = thisStreamBuf.getJSONObject(i);
									postOk = sfsconn.putStreamData(loc_path + 
											"qr_light/" + bssid,pubid, thisDatapt.toString());
									if(postOk){
										Log.i(LocSenseFingerPrint.class.toString(), "\n\tposted: " + thisDatapt.toString());
									} else {
										newStreamBuf.put(thisDatapt);
										Log.i(LocSenseFingerPrint.class.toString(), "\n\tCould not post, saving: " 
												+ thisDatapt.toString());
									}
								}
							
								//post the new data point
								postOk = sfsconn.putStreamData(loc_path + "qr_light/" + 
										bssid,pubid, datapt.toString());
								if(postOk){
									Log.i(LocSenseFingerPrint.class.toString(), "\n\tposted: " + datapt.toString());
								} else {
									newStreamBuf.put(datapt);
									Log.i(LocSenseFingerPrint.class.toString(), "\n\tCould not post, saving: " 
											+ datapt.toString());
								}
							} catch (Exception e){
								e.printStackTrace();
							}
						}
	    				else { //no local buffer entry for this path, so just try to post it directly
	    					try {
		    					boolean postOk=true;
		    					postOk = sfsconn.putStreamData(loc_path + "qr_light/" + 
		    							bssid,pubid, datapt.toString());
		    					if(postOk){
		    						Log.i(LocSenseFingerPrint.class.toString(), "\n\tposted: " + datapt.toString());
		    					} else {
		    						newStreamBuf.put(datapt);
		    						Log.i(LocSenseFingerPrint.class.toString(), "\n\tCould not post, saving: " 
		    								+ datapt.toString());
		    					}
	    					} catch(Exception e){
	    						e.printStackTrace();
	    					}
	    				}
	    				
		    		} else { //save it into local buffer and post when resource become available again
		    			try {
							String thisStreamBufStr = bufferPref.getString(loc_path + "qr_light/" + bssid, null);
							if(thisStreamBufStr != null){
								JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
								for(int i=0; i<thisStreamBuf.length(); i++)
									newStreamBuf.put(thisStreamBuf.get(i));
							}
						
							newStreamBuf.put(datapt);
						} catch(Exception e){
							e.printStackTrace();
						}
		    		}
    				
    				//replace the buffer if necessary; otherwise remove it
    				editor.remove(loc_path);
    				//if(newStreamBuf.length()>0){
					editor.putString(loc_path + "wifi/" + bssid, newStreamBuf.toString());
					Log.i(LocSenseFingerPrint.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" ["+
							loc_path + ", buffer=" + newStreamBuf.toString());
    				//}
    				editor.commit();
				} else {
					Log.i(LocSenseFingerPrint.class.toString(), "Scan not enabled, not recording value");
				}
			}
		}		      
	};
	
	
	
	
	
	
	
	
}
