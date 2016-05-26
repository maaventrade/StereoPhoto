package com.mochalov.photo;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import java.io.*;
import java.util.*;

public class DialogPictures extends Dialog
{
	private Context context;
	private static final String index1 = "INDEX1";
	private static final String index2 = "INDEX2";
	private static final String TEMPSUBDIR = "TEMPSUBDIR";
	private static final String TEMPDIR = "TEMPDIR";

	private ViewPictures viewPictures;
	//ActionBar ab;

	private String tempDirectory = "";
	private String tempSubdir = "";
	
	public DialogPictures(Context context, String tempDirectory) {
		super(context);
		
		//getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
        //        WindowManager.LayoutParams.MATCH_PARENT);
		
		this.context = context;
		//ab = getActionBar();
    	//ab.hide();

        setContentView(R.layout.view);

        viewPictures = (ViewPictures)findViewById(R.id.view);
        //viewPictures.setAbIsVisible(ab.isShowing());

        viewPictures.tapCallback = new ViewPictures.TapCallback() {
			@Override
			public void callbackCall() {
			//	if (ab.isShowing()) ab.hide();
			//	else ab.show();
			//	viewPictures.setAbIsVisible(ab.isShowing());
			}
		};

		int i1 = 1;
		int i2 = 2;
		
		viewPictures.setDir(tempDirectory, tempSubdir);
		viewPictures.setIndex(i1, i2);
			
			
	}


}
