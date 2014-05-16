package com.jc.photowallfallsdemo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

public class ImageLoader {
  
	private static LruCache<String,Bitmap> mMemoryCache;
	
	private static ImageLoader mImageLoader;
	
	private ImageLoader(){
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory/8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
			
		    @SuppressLint("NewApi")
			protected int sizeOf(String key, Bitmap value) {
		      	return value.getByteCount();
		    }
		};
	}
	
	public static ImageLoader getInstance(){
		if(mImageLoader == null)
			mImageLoader = new ImageLoader();
		return mImageLoader;
	}
	
	public void addBitmapToMemoryCache(String key,Bitmap bitmap){
		if(getBitmapToMemoryCache(key)==null)
			mMemoryCache.put(key, bitmap);
	}
	
	public Bitmap getBitmapToMemoryCache(String key){
		return mMemoryCache.get(key);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
			                                int reqWidth,
			                                int reqHeight){
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if(height > reqHeight || width > reqWidth){
			final int halfHeight = height/2;
			final int halfWidth = width/2;
			
			while((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth){
				inSampleSize *= 2;
			}
		}
		
		return inSampleSize;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options,
			                                int reqWidth){
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		/*if(width > reqWidth){
			final int halfWidth = width/2;
			
			while((halfWidth / inSampleSize) > reqWidth)
				inSampleSize *= 2;
		}*/
		if(width > reqWidth ){
			final int widthRadio = Math.round((float)width/(float)reqWidth);
			inSampleSize = widthRadio;
		}
		
		return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth){
		final BitmapFactory.Options options = new BitmapFactory.Options();
		
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName,options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}
}
