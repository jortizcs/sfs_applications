package com.example.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class InitScreen extends Activity {
	

	SensorCollector sc;
	public float xPos;
	public float yPos;
	File logFile;
	/** Called when the activity is first created. */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		
		Button scanButton = (Button) findViewById(R.id.scanButton);
		scanButton.setOnClickListener(scanButtonListener);
		
		Button stopButton = (Button) findViewById(R.id.stopButton);
		stopButton.setOnClickListener(stopButtonListener);
		
		Button locationButton = (Button) findViewById(R.id.locationButton);
		locationButton.setOnClickListener(locationButtonListener);
		
		File sdDir = new File("/sdcard/LocationTracker");
		sdDir.mkdirs();
		logFile = new File("/sdcard/LocationTracker/abcd.txt");
			try {
				sc = new SensorCollector(getApplicationContext(),logFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			Toast.makeText(getApplicationContext(), "exception being thrown", 10);
			}
		
		
	}
	private OnClickListener scanButtonListener = new OnClickListener() {
		public void onClick(View v)
		{
		
			sc.start();
			Toast.makeText(getApplicationContext(), "Starting scanning", Toast.LENGTH_LONG).show();
		}
		
    };

	private OnClickListener stopButtonListener = new OnClickListener() {
        public void onClick(View v)
        {
            sc.pause();
            Toast.makeText(getApplicationContext(), "Stopping scanning", Toast.LENGTH_LONG).show();
            
        }
    };
    
    private OnClickListener locationButtonListener = new OnClickListener() {
        public void onClick(View v)
        {
            Intent chooseLoc = new Intent(InitScreen.this,ChooseLocation.class);
            startActivityForResult(chooseLoc, 1);
            
        }
    };
    @Override 
    public void onActivityResult(int requestCode, int resultCode, Intent data) {     
      super.onActivityResult(requestCode, resultCode, data); 
      switch(requestCode) { 
        case (1) : { 
          if (resultCode == Activity.RESULT_OK) { 
        	  Bundle extras = data.getExtras();
        	  xPos = extras.getFloat("xPos");
        	  yPos = extras.getFloat("yPos");
        	  Toast.makeText(getApplicationContext(), "Position returned : " + Float.toString(xPos) + "," + Float.toString(yPos), Toast.LENGTH_LONG).show();
          } 
          break; 
        } 
      } 
    }

}
