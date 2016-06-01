package com.mochalov.photo;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.media.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import java.io.*;
import java.util.*;

public class ViewPictures extends ImageView{
	//public boolean onTouchEvent(MotionEvent event)
	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	String savedItemClicked;
	
	
	private static final String TAG = "myView";
	private Paint paint = new Paint();
	
	private int height;
	private int width;
	
	private String tempDirectory;
	private String tempSubdir;
	
	private int index1 = 1;
	private int index2 = 2;
	
	float scale = 1; // Coefficient of the zooming
	
	private boolean abIsVisible;
	
	TapCallback tapCallback;
	
	/*
	* Swaps left and right images
	*/
	public void swop()
	{
		int index;
		index = index1;
		index1 = index2;
		index2 = index;
		//Toast.makeText(context, src1.height()
		
		//image1 = load(index1, src1, dst1, 0);
		//image2 = load(index2, src1, dst2, imageWidth+2);
		invalidate();
	}
	
	interface TapCallback { 
		void callbackCall(); 
	}
	
	public ViewPictures(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public ViewPictures(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void loadImages(){
		Log.d("", "tempSubdir "+tempSubdir);
		
		if (tempSubdir.equals("")) return;

		Bitmap bitmap1 = loadImage(1);
		Bitmap bitmap2 = loadImage(2);
		
		//Create a new image bitmap and attach a brand new canvas to it
		Bitmap tempBitmap = Bitmap.createBitmap(bitmap1.getWidth() + bitmap2.getWidth(), bitmap1.getHeight(), Bitmap.Config.RGB_565);
		Canvas tempCanvas = new Canvas(tempBitmap);

		//Draw the image bitmap into the canvas
		tempCanvas.drawBitmap(bitmap1, 0, 0, null);
		tempCanvas.drawBitmap(bitmap2, bitmap1.getWidth(), 0, null);

		//Attach the canvas to the ImageView
		setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
		
        setScaleType(ImageView.ScaleType.MATRIX);
        float scale;
        int dx;
        int dy;
        if (height > width){
        	scale = (float)width/getDrawable().getIntrinsicWidth();
        	dx = 0;
        	dy = (height - getDrawable().getIntrinsicHeight())/2;
        } else {
        	scale = (float)height/getDrawable().getIntrinsicHeight();
        	dy = 0;
        	dx = (width - getDrawable().getIntrinsicWidth())/2;
        }
       	matrix.postScale(scale, scale);
        matrix.postTranslate(dx, dy);
		
		paint.setTextSize(34);
		paint.setColor(Color.YELLOW);
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		height = getHeight();
		width = getWidth();
		
		if (width > 0){
			loadImages();
			invalidate();
		}
	}
	
	/**
	 * 
	 * @param fileIndex - index of image file in subdirectory
	 * @return loaded file bitmap
	 * 
	 */
	private Bitmap loadImage(int fileIndex){
		BitmapFactory.Options options = new BitmapFactory.Options();
		/*options.inJustDecodeBounds = false;
		//options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		options.inSampleSize = 8;
		*/

		String path = tempDirectory+"/"+tempSubdir+"/"+fileIndex+".jpg";
    	File imgFile = new  File(path);
    	Bitmap bmp = null;
		int orientation = 1;
    	try {
    		bmp = BitmapFactory.decodeStream(new FileInputStream(imgFile), null, options);

			ExifInterface exif = new ExifInterface(path);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch(orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					bmp = rotateImage(bmp, 90);
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					bmp = rotateImage(bmp, 180);
					break;
			}
			return bmp;
		} catch (FileNotFoundException e) {
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return bmp;
	}
	
	
	public Bitmap rotateImage(Bitmap source, float angle)
	{
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}	
	
	public void setDir(String tempDirectory, String  tempSubdir){
		this.tempDirectory = tempDirectory;
		this.tempSubdir = tempSubdir;
		
	}
	
	public void setIndex(int index1, int index2){
		this.index1 = index1;
		this.index2 = index2;

	}
	
	public void reSetDir(String tempDirectory, String  tempSubdir){
		this.tempDirectory = tempDirectory;
		this.tempSubdir = tempSubdir;
		loadImages();
		invalidate();
	}
	
	
	public void init(Context context){
		//this.context = context;
	}
	
	public int getIndex1(){
		return index1;
	}
	
	public int getIndex2(){
		return index2;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    // TODO Auto-generated method stub

        setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

	    // Handle touch events here...
	    switch (event.getAction() & MotionEvent.ACTION_MASK) {
	    case MotionEvent.ACTION_DOWN:
	        savedMatrix.set(matrix);
	        start.set(event.getX(), event.getY());
	        Log.d(TAG, "mode=DRAG");
	        mode = DRAG;
	        break;
	    case MotionEvent.ACTION_POINTER_DOWN:
	        oldDist = spacing(event);
	        Log.d(TAG, "oldDist=" + oldDist);
	        if (oldDist > 10f) {
	            savedMatrix.set(matrix);
	            midPoint(mid, event);
	            mode = ZOOM;
	            Log.d(TAG, "mode=ZOOM");
	        }
	        break;
	    case MotionEvent.ACTION_UP:
	    case MotionEvent.ACTION_POINTER_UP:
	        mode = NONE;
            savedMatrix.set(matrix);
	        break;
	    case MotionEvent.ACTION_MOVE:
			if (event.getPointerCount() < 2){
	            matrix.set(savedMatrix);
	            matrix.postTranslate(event.getX() - start.x, event.getY()
	                    - start.y);
	        } else if (mode == ZOOM) {
	        	// pinch zooming
	            float newDist = spacing(event);
	            if (newDist > 10f) {
	                matrix.set(savedMatrix);
	                scale = newDist / oldDist; // setting the scaling of the
                    // matrix...if scale > 1 means
                    // zoom in...if scale < 1 means
                    // zoom out
	                matrix.postScale(scale, scale, mid.x, mid.y);
		            matrix.postTranslate(event.getX() - start.x, event.getY()
		                    - start.y);
	            } else {
	                matrix.set(savedMatrix);
		            matrix.postTranslate(event.getX() - start.x, event.getY()
		                    - start.y);
	            }
	            
	        }
	        break;
	    }
	
	    setImageMatrix(matrix);
//	    invalidate();
	    return true;
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
	    float x = event.getX(0) - event.getX(1);
	    float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
	    float x = event.getX(0) + event.getX(1);
	    float y = event.getY(0) + event.getY(1);
	    point.set(x / 2, y / 2);
	}

	public void setAbIsVisible(boolean visability){
		abIsVisible = visability;
		invalidate();
	}

	public void makeJpeg() {
		/*
		int width = src1.width();
		int height = src1.height();
		Bitmap bmp = Bitmap.createBitmap(width*2, height, Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bmp);
		canvas.drawBitmap(image1, src1, new Rect(0,0,src1.width(),src1.height()), paint);
		canvas.drawBitmap(image2, src1, new Rect(src1.width() ,0, src1.width()*2,src1.height()), paint);
		
		String picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
		
		String fileName = tempSubdir+".jpg";
		
		File file = new File (picturesDir, fileName);
		
		if (file.exists()) file.delete ();
		
		try {
		    FileOutputStream out = new FileOutputStream(file);
		    bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
		    out.flush();
		    out.close();
			Toast.makeText(context, "Saved "+file.toString(), Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			Toast.makeText(context, "Error saving file. "+e.toString(), Toast.LENGTH_LONG).show();
		}		
*/		
	}
	
	
}
