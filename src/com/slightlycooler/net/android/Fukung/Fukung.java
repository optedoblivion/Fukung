package com.slightlycooler.net.android.Fukung;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.view.ViewGroup.LayoutParams;

import java.io.*;
import java.util.regex.*;
import android.graphics.drawable.Drawable;
import android.hardware.SensorListener;
import android.hardware.SensorManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class Fukung extends Activity implements SensorListener{
    /** Called when the activity is first created. */
	
	private static final String TAG = "FUKUNG";
	private ImageView iView;
	private String URL_ROOT="http://www.fukung.org/";
	private String URL_ACTION="random";
	private URL fukung;
	private Pattern imgPattern = Pattern.compile("<img.*>");
	private WebView webView;
	private ScrollView sv;
	private View zoom;
	private SensorManager sensorMgr;
	private FrameLayout mContentView;
	private long lastUpdate = System.currentTimeMillis();
	private float x,y,z;
	private float last_x, last_y, last_z;
	private static final int SHAKE_THRESHOLD = 800;
	private static final FrameLayout.LayoutParams ZOOM_PARAMS =
		new FrameLayout.LayoutParams(
		FrameLayout.LayoutParams.FILL_PARENT,
		FrameLayout.LayoutParams.WRAP_CONTENT,
		Gravity.BOTTOM);

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		if((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()){
			webView.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContentView = (FrameLayout) getWindow().getDecorView().findViewById(android.R.id.content);
        //iView = (ImageView) findViewById(R.id.ImageView01);
        webView = (WebView) findViewById(R.id.webview);
        webView.setInitialScale(75);
        webView.setBackgroundColor(1);
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorMgr.registerListener(this,
        		SensorManager.SENSOR_ACCELEROMETER,
        		SensorManager.SENSOR_DELAY_GAME);
//        LayoutParams params = new 
//        	LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//        webView.setLayoutParams(params);
        zoom = webView.getZoomControls();
        mContentView.addView(zoom, ZOOM_PARAMS);
        zoom.setVisibility(View.GONE);
        
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	long action = event.getAction();
    	boolean ret = true;
    	if(action == MotionEvent.ACTION_DOWN){
    		try{
    			getRandomImage();
    		}catch(Exception e){
    			Log.e(TAG, e.toString());
    		}
    	}
    	return ret;
    }
    
    protected void getRandomImage() throws Exception {
    	String URL = URL_ROOT+URL_ACTION;
    	fukung = new URL(URL);
    	BufferedReader in = new BufferedReader(new 
    					InputStreamReader(fukung.openStream()));
    	String inputLine;
    	
    	while ((inputLine = in.readLine()) != null){
    		Matcher img = imgPattern.matcher(inputLine);
    		if (img.find()){
    			String matches = img.group();
    			String imgURL = matches;
    			imgURL = imgURL.replace("<img src=", "");
    			imgURL = imgURL.replaceAll("width=.*>", "");
    	    	imgURL = imgURL.replace("\"","");
    	    	imgURL = imgURL.replace(" ","%20");
    			//Drawable image = FukungImg(imgURL);
    			//iView.setImageDrawable(image);
    			webView.loadUrl(imgURL);
   			
    		}
    	}
    	in.close();
    }
    
    private Drawable FukungImg(String url){
    	try{
    		InputStream is = (InputStream) fetch(url);
    		Drawable d = Drawable.createFromStream(is, "src");
    		return d;
    	}catch (MalformedURLException e){
    		Log.e(TAG, e.toString());
    		return null;
    	}catch (IOException e){
    		Log.e(TAG, e.toString());
    		return null;
    	}
    }

    public Object fetch(String address) throws MalformedURLException,IOException {

    	Log.i(TAG, address);
		URL url = new URL(address);
		Object content = url.getContent();
		return content;
	}

	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(int sensor, float[] values) {
		// TODO Auto-generated method stub
		if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// only allow one update every 100ms.
			if ((curTime - lastUpdate) > 100) {
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				x = values[SensorManager.DATA_X];
				y = values[SensorManager.DATA_Y];
				z = values[SensorManager.DATA_Z];

				float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

				if (speed > SHAKE_THRESHOLD) {
//					Log.d("sensor", "shake detected w/ speed: " + speed);
//					Toast.makeText(this, "shake detected w/ speed:"  + speed, Toast.LENGTH_SHORT).show();
		    		try{
		    			getRandomImage();
		    		}catch(Exception e){
		    			Log.e(TAG, e.toString());
		    		}
				}
				last_x = x;
				last_y = y;
				last_z = z;
			}
		}
		
	}

}