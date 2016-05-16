package com.mochalov.photo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.*;
import android.graphics.SurfaceTexture;
import android.hardware.Camera.*;
import android.media.ExifInterface;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

public class Camera  implements android.hardware.Camera.AutoFocusCallback, android.hardware.Camera.PictureCallback
{
	
	public float currentAcceleration = 0;
	
	private android.hardware.Camera camera;
	private Context context;
	private String tempDirectory;
	private int num;

	public TakePhotoCallback takePhotoCallback;
	
	private List<android.hardware.Camera.Size> sizes;
	
	public Camera(Context context) {
		super();
		this.context = context;		
	}

	interface TakePhotoCallback { 
		void callbackCall(); 
	}
	
	public void takePicture(){
		camera.takePicture(null, null, null, this);
	}
	
	public void setTempDirectory(String tempDirectory)
	{
		this.tempDirectory = tempDirectory;
	}
	
	public void setParametersFixed()
	{
		Parameters p = camera.getParameters();
		p.setAutoExposureLock(true);
		p.setAutoWhiteBalanceLock(true);
		//p.setFocusMode(Parameters.FOCUS_MODE_FIXED);
		p.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		camera.setParameters(p);
	}

	/**
	 * Set the number of the photo
	 * and start focusing.
	 * @param num - number of the photo
	 */
	public void focus(int num){
		this.num = num;
		camera.autoFocus(this);
	}
	
	public void prepare(){
		Parameters p = camera.getParameters();
		p.setAutoExposureLock(false);
		p.setAutoWhiteBalanceLock(false);
		p.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		camera.setParameters(p);
	}
	
	public void setPictureSize(String size)
	{
		Parameters params = camera.getParameters();
		if (! size.equals("")){
			int pos = size.indexOf("x");

			int width = Integer.parseInt(size.substring(0, pos));
			int height = Integer.parseInt(size.substring(pos+1));

			params.setPictureSize(width, height);
		} else 
			params.setPictureSize(sizes.get(0).width, sizes.get(0).height);
		
		camera.setParameters(params);
	}
	
	/**
	 * Open camera and get supported picture sizes 
	 */
	public void open() {
		//Log.d("", "OPEN CAMERA");
		camera = android.hardware.Camera.open();
		//Log.d("", "OPEN CAMERA OK "+camera);
		
        Parameters parameters = camera.getParameters();
        camera.setParameters(parameters);
		sizes = parameters.getSupportedPictureSizes();
	}
	
	public void setPreview(int width, int height) {
        Parameters parameters = camera.getParameters();
        Display display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();

        if(display.getRotation() == Surface.ROTATION_0)
        {
            parameters.setPreviewSize(height, width);                           
            camera.setDisplayOrientation(90);
        }

        if(display.getRotation() == Surface.ROTATION_90)
        {
            parameters.setPreviewSize(width, height);                           
        }

        if(display.getRotation() == Surface.ROTATION_180)
        {
            parameters.setPreviewSize(height, width);               
        }

        if(display.getRotation() == Surface.ROTATION_270)
        {
            parameters.setPreviewSize(width, height);
            camera.setDisplayOrientation(180);
        }
	}

	public void startPreview(SurfaceTexture surface) {
        try {
            camera.setPreviewTexture(surface);
        } catch (IOException t) {
        }
        camera.startPreview();
	}
/*	
	public void stop() {
		camera.stopPreview();
        camera.release();
	}
*/
	public boolean setRotation(int orientation) {
		Parameters params;
		try{
			params = camera.getParameters();
		} 
		catch (Exception e) { 
			return false;
		} 
		try{
			params.setRotation(orientation);	
			camera.setParameters(params);
		} 
		catch (Exception e) { 
			Toast.makeText(context, "BAD PARAMETER "+orientation, Toast.LENGTH_LONG).show();
			return false;
		}
		return true; 
	}

	/**
	 * When picture is taken, save it. If number of picture equal to 1, call back
	 * to main class for waiting and making second picture.
	 */
	@Override
	public void onPictureTaken(byte[] paramArrayOfByte, android.hardware.Camera camera) {
        try
        {
            File saveDir = new File(tempDirectory);
            if (!saveDir.exists())
            {
                saveDir.mkdirs();
            }
            FileOutputStream os = new FileOutputStream(tempDirectory+"/"+num+".jpg");
            os.write(paramArrayOfByte);
            os.close();
        }
        catch (Exception e)
        {
			Toast.makeText(context, "ERROR "+e, Toast.LENGTH_LONG).show();
        }
        camera.startPreview();
        if (num == 1){
			takePhotoCallback.callbackCall();
        }
        
	}

	@Override
	public void onAutoFocus(boolean success, android.hardware.Camera camera) {
		// TODO Auto-generated method stub
		android.text.format.DateFormat df = new android.text.format.DateFormat();
		String tempSubdir = df.format("yyyy-MM-dd hh:mm:ss", new java.util.Date()).toString();
		Log.d("", "AUTO !!!  "+success+"  "+tempSubdir);
		
		takePicture();
		
	}

	public ArrayList<String> getSupportedPictureSizes(){
		ArrayList<String> sizesStr = new ArrayList<String>();
		for (android.hardware.Camera.Size c:sizes)
			sizesStr.add(""+c.width+"x"+c.height);
		
		return sizesStr;
	}

	public void release() {
		camera.stopPreview();
		camera.release();
	}
	
}
