package com.jc.location.main;

/**
 * Storage for geofence values, implemented in SharedPreferences.
 */
public class SimpleGeofenceStore {
	 // Keys for flattened geofences stored in SharedPreferences
	private static final String KEY_LATITUDE =
			"com.jc.android.geofence,KEY_LATITUDE";
	private static final String KEY_LONGITUDE = 
			"com.jc.android.geofence.KEY_LONGITUDE";
	private static final String KEY_RADIUS =
			"com.jc.android.geofence.KEY_RADIUS";
	private static final String KEY_EXPIRATION_DURATION =
			"com.jc.android.geofence.KEY_EXPIRATION_DURATION";
	private static final String KEY_TRANSATION_TYPE =
			"com.jc.android.geofence.KEY_TRANSATION_DURATION";
	
	 // The prefix for flattened geofence keys
	private static final String KEY_PREFIX =
			"com.jc.android.geofence.KEY";
	
	 /*
     * Invalid values, used to test geofence storage when
     * retrieving geofences
     */
	public static final long INVALID_LONG_VALUE = -9991;
	public static final float INVALID_FLOAT_VALUE = -999.0f;
	public static final int INVALID_INT_VALUE = -999;
}
