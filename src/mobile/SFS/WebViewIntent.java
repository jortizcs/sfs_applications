package mobile.SFS;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.util.Log;

public class WebViewIntent extends Activity{
	
	private String currLocStr =null;
	private String url=null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

}
