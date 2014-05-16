package com.jc.myscrollveiwtest;

import java.io.File;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Environment;
import android.util.LruCache;

@SuppressLint("NewApi")
public class ImageLoader {

	private static ImageLoader imageLoader;

	private static LruCache<String, Bitmap> memoryCache;

	private ImageLoader() {
		final int maxMemory = (int) Runtime.getRuntime().maxMemory();
		final int cacheSize = maxMemory / 8;

		memoryCache = new LruCache<String, Bitmap>(cacheSize) {
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};
	}

	public static ImageLoader getInstance() {
		if (imageLoader == null) {
			imageLoader = new ImageLoader();
		}
		return imageLoader;
	}

	public static void addToMemeory(String key, Bitmap bitmap) {
		if (getBitmapFromMemeory(key) == null)
			memoryCache.put(key, bitmap);
	}

	public static Bitmap getBitmapFromMemeory(String key) {
		return memoryCache.get(key);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int width = reqWidth;
		final int height = reqHeight;
		int inSampleSize = 1;

		if (width > reqWidth || height > reqHeight) {

			final int halfWidth = width / 2;
			final int halfHeight = height / 2;

			while ((halfWidth / inSampleSize) > reqWidth
					&& (halfHeight / inSampleSize) > reqHeight) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth) {
		// Raw height and width of image
		final int width = reqWidth;

		int inSampleSize = 1;

		if (width > reqWidth) {
			final int halfWidth = width / 2;
			while ((halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampleBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static Bitmap decodeSampleBitmapFromResource(String url, int reqWidth) {
		
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(getImagePath(url), options);

			options.inSampleSize = calculateInSampleSize(options, reqWidth);
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(getImagePath(url), options);
	}

	public static String getImagePath(String imageUrl) {
		int lastIndex = imageUrl.lastIndexOf("/");
		String imageName = imageUrl.substring(lastIndex + 1);
		String imageDir = Environment.getExternalStorageDirectory().getPath()
				+ "/PhotoWalls/";
		File file = new File(imageDir);
		if (!file.exists())
			file.mkdirs();
		String imagePath = imageDir + imageName;
		return imagePath;
	}
}
