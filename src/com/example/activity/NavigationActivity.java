package com.example.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

abstract public class NavigationActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	protected void changeActivity(Context  from, Class to) {
		Intent intent = new Intent();
		intent.setClass(from, to);
		startActivity(intent);
		finish();
	}
}
