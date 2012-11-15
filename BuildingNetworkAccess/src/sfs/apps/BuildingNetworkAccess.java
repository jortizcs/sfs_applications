package sfs.apps;

import sfs.lib.*;

import java.util.*;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.net.ConnectivityManager;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.*;

import org.json.*;

public class BuildingNetworkAccess extends Activity {
	
	public static String DEBUG_TAG = "BuildingNetworkAccess::";
	public static NetAccessAsyncTask t=null;
	
	//server time/local time (ms)
	public static long serverTime = -1L;
	public static long localTime = -1L;
	
	//main server
	public static final String SERVER = "http://energylens.sfsprod.is4server.com:8080";
	
	//relevant streams
	public static final String BASE = "/connexp";
	public static final String ALL_TYPE = BASE+"/all/type";//0-wifi, 1-3g, 2-4g
	public static final String ALL_NETACCESS = BASE+"/all/access"; //0-false, 1-true
	public static final String ALL_TYPE_PID = "244fcf93-4f89-4fd3-8def-ce6f407cd04c";
	public static final String ALL_NETACCESS_PID = "82af7423-b246-451e-a1af-dace0e5f2601";
	
	public static final String SODA_TYPE = BASE+"/soda/type";
	public static final String SODA_NETACCESS= BASE+"/soda/access";
	public static final String SODA_LOCATION=BASE+"/soda/loc";
	public static final String SODA_TYPE_PID = "94a02479-e410-46f1-a648-4c34cc49ef9c";
	public static final String SODA_NETACCESS_PID = "3c0c8b34-f2ee-4d29-a005-4c16746db1df";
	public static final String SODA_LOCATION_PID = "f3f5f5ae-9746-46b8-af52-cc5d6e8b8316";
	
	public static final String SDH_TYPE = BASE+"/sdh/type";
	public static final String SDH_NETACCESS = BASE+"/sdh/access";
	public static final String SDH_LOCATION = BASE+"/sdh/loc";
	public static final String SDH_TYPE_PID = "22321ba6-bbbc-453b-a9e8-00bb5ceb8061";
	public static final String SDH_NETACCESS_PID = "a3775e52-fa5a-4d0f-ae9f-3c77a4631d92";
	public static final String SDH_LOCATION_PID = "59ae19bc-4f22-47a9-bf8c-37d9def81a9c";
	
	public static final String CORY_TYPE = BASE+"/cory/type";
	public static final String CORY_NETACCESS = BASE+"/cory/access";
	public static final String CORY_LOCATION = BASE+"/cory/loc";
	public static final String CORY_TYPE_PID = "4e3f5628-bc6f-4a84-bd37-ad6ff1cf974e";
	public static final String CORY_NETACCESS_PID = "ae480f6f-4b50-48d3-a666-66b4f2e85af4";
	public static final String CORY_LOCATION_PID = "612e1479-9ddd-47cc-91b7-90621bdc7ca7";
	
	public static final String MALL_TYPE=BASE+"/mall/type";
	public static final String MALL_NETACCESS=BASE+"/mall/netaccess";
	public static final String MALL_LOCATION = BASE+"/mall/loc";
	public static final String MALL_TYPE_PID="14004c89-4904-4ca5-bdf2-80792ad6d3e4";
	public static final String MALL_NETACCESS_PID="b2ebbafd-f154-45e4-97e2-e8706d775e75";
	public static final String MALL_LOCATION_PID = "ece43cfb-5d09-4278-b799-b320b3093b7e";
	
	public static String CURRENT_CONTEXT = null;
	public static String CURRENT_LOC_ID = null;
	private static Spinner contextSpinner = null;
	private static EditText locIdEditTxt = null;
	
	private static ConnectivityManager connMngr=null;
	private static Handler handler = null;
	private static ProgressBar pbar=null;
	
	private static String NETACCESS_BUFFERED_DATA = "NETACCESS_BUFFERED_DATA";
	
	public static String currentContext = null;//children of /connexp
	public static String currentLocationId = null;//specific location id that refers to landmark
	
	public static boolean netAccessRunning = false;
	public static String[] items;
	
	public static SharedPreferences bufferPref=null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if(bufferPref==null){
        	bufferPref = this.getSharedPreferences("BUFFER_DATA", 
				android.content.Context.MODE_PRIVATE);
        }
        
        //populate the Spinner
        populateSpinners();
        
        //set the context and location id
		if(contextSpinner==null)
			contextSpinner = (Spinner)findViewById(R.id.building);
		if(locIdEditTxt==null)
			locIdEditTxt = (EditText)findViewById(R.id.EditText01);
		CURRENT_CONTEXT = contextSpinner.getSelectedItem().toString();
		CURRENT_LOC_ID = locIdEditTxt.getText().toString();
        
        if(connMngr==null)
        	connMngr = (ConnectivityManager)getSystemService(BuildingNetworkAccess.CONNECTIVITY_SERVICE);
        setupButtons();
        
