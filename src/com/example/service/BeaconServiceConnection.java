package com.example.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.example.model.Beacon;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BeaconServiceConnection implements ServiceConnection {
	private String CLASSNAME = "BeaconServiceConnection";
	private Messenger serviceMsger = null;
	private Messenger activityMsger = null;
	
	public BeaconServiceConnection(Messenger activityMsger) {
		this.activityMsger = activityMsger; 
	}
	
	private int sendMessage(int what, Bundle data) {
		
		if (serviceMsger == null) {
			return -1;
		} else {
			try {
				Message msg = Message.obtain();
				msg.what = what;
				msg.replyTo = activityMsger;
					msg.setData(data);
				serviceMsger.send(msg);

			} catch (RemoteException e) {
				Log.e(CLASSNAME, e.getMessage());
			}
		}
		return 0;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {		
		serviceMsger = new Messenger(service);
		registerActivity();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		unRegisterActivity();
	}
	
	private void registerActivity() {
		sendMessage(BeaconService.MSG_REG_CLIENT,null);
	}
	
	private void unRegisterActivity() {
		sendMessage(BeaconService.MSG_REG_CLIENT,null);
	}
		
	public void sendBeaconConfig(Map<String, String> content) {
		Bundle bundle = new Bundle();
		int i = 0;
		for ( String key : content.keySet() ) {
			bundle.putString(key, content.get(key));
	    }
		
		sendMessage(BeaconService.MSG_BEACON_CONF, bundle);
	}
	
};
	