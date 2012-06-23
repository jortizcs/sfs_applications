package mobile.SFS;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import android.util.Log;
import android.net.Uri;


public class ViewServices extends Activity {
	
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
			
			String urlstr = getIntent().getStringExtra("url");
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