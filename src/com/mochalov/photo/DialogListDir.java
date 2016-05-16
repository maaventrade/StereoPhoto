package com.mochalov.photo;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;

import java.io.*;
import java.util.*;

import com.mochalov.photo.R;

public class DialogListDir extends Dialog{
	private Context context;
	public ListView listView;

	private ArrayList<String> values;

	public DialogListDir(Context context, String tempDirectory) {
		super(context,android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen
				  );
			this.context = context;
			
			setContentView(R.layout.dialog_list_dir);
			setTitle("Select directory with photos");

			listView = (ListView)findViewById(R.id.listDir); 

			values = new ArrayList<String>();  
			
			File dir = new File(tempDirectory); 
			if (dir.isDirectory()) {
				String[] children = dir.list();
			    for (int i = 0; i < children.length; i++) {
			    	values.add(children[i]);
			    }
			}
			
			ArrayAdapter adapter = new ArrayAdapter<String>(context,
	                android.R.layout.simple_list_item_1, values);
			
			listView.setAdapter(adapter);
	        
		registerForContextMenu(listView);
			/*
			Button buttonAdd = (Button)findViewById(R.id.buttonAdd);
			buttonAdd.setOnClickListener(new Button.OnClickListener(){
					@Override
					public void onClick(View p1)
					{
						edit(null, null);
					}
				});
			*/
	}
	@Override public void onCreateContextMenu(
		ContextMenu menu, View v, 
		ContextMenu.ContextMenuInfo menuInfo) { 
		if (v.getId() == R.id.listDir) { 
		ListView lv = (ListView) v; 
		AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo; 
		String str = (String) lv.getItemAtPosition(acmi.position);
		menu.add("Delete");
		menu.add("Two");
		menu.add("Three");
		menu.add(str); 
		menu.setHeaderTitle("Directory:"+str);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		if (item.toString().equals("Delete")){
			;
		}
		return super.onContextItemSelected(item);
	}
	
	

	public String getPath(int position){
		return values.get(position);
	}

}
