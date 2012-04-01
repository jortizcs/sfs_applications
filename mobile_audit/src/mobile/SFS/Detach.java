package mobile.SFS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Detach extends Activity {
	private static final String host_ = GlobalConstants.HOST;
	//private static final String host_ = "http://is4server.com:8083";
	//private static final String host_ = "http://is4server.com:8084";
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind);
		Bundle extras = getIntent().getExtras();
		
		TextView currLoc = (TextView) findViewById(R.id.currLoc);
		final String currLocString = extras.getString("curr_loc");
        currLoc.setText(currLocString);
		
		Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent next = new Intent(Detach.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(Detach.this, Detach.class));
				startActivity(next);
			}
		});
        
        TextView root = (TextView) findViewById(R.id.item);
        TextView node = (TextView) findViewById(R.id.meter);
        final String rootUri = extras.getString("root");
        final String nodeUri = extras.getString("node");
        root.setText(rootUri);
        node.setText(nodeUri);
        
        Button detach = (Button) findViewById(R.id.bind);
        detach.setText("Detach");
        detach.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		try {
        			if(!Util.getProperties(host_ + nodeUri).getString("Type").equals("Item") ||
        					Util.getProperties(host_ + rootUri).getString("Type").equals("Space")) {
        				Toast.makeText(Detach.this, "Invalid detach. Root may not be a space; node must be an item.", Toast.LENGTH_LONG).show();
        				Intent next = new Intent(Detach.this, MobileSFS.class);
        				next.putExtra("curr_loc", currLocString);
        				startActivity(next);
        				return;
        			}
        			
        			String res = "None";
        			
        			try {
        				res = CurlOps.delete(host_ + rootUri + nodeUri.substring(nodeUri.lastIndexOf("/")));
        			} catch(Exception e){
						Log.i("Detach", "Exists? " + host_ + rootUri + nodeUri.substring(nodeUri.lastIndexOf("/")));
						if(Util.isExistingResource(host_ + rootUri + nodeUri.substring(nodeUri.lastIndexOf("/")))){
							Log.i("Detach", "Exists? YES");
							Toast.makeText(getApplicationContext(), 
									"Could not delete:" + rootUri + nodeUri.substring(nodeUri.lastIndexOf("/")), 
									Toast.LENGTH_LONG);
		        			e.printStackTrace();
		        			return;
						}
					}
        			Intent next = new Intent(Detach.this, MobileSFS.class);
        			next.putExtra("curr_loc", currLocString);
        			startActivity(next);
        			//Toast.makeText(Detach.this, res.equals("Accepted") ? "Detached" : "Detach failed", Toast.LENGTH_LONG).show();
        			Toast.makeText(Detach.this, "Detached", Toast.LENGTH_LONG).show();
        		}
        		catch(Exception e) {
        			e.printStackTrace();
        		}
        	}
        });
        
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent next = new Intent(Detach.this, MobileSFS.class);
        		next.putExtra("curr_loc", currLocString);
        		startActivity(next);
        	}
        });
	}
}
