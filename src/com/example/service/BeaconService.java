package com.example.service;

import java.util.Map;

import com.example.activity.MainActivity;
import com.example.model.Beacon;
import com.example.model.BeaconConfig;
import com.example.model.BeaconList;
import com.example.model.BeaconNotification;
import com.example.model.BeaconScanner;
import com.example.model.HttpClient;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
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
	private HttpService httpService = null;

	// for test
	private void initBeaconList(BeaconList list) {
		//String path = "/storage/emulated/0/ibeacon_cfg";
		String path = "/sdcard/data/navigation.cfg";
		Map<String, String> cfg = BeaconConfig.getBeaconConfig(path);

		if (cfg == null) {
			BeaconNotification.send(getApplicationContext(), "Cfg Not Found:"
					+ path);
		}

		int size = Integer.valueOf(cfg.get("size"));

		for (int i = 0; i < size; i++) {
			list.registerUUID(cfg.get("uuid[" + i + "]").toUpperCase());
		}
	}
	// for test end
	
	@Override
	public void onCreate() {
		super.onCreate();
		// for test
		initBeaconList(beaconList);
		// for test end
		initHandler();
		intent = new Intent(this, MainActivity.class);
		httpService = new HttpService();
		httpService.start();
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
					try {
						activityMsger = msg.replyTo;
						httpService.setActivityMsger(activityMsger);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case MSG_UNREG_CLIENT:
					activityMsger = null;
					break;
				default:
					// ignore
					break;
				}
			}
		};
		serviceMsger = new Messenger(handler);
	}

	private void doScanBeacon() {
		new Thread() {
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
							return uuid.toUpperCase();
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
							String udid = "1234567890";
							String uuid = getUUID(scanRecord);
							int major = getMajor(scanRecord);
							int minor = getMinor(scanRecord);
							String mac = device.getAddress();
							boolean lowBatteryFlg = false;
							boolean pressFlg = false;

							// check what battery status is low
							if ((major & (0x00008000)) > 0)
								lowBatteryFlg = true;

							// check what test button is press down
							if ((major & (0x00004000)) > 0)
								pressFlg = true;

							/*
							 * System.out.println("uuid:"+uuid);
							 * System.out.println("major:"+major);
							 * System.out.println("minor:"+minor);
							 * System.out.println("mac:"+mac);
							 * System.out.println
							 * ("lowBatteryFlg:"+lowBatteryFlg);
							 * System.out.println("pressFlg:"+pressFlg);
							 */
							// beacon exists on beaconList and monitor type
							// isn't Monitor.LEAVE
							major &= 0x000000FF;

							if (beaconList.containsUUID(uuid)) {
								Log.i(CLASSNAME, "Monitor");

								if (!beaconList.containsBeacon(mac, uuid,major, minor))
									beaconList.registerBeacon(mac, uuid, major, minor);

								Beacon beacon = beaconList.getBeacon(mac, uuid,major, minor);
								beacon.setBatteryFlg(lowBatteryFlg);
								beacon.setPressFlg(pressFlg);
								beacon.setRSSI(rssi);
								
								// reset count
								beacon.resetCount();
								
								// send to HttpService
								httpService.post(beacon);
								
								
							} else {
								Log.i(CLASSNAME, "The UUID hasn't register:" + uuid);
							}
						}
					});

			private void doRemoveLeaveBeacon() {
				for (int i = 0; i < beaconList.beaconLength(); i++) {
					Beacon beacon = beaconList.getBeacon(i + 1);
					if (beacon != null
							&& beacon.getRSSI() != 0
							&& beacon.getMonitorMode() != Beacon.MonitorMode.LEAVE) {
						int c = beacon.doCount();
						System.out.println(c);
						if (c <= 0) {
							Log.i(CLASSNAME, "add Leave NList");
							beacon.setRSSI(0);
							httpService.post(beacon);
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
						bs.start();
						Thread.sleep(period);
						bs.stop();
						doRemoveLeaveBeacon();	
					} catch (Exception e) {
						e.getStackTrace();
					}
				}
			}
		}.start();
	}
}