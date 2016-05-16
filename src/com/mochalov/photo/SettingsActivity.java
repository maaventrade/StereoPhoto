package com.mochalov.photo;

import android.content.*;
import android.os.*;
import android.preference.*;
import android.view.*;

import java.util.*;

import com.mochalov.photo.R;

public class SettingsActivity extends PreferenceActivity
{
	private SharedPreferences prefs;
	private Preference pref1;
	private Context context;
	
	private ArrayList<String> sizesStr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
							 WindowManager.LayoutParams.FLAG_FULLSCREEN);					 
       
		getFragmentManager().beginTransaction()
        	.replace(android.R.id.content, new PreferencesFragment()).commit();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Intent intent = getIntent();
		sizesStr = intent.getStringArrayListExtra("sizes");
		
		context = this;
	}

	public class PreferencesFragment
	extends PreferenceFragment
	{ 
		@Override
		public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.preferences);
			
	        // Set flash mode. If true turn flash on when shouting
			pref1 = findPreference("FLASH_MODE");
			
			String[] entries = new String[sizesStr.size()];
			sizesStr.toArray(entries);
			
			ListPreference lp = (ListPreference)findPreference("PICTURE_SIZE");
			
			lp.setEntries(entries);
			lp.setEntryValues(entries);
			
			//if (getPackageManager().hasSystemFeature(
	        //        PackageManager.FEATURE_CAMERA_FLASH))
			
		}
	}
	
	
}
