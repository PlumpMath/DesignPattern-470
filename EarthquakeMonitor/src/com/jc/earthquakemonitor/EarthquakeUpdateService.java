package com.jc.earthquakemonitor;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.IBinder;

public class EarthquakeUpdateService extends Service{
    
	public static String TAG = "EARTHQUAKE_UPDATE_SERVICE";
	
	private void addNewQuake(Quake quake){
		ContentResolver cr = getContentResolver();
		
		//Construct a where clause to make sure we don't already have this earthquake in the provider
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
