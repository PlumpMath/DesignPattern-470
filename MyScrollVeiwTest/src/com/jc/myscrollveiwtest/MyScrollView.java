package com.jc.myscrollveiwtest;

/**
 * BufferedOutputStream 
 * write(byte[] array) 会写入多余的数据，文件会变大
 * 实际调用 write(byte[] array,int offset,int len)
 * len为数组的长度。最后一次写入流时,len一般不会为读入字节的长度.除非文件大小刚好被BUFFER_SIZE整除
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView implements OnTouchListener {

	private LinearLayout firstColumn;
	private LinearLayout secondColumn;
	private LinearLayout thirdColumn;

	private int firstColumnHeight;
	private int secondColumnHeight;
	private int thirdColumnHeight;

	private ScrollView scrollView;
	private LinearLayout scrollViewLayout;

	private int scrollViewHeight;
	private int scrollViewLayoutHeight;

	private int columnWidth;

	private ImageLoader imageLoader;

	private int SIZE_IN_ONE_PAGE = 15;
	private int page;
	private boolean loadOnce;

	private ArrayList<ImageView> viewList = new ArrayList<ImageView>();

	private Set<LoadTask> taskCollection = new HashSet<LoadTask>();

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageLoader = ImageLoader.getInstance();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		return false;
	}
    
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		/**
		 * this view 分配大小和位置给childview时都会调用此方法。
		 */
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			scrollView = (ScrollView) findViewById(R.id.container);
			scrollViewLayout = (LinearLayout) findViewById(R.id.childcontainer);
			// scrollViewLayout = (LinearLayout) getChildAt(0);

			scrollViewHeight = scrollView.getHeight();
			// scrollViewHeight = getHeight();
			scrollViewLayoutHeight = scrollViewLayout.getHeight();

			firstColumn = (LinearLayout) findViewById(R.id.firstColumn);
			secondColumn = (LinearLayout) findViewById(R.id.secondColumn);
			thirdColumn = (LinearLayout) findViewById(R.id.thirdColumn);

			columnWidth = firstColumn.getWidth();

			loadOnce = true;
			// load the first page
			loadImages();
		}
	}

	private void loadImages() {
		int startIndex = page * SIZE_IN_ONE_PAGE;
		int endIndex = startIndex + SIZE_IN_ONE_PAGE;

		if (startIndex < Images.imageUrls.length) {
			if (endIndex > Images.imageUrls.length)
				endIndex = Images.imageUrls.length;
			for (int i = startIndex; i < endIndex; i++) {
				String key_url = Images.imageUrls[i];
				Bitmap bitmap = ImageLoader.getBitmapFromMemeory(key_url);
				if (bitmap != null) {
					addBitmapToView(bitmap, bitmap.getWidth(),
							bitmap.getHeight(), key_url);
				} else {
					LoadTask task = new LoadTask();
					taskCollection.add(task);
					task.execute(key_url);
				}
			}
		}
		page++;
	}

	private void addBitmapToView(Bitmap bitmap, int width, int height,
			String url) {
		if (bitmap != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					width, height);
			ImageView view = new ImageView(getContext());
			view.setLayoutParams(params);
			view.setImageBitmap(bitmap);
			view.setScaleType(ScaleType.FIT_XY);
			view.setPadding(5, 5, 5, 5);
			view.setTag(R.string.image_url, url);
			findLinearLayoutColumn(view, height).addView(view);
			viewList.add(view);
		}
	}

	private LinearLayout findLinearLayoutColumn(ImageView view, int imageHeight) {

		if (firstColumnHeight <= secondColumnHeight) {
			if (firstColumnHeight <= thirdColumnHeight) {
				view.setTag(R.string.border_top, firstColumnHeight);
				firstColumnHeight += imageHeight;
				view.setTag(R.string.border_bottom, firstColumnHeight);
				return firstColumn;
			} else {
				view.setTag(R.string.border_top, thirdColumnHeight);
				thirdColumnHeight += imageHeight;
				view.setTag(R.string.border_bottom, thirdColumnHeight);
				return thirdColumn;
			}
		} else {
			if (secondColumnHeight <= thirdColumnHeight) {
				view.setTag(R.string.border_top, secondColumnHeight);
				secondColumnHeight += imageHeight;
				view.setTag(R.string.border_bottom, secondColumnHeight);
				return secondColumn;
			} else {
				view.setTag(R.string.border_top, thirdColumnHeight);
				thirdColumnHeight += imageHeight;
				view.setTag(R.string.border_bottom, thirdColumnHeight);
				return thirdColumn;
			}
		}

	}

	public void downloadImage(String imageUrl) {
		URL url = null;
		HttpURLConnection con = null;
		File file = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		BufferedInputStream bis = null;

		try {
			url = new URL(imageUrl);
			con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(5 * 1000);
			con.setReadTimeout(15 * 1000);
			con.setDoInput(true);
			con.setDoOutput(true);
			bis = new BufferedInputStream(con.getInputStream());
			file = new File(ImageLoader.getImagePath(imageUrl));
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			byte[] b = new byte[1024];
			int length; 
			while ((length=bis.read(b)) != -1) {
				bos.write(b,0,length);
				bos.flush();
			}

			bos.close();
			bis.close();
			con.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class LoadTask extends AsyncTask<String, Void, Bitmap> {

		private String url;
        private Bitmap bitmap;
		
		@Override
		protected Bitmap doInBackground(String... params) {
            
			url = params[0];
			File file = new File(ImageLoader.getImagePath(url));
			if(!file.exists())
				downloadImage(url);
			bitmap = ImageLoader.decodeSampleBitmapFromResource(url,
					columnWidth);
			ImageLoader.addToMemeory(url, bitmap);
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				double radio = bitmap.getWidth() / (columnWidth * 1.0);
				final int scaledHeight = (int) (bitmap.getHeight() / radio);
				addBitmapToView(bitmap, columnWidth, scaledHeight, url);
				taskCollection.remove(this);
			}
		}
	}
}
