package com.example.practice;

/**point 1:
 *   getView() 
 *     可以从LruCache中加载 
 *     若从SD卡中加载，速度很慢，会导致ListView 显示图片错位。
 * point 2;
 *   实现OnScrollListener
 *   只在ListView 停止滑动时，进行图片加载（从SD卡或者网络）
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements OnScrollListener{
    
	private ListView listView;
	private LayoutInflater inflater;
	private LruCache<String, Bitmap> cache;
	private boolean isFirstEnter;
	private int mFirstVisibleItem;
	private int mVisibleItemCount;
	private Set<MyTask> tasks;
	
	private File dir;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
		isFirstEnter = true;
		
		long maxSize = Runtime.getRuntime().maxMemory();
		long cacheSize = maxSize/8;
		cache = new LruCache<String, Bitmap>((int) cacheSize){
			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getByteCount();
			}
		};
		
		if(isExternalStorageWritable())
		    dir = getSuperCarsStorageDir("Superracer");
		
		tasks = new HashSet<MyTask>();
		
		/*dir = getSuperCarsStorageDir("SuperCars");*/
		/*inflater = getLayoutInflater();*/
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		listView = (ListView) findViewById(R.id.listview);
		
		MyAdapter adapter = new MyAdapter();
		listView.setAdapter(adapter);
		listView.setOnScrollListener(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		cancelAllTasks();
	}
	
	public void addToMemoryCache(String key,Bitmap bitmap){
		if(key!= null && bitmap!= null)
			if(getBitmapFromMemory(key)==null)
		           cache.put(key, bitmap);
	}
	
	public Bitmap getBitmapFromMemory(String key){
		return cache.get(key);
	}
	
	public static class ViewHolder{
		public TextView text;
		public ImageView image;
	}
    
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Images.cars.length;
		}

		@Override
		public String getItem(int position) {
			// TODO Auto-generated method stub
			return Images.link[position];
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final String url =  getItem(position);
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = inflater.inflate(R.layout.list_layout, parent, false);
				holder.image = (ImageView) convertView.findViewById(R.id.image);
				holder.image.setTag(url);
				holder.text = (TextView) convertView.findViewById(R.id.text);
				convertView.setTag(holder);
			}else{
			    holder = (ViewHolder) convertView.getTag();
			}
			
			holder.text.setText(Images.cars[position]);
			setImage(holder.image,position);
			
			return convertView;
		}
		
	}
	
	public void setImage(ImageView imageView,int position){
		Bitmap bitmap = getBitmapFromMemory(Images.link[position]);
		if(bitmap != null){
			imageView.setImageBitmap(bitmap);
		}else{
			imageView.setImageResource(R.drawable.empty_photo);
		}
	}
	
	public Bitmap getBitmapFromMemoryOrExternalStorage(String key){
		Bitmap bitmap = null;
		bitmap =getBitmapFromMemory(key);
		if(bitmap == null)
			bitmap = getBitmapFromExternal(key);
		return bitmap;
	}
	
	public Bitmap getBitmapFromExternal(String key){
		Bitmap bitmap = null;
		String s[] = key.split("/");
		int length = s.length;
		File file = new File(dir, s[length-1]);
		if(file.exists())
			bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
		return bitmap;
	}
	
	class MyTask extends AsyncTask<String,Void,Bitmap>{
        
		String imageUrl;
		
		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			imageUrl = params[0];
			URL url;
			HttpURLConnection connection;
			InputStream in =null;
			Bitmap bitmap = null;
			try {
				url = new URL(imageUrl);
				connection = (HttpURLConnection) url.openConnection();
				in =  connection.getInputStream();
				if(in!=null)
					  bitmap = BitmapFactory.decodeStream(in);
				in.close();
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(bitmap!=null){
				addToMemoryCache(imageUrl,bitmap);
			    addToExternalStorage(imageUrl,bitmap);
			}
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			ImageView imageView = (ImageView) listView.findViewWithTag(imageUrl);
			if(result!=null && imageView!=null){
		    imageView.setImageBitmap(result);
		    }
			tasks.remove(this);
		}
	}
	
	public void addToExternalStorage(String key, Bitmap bitmap){
		int index = key.lastIndexOf("/");
		String name = key.substring(index + 1);
		
		File file = new File(dir,name);	
		FileOutputStream out =null;
		try {
		   out= new FileOutputStream(file);
		   bitmap.compress(CompressFormat.JPEG, 100, out);
		} catch (IOException e) {
			Log.e("MyLog", "storage bitmap fail");
		}	
		
	}
	
	//check the external storage state
	public boolean isExternalStorageWritable(){
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)){
			return true;
		}
        return false;
	}
	
	public boolean isExternalStorageReadable(){
		String state = Environment.getExternalStorageState();
		if(Environment.MEDIA_MOUNTED.equals(state)||
				Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			return true;
		}
		return false;
	}
	
	public File getSuperCarsStorageDir(String dirName){
		File file = new File(Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
				,dirName);
		if(!(file.mkdirs()||file.isDirectory())){
			Log.e("MyLog", "Directory not create successfully");
		}
		return file;
	}
    
	/**
	 * Callback method to be invoked while the list view or grid view is being scrolled.
	 * If the view is being scrolled, this method will be called before the next frame 
	 * of the scroll is rendered. In particular, it will be called before 
	 * any calls to getView(int, View, ViewGroup).
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		Log.d("random", "onStateChanged enter");
		if(scrollState == SCROLL_STATE_IDLE){
			loadBitmaps(mFirstVisibleItem,mVisibleItemCount);
		}else{
			cancelAllTasks();
		}
	}
    
	/**
	 * onScroll This will be called after the scroll has completed
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		Log.e("random", "onScroll enter");
		mFirstVisibleItem = firstVisibleItem;
		mVisibleItemCount = visibleItemCount;
		
		//
		if(isFirstEnter && visibleItemCount >0){
			loadBitmaps(firstVisibleItem,visibleItemCount);
			isFirstEnter = false;
		}
		
	}
   
	public void loadBitmaps(int firstVisibleItem, int visibleItemCount){
		for(int i = firstVisibleItem; i<firstVisibleItem+visibleItemCount;i++){
			String imageUrl = Images.link[i];
			Bitmap bitmap = getBitmapFromMemoryOrExternalStorage(imageUrl);
			if(bitmap==null){
				MyTask task = new MyTask();
				tasks.add(task);
				task.execute(imageUrl);
			}else{
				ImageView imageView = (ImageView) listView.findViewWithTag(imageUrl);
				if(imageView!=null)
					imageView.setImageBitmap(bitmap);
			}
		}
	}
	
	public void cancelAllTasks(){
		if(!tasks.isEmpty()){
			for(MyTask task : tasks)
				task.cancel(false);
		}
	}
}
