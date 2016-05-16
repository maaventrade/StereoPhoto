package com.mochalov.photo;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.hardware.*;
import android.media.*;
import android.os.*;
import android.preference.*;
import android.util.*;
import android.view.*;
import android.view.TextureView.*;
import android.widget.*;
import android.widget.AdapterView.*;

import java.io.*;
import java.util.*;

public class MainActivity extends Activity  implements SurfaceTextureListener,
OnSharedPreferenceChangeListener
{
	private static final String TAG = "MainActivity";
	// Object Camera contains the hardware.Camera object
	private Camera camera;
	// The camera uses textureView to show preview
    private TextureView textureView;
    // The object surfaceViewInfo shows buttons and information over the textureView 
    private SurfaceViewInfo surfaceViewInfo;
	
    // SraredPreferences keep parameters of the application
	private SharedPreferences prefs;
	private static final String FLASH_MODE = "FLASH_MODE";
	private static final String PICTURE_SIZE = "PICTURE_SIZE";
	// Turn the flash on or not
	private boolean flashMode = false;
	// Size of the pictures created by the camera
	private String pictureSize;
	// The application changes orientation of the Camera when device is rotated  
	private OrientationEventListener orientationEventListener;
	private int orientation = -1;
	// The directory for saving cameras images
	private String tempDirectory = Environment.getExternalStorageDirectory()+"/xolosoft/stereo";
	// The name of the subdirectory is produced from time of the shooting  
	private String tempSubdir = "";
	// Capturing of the images is running
	private enum States {none, shot1, shot2};
	private States state = States.none;
	// The timer for the capturing of the second image
	private CountDownTimer countDownTimer;
	
	private Context context;
	
	private SensorManager SensorManager;
	private final double calibration = SensorManager.STANDARD_GRAVITY;
	float currentAcceleration1 = 0;
	

	private ArrayList<String> list = new ArrayList<String>(); 
//	private ArrayList<String> list1 = new ArrayList<String>(); 
	
	MenuItem menuItemView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		// Restore preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		flashMode = prefs.getBoolean(FLASH_MODE, false);	
		pictureSize = prefs.getString(PICTURE_SIZE, "");
		
		setViewCamera();
		
		context = this;
	}

	/**
	 * Set contentView, store main visual objects and set main events handlers   
	 */
	private void setViewCamera(){
		
		setContentView(R.layout.activity_main);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
							 WindowManager.LayoutParams.FLAG_FULLSCREEN);					 

		textureView = (TextureView)findViewById(R.id.textureView1);
        textureView.setSurfaceTextureListener(this);

        surfaceViewInfo = (SurfaceViewInfo)findViewById(R.id.SurfaceViewInfo);
        surfaceViewInfo.setZOrderOnTop(true);
        // When the device is rotated this handler sets rotation of the Camera  
		orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL){
		    @Override
		    public void onOrientationChanged(int arg0) {		
		    	int newOrientation = -1;
		    	if (arg0 > 45 && arg0 < 135)
		    		newOrientation = 180;
		    	else if (arg0 > 315 && arg0 <= 360 || arg0 < 45)
					newOrientation = 90;
		    	else if (arg0 > 225 && arg0 < 315)
		    		newOrientation = 0;
		    	else  if (arg0 > 135 && arg0 < 225)
		    		newOrientation = 270;

		    	if (newOrientation != orientation 
		    		&& newOrientation != -1)
		    		if (setOrientation(newOrientation, arg0))
		    			orientation = newOrientation;
		    }
		};

		if (orientationEventListener.canDetectOrientation()){
			orientationEventListener.enable();
		}
		else{
			//	Device does not have the Orientation sensor;
		}

		/**
		 * When user pushes "button" in the surfaceViewInfo, 
		 * application makes first photo.
		 *  
		 */
		surfaceViewInfo.touchEventCallback = new SurfaceViewInfo.TouchEventCallback() {
			@Override
			public void callbackCall() {
				if (state == States.none){
					state = States.shot1;
					File dir = new File(tempDirectory);
					if (!dir.exists())
						dir.mkdirs();

					android.text.format.DateFormat df = new android.text.format.DateFormat();
					tempSubdir = df.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()).toString();

					dir = new File(tempDirectory+"/"+tempSubdir);
					if (!dir.exists())
						dir.mkdirs();
						
					camera.setTempDirectory(tempDirectory+"/"+tempSubdir);
					camera.setParametersFixed();
					
					camera.focus(1);
					surfaceViewInfo.setButtonDisable();
				}
			}
		};
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menuItemView = menu.findItem(R.id.action_view);
		menuItemView.setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id){
		case R.id.action_settings:
			intent = new Intent(this, SettingsActivity.class);

			ArrayList<String> sizesStr = camera.getSupportedPictureSizes();
			intent.putExtra("sizes", sizesStr);
			
			// Parameters to set preference summaries 
			//intent.putExtra("COUNT_DOWN_TIME",COUNT_DOWN_TIME);
			//intent.putExtra("countDownTime",countDownTime);f
			startActivity(intent);
			break;
		case R.id.action_view:
			showImages(tempDirectory, tempSubdir);
			return true;
		case R.id.action_list:
			dialogListDir();
			return true;
		case R.id.action_camera:
		//	setViewCamera();
			return true;
		case R.id.action_select_camera:
		//	camera.open(true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	/ Open dialog for selection subdirectory
	/ with images to show.
	**/
    private void dialogListDir(){
		final DialogListDir dialog = new DialogListDir(this, tempDirectory);
		dialog.listView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					tempSubdir= dialog.getPath(position);
					//dialog.dismiss();
					showImages(tempDirectory, tempSubdir);
				}});
		dialog.show();
    }
	
	private void showImages(String tempDirectory, String tempSubdir){
		Intent intent;
		intent = new Intent(this, PicturesActivity.class);
		intent.putExtra("tempDirectory", tempDirectory);
		intent.putExtra("tempSubdir", tempSubdir);

		startActivity(intent);
	}
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		
		camera = new Camera(this);
		camera.takePhotoCallback = new Camera.TakePhotoCallback(){
			@Override
			public void callbackCall() {
				surfaceViewInfo.setProgress(0);
				startCountDown();
			}
		};
		
		camera.open();
		
		camera.setPictureSize(pictureSize);
		surfaceViewInfo.setInfo1(pictureSize);

		camera.setPreview(width, height);
		camera.startPreview(surface);
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		Log.d("", "onSurfaceTextureDestroyed");
		camera.release();
        return true;
	}

	@Override
	protected void onPause() {
		Log.d("", "PAUSE");
		super.onPause();
		
		Editor editor = prefs.edit();
		editor.putBoolean(FLASH_MODE, flashMode);
		editor.putString(PICTURE_SIZE, pictureSize);
		
		editor.apply();
	}
	
	@Override
	protected void onResume()	{
		super.onResume();
	}
	
	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String prefName)
	{
		if (prefName.equals(FLASH_MODE))
			flashMode = prefs.getBoolean(FLASH_MODE, true);	
		else if (prefName.equals(PICTURE_SIZE)){
			pictureSize = prefs.getString(PICTURE_SIZE, "");
			camera.setPictureSize(pictureSize);
			surfaceViewInfo.setInfo1(pictureSize);
		}
	}
	
	public boolean setOrientation(int orientation, int info){
		if (camera == null) return false;
		camera.setRotation(orientation);
		return true;
	}
	
	private void startCountDown(){
		
		//list = new ArrayList<String>(); 
		//list1 = new ArrayList<String>(); 
		
		countDownTimer = new CountDownTimer(500, 20) {
		public void onTick(long diff) {
			surfaceViewInfo.setProgress((500-diff)/500f);
//			list.add(""+camera.currentAcceleration);
		}
			
		public void onFinish() { 
				//final ToneGenerator tg = 
				//	new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100); 
				//tg.startTone(ToneGenerator.TONE_PROP_BEEP);

			camera.focus(2);
			
			//camera.takePicture(2);
			camera.prepare();

			state = States.none;
			/*
			surfaceViewInfo.setButtonEnable();
			surfaceViewInfo.setProgress(0);
			Log.d("", "SET ENABLE");
			
			String path = tempDirectory+"/"+tempSubdir+"/1.jpg";
			
			Bitmap ThumbImage = 
				ThumbnailUtils.extractThumbnail(
					BitmapFactory.decodeFile(path), 
					64, 64);
				
			menuItemView.setIcon(
				new BitmapDrawable(
					context.getResources(),
					ThumbImage));
			menuItemView.setVisible(true);
			*/
			
		} 
		};
		countDownTimer.start();	
	}
	
}
