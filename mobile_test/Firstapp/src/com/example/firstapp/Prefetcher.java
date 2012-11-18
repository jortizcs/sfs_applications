package com.example.firstapp;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class Prefetcher implements Runnable {
	Core core;
	Timer timer;
	class PrefetcherTask extends TimerTask{
		public void run(){
			Log.d("log_prefetcher","prefetcher start");
			if(isConnected()){
				try{
					CurlOps.get(Core.host); //TO CHECK IF SERVER IS REACHABLE
					Log.d("log_prefetcher","app connected");
					if(!core.oplog.isEmpty()){
						LinkedBlockingQueue<Operation> log = core.oplog.flushLog();
						for(Operation op: log){
							core.putEntry(op.path, op.data);
						}
					}
					// update cache entries
					for(String path : core.cache.entries.keySet()){
						core.getEntry(path);
					}
				}catch(Exception e){
					Log.d("log_prefetcher","app still not connected (exception)");
				}
			}else{
				Log.d("log_prefetcher","app still not connected");
			}
		}
	}
	
	public Prefetcher(Core core){
		this.core = core;
		timer = new Timer();
	}
	private boolean isConnected(){
		ConnectivityManager cm = (ConnectivityManager) core.app.getSystemService(Context.CONNECTIVITY_SERVICE);
		return (cm.getActiveNetworkInfo()!=null);
	}
	public void run(){
		timer.schedule(new PrefetcherTask(), 0, 1000);
	}
}
