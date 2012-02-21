package mobile.SFS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import mobile.SFS.CurlOps;
import mobile.SFS.GlobalConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.StringTokenizer;
import java.util.Vector;
import java.lang.StringBuffer;


public class ViewServices extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewservices);
		
		Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(ViewServices.this, ChangeLocation.class));
			}
		});
        
        WebView webview = (WebView) findViewById(R.id.webView);
        String urlstr = getIntent().getStringExtra("url");
        webview.loadUrl(getIntent().getStringExtra("url"));
        Log.i("VIEW_SERVICES::", urlstr);
        try {
        	String qrcStr = CurlOps.getQrcFromUrl(urlstr);
        	JSONArray cArray = Util.getChildren(GlobalConstants.HOST + GlobalConstants.QRCHOME + "/" + qrcStr);
        	Log.i("VIEW_SERVICES::", "cArray=" + cArray.toString());
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
                Log.i("VIEW_SERVICES", "bundle.curr_loc_1=" + extras.getString("curr_loc"));
        		
        		//get the thing this qrc code points to
        		String thingStr = cArray.getString(0);
        		String childPath = Util.getUriFromQrc(qrcStr);
        		JSONArray paths = Util.getIncidentPaths(GlobalConstants.HOST + childPath);
        		for(int i=0; i<cArray.length(); i++){
        			
        			//check that the location of this item matches the current location
        			String thispath = paths.getString(i).replace("\"", "");
        			if(thispath.startsWith(GlobalConstants.SPACESHOME)){
        				Log.i("VIEW_SERVICES", "LOCATION::" + paths.getString(i));
        				StringTokenizer thisPathTokenizer = new StringTokenizer(thispath, "/");
        				StringTokenizer currLocTokenizer = new StringTokenizer(extras.getString("curr_loc"), "/");
        				int tokMatchCount =0;
        				final int tokMatchThresh = 5;
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
        						StringBuffer newLocBuf = new StringBuffer();
        						for(int k=0; k<tokMatchThresh; k++)
        							newLocBuf.append("/").append(thisPathTokVec.get(k));
        						currLoc_.setText(newLocBuf.toString());
        					}
        						
        				}
        				
        			}
        			else
        				Log.i("VIEW_SERVICES", paths.getString(i) + " does not start with " + GlobalConstants.SPACESHOME);
        		}
        		
        		
                
        	}
        	
        	/**/
        } catch(Exception e){
        	e.printStackTrace();
        	Log.d("VIEW_SERVICE::ERROR", "error while fetching " + urlstr);
        }
	}
}
