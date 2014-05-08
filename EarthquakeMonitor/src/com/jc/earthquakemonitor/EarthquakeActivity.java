package com.jc.earthquakemonitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

//ªÚ’ﬂºÃ≥– FragmentActivity
public class EarthquakeActivity extends ActionBarActivity {
	
	private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_UPDATE = Menu.FIRST+1;
	
	private static final int SHOW_PREFERENCES = 1;
	
	public int minimumMagnitude = 0;
	public boolean autoUpdateChecked = false;
	public int updateFreq = 0;
	
	private void updateFromPreferences(){
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		int minMagIndex = prefs.getInt(PreferencesActivity.PREF_MIN_MAG_INDEX, 0);
		if(minMagIndex < 0)
			minMagIndex = 0;
		
		int freqIndex = prefs.getInt(PreferencesActivity.PREF_UPDATE_FREQ_INDEX, 0);
		if(freqIndex<0)
			freqIndex = 0;
		
		autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);
		
		Resources r = getResources();
		//Get the options value from the array
		String[] minMagValues = r.getStringArray(R.array.magnitude);
		String[] freqValues = r.getStringArray(R.array.update_freq_values);
		
		//COnvert the values to int
		minimumMagnitude = Integer.valueOf(minMagValues[minMagIndex]);
		updateFreq = Integer.valueOf(freqValues[freqIndex]);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		updateFromPreferences();
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(MENU_PREFERENCES, MENU_UPDATE, Menu.NONE, R.string.menu_preferences);
		/*// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);*/
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		  case MENU_UPDATE:
			  Intent i = new Intent(this,PreferencesActivity.class);
			  startActivityForResult(i,SHOW_PREFERENCES);
			  return true;
	      default:break;
		}
		
		return false;
	}
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == SHOW_PREFERENCES)
			if(resultCode == Activity.RESULT_OK){
			     updateFromPreferences();
			     FragmentManager fm = getSupportFragmentManager();
			     final EarthquakeListFragment earthquakeList =
			    		 (EarthquakeListFragment)fm.findFragmentById(R.id.EarthquakeListFragment);
			     Thread t = new Thread(new Runnable(){
			    	 @Override
			    	 public void run(){
			    		 earthquakeList.refreshEarthquakes();
			    	 }
			     });
			     t.start();
		    }
	}
}
