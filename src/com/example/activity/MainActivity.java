package com.example.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.model.Beacon;
import com.example.model.BeaconBase;
import com.example.model.BeaconNotification;
import com.example.model.BeaconWebViewClient;
import com.example.model.AsyncHttpClient;
import com.example.model.BluetoothLowEnergy;
import com.example.navigation.R;
import com.example.service.BeaconService;
import com.example.service.BeaconServiceConnection;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

public class MainActivity extends NavigationActivity {
	private final String CLASSNAME = this.getClass().getName();

	private Messenger activityMsger = null;
	private BeaconServiceConnection beaconServiceConn = null;
	private Handler handler = null;
	private boolean isBind = false;
	private WebView webView = null;
	private Context context = null;
	static public boolean active = true;

	// for test
	private String demoURL = "http://demo.coder.com.tw/ibeacon/webview/index.html";
	private Button bt = null;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		setContentView(R.layout.activity_main);

		// for test
		bt = (Button) findViewById(R.id.button1);

		bt.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent restartIntent = context.getPackageManager()
						.getLaunchIntentForPackage(context.getPackageName());
				PendingIntent intent = PendingIntent.getActivity(context, 0,
						restartIntent, Intent.FLAG_ACTIVITY_CLEAR_TOP);
				AlarmManager manager = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				manager.set(AlarmManager.RTC, System.currentTimeMillis() + 2,
						intent);
				System.exit(2);
			}
		});

		// for test end

		webView = (WebView) findViewById(R.id.webview);

		activityMsger = initServiceHandler();

		initWebView();
		
		inspectBluetoohAvailable();
		inspectNetworkAvailable();
	}

	private void initWebView() {
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAppCacheEnabled(false);
		webView.setWebViewClient(bwvc);
		webView.clearCache(true);
		webView.loadUrl(demoURL);
	}

	private void initBeaconService() {
		doStartService();
		beaconServiceConn = new BeaconServiceConnection(activityMsger);
		doBindService(beaconServiceConn);
	}

	@Override
	protected void onPause() {
		super.onPause();
		BeaconNotification.registerActivity(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
		BeaconNotification.unregisterActivity();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			doUnbindService();
		} catch (Throwable e) {
			e.getStackTrace();
		}
	}

	private void doStartService() {
		startService(new Intent(this, BeaconService.class));
	}

	private void doStopService() {
		stopService(new Intent(this, BeaconService.class));
	}

	private void doBindService(BeaconServiceConnection bsc) {
		Intent bindIntent = new Intent(this, BeaconService.class);
		bindService(bindIntent, bsc, BIND_AUTO_CREATE);
		isBind = true;
	}

	private void doUnbindService() {
		if (isBind) {
			// If we have received the service, and hence registered with it,
			// then now is the time to unregister.
			unbindService(beaconServiceConn);
			isBind = false;
		}
	}

	/**
	 * Handle Service Message
	 */
	private Messenger initServiceHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				System.out.println("HandleMessage");
				Bundle bundle = msg.getData();

				String udid = bundle.getString("udid");
				String mac = bundle.getString("mac");
				String uuid = bundle.getString("uuid");
				int major = bundle.getInt("major");
				int minor = bundle.getInt("minor");
				int rssi = bundle.getInt("rssi");
				int lowBattery 
					= bundle.getBoolean("lowBattery") == true ? 1 : 0;
				int press = bundle.getBoolean("press") == true ? 1 : 0;
				int monitorMode = bundle.getInt("monitorMode");
				String result = bundle.getString("result");

				switch (monitorMode) {
				case Beacon.MonitorMode.ENTER_USER_DEF_METER:
					System.out.println("Handle ENTER_USER_DEF_METER");
					bwvc.doJsFoundBeacon(result);
					break;

				case Beacon.MonitorMode.LEAVE:
					System.out.println("Handle LEAVE");
					bwvc.doJsRemoveBeacon(uuid, major, minor);
					break;

				case Beacon.MonitorMode.ENTER:
					System.out.println("Handle ENTER");
					// nothing
					break;
				case Beacon.MonitorMode.ENTER_1_METER:
					System.out.println("Handle ENTER_1_METER");
					// nothing
					break;

				case Beacon.MonitorMode.LEAVE_1_METER:
					System.out.println("Handle LEAVE_1_METER");
					// nothing
					break;

				case Beacon.MonitorMode.LEAVE_USER_DEF_METER:
					System.out.println("Handle LEAVE_USER_DEF_METER");
					// nothing
					break;
				}

				System.out.println("HandleMessageEnd");
			}
		};
		return new Messenger(handler);
	}

	/**
	 * Handle JS
	 */
	BeaconWebViewClient bwvc = new BeaconWebViewClient() {
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			initBeaconService();
		}

		@Override
		public void doJsFoundBeacon(String json) {
			System.out.println("doJsFoundBeacon : " + json);
			if (view == null)
				return;
			view.loadUrl("javascript:foundBeacon(" + "\"" + json + "\")");
		}

		@Override
		public void doJsRemoveBeacon(String uuid, int major, int minor) {
			System.out.println("doJsRemoveBeacon");
			if (view == null)
				return;
			view.loadUrl("javascript:removeBeacon(\"" + uuid + "\",\"" + major
					+ "\",\"" + minor + "\")");
		}
	};

	private void inspectBluetoohAvailable() {

		final BluetoothLowEnergy ble = new BluetoothLowEnergy(getApplicationContext());
		if ( !ble.isEnabled() ) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Navigation");
			builder.setMessage("你的藍牙裝置未開啟，是否開啟藍芽？");
			builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int i) {
					finish();
				}
			});

			builder.setNegativeButton("確認", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int i) {
					ble.enable();
				}	
			});
			builder.show();
		}
	}
	
	private void inspectNetworkAvailable() {
		ConnectivityManager connectivityManager 
		        		= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if ( !(activeNetworkInfo != null 
				&& activeNetworkInfo.isConnected()) ) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Navigation");
			builder.setMessage("你的設備無法連上網路，請檢查你的網路狀態？");
	
			builder.setNegativeButton("確認", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int i) {
					finish();
				}	
			});
			builder.show();
		}
	}
}
