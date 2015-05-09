package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.activity.MainActivity;
import com.example.logging.LogCode;
import com.example.logging.Log;
import com.example.model.Beacon;
import com.example.model.BeaconConfig;
import com.example.model.BeaconList;
import com.example.model.BeaconNotification;
import com.example.model.BeaconScanner;
import com.example.navigation.R;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BeaconService extends NavigationService {
	private final String CLASSNAME = this.getClass().getName();
	public static final int MSG_REG_CLIENT = 1;
	public static final int MSG_UNREG_CLIENT = 2;
	public static final int MSG_BEACON_CONF = 3;
	
	
	private Intent intent = null;
	private Handler handler = null;
	private Messenger serviceMsger = null;
	private Messenger activityMsger = null;
	final private BeaconList beaconList = BeaconList.create();
	private long period = 1000; //

	// for test
	private void initBeaconList(BeaconList list) {
		//String path = "/storage/emulated/0/ibeacon_cfg";
		String path = "/sdcard/data/navigation.cfg";
		Map<String, String> cfg = BeaconConfig.getBeaconConfig(path);
		
		if ( cfg == null ) {
			BeaconNotification.send(getApplicationContext(),"Cfg Not Found:"+path);
		}
		
		int size = Integer.valueOf(cfg.get("size"));
		
		for (int i=0; i<size; i++) {
			list.register(cfg.get("mac["+i+"]"), 
					cfg.get("uuid["+i+"]"),
					Integer.valueOf(cfg.get("major["+i+"]")),
					Integer.valueOf(cfg.get("minor["+i+"]")));
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// for test
		initBeaconList(beaconList);
		initHandler();
		intent = new Intent(this, MainActivity.class);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return serviceMsger.getBinder();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		doScanBeacon();
		return 0;
	}
	
	@Override
	protected void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_REG_CLIENT:
					activityMsger = msg.replyTo;	
				break;
				case MSG_UNREG_CLIENT:
					activityMsger = null;
				break;
				case MSG_BEACON_CONF:
					setBeaconConfig(msg.getData());
					break;
				}
			}
		};
		serviceMsger = new Messenger(handler);
	}

	private void setBeaconConfig(Bundle data) {
		
		int size = Integer.valueOf(data.getString("size"));
		for ( int i=0; i<size; i++) {
			String mac = data.getString("mac["+i+"]");
			String uuid = data.getString("uuid["+i+"]");
			int major = Integer.valueOf(data.getString("major["+i+"]"));
			int minor = Integer.valueOf(data.getString("minor["+i+"]")); 
			
			if ( beaconList.contains(mac,uuid, major, minor)) {
				Beacon beacon = beaconList.get(mac, uuid, major, minor);
				double dist = Double.valueOf(data.getString("dist["+i+"]"));
				beacon.setNotifyDistance(dist);
			}
		}
	}
	
	private void doScanBeacon() {

		new Thread() {
			private Bundle notifyList = new Bundle();
			int count = 10;
			
			BeaconScanner bs = new BeaconScanner(getApplicationContext(),
					new LeScanCallback() {
						private int uuidStart = 9;
						private int uuidEnd = 24;
						private int majorEnd = 26;
						private int minorEnd = 28;
						private String uuid = "";
						private int major, minor;
						 

						private String getUUID(byte[] scanRecord) {
							uuid = "";
							for (int i = uuidStart; i <= uuidEnd; i++) {
								uuid += String.format("%02x", scanRecord[i]);
								if (i == 12 || i == 14 || i == 16 || i == 18)
									uuid += "-";
							}
							return uuid;
						}

						private int getMajor(byte[] scanRecord) {
							int mask = 0xFF;
							major = 0;
							major = scanRecord[majorEnd] & mask;
							major += (scanRecord[majorEnd - 1] & mask) << 8;
							return major;
						}

						private int getMinor(byte[] scanRecord) {
							int mask = 0xFF;
							minor = 0;
							minor = scanRecord[minorEnd] & mask;
							minor += (scanRecord[minorEnd - 1] & mask) << 8;

							return minor;
						}

						@Override
						public void onLeScan(BluetoothDevice device, int rssi,
								byte[] scanRecord) {
							String uuid = getUUID(scanRecord);
							int major = getMajor(scanRecord);
							int minor = getMinor(scanRecord);
							//String mac = device.getAddress();
							String mac = "";
							boolean lowBatteryFlg = false;

							
							// for test : 只判斷 uuid 
							/*
							if ( beaconList.contains(uuid) ) {
								if ( BeaconNotification.isRegister() ) {
									
									if ( count++ > 5 ) {
										BeaconNotification.send(getApplicationContext(), "Found UUID:"+uuid);
										count = 0;
									}
								}
							}*/
							
							if ( major > 127 ) {
								lowBatteryFlg = true;
								major &= 0x000000FF;
							}
										
							// beacon exists on beaconList and monitor type
							// isn't Monitor.LEAVE
							if (beaconList.contains(mac, uuid, major, minor)) {
								//Log.i(CLASSNAME, "Monitor");
								Beacon beacon = beaconList.get(mac, uuid,
														major, minor);
								
								if ( lowBatteryFlg) beacon.setBatteryFlg(true);
								else beacon.setBatteryFlg(false);
								
								int status = beacon.getMonitorStatus();
								// reset count
								beacon.resetCount();
								
								if (status == Beacon.Monitor.LEAVE) {
									//Log.i(CLASSNAME, "FOUND");
									beacon.setMonitorStatus(Beacon.Monitor.FOUND);
									
									//Log.i(CLASSNAME, "add FOUND NList");
									
									notifyList.putBundle(
											String.valueOf(notifyList.size()+1),
												toBundle(beacon, rssi));
												
									
								} else {
									if (status == Beacon.Monitor.FOUND) {
										double distance = beacon
												.toDistance(rssi);
										
										// when distance < define value , then
										// send notification
										if (distance <= beacon
												.getNotifyDistance()) {
											
											beacon.setMonitorStatus(Beacon.Monitor.ENTERSCOPE);
											
											//Log.i(CLASSNAME,"add ENTERSCOPE NList");
											notifyList.putBundle(
													String.valueOf(notifyList.size()+1),
														toBundle(beacon, rssi));
										}
									}
								}
							} else {
								//Log.i(CLASSNAME, "The beacon hasn't register:"+uuid+major+minor);
							}
						}
					});
			
			private Bundle toBundle(Beacon beacon, int rssi) {
				Bundle bundle = new Bundle();
				bundle.putString("mac", beacon.getMAC());
				bundle.putString("uuid", beacon.getUUID());
				
				int major = beacon.getMajor();
				bundle.putString("major", String.valueOf(beacon.getMajor()));
				bundle.putString("minor", String.valueOf(beacon.getMinor()));
				bundle.putString("minitorStatus", String.valueOf(beacon.getMonitorStatus()));
				if ( rssi != -1 ) bundle.putString("rssi", String.valueOf(rssi));
				return bundle;
			}

			private void doRemoveLeaveBeacon() {
				for (int i = 0; i < beaconList.length; i++) {
					
					Beacon beacon = beaconList.get(i+1);
					
					if (beacon != null
							&& beacon.getMonitorStatus() 
									!= Beacon.Monitor.LEAVE) {
						int c = beacon.doCount();
						
						if (c <= 0) {
							beacon.setMonitorStatus(Beacon.Monitor.LEAVE);
							Log.e(this, LogCode.WAR_ADD_LEAVE_NLIST);
							notifyList.putBundle(
									String.valueOf(notifyList.size()+1),
									toBundle(beacon, -1));
						}
					} else {
						// Beacon type is leave , don't do anything 
					}
				}
			}

			@Override
			public void run() {
				while (true) {
					try {
						if ( notifyList.size() > 0)
							notifyList = new Bundle();
						
						bs.start();
						Thread.sleep(period);
						bs.stop();
						doRemoveLeaveBeacon();
						
						if ( notifyList.size() > 0 )
							sendMessageToActivity(notifyList);
							
					} catch (Exception e) {
						//
					}
				}
			}
		}.start();
	} 
	
	private int sendMessageToActivity(Bundle list) {
		try {
			Message msg = Message.obtain();
			msg.what = 0;
			msg.setData(list);
			activityMsger.send(msg);
		} catch (RemoteException e) {
			return Log.e(this, LogCode.ERR_SEND_MSG_TO_ACTIVITY_FAIL);
		}
		return LogCode.success;
	}
}