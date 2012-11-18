package com.example.firstapp;

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.Date;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class Core {
	static final String host = "http://ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080/";
	Activity app;
	Cache cache;
	OpLog oplog;
	public Core(Activity app){
		this.app = app;
		cache = new Cache();
		oplog = new OpLog();
		Thread prefetcher = new Thread(new Prefetcher(this));
		prefetcher.run();
	}
	
	public void putEntry(String path, JSONObject data) {
		Log.d("log_core", "add entry called");
		if(isConnected()){
			Log.d("log_core", "connected;push data to server");
			try{
				CurlOps.put(data.toString(), host+path+"?type=generic&pubid=e218a0d5-51e3-4056-9341-a1e6cc19605d");
				getEntry(path); // for caching
			}catch(Exception e){
				Log.d("log_core", "exception:"+e);
				Log.d("log_core", "disconnected; push to oplogs");
				pseudoPutEntry(path,data);
				return;
			}
		}else{
			Log.d("log_core", "disconnected; push to oplogs");
			pseudoPutEntry(path,data);
		}
	}
	public void pseudoPutEntry(String path, JSONObject data){
		oplog.addEntry(path, data);
		JSONObject newData = getEntry(path);
		try{
			JSONObject head = newData.getJSONObject("head");
			head.put("value", data.get("value"));
			head.put("ts", (new Date()).getTime());
			newData.put("head", head);
		}catch(Exception e){
			return;
		}
		cache.addEntry(path, newData);
	}
	public JSONObject getEntry(String path){
		if(isConnected()){
			try{
				String objstr = CurlOps.get(host+path);
				JSONObject object = new JSONObject(objstr);
				cache.addEntry(path, object);
				return object;
			}catch(Exception e){
				return cache.getEntry(path);
			}
		}else{
			return cache.getEntry(path);
		}
	}
	
	private boolean isConnected(){
		ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm.getActiveNetworkInfo()!=null);
	}
	
}
