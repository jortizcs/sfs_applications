package com.example.firstapp;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;


public class MainActivity extends Activity {
	Timer timer = new Timer();
	Core core = new Core(this);
	class MyTask extends TimerTask{
		public void run(){
			JSONObject json = new JSONObject();
			try {
				json.put("value", "1");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			core.putEntry("yong/stream2", json);
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timer.schedule(new MyTask(), 0,5000);
        TextView view = (TextView)findViewById(R.id.hello);
        view.setText("hello");
       
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public void onStop(){
    	timer.cancel();
    	timer.purge();
    	timer = null;
    	super.onStop();
    }
    
    @Override
    public void onRestart(){
    	super.onRestart();
    	if(timer==null){
    		timer = new Timer();
    		timer.schedule(new MyTask(), 0,5000);
    	}
    }
}
