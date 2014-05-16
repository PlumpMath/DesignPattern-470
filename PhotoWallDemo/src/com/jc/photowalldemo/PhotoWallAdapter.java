package com.jc.photowalldemo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class PhotoWallAdapter extends ArrayAdapter<String> implements OnScrollListener{
    
	//记录正在下载或等待下载的任务
	private Set<BitmapWorkerTask> taskCollection;
	
	//缓存所有下载好的图片
	private LruCache<String,Bitmap> mMemoryCache; 
	
	private GridView mPhotoWall;
	private int mFirstVisibleItem;
	private int mVisibleItemCount;
	
	//记录是否刚打开程序，用于解决进入程序不滚动屏幕，不会下载图片的问题
	private boolean isFirstIn = true;
	
	public PhotoWallAdapter(Context context, int textViewResourceId,
			String[] objects,GridView photoWall) {
		super(context,textViewResourceId,objects);
		mPhotoWall = photoWall;
		taskCollection = new HashSet<BitmapWorkerTask>();
		
		int maxMemory = (int)Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory/8;
		mMemoryCache = new LruCache<String,Bitmap>(cacheSize){
			@Override
			protected int sizeOf(String key,Bitmap bitmap){
				return bitmap.getByteCount();
			}
		};
		
		mPhotoWall.setOnScrollListener(this);
	}
    
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		final String url = getItem(position);
		
		if(convertView == null){
			convertView = LayoutInflater
					.from(getContext())
					.inflate(R.layout.photo_layout, null);
		}
		
		final ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
		
		// 给ImageView设置一个Tag，保证异步加载图片时不会乱序  
		photo.setTag(url);
		setImageView(url,photo);
		return convertView;
	}
	
	 /** 
     * 给ImageView设置图片。首先从LruCache中取出图片的缓存，设置到ImageView上。如果LruCache中没有该图片的缓存， 
     * 就给ImageView设置一张默认图片。 
     *  
     * @param imageUrl 
     *            图片的URL地址，用于作为LruCache的键。 
     * @param imageView 
     *            用于显示图片的控件。 
     */  
	public void setImageView(String imageUrl,ImageView imageView){
		Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
		if(bitmap!=null){
			imageView.setImageBitmap(bitmap);
		}else{
			imageView.setImageResource(R.drawable.empty_photo);
		}
	}
	
	public void addBitmapToMemoryCache(String key, Bitmap bitmap){
		if(getBitmapFromMemoryCache(key)==null){
			mMemoryCache.put(key, bitmap);
		}
	}
	
	public Bitmap getBitmapFromMemoryCache(String key){
		return mMemoryCache.get(key);
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		if(scrollState == SCROLL_STATE_IDLE){
			loadBitmaps(mFirstVisibleItem,mVisibleItemCount);
		}else{
			cancelAllTasks();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		mFirstVisibleItem = firstVisibleItem;
		mVisibleItemCount = visibleItemCount;
		
		if(isFirstIn && visibleItemCount > 0){
			loadBitmaps(firstVisibleItem,visibleItemCount);
			isFirstIn = false;
		}
	}
	
	private void loadBitmaps(int firstVisibleItem, int visibleItemCount){
		
		for(int i = firstVisibleItem; i< firstVisibleItem+visibleItemCount; i++){
			String imageUrl = Images.src[i];
			Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
			if(bitmap == null){
				BitmapWorkerTask task = new BitmapWorkerTask();
				taskCollection.add(task);
				task.execute(imageUrl);
			}else{
				ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
				if(imageView!=null)
					imageView.setImageBitmap(bitmap);
			}
		}
	}
    
	public void cancelAllTasks(){
		if(taskCollection!=null){
			for(BitmapWorkerTask task : taskCollection)
				task.cancel(false);
		}
	}
	
	class BitmapWorkerTask extends AsyncTask<String,Void,Bitmap>{
        
		private String imageUrl;
		
		@Override
		protected Bitmap doInBackground(String... params) {
			imageUrl = params[0];
			Bitmap bitmap = downloadBitmap(imageUrl);
			if(bitmap != null){
				addBitmapToMemoryCache(imageUrl,bitmap);
			}
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			// TODO Auto-generated method stub
			super.onPostExecute(bitmap);
			
			ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
			if(imageView!=null && bitmap != null){
				imageView.setImageBitmap(bitmap);
			}
			taskCollection.remove(this);
		}
	}
	
	private Bitmap downloadBitmap(String imageUrl){
		Bitmap bitmap = null;
		HttpURLConnection con = null;
		try {
		URL url = new URL(imageUrl);
		con = (HttpURLConnection) url.openConnection();
		con.setConnectTimeout(5*1000);
		con.setReadTimeout(10*1000);
		con.setDoInput(true);
		con.setDoOutput(true);
		bitmap = BitmapFactory.decodeStream(con.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(con!=null)
				con.disconnect();
		}
		return bitmap;
	}
}
