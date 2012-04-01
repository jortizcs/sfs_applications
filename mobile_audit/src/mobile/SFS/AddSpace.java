package mobile.SFS;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class AddSpace extends Activity {
	private String host_ = GlobalConstants.HOST;
	private String uri_ = GlobalConstants.HOMEPATH;
	//private String host_ = "http://is4server.com:8083";
	//private String uri_ = "/buildings/SDH";
	//private String host_ = "http://is4server.com:8084";
	//private String uri_ = "/buildings/home";
	private String qrc;
	private TextView currLoc_;
	private EditText spaceName_;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addspace);
		Bundle extras = getIntent().getExtras();
        
		currLoc_ = (TextView) findViewById(R.id.currLoc);
        currLoc_.setText(extras.getString("curr_loc"));
		
		Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent next = new Intent(AddSpace.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(AddSpace.this, AddSpace.class));
				startActivity(next);
			}
		});
        
        spaceName_ = (EditText) findViewById(R.id.spaceName);
        
        Button scanQrc = (Button) findViewById(R.id.scanQrc);
        scanQrc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        		startActivityForResult(intent, 0);
			}
		});
	}
	
	public Dialog onCreateDialog(int id) {
		switch(id) {
			case 0: {
				Builder b = new Builder(this);
				b.setTitle("This is not a registered QR Code. Register new code?");
				
				b.setPositiveButton("Register", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							JSONObject jsonObj = new JSONObject();
							jsonObj.put("operation", "create_resource");
							jsonObj.put("resourceName", qrc);
							jsonObj.put("resourceType", "default");
							try {
								CurlOps.put(jsonObj.toString(), host_ + uri_ + "/qrc");
							} catch(Exception e){
								Log.i("AddSpace.onActivityResult", "Exists? " + host_ + uri_ + "/qrc/" + qrc);
								if(!Util.isExistingResource(host_ + uri_ + "/qrc/" + qrc)){
									Log.i("AddSpace.onActivityResult", "Exists? NO");
									Toast.makeText(getApplicationContext(), "Could not create:" + uri_ + "/qrc/" + qrc, 
											Toast.LENGTH_LONG).show();
									throw e;
								}
							}
							
							Toast.makeText(AddSpace.this, "QR code registered. Please scan again.", Toast.LENGTH_LONG).show();
							Intent intent = new Intent("com.google.zxing.client.android.SCAN");
							intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
							startActivityForResult(intent, 0);
						}
						catch(Exception e) {
							e.printStackTrace();
						}
					}
				});
				
				b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(AddSpace.this, "Please scan a different QR code", Toast.LENGTH_LONG).show();
						Intent intent = new Intent("com.google.zxing.client.android.SCAN");
						intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
						startActivityForResult(intent, 0);
					}
				});
				
				return b.create();
			}
		}
		
		return null;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			qrc = CurlOps.getQrcFromUrl(intent.getStringExtra("SCAN_RESULT"));
			
			if(Util.isExistingResource(host_ + uri_ + "/qrc/" + qrc)) {
				JSONArray qrcc = Util.getChildren(GlobalConstants.HOST + GlobalConstants.QRCHOME + "/" + qrc);
				Log.i("AddSpace.onActivityResult", "QRC_CHILDREN::" + qrcc);
				if(qrcc !=null && qrcc.length()==0){
					String name = spaceName_.getText().toString();
					try {
						Util.createResource(name, "default", host_ + currLoc_.getText());
					} catch(Exception e){
						Log.i("AddSpace.onActivityResult", "Exists? " + host_ + currLoc_.getText() + "/" + name);
						if(!Util.isExistingResource(host_ + currLoc_.getText() + "/" + name)){
							Log.i("AddSpace.onActivityResult", "Exists? NO");
							Toast.makeText(getApplicationContext(), "Could not create:" + currLoc_.getText() + "/" + name, 
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}
					
					try {
						Util.createSymlink(uri_ + "/qrc/" + qrc, currLoc_.getText() + "/" + name, host_);
					}  catch(Exception e){
						Log.i("AddSpace.onActivityResult", "Exists? " + host_ + uri_ + "/qrc/" + qrc + "/" + name);
						if(!Util.isExistingResource(host_ + uri_ + "/qrc/" + qrc + "/" + name)){
							Log.i("AddSpace.onActivityResult", "Exists? NO");
							Toast.makeText(getApplicationContext(), "Could not create:" + uri_ + "/qrc/" + qrc + "/" + name, 
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}
					
					JSONObject properties = new JSONObject();
					properties.put("Type", "Space");
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("operation", "overwrite_properties");
					jsonObj.put("properties", properties);
					CurlOps.post(jsonObj.toString(), host_ + currLoc_.getText() + "/" + name);
					
					Intent next = new Intent(this, MobileSFS.class);
					next.putExtra("curr_loc", currLoc_.getText() + "/" + name);
					startActivity(next);
				} else {
					Log.i("AddSpace.onActivityResult", "QRC_ALREADY_BOUND:" + qrcc);
					Toast.makeText(getApplicationContext(), "QR Code Already Bound!  Try another!", Toast.LENGTH_LONG).show();
				}
			}
			else
				showDialog(0);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
