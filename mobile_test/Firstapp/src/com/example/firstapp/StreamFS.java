package com.example.firstapp;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;

public class StreamFS {
	static final String host = "http://ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080/";
	Cache cache;
	OpLog log;
	Activity app;
	
	public StreamFS(Activity app){
		cache = new Cache();
		log = new OpLog();
		this.app = app;
	}
	
	private boolean isConnected(){
		ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm!=null);
	}
	
	public JSONObject get(String path){
		if(isConnected()){
			try{
				String objstr = CurlOps.get(host+path);
				JSONObject object = new JSONObject(objstr);
				return object;
			}catch(Exception e){
				return null;
			}
		}else{
			return cache.getEntry(path);
		}
	}
	
	public void put(JSONObject data, String path){
		if(isConnected()){
			if(!log.isEmpty()){
				//log.flush();
			}
			try{
				CurlOps.put(data.toString(), host+path);
			}catch(Exception e){
				return;
			}
		}else{
			log.addEntry(path, data);
			cache.addEntry(path, data);
		}
	}
}
