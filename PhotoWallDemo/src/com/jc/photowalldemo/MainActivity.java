package com.jc.photowalldemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

public class MainActivity extends Activity {
    
	private GridView mPhotoWall;
	private PhotoWallAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
		mPhotoWall = (GridView) findViewById(R.id.photo_wall);
		adapter = new PhotoWallAdapter(this, 0, Images.src, mPhotoWall);
		mPhotoWall.setAdapter(adapter);
	}
	
	@Override
	protected void onStop() {
		
		super.onStop();
		adapter.cancelAllTasks();
	}
}
