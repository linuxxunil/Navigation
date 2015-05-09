package com.example.model;

import java.util.Hashtable;
import java.util.Map;

import com.example.logging.Log;
import com.example.logging.LogBeacon;
import com.example.logging.LogCode;

public class BeaconList {
	private Map list = new Hashtable<String,Beacon>();
	public int length = 0;
	static public BeaconList create() {
		return new BeaconList();
	}
	
	public boolean contains(String mac,String uuid,int major,int minor) {
		String key = mac + uuid + major + minor;
		return list.containsKey(key);
	}
	
	public boolean contains(String uuid) {
		for (Object key :  list.keySet()) {
			String value = (String)key;
			if ( value.startsWith(uuid) ) {
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(String uuid,int major,int minor) {
		return list.containsKey(uuid+major+minor);
	}
	
	/**
	 * 
	 * @param i : start is one
	 * @return beacon,or null if i greater than length
	 */
	public LogBeacon get(int i) {
		if ( i > length ) return null;
		Beacon beacon = null;
		int j = 1;
		for ( Object key : list.keySet() ) {
			if ( i == j++ ) {
				return (Beacon) list.get(key);
			}
		}
		return null;
	}
	
	public Beacon get(String mac, String uuid, int major, int minor) {
		String key = mac + uuid + major + minor;
		return (Beacon) list.get(key);
	}
	
	public int register(String mac, String uuid, int major, int minor) {
		String key = mac + uuid + major + minor;
		if ( list.put(key,
				new Beacon(mac, uuid, major, minor)) == null ) {
			length++;
		} else return Log.e(this, LogCode.INF_REGISTERED); // already register*/
		
		return LogCode.success;
	}
	
	public int unregister(String mac, String uuid, int major, int minor) {
		String key = mac + uuid + major + minor;
		if (list.remove(key) == null ) {
			return Log.e(this, LogCode.INF_NOT_REGISTERED); // not register
		} else length--;
		return LogCode.success;
	}
	
	public void clear() {
		list.clear();
		length = 0;
	}
}
