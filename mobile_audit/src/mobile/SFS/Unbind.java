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

public class Unbind extends Activity {
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
				Intent next = new Intent(Unbind.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(Unbind.this, Unbind.class));
				startActivity(next);
			}
		});
        
        TextView item = (TextView) findViewById(R.id.item);
        TextView meter = (TextView) findViewById(R.id.meter);
        final String itemUri = extras.getString("root");
        final String meterUri = extras.getString("node");
        item.setText(itemUri);
        meter.setText(meterUri);
        
        Button unbind = (Button) findViewById(R.id.bind);
        unbind.setText("Unbind");
        unbind.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		try {
        			if(!Util.getProperties(host_ + itemUri).getString("Type").equals("Item") || 
        					!Util.getProperties(host_ + meterUri).getString("Type").equals("Meter")) {
        				Toast.makeText(Unbind.this, "Invalid unbind. Scan first an item then a meter.", Toast.LENGTH_LONG).show();
        				Intent next = new Intent(Unbind.this, MobileSFS.class);
        				next.putExtra("curr_loc", currLocString);
        				startActivity(next);
        				return;
        			}
        			
        			String res="None";
        			try{
        				res = CurlOps.delete(host_ + itemUri + meterUri.substring(meterUri.lastIndexOf("/")));
        				Toast.makeText(Unbind.this, 
        						res.equals("Accepted") ? "Unbound" : "This resource is not bound to that meter", 
        						Toast.LENGTH_LONG).show();
        			} catch(Exception e){
						Log.i("Unbind", 
								"Exists? " + host_ + itemUri + meterUri.substring(meterUri.lastIndexOf("/")));
						if(Util.isExistingResource(host_ + itemUri + meterUri.substring(meterUri.lastIndexOf("/")))){
							Log.i("Unbind", "Exists? YES");
							Toast.makeText(getApplicationContext(), 
									"Could not delete:" + host_ + itemUri + meterUri.substring(meterUri.lastIndexOf("/")), 
									Toast.LENGTH_LONG);
		        			e.printStackTrace();
		        			return;
						}
        			}
            		Intent next = new Intent(Unbind.this, MobileSFS.class);
        			next.putExtra("curr_loc", currLocString);
        			startActivity(next);
        		}
        		catch(Exception e) {
        			e.printStackTrace();
        		}
        	}
        });
        
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent next = new Intent(Unbind.this, MobileSFS.class);
        		next.putExtra("curr_loc", currLocString);
        		startActivity(next);
        	}
        });
	}
}
