package mobile.SFS;

import java.util.StringTokenizer;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Attach extends Activity {
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
				Intent next = new Intent(Attach.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(Attach.this, Attach.class));
				startActivity(next);
			}
		});
        
        TextView root = (TextView) findViewById(R.id.item);
        TextView node = (TextView) findViewById(R.id.meter);
        final String rootUri = extras.getString("root");
        final String nodeUri = extras.getString("node");
        root.setText(rootUri);
        node.setText(nodeUri);
        
        Button attach = (Button) findViewById(R.id.bind);
        attach.setText("Attach");
        attach.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		try {
        			if(!Util.getProperties(host_ + nodeUri).getString("Type").equals("Item") ||
        					Util.getProperties(host_ + rootUri).getString("Type").equals("Space")) {
        				Toast.makeText(Attach.this, "Invalid attach. Root may not be a space; node must be an item.", Toast.LENGTH_LONG).show();
        				Intent next = new Intent(Attach.this, MobileSFS.class);
        				next.putExtra("curr_loc", currLocString);
        				startActivity(next);
        				return;
        			}
        			
        			String res = "None";
        			String nodeName = "None";
        			try {
        				StringTokenizer tokenizer = new StringTokenizer(nodeUri, "/");
						Vector<String> tokens = new Vector<String>();
						while(tokenizer.hasMoreElements())
							tokens.add(tokenizer.nextToken());
						nodeName = tokens.elementAt(tokens.size()-1);
						
        				res = Util.createSymlink(rootUri, nodeUri, host_);
        				Log.i("Attach", "creating symlink from qrc; " + rootUri + 
								" to " + nodeUri);
					} catch(Exception e){
						Log.i("Attach", "Exists? " + rootUri + "/" + nodeName);
						if(!Util.isExistingResource(host_ + rootUri + "/" + nodeName)){
							Log.i("Attach", "Exists? NO");
							Toast.makeText(getApplicationContext(), 
									"Could not create:" + host_ + rootUri + "/" + nodeName, 
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}
					
        			Intent next = new Intent(Attach.this, MobileSFS.class);
        			next.putExtra("curr_loc", currLocString);
        			startActivity(next);
        			Toast.makeText(Attach.this, res, Toast.LENGTH_LONG).show();
        		}
        		catch(Exception e) {
        			e.printStackTrace();
        		}
        	}
        });
        
        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		Intent next = new Intent(Attach.this, MobileSFS.class);
        		next.putExtra("curr_loc", currLocString);
        		startActivity(next);
        	}
        });
	}
}
