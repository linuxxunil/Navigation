package com.example.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

public class BeaconList {
	private ArrayList<String> uuidList = new ArrayList<String>();
	private Map beaconList = new Hashtable<String,Beacon>();
	
	static public BeaconList create() {
		return new BeaconList();
	}
	
	public boolean containsBeacon(String mac,String uuid,int major,int minor) {
		String key = mac + uuid + major + minor;
		return beaconList.containsKey(key);
	}
	
	public boolean containsUUID(String uuid) {
		return uuidList.contains(uuid);
	}
	
	/**
	 * 
	 * @param i : start is one
	 * @return beacon,or null if i greater than length
	 */
	public Beacon getBeacon(int i) {
		if ( i > beaconList.size() ) return null;
		Beacon beacon = null;
		int j = 1;
		for ( Object key : beaconList.keySet() ) {
			if ( i == j++ ) {
				return (Beacon) beaconList.get(key);
			}
		}
		return null;
	}
	
	public Beacon getBeacon(String mac, String uuid, int major, int minor) {
		String key = mac + uuid + major + minor;
		return (Beacon) beaconList.get(key);
	}
	
	public boolean registerUUID(String uuid) {
		return uuidList.add(uuid);
	}
	
	public boolean unregisterUUID(String uuid) {
		return uuidList.remove(uuid);
	}
	
	public boolean registerBeacon(String mac, String uuid, int major, int minor) {
		String key = mac + uuid + major + minor;
		return (beaconList.put(key,
				new Beacon(mac, uuid, major, minor)) == null)?false:true;
	}
	
	public boolean unregisterBeacon(String mac, String uuid, int major, int minor) {
		String key = mac + uuid + major + minor;
		return (beaconList.remove(key) == null)? false:true;
	}
	
	public void beaconClear() {
		beaconList.clear();
	}
	
	public int beaconLength() {
		return beaconList.size();
	}
}
