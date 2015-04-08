package com.example.activity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.model.Beacon;
import com.example.model.BeaconScanner;
import com.example.model.HttpClient;
import com.example.navigation.R;
import com.example.service.BeaconService;
import com.example.service.NavigationService;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
	private Messenger serviceMsger = null;
	private Handler handler = null;
	private boolean isBind = false;
	private WebView webView = null;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webView = (WebView) findViewById(R.id.webview);

		initHandler();

		//doStartService();

		//doBindService();
		initWebView();
		
		String res = "" ;
	
		HttpClient http = new HttpClient();
		res = http.post("http://demo.coder.com.tw/ibeacon/api/getinfo.php", 
							"major=10&minor=1&uuid=15345164-67AB-3E49-F9D6-E29000000007");		
		
		showContentToWebView("http://demo.coder.com.tw/ibeacon/webview/index.html",
				"15345164-67AB-3E49-F9D6-E29000000007",10,10,"foundBeacon", res );
		
	}
	
	private void showContentToWebView(String url,String uuid, int major,int minor, String funcName, String input) {
		bwvc.setJSONContent(input);
		webView.loadUrl(url+"?funcName="+funcName
							+"&uuid="+uuid
							+"&major="+major
							+"&minor="+minor);
	}
	
	private void initWebView() {
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(bwvc);
	}
	
	class BeaconWebViewClient extends WebViewClient{
		protected String json = "";
		
		protected Map<String, String> parseFoundBeaconJSON() {
			JSONObject obj;
			Map<String, String> map = new HashMap<String, String>();
			try {
				obj = new JSONObject(json);
				if ( obj.getString("result").equals("true")) {
					JSONObject data = new JSONObject(obj.getString("data"));
					map.put("client_name", data.getString("client_name"));
					map.put("client_image", data.getString("client_image"));
					map.put("youtube", data.getString("youtube"));
					map.put("content", data.getString("content"));
				}
			} catch (JSONException e ) {
				e.printStackTrace();
			}
			return map;
		}
				
		public void setJSONContent(String ct) {
			json = ct;
		}
	}
	
	BeaconWebViewClient bwvc = new BeaconWebViewClient() {
		
		private Map<String, String> getQueryMap(String query)
		{
		    String[] params = query.split("&");
		    Map<String, String> map = new HashMap<String, String>();
		    for (String param : params)
		    {
		        String name = param.split("=")[0];
		        String value = param.split("=")[1];
		        map.put(name, value);
		    }
		    return map;
		}
		
		@Override
		 public void onPageFinished(WebView view, String url) {
			String[] data = url.split("\\?");
			Map parm = getQueryMap(data[1]);
			String funcName = (String) parm.get("funcName");
			String uuid = (String) parm.get("uuid");
			String major = (String) parm.get("major");
			String minor = (String) parm.get("minor");
			
			
			if ( funcName.equals("foundBeacon") ) {
				Map<String, String> map = parseFoundBeaconJSON();
				view.loadUrl("javascript:foundBeacon("
						+ "\'" + uuid + "_" + major + "_" + minor  + "\',"
						+ "\'" + map.get("client_name") + "\',"
						+ "\'" + map.get("client_image")+ "\',"
						+ "\'" + map.get("content")	 	+ "\',"
						+ "\'" + map.get("youtube") 	+ "\');");	
					
			} else if ( funcName.equals("removeBeacon") ) {
				
			}
		}
	};

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

	private void doBindService() {
		Intent bindIntent = new Intent(this, BeaconService.class);
		bindService(bindIntent, connection, BIND_AUTO_CREATE);
		isBind = true;
	}

	private void doUnbindService() {
		if (isBind) {
			// If we have received the service, and hence registered with it,
			// then now is the time to unregister.
			if (serviceMsger != null) {
				try {
					Message msg = Message.obtain(null,
							BeaconService.MSG_UNREG_CLIENT);
					msg.replyTo = activityMsger;
					serviceMsger.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has
					// crashed.
				}
			}
			// Detach our existing connection.
			unbindService(connection);
			isBind = false;
		}
	}

	public class JavaScriptInterface {
	    public void fun1() {
	        //Android 要執行的程式碼
	    }
	}

	
	static public class Restful {
		static public int FOUND;
		static public int LEAVE;
		
	}
 	
	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Bundle list = msg.getData();
				String cv = "";
				
				
				for (int i=1; i<=list.size(); i++) {
					
					Bundle beacon = list.getBundle(String.valueOf(i));
					int len = beacon.keySet().size();
					
					int status = Integer.valueOf(beacon.getString("minitorStatus"));
						
					if ( status == Beacon.Monitor.FOUND ) {
						
					} else if ( status == Beacon.Monitor.LEAVE) {
						
					} else if ( status == Beacon.Monitor.ENTERSCOPE ) {
						
					}
					
					System.out.println(cv);
				}
			}
		};
		activityMsger = new Messenger(handler);
	}

	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			serviceMsger = new Messenger(service);
			try {
				Message msg = Message.obtain();
				msg.what = BeaconService.MSG_REG_CLIENT;
				msg.replyTo = activityMsger;
				serviceMsger.send(msg);

			} catch (RemoteException e) {
				Log.e(CLASSNAME, e.getMessage());
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};
}