        if(bufferPref==null){
	        bufferPref = this.getSharedPreferences(NETACCESS_BUFFERED_DATA, 
					android.content.Context.MODE_PRIVATE);
        }
        
        TextView progressDoneTxt = (TextView)findViewById(R.id.NetAccessProgressDone);
    	progressDoneTxt.setText("");
		
        handler = new Handler(){
        	  @Override
        	  public void handleMessage(Message msg) {
        		double leveld = 1;
    		    if(!msg.getData().getString("progress_level").equalsIgnoreCase("done"))
    		    	leveld = Double.parseDouble(msg.getData().getString("progress_level"));
				pbar = (ProgressBar)findViewById(R.id.ProgressBar01);
				int val = new Double(leveld*(double)pbar.getMax()).intValue();
				Log.i("handler::max=", new Integer(pbar.getMax()).toString() + ", val=" + new Integer(val).toString());
				pbar.setProgress(val);
				
				String statusLine = msg.getData().getString("progress_msg");
				if(statusLine!=null){
					EditText statusEditText = (EditText)findViewById(R.id.NetAccessStream);
					statusEditText.append(statusLine);
				}
				if(msg.getData().getString("progress_level").equalsIgnoreCase("done")){
					TextView progressDoneTxt = (TextView)findViewById(R.id.NetAccessProgressDone);
					progressDoneTxt.setText("Finshed Experiment");
				}
				
				//set the context and location id
				if(contextSpinner==null)
					contextSpinner = (Spinner)findViewById(R.id.building);
				if(locIdEditTxt==null)
					locIdEditTxt = (EditText)findViewById(R.id.EditText01);
				CURRENT_CONTEXT = contextSpinner.getSelectedItem().toString();
				CURRENT_LOC_ID = locIdEditTxt.getText().toString();
				
				Log.i(BuildingNetworkAccess.DEBUG_TAG,contextSpinner.getSelectedItem().toString());
				Log.i(BuildingNetworkAccess.DEBUG_TAG,locIdEditTxt.getText().toString());
        	  }
        	};
    }
    
    private void populateSpinners() {
    	try {
    		Spinner contextSpinner = (Spinner)findViewById(R.id.building);
    		if(items!=null){
		    	ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_list_item_1,items);
				aa.setDropDownViewResource(
				   android.R.layout.simple_spinner_dropdown_item);
				contextSpinner.setAdapter(aa);
    		} else {
		    	SFSConnector connector = new SFSConnector("energylens.sfsprod.is4server.com", 8080);
		    	JSONArray contextList = connector.getChildren(BASE);
		    	if(contextList!=null){
		    		items = new String[contextList.length()];
			    	for(int i=0; i<contextList.length(); i++)
			    		items[i] = contextList.getString(i);
			    	ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_list_item_1,items);		
					aa.setDropDownViewResource(
					   android.R.layout.simple_spinner_dropdown_item);
					contextSpinner.setAdapter(aa);
		    	}
    		}
    	} catch(Exception e){
    		Log.e(this.DEBUG_TAG, "", e);
    	}
		
	}
    
    /*public void onItemSelected(AdapterView<?> parent, View v, int position,
			long id) {
		Log.i(this.DEBUG_TAG, items[position]);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}*/

	public void setupButtons(){
		//set up the start button
		Button startButton = (Button) findViewById(R.id.startButton);
			startButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v){
					//start trying to contact server
					if(!netAccessRunning){
						t = new NetAccessAsyncTask(connMngr, 
								handler);
						t.execute();
						TextView progressDoneTxt = (TextView)findViewById(R.id.NetAccessProgressDone);
		    	    	progressDoneTxt.setText("Experiment started...");
					}

			    	EditText statusEditText = (EditText)findViewById(R.id.NetAccessStream);
				    statusEditText.setText("");
				}
		});
		
		//set up the stop button
		Button stopButton = (Button) findViewById(R.id.stopButton);
		  stopButton.setOnClickListener(new OnClickListener() {
		  	public void onClick(View v){
		  		//stop trying to contact server
        	    pbar = (ProgressBar)findViewById(R.id.ProgressBar01);
        	    pbar.setProgress(0);
		  		t.cancel(true);
		  		TextView progressDoneTxt = (TextView)findViewById(R.id.NetAccessProgressDone);
    	    	progressDoneTxt.setText("Experiment stopped...");
		  		netAccessRunning = false;
		  	}
		  });
		  
		//set up the quit button
		Button quitButton = (Button) findViewById(R.id.quitButton);
		quitButton.setOnClickListener(new OnClickListener() {
		  		public void onClick(View v){
		  			System.exit(1);
		  		}
		  });

		//set up the stop button
		Button showdata = (Button) findViewById(R.id.DataShowButton);
		showdata.setOnClickListener(new OnClickListener() {
		  	public void onClick(View v){
		  		EditText statusEditText = (EditText)findViewById(R.id.NetAccessStream);
		  		statusEditText.setText("");
				statusEditText.append(bufferPref.getAll().keySet().toString());
				statusEditText.append("\nSIZE (bytes)="+bufferPref.getAll().toString().getBytes().length);
			
		  	}
		  });
		
		//set up the stop button
		Button cleardata = (Button) findViewById(R.id.DataClearButton);
		cleardata.setOnClickListener(new OnClickListener() {
		  	public void onClick(View v){
		  		EditText statusEditText = (EditText)findViewById(R.id.NetAccessStream);
				SharedPreferences.Editor e = bufferPref.edit();
				e.clear();
				e.commit();
		  		statusEditText.setText("");
		  		statusEditText.append(bufferPref.getAll().keySet().toString());
				statusEditText.append("\nSIZE (bytes)="+bufferPref.getAll().toString().getBytes().length);
		  	}
		  });
		
		//set up the stop button
		Button sendata = (Button) findViewById(R.id.SendDataButton);
		sendata.setOnClickListener(new OnClickListener() {
		  	public void onClick(View v){
		  		EditText statusEditText = (EditText)findViewById(R.id.NetAccessStream);
		  		statusEditText.setText("Sending...");
		  		Iterator keys = bufferPref.getAll().keySet().iterator();
		  		int totalBytesSent=0;
		  		while(keys.hasNext()){
		  			try {
			  			String thisPath = (String)keys.next();
			  			String thisPubid = getAssociatedPubId(thisPath);
			  			JSONArray data = new JSONArray(bufferPref.getString(thisPath, new JSONArray().toString()));
			  			if(thisPubid!=null){
			  				SFSConnector sfsConnector = new SFSConnector("energylens.sfsprod.is4server.com", 8080);
			  				boolean sendOk=sfsConnector.bulkdDataPost(thisPath, thisPubid, data);
			  				if(sendOk){
			  					totalBytesSent+=data.toString().getBytes().length;
			  					statusEditText.append("\nSent:[" + thisPath + ", bytes=" + data.toString().getBytes().length);
			  					SharedPreferences.Editor e = bufferPref.edit();
			  					e.remove(thisPath);
			  					e.commit();
			  				}
			  			} 
		  			} catch(Exception e){
		  				Log.e(BuildingNetworkAccess.DEBUG_TAG, "", e);
		  			}
		  		}
		  		statusEditText.append("\nTotal Bytes Sent=" + totalBytesSent);
		  	}
		  });
		
		
		//setup the experiment spinner
		Spinner buildingChoice = (Spinner)findViewById(R.id.building);
		buildingChoice.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view, int pos,long id){
				//do something
			}
			public void onNothingSelected(AdapterView<?> arg0) {
				//do nothing
			  }
		});
    }
    
    
	protected static String getAssociatedPubId(String path){
		if(path.equals(BuildingNetworkAccess.ALL_NETACCESS))
			return BuildingNetworkAccess.ALL_NETACCESS_PID;
		else if(path.equals(BuildingNetworkAccess.ALL_TYPE))
			return BuildingNetworkAccess.ALL_TYPE_PID;
		else if(path.equals(BuildingNetworkAccess.SODA_TYPE))
			return BuildingNetworkAccess.SODA_TYPE_PID;
		else if(path.equals(BuildingNetworkAccess.SODA_NETACCESS))
			return BuildingNetworkAccess.SODA_NETACCESS_PID;
		else if(path.equals(BuildingNetworkAccess.SODA_LOCATION))
			return BuildingNetworkAccess.SODA_LOCATION_PID;
		else if(path.equals(BuildingNetworkAccess.CORY_TYPE))
			return BuildingNetworkAccess.CORY_TYPE_PID;
		else if(path.equals(BuildingNetworkAccess.CORY_NETACCESS))
			return BuildingNetworkAccess.CORY_NETACCESS_PID;
		else if(path.equals(BuildingNetworkAccess.CORY_LOCATION))
			return BuildingNetworkAccess.CORY_LOCATION_PID;
		else if(path.equals(BuildingNetworkAccess.SDH_TYPE))
			return BuildingNetworkAccess.SDH_TYPE_PID;
		else if(path.equals(BuildingNetworkAccess.SDH_NETACCESS))
			return BuildingNetworkAccess.SDH_NETACCESS_PID;
		else if(path.equals(BuildingNetworkAccess.SDH_LOCATION))
			return BuildingNetworkAccess.SDH_LOCATION_PID;
		else if(path.equals(BuildingNetworkAccess.MALL_TYPE))
			return BuildingNetworkAccess.MALL_TYPE_PID;
		else if(path.equals(BuildingNetworkAccess.MALL_NETACCESS))
			return BuildingNetworkAccess.MALL_NETACCESS_PID;
		else if(path.equals(BuildingNetworkAccess.MALL_LOCATION))
			return BuildingNetworkAccess.MALL_LOCATION_PID;
		return null;
	}
    
    
}