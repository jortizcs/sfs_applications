package com.example.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.example.local.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ChooseLocation extends Activity {
	public MyImageView image;
	public float xPos;
	public float yPos;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.floorplan);
		xPos = -1;
		yPos = -1;
		image = new MyImageView(this);
	    LinearLayout layout=(LinearLayout)findViewById(R.id.ll2);
	    layout.addView(image);
		
	    Button bt1 = (Button)findViewById(R.id.displayXY);
	    bt1.setOnClickListener(displayLocationListener);
	    
	    Button bt2 = (Button)findViewById(R.id.finalizeLocation);
	    bt2.setOnClickListener(finalizeLocationListener);
	
	
	
	}
	private OnClickListener finalizeLocationListener = new OnClickListener() {
        public void onClick(View v)
        {
        	Intent resultIntent = new Intent();
        	resultIntent.putExtra("xPos", xPos);
        	resultIntent.putExtra("yPos", yPos);

            setResult(Activity.RESULT_OK,resultIntent);
            finish();
        }
    };
	
	 private OnClickListener displayLocationListener = new OnClickListener() {
	        public void onClick(View v)
	        {
	        	xPos = image.getPosX();
	        	yPos = image.getPosY();
	        	 EditText Xcoord = (EditText)findViewById(R.id.Xlabel);
	             EditText Ycoord = (EditText)findViewById(R.id.Ylabel);
	             if (Xcoord!=null)
	             {
	             	Xcoord.setText(Float.toString(xPos));
	             	
	             	Log.d("BUTTON","FINDS XTEXTBOX NOT NULL . " + Xcoord.getText());
	             }
	             else
	             	Log.d("BUTTON","XTEXTBOX NULL");
	             if (Ycoord!=null)
	             {
	             	Ycoord.setText(Float.toString(yPos));
	             	
	             	Log.d("BUTTON","FINDS YTEXTBOX NOT NULL" + Ycoord.getText());
	             }
	             else
	             	Log.d("BUTTON","YTEXTBOX NULL");
	            
	        }
	    };
	 private OnLongClickListener imageclicklistener = new OnLongClickListener() {
	        public boolean onLongClick(View v)
	        {
	        	Log.d("LONGCLICK","listened");
	            Toast.makeText(getApplicationContext(), "loooooooong click", Toast.LENGTH_LONG);
	            return true;
	            
	        }
	    };

}
