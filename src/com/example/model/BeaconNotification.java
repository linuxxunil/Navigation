package com.example.model;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.example.navigation.R;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class BeaconNotification {
	
	static Intent intent = null;
	static public void registerActivity(Intent it) {
		intent = it;
	}
	
	static public void unregisterActivity() {
		intent = null;
	}
	
	static public boolean isRegister() {
		return intent == null ? false : true;
	}
	
	static public void send(Context ctx,String text) {
		int notifyID = 1; // 通知的識別號碼
		int requestCode = notifyID; // PendingIntent的Request Code
		int flags = PendingIntent.FLAG_CANCEL_CURRENT; // ONE_SHOT：PendingIntent只使用一次；CANCEL_CURRENT：PendingIntent執行前會先結束掉之前的；NO_CREATE：沿用先前的PendingIntent，不建立新的PendingIntent；UPDATE_CURRENT：更新先前PendingIntent所帶的額外資料，並繼續沿用
	
		
		PendingIntent pendingIntent = (intent == null) ? 
				 PendingIntent.getActivity(ctx, requestCode, new Intent(), flags)
				:PendingIntent.getActivity(ctx, requestCode, intent, flags); // 取得PendingIntent
				 
		NotificationManager notificationManager = 
				(NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE); // 取得系統的通知服務
		
		Notification notification = new Notification.Builder(ctx)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("內容標題")
				.setContentText(text)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL)
				.build(); // 建立通知
		
		notificationManager.notify(notifyID, notification); // 發送通知
	}
}
