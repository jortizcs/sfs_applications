package mobile.SFS;

import android.app.Activity;
import android.content.Intent;
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
	private static final String DEFAULT_SPACE = GlobalConstants.SPACESHOME;
	//private static final String DEFAULT_SPACE = "/buildings/SDH/spaces";
	//private static final String DEFAULT_SPACE = "/buildings/home/spaces";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobilesfs);
        Bundle extras = getIntent().getExtras();
        
        TextView currLoc = (TextView) findViewById(R.id.currLoc);
        String s = extras == null ? null : extras.getString("curr_loc");
        final String currLocString = s == null ? DEFAULT_SPACE : s;
        currLoc.setText(currLocString);
        
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
					case 0: intent = new Intent(MobileSFS.this, UpdateHierarchy.class); break;
					case 1: {//intent = new Intent(MobileSFS.this, ViewServices.class); break;
						intent = new Intent("com.google.zxing.client.android.SCAN");
		        		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		        		startActivityForResult(intent, 0);
		        		break;
					}
				}
				
				intent.putExtra("curr_loc", currLocString);
				startActivity(intent);
			}
        });
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
			Intent next = new Intent(this, ViewServices.class);
			next.putExtra("url", (intent.getStringExtra("SCAN_RESULT")));
			startActivity(next);
	}
}