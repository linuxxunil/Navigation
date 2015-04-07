package com.example.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

abstract public class NavigationService extends Service {

	protected Handler handler = null;

	abstract protected void initHandler();

	@Override
	public void onCreate() {
		super.onCreate();
		initHandler();
	}
	
	@Override
	abstract public IBinder onBind(Intent intent);
}
