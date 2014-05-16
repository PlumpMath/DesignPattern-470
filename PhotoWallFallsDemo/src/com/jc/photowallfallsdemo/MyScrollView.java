package com.jc.photowallfallsdemo;
/**
 * ScrollView getHeight() 获取到的值是固定不变的，即屏幕的高度
 * ScrollView 中嵌套的View,如LinearLayout 会随着的内容的加载，getHeight()的值不断变化
 */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class MyScrollView extends ScrollView implements OnTouchListener{
  
	public static final int SIZE_IN_ONE_PAGE = 15;
	
	//record the current page
	private int page;
	//the columnWidth 
	private int columnWidth;       
	//the height of first column
	private int firstColumnHeight; 
	//the height of second column
	private int secondColumnHeight;
	//the height of third column
	private int thirdColumnHeight;
	
	private boolean loadOnce;
	
	private ImageLoader imageLoader;
	
	private LinearLayout firstColumn;
	private LinearLayout secondColumn;
	private LinearLayout thirdColumn;
	
	private static Set<LoadImageTask> taskCollection;
	
	private static LinearLayout scrollLayout;
	private static int scrollViewHeight;
	
	//record the distance of Y
	private static int lastScrollY = -1;
	
	private List<ImageView> imageViewList = new ArrayList<ImageView>();
	
	private static Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			MyScrollView myScrollView = (MyScrollView)msg.obj;
			int scrollY = myScrollView.getScrollY();
			 // 如果当前的滚动位置和上次相同，表示已停止滚动  
			if(scrollY == lastScrollY){
				// 当滚动到最底部，并且当前没有正在下载的任务时，开始加载下一页的图片
				Log.e("MyLog", "scrollView height: "+myScrollView.getHeight());
				Log.e("MyLog", "scrollY =: "+scrollY);
				Log.e("MyLog", "scrollLayout height: "+scrollLayout.getHeight());
				if(scrollViewHeight + scrollY >= scrollLayout.getHeight()
						&& taskCollection.isEmpty()){
					myScrollView.loadMoreImages();
				}
				myScrollView.checkVisibility();
			}else{
				lastScrollY = scrollY;
				Message message = new Message();
				message.obj = myScrollView;
				handler.sendMessageDelayed(message, 5);
			}
		}
	};
	
	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageLoader = ImageLoader.getInstance();
		taskCollection = new HashSet<LoadImageTask>();
		setOnTouchListener(this);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if(event.getAction() == MotionEvent.ACTION_UP){
			Message message = new Message();
			message.obj = this;
			handler.sendMessageDelayed(message, 5);
		}
		return false;
	}
	
	public void loadMoreImages(){
		if(hasSDCard()){
			int startIndex = page * SIZE_IN_ONE_PAGE;
			int endIndex = startIndex + SIZE_IN_ONE_PAGE;
			
			if(startIndex < Images.imageUrls.length){
				Toast.makeText(getContext(), "正在加载。。。", Toast.LENGTH_SHORT).show();
				if(endIndex > Images.imageUrls.length)
					endIndex = Images.imageUrls.length;
				
				for(int i =startIndex;i<endIndex;i++){
					LoadImageTask task = new LoadImageTask();
					taskCollection.add(task);
					task.execute(Images.imageUrls[i]);
				}
				page++;
			}else{
				Toast.makeText(getContext(), "已没有更多图片", Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(getContext(), "未发现SD卡", Toast.LENGTH_SHORT).show();
		}
	}
	
	/** 
     * 遍历imageViewList中的每张图片，对图片的可见性进行检查，
     * 如果图片已经离开屏幕可见范围，则将图片替换成一张空图。 
     */  
	public void checkVisibility(){
		for(int i= 0;i< imageViewList.size();i++){
			ImageView imageView = imageViewList.get(i);
			int borderTop = (Integer) imageView.getTag(R.string.border_top);
			int borderBottom = (Integer) imageView.getTag(R.string.border_bottom);
			
			if(borderBottom > getScrollY()
					&& borderTop < (getScrollY()+scrollViewHeight)){
				String imageUrl = (String)imageView.getTag(R.string.image_url);
				Bitmap bitmap = imageLoader.getBitmapToMemoryCache(imageUrl);
				if(bitmap != null)
					imageView.setImageBitmap(bitmap);
				else{
					/**
					 * 1.图片可见，但LruCache中没有，说明图片已保存到本地但已被从LruCache中释放，
					 * 无需重复下载，只需从本地中加载即可（考虑到加载速度问题，故使用异步任务）
					 * 2.不要将此task 添加到taskCollection中
					 */
					LoadImageTask task = new LoadImageTask(imageView);
					task.execute(imageUrl);
				}
			}else{
				imageView.setImageResource(R.drawable.empty_photo);
			}
		}
	}
	
	private boolean hasSDCard(){
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
    
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		 /** 
	     * 进行一些关键性的初始化操作，获取MyScrollView的高度，以及得到第一列的宽度值。
	     * 并在这里开始加载第一页的图片。 
	     */  
		super.onLayout(changed, l, t, r, b);
		if(changed && !loadOnce){
			//MyScrollView 的高度
			scrollViewHeight = getHeight();
			scrollLayout = (LinearLayout) getChildAt(0);
			firstColumn = (LinearLayout) findViewById(R.id.first_column);  
	        secondColumn = (LinearLayout) findViewById(R.id.second_column);  
	        thirdColumn = (LinearLayout) findViewById(R.id.third_column); 
	        columnWidth = firstColumn.getWidth();
	        loadOnce = true;
	        loadMoreImages();
		}
	}
	
	class LoadImageTask extends AsyncTask<String,Void,Bitmap>{
        
		private String mImageUrl;
		private ImageView mImageView;
		
		public LoadImageTask(){
		}
		
		public LoadImageTask(ImageView imageView){
			this.mImageView = imageView;
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			
			mImageUrl = params[0];
			Bitmap imageBitmap = imageLoader.getBitmapToMemoryCache(mImageUrl);
			if(imageBitmap == null){
				imageBitmap = loadImage(mImageUrl);
			}
			return imageBitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			
			if(bitmap != null){
				double radio = bitmap.getWidth() / (columnWidth * 1.0);
				int scaleHeight = (int) (bitmap.getHeight() / radio);
				addImage(bitmap,columnWidth,scaleHeight);
			}
			taskCollection.remove(this);
		}
		
		private Bitmap loadImage(String imageUrl){
			File imageFile = new File(getImagePath(imageUrl));
			if(!imageFile.exists()){
				downloadImage(imageUrl);
			}
			if(imageUrl !=null ){
				Bitmap bitmap = ImageLoader
						.decodeSampledBitmapFromResource(imageFile.getPath(), columnWidth);
				if(bitmap != null){
					imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
					return bitmap;
				}
			}
			return null;
		}
		
		private void addImage(Bitmap bitmap, int imageWidth,int imageHeight){
			
			LinearLayout.LayoutParams params = 
					new LinearLayout.LayoutParams(imageWidth,imageHeight);
			if(mImageView !=null )
				mImageView.setImageBitmap(bitmap);
			else{
				ImageView imageView = new ImageView(getContext());
				imageView.setLayoutParams(params);
				imageView.setImageBitmap(bitmap);
				imageView.setScaleType(ScaleType.FIT_XY);
				imageView.setPadding(5, 5, 5, 5);
				imageView.setTag(R.string.image_url,mImageUrl);
				findColumnToAdd(imageView,imageHeight)
				        .addView(imageView);
				imageViewList.add(imageView);
			}
		}
		
		private LinearLayout findColumnToAdd(ImageView imageView,
				int imageHeight){
			if(firstColumnHeight <= secondColumnHeight){
				if(firstColumnHeight <= thirdColumnHeight){
					imageView.setTag(R.string.border_top, firstColumnHeight);
					firstColumnHeight += imageHeight;
					imageView.setTag(R.string.border_bottom, firstColumnHeight);
					return firstColumn;
				}
				imageView.setTag(R.string.border_top, thirdColumnHeight);
				thirdColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, thirdColumnHeight);
				return thirdColumn;
			}else{
				if(secondColumnHeight <= thirdColumnHeight){
					imageView.setTag(R.string.border_top, secondColumnHeight);
					secondColumnHeight += imageHeight;
					imageView.setTag(R.string.border_bottom, secondColumnHeight);
					return secondColumn;
				}
				imageView.setTag(R.string.border_top, thirdColumnHeight);
				thirdColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, thirdColumnHeight);
				return thirdColumn;
			}
		}
		
		private void downloadImage(String imageUrl){
			HttpURLConnection con = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			BufferedInputStream bis = null;
			File imageFile = null;
			
			try {
				URL url = new URL(imageUrl);
				con = (HttpURLConnection) url.openConnection();
				con.setConnectTimeout(1000 * 5);
				con.setDoOutput(true);
				con.setDoInput(true);
				bis = new BufferedInputStream(con.getInputStream());
				imageFile = new File(getImagePath(imageUrl));
				fos = new FileOutputStream(imageFile);
				bos = new BufferedOutputStream(fos);
				byte[] b = new byte[1024]; 
				int length;
				while((length = bis.read(b)) != -1){
					bos.write(b,0,length);
					bos.flush();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try{
				if(bis != null)
					bis.close();
				if(bos != null)
					bos.close();
				if(con != null)
					con.disconnect();
				}catch(IOException e){e.printStackTrace();}
			}
			
			if(imageFile != null){
				Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if(bitmap!= null)
					imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
			}
		}
		
		private String getImagePath(String imageUrl){
			int lastSlashIndex = imageUrl.lastIndexOf("/");
			String imageName = imageUrl.substring(lastSlashIndex + 1);
			String imageDir = Environment.getExternalStorageDirectory()
					.getPath() + "/PhotoWallFalls/";
			File file = new File(imageDir);
			if(!file.exists())
				file.mkdirs();
			String imagePath = imageDir + imageName;
			return imagePath;
		}
	}
	
}
