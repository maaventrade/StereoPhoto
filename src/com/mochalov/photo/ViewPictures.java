package com.mochalov.photo;

import android.content.*;
import android.graphics.*;
import android.media.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public class ViewPictures extends View{
	private static final String TAG = "myView";
	private Context context;
	private Paint paint = new Paint();
	
	private int height;
	private int width;
	private int imageWidth;
	
	private Bitmap image1 = null;
	private Bitmap image2 = null;
	private Rect src1 = new Rect();

	private Rect dst12 = new Rect();
	private Rect dst22 = new Rect();
	
	private Rect dst1 = new Rect();
	private Rect dst2 = new Rect();
	
	private String tempDirectory;
	private String tempSubdir;
	
	private float x0;
	private float y0;
	
	private int index1 = 1;
	private int index2 = 2;
	
	double scale = 1; // Coeffitient of zoominh
	
	private boolean abIsVisible;
	
	TapCallback tapCallback;
	
	/*
	* Swaps left and right images
	*/
	public void swop()
	{
		index = index1;
		index1 = index2;
		index2 = index;
		//Toast.makeText(context, src1.height()
		
		image1 = load(index1, src1, dst1, 0);
		image2 = load(index2, src1, dst2, imageWidth+2);
		invalidate();
	}
	
	private Bitmap load(int i, Rect src, Rect dst, int dx){
		Bitmap img = loadImage(i);
		src.set(new Rect(0, 0, img.getWidth()-1, img.getHeight()-1));
		float k = (float)img.getWidth()/img.getHeight();
		dst.set(new Rect(dx, 0, imageWidth+dx, (int)(imageWidth/k)));
		
		return img;
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

	private void loadFileNames(){
		if (tempSubdir.equals("")) return;

		image1 = load(1, src1, dst1, 0);
		image2 = load(2, src1, dst2, imageWidth+2);
		
		dst12 = new Rect(dst1);
		dst22 = new Rect(dst2);
		
		paint.setTextSize(34);
		paint.setColor(Color.YELLOW);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		height = getHeight();
		width = getWidth();
		imageWidth = width/2 - 2;
		if (imageWidth > 0){
			loadFileNames();
			invalidate();
		}
	}
	
	@SuppressWarnings("deprecation")
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
		loadFileNames();
		invalidate();
	}
	
	
	public void init(Context context){
		this.context = context;
	}
	
	public int getIndex1(){
		return index1;
	}
	
	public int getIndex2(){
		return index2;
	}
	
	private Rect rectMove;
	private int x1;
	private int y1;
	private float x2;
	private float y2;
	private int index;
	
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
	double oldDist = 1f;
	String savedItemClicked;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    // TODO Auto-generated method stub

	    //ImageView view = (ImageView) v;
	  //  dumpEvent(event);
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
	        Log.d(TAG, "mode=NONE");
	        break;
	    case MotionEvent.ACTION_MOVE:
	        if (mode == DRAG) {
	        	
	        	//dst12
	        	
	            // ...
	            matrix.set(savedMatrix);
	            matrix.postTranslate(event.getX() - start.x, event.getY()
	                    - start.y);
	        } else if (mode == ZOOM) {
	            double newDist = spacing(event);
	            Log.d(TAG, "newDist=" + newDist);
	            if (newDist > 10f) {
	               // matrix.set(savedMatrix);
	                scale = scale + (newDist / oldDist)/10;
					
					dst12.right = (int)(dst1.right * scale); 
					dst12.bottom = (int)(dst1.bottom * scale); 

					dst22.right = (int)(dst2.right * scale); 
					dst22.bottom = (int)(dst2.bottom * scale); 
					
//					src2.right = (int)(src2.right * scale); 
//					src2.bottom = (int)(src2.bottom * scale); 
					
//					Log.d(TAG,"scale "+scale+"  "+src2.left+"  "+src2.right);
					
					
					invalidate();
	               // matrix.postScale(scale, scale, mid.x, mid.y);
	            }
	        }
	        break;
	    }
	

	  //  this.setImageMatriex(matrix);
	    return true;
	}

	/** Determine the space between the first two fingers */
	private double spacing(MotionEvent event) {
	    float x = event.getX(0) - event.getX(1);
	    float y = event.getY(0) - event.getY(1);
		return Math.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
	    float x = event.getX(0) + event.getX(1);
	    float y = event.getY(0) + event.getY(1);
	    point.set(x / 2, y / 2);
	}


	@Override
	protected void onDraw(Canvas canvas){
		ExifInterface exif1 = null;
		ExifInterface exif2 = null;
		try
		{
			exif1 = new ExifInterface(tempDirectory+"/"+tempSubdir + "/" + index1+".jpg");
			exif2 = new ExifInterface(tempDirectory+"/"+tempSubdir + "/" + index2+".jpg");
		}
		catch (IOException e)
		{
			Log.d(TAG,"ERROR "+e);
		}
		
		
		if (image1 != null){
			canvas.drawBitmap(image1, src1, dst12, paint);
		}
		if (image2 != null){
			canvas.drawBitmap(image2, src1, dst22, paint);
		}
		/*
		if (abIsVisible){
			canvas.drawText(tempSubdir+"/"+index1, dst1.left, Math.min(dst1.bottom, getHeight()), paint);
			canvas.drawText(tempSubdir+"/"+index2, dst2.left, Math.min(dst2.bottom, getHeight()), paint);
		} else {
			canvas.drawText(""+index1, dst1.left, Math.min(dst1.bottom, getHeight()), paint);
			canvas.drawText(""+index2, dst2.left, Math.min(dst2.bottom, getHeight()), paint);
		}
		*/
		
		/*
		paint.setColor(Color.BLUE);
		canvas.drawRect(new RectF(0,0,dst2.right,300), paint);
		paint.setColor(Color.YELLOW);
		canvas.drawText("aperture "+exif1.getAttribute(ExifInterface.TAG_APERTURE),dst1.left,20,paint);
		canvas.drawText("datetime "+exif1.getAttribute(ExifInterface.TAG_DATETIME),dst1.left,50,paint);
		canvas.drawText("exposure "+exif1.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),dst1.left,80,paint);
		canvas.drawText("focal "+exif1.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),dst1.left,110,paint);
		
		canvas.drawText("aperture "+exif2.getAttribute(ExifInterface.TAG_APERTURE),dst2.left,20,paint);
		canvas.drawText("datetime "+exif2.getAttribute(ExifInterface.TAG_DATETIME),dst2.left,50,paint);
		canvas.drawText("exposure "+exif2.getAttribute(ExifInterface.TAG_EXPOSURE_TIME),dst2.left,80,paint);
		canvas.drawText("focal "+exif2.getAttribute(ExifInterface.TAG_FOCAL_LENGTH),dst2.left,110,paint);
		
		*/
	}
	
	public void setAbIsVisible(boolean visability){
		abIsVisible = visability;
		invalidate();
	}

	public void makeJpeg() {
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
		
	}
	
	
}
