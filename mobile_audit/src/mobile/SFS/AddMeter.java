package mobile.SFS;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddMeter extends Activity {
	private String host_ = GlobalConstants.HOST;
	private String uri_ = GlobalConstants.HOMEPATH;
	private TextView currLoc_;
	private AutoCompleteTextView meterId_;
	private String qrc_;
	
	//public static final String METER_HOST = "http://ec2-184-169-204-224.us-west-1.compute.amazonaws.com:8080";
	public static final String METER_REG_URL = "/jorge";
	
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addmeter);
		Bundle extras = getIntent().getExtras();
        
		currLoc_ = (TextView) findViewById(R.id.currLoc);
        currLoc_.setText(extras.getString("curr_loc"));
        meterId_ = (AutoCompleteTextView) findViewById(R.id.meterId);
        
        try {
        	JSONArray arr = new JSONObject(CurlOpsReal.get(host_ + METER_REG_URL)).getJSONArray("children");
        	System.out.println("A");
            String[] meterIds = new String[arr.length()];
            for(int i = 0; i < arr.length(); i++)
            	meterIds[i] = arr.getString(i);
            System.out.println("b");
            meterId_.setAdapter(new ArrayAdapter<String>(this, R.layout.listitem, meterIds));
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        
        meterId_.setThreshold(1);
		
		Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent next = new Intent(AddMeter.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(AddMeter.this, AddMeter.class));
				startActivity(next);
			}
		});
        
        Button scanQrc = (Button) findViewById(R.id.scanQrc);
        scanQrc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
        		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        		startActivityForResult(intent, 0);
			}
		});
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		try {
			qrc_ = CurlOps.getQrcFromUrl(intent.getStringExtra("SCAN_RESULT"));
			
			if(Util.isExistingResource(host_ + uri_ + "/qrc/" + qrc_)) {
				JSONArray qrcc = Util.getChildren(GlobalConstants.HOST + GlobalConstants.QRCHOME + "/" + qrc_);
				Log.i("AddResource.onActivityResult", "QRC_CHILDREN::" + qrcc);
				if(qrcc !=null && qrcc.length()==0) {
					String name = meterId_.getText().toString();
					
					//JSONObject properties = new JSONObject();
					
					/*properties.put("Type", "Meter");
					try {
						Util.createResource(name, "meter", host_ + uri_ + "/inventory");
						Log.i("AddResource", "creating meter resource; " + host_ + uri_ + "/inventory/" + name);
					} catch (Exception e) {
						Log.i("AddResource.onActivityResult", "Exists? " + host_ + uri_ + "/inventory/" + name);
						if(!Util.isExistingResource(host_ + uri_ + "/inventory/" + name)){
							Log.i("AddResource.onActivityResult", "Exists? NO");
							Toast.makeText(getApplicationContext(), "Could not create:" + host_ + uri_ + "/inventory/" + name, 
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}*/
					
					/*try {
						Util.createSymlink(GlobalConstants.TAXHOME + "/" + "Electronics/Other",
								uri_ + "/inventory/" + name, host_);
						Log.i("AddResource", "creating symlink from taxonomy; " + uri_ + "/taxonomies/ma2/" + "Electronics/Other" + 
								" to new resource (" + host_ + uri_ + "/inventory/" + name + ")");
					} catch (Exception e){
						Log.i("AddResource.onActivityResult", 
								"Exists? " + GlobalConstants.TAXHOME + "/" + "Electronics/Other" + "/" + name);
						if(!Util.isExistingResource(host_ + GlobalConstants.TAXHOME + "/" + "Electronics/Other" + "/" + name)){
							Log.i("AddResource.onActivityResult", "Exists? NO");
							Toast.makeText(getApplicationContext(), 
									"Could not create:" + host_ + GlobalConstants.TAXHOME + "/" + "Electronics/Other" + name,
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}*/
					
					try {
						Util.createSymlink(uri_ + "/qrc/" + qrc_, METER_REG_URL + "/" + name, host_);
						Log.i("AddResource", "creating symlink from qrc; " + uri_ + "/qrc/" + qrc_ + 
								" to new meter (" + METER_REG_URL + "/" + name + ")");
					} catch(Exception e){
						Log.i("AddResource.onActivityResult", "Exists? " + uri_ + "/inventory/" + name);
						if(!Util.isExistingResource(host_ + uri_ + "/inventory/" + name)){
							Log.i("AddResource.onActivityResult", "Exists? NO");
							Toast.makeText(getApplicationContext(), "Could not create:" + host_ + uri_ + "/inventory/" + name, 
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}
					
					/*JSONObject jsonObj = new JSONObject();
					jsonObj.put("operation", "overwrite_properties");
					jsonObj.put("properties", properties);
					try {
						CurlOps.post(jsonObj.toString(), host_ + METER_REG_URL + "/" + name);
					} catch(Exception e){
						e.printStackTrace();
					}*/
			
					try {
						Util.createSymlink(currLoc_.getText().toString(), METER_REG_URL + "/" + name, host_);
					} catch(Exception e){
						Log.i("AddResource.onActivityResult", "Exists? " + currLoc_.getText().toString());
						if(!Util.isExistingResource(host_ + currLoc_.getText().toString())){
							Log.i("AddResource.onActivityResult", "Exists? NO");
							Toast.makeText(getApplicationContext(), "Could not create:" + host_ + currLoc_.getText().toString(), 
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}
					
					Intent next = new Intent(this, MobileSFS.class);
					next.putExtra("curr_loc", currLoc_.getText());
					startActivity(next);
				} else {
					Log.i("AddResource.onActivityResult", "QRC_ALREADY_BOUND:" + qrcc);
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
							jsonObj.put("resourceName", qrc_);
							jsonObj.put("resourceType", "default");
							try {
								CurlOps.put(jsonObj.toString(), host_ + uri_ + "/qrc");
							} catch(Exception e){
								Log.i("AddResource.onCreateDialog", "Exists? " + host_ + uri_ + "/qrc/" + qrc_);
								if(!Util.isExistingResource(host_ + uri_ + "/qrc/" + qrc_)){
									Log.i("AddResource.onActivityResult", "Exists? NO");
									Toast.makeText(getApplicationContext(), "Could not create:" + uri_ + "/qrc" + qrc_, 
											Toast.LENGTH_LONG).show();
									throw e;
								}
							}
							
							Toast.makeText(AddMeter.this, "QR code registered. Please scan again.", Toast.LENGTH_LONG).show();
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
						Toast.makeText(AddMeter.this, "Please scan a different QR code", Toast.LENGTH_LONG).show();
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
}
