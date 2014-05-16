package com.jc.handler_looper_messageque;
import com.jc.handler_looper_messageque.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class LooperThreadActivity extends Activity {

	private final int MESSAGE_HELLO = 0;
	private Handler mhandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		new CustomThread().start();
		
		findViewById(R.id.send_btn).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String str = "hello";
				Log.d("test", "main thread is ready to send msg: "+str);
				mhandler.obtainMessage(MESSAGE_HELLO, str).sendToTarget();
			}
		});
		
	}

	class CustomThread extends Thread {

		@Override
		public void run() {

			Looper.prepare();
			mhandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch(msg.what){
					case MESSAGE_HELLO:
						Log.d("test", "CustomThread receive message: "+(String)msg.obj);
						break;
					}
				}
			};
           Looper.loop();
		}
	}
}
