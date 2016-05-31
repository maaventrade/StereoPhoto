package com.mochalov.photo;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.Bitmap;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;

import java.io.*;

import com.mochalov.photo.R;

public class PicturesActivity extends Activity{
	private static final String TAG = "myActivityView";
	private static final String index1 = "INDEX1";
	private static final String index2 = "INDEX2";
	private static final String TEMPSUBDIR = "TEMPSUBDIR";
	private static final String TEMPDIR = "TEMPDIR";
	
	private ViewPictures viewPictures;
	ActionBar ab;

	private String tempDirectory = "";
	private String tempSubdir = "";
	private Context context;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
    	super.onCreate(savedInstanceState);
    	
    	context = this;
    	
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
    	
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
    	ab = getActionBar();
    	ab.hide();
        
        setContentView(R.layout.view);
        
        Intent myIntent = getIntent(); 
        tempDirectory = myIntent.getStringExtra("tempDirectory");
        tempSubdir = myIntent.getStringExtra("tempSubdir");
        
        viewPictures = (ViewPictures)findViewById(R.id.view);
        viewPictures.setAbIsVisible(ab.isShowing());
        
        viewPictures.tapCallback = new ViewPictures.TapCallback() {
			@Override
			public void callbackCall() {
				if (ab.isShowing()) ab.hide();
				else ab.show();
				viewPictures.setAbIsVisible(ab.isShowing());
			}
		};
		
		int i1 = 1;
		int i2 = 2;
		if (savedInstanceState != null){
			if (savedInstanceState.containsKey(index1))
				i1 = savedInstanceState.getInt(index1, 1);
			if (savedInstanceState.containsKey(index2))
				i2 = savedInstanceState.getInt(index2, 2);
			if (savedInstanceState.containsKey(TEMPDIR))
				tempDirectory = savedInstanceState.getString(TEMPDIR);
			if (savedInstanceState.containsKey(TEMPDIR))
				tempSubdir = savedInstanceState.getString(TEMPSUBDIR);
		}
		viewPictures.setDir(tempDirectory, tempSubdir);
		viewPictures.setIndex(i1, i2);
	}
    
	/**
	/* When Activity starts it checks is tempSubdir defined.
	/* If is not defined then opens the Dialog for selecting the tempSubdir
	**/
    @Override
    protected void onStart() {
        super.onStart();
        if (tempSubdir.equals(""))
			dialogListDir();
        
    }    
    
    private void dialogListDir(){
		final DialogListDir dialog = new DialogListDir(this, tempDirectory);
		dialog.listView.setOnItemClickListener(new OnItemClickListener(){
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			tempSubdir= dialog.getPath(position);
			dialog.dismiss();
			viewPictures.reSetDir(tempDirectory, tempSubdir);
			
		}});
		dialog.show();
		
    }

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt(index1, viewPictures.getIndex1());
		outState.putInt(index2, viewPictures.getIndex2());
		outState.putString(TEMPSUBDIR, tempSubdir);
		outState.putString(TEMPDIR, tempDirectory);
		
		super.onSaveInstanceState(outState);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view, menu);
		return true;
	}
    
	private void swop(){
		viewPictures.swop();
	}
	
	private void DeleteRecursive(File fileOrDirectory) {
	    if (fileOrDirectory.isDirectory())
	        for (File child : fileOrDirectory.listFiles())
	            DeleteRecursive(child);

	    fileOrDirectory.delete();
	}
	
	private void deleteDir(){
		if (tempSubdir.equals("")){
			Toast.makeText(context, "Directory is not selected.", Toast.LENGTH_LONG).show();
			return;
		}
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
					File dir = new File(tempDirectory+"/"+tempSubdir);
					DeleteRecursive(dir);
					tempSubdir = "";
					viewPictures.reSetDir(tempDirectory, tempSubdir);
		            break;
		        case DialogInterface.BUTTON_NEGATIVE:
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Delete directory "+tempSubdir+" with all photos?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
		case (R.id.action_list):
			dialogListDir();
			return true;
		case (R.id.action_delete):
			deleteDir();
			return true;
		case (R.id.action_swop):
				swop();
				return true;
		case (R.id.action_make):
			makeJpeg();
			return true;
		default:
			return true;
		}
	}

	private void makeJpeg()
	{
		viewPictures.makeJpeg();
	}
	
    @Override
    public void onResume(){
		super.onResume();
		viewPictures.init(this);
    }
}
