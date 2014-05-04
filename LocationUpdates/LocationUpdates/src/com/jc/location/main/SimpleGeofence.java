package com.jc.location.main;

import com.google.android.gms.location.Geofence;

/**
 * A single Geofence object, defined by its center and radius.
 */
public class SimpleGeofence {
     
	//Instance variables
	private final String mId;
	private final double mLatitude;
	private final double mLongitude;
	private final float mRadius;
	private long mExpirationDuration;
	private int mTransitionType;
	
	/**
     * @param geofenceId The Geofence's request ID
     * @param latitude Latitude of the Geofence's center.
     * @param longitude Longitude of the Geofence's center.
     * @param radius Radius of the geofence circle.
     * @param expiration Geofence expiration duration
     * @param transition Type of Geofence transition.
     */
	public SimpleGeofence(String geofenceId,
			              double latitude,
			              double longitude,
			              float radius,
			              long expiration,
			              int transition){
		this.mId = geofenceId;
		this.mLatitude = latitude;
		this.mLongitude = longitude;
		this.mRadius = radius;
		this.mExpirationDuration = expiration;
		this.mTransitionType = transition;
	}

	public String getmId() {
		return mId;
	}

	public double getmLatitude() {
		return mLatitude;
	}

	public double getmLongitude() {
		return mLongitude;
	}

	public float getmRadius() {
		return mRadius;
	}

	public long getmExpirationDuration() {
		return mExpirationDuration;
	}

	public int getmTransitionType() {
		return mTransitionType;
	}

	/**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
	
	public Geofence toGeofence(){
		
		return new Geofence.Builder()
		           .setRequestId(getmId())
		           .setTransitionTypes(getmTransitionType())
		           .setCircularRegion(getmLatitude(), getmLongitude(), getmRadius())
		           .setExpirationDuration(getmExpirationDuration())
		           .build();
	}
}
