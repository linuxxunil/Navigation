package com.example.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.model.Beacon;
import com.example.model.BeaconNotification;
import com.example.model.BeaconScanner;
import com.example.model.BeaconWebViewClient;
import com.example.model.HttpClient;
import com.example.navigation.R;
import com.example.service.BeaconService;
import com.example.service.BeaconServiceConnection;
import com.example.service.NavigationService;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends NavigationActivity {
	private final String CLASSNAME = this.getClass().getName();
	
	private Messenger activityMsger = null;
	private BeaconServiceConnection beaconServiceConn = null;
	private Handler handler = null;
	private boolean isBind = false;
	private WebView webView = null;
	static public boolean active = true;
	// for test
	private String url = "http://demo.coder.com.tw/ibeacon/api";
	private String jsURL = "http://demo.coder.com.tw/ibeacon/webview/index.html";
	private boolean tst = false;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webView = (WebView) findViewById(R.id.webview);

		activityMsger = initServiceHandler();

		doStartService();

		beaconServiceConn = new BeaconServiceConnection(activityMsger);
		
		initWebView();
		
	
		doBindService(beaconServiceConn);
	}
	

	
	private void initWebView() {
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setAppCacheEnabled(false);
		webView.setWebViewClient(bwvc);
		webView.clearCache(true);
		webView.loadUrl(jsURL);
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
		} catch (Throwable t) {

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
			private Map<String, String> parseGetConfig(String json) {
				JSONObject obj;
				Map<String, String> map = new HashMap<String, String>();
				try {
					obj = new JSONObject(json);
					if (obj.getString("result").equals("true")) {
						JSONArray array = new JSONArray(obj.getString("data"));
						map.put("size", String.valueOf(array.length()));
						
						for (int i=0; i<array.length(); i++) {	
							JSONObject data = array.getJSONObject(i);
							//map.put("mac["+i+"]", data.getString("mac"));
							map.put("dist["+i+"]", data.getString("dist"));
							map.put("uuid["+i+"]", data.getString("uuid"));
							map.put("major["+i+"]", data.getString("major"));
							map.put("minor["+i+"]", data.getString("minor"));
						}
						
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return map;
			}

			private Map<String, String> parseGetInfo(String json) {
				JSONObject obj;
				Map<String, String> map = new HashMap<String, String>();
				try {
					obj = new JSONObject(json);
					if (obj.getString("result").equals("true")) {
						JSONObject data = new JSONObject(obj.getString("data"));
						map.put("client_name", data.getString("client_name"));
						map.put("client_image", data.getString("client_image"));
						map.put("youtube", data.getString("youtube"));
						map.put("content", data.getString("content"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return map;
			}
			
			@Override
			public void handleMessage(Message msg) {
				Bundle list = msg.getData();
				boolean isExecuteFound = false;
				HttpClient http = new HttpClient();
				String parm = "";

				for (int i = 1, j = 0; i <= list.size(); i++) {

					Bundle beacon = list.getBundle(String.valueOf(i));
					int status = Integer.valueOf(beacon
							.getString("minitorStatus"));
					String uuid = beacon.getString("uuid");
					String major = beacon.getString("major");
					String rssi = beacon.getString("rssi");
					String minor = beacon.getString("minor");
					
					if (status == Beacon.Monitor.FOUND) {

						if (isExecuteFound == false) {
							parm = "lat=0&lon=0";
							isExecuteFound = true;
						}

						parm += "&data[" + j + "][major]=" + major + "&data["
								+ j + "][minor]=" + minor + "&data[" + j
								+ "][rssi]=" + rssi + "&data[" + j + "][uuid]="
								+ uuid;

					} else if (status == Beacon.Monitor.LEAVE) {
						bwvc.doJavaScript(uuid, major, minor, "removeBeacon", null);
					} else if (status == Beacon.Monitor.ENTERSCOPE) {
						String parm2 = "uuid=" + uuid + "&" + "major=" + major
								+ "&" + "minor=" + minor;
						String json = http.post(url + "/getinfo.php", parm2);
						Map result = parseGetInfo(json);
						bwvc.doJavaScript(uuid, major, minor, "foundBeacon", result);
					}
				}

				if (isExecuteFound) {
					String json = http.post(url + "/getconfig.php", parm);

					Map result = parseGetConfig(json);
					beaconServiceConn.sendBeaconConfig(result);
				}
			}
		};
		return new Messenger(handler);
	}

	/**
	 * Handle JS
	 */
	BeaconWebViewClient bwvc = new BeaconWebViewClient() {
		
		@Override
		public void doJavaScript(String uuid, String major,String minor,
								String funcName, Map<String, String> parm) {
			
			if (funcName.equals("foundBeacon")) {
				
				System.out.println("Execute JS (FoundBeacon)");
				
				if ( view == null)
					return;
				view.loadUrl("javascript:foundBeacon(" + "\'" + uuid + "_"
						+ major + "_" + minor + "\'," + "\'"
						+ parm.get("client_name") + "\'," + "\'"
						+ parm.get("client_image") + "\'," + "\'"
						+ parm.get("content") + "\'," + "\'"
						+ parm.get("youtube") + "\');");

			} else if (funcName.equals("removeBeacon")) {
				System.out.println("Execute JS (RemoveBeacon)");
				view.loadUrl("javascript:removeBeacon(" + "\'" + uuid + "_"
						+ major + "_" + minor + "\');");
			}
		}		
		
	};
}
