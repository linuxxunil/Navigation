package com.example.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.model.Beacon;
import com.example.model.BeaconBase;
import com.example.model.HttpClient;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class HttpService extends Thread{
	private final String CLASSNAME = this.getClass().getName();
	private Messenger activityMsger = null;
	private BlockingQueue<Request> queue = new ArrayBlockingQueue<Request>(10);
	private String udid="1234567890";
	private HttpClient http = new HttpClient();
	private String apiURL = "http://demo.coder.com.tw/ibeacon/api"; 
	private boolean running = true;
	
	@Override
	public void run() {
		Request request ;
		System.out.println("HttpService start");

		while(running) { 
			Beacon beacon = null;
			try {
				request = queue.take();
				beacon = request.beacon;
				System.out.println("get request");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			double meter = BeaconBase.toMeter(beacon.getRSSI());
			String result = "";
					
			if ( beacon.getRSSI() == 0 ) {
				beacon.setMonitorMode(Beacon.MonitorMode.LEAVE);
				result = doPostLevave(beacon, (int) meter);
				sendMessageToActivity(toBundle(beacon, result));
				continue;
			} 
					
			switch (beacon.getMonitorMode()) {
			case Beacon.MonitorMode.LEAVE:
				Log.i(CLASSNAME,"Mode : ENTER");
				beacon.setMonitorMode(Beacon.MonitorMode.ENTER);
				result = doPostGetConfig(beacon, (int) meter);
				Map map = parseGetConfig(result);
				if ( map != null)
					beacon.setUserDefMeter(
						Double.valueOf((String) map.get("dist[0]")));
				break;

			case Beacon.MonitorMode.ENTER:
				// when distance < define value , then
				// send notification
				if (meter < beacon.getUserDefMeter()) {
					Log.i(CLASSNAME,"Mode : ENTER_USER_DEF_METER");
					beacon.setMonitorMode(Beacon.MonitorMode.ENTER_USER_DEF_METER);
					result = doPostGetInfo(beacon, (int) meter);
					
					sendMessageToActivity(toBundle(beacon, result));
				}
						
				break;

			case Beacon.MonitorMode.ENTER_USER_DEF_METER:	
				if ( meter < 1 ) { // less than 1 meter
					beacon.setMonitorMode(Beacon.MonitorMode.ENTER_1_METER);
					result = doPostEnter1Meter(beacon, (int) meter);
							
				} else if ( meter >= beacon.getUserDefMeter() ){
					beacon.setMonitorMode(Beacon.MonitorMode.LEAVE_USER_DEF_METER);
					result = doPostLevave(beacon, (int) meter);				
				}
				break;
						
			case Beacon.MonitorMode.ENTER_1_METER:
				if ( meter >= 1 ) { // more than 1 meter
					beacon.setMonitorMode(Beacon.MonitorMode.LEAVE_1_METER);
					result = doPostLevave(beacon, (int) meter);
				}
				break;

			case Beacon.MonitorMode.LEAVE_1_METER:
				if ( meter < 1 ) {	// less than 1 meter
					beacon.setMonitorMode(Beacon.MonitorMode.ENTER_1_METER);
					result = doPostEnter1Meter(beacon, (int) meter);					
				} else if ( meter >= beacon.getUserDefMeter() ) {
					beacon.setMonitorMode(Beacon.MonitorMode.LEAVE_USER_DEF_METER);
					result = doPostLevave(beacon, (int) meter);
				}	
				break;
			case Beacon.MonitorMode.LEAVE_USER_DEF_METER:
				if ( meter < beacon.getUserDefMeter() ) {
					beacon.setMonitorMode(Beacon.MonitorMode.ENTER_USER_DEF_METER);
					result = doPostLevave(beacon, (int) meter);
				} 
				break;
					
			}
		}
		
		System.out.println("HttpService stop");
	}
	
	private String doPostGetConfig(Beacon beacon, int meter) {
		String parm = "lat=0&lon=0&udid=" + udid 
				+ "&data[0][major]=" + beacon.getMajor()  
				+ "&data[0][minor]=" + beacon.getMinor()
				+ "&data[0][meter]=" + (int) meter
				+ "&data[0][uuid]=" + beacon.getUUID() 
				+ "&data[0][mac]=" + beacon.getMAC()
				+ "&data[0][low_battery]=0"
				+ "&data[0][press]=0" ;
				
		return http.post(apiURL + "/getconfig.php", parm);
	}
	
	private String doPostGetInfo(Beacon beacon, int meter) {
		String parm = "udid=" + udid 
				+ "&uuid=" + beacon.getUUID()
				+ "&major=" + beacon.getMajor() 
				+ "&minor=" + beacon.getMinor()
				+ "&mac=" + beacon.getMAC() 
				+ "&meter=" + meter;
				
		return http.post(apiURL + "/getinfo.php",parm);
	}
	
	private String doPostEnter1Meter(Beacon beacon, int meter) {
		String parm = "udid=" + udid 
				+ "&uuid=" + beacon.getUUID()
				+ "&major=" + beacon.getMajor()
				+ "&minor=" + beacon.getMinor()
				+ "&mac=" + beacon.getMAC() 
				+ "&meter="+ (int) meter
				+ "&type=1";
				
		return http.post(apiURL + "/setstatus.php", parm);	
	}
	
	private String doPostLevave(Beacon beacon, int meter) {
		String parm = "udid=" + udid 
				+ "&uuid=" + beacon.getUUID()
				+ "&major=" + beacon.getMajor()
				+ "&minor=" + beacon.getMinor()
				+ "&mac=" + beacon.getMAC() 
				+ "&meter="+ meter
				+ "&type=3";
		return http.post(apiURL + "/setstatus.php", parm);
	}
	
	protected class Request {
		 protected Beacon beacon ;
		 protected Request(Beacon beacon) {
			 this.beacon = beacon;
		 }
	}
	
	public boolean post(Beacon beacon) {
		return queue.offer(new Request(beacon));
	}
	
	private Bundle beaconToBundle(Beacon beacon, int rssi) {
		Bundle bundle = new Bundle();
		bundle.putString("mac", beacon.getMAC());
		bundle.putString("uuid", beacon.getUUID());
		bundle.putInt("major", beacon.getMajor());
		bundle.putInt("minor", beacon.getMinor());
		bundle.putInt("monitorMode", beacon.getMonitorMode());
		bundle.putBoolean("lowBattery", beacon.getBatteryFlg());
		bundle.putBoolean("press", beacon.getPressFlg());
		if (rssi != -1)
			bundle.putInt("rssi", rssi);
		return bundle;
	}
	
	public void setActivityMsger(Messenger msger) {
		activityMsger = msger;
	}
	
	private void sendMessageToActivity(Bundle bundle) {
		try {
			Message msg = Message.obtain();
			msg.what = 0;
			msg.setData(bundle);
			if ( activityMsger != null )
			activityMsger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private Map<String, String> parseGetConfig(String json) {
		if (json == null || json.isEmpty())
			return null;
		JSONObject obj;
		Map<String, String> map = new HashMap<String, String>();
		try {
			obj = new JSONObject(json);
			if (obj.getString("result").equals("true")) {
				JSONArray array = new JSONArray(obj.getString("data"));
				map.put("size", String.valueOf(array.length()));

				for (int i = 0; i < array.length(); i++) {
					JSONObject data = array.getJSONObject(i);

					map.put("dist[" + i + "]", data.getString("dist"));
					map.put("uuid[" + i + "]", data.getString("uuid"));
					map.put("major[" + i + "]", data.getString("major"));
					map.put("minor[" + i + "]", data.getString("minor"));
				}
			}
		} catch (JSONException e) {
			map = null;
			e.printStackTrace();
		}
		return map;
	}
	
	private Bundle toBundle(Beacon beacon, String result) {
		Bundle bundle = new Bundle();
		bundle.putString("mac", beacon.getMAC());
		bundle.putString("uuid", beacon.getUUID());
		bundle.putInt("major", beacon.getMajor());
		bundle.putInt("minor", beacon.getMinor());
		bundle.putInt("monitorMode", beacon.getMonitorMode());
		bundle.putBoolean("lowBattery", beacon.getBatteryFlg());
		bundle.putBoolean("press", beacon.getPressFlg());
		bundle.putInt("rssi", beacon.getRSSI());
		bundle.putString("result", result);
		return bundle;
	}
}
