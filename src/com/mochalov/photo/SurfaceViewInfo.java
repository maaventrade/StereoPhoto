package com.mochalov.photo;

import java.io.File;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 
 * @author @Alexey Mochalov
 * Draw button and progress over the textureView
 */
public class SurfaceViewInfo extends SurfaceView implements SurfaceHolder.Callback {
	private DrawThread drawThread;
	String info1 = "";
	
	public TouchEventCallback touchEventCallback; // Event: snapshot button clicked
	
    public SurfaceViewInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
        getHolder().addCallback(this);
	}

	public SurfaceViewInfo(Context context) {
        super(context);
        getHolder().addCallback(this);
    }
    
	interface TouchEventCallback { 
		void snapShotButtonClicked(); // Event: snapshot button clicked (ACTION_UP) 
	}
	
	/**
	 * 
	 * @param progress - percent of the time elapced
	 */
	public void setProgress(float progress){
		drawThread.rectProgress.right = drawThread.rectButtonShot.left+drawThread.rectButtonShot.width() * progress; 
	}
	
	@Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
        int height) {	
		holder.setFormat(PixelFormat.TRANSPARENT);
		drawThread.paintButton.setStyle(Paint.Style.FILL_AND_STROKE);
		drawThread.rectButtonShot = new RectF(30, height-160, width-30, height-60);
		drawThread.rectProgress = new RectF(drawThread.rectButtonShot);
		drawThread.rectProgress.right = drawThread.rectButtonShot.left; 
	}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	drawThread = new DrawThread(getHolder(), this);
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        // finish the thread wirking
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again and again
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (drawThread.rectButtonShot.contains(event.getX(), event.getY()))
				drawThread.setStateOn();
			break;
		case MotionEvent.ACTION_MOVE:
			if (! drawThread.rectButtonShot.contains(event.getX(), event.getY()))
				drawThread.setStateOff();
			break;
		case MotionEvent.ACTION_UP:
			if (drawThread.rectButtonShot.contains(event.getX(), event.getY())){
				// Take photos 
				touchEventCallback.snapShotButtonClicked();
			}
			else drawThread.setStateOff();
			break; 
		}		
		return true; 
    }

	public void setButtonDisable() {
		drawThread.setStateDisabled();
	}

	public void setButtonEnable() {
		drawThread.setStateOff();
	}    	     

	public void setInfo(String info1) {
		this.info1 = info1;
	}    	     
}

class DrawThread extends Thread{
    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;

	RectF rectButtonShot = null;
	RectF rectProgress = null;
	
	Paint paintButton = new Paint(Paint.ANTI_ALIAS_FLAG);
	Paint paintProgress = new Paint();
	
	private enum states {on, off, disabled};
	private states state = states.off;
	
    public DrawThread(SurfaceHolder surfaceHolder, SurfaceViewInfo surfaceViewInfo){
        this.surfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

	public void setStateOn(){
		state = states.on;
	}

	public void setStateOff(){
		state = states.off;
	}

	public void setStateDisabled(){
		state = states.disabled;
	}
    
    @Override
    public void run() {
        Canvas canvas;
        Paint textPaint = new Paint();
        textPaint.setTextSize(40);
        
        while (runFlag) {
            long now = System.currentTimeMillis();
            canvas = null;
            try {
                // get the Canvas and start drawing
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                	if (canvas != null){
                        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);	
                        //canvas.drawText("Hello World! "+now, 10, 450, textPaint);
                        //canvas.drawText(surfaceViewInfo.info1, 10, 500, textPaint);
                       
                        if (rectButtonShot != null){
                    		if (state == states.on){
                    				paintButton.setColor(Color.YELLOW);
                    			paintButton.setStyle(Paint.Style.FILL_AND_STROKE);
                        		canvas.drawRect(rectButtonShot, paintButton);
                    			
                    			paintButton.setColor(Color.rgb(63, 163, 248));
                    			paintButton.setStyle(Paint.Style.STROKE);
                    			paintButton.setStrokeWidth(12);
                        		canvas.drawRect(rectButtonShot, paintButton);
                        		
                    			paintButton.setStyle(Paint.Style.FILL_AND_STROKE);
                    			paintButton.setStrokeWidth(1);
                    				paintButton.setColor(Color.rgb(0, 0, 128));
                    			paintButton.setTextSize(40);
                    			
                    			String text = "Click button and rotate camera->";
                    			Rect bounds = new Rect(); 
                    			paintButton.getTextBounds(text, 0, text.length(), bounds);
                    			
                    			float top = rectButtonShot.top + rectButtonShot.height()/2 + 10;
                    			float left = rectButtonShot.left+rectButtonShot.width()/2 - bounds.width()/2;
                    			
                                canvas.drawText(text, left, top, paintButton);
                    		} else if (state == states.off){
                    			paintButton.setColor(Color.argb(200, 135, 206, 235));
                    			paintButton.setStyle(Paint.Style.FILL_AND_STROKE);
                        		canvas.drawRect(rectButtonShot, paintButton);
                    			
                    			paintButton.setColor(Color.rgb(63, 163, 248));
                    			paintButton.setStyle(Paint.Style.STROKE);
                    			paintButton.setStrokeWidth(12);
                        		canvas.drawRect(rectButtonShot, paintButton);
                        		
                    			paintButton.setStyle(Paint.Style.FILL_AND_STROKE);
                    			paintButton.setStrokeWidth(1);
                    			paintButton.setColor(Color.WHITE);
                    			paintButton.setTextSize(40);
                    			
                    			String text = "Click button and rotate camera->";
                    			Rect bounds = new Rect(); 
                    			paintButton.getTextBounds(text, 0, text.length(), bounds);
                    			
                    			float top = rectButtonShot.top + rectButtonShot.height()/2 + 10;
                    			float left = rectButtonShot.left+rectButtonShot.width()/2 - bounds.width()/2;
                    			
                                canvas.drawText(text, left, top, paintButton);
                    			
                        	} else {
                				paintButton.setColor(Color.GRAY);
                				paintButton.setStyle(Paint.Style.FILL_AND_STROKE);
                				canvas.drawRect(rectButtonShot, paintButton);
                			
                				paintButton.setColor(Color.rgb(63, 163, 248));
                				paintButton.setStyle(Paint.Style.STROKE);
                				paintButton.setStrokeWidth(12);
                				canvas.drawRect(rectButtonShot, paintButton);
                    		
                				paintButton.setStyle(Paint.Style.FILL_AND_STROKE);
                				paintButton.setStrokeWidth(1);
                				paintButton.setColor(Color.rgb(0, 0, 128));
                				paintButton.setTextSize(40);
                			
                				String text = "Click button and rotate camera->";
                				Rect bounds = new Rect(); 
                				paintButton.getTextBounds(text, 0, text.length(), bounds);
                			
                				float top = rectButtonShot.top + rectButtonShot.height()/2 + 10;
                				float left = rectButtonShot.left+rectButtonShot.width()/2 - bounds.width()/2;
                			
                        	}	
                        }
                		if (rectProgress != null){
                            paintProgress.setColor(Color.BLACK);
                    		canvas.drawRect(rectProgress, paintProgress);
                		}
                	}
                }
            } 
            finally {
                if (canvas != null) {
                    // drawing is finished
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
