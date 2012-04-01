package mobile.SFS;

import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.StringTokenizer;
import java.util.Vector;
import java.lang.StringBuffer;

public class Delete extends Activity {
	private String host_ = GlobalConstants.HOST;
	private String home_ = GlobalConstants.HOMEPATH;
	
	//private String host_ = "http://is4server.com:8083";	
	//private String home_ = "/buildings/SDH";

	//private String host_ = "http://is4server.com:8084";
	//private String home_ = "/buildings/home";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete);
		Bundle extras = getIntent().getExtras();
		
		TextView currLoc = (TextView) findViewById(R.id.currLoc);
		final String currLocString = extras.getString("curr_loc");
        currLoc.setText(currLocString);
		
		Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent next = new Intent(Delete.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(Delete.this, Delete.class));
				startActivity(next);
			}
		});
        
        TextView res = (TextView) findViewById(R.id.res);
		final String resUri = extras.getString("node");
		res.setText(resUri);
		
		 Button delete = (Button) findViewById(R.id.delete);
		 delete.setOnClickListener(new OnClickListener() {
	        	public void onClick(View v) {
	        		String res = "None";
	        		try {
	        			res = CurlOps.delete(host_ + resUri);
	        		} catch(Exception e) {
	        			Log.i("Delete.onCreate", "Exists? " + host_ + resUri);
						if(Util.isExistingResource(host_ + resUri)){
							Log.i("Delete.onCreate", "Exists? YES");
							Toast.makeText(getApplicationContext(), "Could not delete:" + resUri, 
									Toast.LENGTH_LONG);
		        			e.printStackTrace();
		        			return;
						} else {
							//delete the references
							//String qrcChilds = "None";
							String allQrcRefs = "None";
							String allTaxRefs = "None";
							String allSpaceRefs = "None";
							try {
								Log.i("Delete.onCreate", "Getting: " + host_ + "/buildings/home/qrc/");
								//qrcChilds = CurlOps.get(host_ + home_ + "/qrc/");
								//Log.i("qrc_children", qrcChilds);
								allQrcRefs = CurlOps.get(host_ + home_ + "/qrc/*");
								deleteRefs(allQrcRefs, resUri);
								Log.i("qrc_deep", allQrcRefs);
								if(!resUri.contains("/spaces/")){
									allTaxRefs = CurlOps.get(host_ + "/taxonomies/ma2/*");
									deleteRefs(allTaxRefs, resUri);
									
									allSpaceRefs = CurlOps.get(host_ + home_ + "/spaces/*");
									deleteRefs(allSpaceRefs, resUri);
								}
							} catch(Exception e1){
								e1.printStackTrace();
							}
							//Log.i("Delete.onCreate", qrcChilds);
						}
	        		}
	        		Intent next = new Intent(Delete.this, MobileSFS.class);
	        		StringTokenizer tokenizer = new StringTokenizer(currLocString, "/");
	        		Vector<String> tokens = new Vector<String>();
	        		StringBuffer newCurrLocBuf = new StringBuffer();
	        		while(tokenizer.hasMoreElements())
	        			tokens.add(tokenizer.nextToken());
	        		newCurrLocBuf.append("/");
	        		for(int i=0; i<tokens.size()-1; ++i){
	        			newCurrLocBuf.append(tokens.elementAt(i));
	        			if(i!=tokens.size()-2)
	        				newCurrLocBuf.append("/");
	        		}
        			next.putExtra("curr_loc", newCurrLocBuf.toString());
        			startActivity(next);
        			Toast.makeText(Delete.this, "Deleted " + resUri, Toast.LENGTH_LONG).show();
	        	}
		 });
		 
		 Button cancel = (Button) findViewById(R.id.cancel);
	        cancel.setOnClickListener(new OnClickListener() {
	        	public void onClick(View v) {
	        		Intent next = new Intent(Delete.this, MobileSFS.class);
	        		next.putExtra("curr_loc", currLocString);
	        		startActivity(next);
	        	}
	        });
	}
	
	private void deleteRefs(String refsObjStr, String delUri){
		try {
			JSONObject refObj = new JSONObject(refsObjStr);
			Iterator<String> keys = (Iterator<String>) refObj.keys();
			while(keys.hasNext()){
				String thisKey = keys.next();				
				Log.i("Delete.deleteRef", "Examining: " + thisKey);
				JSONObject keyInfo = refObj.getJSONObject(thisKey);
				JSONArray keyChildren = keyInfo.getJSONArray("children");
				for(int i=0; i<keyChildren.length(); ++i){
					String thisChild = keyChildren.getString(i);
					Log.i("Delete.deleteRef", "Checking: " + thisChild + " has " + delUri + " ?");
					if(thisChild != null && thisChild.contains(" -> ")){
						StringTokenizer tokenizer = new StringTokenizer(thisChild, " -> ");
						Vector<String> tokens = new Vector<String>(2);
						while(tokenizer.hasMoreElements())
							tokens.add(tokenizer.nextToken());
						if(tokens.size()==2 && tokens.elementAt(1).equalsIgnoreCase(delUri)){
							Log.i("Delete.deleteRef", "Deleting: " + host_ + thisKey + tokens.elementAt(0));
							try{
								CurlOps.delete(host_ + thisKey + tokens.elementAt(0));
							} catch(Exception e){
								Log.i("Delete.deleteRefs", "Exists? " + host_ + thisKey + tokens.elementAt(0));
								if(Util.isExistingResource(host_ + thisKey + tokens.elementAt(0))){
									Log.i("Delete.deleteRefs", "Exists? YES");
									Toast.makeText(getApplicationContext(), 
											"Could not delete:" + thisKey + tokens.elementAt(0), 
											Toast.LENGTH_LONG);
				        			e.printStackTrace();
				        			return;
								}
							}
						}
					}
				}
			}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
