package com.jc.location.main;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.jc.locationupdates.R;

public class MainActivity extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		LocationListener{

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	// Milliseconds per second
    private final static int MILLISECONDS_PER_SECONDS = 1000;
    // Update frequency in seconds
    private final static int UPDATE_INTERNAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private final static long UDATE_INTERNAL =
    		                 MILLISECONDS_PER_SECONDS * UPDATE_INTERNAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private final static int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private final static long FASTEST_INTERVAL = 
    		MILLISECONDS_PER_SECONDS * FASTEST_INTERVAL_IN_SECONDS;
	
    // Define an object that holds accuracy and frequency parameters
    private LocationRequest mLocationRequest;
    
	private Location mCurrentLocation;
	private boolean mUpdatesRequested;
	
	private LocationClient mLocationClient;
	
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mEditor;
	
	private TextView mAddress;
	private ProgressBar mActivityIndicator;
	private Button mAddressButton;

	/* mCurrentLocation = mLocationClient.getLastLocation(); */

	public static class ErrorDialogFragment extends DialogFragment {

		private Dialog mDialog;

		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
    
	 /**
	    * A subclass of AsyncTask that calls getFromLocation() in the
	    * background. The class definition has these generic types:
	    * Location - A Location object containing
	    * the current location.
	    * Void     - indicates that progress units are not used
	    * String   - An address passed to onPostExecute()
	    */
	private class GetAddressTask extends AsyncTask<Location,Void,String>{
        
		Context mContext;
		public GetAddressTask(Context context){
			super();
			this.mContext = context;
		}
		
		/**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @params params One or more Location objects
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         */
		@Override
		protected String doInBackground(Location... params) {
			// TODO Auto-generated method stub
			Geocoder geocoder = new Geocoder(mContext,Locale.getDefault());
			
			// Get the current location from the input parameter list
			Location loc = params[0];
			// Create a list to contain the result address
			List<Address> addresses = null;
			
			try{
				//return 1 address
				addresses = geocoder.getFromLocation(loc.getAltitude(), loc.getLongitude(), 1);
			}catch(IOException e1){
				Log.e("LocationSampleActivity", "IO Exception in getFromLocation()");
				e1.printStackTrace();
				return ("IO Exception try to get Address");
			}catch(IllegalArgumentException e2){
				 // Error message to post in the log
	            String errorString = "Illegal arguments " +
	                    Double.toString(loc.getLatitude()) +
	                    " , " +
	                    Double.toString(loc.getLongitude()) +
	                    " passed to address service";
	            Log.e("LocationSampleActivity", errorString);
	            e2.printStackTrace();
	            return errorString;
			}
			
			 // If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
		}
		
		 /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
		@Override
		protected void onPostExecute(String address){
			// Set activity indicator visibility to "gone"
			mActivityIndicator.setVisibility(View.GONE);
			// Display the results of the lookup
			mAddress.setText(address);
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				/*
				 * Try the request again
				 */
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}

	}

	private boolean servicesConnected() {

		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);

		if (ConnectionResult.SUCCESS == resultCode) {
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			/*int errorCode = connectionResult.getErrorCode();*/
			int errorCode = resultCode ;

			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					errorCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			if (errorDialog != null) {
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				errorFragment.show(getSupportFragmentManager(),
						"Location Updates");
			}
			return false;
		}
	}
    
	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		if (connectionResult.hasResolution()) {
			try {
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
			} catch (IntentSender.SendIntentException e) {
				e.printStackTrace();
			}
		} else {
			showErrorDialog(connectionResult.getErrorCode());
		}
	}

	public void showErrorDialog(int connectionErrorCode) {
		int errorCode = connectionErrorCode;
		Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
				this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

		if (errorDialog != null) {
			ErrorDialogFragment errorFragment = new ErrorDialogFragment();
			errorFragment.setDialog(errorDialog);
			errorFragment.show(getSupportFragmentManager(), "Location Updates");
		}
	}
    
	
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
	@Override
	public void onConnected(Bundle dataBundle) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		// If already requested, start periodic updates
		if(true){
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
			mCurrentLocation = mLocationClient.getLastLocation();
		}
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}
    
	public void getAddress(View v){
		 // Ensure that a Geocoder services is available
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.GINGERBREAD
                            &&
                Geocoder.isPresent()) {
            // Show the activity indicator
            mActivityIndicator.setVisibility(View.VISIBLE);
            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes,
             * onPostExecute() displays the address.
             */
            (new GetAddressTask(this)).execute(mCurrentLocation);}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		
		mAddress = (TextView)findViewById(R.id.address);
		mAddress.setText("获取地址中。。。");
		
		mAddressButton = (Button)findViewById(R.id.getAddress);
		mAddressButton.setText("Get Address");
		mAddressButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getAddress(v);
			}
		});
		
		mActivityIndicator = (ProgressBar)findViewById(R.id.address_progress);
		
		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		//Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UDATE_INTERNAL);
		 // Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
		
		
		
		// Open the shared preferences
        mPrefs = getSharedPreferences("SharedPreferences",
        		Context.MODE_PRIVATE);
        // Get a SharedPreferences editor
        mEditor = mPrefs.edit();
        
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);
        
       // Start with updates turned off
        mUpdatesRequested = false;
	}

	@Override
	protected void onStart() {
		mLocationClient.connect();
		super.onStart();
	}
    
	@Override
	protected void onResume(){
		/*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
		if(mPrefs.contains("KEY_UPDATES_ON")){
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
			
		// Otherwise, turn off location updates
		}else{
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
		}
		super.onResume();
	}
	
	@Override
	protected void onPause(){
		// Save the current setting for updates
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();
		super.onPause();
		
	}
	
	 /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
	@Override
	protected void onStop() {
		// If the client is connected
		if(mLocationClient.isConnected()){
			 /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
			mLocationClient.removeLocationUpdates(this);
		}
		/*
         * After disconnect() is called, the client is
         * considered "dead".
         */
		mLocationClient.disconnect();
		super.onStop();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		String msg = "Updated Location: "+
		                Double.toString(location.getLatitude())+","+
		                Double.toString(location.getLongitude());
		
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
