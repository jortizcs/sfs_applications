package mobile.SFS;

import java.util.StringTokenizer;
import java.util.Vector;

import org.json.JSONArray;

import android.app.Activity;
import android.util.Log;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MobileSFS extends Activity {
	private static final String DEFAULT_SPACE = GlobalConstants.SPACESHOME + "/room1";
	//private static final String DEFAULT_SPACE = "/buildings/SDH/spaces";
	//private static final String DEFAULT_SPACE = "/buildings/home/spaces";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobilesfs);
        Bundle extras = getIntent().getExtras();
        
        TextView currLoc = (TextView) findViewById(R.id.currLoc);
        String s = extras == null ? null : extras.getString("curr_loc");
        Log.i("MOBILE_SFS","s=" + s);
        final String currLocString = s == null ? DEFAULT_SPACE : s;
        Log.i("MOBILE_SFS","currLocString=" + currLocString);
        currLoc.setText(currLocString);
        if(extras==null){
        	getIntent().putExtra("curr_loc", currLocString);
        	Log.i("MOBILE_SFS", "Putting extras="+ currLocString);
        }
        
        Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent next = new Intent(MobileSFS.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(MobileSFS.this, MobileSFS.class));
				startActivity(next);
			}
		});
        
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem, new String[] {"Update Hierarchy", "View Services"}));
        //listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, new String[] {"Update Hierarchy", "View Services"}));
        
        listView.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		Intent intent = null;
        		
				switch(arg2) {
					case 0: 
						intent = new Intent(MobileSFS.this, UpdateHierarchy.class); 
						intent.putExtra("curr_loc", currLocString);
						startActivity(intent);
						break;
					case 1: {//intent = new Intent(MobileSFS.this, ViewServices.class); break;
						intent = new Intent("com.google.zxing.client.android.SCAN");
		        		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		        		intent.putExtra("curr_loc", currLocString);
		        		startActivityForResult(intent, 0);
		        		break;
					}
				}
				
				//intent.putExtra("curr_loc", currLocString);
				//startActivity(intent);
			}
        });
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
			/*Intent next = new Intent(this, ViewServices.class);
			next.putExtra("url", (intent.getStringExtra("SCAN_RESULT")));
			TextView currLoc = (TextView) findViewById(R.id.currLoc);
	        next.putExtra("curr_loc", currLoc.getText().toString());
			startActivity(next);*/
    	
    	String urlstr = intent.getStringExtra("SCAN_RESULT");//getIntent().getStringExtra("url");
        Uri uri = Uri.parse(urlstr);
        //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Intent next = new Intent(this, ViewServices.class);
        
        try {
        	String qrcStr = CurlOps.getQrcFromUrl(urlstr);
        	JSONArray cArray = Util.getChildren(GlobalConstants.HOST + GlobalConstants.QRCHOME + "/" + qrcStr);
        	Log.i("VIEW_SERVICES", "cArray=" + cArray.toString());
        	if(cArray.length()>0){
        		
        		//find out if the space of this resource is different from the current location
        		//TextView currLoc = (TextView) findViewById(R.id.currLoc);
        		Bundle extras = getIntent().getExtras();
        		TextView currLoc_ = (TextView) findViewById(R.id.currLoc);
        		if(extras==null)
        			extras.putString("curr_loc", currLoc_.getText().toString());
        		else
        			extras.putString("curr_loc", extras.getString("curr_loc"));
                currLoc_.setText(extras.getString("curr_loc"));
                next.putExtra("curr_loc", extras.getString("curr_loc"));
                Log.i("VIEW_SERVICES", "bundle.curr_loc_1=" + extras.getString("curr_loc"));
        		
        		//get the thing this qrc code points to
        		String thingStr = cArray.getString(0);
        		Log.i("VIEW_SERVICES", "thingStr=" + thingStr);
        		String childPath = Util.getUriFromQrc(qrcStr);
        		Log.i("VIEW_SERVICES", "childPath=" + childPath);
        		JSONArray paths = Util.getIncidentPaths(GlobalConstants.HOST + childPath);
        		Log.i("VIEW_SERVICES", "path=" + paths.toString());
        		for(int i=0; i<cArray.length(); i++){
        			
        			//check that the location of this item matches the current location
        			String thispath = paths.getString(i).replace("\"", "");
        			if(thispath.startsWith(GlobalConstants.SPACESHOME)){
        				String itemLocStr = Util.getLocation(qrcStr);
        				Log.i("VIEW_SERVICES", "thisIncidentPath[" + i + "]=" + paths.getString(i));
        				Log.i("VIEW_SERVICES", "LOCATION::" + itemLocStr);
        				StringTokenizer thisPathTokenizer = new StringTokenizer(thispath, "/");
        				StringTokenizer currLocTokenizer = new StringTokenizer(extras.getString("curr_loc"), "/");
        				int tokMatchCount =0;
        				final int tokMatchThresh = 4;
        				Vector<String> thisPathTokVec = new Vector<String>();
        				Vector<String> currLocTokVec = new Vector<String>();
        				while(thisPathTokenizer.hasMoreTokens())
        					thisPathTokVec.add(thisPathTokenizer.nextToken());
        				while(currLocTokenizer.hasMoreElements())
        					currLocTokVec.add(currLocTokenizer.nextToken());
        				
        				if(thisPathTokVec.size()>=tokMatchThresh && currLocTokVec.size()>=tokMatchThresh){
        					for(int j=0; j<tokMatchThresh; j++){
        						if(thisPathTokVec.get(j).equals(currLocTokVec.get(j)))
        							tokMatchCount+=1;
        					}
        					
        					//if there locations don't match, set the new location to the location
        					//of this item
        					if(tokMatchCount != tokMatchThresh){
        						Log.i("VIEW_SERVICES", "Item is in different location as current setting.");
        						StringBuffer newLocBuf = new StringBuffer();
        						for(int k=0; k<tokMatchThresh; k++)
        							newLocBuf.append("/").append(thisPathTokVec.get(k));
        						currLoc_.setText(newLocBuf.toString());
        						next.putExtra("curr_loc", newLocBuf.toString());
        					} else{
        						Log.i("VIEW_SERVICES", "Item is in same location as current setting");
        					}
        						
        				}
        				
        			}
        			else
        				Log.i("VIEW_SERVICES", paths.getString(i) + " does not start with " + GlobalConstants.SPACESHOME);
        		}
        		
        		
                
        	}
        	
        	/*WebView webview = (WebView) findViewById(R.id.webView);
            webview.loadUrl(urlstr);
            Log.i("VIEW_SERVICES::", urlstr);*/
        	next.putExtra("url", urlstr);
            startActivity(next);
        } catch(Exception e){
        	e.printStackTrace();
        	Log.d("VIEW_SERVICE::ERROR", "error while fetching " + urlstr);
        }
	}
}