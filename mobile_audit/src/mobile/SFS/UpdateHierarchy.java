package mobile.SFS;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;

public class UpdateHierarchy extends Activity {
	private String root_, currLocString_;
	private Intent finalIntent_;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.updatehierarchy);
		Bundle extras = getIntent().getExtras();
		
		if(!checkGlobals())
			return;
        
        TextView currLoc = (TextView) findViewById(R.id.currLoc);
        currLocString_ = extras.getString("curr_loc");
        currLoc.setText(currLocString_);
		
		Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent next = new Intent(UpdateHierarchy.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(UpdateHierarchy.this, UpdateHierarchy.class));
				startActivity(next);
			}
		});
        
		ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem,
        		new String[] {
        			"Add Space", "Add Resource", "Bind Meter to Item", "Unbind Meter from Item",
        			"Attach Resources", "Detach Resources", "Delete"
        		}));
        
        //listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, new String[] {"Update Hierarchy", "View Services"}));
        
        listView.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		Intent intent = null;
        		
				switch(arg2) {
					case 0: {
						intent = new Intent(UpdateHierarchy.this, AddSpace.class); 
						intent.putExtra("curr_loc", currLocString_);
						startActivity(intent);
						return;
					}
					case 1: {
						intent = new Intent(UpdateHierarchy.this, AddResource.class);
						intent.putExtra("curr_loc", currLocString_);
						startActivity(intent);
						return;
					}
					case 2: finalIntent_ = new Intent(UpdateHierarchy.this, Bind.class); break;
					case 3:	finalIntent_ = new Intent(UpdateHierarchy.this, Unbind.class); break;
					case 4: finalIntent_ = new Intent(UpdateHierarchy.this, Attach.class); break;
					case 5: finalIntent_ = new Intent(UpdateHierarchy.this, Detach.class); break;
					case 6: {
						finalIntent_ = new Intent(UpdateHierarchy.this, Delete.class);
						intent = new Intent("com.google.zxing.client.android.SCAN");
						intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
						startActivityForResult(intent, 1);
						Toast.makeText(UpdateHierarchy.this, "Scan QR of Resource or Space to delete", Toast.LENGTH_LONG);
						return;
					}
				}
				
				intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				intent.putExtra("arg", (arg2 < 4 ? "Meter" : "Node"));
				startActivityForResult(intent, 0);
				Toast.makeText(UpdateHierarchy.this, "Scan QR Code of " + (arg2 < 4 ? "Item" : "Root"), Toast.LENGTH_LONG).show();
			}
        });
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			switch(requestCode) {
				case 0: {
					root_ = Util.getUriFromQrc(CurlOps.getQrcFromUrl(intent.getStringExtra("SCAN_RESULT")));
					
					Intent next = new Intent("com.google.zxing.client.android.SCAN");
					next.putExtra("SCAN_MODE", "QR_CODE_MODE");
					startActivityForResult(next, 1);
					Toast.makeText(this, "Scan QR Code of " + intent.getStringExtra("arg"), Toast.LENGTH_LONG).show();
					break;
				}
				case 1: {
					String node = Util.getUriFromQrc(CurlOps.getQrcFromUrl(intent.getStringExtra("SCAN_RESULT")));
					
					Log.i("UpdateHierarchy", "Root=" + root_ + "\tNode=" + node);
					
					finalIntent_.putExtra("root", root_);
					finalIntent_.putExtra("node", node);
					finalIntent_.putExtra("curr_loc", currLocString_);
					startActivity(finalIntent_);
					break;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkGlobals(){
    	SharedPreferences pref = this.getSharedPreferences(GlobalConstants.PREFS, MODE_PRIVATE);
    	String testStr = pref.getString("host", "");
    	if(testStr.equals("")){
    		Toast.makeText(this, "Deployment Information Not Set;  Go to " +
    				"http://is4server.com/energylens to set it.",
					 Toast.LENGTH_LONG).show();
    		return false;
    	}
    	return true;
    }
}
