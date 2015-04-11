package com.example.model;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
	public Beacon get(int i) {
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
	
	public Beacon get(String uuid, int major, int minor) {
		String key = uuid + major + minor;
		return (Beacon) list.get(key);
	}
	
	public int register(String mac, String uuid, int major, int minor, int interval, int distance) {
		String key = mac + uuid + major + minor;
		System.out.println(key);
		if ( list.put(key,
				new Beacon(mac, uuid, major, minor, interval, distance)) == null ) {
			length++;
		} else return -1; // already register*/
		
		return 0;
	}
	
	public int unregister(String mac, String uuid, int major, int minor) {
		String key = mac + uuid + major + minor;
		if (list.remove(key) == null ) {
			return -1; // not register
		} else length--;
		return 0;
	}
	
	public void clear() {
		list.clear();
		length = 0;
	}
}
