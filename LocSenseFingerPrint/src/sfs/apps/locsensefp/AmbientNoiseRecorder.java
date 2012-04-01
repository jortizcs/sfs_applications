package sfs.apps.locsensefp;

import android.content.SharedPreferences;
import android.media.AudioRecord;
import android.util.Log;
import android.media.MediaRecorder.AudioSource;
import android.media.AudioFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;

import sfs.lib.Util;
import sfs.lib.SFSConnector;
import java.util.HashMap;

//import org.json.JSONObject;

public class AmbientNoiseRecorder extends Thread{
	private static AudioRecord recorder;
	private static boolean setupDone = false;
	private static boolean recording = false;
	private static int MINBUFSIZE = -1;
	
	private static HashMap<String, String> pubidHashMap;
	private static SharedPreferences bufferPref = null;
	
	public AmbientNoiseRecorder(SharedPreferences buffer){
		super();
		pubidHashMap = new HashMap<String, String>();
		bufferPref = buffer;
	}
	
	public boolean setup(){
		int buffsize = AudioRecord.getMinBufferSize(44100, 
				AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		MINBUFSIZE = buffsize;
		Log.d(AmbientNoiseRecorder.class.toString(), 
				"Minimum Buffer Audio Sampling Size(44100Hz, stereo, 16-bit)=" + buffsize);
		recorder = new AudioRecord(AudioSource.MIC, 
				44100,  
				AudioFormat.CHANNEL_CONFIGURATION_STEREO, 
				AudioFormat.ENCODING_PCM_16BIT, 
				buffsize);
		if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
			setupDone = true;
		else{
			Log.i(AmbientNoiseRecorder.class.toString(), "State=" + recorder.getState());
			setupDone = false;
		}
        return setupDone;
	}
	
	public void record(){
		if(!setupDone)
			setup();
		synchronized(this){
			recording = true;
			recorder.startRecording();
		}
		
		double db_avg = 0;
		//collect samples here
		long startTime = new Date().getTime()/1000;
		Log.i(AmbientNoiseRecorder.class.toString(), "starttime=" + startTime);
		while(recording){
			short[] buffer = new short[MINBUFSIZE];  
			int ret  = recorder.read(buffer, 0, MINBUFSIZE/2);
			//Log.i(AmbientNoiseRecorder.class.toString(), "Bytes recorded=" + ret*2 + " bytes");
			if(ret == AudioRecord.ERROR_INVALID_OPERATION){
				Log.i(AmbientNoiseRecorder.class.toString(),
						"recording returned ERROR_INVALID_OPERATION");
			} else if (ret == AudioRecord.ERROR_BAD_VALUE){
				Log.i(AmbientNoiseRecorder.class.toString(),
				"recording returned ERROR_BAD_VALUE");
			}else {	//print the value (or record it to streamsfs
				if(ret>0){
					StringBuffer msg = new StringBuffer();
					msg.append("[data=[");
					for(int i=0; i<ret-1; i++){
						msg.append(buffer[i]).append(",");
						
						//calc db
						double abs = Math.abs(new Short(buffer[i]).doubleValue());
						double v = abs/Short.MAX_VALUE;
						double db = Math.log10(v) * 20;
						//Log.i(AmbientNoiseRecorder.class.toString() + "::Power", "dbv=" + db + " dB");
						
						if(i==0)
							db_avg=db;
						else
							db_avg = (db+db_avg)/2;

						long currentTime = new Date().getTime()/1000;
						long diff = currentTime-startTime;
						/*Log.i(AmbientNoiseRecorder.class.toString()+"::TIME", 
								"startTime= " + startTime + ", currentTime=" + currentTime + ", diff=" + diff);*/
						if(diff >= 1 && db_avg != Double.NEGATIVE_INFINITY){
							Log.i(AmbientNoiseRecorder.class.toString() + "::AVG_POWER", 
									"[db_avg=" + db_avg + ", ts=" + currentTime + "]");
							postIt(db_avg, currentTime);
							db_avg=db;
							startTime = new Date().getTime()/1000;
						}
					}
					msg.append(buffer[ret-1]);
					msg.append("], ts=").append((new Date()).getTime()/1000).append("]");
					//Log.i(AmbientNoiseRecorder.class.toString(), msg.toString());
				}
			}
			
		}
		
		synchronized(this){
			recorder.stop();
			recorder.release();
			setupDone = false;
		}
		
		
	}
	
	public synchronized static void stopRecording(){
		recording = false;
	}
	
	public void run(){
		record();
	}

	public void postIt(double currentReading, long ts){
		/*float currentReading = event.values[0];
		Log.i(AmbientNoiseRecorder.class.toString(), "currentReading=" + currentReading);
		long ts = new Date().getTime()/1000;*/
		JSONObject datapt = new JSONObject();
		try {
    		datapt.put("ts", ts);
    		datapt.put("value", currentReading);
		} catch (Exception e){
			e.printStackTrace();
		}
		
		String loc_path = Util.cleanPath(LocSenseFingerPrint.currLocString);
		URL u;
		try {
			u= new URL(GlobalConstants.HOST);
		} catch(Exception e){
			e.printStackTrace();
			return;
		}
		int port= 80;
		if(u.getPort()>0)
			port = u.getPort();
		SFSConnector sfsconn = new SFSConnector(u.getHost(), port);
		
		Log.i(AmbientNoiseRecorder.class.toString(), "checking: " + 
				GlobalConstants.HOST + loc_path);
		String bssid = "reading";
		if(loc_path != null && sfsconn.exists(loc_path)){
			String stream_path = loc_path + "sound/" + bssid;
			Log.i(AmbientNoiseRecorder.class.toString(), "stream_path=" + stream_path);
			if(!sfsconn.exists(stream_path)){
				if(!sfsconn.exists(loc_path + "sound"))
					sfsconn.mkrsrc(loc_path, "sound", "default");
				sfsconn.mkrsrc(loc_path + "sound/", bssid, "genpub");
				JSONObject propsObj = new JSONObject();
				try {
					propsObj.put("units", "lumen");
				}catch (Exception e){
					e.printStackTrace();
				}
				sfsconn.updateProps(loc_path + "sound/" + bssid, propsObj.toString());
			}
			String pubid;
			if(!pubidHashMap.containsKey(loc_path + "sound/" + bssid)){
				pubid = sfsconn.getPubId(loc_path + "sound/" + bssid);
				if(pubid!=null)
					pubidHashMap.put(loc_path + "sound/" + bssid, pubid);
			}
			else
				pubid = pubidHashMap.get(loc_path + "sound/" + bssid);
			
			//now post it or save it locally if the server is down
			JSONArray newStreamBuf = new JSONArray();
			if(pubid!=null)
				postItOrSaveIt(sfsconn, loc_path, bssid, pubid, newStreamBuf, datapt);
			else
				bufferIt(loc_path, bssid, datapt);
				
		} else { //save it into local buffer and post when resource become available again
			bufferIt(loc_path, bssid, datapt);
		}
	} 
	
	private void postItOrSaveIt(SFSConnector sfsconn,String loc_path, String bssid, String pubid, 
			JSONArray newStreamBuf, JSONObject datapt){
		//post all the rest that you couldn't post before
		SharedPreferences.Editor editor = bufferPref.edit();
		String thisStreamBufStr = bufferPref.getString(loc_path + "sound/" + bssid, null);
		if(thisStreamBufStr != null){
			try {
				boolean postOk = true;
				JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
				
				for(int i=0; i<thisStreamBuf.length(); ++i){
					JSONObject thisDatapt = thisStreamBuf.getJSONObject(i);
					postOk = sfsconn.putStreamData(loc_path + "sound/" + 
							bssid,pubid, thisDatapt.toString());
					if(postOk){
						Log.i(AmbientNoiseRecorder.class.toString(), "\n\tposted: " + thisDatapt.toString());
					} else {
						newStreamBuf.put(thisDatapt);
						Log.i(LocSenseFingerPrint.class.toString(), "\n\tCould not post, saving: " 
								+ thisDatapt.toString());
					}
				}
				
				//post the new data point
				postOk = sfsconn.putStreamData(loc_path + "sound/" + 
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
		
		else {  //not in local buffer, so just try to post it and create a local buffer if the server is down
			try {
				boolean postOk=true;
				postOk = sfsconn.putStreamData(loc_path + "sound/" + 
						bssid,pubid, datapt.toString());
				if(postOk){
					Log.i(AmbientNoiseRecorder.class.toString(), "\n\tposted: " + datapt.toString());
				} else {
					newStreamBuf.put(datapt);
					Log.i(AmbientNoiseRecorder.class.toString(), "\n\tCould not post, saving: " 
							+ datapt.toString());
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		
		//replace the buffer if necessary; otherwise remove it
		editor.remove(loc_path);
		//if(newStreamBuf.length()>0) {
		editor.putString(loc_path + "wifi/" + bssid, newStreamBuf.toString());
		Log.i(AmbientNoiseRecorder.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
				+ loc_path + ", buffer=" + newStreamBuf.toString());
		//}
		editor.commit();
	  }
	  
	private void bufferIt(String loc_path, String bssid, JSONObject datapt){
	  try {
		Log.e(AmbientNoiseRecorder.class.toString(), "Could not get pubid for path " + 
				GlobalConstants.HOST + loc_path + "sound/" + bssid);
		SharedPreferences.Editor editor = bufferPref.edit();
		String thisStreamBufStr = bufferPref.getString(loc_path + "sound/" + bssid, null);
		if(thisStreamBufStr != null){
			JSONArray thisStreamBuf = new JSONArray(thisStreamBufStr);
			thisStreamBuf.put(datapt);
			editor.remove(loc_path);
			editor.putString(loc_path + "sound/" + bssid, thisStreamBuf.toString());
			Log.i(AmbientNoiseRecorder.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
					+ loc_path + "sound/" + bssid +", buffer=" + thisStreamBuf.toString());
		} else {
			JSONArray buf = new JSONArray();
			buf.put(datapt);
			editor.putString(loc_path + "sound/" + bssid, buf.toString());
			Log.i(AmbientNoiseRecorder.class.toString(), "saving in : " + GlobalConstants.BUFFER_DATA +" [" 
					+ loc_path + "sound/" + bssid +", buffer=" + buf.toString());
		}
		editor.commit();
	  } catch(Exception e){
		  e.printStackTrace();
	  }
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
