package mobile.SFS;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;


public class ViewServices extends Activity {
	private static final String GRAPH_HOME = "http://ec2-204-236-167-113.us-west-1.compute.amazonaws.com/grapher/development";
	
	private String currLocStr =null;
	private String url=null;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		if(!checkGlobals())
			return;
		
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			currLocStr = extras.getString("curr_loc");
			url = extras.getString("url");
			Log.i("WebViewIntent", "curr_loc=" + currLocStr);
			Log.i("WebViewIntent", "url=" + url);
			
			String urlstr = "";
			
			try {
				String qrc = CurlOps.getQrcFromUrl(getIntent().getStringExtra("url"));
				JSONArray arr = new JSONObject(CurlOps.get(GlobalConstants.HOST + GlobalConstants.QRCHOME + "/" + qrc)).getJSONArray("children");
				System.out.println("Item: " + arr.getString(0));
				
				if(arr != null && arr.length() == 1) {
					arr = new JSONObject(CurlOps.get(GlobalConstants.HOST + arr.getString(0).split("->")[1].trim())).getJSONArray("children");
					System.out.println("Meter: " + arr.getString(0));
					if(arr != null && arr.length() == 1) { //incorrect but a simple hack for now, items can have multiple children (attachments)
						urlstr = GRAPH_HOME + arr.getString(0).split("->")[1].trim() + "/true_power";
						System.out.println("Urlstr: " + urlstr);
					}
					else {
						Toast.makeText(this, "Item has no meter attached", Toast.LENGTH_LONG).show();
					}
				}
				else {
					Toast.makeText(this, "That QR code is not bound to an item", Toast.LENGTH_LONG).show();
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
	        Uri uri = Uri.parse(urlstr);
	        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
	        intent.putExtra("curr_loc", currLocStr);
	        intent.putExtra("url", url);
	        startActivity(intent);
		}
		else
			Log.i("WebViewIntent", "NO CURR_LOC IN WEBVIEW SET");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	        Log.d(this.getClass().getName(), "back button pressed");
	        Log.i(this.getClass().getName(), "Setting curr_loc to " + currLocStr);
	        Intent intent = new Intent(this, MobileSFS.class);
	        intent.putExtra("curr_loc", currLocStr);
	        startActivity(intent);
	    }
	    return super.onKeyDown(keyCode, event);
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
