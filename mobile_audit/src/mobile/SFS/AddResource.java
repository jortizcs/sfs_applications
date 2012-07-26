package mobile.SFS;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

public class AddResource extends Activity {
	private String host_ = GlobalConstants.HOST;
	private String uri_ = GlobalConstants.HOMEPATH;
	//private String host_ = "http://is4server.com:8083";
	//private String uri_ = "/buildings/SDH";
	//private String host_ = "http://is4server.com:8084";
	//private String uri_ = "/buildings/home";
	private String qrc_;
	private TextView currLoc_;
	private EditText powerRating_, currentDraw_;
	private AutoCompleteTextView resName_;
//	private Spinner resType_;
	
	public static String getTax(ResType resType) {
		switch(resType) {
			case ACM: return "Electronics/Other";
			case BAT: return "Electronics/Other";
			case COF: return "Miscellaneous/Electric_Housewares";
			case CPU: return "Electronics/Computer/other_computer/";
			case INK: return "Electronics/Imaging/printer";
			case LAM: return "Miscellaneous/Electric_Housewares";
			case LAS: return "Electronics/Imaging/printer/";
			case MAC: return "Electronics/Computer/integrated_tower_lcd/";
			case MIC: return "Miscellaneous/Electric_Housewares/";
			case NOT: return "Electronics/Computer/laptop/";
			case OTH: return "Miscellaneous/Other";
			case PHO: return "Electronics/Telephony/";
			case REF: return "Miscellaneous/Electric_Housewares";
			case SER: return "Electronics/Computer/server_tower";
			case SPH: return "Electronics/Other/";
			case SPS: return "Miscellaneous/Other/";
			case TEA: return "Miscellaneous/Electric_Housewares";
			case LCD: return "Electronics/Display/computer_lcd";
			default: return "Miscellaneous/Other/";
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addresource);
		Bundle extras = getIntent().getExtras();
        
		currLoc_ = (TextView) findViewById(R.id.currLoc);
        currLoc_.setText(extras.getString("curr_loc"));
        powerRating_ = (EditText) findViewById(R.id.powerRating);
        currentDraw_ = (EditText) findViewById(R.id.currentDraw);
        resName_ = (AutoCompleteTextView) findViewById(R.id.resName);
//        resType_ = (Spinner) findViewById(R.id.resType);
        
//        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
//        		new String[] {"Item", "Meter"});
//        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        resType_.setAdapter(aa);
        
        resName_.setAdapter(new ArrayAdapter<ResType>(this, R.layout.listitem, ResType.values()));
        resName_.setThreshold(1);
		
		Button changeCurrLoc = (Button) findViewById(R.id.changeCurrLoc);
        changeCurrLoc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent next = new Intent(AddResource.this, ChangeLocation.class);
				next.putExtra("return_intent", new Intent(AddResource.this, AddResource.class));
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
							
							Toast.makeText(AddResource.this, "QR code registered. Please scan again.", Toast.LENGTH_LONG).show();
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
						Toast.makeText(AddResource.this, "Please scan a different QR code", Toast.LENGTH_LONG).show();
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
			qrc_ = CurlOps.getQrcFromUrl(intent.getStringExtra("SCAN_RESULT"));
			
			if(Util.isExistingResource(host_ + uri_ + "/qrc/" + qrc_)) {
				JSONArray qrcc = Util.getChildren(GlobalConstants.HOST + GlobalConstants.QRCHOME + "/" + qrc_);
				Log.i("AddResource.onActivityResult", "QRC_CHILDREN::" + qrcc);
				if(qrcc !=null && qrcc.length()==0) {
					String name = resName_.getText().toString();
					
					Log.i("AddResource", "name="+name);
					Log.i("AddResource", "GET::" + host_ + GlobalConstants.TAXHOME + "/" + getTax(ResType.valueOf(name)));
					JSONArray jarr = Util.getChildren(host_ + GlobalConstants.TAXHOME + "/" + getTax(ResType.valueOf(name)));
					Log.i("AddResource", "GET_RESULT::" + jarr.toString());
					int count = 1;
			
					for(int i = 0; i < jarr.length(); i++) {
						if(jarr.getString(i).startsWith(name))
						//if(jarr.getJSONObject(i).getString("name").startsWith(name))
							count++;
					}
					
					String nname = name + count;
					Log.i("AddResource", "nname=" + nname);
					
					JSONObject properties = new JSONObject();
					properties.put("PowerRating", powerRating_.getText());
					properties.put("CurrentDraw", currentDraw_.getText());
	//				properties.put("Type", resType_.getSelectedItem().toString());
	//				properties.put("Type", name.startsWith("ACM") ? "Meter" : "Item");
					
					CheckBox isMeterCB = (CheckBox) findViewById(R.id.is_meter);
					if(name.startsWith("ACM") || isMeterCB.isChecked()) {
						properties.put("Type", "Meter");
						try {
							Util.createResource(nname, "meter", host_ + uri_ + "/inventory");
							Log.i("AddResource", "creating meter resource; " + host_ + uri_ + "/inventory/" + nname);
						} catch (Exception e) {
							Log.i("AddResource.onActivityResult", "Exists? " + host_ + uri_ + "/inventory/" + nname);
							if(!Util.isExistingResource(host_ + uri_ + "/inventory/" + nname)){
								Log.i("AddResource.onActivityResult", "Exists? NO");
								Toast.makeText(getApplicationContext(), "Could not create:" + host_ + uri_ + "/inventory/" + nname, 
										Toast.LENGTH_LONG).show();
								throw e;
							}
						}
					} else {
						properties.put("Type", "Item");
						try {
							Util.createResource(nname, "default", host_ + uri_ + "/inventory");
							Log.i("AddResource", "creating default resource; " + host_ + uri_ + "/inventory/" + nname);
						} catch(Exception e){
							Log.i("AddResource.onActivityResult", "Exists? " + host_ + uri_ + "/inventory/" + nname);
							if(!Util.isExistingResource(host_ + uri_ + "/inventory/" + nname)){
								Log.i("AddResource.onActivityResult", "Exists? NO");
								Toast.makeText(getApplicationContext(), "Could not create:" + host_ + uri_ + "/inventory/" + nname, 
										Toast.LENGTH_LONG).show();
								throw e;
							}
						}
					}
					
					try {
						Util.createSymlink(GlobalConstants.TAXHOME + "/" + getTax(ResType.valueOf(name)),
								uri_ + "/inventory/" + nname, host_);
						Log.i("AddResource", "creating symlink from taxonomy; " + uri_ + "/taxonomies/ma2/" + getTax(ResType.valueOf(name)) + 
								" to new resource (" + host_ + uri_ + "/inventory/" + nname + ")");
					} catch (Exception e){
						Log.i("AddResource.onActivityResult", 
								"Exists? " + GlobalConstants.TAXHOME + "/" + getTax(ResType.valueOf(name)) + "/" + nname);
						if(!Util.isExistingResource(host_ + GlobalConstants.TAXHOME + "/" + getTax(ResType.valueOf(name)) + "/" + nname)){
							Log.i("AddResource.onActivityResult", "Exists? NO");
							Toast.makeText(getApplicationContext(), 
									"Could not create:" + host_ + GlobalConstants.TAXHOME + "/" + getTax(ResType.valueOf(name)) + nname, 
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}
					
					try {
						Util.createSymlink(uri_ + "/qrc/" + qrc_, uri_ + "/inventory/" + nname, host_);
						Log.i("AddResource", "creating symlink from qrc; " + uri_ + "/qrc/" + qrc_ + 
								" to new resource (" + host_ + uri_ + "/inventory/" + nname + ")");
					} catch(Exception e){
						Log.i("AddResource.onActivityResult", "Exists? " + uri_ + "/inventory/" + nname);
						if(!Util.isExistingResource(host_ + uri_ + "/inventory/" + nname)){
							Log.i("AddResource.onActivityResult", "Exists? NO");
							Toast.makeText(getApplicationContext(), "Could not create:" + host_ + uri_ + "/inventory/" + nname, 
									Toast.LENGTH_LONG).show();
							throw e;
						}
					}
					
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("operation", "overwrite_properties");
					jsonObj.put("properties", properties);
					try {
						CurlOps.post(jsonObj.toString(), host_ + uri_ + "/inventory/" + nname);
					} catch(Exception e){
						e.printStackTrace();
					}
			
					try {
						Util.createSymlink(currLoc_.getText().toString(), uri_ + "/inventory/" + nname, host_);
						//Util.createSymlink("/is4/taxonomies/" + type, uri_ + "/inventory/" + name, host_);
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
}
