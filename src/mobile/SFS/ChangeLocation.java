package mobile.SFS;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ChangeLocation extends Activity {
	private Intent returnIntent_;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changelocation);
		
		returnIntent_ = getIntent().getExtras().getParcelable("return_intent");
		
		Button scanQrc = (Button) findViewById(R.id.scanCurrQrc);
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
			String newLoc = Util.getUriFromQrc(CurlOps.getQrcFromUrl(intent.getStringExtra("SCAN_RESULT")));
			returnIntent_.putExtra("curr_loc", newLoc);
		}
		catch(Exception e) {
			e.printStackTrace();
			//returnIntent_.putExtra("curr_loc", "Unknown");
			Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, "Unknown QR code", duration);
			toast.show();
		}
		
		startActivity(returnIntent_);
	}
}
